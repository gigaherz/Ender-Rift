package gigaherz.enderRift.automation;

import gigaherz.enderRift.plugins.tesla.TeslaControllerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper
{
    public static boolean isPowerSource(Object object, EnumFacing facing)
    {
        if (!(object instanceof ICapabilityProvider))
            return false;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        Capability teslaCap = TeslaControllerBase.PRODUCER.getCapability();
        return cap.hasCapability(CapabilityEnergy.ENERGY, facing)
                || (teslaCap != null && cap.hasCapability(teslaCap, facing));
    }

    public static boolean isAutomatable(Object object, EnumFacing facing)
    {
        if (!(object instanceof ICapabilityProvider))
            return false;

        ICapabilityProvider cap = (ICapabilityProvider) object;

        return cap.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    public static ItemStack insertItems(@Nonnull IItemHandler parent, @Nonnull ItemStack stack)
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

    public static ItemStack extractItems(@Nonnull IItemHandler parent, @Nonnull ItemStack stack, int wanted, boolean simulate)
    {
        if (stack.stackSize <= 0 || wanted <= 0)
            return null;

        wanted = Math.min(stack.getMaxStackSize(), wanted);

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
