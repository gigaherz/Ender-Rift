package dev.gigaherz.enderrift.rift.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.rift.IRiftChangeListener;
import dev.gigaherz.enderrift.rift.RiftChangeHook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RiftInventory implements ResourceHandler<ItemResource>
{
    private final RiftHolder holder;
    private final ArrayList<RiftSlot> slots = new ArrayList<>();
    private final ArrayList<RiftSlotJournal> slotsJournal = new ArrayList<>();
    private final RiftInventoryJournal inventoryJournal = new RiftInventoryJournal();

    private final List<Reference<? extends IRiftChangeListener>> listeners = Lists.newArrayList();
    private final ReferenceQueue<IRiftChangeListener> pendingRemovals = new ReferenceQueue<>();
    private final List<RiftChangeHook> hooks = new ArrayList<>();
    private boolean isDirty;

    RiftInventory(final RiftHolder holder)
    {
        this.holder = Objects.requireNonNull(holder);
        updateStacksSize();
    }

    private void updateStacksSize() {
        slotsJournal.ensureCapacity(slots.size());
        // Add missing entries
        while (slotsJournal.size() < slots.size()) {
            slotsJournal.add(new RiftSlotJournal(slotsJournal.size()));
        }
        // Remove superfluous entries
        if (slotsJournal.size() > slots.size()) {
            slotsJournal.subList(slots.size(), slotsJournal.size()).clear();
        }
    }

    private void recordAndAddSlot(ItemResource resource, int amount, TransactionContext transaction)
    {
        inventoryJournal.updateSnapshots(transaction);
        var slot = new RiftSlot(resource, amount);
        slots.add(slot);
        afterAdd(slot);
        onContentsChanged();
    }

    private void recordAndAddCount(int index, RiftSlot slot, long count, TransactionContext transaction)
    {
        slotsJournal.get(index).updateSnapshots(transaction);
        slot.addCount(count);
        onContentsChanged();
    }

    private void recordAndSubtractCount(int index, RiftSlot slot, long count, TransactionContext transaction)
    {
        slotsJournal.get(index).updateSnapshots(transaction);
        slot.subtractCount(count);
        onContentsChanged();
    }

    private void recordAndRemoveSlot(int index, RiftSlot slot, TransactionContext transaction)
    {
        inventoryJournal.updateSnapshots(transaction);
        slots.remove(index);
        afterRemove(slot);
        onContentsChanged();
    }


    public boolean isDirty()
    {
        return this.isDirty;
    }

    public void clearDirty()
    {
        this.isDirty = false;
    }

    public UUID getId()
    {
        return holder.getId();
    }

    protected CompoundTag save(HolderLookup.Provider lookup)
    {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        RiftSlot[] slots = this.slots.toArray(RiftSlot[]::new);
        for (RiftSlot slot : slots)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Count", slot.getCount());
            tag.put("Item", ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, lookup), slot.getResource().toStack()).getOrThrow());
            list.add(tag);
        }
        root.put("Contents", list);
        return root;
    }

    protected void load(CompoundTag root, HolderLookup.Provider lookup)
    {
        if (!slots.isEmpty())
        {
            slots.clear();
            afterClear();
        }

        root.getList("Contents").ifPresent(list -> {
            for (Tag rawTag : list)
            {
                CompoundTag tag = (CompoundTag) rawTag;
                long count = tag.getLongOr("Count", 0);
                if (count == 0) continue;
                var itemCompound = tag.getCompound("Item").orElseThrow();
                if (itemCompound.isEmpty()) continue;
                itemCompound.putInt("count", 1);
                ItemStack.OPTIONAL_CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, lookup), itemCompound).ifSuccess(pair -> {
                    ItemStack stack = pair.getFirst();
                    RiftSlot slot = new RiftSlot(ItemResource.of(stack), count);
                    slots.add(slot);
                    afterAdd(slot);
                });
            }
        });

        onContentsChanged();
    }

    @Override
    public int size()
    {
        return slots.size() + 1;
    }

    private RiftSlot getSlotSafe(int index)
    {
        Objects.checkIndex(index, size());
        return slots.get(index);
    }

    @Override
    public ItemResource getResource(int index)
    {
        if (index == slots.size())
            return ItemResource.EMPTY;
        return getSlotSafe(index).getResource();
    }

    @Override
    public long getAmountAsLong(int index)
    {
        return getSlotSafe(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource)
    {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, ItemResource resource)
    {
        return true;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        for (int i = 0; i < slots.size(); i++)
        {
            if (tryCombineStacks(i, resource, amount, transaction))
            {
                return amount;
            }
        }

        recordAndAddSlot(resource, amount, transaction);
        return amount;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (index == slots.size())
        {
            recordAndAddSlot(resource, amount, transaction);
            return amount;
        }

        if (tryCombineStacks(index, resource, amount, transaction))
        {
            return amount;
        }

        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        for (int i = 0; i < slots.size(); i++)
        {
            var slot = slots.get(i);
            var sample = slot.getResource();
            if (isSameItemSameComponents(sample, resource))
            {
                return extractInternal(i, slot, amount, transaction);
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int wanted, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, wanted);

        if (index >= slots.size())
        {
            return 0;
        }

        var slot = slots.get(index);
        var sample = slot.getResource();
        if (isSameItemSameComponents(sample, resource))
        {
            return extractInternal(index, slot, wanted, transaction);
        }

        return 0;
    }

    private int extractInternal(int index, RiftSlot slot, int wanted, TransactionContext transaction)
    {
        long count = slot.getCount();
        if (wanted >= count)
        {
            recordAndRemoveSlot(index, slot, transaction);
            return (int) count;
        }
        else
        {
            recordAndSubtractCount(index, slot, wanted, transaction);
            return wanted;
        }
    }

    private boolean tryCombineStacks(int index, ItemResource resource, long count, TransactionContext transaction)
    {
        var slot = slots.get(index);
        var sample = slot.getResource();
        if (isSameItemSameComponents(sample, resource))
        {
            recordAndAddCount(index, slot, count, transaction);
            return true;
        }
        return false;
    }

    public static boolean isSameItemSameComponents(ItemResource a, ItemResource b) {
        return a.is(b.getItem()) && (a.isEmpty() && b.isEmpty() || Objects.equals(a.getComponents(), b.getComponents()));
    }

    // Listeners, Hooks, and whatnot

    protected void onContentsChanged()
    {
        isDirty = true;
        updateStacksSize();
        walkListeners(IRiftChangeListener::onRiftChanged);
    }

    //region Listeners
    public void addWeakListener(IRiftChangeListener e)
    {
        listeners.add(new WeakReference<>(e, pendingRemovals));
    }

    private void walkListeners(Consumer<IRiftChangeListener> consumer)
    {
        for (Reference<? extends IRiftChangeListener> ref = pendingRemovals.poll(); ref != null; ref = pendingRemovals.poll())
        {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends IRiftChangeListener>> iterator = listeners.iterator(); iterator.hasNext(); )
        {
            Reference<? extends IRiftChangeListener> reference = iterator.next();
            IRiftChangeListener listener = reference.get();
            if (listener == null || listener.isInvalid())
            {
                iterator.remove();
                continue;
            }
            consumer.accept(listener);
        }
    }

    public void locateListeners(Level level, Consumer<BlockPos> locationConsumer)
    {
        walkListeners(listener -> {
            if (!level.dimension().equals(listener.getRiftLevel().map(Level::dimension).orElse(null)))
            {
                return;
            }
            locationConsumer.accept(listener.getLocation());
        });
    }
    //endregion Listeners

    //region Hooks
    @ApiStatus.Internal
    public void addHook(RiftChangeHook hook)
    {
        hooks.add(hook);
        for (var slot : slots)
        {
            hook.onAdd(slot);
        }
    }

    private void afterClear()
    {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < hooks.size(); i++)
        {
            hooks.get(i).onClear();
        }
    }

    private void afterRemove(RiftSlot slot)
    {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < hooks.size(); i++)
        {
            hooks.get(i).onRemove(slot);
        }
    }

    private void afterAdd(RiftSlot slot)
    {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < hooks.size(); i++)
        {
            hooks.get(i).onAdd(slot);
        }
    }
    //endregion Hooks

    //region Features
    private final Map<Class<?>, Object> attachedFeatures = new IdentityHashMap<>();

    public <T> T getOrCreateFeature(Class<T> featureClass, Function<RiftInventory, T> featureFactory)
    {
        //noinspection unchecked
        return (T) attachedFeatures.computeIfAbsent(featureClass, key -> featureFactory.apply(this));
    }
    //endregion Features

    private class RiftInventoryJournal extends SnapshotJournal<ArrayList<RiftSlot>>
    {
        private RiftInventoryJournal() {
        }

        @Override
        protected ArrayList<RiftSlot> createSnapshot() {
            return new ArrayList<>(slots);
        }

        @Override
        protected void revertToSnapshot(ArrayList<RiftSlot> snapshot) {
            slots.clear();
            slots.addAll(snapshot);
            updateStacksSize();
        }

        @Override
        protected void onRootCommit(ArrayList<RiftSlot> originalState) {
            onContentsChanged();
        }
    }

    private class RiftSlotJournal extends SnapshotJournal<RiftSlot>
    {
        private final int index;

        private RiftSlotJournal(int index) {
            this.index = index;
        }

        @Override
        protected RiftSlot createSnapshot() {
            return slots.get(index).copy();
        }

        @Override
        protected void revertToSnapshot(RiftSlot snapshot) {
            slots.set(index, snapshot);
        }

        @Override
        protected void onRootCommit(RiftSlot originalState) {
            onContentsChanged();
        }
    }
}
