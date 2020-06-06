package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.network.ClearCraftingGrid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.Optional;

public class CraftingBrowserContainer extends AbstractBrowserContainer
{
    @ObjectHolder("enderrift:crafting_browser")
    public static ContainerType<CraftingBrowserContainer> TYPE;

    public static final int INVENTORY_SLOT_START = SCROLL_SLOTS;
    public static final int CRAFTING_SLOT_START = SCROLL_SLOTS + PLAYER_SLOTS + 1;

    private final static int CRAFTER_HEIGHT = 58;
    private final static int CRAFTING_OFFSET = 59;

    public CraftingInventory craftMatrix = new CraftingInventory(this, 3, 3);
    public CraftResultInventory craftResult = new CraftResultInventory();

    private final World world;
    private final PlayerEntity player;

    Slot slotCraftResult;

    public CraftingBrowserContainer(int id, PlayerInventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public CraftingBrowserContainer(int id, @Nullable BrowserTileEntity te, PlayerInventory playerInventory)
    {
        super(TYPE, id, te, playerInventory);

        this.world = playerInventory.player.world;
        this.player = playerInventory.player;

        bindCraftingGrid(player.inventory, CRAFTING_OFFSET);
    }

    protected void bindCraftingGrid(PlayerInventory playerInventory, int top)
    {
        slotCraftResult = this.addSlot(new CraftingResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35 + top));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlot(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18 + top));
            }
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    protected void bindPlayerInventory(PlayerInventory playerInventory)
    {
        bindPlayerInventory(playerInventory, TOP + SCROLL_ROWS * SLOT_HEIGHT + 14 + CRAFTER_HEIGHT);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        if (inventoryIn == craftMatrix)
            this.slotChangedCraftingGrid(this.world, this.player, this.craftMatrix, this.craftResult);
        else
            super.onCraftMatrixChanged(inventoryIn);
    }

    protected void slotChangedCraftingGrid(World world, PlayerEntity player, CraftingInventory inventoryCrafting, CraftResultInventory craftingResult)
    {
        if (!world.isRemote)
        {
            ServerPlayerEntity entityplayermp = (ServerPlayerEntity) player;
            Optional<ICraftingRecipe> irecipe = this.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, inventoryCrafting, world);

            Optional<ItemStack> stack = irecipe.map((recipe) -> {
                if (recipe.isDynamic() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || entityplayermp.getRecipeBook().isUnlocked(recipe))
                {
                    craftingResult.setRecipeUsed(recipe);
                    return recipe.getCraftingResult(inventoryCrafting);
                }
                return null;
            });

            ItemStack itemstack = stack.orElse(ItemStack.EMPTY);
            craftingResult.setInventorySlotContents(0, itemstack);
            entityplayermp.connection.sendPacket(new SSetSlotPacket(this.windowId, slotCraftResult.slotNumber, itemstack));
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!this.world.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (itemstack.getCount() > 0)
                {
                    ItemHandlerHelper.giveItemToPlayer(playerIn, itemstack);
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex)
    {
        if (slotIndex < SCROLL_SLOTS + PLAYER_SLOTS)
        {
            return super.transferStackInSlot(player, slotIndex);
        }

        Slot slot = this.inventorySlots.get(slotIndex);

        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (!this.mergeItemStack(stack, INVENTORY_SLOT_START, CRAFTING_SLOT_START, false))
        {
            return ItemStack.EMPTY;
        }

        if (slotIndex == CRAFTING_SLOT_START)
            slot.onSlotChange(stack, stackCopy);

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

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }

    public void clearCraftingGrid(PlayerEntity playerIn)
    {
        boolean isRemote = tile == null ? true : tile.getWorld().isRemote;

        if (!this.world.isRemote)
        {
            IItemHandler parent = tile.getCombinedInventory();

            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

                if (!isRemote && itemstack.getCount() > 0)
                {
                    ItemStack remaining = ItemStack.EMPTY;
                    if (parent != null)
                        remaining = AutomationHelper.insertItems(parent, itemstack);

                    if (remaining.getCount() > 0)
                    {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, remaining);

                        if (remaining.getCount() != itemstack.getCount())
                            tile.markDirty();
                    }
                    else
                    {
                        tile.markDirty();
                    }
                }
            }
        }

        if (isRemote)
        {
            EnderRiftMod.channel.sendToServer(new ClearCraftingGrid(windowId));
        }

        this.onCraftMatrixChanged(craftMatrix);
    }
}
