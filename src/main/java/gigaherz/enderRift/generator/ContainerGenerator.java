package gigaherz.enderRift.generator;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.network.UpdateField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
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
        prevFields = this.tile.getFields();
        for (int i = 0; i < prevFields.length; i++) { prevFields[i]--; }

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

        int[] fields = this.tile.getFields();
        for (int i = 0; i < prevFields.length; i++)
        {
            if (prevFields[i] != fields[i])
            {
                prevFields[i] = fields[i];
                needUpdate = true;
            }
        }

        if (needUpdate)
        {
            this.listeners.stream().filter(watcher -> watcher instanceof EntityPlayerMP).forEach(watcher ->
                    EnderRiftMod.channel.sendTo(new UpdateField(this.windowId, prevFields), (EntityPlayerMP) watcher));
        }
    }

    public void updateFields(int[] data)
    {
        this.tile.setFields(data);
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
            return ItemStack.EMPTY;
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
                return ItemStack.EMPTY;

            startIndex = 0;
            endIndex = 1;
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
