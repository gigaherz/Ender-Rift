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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AbstractBrowserContainer extends Container
{
    protected final static int LEFT = 8;
    protected final static int TOP = 18;
    protected final static int SLOT_WIDTH = 18;
    protected final static int SLOT_HEIGHT = 18;

    protected final static int SCROLL_ROWS = 3;
    protected final static int SCROLL_COLUMNS = 9;
    protected final static int SCROLL_SLOTS = SCROLL_ROWS * SCROLL_COLUMNS;

    protected final static int PLAYER_ROWS = 4;
    protected final static int PLAYER_COLUMNS = 9;
    protected final static int PLAYER_SLOTS = PLAYER_ROWS * PLAYER_COLUMNS;

    private NonNullList<ItemStack> currentStacks = NonNullList.create();
    private final PlayerEntity player;
    private int prevChangeCount;
    private ItemStack stackInCursor = ItemStack.EMPTY;

    @Nullable
    protected BrowserTileEntity tile;

    public final IItemHandlerModifiable scrollInventory;
    public int scroll;
    public SortMode sortMode = SortMode.STACK_SIZE;
    public String filterText = "";

    public final boolean isClient()
    {
        return tile == null;
    }

    public ClientScrollInventory getClient()
    {
        if (!isClient())
            throw new IllegalStateException("Attempted to get client inventory on the server");
        return (ClientScrollInventory) scrollInventory;
    }

    public ServerScrollInventory getServer()
    {
        if (isClient())
            throw new IllegalStateException("Attempted to get server inventory on the client");
        return (ServerScrollInventory) scrollInventory;
    }

    protected AbstractBrowserContainer(ContainerType<? extends AbstractBrowserContainer> type, int id, @Nullable BrowserTileEntity tileEntity, PlayerInventory playerInventory)
    {
        super(type, id);

        this.player = playerInventory.player;
        boolean isClient = player.world.isRemote;

        if (isClient)
        {
            this.tile = null;
            scrollInventory = new ClientScrollInventory();
        }
        else
        {
            this.tile = Objects.requireNonNull(tileEntity);
            scrollInventory = new ServerScrollInventory(tile);
        }

        for (int y = 0; y < SCROLL_ROWS; y++)
        {
            for (int x = 0; x < SCROLL_COLUMNS; x++)
            {
                addSlot(new SlotFake(scrollInventory, x + y * SCROLL_COLUMNS,
                        LEFT + x * SLOT_WIDTH, TOP + y * SLOT_HEIGHT));
            }
        }

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(PlayerInventory playerInventory)
    {
        bindPlayerInventory(playerInventory, TOP + SCROLL_ROWS * SLOT_HEIGHT + 14);
    }

    protected void bindPlayerInventory(PlayerInventory playerInventory, int top)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlot(new Slot(playerInventory,
                        x + y * 9 + 9,
                        LEFT + x * SLOT_WIDTH, top + y * SLOT_HEIGHT));
            }
        }

        top += 3 * SLOT_HEIGHT + 4;
        for (int x = 0; x < 9; x++)
        {
            addSlot(new Slot(playerInventory, x, LEFT + x * SLOT_WIDTH, top));
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
        if (isClient())
            return;

        boolean isLowOnPowerNew = tile.isLowOnPower();
        if (isLowOnPowerPrev != isLowOnPowerNew)
        {
            for (IContainerListener crafter : this.listeners)
            {
                if (!(crafter instanceof ServerPlayerEntity))
                    continue;

                EnderRiftMod.CHANNEL.sendTo(new UpdatePowerStatus(windowId, isLowOnPowerNew), ((ServerPlayerEntity) crafter).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }

            isLowOnPowerPrev = isLowOnPowerNew;
        }

        ServerScrollInventory serverInv = getServer();
        if (prevChangeCount != tile.getChangeCount())
        {
            serverInv.refresh();
            prevChangeCount = tile.getChangeCount();
        }

        int oldLength = currentStacks.size();
        int newLength;
        List<Integer> indicesChanged = Lists.newArrayList();
        List<ItemStack> stacksChanged = Lists.newArrayList();


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

        for (int i = SCROLL_SLOTS; i < this.inventorySlots.size(); ++i)
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
                EnderRiftMod.CHANNEL.sendTo(new SendSlotChanges(windowId, newLength, indicesChanged, stacksChanged), ((ServerPlayerEntity) crafter).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
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
            serverInv.resetVisible();
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

        ClientScrollInventory clientInv = getClient();
        clientInv.setArray(currentStacks);

        setVisibleSlots(clientInv.getIndices());
    }

    @Override
    public void setAll(List<ItemStack> p_75131_1_)
    {
        // Left blank intentionally
    }

    public void setVisibleSlots(int[] visible)
    {
        if (isClient())
        {
            EnderRiftMod.CHANNEL.sendToServer(new SetVisibleSlots(windowId, visible));
        }
        else
        {
            getServer().setVisible(visible);
        }
    }

    public void setScrollPos(int scroll)
    {
        this.scroll = scroll;

        setVisibleSlots(getClient().getIndices());
    }

    public void setSortMode(SortMode sortMode)
    {
        this.sortMode = sortMode;
        this.scroll = 0;

        ClientScrollInventory clientInv = getClient();
        clientInv.setArray(currentStacks);

        setVisibleSlots(clientInv.getIndices());
    }

    public void setFilterText(String text)
    {
        this.filterText = text.toLowerCase();
        this.scroll = 0;

        ClientScrollInventory clientInv = getClient();
        clientInv.setArray(currentStacks);

        setVisibleSlots(clientInv.getIndices());
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

        if (slotId >= 0 && slotId < SCROLL_SLOTS)
        {
            IItemHandler parent = getTileInventory();

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
                                markTileDirty();

                            for (IContainerListener crafter : this.listeners)
                            {
                                if (!(crafter instanceof ServerPlayerEntity))
                                    continue;

                                sendStackInCursor((ServerPlayerEntity) crafter, remaining);
                            }
                        }
                        else
                        {
                            markTileDirty();
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
                                markTileDirty();
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
                            markTileDirty();
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
                        markTileDirty();
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

                        markTileDirty();
                        detectAndSendChanges();
                    }
                }
            }

            if (mode != ClickType.CLONE)
                return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Nullable
    private IItemHandler getTileInventory()
    {
        return isClient() ? null : tile.getCombinedInventory();
    }

    private void markTileDirty()
    {
        if (!isClient())
            tile.markDirty();
    }

    private ItemStack extractItemsSided(@Nullable IItemHandler parent, ItemStack existing, int amount, boolean simulate)
    {
        if (isClient() || parent == null)
        {
            return existing.copy();
        }
        return AutomationHelper.extractItems(parent, existing, amount, simulate);
    }

    private ItemStack insertItemsSided(@Nullable IItemHandler parent, ItemStack dropping)
    {
        if (isClient() || parent == null)
            return ItemStack.EMPTY;
        return AutomationHelper.insertItems(parent, dropping);
    }

    private ItemStack simulateAddToPlayer(ItemStack stack, int amount)
    {
        int startIndex = SCROLL_SLOTS;
        int endIndex = startIndex + PLAYER_SLOTS;

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
        int startIndex = SCROLL_SLOTS;
        int endIndex = startIndex + PLAYER_SLOTS;

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

        if (slotIndex < SCROLL_SLOTS)
        {
            // Shouldn't even happen, handled above.
            return ItemStack.EMPTY;
        }
        else
        {
            ItemStack stack = slot.getStack();
            ItemStack stackCopy = stack.copy();

            ItemStack remaining = insertItemsSided(getTileInventory(), stack);
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

                markTileDirty();
                stack.setCount(remaining.getCount());
                slot.onSlotChanged();
            }
            else
            {
                markTileDirty();
                stack.setCount(0);
                slot.putStack(ItemStack.EMPTY);
            }

            slot.onTake(player, stack);
            return stackCopy;
        }
    }

    public int getActualSlotCount()
    {
        return scrollInventory.getSlots();
    }

    public boolean isLowOnPower()
    {
        return isLowOnPowerPrev;
    }

    public void updatePowerStatus(boolean status)
    {
        isLowOnPowerPrev = status;
    }

    public static class ServerScrollInventory implements IItemHandlerModifiable
    {
        final BrowserTileEntity tile;
        final List<ItemStack> slots = Lists.newArrayList();
        private int[] visible = new int[0];

        public ServerScrollInventory(BrowserTileEntity tile)
        {
            this.tile = tile;
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

    public class ClientScrollInventory implements IItemHandlerModifiable
    {
        private int[] indices;
        private NonNullList<ItemStack> stacks;

        public ClientScrollInventory()
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
                    case ALPHABETIC:
                        indices.sort((ia, ib) ->
                        {
                            ItemStack a = stacks.get(ia);
                            ItemStack b = stacks.get(ib);
                            return a.getDisplayName().getString().compareToIgnoreCase(b.getDisplayName().getString());
                        });
                        break;
                    case STACK_SIZE:
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
            int to = Math.min(from + SCROLL_SLOTS, indices.length);
            return Arrays.copyOfRange(indices, from, to);
        }
    }
}
