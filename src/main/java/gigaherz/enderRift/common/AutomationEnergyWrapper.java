package gigaherz.enderRift.common;

import gigaherz.enderRift.ConfigValues;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class AutomationEnergyWrapper implements IItemHandler
{
    private final IPoweredAutomation owner;

    public AutomationEnergyWrapper(IPoweredAutomation owner)
    {
        this.owner = owner;
    }

    private double getEnergyInsert()
    {
        int sizeInventory = owner.getInventory().getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return Math.min(ConfigValues.PowerPerInsertionCap,
                ConfigValues.PowerPerInsertionConstant
                + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric));
    }

    private double getEnergyExtract()
    {
        int sizeInventory = owner.getInventory().getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return Math.min(ConfigValues.PowerPerExtractionCap,
                ConfigValues.PowerPerExtractionConstant
                + (sizeInventory * ConfigValues.PowerPerExtractionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerExtractionGeometric));
    }

    private int getEffectivePowerUsageToInsert(int stackSize)
    {
        return owner.getWorld().isRemote ? 0 : (int) Math.ceil(getEnergyInsert() * stackSize);
    }

    private int getEffectivePowerUsageToExtract(int limit)
    {
        return owner.getWorld().isRemote ? 0 : (int) Math.ceil(getEnergyExtract() * limit);
    }

    @Override
    public int getSlots()
    {
        return owner.getInventory().getSlots();
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return owner.getInventory().getStackInSlot(slot);
    }

    @Nullable
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        int stackSize = stack.stackSize;
        int cost = getEffectivePowerUsageToInsert(stackSize);
        while (cost > owner.getEnergyBuffer().getEnergyStored() && stackSize > 0)
        {
            stackSize--;
        }

        if (stackSize <= 0)
            return stack;

        ItemStack temp = stack.copy();
        temp.stackSize = stackSize;

        ItemStack remaining = owner.getInventory().insertItem(slot, temp, simulate);

        if (!simulate)
        {
            if (remaining != null)
                stackSize -= remaining.stackSize;

            int actualCost = getEffectivePowerUsageToInsert(stackSize);
            owner.getEnergyBuffer().extractEnergy(actualCost, false);

            owner.markDirty();
        }

        return remaining;
    }

    @Nullable
    @Override
    public ItemStack extractItem(int slot, int wanted, boolean simulate)
    {
        int cost = getEffectivePowerUsageToExtract(wanted);
        while (cost > owner.getEnergyBuffer().getEnergyStored() && wanted > 0)
        {
            wanted--;
        }

        if (wanted <= 0)
            return null;

        ItemStack extracted = owner.getInventory().extractItem(slot, wanted, simulate);
        if (extracted == null)
            return null;

        if (!simulate)
        {
            int actualCost = getEffectivePowerUsageToExtract(extracted.stackSize);
            owner.getEnergyBuffer().extractEnergy(actualCost, false);

            owner.markDirty();
        }

        return extracted;
    }
}
