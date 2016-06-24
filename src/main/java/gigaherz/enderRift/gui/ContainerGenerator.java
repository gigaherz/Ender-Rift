package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileGenerator;
import gigaherz.enderRift.network.UpdateField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerGenerator
        extends Container
{
    protected TileGenerator tile;
    private int[] prevFields;

    public ContainerGenerator(TileGenerator tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;
        prevFields = new int[tile.getFieldCount()];
        for (int i = 0; i < prevFields.length; i++)
        {
            prevFields[i] = this.tile.getField(i)-1;
        }

        addSlotToContainer(new SlotItemHandler(tileEntity.inventory(), 0, 80, 53));

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlotToContainer(new Slot(playerInventory,
                        x + y * 9 + 9,
                        8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        boolean needUpdate = false;
        for (int i = 0; i < prevFields.length; i++)
        {
            int field = this.tile.getField(i);
            if (prevFields[i] != field)
            {
                prevFields[i] = this.tile.getField(i);
                needUpdate = true;
            }
        }

        if(needUpdate)
        {
            for (IContainerListener watcher : this.listeners)
            {
                if (watcher instanceof EntityPlayerMP)
                {
                    EnderRiftMod.channel.sendTo(new UpdateField(this.windowId, prevFields), (EntityPlayerMP) watcher);
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        this.tile.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex == 0)
        {
            startIndex = 1;
            endIndex = 1 + 4 * 9;
        }
        else
        {
            if (TileEntityFurnace.getItemBurnTime(slot.getStack()) <= 0)
                return null;

            startIndex = 0;
            endIndex = 1;
        }

        if (!this.mergeItemStack(stack, startIndex, endIndex, false))
        {
            return null;
        }

        if (stack.stackSize == 0)
        {
            slot.putStack(null);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.stackSize == stackCopy.stackSize)
        {
            return null;
        }

        slot.onPickupFromSlot(player, stack);
        return stackCopy;
    }
}
