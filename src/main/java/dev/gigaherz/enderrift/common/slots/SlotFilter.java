package dev.gigaherz.enderrift.common.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SlotFilter extends SlotItemHandler
{
    public SlotFilter(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack)
    {
    }

    @Override
    public ItemStack remove(final int par1)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(final ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean mayPickup(final Player par1EntityPlayer)
    {
        return false;
    }
}