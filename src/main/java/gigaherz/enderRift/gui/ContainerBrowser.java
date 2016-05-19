package gigaherz.enderRift.gui;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.IInventoryAutomation;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.misc.SortMode;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.SetVisibleSlots;
import gigaherz.enderRift.slots.SlotFake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ContainerBrowser
        extends Container
{
    public final FakeInventoryClient fakeInventoryClient;
    public final FakeInventoryServer fakeInventoryServer;
    protected TileBrowser tile;
    private int prevChangeCount;
    public int scroll;
    public SortMode sortMode = SortMode.StackSize;
    private String filterText = "";
    private ItemStack stackInCursor;

    private EntityPlayer player;

    private ItemStack[] currentStacks = new ItemStack[0];

    protected final static int Left = 8;
    protected final static int Top = 18;
    protected final static int SlotWidth = 18;
    protected final static int SlotHeight = 18;

    protected final static int FakeRows = 3;
    protected final static int FakeColumns = 9;
    protected final static int FakeSlots = FakeRows * FakeColumns;

    protected final static int PlayerRows = 4;
    protected final static int PlayerColumns = 9;
    protected final static int PlayerSlots = PlayerRows * PlayerColumns;

    public ContainerBrowser(TileBrowser tileEntity, EntityPlayer player, boolean isClient)
    {
        this.tile = tileEntity;
        this.player = player;

        IItemHandlerModifiable fake;
        if (isClient)
        {
            fakeInventoryClient = new FakeInventoryClient();
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

        bindPlayerInventory(player.inventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
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
        if (tile.getWorld().isRemote)
            return;

        if (prevChangeCount != tile.getChangeCount())
        {
            fakeInventoryServer.refresh();
            prevChangeCount = tile.getChangeCount();
        }

        int oldLength = currentStacks.length;
        int newLength;
        List<Integer> indicesChanged = Lists.newArrayList();
        List<ItemStack> stacksChanged = Lists.newArrayList();

        FakeInventoryServer serverInv = fakeInventoryServer;

        newLength = serverInv.getRealSizeInventory();
        if (newLength != oldLength)
        {
            currentStacks = Arrays.copyOf(currentStacks, newLength);
        }

        for (int i = 0; i < newLength; ++i)
        {
            ItemStack newStack = serverInv.getStack(i);
            ItemStack current = currentStacks[i];

            if (!ItemStack.areItemStacksEqual(current, newStack))
            {
                current = newStack == null ? null : newStack.copy();
                currentStacks[i] = current;

                indicesChanged.add(i);
                stacksChanged.add(current);
            }
        }

        for (int i = FakeSlots; i < this.inventorySlots.size(); ++i)
        {
            ItemStack inSlot = inventorySlots.get(i).getStack();
            ItemStack inCache = inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(inCache, inSlot))
            {
                inCache = inSlot == null ? null : inSlot.copy();
                this.inventoryItemStacks.set(i, inCache);

                for (IContainerListener crafter : this.listeners)
                {
                    crafter.sendSlotContents(this, i, inCache);
                }
            }
        }

        for (IContainerListener crafter : this.listeners)
        {
            if (!(crafter instanceof EntityPlayerMP))
                continue;

            if(newLength != oldLength || indicesChanged.size() > 0)
            {
                EnderRiftMod.channel.sendTo(new SendSlotChanges(windowId, newLength, indicesChanged, stacksChanged), (EntityPlayerMP) crafter);
            }

            EntityPlayerMP player = (EntityPlayerMP) crafter;
            ItemStack newStack = player.inventory.getItemStack();

            if (!ItemStack.areItemStacksEqual(stackInCursor, newStack))
            {
                stackInCursor = newStack == null ? null : newStack.copy();

                player.connection.sendPacket(new SPacketSetSlot(-1, -1, newStack));
            }
        }
    }

    public void slotsChanged(int slotCount, List<Integer> indices, List<ItemStack> stacks)
    {
        if(slotCount != currentStacks.length)
        {
            currentStacks = Arrays.copyOf(currentStacks, slotCount);
        }

        for(int i=0;i<indices.size();i++)
        {
            int slot = indices.get(i);
            ItemStack stack = stacks.get(i);

            currentStacks[slot] = stack;
        }

        fakeInventoryClient.setArray(currentStacks);

        setVisibleSlots(fakeInventoryClient.getIndices());
    }

    @Override
    public void putStacksInSlots(ItemStack[] p_75131_1_)
    {
        // Left blank intentionally
    }

    public void setVisibleSlots(int[] visible)
    {
        if (tile.getWorld().isRemote)
        {
            EnderRiftMod.channel.sendToServer(new SetVisibleSlots(windowId, visible));
        }
        else
        {
            fakeInventoryServer.setVisible(visible);
        }
    }

    public void setScrollPos(int scroll)
    {
        this.scroll = scroll;

        setVisibleSlots(fakeInventoryClient.getIndices());
    }

    public void setSortMode(SortMode sortMode)
    {
        this.sortMode = sortMode;
        this.scroll = 0;

        fakeInventoryClient.setArray(currentStacks);

        setVisibleSlots(fakeInventoryClient.getIndices());
    }

    public void setFilterText(String text)
    {
        this.filterText = text.toLowerCase();
        this.scroll = 0;

        fakeInventoryClient.setArray(currentStacks);

        setVisibleSlots(fakeInventoryClient.getIndices());
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack)
    {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, ClickType mode, EntityPlayer playerIn)
    {
        InventoryPlayer inventoryPlayer = playerIn.inventory;

        if (slotId >= 0 && slotId < FakeSlots)
        {
            IInventoryAutomation parent = tile.getAutomation();
            if (parent == null)
                return null;

            Slot slot = this.inventorySlots.get(slotId);
            ItemStack existing = slot.getStack();

            if (mode == ClickType.PICKUP)
            {
                ItemStack dropping = inventoryPlayer.getItemStack();

                if (dropping != null)
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
                        int amount = 1;

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
                    int amount = clickedButton == 0
                            ? existing.getMaxStackSize()
                            : existing.getMaxStackSize() / 2;

                    ItemStack extracted = parent.extractItems(existing, amount, false);
                    if (extracted != null)
                        tile.markDirty();
                    inventoryPlayer.setItemStack(extracted);
                }

                detectAndSendChanges();
                return slot.getStack();
            }
            else if (mode == ClickType.QUICK_MOVE && existing != null)
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

            if (mode != ClickType.CLONE)
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

    public int getActualSlotCount()
    {
        return fakeInventoryClient.getSlots();
    }

    public class FakeInventoryServer implements IItemHandlerModifiable
    {
        final List<ItemStack> slots = Lists.newArrayList();
        private int[] visible = new int[0];

        public FakeInventoryServer()
        {
        }

        public void setVisible(int[] visible)
        {
            this.visible = visible;
        }

        public void refresh()
        {
            IInventoryAutomation inv = tile.getAutomation();

            final List<ItemStack> slotsSeen = Lists.newArrayList();

            slots.clear();
            visible = new int[0];

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

                    slots.add(stack);
                }
            }
        }

        public int getRealSizeInventory()
        {
            return slots.size();
        }

        @Override
        public int getSlots()
        {
            return visible.length;
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            if (index >= visible.length)
                return null;
            return slots.get(visible[index]);
        }

        public ItemStack getStack(int index)
        {
            return slots.get(index);
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
        private int[] indices;
        private ItemStack[] stacks;

        public FakeInventoryClient()
        {
        }

        public void setArray(final ItemStack[] stacks)
        {
            this.stacks = stacks;

            final List<Integer> indices = Lists.newArrayList();

            final List<String> itemData = Lists.newArrayList();

            int indexx = 0;
            for(ItemStack invStack : stacks)
            {
                ItemStack stack = invStack.copy();

                boolean matchesSearch = true;
                if (filterText != null && filterText.length() > 0)
                {
                    itemData.clear();
                    Item item = invStack.getItem();
                    itemData.add(stack.getDisplayName());
                    itemData.add(Item.REGISTRY.getNameForObject(item).toString());
                    item.addInformation(stack, player, itemData, false);
                    matchesSearch = false;
                    for (String s : itemData)
                    {
                        if (StringUtils.containsIgnoreCase(s, filterText))
                        {
                            matchesSearch = true;
                            break;
                        }
                    }
                }

                if (matchesSearch)
                {
                    indices.add(indexx);
                }
                indexx++;
            }

            if (sortMode != null)
            {
                switch (sortMode)
                {
                    case Alphabetic:
                        indices.sort((ia, ib) -> {
                            ItemStack a = stacks[ia];
                            ItemStack b = stacks[ib];
                            return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                        });
                        break;
                    case StackSize:
                        indices.sort((ia, ib) -> {
                            ItemStack a = stacks[ia];
                            ItemStack b = stacks[ib];
                            int diff = a.stackSize - b.stackSize;
                            if (diff > 0) return -1;
                            if (diff < 0) return 1;
                            return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                        });
                        break;
                }
            }

            this.indices = new int[indices.size()];
            for(int i=0;i<this.indices.length;i++)
            {
                this.indices[i] = indices.get(i);
            }
        }

        @Override
        public int getSlots()
        {
            return indices.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if((slot+scroll) >= indices.length)
                return null;
            ItemStack stack = stacks[indices[slot + scroll]];
            if(stack != null)
            {
                stack = stack.copy();
                stack.stackSize = 1;
            }
            return stack;
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
            if((slot+scroll) < indices.length)
                stacks[indices[slot + scroll]] = stack;
        }

        public int getStackSizeForSlot(int slot)
        {
            if((slot+scroll) >= indices.length)
                return 0;
            return stacks[indices[slot+scroll]].stackSize;
        }

        public int[] getIndices()
        {
            int from = Math.max(0,Math.min(scroll,indices.length-1));
            int to = Math.min(from + FakeSlots, indices.length);
            return Arrays.copyOfRange(indices, from, to);
        }
    }
}
