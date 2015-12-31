package gigaherz.enderRift.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IInventoryAutomation
{
    default IInventory getInventorySide(EnumFacing face)
    {
        if (this instanceof ISidedInventory)
            return InventoryFromSlots.create((IInventory)this, ((ISidedInventory)this).getSlotsForFace(face));
        return null;
    }

    ItemStack pushItems(ItemStack stack);
}
