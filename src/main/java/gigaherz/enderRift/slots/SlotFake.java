package gigaherz.enderRift.slots;

import gigaherz.enderRift.gui.ContainerBrowser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotFake extends Slot
{
    ContainerBrowser parent;

    public SlotFake(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void putStack(ItemStack stack)
    {
        super.putStack(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn)
    {
        return super.canTakeStack(playerIn);
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        return super.decrStackSize(amount);
    }
}
