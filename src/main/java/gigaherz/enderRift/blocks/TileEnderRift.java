package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import com.google.common.base.Predicate;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.api.automation.IInventoryAutomation;
import gigaherz.enderRift.storage.RiftInventory;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;

public class TileEnderRift
        extends TileEntity
        implements IEnergyReceiver, IInventoryAutomation, IBrowsableInventory, ITickable
{
    public final int energyLimit = 10000000;
    public int energyBuffer = 0;

    public int riftId;
    RiftInventory inventory;
    IInventoryAutomation automation;

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
        return getInventory().getSizeInventory();
    }

    private int getEffectivePowerUsageToInsert(int stackSize)
    {
        return (int) Math.ceil(getEnergyInsert() * stackSize);
    }

    private int getEffectivePowerUsageToExtract(int limit)
    {
        return (int) Math.ceil(getEnergyExtract() * limit);
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.itemEnderRift);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("RiftId", riftId);

        stack.setTagCompound(tag);

        return stack;
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

    @Override
    public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
    {
        // Save a call by not forwarding to the automation instance,
        // since it will return "this".
        return automation = getInventory();
    }

    @Override
    public ItemStack pushItems(@Nonnull ItemStack stack)
    {
        int stackSize = stack.stackSize;
        int cost = getEffectivePowerUsageToInsert(stackSize);
        while(cost > this.energyBuffer && stackSize > 0)
        {
            stackSize--;
        }

        if(stackSize <= 0)
            return stack;

        ItemStack temp = stack;
        if(stackSize != stack.stackSize)
        {
            temp = stack.copy();
            temp.stackSize = stackSize;
        }

        ItemStack remaining = (automation = getInventory()).pushItems(temp);
        if(remaining != null)
            stackSize -= remaining.stackSize;

        int actualCost = getEffectivePowerUsageToInsert(stackSize);
        this.energyBuffer -= actualCost;

        return remaining;
    }

    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        int cost = getEffectivePowerUsageToExtract(limit);
        while(cost > this.energyBuffer && limit > 0)
        {
            limit--;
        }

        if (limit <= 0)
            return null;

        ItemStack extracted = (automation = getInventory()).pullItems(limit, filter);
        if(extracted == null)
            return null;

        int actualCost = getEffectivePowerUsageToExtract(extracted.stackSize);
        this.energyBuffer -= actualCost;

        return extracted;
    }

    @Override
    public ItemStack extractItems(@Nonnull ItemStack stack, int wanted)
    {
        int cost = getEffectivePowerUsageToExtract(wanted);
        while(cost > this.energyBuffer && wanted > 0)
        {
            wanted--;
        }

        if (wanted <= 0)
            return null;

        ItemStack extracted = (automation = getInventory()).extractItems(stack, wanted);
        if(extracted == null)
            return null;

        int actualCost = getEffectivePowerUsageToExtract(extracted.stackSize);
        this.energyBuffer -= actualCost;

        return extracted;
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.getStackInSlot(index);
    }
}