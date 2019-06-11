package gigaherz.enderRift.automation.browser;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.common.slots.SlotFake;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.SetVisibleSlots;
import gigaherz.enderRift.network.UpdatePowerStatus;
import joptsimple.internal.Strings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class BrowserContainer extends Container
{
    @ObjectHolder("enderrift:browser")
    public static ContainerType<BrowserContainer> TYPE;

    public final FakeInventoryClient fakeInventoryClient;
    public final FakeInventoryServer fakeInventoryServer;
    protected BrowserEntityTileEntity tile;
    private int prevChangeCount;
    public int scroll;
    public SortMode sortMode = SortMode.StackSize;
    private String filterText = "";
    private ItemStack stackInCursor = ItemStack.EMPTY;

    private PlayerEntity player;

    private NonNullList<ItemStack> currentStacks = NonNullList.create();

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

    private boolean isClient = false;

    public BrowserContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        this(id, extraData.readBlockPos(), playerInventory);
    }

    public BrowserContainer(int id, BlockPos pos, PlayerInventory playerInventory)
    {
        this(id, pos, playerInventory, TYPE);
    }

    protected BrowserContainer(int id, BlockPos pos, PlayerInventory playerInventory, ContainerType<? extends BrowserContainer> type)
    {
        super(type, id);

        TileEntity tileEntity = playerInventory.player.world.getTileEntity(pos);

        this.tile = (BrowserEntityTileEntity)tileEntity;
        this.player = playerInventory.player;

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
                addSlot(new SlotFake(fake,
                        x + y * FakeColumns,
                        Left + x * SlotWidth, Top + y * SlotHeight));
            }
        }

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(PlayerInventory playerInventory)
    {
        bindPlayerInventory(playerInventory, Top + FakeRows * SlotHeight + 14);
    }

    protected void bindPlayerInventory(PlayerInventory playerInventory, int top)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlot(new Slot(playerInventory,
                        x + y * 9 + 9,
                        Left + x * SlotWidth, top + y * SlotHeight));
            }
        }

        top += 3 * SlotHeight + 4;
        for (int x = 0; x < 9; x++)
        {
            addSlot(new Slot(playerInventory, x, Left + x * SlotWidth, top));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player)
    {
        return true;
    }


    private boolean isLowOnPowerPrev;

    @Override
    public void detectAndSendChanges()
    {
        if (tile.getWorld().isRemote)
            return;

        boolean isLowOnPowerNew = tile.isLowOnPower();
        if (isLowOnPowerPrev != isLowOnPowerNew)
        {
            for (IContainerListener crafter : this.listeners)
            {
                if (!(crafter instanceof ServerPlayerEntity))
                    continue;

                EnderRiftMod.channel.sendTo(new UpdatePowerStatus(windowId, isLowOnPowerNew), ((ServerPlayerEntity) crafter).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }

            isLowOnPowerPrev = isLowOnPowerNew;
        }

        if (prevChangeCount != tile.getChangeCount())
        {
            fakeInventoryServer.refresh();
            prevChangeCount = tile.getChangeCount();
        }

        int oldLength = currentStacks.size();
        int newLength;
        List<Integer> indicesChanged = Lists.newArrayList();
        List<ItemStack> stacksChanged = Lists.newArrayList();

        FakeInventoryServer serverInv = fakeInventoryServer;

        newLength = serverInv.getRealSizeInventory();
        if (newLength != oldLength)
        {
            changeSize(oldLength, newLength);
        }

        for (int i = 0; i < newLength; ++i)
        {
            ItemStack newStack = serverInv.getStack(i);
            ItemStack current = currentStacks.get(i);

            if (!ItemStack.areItemStacksEqual(current, newStack))
            {
                current = newStack.copy();
                currentStacks.set(i, current);

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
                inCache = inSlot.copy();
                this.inventoryItemStacks.set(i, inCache);

                for (IContainerListener crafter : this.listeners)
                {
                    crafter.sendSlotContents(this, i, inCache);
                }
            }
        }

        for (IContainerListener crafter : this.listeners)
        {
            if (!(crafter instanceof ServerPlayerEntity))
                continue;

            if (newLength != oldLength || indicesChanged.size() > 0)
            {
                EnderRiftMod.channel.sendTo(new SendSlotChanges(windowId, newLength, indicesChanged, stacksChanged), ((ServerPlayerEntity) crafter).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }

            ServerPlayerEntity player = (ServerPlayerEntity) crafter;
            ItemStack newStack = player.inventory.getItemStack();

            if (!ItemStack.areItemStacksEqual(stackInCursor, newStack))
            {
                sendStackInCursor(player, newStack);
            }
        }

        if (newLength != oldLength || indicesChanged.size() > 0)
        {
            fakeInventoryServer.resetVisible();
        }
    }

    private void changeSize(int oldLength, int newLength)
    {
        NonNullList<ItemStack> oldStacks = currentStacks;
        currentStacks = NonNullList.withSize(newLength, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(newLength, oldLength); i++)
        { currentStacks.set(i, oldStacks.get(i)); }
    }

    private void sendStackInCursor(ServerPlayerEntity player, ItemStack newStack)
    {
        stackInCursor = newStack.copy();

        player.connection.sendPacket(new SSetSlotPacket(-1, -1, newStack));
    }

    public void slotsChanged(int slotCount, List<Integer> indices, List<ItemStack> stacks)
    {
        if (slotCount != currentStacks.size())
        {
            changeSize(currentStacks.size(), slotCount);
        }

        for (int i = 0; i < indices.size(); i++)
        {
            int slot = indices.get(i);
            ItemStack stack = stacks.get(i);

            currentStacks.set(slot, stack);
        }

        fakeInventoryClient.setArray(currentStacks);

        setVisibleSlots(fakeInventoryClient.getIndices());
    }

    @Override
    public void setAll(List<ItemStack> p_75131_1_)
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
    public ItemStack slotClick(int slotId, int clickedButton, ClickType mode, PlayerEntity playerIn)
    {
        PlayerInventory inventoryPlayer = playerIn.inventory;

        if (slotId >= 0 && slotId < FakeSlots)
        {
            IItemHandler parent = tile.getCombinedInventory();

            Slot slot = this.inventorySlots.get(slotId);
            ItemStack existing = slot.getStack();

            if (mode == ClickType.PICKUP)
            {
                ItemStack dropping = inventoryPlayer.getItemStack();

                if (dropping.getCount() > 0)
                {
                    if (clickedButton == 0)
                    {
                        ItemStack remaining = insertItemsSided(parent, dropping);
                        if (remaining.getCount() > 0)
                        {
                            if (dropping.getCount() != remaining.getCount())
                                tile.markDirty();

                            for (IContainerListener crafter : this.listeners)
                            {
                                if (!(crafter instanceof ServerPlayerEntity))
                                    continue;

                                sendStackInCursor((ServerPlayerEntity) crafter, remaining);
                            }
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
                        push.setCount(amount);
                        ItemStack remaining = insertItemsSided(parent, push);

                        dropping.shrink(push.getCount());

                        if (remaining.getCount() > 0)
                        {
                            if (push.getCount() != remaining.getCount())
                                tile.markDirty();
                            dropping.grow(remaining.getCount());

                            for (IContainerListener crafter : this.listeners)
                            {
                                if (!(crafter instanceof ServerPlayerEntity))
                                    continue;

                                sendStackInCursor((ServerPlayerEntity) crafter, remaining);
                            }
                        }
                        else
                        {
                            tile.markDirty();
                        }

                        if (dropping.getCount() <= 0)
                            dropping = ItemStack.EMPTY;

                        inventoryPlayer.setItemStack(dropping);
                    }
                }
                else if (existing.getCount() > 0)
                {
                    int amount = clickedButton == 0
                            ? existing.getMaxStackSize()
                            : existing.getMaxStackSize() / 2;

                    ItemStack extracted = extractItemsSided(parent, existing, amount, false);
                    if (extracted.getCount() > 0)
                    {
                        tile.markDirty();
                    }
                    else
                    {

                        for (IContainerListener crafter : this.listeners)
                        {
                            if (!(crafter instanceof ServerPlayerEntity))
                                continue;

                            sendStackInCursor((ServerPlayerEntity) crafter, extracted);
                        }
                    }
                    inventoryPlayer.setItemStack(extracted);
                }

                detectAndSendChanges();
                return slot.getStack();
            }
            else if (mode == ClickType.QUICK_MOVE && existing.getCount() > 0)
            {
                int amount = existing.getMaxStackSize();
                if (clickedButton != 0 && amount > 1)
                    amount /= 2;

                if (amount == 0)
                    return ItemStack.EMPTY;

                ItemStack remaining = simulateAddToPlayer(existing, amount);

                if (remaining.getCount() > 0)
                    amount -= remaining.getCount();

                if (amount > 0)
                {
                    ItemStack finalExtract = extractItemsSided(parent, existing, amount, false);

                    if (finalExtract.getCount() > 0)
                    {
                        addToPlayer(finalExtract);

                        tile.markDirty();
                        detectAndSendChanges();
                    }
                }
            }

            if (mode != ClickType.CLONE)
                return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    private ItemStack extractItemsSided(IItemHandler parent, ItemStack existing, int amount, boolean simulate)
    {
        if (fakeInventoryClient != null)
        {
            return existing.copy();
        }
        return AutomationHelper.extractItems(parent, existing, amount, simulate);
    }

    private ItemStack insertItemsSided(IItemHandler parent, ItemStack dropping)
    {
        if (fakeInventoryClient != null)
            return ItemStack.EMPTY;
        return AutomationHelper.insertItems(parent, dropping);
    }

    private ItemStack simulateAddToPlayer(ItemStack stack, int amount)
    {
        int startIndex = FakeSlots;
        int endIndex = startIndex + PlayerSlots;

        ItemStack stackCopy = stack.copy();
        stackCopy.setCount(amount);

        if (!this.simulateInsertStack(stackCopy, startIndex, endIndex))
        {
            return stackCopy;
        }

        if (stackCopy.getCount() <= 0)
        {
            return ItemStack.EMPTY;
        }

        return stackCopy;
    }

    protected boolean simulateInsertStack(ItemStack stack, int startIndex, int endIndex)
    {
        boolean canInsert = false;

        if (stack.isStackable())
        {
            for (int i = startIndex; stack.getCount() > 0 && i < endIndex; i++)
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot.getCount() < stackInSlot.getMaxStackSize()
                        && ItemStack.areItemsEqual(stackInSlot, stack)
                        && ItemStack.areItemStackTagsEqual(stack, stackInSlot))
                {
                    int j = stackInSlot.getCount() + stack.getCount();

                    if (j <= stack.getMaxStackSize())
                    {
                        stack.setCount(0);
                        canInsert = true;
                    }
                    else
                    {
                        stack.shrink(stack.getMaxStackSize() - stackInSlot.getCount());
                        canInsert = true;
                    }
                }
            }
        }

        if (stack.getCount() > 0)
        {
            for (int i = startIndex; i < endIndex; i++)
            {
                Slot slot = this.inventorySlots.get(i);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot.getCount() <= 0 && slot.isItemValid(stack))
                {
                    stack.setCount(0);
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

        if (stackCopy.getCount() <= 0)
        {
            return ItemStack.EMPTY;
        }

        return stackCopy;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);

        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        if (slotIndex < FakeSlots)
        {
            // Shouldn't even happen, handled above.
            return ItemStack.EMPTY;
        }
        else
        {
            IItemHandler parent = tile.getCombinedInventory();
            if (parent == null)
                return ItemStack.EMPTY;

            ItemStack stack = slot.getStack();
            ItemStack stackCopy = stack.copy();

            ItemStack remaining = AutomationHelper.insertItems(parent, stack);
            if (remaining.getCount() > 0)
            {
                if (player instanceof IContainerListener)
                {
                    ((IContainerListener) player).sendSlotContents(this, slotIndex, remaining);
                }

                if (remaining.getCount() == stackCopy.getCount())
                {
                    return ItemStack.EMPTY;
                }

                tile.markDirty();
                stack.setCount(remaining.getCount());
                slot.onSlotChanged();
            }
            else
            {
                tile.markDirty();
                stack.setCount(0);
                slot.putStack(ItemStack.EMPTY);
            }

            slot.onTake(player, stack);
            return stackCopy;
        }
    }

    public int getActualSlotCount()
    {
        return fakeInventoryClient.getSlots();
    }

    public boolean isLowOnPower()
    {
        return isLowOnPowerPrev;
    }

    public void updatePowerStatus(boolean status)
    {
        isLowOnPowerPrev = status;
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

        public void resetVisible()
        {
            visible = new int[0];
        }

        public void refresh()
        {
            IItemHandler inv = tile.getCombinedInventory();

            final List<ItemStack> slotsSeen = Lists.newArrayList();

            slots.clear();

            if (inv == null)
                return;

            int invSlots = inv.getSlots();
            for (int j = 0; j < invSlots; j++)
            {
                ItemStack invStack = inv.getStackInSlot(j);
                if (invStack.getCount() <= 0)
                    continue;

                boolean found = false;

                for (ItemStack cachedStack : slotsSeen)
                {
                    if (ItemStack.areItemsEqual(cachedStack, invStack) && ItemStack.areItemStackTagsEqual(cachedStack, invStack))
                    {
                        cachedStack.grow(invStack.getCount());
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
                return ItemStack.EMPTY;
            int i = visible[index];
            if (i >= slots.size())
                return ItemStack.EMPTY;
            return slots.get(visible[index]);
        }

        public ItemStack getStack(int index)
        {
            return slots.get(index);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            return true;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
        }
    }

    public class FakeInventoryClient implements IItemHandlerModifiable
    {
        private int[] indices;
        private NonNullList<ItemStack> stacks;

        public FakeInventoryClient()
        {
        }

        public void setArray(final NonNullList<ItemStack> stacks)
        {
            this.stacks = stacks;

            final List<Integer> indices = Lists.newArrayList();

            final List<ITextComponent> itemData = Lists.newArrayList();

            int indexx = 0;
            for (ItemStack invStack : stacks)
            {
                ItemStack stack = invStack.copy();

                boolean matchesSearch = true;
                if (!Strings.isNullOrEmpty(filterText))
                {
                    itemData.clear();
                    Item item = invStack.getItem();
                    itemData.add(stack.getDisplayName());
                    itemData.add(new StringTextComponent(ForgeRegistries.ITEMS.getKey(item).toString()));
                    item.addInformation(stack, player.world, itemData, ITooltipFlag.TooltipFlags.NORMAL);
                    matchesSearch = false;
                    for (ITextComponent s : itemData)
                    {
                        if (StringUtils.containsIgnoreCase(s.getString(), filterText))
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
                        indices.sort((ia, ib) ->
                        {
                            ItemStack a = stacks.get(ia);
                            ItemStack b = stacks.get(ib);
                            return a.getDisplayName().getString().compareToIgnoreCase(b.getDisplayName().getString());
                        });
                        break;
                    case StackSize:
                        indices.sort((ia, ib) ->
                        {
                            ItemStack a = stacks.get(ia);
                            ItemStack b = stacks.get(ib);
                            int diff = a.getCount() - b.getCount();
                            if (diff > 0) return -1;
                            if (diff < 0) return 1;
                            return a.getDisplayName().getString().compareToIgnoreCase(b.getDisplayName().getString());
                        });
                        break;
                }
            }

            this.indices = new int[indices.size()];
            for (int i = 0; i < this.indices.length; i++)
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
            if ((slot + scroll) >= indices.length)
                return ItemStack.EMPTY;
            ItemStack stack = stacks.get(indices[slot + scroll]);
            stack = stack.copy();
            stack.setCount(1);
            return stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            return true;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            if ((slot + scroll) < indices.length)
                stacks.set(indices[slot + scroll], stack);
        }

        public int getStackSizeForSlot(int slot)
        {
            if ((slot + scroll) >= indices.length)
                return 0;
            return stacks.get(indices[slot + scroll]).getCount();
        }

        public int[] getIndices()
        {
            int from = Math.max(0, Math.min(scroll, indices.length - 1));
            int to = Math.min(from + FakeSlots, indices.length);
            return Arrays.copyOfRange(indices, from, to);
        }
    }
}
