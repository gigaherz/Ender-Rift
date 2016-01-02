package gigaherz.api.automation;

import net.minecraft.item.ItemStack;

public interface IBrowsableInventory
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
}
