package dev.gigaherz.enderrift.rift;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public interface ILongItemHandler extends IItemHandler
{
    long getCount(int slot);

    static ILongItemHandler wrap(IItemHandler handler)
    {
        if (handler instanceof ILongItemHandler longHandler)
            return longHandler;

        return new ILongItemHandler()
        {
            private final IItemHandler inner = handler;

            @Override
            public int getSlots()
            {
                return inner.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot)
            {
                return inner.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
            {
                return inner.insertItem(slot, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                return inner.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot)
            {
                return inner.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack)
            {
                return inner.isItemValid(slot, stack);
            }

            @Override
            public long getCount(int slot)
            {
                return inner.getStackInSlot(slot).getCount();
            }
        };
    }
}
