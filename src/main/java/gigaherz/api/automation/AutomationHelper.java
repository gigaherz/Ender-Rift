package gigaherz.api.automation;

import com.google.common.base.Predicate;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

/**
 * Provides a basic implementation for automatable inventory.
 */
public class AutomationHelper implements IInventoryAutomation
{
    IInventory parent;

    /**
     * Gets an inventory automation instance, wrapping an inventory if necessary.
     * @param object The object to get an inventory from.
     * @param facing The face from which the automation is happening.
     * @return Returns an instance of an easy-to-automate inventory.
     */
    public static IInventoryAutomation get(Object object, EnumFacing facing)
    {
        // If the inventory is already automated, return it.
        if (object instanceof IInventoryAutomation)
            return ((IInventoryAutomation)object).getInventoryForSide(facing);

        // If the inventory is better sided, make use of it.
        if (object instanceof IBetterSidedInventory)
            return new AutomationHelper(((IBetterSidedInventory) object).getInventoryForSide(facing));

        // If the inventory is plain old sided, wrap it in a side-aware way.
        if (object instanceof ISidedInventory)
        {
            ISidedInventory sided = (ISidedInventory) object;
            return new AutomationHelper(InventorySlotsWrapper.create(sided, sided.getSlotsForFace(facing)));
        }

        // If it's just a plain old inventory, wrap it.
        if (object instanceof IInventory)
            return new AutomationHelper((IInventory)object);

        // If none of the above worked, give up.
        return null;
    }

    public AutomationHelper(IInventory parent)
    {
        this.parent = parent;
    }

    @Override
    public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
    {
        return this;
    }

    /**
     * Tries to put items into an inventory. By default,
     * it will add to existing slots before filling in a new slot.
     * @param stack The items to insert.
     * @return Returns the remaining items in the stack.
     */
    @Override
    public ItemStack pushItems(@Nonnull ItemStack stack)
    {
        ItemStack remaining = stack.copy();
        int firstEmpty = -1;

        // Try to fill existing slots first
        for(int i=0;i<parent.getSizeInventory();i++)
        {
            ItemStack slot = parent.getStackInSlot(i);
            if(slot != null)
            {
                int max = Math.min(remaining.getMaxStackSize(), parent.getInventoryStackLimit());
                int transfer = Math.min(remaining.stackSize, max - slot.stackSize);
                if (transfer > 0 && ItemStack.areItemsEqual(slot, remaining) && ItemStack.areItemStackTagsEqual(slot, remaining))
                {
                    slot.stackSize += transfer;
                    remaining.stackSize -= transfer;
                    if(remaining.stackSize == 0)
                        break;
                }
            }
            else if(firstEmpty < 0)
            {
                firstEmpty = i;
            }
        }

        // Then place the remaining items in the first available empty slot
        if(remaining.stackSize > 0 && firstEmpty >= 0)
        {
            for(int i=0;i<parent.getSizeInventory();i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if(slot == null)
                {
                    int max = Math.min(remaining.getMaxStackSize(), parent.getInventoryStackLimit());
                    int transfer = Math.min(remaining.stackSize, max);
                    if (transfer > 0)
                    {
                        ItemStack insert = remaining.copy();
                        insert.stackSize = transfer;
                        parent.setInventorySlotContents(i, insert);
                        remaining.stackSize -= transfer;
                    }
                }
            }
        }

        if(remaining.stackSize > 0)
            return remaining;

        return null;
    }

    /**
     * Pulls items from the inventory. By default,
     * it will pull from the first accepted slot.
     * @param limit  The maximum number of items to pull.
     * @param filter A filtering function to apply to the items.
     * @return Returns the first matching stack, with up to {limit} items.
     */
    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        for(int i=0;i<parent.getSizeInventory();i++)
        {
            ItemStack slot = parent.getStackInSlot(i);
            if(slot != null)
            {
                int available = Math.min(limit, slot.stackSize);
                if(filter.apply(slot) && available > 0)
                {
                    ItemStack pulled = slot.splitStack(available);
                    if(slot.stackSize <= 0)
                        parent.setInventorySlotContents(i, null);
                    return pulled;
                }
            }
        }
        return null;
    }

    /**
     * Extracts the first matching stack, with at least as many items as requested. By default,
     * it will scan the entire inventory until the item is found, but implementations can choose
     * to use a multimap or similar for indexing large inventories.
     * @param stack The item to extract.
     * @param wanted The quantity being requested.
     * @return Returns the matching stack, up to the specified stackSize.
     */
    @Override
    public ItemStack extractItems(@Nonnull ItemStack stack, int wanted)
    {
        ItemStack extracted = stack.copy();
        extracted.stackSize = 0;

        if(stack.stackSize <= 0)
            return null;

        for(int i=0;i<parent.getSizeInventory();i++)
        {
            ItemStack slot = parent.getStackInSlot(i);
            if(slot != null)
            {
                int available = Math.min(wanted, slot.stackSize);
                if (ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack) && available > 0)
                {
                    slot.stackSize -= available;
                    extracted.stackSize += available;
                    if(slot.stackSize <= 0)
                        parent.setInventorySlotContents(i, null);

                    wanted = extracted.stackSize - stack.stackSize;
                    if(wanted <= 0)
                        break;
                }
            }
        }

        if(extracted.stackSize <= 0)
            return null;

        return extracted;
    }
}
