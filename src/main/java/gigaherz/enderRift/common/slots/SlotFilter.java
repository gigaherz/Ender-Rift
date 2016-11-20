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
    public ItemStack onTake(EntityPlayer p_190901_1_, ItemStack p_190901_2_)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(final int par1)
    {
        return ItemStack.EMPTY;
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
