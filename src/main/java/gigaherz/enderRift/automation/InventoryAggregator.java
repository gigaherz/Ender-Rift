package gigaherz.enderRift.automation;

import com.google.common.collect.Lists;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class InventoryAggregator implements IItemHandler
{
    final List<IItemHandler> aggregated = Lists.newArrayList();

    public void addAll(Iterable<IItemHandler> inventorySet)
    {
        for (IItemHandler value : inventorySet)
        {
            add(value);
        }
    }

    public void add(IItemHandler inv)
    {
        aggregated.add(inv);
    }

    @Override
    public int getSlots()
    {
        int sum = 0;
        for (IItemHandler inv : aggregated)
        {
            sum += inv.getSlots();
        }
        return sum;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        for (IItemHandler inv : aggregated)
        {
            int size = inv.getSlots();
            if (index < size)
            {
                return inv.getStackInSlot(index);
            }

            index -= size;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate)
    {
        // KEEP indexed!
        for (int i = 0; i < aggregated.size(); i++)
        {
            IItemHandler inv = aggregated.get(i);
            int size = inv.getSlots();
            if (index < size)
            {
                return inv.insertItem(index, stack, simulate);
            }

            index -= size;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate)
    {
        // KEEP indexed!
        for (int i = 0; i < aggregated.size(); i++)
        {
            IItemHandler inv = aggregated.get(i);
            int size = inv.getSlots();
            if (index < size)
            {
                return inv.extractItem(index, amount, simulate);
            }

            index -= size;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int index)
    {
        for (IItemHandler inv : aggregated)
        {
            int size = inv.getSlots();
            if (index < size)
            {
                return inv.getSlotLimit(index);
            }

            index -= size;
        }
        return 0;
    }
}
