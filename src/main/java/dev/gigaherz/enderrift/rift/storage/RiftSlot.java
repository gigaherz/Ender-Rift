package dev.gigaherz.enderrift.rift.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class RiftSlot
{

    private final ItemStack sample;
    private long count;

    public RiftSlot(ItemStack stack)
    {
        this.sample = stack.copy();
        this.count = stack.getCount();
    }

    public RiftSlot(CompoundTag tag)
    {
        this.count = tag.getLong("Count");
        this.sample = ItemStack.of(tag.getCompound("Item"));
        sample.setCount((int) Math.min(this.count, Integer.MAX_VALUE));
    }

    public ItemStack getSample()
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
        sample.setCount((int) Math.min(this.count, Integer.MAX_VALUE));
    }

    public void subtractCount(long count)
    {
        this.count -= count;
        sample.setCount((int) Math.min(this.count, Integer.MAX_VALUE));
    }

    public void setCount(long count)
    {
        this.count = count;
        sample.setCount((int) Math.min(this.count, Integer.MAX_VALUE));
    }
}
