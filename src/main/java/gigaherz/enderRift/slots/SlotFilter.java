package gigaherz.enderRift.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotFilter extends Slot
{
    public SlotFilter(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public void onPickupFromSlot( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
    {
    }

    @Override
    public ItemStack decrStackSize( final int par1 )
    {
        return null;
    }

    @Override
    public boolean isItemValid( final ItemStack par1ItemStack )
    {
        return false;
    }

    @Override
    public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
    {
        return false;
    }
}
