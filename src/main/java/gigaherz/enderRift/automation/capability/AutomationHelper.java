package gigaherz.enderRift.automation.capability;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper implements IInventoryAutomation
{
    /**
     * Gets an inventory automation instance, wrapping an inventory if necessary.
     *
     * @param object The object to get an inventory from.
     * @param facing The face from which the automation is happening.
     * @return Returns an instance of an easy-to-automate inventory.
     */
    public static IInventoryAutomation get(Object object, EnumFacing facing)
    {
        // If the inventory implements IItemHandler, wrap it.
        if (!(object instanceof ICapabilityProvider))
            return null;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        if (cap.hasCapability(CapabilityAutomation.INSTANCE, facing))
            return cap.getCapability(CapabilityAutomation.INSTANCE, facing);

        if (cap.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
            return new ItemHandlerWrapper(cap.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing));

        // If none of the above worked, give up.
        return null;
    }

    public static boolean isAutomatable(Object object, EnumFacing facing)
    {
        // If the inventory is better sided, make use of it.
        if (!(object instanceof ICapabilityProvider))
            return false;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        return cap.hasCapability(CapabilityAutomation.INSTANCE, facing)
                || cap.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    private static class ItemHandlerWrapper extends AutomationHelper
    {
        IItemHandler parent;

        private ItemHandlerWrapper(IItemHandler parent)
        {
            this.parent = parent;
        }

        @Override
        public int getSlots()
        {
            return parent.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            return parent.getStackInSlot(index);
        }

        @Override
        public ItemStack insertItems(@Nonnull ItemStack stack)
        {
            ItemStack remaining = stack.copy();

            for (int i = 0; i < parent.getSlots(); i++)
            {
                remaining = parent.insertItem(i, remaining, false);
                if (remaining == null || remaining.stackSize <= 0)
                    break;
            }

            if (remaining != null && remaining.stackSize > 0)
                return remaining;

            return null;
        }

        @Override
        public ItemStack extractItems(@Nonnull ItemStack stack, int wanted, boolean simulate)
        {
            if (stack.stackSize <= 0 || wanted <= 0)
                return null;

            ItemStack extracted = stack.copy();
            extracted.stackSize = 0;

            for (int i = 0; i < parent.getSlots(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int requested = Math.min(wanted, slot.stackSize);
                    if (requested > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
                    {
                        ItemStack obtained = parent.extractItem(i, requested, simulate);

                        if (obtained != null && !simulate)
                        {
                            int remaining = slot.stackSize - obtained.stackSize;
                            int found = 0;
                            slot = parent.getStackInSlot(i);
                            if (slot != null)
                                found = slot.stackSize;

                            if (found != remaining)
                                EnderRiftMod.logger.warn("DAFUQ, Found an incorrect number of items in the slot " + i + " after extraction! Found: " + found + " expected " + remaining);
                        }

                        int returned = (obtained != null) ? obtained.stackSize : 0;

                        extracted.stackSize += returned;
                        wanted -= returned;
                        if (wanted <= 0)
                            break;
                    }
                }
            }

            if (extracted.stackSize <= 0)
                return null;

            return extracted;
        }
    }
}
