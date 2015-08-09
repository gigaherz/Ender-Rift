package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.network.NetworkRegistry;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.network.ValueUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TileEnderRift
        extends TileEntity
        implements IEnergyReceiver, IInventory {

    public static final float PowerPerInsertionConstant = 1;
    public static final float PowerPerInsertionLinear = 1;


    public static final float PowerPerExtractionConstant = 1;
    public static final float PowerPerExtractionLinear = 0;

    public static int BroadcastRange = 256;

    private final List<ItemStack> inventorySlots = new ArrayList<ItemStack>();

    public final int energyLimit = 100000;
    public int energyBuffer = 0;

    public int getEnergyInsert() {
        return (int) Math.ceil(PowerPerInsertionConstant + (inventorySlots.size() * PowerPerInsertionLinear));
    }

    public int getEnergyExtract() {
        return (int) Math.ceil(PowerPerExtractionConstant + (inventorySlots.size() * PowerPerExtractionLinear));
    }

    public int countInventoryStacks() {
        int count = 0;
        for (ItemStack stack : inventorySlots) {
            if (stack != null)
                count++;
        }
        return count;
    }

    @Override
    public int getSizeInventory() {
        return inventorySlots.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {

        if (slotIndex >= inventorySlots.size())
            return null;

        return inventorySlots.get(slotIndex);
    }

    private void setStackInSlotInternal(int slotIndex, ItemStack stack) {

        if(slotIndex >= inventorySlots.size())
        {
            inventorySlots.add(stack);
            return;
        }

        inventorySlots.set(slotIndex, stack);
    }

    private void removeStackFromSlotInternal(int slotIndex) {

        if (slotIndex >= inventorySlots.size())
            return;

        inventorySlots.remove(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {

        if (stack == null) {
            ItemStack oldStack = getStackInSlot(slot);

            if (oldStack == null)
                return;

            int powerCost = getEnergyExtract();

            if (energyBuffer >= powerCost) {
                removeStackFromSlotInternal(slot);
                energyBuffer -= powerCost;
            }

            return;
        }

        int powerCost = getEnergyInsert();

        if (energyBuffer >= powerCost) {
            setStackInSlotInternal(slot, stack);
            energyBuffer -= powerCost;
        } else {
            stack.stackSize = 0;
        }

        if (stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack == null)
            return null;

        if (stack.stackSize <= amount) {
            setInventorySlotContents(slotIndex, null);
        } else {
            stack = stack.splitStack(amount);

            if (stack.stackSize == 0) {
                setInventorySlotContents(slotIndex, null);
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        NBTTagList nbtTagList = nbtTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < nbtTagList.tagCount(); ++i) {
            NBTTagCompound nbtTagCompound1 = nbtTagList.getCompoundTagAt(i);
            int j = nbtTagCompound1.getByte("Slot");

            setStackInSlotInternal(j, ItemStack.loadItemStackFromNBT(nbtTagCompound1));
        }

        energyBuffer = nbtTagCompound.getInteger("Energy");
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null) {
                NBTTagCompound nbtTagCompound1 = new NBTTagCompound();
                nbtTagCompound1.setInteger("Slot", i);
                stack.writeToNBT(nbtTagCompound1);
                nbtTagList.appendTag(nbtTagCompound1);
            }
        }

        nbtTagCompound.setTag("Items", nbtTagList);
        nbtTagCompound.setInteger("Energy", energyBuffer);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        int receive = Math.min(maxReceive, energyLimit - energyBuffer);
        if (!simulate)
            energyBuffer += receive;

        int dim = worldObj.provider.dimensionId;
        EnderRiftMod.channel.sendToAllAround(new ValueUpdate(this, 0, energyBuffer), new NetworkRegistry.TargetPoint(dim, xCoord, yCoord, zCoord, BroadcastRange));
        this.markDirty();
        return receive;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energyBuffer;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energyLimit;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    public void updateValue(int barIndex, int barValue) {
        if(barIndex == 0)
        {
            energyBuffer = barValue;
        }
    }
}