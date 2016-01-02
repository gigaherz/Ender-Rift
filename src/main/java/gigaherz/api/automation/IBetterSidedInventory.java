package gigaherz.api.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;

public interface IBetterSidedInventory
{
    IInventory getInventoryForSide(EnumFacing face);
    /* ENABLE default impl after switching to Java8 {
        if (this instanceof ISidedInventory)
            return InventorySlotsWrapper.create((IInventory)this, ((ISidedInventory)this).getSlotsForFace(face));
        return null;
    }*/
}
