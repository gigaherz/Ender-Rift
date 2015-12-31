package gigaherz.enderRift.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public interface IInventoryAutomation
{
    /**
     * Gets the corresponding sub-inventory for the given side.
     * @param face The requested side.
     * @return Returns an inventory instance representing the requested side's slots. Can be null.
     */
    IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face);

    /**
     * Tries to insert items into the inventory.
     * @param stack The items to insert.
     * @return Returns the remaining items it was unable to insert. Can be null.
     */
    ItemStack pushItems(@Nonnull ItemStack stack);

    /**
     * Tries to pull from the first available stack from the inventory.
     * @param limit The maximum number of items to pull.
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack pullItems(int limit);

    /**
     * Tries to extract a specific amount of a certain item, as defined by the provided ItemStack.
     * @param stack The item to extract, and the quantity being requested.
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack extractItems(@Nonnull ItemStack stack);
}
