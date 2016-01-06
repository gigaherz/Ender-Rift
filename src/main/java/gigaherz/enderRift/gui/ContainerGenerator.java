package gigaherz.enderRift.gui;

import gigaherz.enderRift.blocks.TileGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerGenerator
        extends Container
{
    protected TileGenerator tile;
    private int[] prevFields;

    public ContainerGenerator(TileGenerator tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;
        prevFields = new int[tile.getFieldCount()];

        addSlotToContainer(new Slot(tileEntity, 0, 80, 53));

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(playerInventory,
                        j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (ICrafting watcher : this.crafters)
        {
            for (int i = 0; i < prevFields.length; i++)
            {
                int field = this.tile.getField(i);
                if (prevFields[i] != field)
                {
                    watcher.sendProgressBarUpdate(this, i, field);
                }
            }
        }

        for (int i = 0; i < prevFields.length; i++)
        {
            prevFields[i] = this.tile.getField(i);
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
