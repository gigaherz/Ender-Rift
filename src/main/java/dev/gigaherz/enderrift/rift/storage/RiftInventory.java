package dev.gigaherz.enderrift.rift.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.rift.ILongItemHandler;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RiftInventory implements ILongItemHandler
{

    final List<Reference<? extends IRiftChangeListener>> listeners = Lists.newArrayList();
    final ReferenceQueue<IRiftChangeListener> pendingRemovals = new ReferenceQueue<>();
    private final List<RiftSlot> slots = Lists.newArrayList();
    private final RiftHolder holder;
    private final List<RiftChangeHook> hooks = new ArrayList<>();
    private boolean isDirty;

    RiftInventory(final RiftHolder holder)
    {
        this.holder = Objects.requireNonNull(holder);
    }

    public UUID getId()
    {
        return holder.getId();
    }

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

    protected void onContentsChanged()
    {
        isDirty = true;
        walkListeners(IRiftChangeListener::onRiftChanged);
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

    CompoundTag save(HolderLookup.Provider lookup)
    {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        RiftSlot[] slots = this.slots.toArray(RiftSlot[]::new);
        for (RiftSlot slot : slots)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Count", slot.getCount());
            tag.put("Item", ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, lookup), slot.getSample().copyWithCount(1)).getOrThrow());
            list.add(tag);
        }
        root.put("Contents", list);
        return root;
    }

    void load(CompoundTag root, HolderLookup.Provider lookup)
    {
        if (slots.size() > 0)
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
                    RiftSlot slot = new RiftSlot(stack, count);
                    slots.add(slot);
                    afterAdd(slot);
                });
            }
        });
        onContentsChanged();
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

    @Override
    public int getSlots()
    {
        return slots.size() + 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int index)
    {
        if (index >= slots.size())
        {
            return ItemStack.EMPTY;
        }
        return slots.get(index).getSample();
    }

    @Override
    public @NotNull ItemStack insertItem(int index, @NotNull ItemStack stack, boolean simulate)
    {
        if (simulate)
        {
            return ItemStack.EMPTY;
        }
        try
        {
            if (index < slots.size() && index >= 0 && tryCombineStacks(index, stack))
            {
                return ItemStack.EMPTY;
            }
            for (int i = 0; i < slots.size(); i++)
            {
                if (index == i || !tryCombineStacks(i, stack))
                {
                    continue;
                }
                return ItemStack.EMPTY;
            }
            var slot = new RiftSlot(stack);
            slots.add(slot);
            afterAdd(slot);
            return ItemStack.EMPTY;
        }
        finally
        {
            onContentsChanged();
        }
    }

    private boolean tryCombineStacks(int index, ItemStack stack)
    {
        RiftSlot slot = slots.get(index);
        ItemStack sample = slot.getSample();
        if (ItemStack.isSameItemSameComponents(sample, stack))
        {
            slot.addCount(stack.getCount());
            return true;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack extractItem(int index, int wanted, boolean simulate)
    {
        if (index >= slots.size())
        {
            return ItemStack.EMPTY;
        }
        RiftSlot slot = slots.get(index);
        ItemStack stack = slot.getSample();
        long count = slot.getCount();
        if (simulate)
        {
            int amount = (int) Math.min(wanted, count);
            stack = stack.copyWithCount(amount);
        }
        else if (count <= wanted)
        {
            slots.remove(index);
            afterRemove(slot);
        }
        else
        {
            stack = stack.copyWithCount(wanted);
            slot.subtractCount(wanted);
        }
        onContentsChanged();
        return stack;
    }

    @Override
    public int getSlotLimit(int index)
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        return !ItemStack.isSameItem(ItemStack.EMPTY, stack);
    }

    @Override
    public long getCount(int index)
    {
        if (index < 0 || index >= slots.size())
        {
            return 0;
        }
        return slots.get(index).getCount();
    }

    public void clear()
    {
        if (slots.size() > 0)
        {
            slots.clear();
            afterClear();
            onContentsChanged();
        }
    }

    @ApiStatus.Internal
    public void append(ItemStack stack, long amount)
    {
        var slot = new RiftSlot(stack, amount);
        slots.add(slot);
        afterAdd(slot);
    }

    public void clearSlot(RiftSlot slot)
    {
        if (slots.remove(slot))
        {
            afterRemove(slot);
        }
    }

    @ApiStatus.Internal
    public void addHook(RiftChangeHook hook)
    {
        hooks.add(hook);
        for (var slot : slots)
        {
            hook.onAdd(slot);
        }
    }

    private final Map<Class<?>, Object> attachedFeatures = new IdentityHashMap<>();

    public <T> T getOrCreateFeature(Class<T> featureClass, Function<RiftInventory, T> featureFactory)
    {
        //noinspection unchecked
        return (T) attachedFeatures.computeIfAbsent(featureClass, key -> featureFactory.apply(this));
    }

    public boolean isDirty()
    {
        return this.isDirty;
    }

    public void clearDirty()
    {
        this.isDirty = false;
    }
}
