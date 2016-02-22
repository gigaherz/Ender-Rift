package gigaherz.enderRift.automation;

import net.minecraft.util.EnumFacing;

public interface IAutomationProvider
{
    /**
     * Gets the corresponding inventory for the given side.
     *
     * @param side The requested side.
     * @return Returns an inventory instance representing the requested side's slots. Can be null.
     */
    IInventoryAutomation getAutomation(EnumFacing side);
}
