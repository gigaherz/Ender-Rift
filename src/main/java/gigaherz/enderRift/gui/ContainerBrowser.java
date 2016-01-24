package gigaherz.enderRift.gui;

import com.google.common.collect.Lists;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.network.SetSpecialSlot;
import gigaherz.enderRift.slots.SlotFake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class ContainerBrowser
        extends Container
{
    public final FakeInventory fakeInventory;
    protected TileBrowser tile;
    int prevChangeCount;

    final static int Left = 8;
    final static int Top = 33;
    final static int SlotWidth = 18;
    final static int SlotHeight = 28;

    final static int FakeRows = 3;
    final static int FakeColumns = 9;
    final static int FakeSlots = FakeRows * FakeColumns;

    final static int PlayerRows = 4;
    final static int PlayerColumns = 9;
    final static int PlayerSlots = PlayerRows * PlayerColumns;

    public ContainerBrowser(TileBrowser tileEntity, InventoryPlayer playerInventory)
    {
        this.tile = tileEntity;

        fakeInventory = new FakeInventory(FakeSlots);
        prevChangeCount = tile.changeCount;


        for (int y = 0; y < FakeRows; y++)
        {
            for (int x = 0; x < FakeColumns; x++)
            {
                addSlotToContainer(new Slot(fakeInventory,
                        x + y * FakeColumns,
                        Left + x * SlotWidth, Top + y * SlotHeight));
            }
        }

        bindPlayerInventory(playerInventory, Top + FakeRows * SlotHeight + 14);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory, int top)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlotToContainer(new Slot(playerInventory,
                        x + y * 9 + 9,
                        Left + x * SlotWidth, top + y * SlotHeight));
            }
        }

        top += 3 * SlotHeight + 4;
        for (int x = 0; x < 9; x++)
        {
            addSlotToContainer(new Slot(playerInventory, x, Left + x * SlotWidth, top));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void detectAndSendChanges()
    {
        if(prevChangeCount != tile.changeCount)
        {
            fakeInventory.refresh();
        }

        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            Slot slot = this.inventorySlots.get(i);
            ItemStack newStack = slot.getStack();
            ItemStack current = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(current, newStack))
            {
                current = newStack == null ? null : newStack.copy();
                this.inventoryItemStacks.set(i, current);

                for (ICrafting crafter : this.crafters)
                {
                    if (slot instanceof SlotFake && crafter instanceof EntityPlayerMP)
                        sendSpecialSlotContents((EntityPlayerMP) crafter, this, i, current);
                    else
                        crafter.sendSlotContents(this, i, current);
                }
            }
        }
    }

    private void sendSpecialSlotContents(EntityPlayerMP crafter, ContainerBrowser containerEssentializer, int i, ItemStack current)
    {
        EnderRiftMod.channel.sendTo(new SetSpecialSlot(containerEssentializer.windowId, i, current), crafter);
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        super.putStackInSlot(slotID, stack);
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

        if (slotIndex < FakeSlots)
        {
            startIndex = FakeSlots;
            endIndex = startIndex + PlayerSlots;
        }
        else
        {
            startIndex = 0;
            endIndex = startIndex + FakeSlots;
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

    private class FakeInventory implements IInventory
    {
        final int slots;
        final List<ItemStack> singleItems = Lists.newArrayList();
        final List<ItemStack> accumulated = Lists.newArrayList();

        int scroll;

        public FakeInventory(int slots)
        {
            this.slots = slots;
        }

        void setScroll(int scroll)
        {
            this.scroll = scroll;
        }

        public void refresh()
        {
            IBrowsableInventory inv = tile.getParent();

            for(int i=0;i<slots;i++)
            {
                singleItems.clear();
                accumulated.clear();
            }

            if(inv == null)
                return;

            int invSlots = inv.getSizeInventory();
            for(int j=0;j<invSlots;j++)
            {
                ItemStack invStack = inv.getStackInSlot(j);
                for(int i = 0; i< accumulated.size(); i++)
                {
                    ItemStack cachedStack = accumulated.get(i);

                    if (cachedStack == null)
                    {
                        ItemStack stack = invStack.copy();
                        ItemStack single = stack.copy();
                        single.stackSize = 1;
                        singleItems.add(single);
                        accumulated.add(stack);
                    }
                    else if (ItemStack.areItemsEqual(cachedStack,invStack) && ItemStack.areItemStackTagsEqual(cachedStack,invStack))
                    {
                        cachedStack.stackSize += invStack.stackSize;
                    }
                }
            }
        }

        @Override
        public int getSizeInventory()
        {
            return singleItems.size() - scroll;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if(index >= singleItems.size())
                return null;
            return singleItems.get(index);
        }

        public int getStackSizeForSlot(int index)
        {
            if(index >= singleItems.size())
                return 0;

            ItemStack stack = accumulated.get(index);
            if(stack == null)
                return 0;

            return stack.stackSize;
        }

        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            return null;
        }

        @Override
        public ItemStack removeStackFromSlot(int index)
        {
            return null;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack)
        {

        }

        @Override
        public int getInventoryStackLimit()
        {
            return 0;
        }

        @Override
        public void markDirty()
        {

        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return false;
        }

        @Override
        public void openInventory(EntityPlayer player)
        {

        }

        @Override
        public void closeInventory(EntityPlayer player)
        {

        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return false;
        }

        @Override
        public int getField(int id)
        {
            return 0;
        }

        @Override
        public void setField(int id, int value)
        {

        }

        @Override
        public int getFieldCount()
        {
            return 0;
        }

        @Override
        public void clear()
        {

        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public boolean hasCustomName()
        {
            return false;
        }

        @Override
        public IChatComponent getDisplayName()
        {
            return null;
        }
    }
}
