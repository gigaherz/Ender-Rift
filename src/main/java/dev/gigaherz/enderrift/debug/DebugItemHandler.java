package dev.gigaherz.enderrift.debug;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class DebugItemHandler implements IItemHandler {

    private long seed;
    private long iterations = 0L;

    private final DebugItemBlockEntity entity;

    public DebugItemHandler(final DebugItemBlockEntity entity) {
        this.entity = entity;
        this.seed = System.currentTimeMillis();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getIterations() {
        return iterations;
    }

    public void setIterations(long iterations) {
        this.iterations = iterations;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        long prev = iterations;
        ItemStack stack = next();
        iterations = prev;
        return stack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (simulate) {
            return getStackInSlot(slot);
        }
        return next();
    }

    @Override
    public int getSlotLimit(int slot) {
        return getStackInSlot(slot).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }

    private int nextInt(int max) {
        if (max <= 0) {
            return 0;
        }
        if (iterations == Long.MAX_VALUE) {
            iterations = 0;
        }
        long num = (iterations++) * 3432918353L;
        num = (num << 15 | num >> 17);
        num *= 461845907L;
        long num2 = seed ^ num;
        num2 = (num2 << 13 | num2 >> 19);
        num2 = num2 * 5L + 3864292196L;
        num2 ^= 2834544218L;
        num2 ^= num2 >> 16;
        num2 *= 2246822507L;
        num2 ^= num2 >> 13;
        num2 *= 3266489909L;
        int number = (int) (num2 ^ num2 >> 16);
        if (!entity.isRemoved()) {
            entity.setChanged();
        }
        return Math.abs(number % max);
    }

    private ItemStack next() {
        return next(false);
    }

    private ItemStack next(boolean nulled) {
        Holder<Item> holder = DebugKeyCache.getItem(nextInt(DebugKeyCache.size()));
        if (holder == null) {
            DebugKeyCache.update();
            if (nulled) {
                return ItemStack.EMPTY;
            }
            return next(true);
        }
        ItemStack stack = new ItemStack(holder);
        stack.setCount(1 + nextInt(stack.getMaxStackSize() - 1));
        return stack;
    }
}
