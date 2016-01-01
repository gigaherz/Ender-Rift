package gigaherz.enderRift.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public class InventorySlotsWrapper implements IInventory
{
    IInventory parent;
    int[] slots;

    private InventorySlotsWrapper(IInventory parent, int[] slots)
    {
        this.parent = parent;
        this.slots = slots;
    }

    public static InventorySlotsWrapper create(IInventory parent, int... slots)
    {
        return new InventorySlotsWrapper(parent, slots);
    }

    @Override
    public int getSizeInventory()
    {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if(index > slots.length)
            return null;
        return parent.getStackInSlot(slots[index]);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if(index > slots.length)
            return null;
        return parent.decrStackSize(slots[index], count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if(index > slots.length)
            return null;
        return parent.removeStackFromSlot(slots[index]);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if(index > slots.length)
            return;
        parent.setInventorySlotContents(slots[index], stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return parent.getInventoryStackLimit();
    }

    @Override
    public void markDirty()
    {
        parent.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index <= slots.length
                && parent.isItemValidForSlot(slots[index], stack);
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
        for(int i: slots)
        {
            parent.setInventorySlotContents(i, null);
        }
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
