package gigaherz.api.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;

public interface IBetterSidedInventory
{
    default IInventory getInventoryForSide(EnumFacing face)
    {
        if (this instanceof ISidedInventory)
            return InventorySlotsWrapper.create((IInventory)this, ((ISidedInventory)this).getSlotsForFace(face));
        return null;
    }

}
