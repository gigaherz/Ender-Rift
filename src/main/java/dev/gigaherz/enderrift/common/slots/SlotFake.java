package dev.gigaherz.enderrift.common.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotFake extends Slot
{
    public SlotFake(Container itemHandler, int index, int xPosition, int yPosition)
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