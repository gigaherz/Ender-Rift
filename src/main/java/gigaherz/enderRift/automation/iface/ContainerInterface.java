package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.common.slots.SlotFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerInterface
        extends Container
{
    protected TileInterface tile;

    public ContainerInterface(TileInterface tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;

        IItemHandler filters = tileEntity.inventoryFilter();
        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new SlotFilter(filters, x, 8 + x * 18, 33));
        }

        IItemHandler outputs = tileEntity.inventoryOutputs();
        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new SlotItemHandler(outputs, x, 8 + x * 18, 62));
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
    public ItemStack slotClick(int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn)
    {
        if (slotId >= 0 && slotId < 9)
        {
            if (mode == ClickType.PICKUP || mode == ClickType.PICKUP_ALL ||
                    mode == ClickType.SWAP) // 1 is shift-click
            {
                Slot slot = this.inventorySlots.get(slotId);

                ItemStack dropping = playerIn.inventory.getItemStack();

                if (dropping.getCount() > 0)
                {
                    ItemStack copy = dropping.copy();
                    copy.setCount(1);
                    slot.putStack(copy);
                }
                else if (slot.getStack() != null)
                {
                    slot.putStack(null);
                }

                return slot.getStack().copy();
            }

            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        if (slotIndex < 8)
        {
            return ItemStack.EMPTY;
        }

        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex < 9)
        {
            return ItemStack.EMPTY;
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
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return stackCopy;
    }
}
