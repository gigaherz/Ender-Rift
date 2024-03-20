package dev.gigaherz.enderrift.rift.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.rift.ILongItemHandler;
import dev.gigaherz.enderrift.rift.IRiftChangeListener;
import dev.gigaherz.enderrift.rift.RiftChangeHook;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private RiftChangeHook hook;

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

    CompoundTag save()
    {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        RiftSlot[] slots = this.slots.toArray(RiftSlot[]::new);
        for (RiftSlot slot : slots)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Count", slot.getCount());
            tag.put("Item", slot.getSample().copyWithCount(1).save(new CompoundTag()));
            list.add(tag);
        }
        root.put("Contents", list);
        return root;
    }

    void load(CompoundTag root)
    {
        if (slots.size() > 0)
        {
            slots.clear();
            if (hook != null) hook.onClear();
        }

        ListTag list = root.getList("Contents", Tag.TAG_COMPOUND);
        for (Tag rawTag : list)
        {
            CompoundTag tag = (CompoundTag) rawTag;
            long count = tag.getLong("Count");
            var itemCompound = tag.getCompound("Item");
            itemCompound.putInt("Count", 1);
            ItemStack stack = ItemStack.of(itemCompound);
            RiftSlot slot = new RiftSlot(stack, count);
            slots.add(slot);
            if (hook != null) hook.onAdd(slot);
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
            var slot = new RiftSlot(stack);
            slots.add(slot);
            if (hook != null) hook.onAdd(slot);
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
            if (hook != null) hook.onRemove(slot);
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
            if (hook != null) hook.onClear();
            onContentsChanged();
        }
    }

    @ApiStatus.Internal
    public void append(ItemStack stack, long amount)
    {
        var slot = new RiftSlot(stack, amount);
        slots.add(slot);
        if (hook != null) hook.onAdd(slot);
    }

    public void clearSlot(RiftSlot slot)
    {
        if (slots.remove(slot))
        {
            if (hook != null) hook.onRemove(slot);
        }
    }

    @ApiStatus.Internal
    public void setHook(RiftChangeHook hook)
    {
        if (this.hook != hook)
        {
            this.hook = hook;
            for (var slot : slots)
            {
                this.hook.onAdd(slot);
            }
        }
    }
}
