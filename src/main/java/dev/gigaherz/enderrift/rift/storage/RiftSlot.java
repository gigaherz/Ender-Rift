package dev.gigaherz.enderrift.rift.storage;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class RiftSlot
{
    private final ItemResource sample;
    private long count;

    public RiftSlot(ItemResource stack, long count)
    {
        this.sample = stack;
        this.count = count;
    }

    public Item getItem()
    {
        return sample.getItem();
    }

    public ItemResource getResource()
    {
        return sample;
    }

    public long getCount()
    {
        return count;
    }

    public void addCount(long count)
    {
        this.count += count;
    }

    public void subtractCount(long count)
    {
        this.count -= count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    @Override
    public String toString()
    {
        return sample.getItem() + " x " + count;
    }

    public RiftSlot copy()
    {
        return new RiftSlot(sample, count);
    }
}
