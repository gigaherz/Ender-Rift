package dev.gigaherz.enderrift.automation.browser;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.slots.SlotFake;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.network.SetVisibleSlots;
import joptsimple.internal.Strings;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AbstractBrowserContainer extends AbstractContainerMenu
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
    private final Player player;
    private int prevChangeCount;
    private ItemStack stackInCursor = ItemStack.EMPTY;

    @Nullable
    protected BrowserBlockEntity tile;

    public final IItemHandlerModifiable scrollInventory;
    public int scroll;
    public SortMode sortMode = SortMode.STACK_SIZE;
    public String filterText = "";

    private DataSlot isLowOnPower = new DataSlot()
    {
        boolean value = false;

        @Override
        public int get()
        {
            return (isClient() ? value : tile.isLowOnPower()) ? 1 : 0;
        }

        @Override
        public void set(int value)
        {
            this.value = value != 0;
        }
    };

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

    protected AbstractBrowserContainer(MenuType<? extends AbstractBrowserContainer> type, int id, @Nullable BrowserBlockEntity tileEntity, Inventory playerInventory)
    {
        super(type, id);

        this.player = playerInventory.player;
        boolean isClient = player.level.isClientSide;

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

        addDataSlot(isLowOnPower);
    }

    protected void bindPlayerInventory(Inventory playerInventory)
    {
        bindPlayerInventory(playerInventory, TOP + SCROLL_ROWS * SLOT_HEIGHT + 14);
    }

    protected void bindPlayerInventory(Inventory playerInventory, int top)
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
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void broadcastChanges()
    {
        
        if (isClient())
            return;

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

            if (!ItemStack.matches(current, newStack))
            {
                current = newStack.copy();
                currentStacks.set(i, current);

                indicesChanged.add(i);
                stacksChanged.add(current);
            }
        }

        for (int i = SCROLL_SLOTS; i < this.slots.size(); ++i)
        {
            ItemStack inSlot = slots.get(i).getItem();
            ItemStack inCache = lastSlots.get(i);

            if (!ItemStack.matches(inCache, inSlot))
            {
                inCache = inSlot.copy();
                this.lastSlots.set(i, inCache);

                for (ContainerListener crafter : this.containerListeners)
                {
                    crafter.slotChanged(this, i, inCache);
                }
            }
        }

        if (player instanceof ServerPlayer crafter)
        {
            if (newLength != oldLength || indicesChanged.size() > 0)
            {
                EnderRiftMod.CHANNEL.sendTo(new SendSlotChanges(containerId, newLength, indicesChanged, stacksChanged), crafter.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }

            ItemStack newStack = getCarried();

            if (!ItemStack.matches(stackInCursor, newStack))
            {
                sendStackInCursor(crafter, newStack);
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

    private void sendStackInCursor(ServerPlayer player, ItemStack newStack)
    {
        stackInCursor = newStack.copy();

        player.connection.send(new ClientboundContainerSetSlotPacket(-1, incrementStateId(), -1, newStack));
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
    public void initializeContents(int p_182411_, List<ItemStack> p_182412_, ItemStack p_182413_)
    {
        super.initializeContents(p_182411_, List.of(), p_182413_);
    }

    public void setVisibleSlots(int[] visible)
    {
        if (isClient())
        {
            EnderRiftMod.CHANNEL.sendToServer(new SetVisibleSlots(containerId, visible));
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
    public void setItem(int slotID, int stateId, ItemStack stack)
    {
        super.setItem(slotID, stateId, stack);
    }

    @Override
    public void clicked(int slotId, int clickedButton, ClickType mode, Player playerIn)
    {
        if (slotId >= 0 && slotId < SCROLL_SLOTS)
        {
            IItemHandler parent = getTileInventory();

            Slot slot = this.slots.get(slotId);
            ItemStack existing = slot.getItem();
            int existingSize = isClient() ? getClient().getStackSizeForSlot(slotId) : existing.getCount();

            if (mode == ClickType.PICKUP)
            {
                ItemStack dropping = getCarried();

                if (dropping.getCount() > 0)
                {
                    if (clickedButton == 0)
                    {
                        ItemStack remaining = insertItemsSided(parent, dropping);
                        if (remaining.getCount() > 0)
                        {
                            if (dropping.getCount() != remaining.getCount())
                                markTileDirty();

                            if (player instanceof ServerPlayer crafter)
                            {
                                sendStackInCursor(crafter, remaining);
                            }
                        }
                        else
                        {
                            markTileDirty();
                        }
                        setCarried(remaining);
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

                            if (player instanceof ServerPlayer crafter)
                            {
                                sendStackInCursor(crafter, remaining);
                            }
                        }
                        else
                        {
                            markTileDirty();
                        }

                        if (dropping.getCount() <= 0)
                            dropping = ItemStack.EMPTY;

                        setCarried(dropping);
                    }
                }
                else if (existingSize > 0)
                {
                    int amount = clickedButton == 0 
                            ? existing.getMaxStackSize() 
                            : Math.min(existing.getCount(), existing.getMaxStackSize()) / 2;

                    ItemStack extracted = extractItemsSided(parent, existing, existingSize, amount, false);
                    if (extracted.getCount() > 0)
                    {
                        markTileDirty();
                    }
                    else
                    {

                        if (player instanceof ServerPlayer crafter)
                        {
                            sendStackInCursor(crafter, extracted);
                        }
                    }
                    setCarried(extracted);
                }

                broadcastChanges();
                return;
            }
            else if (mode == ClickType.QUICK_MOVE && existingSize > 0)
            {
                int amount = clickedButton == 0 
                        ? existing.getMaxStackSize() 
                        : Math.min(existing.getCount(), existing.getMaxStackSize()) / 2;

                if (amount == 0)
                    return;

                ItemStack remaining = simulateAddToPlayer(existing, amount);

                if (remaining.getCount() > 0)
                    amount -= remaining.getCount();

                if (amount > 0)
                {
                    ItemStack finalExtract = extractItemsSided(parent, existing, existingSize, amount, false);

                    if (finalExtract.getCount() > 0)
                    {
                        addToPlayer(finalExtract);

                        markTileDirty();
                        broadcastChanges();
                    }
                }
            }

            if (mode != ClickType.CLONE)
                return;
        }

        super.clicked(slotId, clickedButton, mode, playerIn);
    }

    @Nullable
    private IItemHandler getTileInventory()
    {
        return isClient() ? null : tile.getCombinedInventory();
    }

    private void markTileDirty()
    {
        if (!isClient())
            tile.setChanged();
    }

    private ItemStack extractItemsSided(@Nullable IItemHandler parent, ItemStack existing, int existingSize, int amount, boolean simulate)
    {
        if (isClient() || parent == null)
        {
            var copy = existing.copy();
            copy.setCount(Math.min(existingSize, amount));
            return copy;
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
                Slot slot = this.slots.get(i);
                ItemStack stackInSlot = slot.getItem();

                if (stackInSlot.getCount() < stackInSlot.getMaxStackSize() 
                        && ItemStack.isSame(stackInSlot, stack)
                        && ItemStack.tagMatches(stack, stackInSlot))
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
                Slot slot = this.slots.get(i);
                ItemStack stackInSlot = slot.getItem();

                if (stackInSlot.getCount() <= 0 && slot.mayPlace(stack))
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

        if (!this.moveItemStackTo(stackCopy, startIndex, endIndex, false))
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
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        Slot slot = this.slots.get(slotIndex);

        if (slot == null || !slot.hasItem())
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
            ItemStack stack = slot.getItem();
            ItemStack stackCopy = stack.copy();

            ItemStack remaining = insertItemsSided(getTileInventory(), stack);
            if (remaining.getCount() > 0)
            {
                if (player instanceof ContainerListener)
                {
                    ((ContainerListener) player).slotChanged(this, slotIndex, remaining);
                }

                if (remaining.getCount() == stackCopy.getCount())
                {
                    return ItemStack.EMPTY;
                }

                markTileDirty();
                stack.setCount(remaining.getCount());
                slot.setChanged();
            }
            else
            {
                markTileDirty();
                stack.setCount(0);
                slot.set(ItemStack.EMPTY);
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
        return isLowOnPower.get() != 0;
    }

    public static class ServerScrollInventory implements IItemHandlerModifiable
    {
        final BrowserBlockEntity tile;
        final List<ItemStack> slots = Lists.newArrayList();
        private int[] visible = new int[0];

        public ServerScrollInventory(BrowserBlockEntity tile)
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
                    if (ItemStack.isSame(cachedStack, invStack) && ItemStack.tagMatches(cachedStack, invStack))
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

            final List<Component> itemData = Lists.newArrayList();

            int indexx = 0;
            for (ItemStack invStack : stacks)
            {
                ItemStack stack = invStack.copy();

                boolean matchesSearch = true;
                if (!Strings.isNullOrEmpty(filterText))
                {
                    itemData.clear();
                    Item item = invStack.getItem();
                    itemData.add(stack.getHoverName());
                    itemData.add(Component.literal(ForgeRegistries.ITEMS.getKey(item).toString()));
                    item.appendHoverText(stack, player.level, itemData, TooltipFlag.Default.NORMAL);
                    matchesSearch = false;
                    for (Component s : itemData)
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
                            return a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString());
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
                            return a.getHoverName().getString().compareToIgnoreCase(b.getHoverName().getString());
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