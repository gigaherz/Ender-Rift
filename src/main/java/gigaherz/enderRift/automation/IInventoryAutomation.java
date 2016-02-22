package gigaherz.enderRift.automation;

import com.google.common.base.Predicate;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IInventoryAutomation
{
    /**
     * Gets the current number of slots in the inventory.
     * Named after the IInventory method for convenience.
     *
     * @return Returns the number of slots.
     */
    int getSizeInventory();

    /**
     * Gets the contents of the slot in the inventory.
     * Named after the IInventory method for convenience.
     * This stack should only ever be used for display,
     * WARNING: IMPORTANT ---> DO NOT MODIFY THE STACK! <--- IMPORTANT: WARNING
     * You should always use the IInventoryAutomation methods to add/remove items!
     *
     * @param index The index of the slot to peek at.
     * @return Returns the stack contained in the slot. DO NOT MODIFY!
     */
    ItemStack getStackInSlot(int index);


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
     * @param stack    The item to extract.
     * @param wanted   The quantity being requested.
     * @param simulate
     * @return Returns the items that were extracted. Can be null.
     */
    ItemStack extractItems(@Nonnull ItemStack stack, int wanted, boolean simulate);
}
