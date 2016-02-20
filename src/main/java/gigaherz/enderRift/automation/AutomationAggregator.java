package gigaherz.enderRift.automation;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.List;

public class AutomationAggregator implements IInventoryAutomation
{
    final List<IInventoryAutomation> aggregated = Lists.newArrayList();

    public void addAll(Iterable<IInventoryAutomation> inventorySet)
    {
        for (IInventoryAutomation value : inventorySet)
        { add(value); }
    }

    public void add(IInventoryAutomation inv)
    {
        aggregated.add(inv);
    }

    public void clear()
    {
        aggregated.clear();
    }

    @Override
    public int getSizeInventory()
    {
        int sum = 0;
        for (IInventoryAutomation inv : aggregated)
        {
            sum += inv.getSizeInventory();
        }
        return sum;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        for (IInventoryAutomation inv : aggregated)
        {
            int size = inv.getSizeInventory();
            if (index < size)
            {
                return inv.getStackInSlot(index);
            }

            index -= size;
        }
        return null;
    }

    @Override
    public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
    {
        return this;
    }

    @Override
    public ItemStack pushItems(@Nonnull ItemStack stack)
    {
        ItemStack remaining = null;

        // DO NOT CHANGE BACK TO FOREACH, CAUSES ConcurrentModificationException
        for (int i = 0; i < aggregated.size(); i++)
        {
            IInventoryAutomation inv = aggregated.get(i);
            remaining = inv.pushItems(stack);
            if (remaining == null)
                return null;
        }
        return remaining;
    }

    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        // DO NOT CHANGE BACK TO FOREACH, CAUSES ConcurrentModificationException
        for (int i = 0; i < aggregated.size(); i++)
        {
            IInventoryAutomation inv = aggregated.get(i);
            ItemStack obtained = inv.pullItems(limit, filter);
            if (obtained != null)
                return obtained;
        }
        return null;
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
