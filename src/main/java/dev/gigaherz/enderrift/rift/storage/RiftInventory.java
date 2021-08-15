package dev.gigaherz.enderrift.rift.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.rift.IRiftChangeListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class RiftInventory implements IItemHandler
{
    private final RiftStorage manager;

    final List<Reference<? extends IRiftChangeListener>> listeners = Lists.newArrayList();
    final ReferenceQueue<IRiftChangeListener> pendingRemovals = new ReferenceQueue<>();
    private final List<ItemStack> inventorySlots = Lists.newArrayList();

    RiftInventory(RiftStorage manager)
    {
        this.manager = manager;
    }

    public void addWeakListener(IRiftChangeListener e)
    {
        listeners.add(new WeakReference<>(e, pendingRemovals));
    }

    private void walkListeners(Consumer<IRiftChangeListener> consumer)
    {
        for (Reference<? extends IRiftChangeListener>
             ref = pendingRemovals.poll();
             ref != null;
             ref = pendingRemovals.poll())
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
        manager.setDirty();
    }

    public void locateListeners(Consumer<BlockPos> locationConsumer)
    {
        walkListeners(listener -> listener.getLocation().ifPresent(locationConsumer));
    }

    public void readFromNBT(CompoundTag nbtTagCompound)
    {
        ListTag itemList = nbtTagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < itemList.size(); ++i)
        {
            CompoundTag slot = itemList.getCompound(i);
            inventorySlots.add(ItemStack.of(slot));
        }
    }

    public void writeToNBT(CompoundTag nbtTagCompound)
    {
        ListTag itemList = new ListTag();

        for (ItemStack stack : inventorySlots)
        {
            if (stack != null)
            {
                CompoundTag slot = new CompoundTag();
                stack.save(slot);
                itemList.add(slot);
            }
        }

        nbtTagCompound.put("Items", itemList);
    }

    @Override
    public int getSlots()
    {
        return inventorySlots.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (index >= inventorySlots.size())
            return ItemStack.EMPTY;
        return inventorySlots.get(index);
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate)
    {
        if (index >= inventorySlots.size())
        {
            if (!simulate)
            {
                inventorySlots.add(stack.copy());
                onContentsChanged();
            }
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();
        ItemStack slot = inventorySlots.get(index);
        if (slot.getCount() > 0)
        {
            int max = Math.min(remaining.getMaxStackSize(), 64);
            int transfer = Math.min(remaining.getCount(), max - slot.getCount());
            if (transfer > 0 && ItemHandlerHelper.canItemStacksStack(remaining, slot))
            {
                if (!simulate) slot.grow(transfer);
                remaining.shrink(transfer);
                if (remaining.getCount() <= 0)
                    remaining = ItemStack.EMPTY;
            }
        }

        if (!simulate) onContentsChanged();

        return remaining;
    }

    @Override
    public ItemStack extractItem(int index, int wanted, boolean simulate)
    {
        if (index >= inventorySlots.size())
        {
            return ItemStack.EMPTY;
        }

        ItemStack slot = inventorySlots.get(index);
        if (slot == null)
            return ItemStack.EMPTY;

        ItemStack _extracted = slot.copy();
        int extractedCount = 0;

        int available = Math.min(wanted, slot.getCount());
        if (available > 0)
        {
            extractedCount += available;

            if (!simulate)
            {
                slot.shrink(available);
                if (slot.getCount() <= 0)
                    inventorySlots.remove(index);
            }
        }

        if (extractedCount <= 0)
            return ItemStack.EMPTY;

        if (!simulate) onContentsChanged();

        _extracted.setCount(extractedCount);
        return _extracted;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return true;
    }
}