package gigaherz.enderRift.rift.storage;

import com.google.common.collect.Lists;
import gigaherz.enderRift.rift.TileEnderRift;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class RiftInventory implements IItemHandler
{
    private final RiftStorageWorldData manager;

    final List<Reference<? extends TileEnderRift>> listeners = Lists.newArrayList();
    final ReferenceQueue<TileEnderRift> deadListeners = new ReferenceQueue<>();
    private final List<ItemStack> inventorySlots = Lists.newArrayList();

    RiftInventory(RiftStorageWorldData manager)
    {
        this.manager = manager;
    }

    public void addWeakListener(TileEnderRift e)
    {
        listeners.add(new WeakReference<>(e, deadListeners));
    }

    private void onContentsChanged()
    {
        for (Reference<? extends TileEnderRift>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends TileEnderRift>> it = listeners.iterator(); it.hasNext(); )
        {
            TileEnderRift rift = it.next().get();
            if (rift == null || rift.isRemoved())
            {
                it.remove();
            }
            else
            {
                rift.markDirty();
            }
        }

        manager.markDirty();
    }

    public void readFromNBT(CompoundNBT nbtTagCompound)
    {
        ListNBT itemList = nbtTagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < itemList.size(); ++i)
        {
            CompoundNBT slot = itemList.getCompound(i);
            inventorySlots.add(ItemStack.read(slot));
        }
    }

    public void writeToNBT(CompoundNBT nbtTagCompound)
    {
        ListNBT itemList = new ListNBT();

        for (ItemStack stack : inventorySlots)
        {
            if (stack != null)
            {
                CompoundNBT slot = new CompoundNBT();
                stack.write(slot);
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
