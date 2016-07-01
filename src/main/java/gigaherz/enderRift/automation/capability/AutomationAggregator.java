package gigaherz.enderRift.automation.capability;

import com.google.common.collect.Lists;
import gigaherz.enderRift.ConfigValues;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class AutomationAggregator implements IInventoryAutomation
{
    final List<IInventoryAutomation> aggregated = Lists.newArrayList();

    public void addAll(Iterable<IInventoryAutomation> inventorySet)
    {
        for (IInventoryAutomation value : inventorySet)
        {
            add(value);
        }
    }

    public void add(IInventoryAutomation inv)
    {
        aggregated.add(inv);
    }

    @Override
    public int getSlots()
    {
        int sum = 0;
        for (IInventoryAutomation inv : aggregated)
        {
            sum += inv.getSlots();
        }
        return sum;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        for (IInventoryAutomation inv : aggregated)
        {
            int size = inv.getSlots();
            if (index < size)
            {
                return inv.getStackInSlot(index);
            }

            index -= size;
        }
        return null;
    }

    @Override
    public ItemStack insertItems(@Nonnull ItemStack stack)
    {
        ItemStack remaining = stack.copy();

        // New feature: try to push into existing inventories that contain the item, first
        if (ConfigValues.PreferContainersWithExistingStacks)
        {
            // DO NOT CHANGE BACK TO FOREACH, CAUSES ConcurrentModificationException
            for (int i = 0; i < aggregated.size(); i++)
            {
                IInventoryAutomation inv = aggregated.get(i);

                for (int j = 0; j < inv.getSlots(); j++)
                {
                    ItemStack existing = inv.getStackInSlot(j);
                    if (ItemStack.areItemsEqual(stack, existing) && ItemStack.areItemStackTagsEqual(stack, existing))
                    {
                        remaining = inv.insertItems(remaining);
                        break;
                    }
                }

                if (remaining == null)
                    return null;
            }
        }

        // DO NOT CHANGE BACK TO FOREACH, CAUSES ConcurrentModificationException
        for (int i = 0; i < aggregated.size(); i++)
        {
            IInventoryAutomation inv = aggregated.get(i);
            remaining = inv.insertItems(remaining);
            if (remaining == null)
                return null;
        }

        return remaining;
    }

    @Override
    public ItemStack extractItems(@Nonnull ItemStack stack, int wanted, boolean simulate)
    {
        wanted = Math.min(wanted, stack.getMaxStackSize());

        ItemStack extracted = null;

        // DO NOT CHANGE BACK TO FOREACH, CAUSES ConcurrentModificationException
        for (int i = 0; i < aggregated.size(); i++)
        {
            IInventoryAutomation inv = aggregated.get(i);
            ItemStack obtained = inv.extractItems(stack, wanted, simulate);
            if (obtained != null)
            {
                if (extracted == null)
                {
                    extracted = obtained.copy();
                }
                else
                {
                    extracted.stackSize += obtained.stackSize;
                }
                wanted -= obtained.stackSize;
                if (wanted <= 0)
                    break;
            }
        }
        return extracted;
    }
}
