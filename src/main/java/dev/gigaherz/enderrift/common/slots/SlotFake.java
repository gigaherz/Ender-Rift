package dev.gigaherz.enderrift.common.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotFake extends SlotItemHandler
{
    public SlotFake(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn)
    {
        return false;
    }
}