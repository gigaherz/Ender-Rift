package dev.gigaherz.enderrift.rift.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.rift.ILongItemHandler;
import dev.gigaherz.enderrift.rift.IRiftChangeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class RiftInventory implements ILongItemHandler
{

    final List<Reference<? extends IRiftChangeListener>> listeners = Lists.newArrayList();
    final ReferenceQueue<IRiftChangeListener> pendingRemovals = new ReferenceQueue<>();
    private final List<RiftSlot> slots = Lists.newArrayList();
    private final RiftHolder holder;

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
        walkListeners(IRiftChangeListener::onRiftChanged);
    }

    public void locateListeners(Level level, Consumer<BlockPos> locationConsumer)
    {
        walkListeners(listener -> {
            if (!level.dimension().equals(listener.getRiftLevel().map(Level::dimension).orElse(null)))
            {
                return;
            }
            listener.getLocation().ifPresent(locationConsumer);
        });
    }

    public long getCount(int slot)
    {
        if (slot < 0 || slot >= slots.size())
        {
            return 0;
        }
        return slots.get(slot).getCount();
    }

    CompoundTag save()
    {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        RiftSlot[] slots = this.slots.toArray(RiftSlot[]::new);
        for (RiftSlot slot : slots)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Count", slot.getCount());
            tag.put("Item", slot.getSample().save(new CompoundTag()));
            list.add(tag);
        }
        root.put("Contents", list);
        return root;
    }

    void load(CompoundTag root)
    {
        slots.clear();
        ListTag list = root.getList("Contents", Tag.TAG_COMPOUND);
        for (Tag rawTag : list)
        {
            CompoundTag tag = (CompoundTag) rawTag;
            long count = tag.getLong("Count");
            ItemStack stack = ItemStack.of(tag.getCompound("Item"));
            RiftSlot slot = new RiftSlot(stack);
            slot.setCount(count);
            slots.add(slot);
        }
        onContentsChanged();
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
        RiftSlot slot = slots.get(index);
        return slot.getSample();
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
            slots.add(new RiftSlot(stack));
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
        if (ItemStack.isSameItemSameTags(sample, stack))
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

    public void clear()
    {
        slots.clear();
        onContentsChanged();
    }
}
