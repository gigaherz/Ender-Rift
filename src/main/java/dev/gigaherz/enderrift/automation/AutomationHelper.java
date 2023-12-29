package dev.gigaherz.enderrift.automation;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper
{
    public static boolean isPowerSource(BlockEntity object, Direction facing)
    {
        return object.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, object.getBlockPos(), object.getBlockState(), object, facing) != null;
    }

    public static boolean isAutomatable(BlockEntity object, Direction facing)
    {
        return object.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, object.getBlockPos(), object.getBlockState(), object, facing) != null;
    }

    public static ItemStack insertItems(IItemHandler parent, ItemStack stack)
    {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < parent.getSlots(); i++)
        {
            remaining = parent.insertItem(i, remaining, false);
            if (remaining.getCount() <= 0)
                break;
        }

        if (remaining.getCount() > 0)
            return remaining;

        return ItemStack.EMPTY;
    }

    public static ItemStack extractItems(IItemHandler parent, ItemStack stack, int wanted, boolean simulate)
    {
        if (stack.getCount() <= 0 || wanted <= 0)
            return ItemStack.EMPTY;

        wanted = Math.min(stack.getMaxStackSize(), wanted);

        ItemStack _extracted = stack.copy();
        int extractCount = 0;

        for (int i = 0; i < parent.getSlots(); i++)
        {
            ItemStack slot = parent.getStackInSlot(i);
            if (slot.getCount() > 0)
            {
                int requested = Math.min(wanted, slot.getCount());
                if (requested > 0 && ItemStack.isSameItemSameTags(slot, stack))
                {
                    ItemStack obtained = parent.extractItem(i, requested, simulate);

                    int returned = obtained.getCount();

                    extractCount += returned;
                    wanted -= returned;
                    if (wanted <= 0)
                        break;
                }
            }
        }

        if (extractCount <= 0)
            return ItemStack.EMPTY;

        _extracted.setCount(extractCount);
        return _extracted;
    }
}