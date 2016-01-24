package gigaherz.enderRift.gui;

import com.google.common.collect.Lists;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.network.SetSpecialSlot;
import gigaherz.enderRift.slots.SlotFakeClient;
import gigaherz.enderRift.slots.SlotFakeServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class ContainerBrowser
        extends Container
{
    public final FakeInventoryClient fakeInventoryClient;
    public final FakeInventoryServer fakeInventoryServer;
    protected TileBrowser tile;
    int prevChangeCount;
    int actualSlotCount;

    final static int Left = 8;
    final static int Top = 18;
    final static int SlotWidth = 18;
    final static int SlotHeight = 18;

    final static int FakeRows = 3;
    final static int FakeColumns = 9;
    final static int FakeSlots = FakeRows * FakeColumns;

    final static int PlayerRows = 4;
    final static int PlayerColumns = 9;
    final static int PlayerSlots = PlayerRows * PlayerColumns;

    public ContainerBrowser(TileBrowser tileEntity, InventoryPlayer playerInventory, boolean isClient)
    {
        this.tile = tileEntity;

        if(isClient)
        {
            fakeInventoryClient = new FakeInventoryClient(FakeSlots);
            fakeInventoryServer = null;
            for (int y = 0; y < FakeRows; y++)
            {
                for (int x = 0; x < FakeColumns; x++)
                {
                    addSlotToContainer(new SlotFakeClient(fakeInventoryClient,
                            x + y * FakeColumns,
                            Left + x * SlotWidth, Top + y * SlotHeight));
                }
            }
        }
        else
        {
            fakeInventoryClient = null;
            fakeInventoryServer = new FakeInventoryServer();
            for (int y = 0; y < FakeRows; y++)
            {
                for (int x = 0; x < FakeColumns; x++)
                {
                    addSlotToContainer(new SlotFakeServer(fakeInventoryServer,
                            x + y * FakeColumns,
                            Left + x * SlotWidth, Top + y * SlotHeight));
                }
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
        if(prevChangeCount != tile.changeCount && !tile.getWorld().isRemote)
        {
            fakeInventoryServer.refresh();
            prevChangeCount = tile.changeCount;

            actualSlotCount = fakeInventoryServer.getSizeInventory();

            for (ICrafting crafter : this.crafters)
            {
                crafter.sendProgressBarUpdate(this, 0, actualSlotCount);
            }
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
                    if(i < FakeSlots)
                        EnderRiftMod.channel.sendTo(new SetSpecialSlot(windowId, i, current), (EntityPlayerMP) crafter);
                    else
                        crafter.sendSlotContents(this, i, current);
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        if(id == 0)
            actualSlotCount = data;
    }

    public void setScrollPosition(int scrollPosition)
    {
        this.fakeInventoryServer.setScroll(scrollPosition);
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        return null;

        /*
        int startIndex, endIndex;
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

        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

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
        */
    }

    public class FakeInventoryServer implements IInventory
    {
        final List<ItemStack> slots = Lists.newArrayList();

        int scroll;

        public FakeInventoryServer()
        {
        }

        void setScroll(int scroll)
        {
            this.scroll = scroll;
        }

        public void refresh()
        {
            IBrowsableInventory inv = tile.getParent();

            slots.clear();

            if(inv == null)
                return;

            int invSlots = inv.getSizeInventory();
            for(int j=0;j<invSlots;j++)
            {
                ItemStack invStack = inv.getStackInSlot(j);
                if(invStack == null)
                    continue;

                boolean found = false;

                for (ItemStack cachedStack : slots)
                {
                    if (ItemStack.areItemsEqual(cachedStack, invStack) && ItemStack.areItemStackTagsEqual(cachedStack, invStack))
                    {
                        cachedStack.stackSize += invStack.stackSize;
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    ItemStack stack = invStack.copy();
                    slots.add(stack);
                }
            }
        }

        @Override
        public int getSizeInventory()
        {
            return slots.size() - scroll;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if((index+scroll) >= slots.size())
                return null;
            return slots.get(index+scroll);
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
        public void setInventorySlotContents(int index, ItemStack invStack)
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

    public class FakeInventoryClient implements IInventory
    {
        ItemStack[] totals;
        ItemStack[] singles;

        public FakeInventoryClient(int slots)
        {
            totals = new ItemStack[slots];
            singles = new ItemStack[slots];
        }

        @Override
        public int getSizeInventory()
        {
            return totals.length;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if(index >= totals.length)
                return null;
            return singles[index];
        }

        public int getStackSizeForSlot(int index)
        {
            if(index >= totals.length)
                return 0;

            ItemStack stack = totals[index];
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
            if(index >= totals.length)
                return;

            totals[index] = stack;
            if(stack != null)
            {
                ItemStack single = stack.copy();
                single.stackSize = 1;
                singles[index] = single;
            }
            else
            {
                singles[index] = null;
            }
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
