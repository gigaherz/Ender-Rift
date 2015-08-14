package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.storage.RiftInventory;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEnderRift
        extends TileEntity
        implements IEnergyReceiver, IInventory
{

    public static int BroadcastRange = 256;

    public final int energyLimit = 10000000;
    public int energyBuffer = 0;

    public int riftId;
    RiftInventory inventory;

    boolean alreadyMarkedDirty;

    RiftInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = RiftStorageWorldData.get(worldObj).getRift(riftId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
    }

    public int getEnergyInsert()
    {
        int sizeInventory = getInventory().getSizeInventory();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return (int) Math.ceil(
                ConfigValues.PowerPerInsertionConstant
                        + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                        + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric));
    }

    public int getEnergyExtract()
    {
        int sizeInventory = getInventory().getSizeInventory();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return (int) Math.ceil(
                ConfigValues.PowerPerExtractionConstant
                        + (sizeInventory * ConfigValues.PowerPerExtractionLinear)
                        + (sizeInventory2 * ConfigValues.PowerPerExtractionGeometric));
    }

    public int countInventoryStacks()
    {
        return getInventory().countInventoryStacks();
    }

    @Override
    public int getSizeInventory()
    {
        return getInventory().getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return getInventory().getStackInSlot(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if (stack == null)
        {
            ItemStack oldStack = getStackInSlot(slot);

            if (oldStack == null)
                return;

            int powerCost = getEnergyExtract();

            if (energyBuffer >= powerCost)
            {
                getInventory().setInventorySlotContents(slot, null);
                energyBuffer -= powerCost;
            }

            return;
        }

        int powerCost = getEnergyInsert();

        if (energyBuffer >= powerCost)
        {
            getInventory().setInventorySlotContents(slot, stack);
            energyBuffer -= powerCost;
        }
        else
        {
            stack.stackSize = 0;
        }

        if (stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName()
    {
        return getInventory().getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return getInventory().hasCustomInventoryName();
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        return getInventory().decrStackSize(slotIndex, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        return null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        getInventory().markDirty();
    }

    public void setDirty()
    {
        if (alreadyMarkedDirty)
            return;

        alreadyMarkedDirty = true;
        super.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
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
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
    {
        return energyBuffer >= getEnergyInsert();
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        energyBuffer = nbtTagCompound.getInteger("Energy");
        riftId = nbtTagCompound.getInteger("RiftId");
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("Energy", energyBuffer);
        nbtTagCompound.setInteger("RiftId", riftId);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, energyLimit - energyBuffer);
        if (!simulate && receive > 0)
        {
            energyBuffer += receive;

            this.setDirty();
        }

        return receive;
    }

    @Override
    public int getEnergyStored(ForgeDirection from)
    {
        return energyBuffer;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return energyLimit;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from)
    {
        return false;
    }

    public void updateValue(int barIndex, int barValue)
    {
        if (barIndex == 0)
        {
            energyBuffer = barValue;
        }
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
        return oldBlock != newBlock || ((oldMeta != 0) != (newMeta != 0));
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        alreadyMarkedDirty = false;
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.itemEnderRift);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("RiftId", riftId);

        stack.setTagCompound(tag);

        return stack;
    }
}