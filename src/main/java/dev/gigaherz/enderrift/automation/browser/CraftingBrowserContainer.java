package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import dev.gigaherz.enderrift.network.ClearCraftingGrid;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Optional;

public class CraftingBrowserContainer extends AbstractBrowserContainer
{
    public static final int INVENTORY_SLOT_START = SCROLL_SLOTS;
    public static final int INVENTORY_SLOT_END = SCROLL_SLOTS + PLAYER_SLOTS;
    public static final int CRAFTING_RESULT_SLOT = INVENTORY_SLOT_END;
    public static final int CRAFTING_SLOT_START = INVENTORY_SLOT_END + 1;

    private final static int CRAFTER_HEIGHT = 58;
    private final static int CRAFTING_OFFSET = 59;

    public CraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3);
    public ResultContainer craftResult = new ResultContainer();

    private final Level world;
    private final Player player;

    Slot slotCraftResult;

    public CraftingBrowserContainer(int id, Inventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public CraftingBrowserContainer(int id, @Nullable BrowserBlockEntity te, Inventory playerInventory)
    {
        super(EnderRiftMod.CRAFTING_BROWSER_MENU.get(), id, te, playerInventory);

        this.world = playerInventory.player.level();
        this.player = playerInventory.player;

        bindCraftingGrid(player.getInventory(), CRAFTING_OFFSET);
    }

    protected void bindCraftingGrid(Inventory playerInventory, int top)
    {
        slotCraftResult = this.addSlot(new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35 + top));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlot(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18 + top));
            }
        }

        this.slotsChanged(this.craftMatrix);
    }

    @Override
    protected void bindPlayerInventory(Inventory playerInventory)
    {
        bindPlayerInventory(playerInventory, TOP + SCROLL_ROWS * SLOT_HEIGHT + 14 + CRAFTER_HEIGHT);
    }

    @Override
    public void slotsChanged(Container inventoryIn)
    {
        if (inventoryIn == craftMatrix)
            this.slotChangedCraftingGrid(this.world, this.player, this.craftMatrix, this.craftResult);
        else
            super.slotsChanged(inventoryIn);
    }

    protected void slotChangedCraftingGrid(Level world, Player player, CraftingContainer inventoryCrafting, ResultContainer craftingResult)
    {
        if (!world.isClientSide)
        {
            ServerPlayer entityplayermp = (ServerPlayer) player;
            Optional<RecipeHolder<CraftingRecipe>> irecipe = this.world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventoryCrafting, world);

            Optional<ItemStack> stack = irecipe.map((recipe) -> {
                if (recipe.value().isSpecial() || !world.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || entityplayermp.getRecipeBook().contains(recipe))
                {
                    craftingResult.setRecipeUsed(recipe);
                    return recipe.value().assemble(inventoryCrafting, world.registryAccess());
                }
                return null;
            });

            ItemStack itemstack = stack.orElse(ItemStack.EMPTY);
            craftingResult.setItem(0, itemstack);
            entityplayermp.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, incrementStateId(), slotCraftResult.index, itemstack));
        }
    }

    @Override
    public void removed(Player playerIn)
    {
        super.removed(playerIn);

        if (!this.world.isClientSide)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeItemNoUpdate(i);

                if (itemstack.getCount() > 0)
                {
                    ItemHandlerHelper.giveItemToPlayer(playerIn, itemstack);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        if (slotIndex < CRAFTING_RESULT_SLOT)
        {
            return super.quickMoveStack(player, slotIndex);
        }

        Slot slot = this.slots.get(slotIndex);

        if (slot == null || !slot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack stackCopy = stack.copy();

        if (!this.moveItemStackTo(stack, SCROLL_SLOTS, INVENTORY_SLOT_END, false))
        {
            return ItemStack.EMPTY;
        }

        if (slotIndex == CRAFTING_RESULT_SLOT)
        {
            slot.onQuickCraft(stack, stackCopy);
        }

        if (stack.getCount() == 0)
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);

        return stackCopy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn)
    {
        return slotIn.container != this.craftResult && super.canTakeItemForPickAll(stack, slotIn);
    }

    public void clearCraftingGrid(Player playerIn, boolean toPlayer)
    {
        boolean isRemote = tile == null ? true : tile.getLevel().isClientSide;

        if (!isRemote)
        {
            boolean anyChanged = false;
            IItemHandler parent = tile.getCombinedInventory();

            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.removeItemNoUpdate(i);
                if (itemstack.getCount() > 0)
                {
                    anyChanged = true;
                    ItemStack remaining = itemstack;
                    if (parent != null && !toPlayer)
                    {
                        remaining = AutomationHelper.insertItems(parent, itemstack);
                    }

                    if (remaining.getCount() > 0)
                    {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, remaining);

                        if (remaining.getCount() != itemstack.getCount())
                            tile.setChanged();
                    }
                    else
                    {
                        tile.setChanged();
                    }
                }
            }

            if (anyChanged)
            {
                slotsChanged(this.craftMatrix);
                this.broadcastChanges();
            }
        }

        if (isRemote)
        {
            PacketDistributor.SERVER.noArg().send(new ClearCraftingGrid(containerId, toPlayer));
        }

        this.slotsChanged(craftMatrix);
    }
}