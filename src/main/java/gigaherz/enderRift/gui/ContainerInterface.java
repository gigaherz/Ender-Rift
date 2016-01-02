package gigaherz.enderRift.gui;

import gigaherz.enderRift.blocks.TileInterface;
import gigaherz.enderRift.slots.SlotFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInterface
        extends Container
{
    protected TileInterface tile;

    public ContainerInterface(TileInterface tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new SlotFilter(tileEntity.inventoryFilter(), i, 8 + i * 18, 33));
        }
        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(tileEntity, i, 8 + i * 18, 62));
        }

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
                        8 + j * 18, 94 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 152));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn)
    {
        if (slotId >= 0 && slotId < 9)
        {
            if (mode == 0 || mode == 1) // 1 is shift-click
            {
                Slot slot = this.inventorySlots.get(slotId);

                ItemStack dropping = playerIn.inventory.getItemStack();

                if (dropping != null)
                {
                    ItemStack copy = dropping.copy();
                    copy.stackSize = 1;
                    slot.putStack(copy);
                }
                else if (slot.getStack() != null)
                {
                    slot.putStack(null);
                }

                if (slot.getStack() != null)
                    return slot.getStack().copy();
            }

            return null;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        if (slotIndex < 8)
        {
            return null;
        }

        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex < 9)
        {
            return null;
        }
        else if (slotIndex < 18)
        {
            startIndex = 18;
            endIndex = 18 + 27 + 9;
        }
        else
        {
            startIndex = 9;
            endIndex = 18;
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
