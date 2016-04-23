package gigaherz.enderRift.gui;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.IInventoryAutomation;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.misc.SortMode;
import gigaherz.enderRift.network.SetFilterText;
import gigaherz.enderRift.network.SetScrollPosition;
import gigaherz.enderRift.network.SetSortMode;
import gigaherz.enderRift.network.SetSpecialSlot;
import gigaherz.enderRift.slots.SlotFake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Comparator;
import java.util.List;

public class ContainerBrowser
        extends Container
{
    public final FakeInventoryClient fakeInventoryClient;
    public final FakeInventoryServer fakeInventoryServer;
    protected TileBrowser tile;
    int prevChangeCount;
    int actualSlotCount;
    public int scroll;
    public SortMode sortMode = SortMode.StackSize;
    private String filterText = "";
    private ItemStack stackInCursor;

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

        IItemHandlerModifiable fake;
        if (isClient)
        {
            fakeInventoryClient = new FakeInventoryClient(FakeSlots);
            fakeInventoryServer = null;
            fake = fakeInventoryClient;
        }
        else
        {
            fakeInventoryClient = null;
            fakeInventoryServer = new FakeInventoryServer();
            fake = fakeInventoryServer;
        }

        for (int y = 0; y < FakeRows; y++)
        {
            for (int x = 0; x < FakeColumns; x++)
            {
                addSlotToContainer(new SlotFake(fake,
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
        if (prevChangeCount != tile.getChangeCount() && !tile.getWorld().isRemote)
        {
            fakeInventoryServer.refresh();
            prevChangeCount = tile.getChangeCount();

            actualSlotCount = fakeInventoryServer.getRealSizeInventory();

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
                    if (i < FakeSlots)
                        EnderRiftMod.channel.sendTo(new SetSpecialSlot(windowId, i, current), (EntityPlayerMP) crafter);
                    else
                        crafter.sendSlotContents(this, i, current);
                }
            }
        }

        for (ICrafting crafter : this.crafters)
        {
            if (!(crafter instanceof EntityPlayerMP))
                continue;

            EntityPlayerMP player = (EntityPlayerMP) crafter;
            ItemStack newStack = player.inventory.getItemStack();

            if (!ItemStack.areItemStacksEqual(stackInCursor, newStack))
            {
                stackInCursor = newStack == null ? null : newStack.copy();

                player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, newStack));
            }
        }
    }

    @Override
    public void putStacksInSlots(ItemStack[] p_75131_1_)
    {
        //super.putStacksInSlots(p_75131_1_);
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        if (id == 0)
            actualSlotCount = data;
    }

    public void setScrollPosition(int scrollPosition)
    {
        this.scroll = scrollPosition;

        if (tile.getWorld().isRemote)
        {
            EnderRiftMod.channel.sendToServer(new SetScrollPosition(windowId, scrollPosition));
        }
    }

    public void setSortMode(SortMode sortMode)
    {
        this.sortMode = sortMode;

        if (!tile.getWorld().isRemote)
        {
            fakeInventoryServer.resort();
        }
        else
        {
            EnderRiftMod.channel.sendToServer(new SetSortMode(windowId, sortMode));
        }
    }

    public void setFilterText(String text)
    {
        this.filterText = text;

        if (!tile.getWorld().isRemote)
        {
            fakeInventoryServer.refresh();
        }
        else
        {
            EnderRiftMod.channel.sendToServer(new SetFilterText(windowId, filterText));
        }
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn)
    {
        InventoryPlayer inventoryPlayer = playerIn.inventory;

        if (slotId >= 0 && slotId < FakeSlots)
        {
            IInventoryAutomation parent = tile.getAutomation();
            if (parent == null)
                return null;

            Slot slot = this.inventorySlots.get(slotId);
            ItemStack existing = slot.getStack();

            if (mode == 0)
            {
                int amount = 1;
                ItemStack dropping = inventoryPlayer.getItemStack();

                if (dropping != null && ItemStack.areItemsEqual(dropping, existing))
                {
                    if (clickedButton == 0)
                    {
                        if (dropping.stackSize < dropping.getMaxStackSize())
                        {
                            ItemStack extracted = parent.extractItems(existing, amount, false);
                            if (extracted != null)
                            {
                                dropping.stackSize += extracted.stackSize;
                                tile.markDirty();
                            }
                        }
                    }
                    else
                    {
                        ItemStack push = dropping.copy();
                        push.stackSize = amount;
                        ItemStack remaining = parent.insertItems(push);

                        dropping.stackSize -= push.stackSize;

                        if (remaining != null)
                        {
                            if (push.stackSize != remaining.stackSize)
                                tile.markDirty();
                            dropping.stackSize += remaining.stackSize;
                        }
                        else
                        {
                            tile.markDirty();
                        }

                        if (dropping.stackSize <= 0)
                            dropping = null;

                        inventoryPlayer.setItemStack(dropping);
                    }
                }
                else if (dropping != null)
                {
                    if (clickedButton == 0)
                    {
                        ItemStack remaining = parent.insertItems(dropping);
                        if (remaining != null)
                        {
                            if (dropping.stackSize != remaining.stackSize)
                                tile.markDirty();
                        }
                        else
                        {
                            tile.markDirty();
                        }
                        inventoryPlayer.setItemStack(remaining);
                    }
                    else
                    {
                        ItemStack push = dropping.copy();
                        push.stackSize = amount;
                        ItemStack remaining = parent.insertItems(push);

                        dropping.stackSize -= push.stackSize;

                        if (remaining != null)
                        {
                            if (push.stackSize != remaining.stackSize)
                                tile.markDirty();
                            dropping.stackSize += remaining.stackSize;
                        }
                        else
                        {
                            tile.markDirty();
                        }

                        if (dropping.stackSize <= 0)
                            dropping = null;

                        inventoryPlayer.setItemStack(dropping);
                    }
                }
                else if (existing != null)
                {
                    ItemStack extracted = parent.extractItems(existing, amount, false);
                    if (extracted != null)
                        tile.markDirty();
                    inventoryPlayer.setItemStack(extracted);
                }

                detectAndSendChanges();
                return slot.getStack();
            }
            else if (mode == 1 && existing != null)
            {
                int amount = existing.getMaxStackSize();
                if (clickedButton != 0 && amount > 1)
                    amount /= 2;

                if (amount == 0)
                    return null;

                ItemStack remaining = simulateAddToPlayer(existing, amount);

                if (remaining != null)
                    amount -= remaining.stackSize;

                if (amount > 0)
                {
                    ItemStack finalExtract = parent.extractItems(existing, amount, false);

                    if (finalExtract != null)
                    {
                        addToPlayer(finalExtract);

                        tile.markDirty();
                        detectAndSendChanges();
                    }
                }
            }

            return null;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public ItemStack simulateAddToPlayer(ItemStack stack, int amount)
    {
        int startIndex = FakeSlots;
        int endIndex = startIndex + PlayerSlots;

        ItemStack stackCopy = stack.copy();
        stackCopy.stackSize = amount;

        if (!this.simulateInsertStack(stackCopy, startIndex, endIndex))
        {
            return stackCopy;
        }

        if (stackCopy.stackSize <= 0)
        {
            return null;
        }

        return stackCopy;
    }

    protected boolean simulateInsertStack(ItemStack stack, int startIndex, int endIndex)
    {
        boolean canInsert = false;

        if (stack.isStackable())
        {
            for (int i = startIndex; stack.stackSize > 0 && i < endIndex; i++)
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot != null && stackInSlot.stackSize < stackInSlot.getMaxStackSize() &&
                        ItemStack.areItemsEqual(stackInSlot, stack) && ItemStack.areItemStackTagsEqual(stack, stackInSlot))
                {
                    int j = stackInSlot.stackSize + stack.stackSize;

                    if (j <= stack.getMaxStackSize())
                    {
                        stack.stackSize = 0;
                        canInsert = true;
                    }
                    else
                    {
                        stack.stackSize -= stack.getMaxStackSize() - stackInSlot.stackSize;
                        canInsert = true;
                    }
                }
            }
        }

        if (stack.stackSize > 0)
        {
            for (int i = startIndex; i < endIndex; i++)
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot == null && slot.isItemValid(stack))
                {
                    stack.stackSize = 0;
                    canInsert = true;
                    break;
                }
            }
        }

        return canInsert;
    }

    public ItemStack addToPlayer(ItemStack stack)
    {
        int startIndex = FakeSlots;
        int endIndex = startIndex + PlayerSlots;

        ItemStack stackCopy = stack.copy();

        if (!this.mergeItemStack(stackCopy, startIndex, endIndex, false))
        {
            return stackCopy;
        }

        if (stackCopy.stackSize <= 0)
        {
            return null;
        }

        return stackCopy;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);

        if (slot == null || !slot.getHasStack())
        {
            return null;
        }

        if (slotIndex < FakeSlots)
        {
            // Shouldn't even happen, handled above.
            return null;
        }
        else
        {
            IInventoryAutomation parent = tile.getAutomation();
            if (parent == null)
                return null;

            ItemStack stack = slot.getStack();
            ItemStack stackCopy = stack.copy();

            ItemStack remaining = parent.insertItems(stack);
            if (remaining != null)
            {
                if (remaining.stackSize == stackCopy.stackSize)
                {
                    return null;
                }

                tile.markDirty();
                stack.stackSize = remaining.stackSize;
                slot.onSlotChanged();
            }
            else
            {
                tile.markDirty();
                stack.stackSize = 0;
                slot.putStack(null);
            }

            slot.onPickupFromSlot(player, stack);
            return stackCopy;
        }
    }

    public class FakeInventoryServer implements IItemHandlerModifiable
    {
        final List<ItemStack> slots = Lists.newArrayList();

        public FakeInventoryServer()
        {
        }

        public void refresh()
        {
            IInventoryAutomation inv = tile.getAutomation();

            final List<ItemStack> slotsSeen = Lists.newArrayList();
            final List<String> itemData = Lists.newArrayList();

            slots.clear();

            if (inv == null)
                return;

            int invSlots = inv.getSlots();
            for (int j = 0; j < invSlots; j++)
            {
                ItemStack invStack = inv.getStackInSlot(j);
                if (invStack == null)
                    continue;

                boolean found = false;

                for (ItemStack cachedStack : slotsSeen)
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
                    slotsSeen.add(stack);

                    boolean matchesSearch = true;
                    if (filterText != null && filterText.length() > 0)
                    {
                        itemData.clear();
                        Item item = invStack.getItem();
                        itemData.add(stack.getDisplayName());
                        itemData.add(Item.itemRegistry.getNameForObject(item).toString());
                        item.addInformation(stack, null, itemData, false);
                        matchesSearch = false;
                        for (String s : itemData)
                        {
                            if (s.contains(filterText))
                            {
                                matchesSearch = true;
                                break;
                            }
                        }
                    }

                    if (matchesSearch)
                    {
                        slots.add(stack);
                    }
                }
            }

            resort();
        }

        public void resort()
        {
            if (sortMode == null)
                return;
            switch (sortMode)
            {
                case Alphabetic:
                    slots.sort(new Comparator<ItemStack>()
                    {
                        @Override
                        public int compare(ItemStack a, ItemStack b)
                        {
                            return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                        }
                    });
                    break;
                case StackSize:
                    slots.sort(new Comparator<ItemStack>()
                    {
                        @Override
                        public int compare(ItemStack a, ItemStack b)
                        {
                            int diff = a.stackSize - b.stackSize;
                            if (diff > 0) return -1;
                            if (diff < 0) return 1;
                            return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                        }
                    });
                    break;
            }
        }

        public int getRealSizeInventory()
        {
            return slots.size();
        }

        @Override
        public int getSlots()
        {
            return slots.size() - scroll;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if ((index + scroll) >= slots.size())
                return null;
            return slots.get(index + scroll);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return null;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return null;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
        }
    }

    public class FakeInventoryClient implements IItemHandlerModifiable
    {
        ItemStack[] totals;
        ItemStack[] singles;

        public FakeInventoryClient(int slots)
        {
            totals = new ItemStack[slots];
            singles = new ItemStack[slots];
        }

        @Override
        public int getSlots()
        {
            return totals.length;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if (index >= totals.length)
                return null;
            return singles[index];
        }

        public int getStackSizeForSlot(int index)
        {
            if (index >= totals.length)
                return 0;

            ItemStack stack = totals[index];
            if (stack == null)
                return 0;

            return stack.stackSize;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return null;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return null;
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack)
        {
            if (index >= totals.length)
                return;

            totals[index] = stack;
            if (stack != null)
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
    }
}
