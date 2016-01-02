package gigaherz.api.automation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * An utility class for aggreagating multiple inventories.
 */
public class InventoryAggregator implements IInventory
{
    List<IInventory> inventories = new ArrayList<>();

    @Override
    public int getSizeInventory()
    {
        return inventories.stream().mapToInt(IInventory::getSizeInventory).sum();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        IInventory actual = null;
        for(IInventory i : inventories)
        {
            int size = i.getSizeInventory();
            if(index < size)
            {
                actual = i;
                break;
            }
            index -= size;
        }
        if(actual == null)
            return null;
        return actual.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        IInventory actual = null;
        for(IInventory i : inventories)
        {
            int size = i.getSizeInventory();
            if(index < size)
            {
                actual = i;
                break;
            }
            index -= size;
        }
        if(actual == null)
            return null;
        return actual.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        IInventory actual = null;
        for(IInventory i : inventories)
        {
            int size = i.getSizeInventory();
            if(index < size)
            {
                actual = i;
                break;
            }
            index -= size;
        }
        if(actual == null)
            return null;
        return actual.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        IInventory actual = null;
        for(IInventory i : inventories)
        {
            int size = i.getSizeInventory();
            if(index < size)
            {
                actual = i;
                break;
            }
            index -= size;
        }
        if(actual == null)
            return;

        actual.setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return inventories.get(0).getInventoryStackLimit();
    }

    @Override
    public void markDirty()
    {
        inventories.stream().forEach(IInventory::markDirty);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return inventories.get(0).isUseableByPlayer(player);
    }

    @Override
    public void openInventory(final EntityPlayer player)
    {
        inventories.stream().forEach(i -> i.openInventory(player));
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        inventories.stream().forEach(i -> i.closeInventory(player));
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        IInventory actual = null;
        for (IInventory i : inventories)
        {
            int size = i.getSizeInventory();
            if (index < size)
            {
                actual = i;
                break;
            }
            index -= size;
        }
        return actual != null && actual.isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {

    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }
}
