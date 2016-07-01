package gigaherz.enderRift.common.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotFilter extends SlotItemHandler
{
    public SlotFilter(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public void onPickupFromSlot(final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack)
    {
    }

    @Override
    public ItemStack decrStackSize(final int par1)
    {
        return null;
    }

    @Override
    public boolean isItemValid(final ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean canTakeStack(final EntityPlayer par1EntityPlayer)
    {
        return false;
    }
}
