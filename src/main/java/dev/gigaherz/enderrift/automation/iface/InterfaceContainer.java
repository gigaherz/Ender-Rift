package dev.gigaherz.enderrift.automation.iface;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.slots.SlotFilter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ObjectHolder;

public class InterfaceContainer extends AbstractContainerMenu
{
    @ObjectHolder("enderrift:interface")
    public static MenuType<InterfaceContainer> TYPE;
    private final ContainerLevelAccess levelAccess;

    public InterfaceContainer(int id, Inventory playerInventory)
    {
        this(id, playerInventory, new ItemStackHandler(9), new ItemStackHandler(9), ContainerLevelAccess.NULL);
    }

    public InterfaceContainer(int id, Inventory playerInventory, IItemHandlerModifiable filters, IItemHandlerModifiable outputs, final ContainerLevelAccess levelAccess)
    {
        super(TYPE, id);

        this.levelAccess = levelAccess;

        for (int x = 0; x < 9; x++)
        {
            addSlot(new SlotFilter(filters, x, 8 + x * 18, 33));
        }

        for (int x = 0; x < 9; x++)
        {
            addSlot(new SlotItemHandler(outputs, x, 8 + x * 18, 62));
        }

        bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(Inventory playerInventory)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlot(new Slot(playerInventory,
                        x + y * 9 + 9,
                        8 + x * 18, 94 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
        {
            addSlot(new Slot(playerInventory, x, 8 + x * 18, 152));
        }
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return levelAccess.evaluate((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!blockState.is(EnderRiftMod.EnderRiftBlocks.INTERFACE)) return false;

            return playerIn.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public void clicked(int slotId, int clickedButton, ClickType mode, Player playerIn)
    {
        if (slotId >= 0 && slotId < 9)
        {
            if (mode == ClickType.PICKUP || mode == ClickType.PICKUP_ALL ||
                    mode == ClickType.SWAP) // 1 is shift-click
            {
                Slot slot = this.slots.get(slotId);

                ItemStack dropping = getCarried();

                if (dropping.getCount() > 0)
                {
                    ItemStack copy = dropping.copy();
                    copy.setCount(1);
                    slot.set(copy);
                }
                else if (slot.getItem().getCount() > 0)
                {
                    slot.set(ItemStack.EMPTY);
                }

                return;
            }

            return;
        }

        super.clicked(slotId, clickedButton, mode, playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        if (slotIndex < 8)
        {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex < 9)
        {
            return ItemStack.EMPTY;
        }
        else if (slotIndex < 18)
        {
            startIndex = 18;
            endIndex = 18 + 27 + 9;
        }
        else
        {
            startIndex = 9;
            endIndex = 18;
        }

        if (!this.moveItemStackTo(stack, startIndex, endIndex, false))
        {
            return ItemStack.EMPTY;
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
}