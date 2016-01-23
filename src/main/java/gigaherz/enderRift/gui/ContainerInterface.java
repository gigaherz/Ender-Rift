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

        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new SlotFilter(tileEntity.inventoryFilter(), x, 8 + x * 18, 33));
        }
        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new Slot(tileEntity, x, 8 + x * 18, 62));
        }

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
                        8 + x * 18, 94 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 152));
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
