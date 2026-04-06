package dev.gigaherz.enderrift.common;

import dev.gigaherz.enderrift.ConfigValues;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.Optional;

public class AutomationEnergyWrapper implements ResourceHandler<ItemResource>
{
    private final IPoweredAutomation owner;

    public AutomationEnergyWrapper(IPoweredAutomation owner)
    {
        this.owner = owner;
    }

    private double getEnergyInsert()
    {
        int sizeInventory = size();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return Math.min(ConfigValues.PowerPerInsertionCap,
                ConfigValues.PowerPerInsertionConstant
                        + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                        + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric));
    }

    private double getEnergyExtract()
    {
        int sizeInventory = size();
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
    public int size()
    {
        var inventory = owner.getInventory();
        return inventory != null ? inventory.size() : 0;
    }

    @Override
    public ItemResource getResource(int slot)
    {
        var inventory = owner.getInventory();
        return inventory != null ? inventory.getResource(slot) : ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int slot)
    {
        var inventory = owner.getInventory();
        return inventory != null ? inventory.getAmountAsLong(slot) : 0;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        int cost = getEffectivePowerUsageToInsert(amount);

        Optional<EnergyHandler> optBuffer = owner.getEnergyBuffer();
        if (optBuffer.isPresent())
        {
            var energyBuffer = optBuffer.get();
            var inventory = owner.getInventory();

            if (inventory == null)
                return 0;

            boolean powerFailure = false;
            while (cost > energyBuffer.getAmountAsInt() && amount > 0)
            {
                powerFailure = true;

                amount--;
            }

            if (powerFailure)
                owner.setLowOnPowerTemporary();

            if (amount <= 0)
                return amount;

            int inserted = inventory.insert(index, resource, amount, transaction);

            int actualCost = getEffectivePowerUsageToInsert(inserted);
            if (actualCost < energyBuffer.getAmountAsInt())
            {
                energyBuffer.extract(actualCost, transaction);
                owner.setDirty();
                return inserted;
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        int cost = getEffectivePowerUsageToExtract(amount);

        Optional<EnergyHandler> optBuffer = owner.getEnergyBuffer();
        if (optBuffer.isPresent())
        {
            var energyBuffer = optBuffer.get();
            var inventory = owner.getInventory();

            if (inventory == null)
                return 0;

            int extracted = inventory.extract(index, resource, amount, transaction);

            boolean powerFailure = false;
            while (cost > energyBuffer.getAmountAsInt() && amount > 0)
            {
                powerFailure = true;

                amount--;
                cost = getEffectivePowerUsageToExtract(amount);
            }

            if (powerFailure)
                owner.setLowOnPowerTemporary();

            if (amount <= 0)
                return 0;

            int extracted1 = inventory.extract(index, resource, amount, transaction);
            if (extracted1 <= 0)
                return 0;

            int actualCost = getEffectivePowerUsageToExtract(extracted1);
            energyBuffer.extract(actualCost, transaction);

            owner.setDirty();

            return extracted;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource)
    {
        return 64;
    }

    @Override
    public boolean isValid(int slot, ItemResource stack)
    {
        return true;
    }

    public boolean isLowOnPower()
    {
        return owner.getEnergyBuffer().map(buffer -> getEffectivePowerUsageToExtract(1) > buffer.getAmountAsInt()).orElse(false);
    }
}