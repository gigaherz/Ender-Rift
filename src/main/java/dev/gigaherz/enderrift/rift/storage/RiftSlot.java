package dev.gigaherz.enderrift.rift.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class RiftSlot
{
    private final ItemStack sample;
    private long count;

    public RiftSlot(ItemStack stack)
    {
        this(stack, stack.getCount());
    }

    public RiftSlot(ItemStack stack, long count)
    {
        this.sample = stack.copy();
        this.count = count;
        updateCount();
    }

    private void updateCount()
    {
        sample.setCount((int) Math.min(this.count, Integer.MAX_VALUE));
    }

    public Item getItem()
    {
        return sample.getItem();
    }

    @Nullable
    public CompoundTag getTag()
    {
        return sample.getTag();
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
        updateCount();
    }

    public void subtractCount(long count)
    {
        this.count -= count;
        updateCount();
    }

    public void setCount(long count)
    {
        this.count = count;
        updateCount();
    }

    @Override
    public String toString()
    {
        return sample.getItem() + " x " + count;
    }
}
