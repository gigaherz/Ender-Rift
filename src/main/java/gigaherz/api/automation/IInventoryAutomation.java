package gigaherz.api.automation;

import com.google.common.base.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public interface IInventoryAutomation
{
    /**
     * Gets the corresponding sub-inventory for the given side.
     *
     * @param face The requested side.
     * @return Returns an inventory instance representing the requested side's slots. Can be null.
     */
    IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face);

    /**
     * Tries to insert items into the inventory.
     *
     * @param stack The items to insert.
     * @return Returns the remaining items it was unable to insert. Can be null.
     */
    ItemStack pushItems(@Nonnull ItemStack stack);

    /**
     * Tries to pull from the first available stack from the inventory.
     *
     * @param limit The maximum number of items to pull.
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack pullItems(int limit, Predicate<ItemStack> filter);

    /**
     * Tries to extract a specific amount of a certain item, as defined by the provided ItemStack.
     * Will attempt to gather from more than one stack.
     *
     * @param stack  The item to extract.
     * @param wanted The quantity being requested.
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack extractItems(@Nonnull ItemStack stack, int wanted);

    /**
     * Pretends to extract a specific amount of a certain item, but does not actually perform the extraction.
     * Will attempt to gather from more than one stack.
     *
     * @param stack  The item to extract.
     * @param wanted The quantity being requested.
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack simulateExtraction(@Nonnull ItemStack stack, int wanted);
}
