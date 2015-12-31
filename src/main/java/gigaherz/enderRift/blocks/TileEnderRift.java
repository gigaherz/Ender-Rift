package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.storage.RiftInventory;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;

public class TileEnderRift
        extends TileEntity
        implements IEnergyReceiver, ISidedInventory, ITickable
{
    public final int energyLimit = 10000000;
    public int energyBuffer = 0;

    public int riftId;
    RiftInventory inventory;

    boolean alreadyMarkedDirty;

    RiftInventory getInventory() {
        if (inventory == null) {
            inventory = RiftStorageWorldData.get(worldObj).getRift(riftId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    public double getEnergyInsert() {
        int sizeInventory = getInventory().getSizeInventory();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return ConfigValues.PowerPerInsertionConstant
                + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric);
    }

    public double getEnergyExtract() {
        int sizeInventory = getInventory().getSizeInventory();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return ConfigValues.PowerPerExtractionConstant
                + (sizeInventory * ConfigValues.PowerPerExtractionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerExtractionGeometric);
    }

    public int countInventoryStacks() {
        return getInventory().countInventoryStacks();
    }

    @Override
    public int getSizeInventory() {
        return getInventory().getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return getInventory().getStackInSlot(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        int power = getEffectivePowerUsageToReplace(slot, stack);

        getInventory().setInventorySlotContents(slot, null);
        energyBuffer = Math.max(0, energyBuffer - power);

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    private int getEffectivePowerUsageToReplace(int slot, ItemStack newStack) {
        int oldCount = 0;
        int newCount = 0;

        ItemStack oldStack = getStackInSlot(slot);

        if (oldStack != null) {
            oldCount = oldStack.stackSize;
        }

        if (newStack != null) {
            newCount = newStack.stackSize;
        }

        int itemsRemoved = 0;
        int itemsAdded = 0;
        if (oldStack != null && newStack != null && oldStack.isItemEqual(newStack)) {
            itemsRemoved = oldCount;
            itemsAdded = newCount;
        } else if (oldCount > newCount) {
            itemsRemoved = oldCount - newCount;
        } else if (oldCount < newCount) {
            itemsAdded = newCount - oldCount;
        }

        return (int) Math.ceil(getEnergyExtract() * itemsRemoved + getEnergyInsert() * itemsAdded);
    }

    @Override
    public String getName()
    {
        return getInventory().getName();
    }

    @Override
    public boolean hasCustomName()
    {
        return getInventory().hasCustomName();
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        return getInventory().decrStackSize(slotIndex, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        getInventory().markDirty();
    }

    public void setDirty() {
        if (allAccessibleSlots != null && allAccessibleSlots.length != getSizeInventory())
            allAccessibleSlots = null;

        if (alreadyMarkedDirty)
            return;

        alreadyMarkedDirty = true;
        super.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
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
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return energyBuffer >= getEnergyInsert();
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

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        energyBuffer = nbtTagCompound.getInteger("Energy");
        riftId = nbtTagCompound.getInteger("RiftId");
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("Energy", energyBuffer);
        nbtTagCompound.setInteger("RiftId", riftId);
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        int receive = Math.min(maxReceive, energyLimit - energyBuffer);
        if (!simulate && receive > 0) {
            energyBuffer += receive;

            this.setDirty();
        }

        return receive;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return energyBuffer;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return energyLimit;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return false;
    }

    @Override
    public void update() {
        alreadyMarkedDirty = false;
    }

    public ItemStack getRiftItem() {
        ItemStack stack = new ItemStack(EnderRiftMod.itemEnderRift);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("RiftId", riftId);

        stack.setTagCompound(tag);

        return stack;
    }

    int[] allAccessibleSlots;

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (allAccessibleSlots == null) {
            allAccessibleSlots = new int[getSizeInventory()];
            for (int i = 0; i < allAccessibleSlots.length; i++)
                allAccessibleSlots[i] = i;
        }
        return allAccessibleSlots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side)
    {
        return this.energyBuffer >= getEffectivePowerUsageToReplace(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side)
    {
        return this.energyBuffer >= getEffectivePowerUsageToReplace(slot, null);
    }
}