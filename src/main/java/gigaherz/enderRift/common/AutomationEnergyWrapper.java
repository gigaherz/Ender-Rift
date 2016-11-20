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
        int sizeInventory = getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return Math.min(ConfigValues.PowerPerInsertionCap,
                ConfigValues.PowerPerInsertionConstant
                + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric));
    }

    private double getEnergyExtract()
    {
        int sizeInventory = getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return Math.min(ConfigValues.PowerPerExtractionCap,
                ConfigValues.PowerPerExtractionConstant
                + (sizeInventory * ConfigValues.PowerPerExtractionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerExtractionGeometric));
    }

    private int getEffectivePowerUsageToInsert(int stackSize)
    {
        return owner.isRemote() ? 0 : (int) Math.ceil(getEnergyInsert() * stackSize);
    }

    private int getEffectivePowerUsageToExtract(int limit)
    {
        return owner.isRemote() ? 0 : (int) Math.ceil(getEnergyExtract() * limit);
    }

    @Override
    public int getSlots()
    {
        IItemHandler inventory = owner.getInventory();
        return inventory != null ? inventory.getSlots() : 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        IItemHandler inventory = owner.getInventory();
        return inventory != null ? inventory.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        int stackSize = stack.getCount();
        int cost = getEffectivePowerUsageToInsert(stackSize);

        IEnergyStorage energyBuffer = owner.getEnergyBuffer();
        IItemHandler inventory = owner.getInventory();

        if (inventory == null)
            return ItemStack.EMPTY;

        while (cost > energyBuffer.getEnergyStored() && stackSize > 0)
        {
            stackSize--;
        }

        if (stackSize <= 0)
            return stack;

        ItemStack temp = stack.copy();
        temp.setCount(stackSize);

        ItemStack remaining = inventory.insertItem(slot, temp, simulate);

        if (!simulate)
        {
            stackSize -= remaining.getCount();

            int actualCost = getEffectivePowerUsageToInsert(stackSize);
            energyBuffer.extractEnergy(actualCost, false);

            owner.setDirty();
        }

        return remaining;
    }

    @Override
    public ItemStack extractItem(int slot, int wanted, boolean simulate)
    {
        int cost = getEffectivePowerUsageToExtract(wanted);

        IEnergyStorage energyBuffer = owner.getEnergyBuffer();
        IItemHandler inventory = owner.getInventory();

        if (inventory == null)
            return ItemStack.EMPTY;

        while (cost > energyBuffer.getEnergyStored() && wanted > 0)
        {
            wanted--;
        }

        if (wanted <= 0)
            return ItemStack.EMPTY;

        ItemStack extracted = inventory.extractItem(slot, wanted, simulate);
        if (extracted.getCount() <= 0)
            return ItemStack.EMPTY;

        if (!simulate)
        {
            int actualCost = getEffectivePowerUsageToExtract(extracted.getCount());
            energyBuffer.extractEnergy(actualCost, false);

            owner.setDirty();
        }

        return extracted;
    }
}
