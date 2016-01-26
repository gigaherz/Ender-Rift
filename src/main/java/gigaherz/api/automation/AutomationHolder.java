package gigaherz.api.automation;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.List;

public class AutomationHolder implements IInventoryAutomation, IBrowsableInventory
{
    private final List<ItemStack> inventorySlots = Lists.newArrayList();

    // Override to handle
    public void onContentsChanged()
    {

    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList itemList = nbtTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < itemList.tagCount(); ++i)
        {
            NBTTagCompound slot = itemList.getCompoundTagAt(i);
            inventorySlots.add(ItemStack.loadItemStackFromNBT(slot));
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList itemList = new NBTTagList();

        for (ItemStack stack : inventorySlots)
        {
            if (stack != null)
            {
                NBTTagCompound slot = new NBTTagCompound();
                stack.writeToNBT(slot);
                itemList.appendTag(slot);
            }
        }

        nbtTagCompound.setTag("Items", itemList);
    }

    @Override
    public int getSizeInventory()
    {
        return inventorySlots.size();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventorySlots.get(index);
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

        // Try to fill existing slots first
        for (ItemStack slot : inventorySlots)
        {
            if (slot != null)
            {
                int max = Math.min(remaining.getMaxStackSize(), 64);
                int transfer = Math.min(remaining.stackSize, max - slot.stackSize);
                if (transfer > 0 && ItemStack.areItemsEqual(slot, remaining) && ItemStack.areItemStackTagsEqual(slot, remaining))
                {
                    slot.stackSize += transfer;
                    remaining.stackSize -= transfer;
                    if (remaining.stackSize <= 0)
                        break;
                }
            }
        }

        // Then place any remaining items in the first available empty slot
        if (remaining.stackSize > 0)
            inventorySlots.add(remaining);

        onContentsChanged();

        return null;
    }

    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        for (int i = 0; i < inventorySlots.size(); i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if (slot != null)
            {
                int available = Math.min(limit, slot.stackSize);
                if (filter.apply(slot) && available > 0)
                {
                    ItemStack pulled = slot.splitStack(available);
                    if (slot.stackSize <= 0)
                        inventorySlots.remove(i);
                    onContentsChanged();
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

        for (int i = 0; i < inventorySlots.size(); i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if (slot != null)
            {
                int available = Math.min(wanted, slot.stackSize);
                if (ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack) && available > 0)
                {
                    slot.stackSize -= available;
                    extracted.stackSize += available;
                    if (slot.stackSize <= 0)
                        inventorySlots.remove(i);

                    wanted = extracted.stackSize - stack.stackSize;
                    if (wanted <= 0)
                        break;
                }
            }
        }

        if (extracted.stackSize <= 0)
            return null;

        onContentsChanged();
        return extracted;
    }

    @Override
    public ItemStack simulateExtraction(@Nonnull ItemStack stack, int wanted)
    {
        ItemStack extracted = stack.copy();
        extracted.stackSize = 0;

        if (stack.stackSize <= 0)
            return null;

        for (ItemStack slot : inventorySlots)
        {
            if (slot != null)
            {
                int available = Math.min(wanted, slot.stackSize);
                if (ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack) && available > 0)
                {
                    extracted.stackSize += available;
                    wanted = extracted.stackSize - stack.stackSize;
                    if (wanted <= 0)
                        break;
                }
            }
        }

        if (extracted.stackSize <= 0)
            return null;

        return extracted;
    }
}
