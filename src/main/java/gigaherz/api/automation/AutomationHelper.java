package gigaherz.api.automation;

import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Provides a basic implementation for automatable inventory.
 */
public abstract class AutomationHelper implements IInventoryAutomation, IBrowsableInventory
{
    /**
     * Gets an inventory automation instance, wrapping an inventory if necessary.
     *
     * @param object The object to get an inventory from.
     * @param facing The face from which the automation is happening.
     * @return Returns an instance of an easy-to-automate inventory.
     */
    public static IInventoryAutomation get(Object object, EnumFacing facing)
    {
        // If the inventory is already automated, return it.
        if (object instanceof IInventoryAutomation)
            return ((IInventoryAutomation) object).getInventoryForSide(facing);

        // If the inventory is better sided, make use of it.
        if (object instanceof ICapabilityProvider)
        {
            ICapabilityProvider cap = (ICapabilityProvider) object;

            if (cap.hasCapability(CapabilityAutomation.AUTOMATION_CAPABILITY, facing))
                return cap.getCapability(CapabilityAutomation.AUTOMATION_CAPABILITY, facing);

            if (cap.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
                return new ItemHandlerWrapper(cap.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing));
        }

        // If the inventory is plain old sided, wrap it in a side-aware way.
        if (object instanceof ISidedInventory)
        {
            ISidedInventory sided = (ISidedInventory) object;
            return new IInventoryWrapper(InventorySlotsWrapper.create(sided, sided.getSlotsForFace(facing)));
        }

        // If it's just a plain old inventory, wrap it.
        if (object instanceof IInventory)
            return new IInventoryWrapper((IInventory) object);

        // If none of the above worked, give up.
        return null;
    }

    public static boolean isAutomatable(Object object, EnumFacing facing)
    {
        // If the inventory is already automated, return it.
        if (object instanceof IInventoryAutomation)
            return true;

        // If the inventory is better sided, make use of it.
        if (object instanceof ICapabilityProvider)
        {
            ICapabilityProvider cap = (ICapabilityProvider) object;

            if (cap.hasCapability(CapabilityAutomation.AUTOMATION_CAPABILITY, facing))
                return true;

            if (cap.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
                return true;
        }

        return object instanceof ISidedInventory || object instanceof IInventory;
    }

    private static class ItemHandlerWrapper extends AutomationHelper
    {
        IItemHandler parent;

        private ItemHandlerWrapper(IItemHandler parent)
        {
            this.parent = parent;
        }

        @Override
        public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
        {
            return this;
        }

        @Override
        public ItemStack pushItems(@Nonnull ItemStack stack)
        {
            ItemStack remaining = stack.copy();

            for (int i = 0; i < parent.getSlots(); i++)
            {
                remaining = parent.insertItem(i, remaining, false);
                if (remaining == null || remaining.stackSize <= 0)
                    break;
            }

            if (remaining != null && remaining.stackSize > 0)
                return remaining;

            return null;
        }

        @Override
        public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
        {
            for (int i = 0; i < parent.getSlots(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int available = Math.min(limit, slot.stackSize);
                    if (filter.apply(slot) && available > 0)
                    {
                        ItemStack pulled = parent.extractItem(i, available, false);
                        if (pulled != null && pulled.stackSize > 0)
                            return pulled;
                    }
                }
            }
            return null;
        }

        @Override
        public ItemStack extractItems(@Nonnull ItemStack stack, int wanted)
        {
            return extractItems(stack, wanted, false);
        }

        @Override
        public ItemStack simulateExtraction(@Nonnull ItemStack stack, int wanted)
        {
            return extractItems(stack, wanted, true);
        }

        private ItemStack extractItems(@Nonnull ItemStack stack, int wanted, boolean simulate)
        {
            if (stack.stackSize <= 0 || wanted <= 0)
                return null;

            ItemStack extracted = stack.copy();
            extracted.stackSize = 0;

            for (int i = 0; i < parent.getSlots(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int requested = Math.min(wanted, slot.stackSize);
                    if (requested > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
                    {
                        ItemStack obtained = parent.extractItem(i, requested, simulate);

                        int returned = (obtained != null) ? obtained.stackSize : 0;

                        extracted.stackSize += returned;

                        wanted -= returned;
                        if (wanted <= 0)
                            break;
                    }
                }
            }

            if (extracted.stackSize <= 0)
                return null;

            return extracted;
        }

        @Override
        public int getSizeInventory()
        {
            return parent.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            return parent.getStackInSlot(index);
        }
    }

    private static class IInventoryWrapper extends AutomationHelper
    {
        IInventory parent;

        private IInventoryWrapper(IInventory parent)
        {
            this.parent = parent;
        }

        @Override
        public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
        {
            return this;
        }

        @Override
        public ItemStack pushItems(@Nonnull ItemStack stack)
        {
            ItemStack remaining = stack.copy();
            int firstEmpty = -1;

            // Try to fill existing slots first
            for (int i = 0; i < parent.getSizeInventory(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int max = Math.min(remaining.getMaxStackSize(), parent.getInventoryStackLimit());
                    int transfer = Math.min(remaining.stackSize, max - slot.stackSize);
                    if (transfer > 0 && ItemStack.areItemsEqual(slot, remaining) && ItemStack.areItemStackTagsEqual(slot, remaining))
                    {
                        slot.stackSize += transfer;
                        remaining.stackSize -= transfer;
                        if (remaining.stackSize == 0)
                            break;
                    }
                }
                else if (firstEmpty < 0)
                {
                    firstEmpty = i;
                }
            }

            // Then place the remaining items in the first available empty slot
            if (remaining.stackSize > 0 && firstEmpty >= 0)
            {
                for (int i = 0; i < parent.getSizeInventory(); i++)
                {
                    ItemStack slot = parent.getStackInSlot(i);
                    if (slot == null)
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

            if (remaining.stackSize > 0)
                return remaining;

            return null;
        }

        @Override
        public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
        {
            for (int i = 0; i < parent.getSizeInventory(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int available = Math.min(limit, slot.stackSize);
                    if (filter.apply(slot) && available > 0)
                    {
                        ItemStack pulled = slot.splitStack(available);
                        if (slot.stackSize <= 0)
                            parent.setInventorySlotContents(i, null);
                        return pulled;
                    }
                }
            }
            return null;
        }

        @Override
        public ItemStack extractItems(@Nonnull ItemStack stack, int wanted)
        {
            ItemStack extracted = stack.copy();
            extracted.stackSize = 0;

            if (stack.stackSize <= 0)
                return null;

            for (int i = 0; i < parent.getSizeInventory(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int available = Math.min(wanted, slot.stackSize);
                    if (available > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
                    {
                        slot.stackSize -= available;
                        extracted.stackSize += available;
                        if (slot.stackSize <= 0)
                            parent.setInventorySlotContents(i, null);

                        wanted -= available;
                        if (wanted <= 0)
                            break;
                    }
                }
            }

            if (extracted.stackSize <= 0)
                return null;

            return extracted;
        }

        @Override
        public ItemStack simulateExtraction(@Nonnull ItemStack stack, int wanted)
        {
            ItemStack extracted = stack.copy();
            extracted.stackSize = 0;

            if (stack.stackSize <= 0)
                return null;

            for (int i = 0; i < parent.getSizeInventory(); i++)
            {
                ItemStack slot = parent.getStackInSlot(i);
                if (slot != null)
                {
                    int available = Math.min(wanted, slot.stackSize);
                    if (available > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
                    {
                        extracted.stackSize += available;
                        wanted -= available;
                        if (wanted <= 0)
                            break;
                    }
                }
            }

            if (extracted.stackSize <= 0)
                return null;

            return extracted;
        }

        @Override
        public int getSizeInventory()
        {
            return parent.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            return parent.getStackInSlot(index);
        }
    }

    private static class InventorySlotsWrapper implements IInventory
    {
        IInventory parent;
        int[] slots;

        private InventorySlotsWrapper(IInventory parent, int[] slots)
        {
            this.parent = parent;
            this.slots = slots;
        }

        public static InventorySlotsWrapper create(IInventory parent, int... slots)
        {
            return new InventorySlotsWrapper(parent, slots);
        }

        @Override
        public int getSizeInventory()
        {
            return slots.length;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if (index > slots.length)
                return null;
            return parent.getStackInSlot(slots[index]);
        }

        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            if (index > slots.length)
                return null;
            return parent.decrStackSize(slots[index], count);
        }

        @Override
        public ItemStack removeStackFromSlot(int index)
        {
            if (index > slots.length)
                return null;
            return parent.removeStackFromSlot(slots[index]);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            if (index > slots.length)
                return;
            parent.setInventorySlotContents(slots[index], stack);
        }

        @Override
        public int getInventoryStackLimit()
        {
            return parent.getInventoryStackLimit();
        }

        @Override
        public void markDirty()
        {
            parent.markDirty();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return false;
        }

        @Override
        public void openInventory(EntityPlayer player)
        {
        }

        @Override
        public void closeInventory(EntityPlayer player)
        {
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return index <= slots.length
                    && parent.isItemValidForSlot(slots[index], stack);
        }

        @Override
        public int getField(int id)
        {
            return 0;
        }

        @Override
        public void setField(int id, int value)
        {

        }

        @Override
        public int getFieldCount()
        {
            return 0;
        }

        @Override
        public void clear()
        {
            for (int i : slots)
            {
                parent.setInventorySlotContents(i, null);
            }
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public boolean hasCustomName()
        {
            return false;
        }

        @Override
        public IChatComponent getDisplayName()
        {
            return null;
        }
    }
}
