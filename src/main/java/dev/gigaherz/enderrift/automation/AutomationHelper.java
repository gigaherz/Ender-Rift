package dev.gigaherz.enderrift.automation;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper
{
    public static boolean isPowerSource(BlockEntity object, Direction facing)
    {
        Level level = object.getLevel();
        return level != null && level.getCapability(Capabilities.Energy.BLOCK, object.getBlockPos(), object.getBlockState(), object, facing) != null;
    }

    public static boolean isAutomatable(BlockEntity object, Direction facing)
    {
        Level level = object.getLevel();
        return level != null && level.getCapability(Capabilities.Item.BLOCK, object.getBlockPos(), object.getBlockState(), object, facing) != null;
    }

    public static ItemStack insertItems(ResourceHandler<ItemResource> parent, ItemStack stack)
    {
        try(var transaction = Transaction.openRoot())
        {
            var inserted = parent.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            if (inserted >= stack.getCount())
                return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public static ItemStack extractItems(ResourceHandler<ItemResource> parent, ItemStack stack, int wanted, boolean simulate)
    {
        if (stack.isEmpty() || wanted <= 0)
            return ItemStack.EMPTY;

        var resource = ItemResource.of(stack);
        wanted = Math.min(stack.getMaxStackSize(), wanted);

        try(var transaction = Transaction.openRoot())
        {
            var extracted = parent.extract(resource, wanted, transaction);

            if (!simulate)
                transaction.commit();

            if (extracted <= 0)
                return ItemStack.EMPTY;
            return stack.copyWithCount(extracted);
        }
    }
}