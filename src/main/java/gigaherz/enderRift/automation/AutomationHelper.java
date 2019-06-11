package gigaherz.enderRift.automation;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper
{
    public static boolean isPowerSource(Object object, Direction facing)
    {
        if (!(object instanceof ICapabilityProvider))
            return false;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        return cap.getCapability(CapabilityEnergy.ENERGY, facing).isPresent();
    }

    public static boolean isAutomatable(Object object, Direction facing)
    {
        if (!(object instanceof ICapabilityProvider))
            return false;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        return cap.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent();
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
                if (requested > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
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
