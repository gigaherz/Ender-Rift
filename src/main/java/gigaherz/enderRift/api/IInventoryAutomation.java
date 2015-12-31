package gigaherz.enderRift.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IInventoryAutomation
{
    default IInventory getInventoryForSide(EnumFacing face)
    {
        if (this instanceof ISidedInventory)
            return InventoryFromSlots.create((IInventory)this, ((ISidedInventory)this).getSlotsForFace(face));
        return null;
    }

    /**
     * Tries to insert items into the inventory.
     * @param stack The items to insert.
     * @return Returns the remaining items it was unable to insert.
     */
    ItemStack pushItems(ItemStack stack);

    /**
     * Tries to pull from the first available stack from the inventory.
     * @param limit The maximum number of items to pull.
     * @return Returns the items that were extracted.
     */
    ItemStack pullItems(int limit);

    /**
     * Tries to extract a specific amount of a certain item, as defined by the provided ItemStack.
     * @param stack The item to extract, and the quantity being requested.
     * @return Returns the items that were extracted.
     */
    ItemStack extractItems(ItemStack stack);
}
