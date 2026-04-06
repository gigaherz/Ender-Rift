package dev.gigaherz.enderrift.automation;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class InventoryAggregator implements ResourceHandler<ItemResource>
{
    final List<ResourceHandler<ItemResource>> aggregated = Lists.newArrayList();

    public void addAll(Iterable<ResourceHandler<ItemResource>> inventorySet)
    {
        for (var value : inventorySet)
        {
            add(value);
        }
    }

    public void add(ResourceHandler<ItemResource> inv)
    {
        aggregated.add(inv);
    }

    @Override
    public int size()
    {
        int sum = 0;
        for (ResourceHandler<ItemResource> inv : aggregated)
        {
            sum += inv.size();
        }
        return sum;
    }

    @Override
    public ItemResource getResource(int index)
    {
        for (var inv : aggregated)
        {
            int size = inv.size();
            if (index < size)
            {
                return inv.getResource(index);
            }

            index -= size;
        }
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index)
    {
        for (var inv : aggregated)
        {
            int size = inv.size();
            if (index < size)
            {
                return inv.getAmountAsLong(index);
            }

            index -= size;
        }
        return 0;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        // KEEP indexed!
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < aggregated.size(); i++)
        {
            var inv = aggregated.get(i);
            int size = inv.size();
            if (index < size)
            {
                return inv.insert(index, resource, amount, transaction);
            }

            index -= size;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        // KEEP indexed!
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < aggregated.size(); i++)
        {
            var inv = aggregated.get(i);
            int size = inv.size();
            if (index < size)
            {
                return inv.extract(index, resource, amount, transaction);
            }

            index -= size;
        }
        return 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource)
    {
        for (var inv : aggregated)
        {
            int size = inv.size();
            if (index < size)
            {
                return inv.getCapacityAsLong(index, resource);
            }

            index -= size;
        }
        return 0;
    }

    @Override
    public boolean isValid(int index, ItemResource stack)
    {
        for (var inv : aggregated)
        {
            int size = inv.size();
            if (index < size)
            {
                return inv.isValid(index, stack);
            }

            index -= size;
        }
        return false;
    }
}