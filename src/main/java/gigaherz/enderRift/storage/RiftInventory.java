package gigaherz.enderRift.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class RiftInventory implements IInventory
{
    private final List<ItemStack> inventorySlots = new ArrayList<ItemStack>();
    private final RiftStorageWorldData manager;

    RiftInventory(RiftStorageWorldData manager)
    {
        this.manager = manager;
    }

    public int countInventoryStacks()
    {
        int count = 0;
        for (ItemStack stack : inventorySlots)
        {
            if (stack != null)
                count++;
        }
        return count;
    }

    @Override
    public int getSizeInventory()
    {
        return inventorySlots.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {

        if (slotIndex >= inventorySlots.size())
            return null;

        return inventorySlots.get(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack stack)
    {
        if (stack == null)
        {
            if (slotIndex >= inventorySlots.size())
                return;

            inventorySlots.remove(slotIndex);
            manager.markDirty();

            return;
        }

        if (slotIndex >= inventorySlots.size())
        {
            inventorySlots.add(stack);
            manager.markDirty();

            return;
        }

        inventorySlots.set(slotIndex, stack);
        manager.markDirty();
    }

    @Override
    public String getInventoryName()
    {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack == null)
            return null;

        if (stack.stackSize <= amount)
        {
            setInventorySlotContents(slotIndex, null);
        }
        else
        {
            stack = stack.splitStack(amount);

            if (stack.stackSize == 0)
            {
                setInventorySlotContents(slotIndex, null);
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        return null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void markDirty()
    {
        manager.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList nbtTagList = nbtTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < nbtTagList.tagCount(); ++i)
        {
            NBTTagCompound nbtTagCompound1 = nbtTagList.getCompoundTagAt(i);
            int j = nbtTagCompound1.getInteger("Slot");

            setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbtTagCompound1));
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i)
        {
            ItemStack stack = getStackInSlot(i);
            if (stack != null)
            {
                NBTTagCompound nbtTagCompound1 = new NBTTagCompound();
                nbtTagCompound1.setInteger("Slot", i);
                stack.writeToNBT(nbtTagCompound1);
                nbtTagList.appendTag(nbtTagCompound1);
            }
        }

        nbtTagCompound.setTag("Items", nbtTagList);
    }
}
