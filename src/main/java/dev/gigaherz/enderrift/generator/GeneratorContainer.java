package dev.gigaherz.enderrift.generator;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ObjectHolder;

public class GeneratorContainer extends AbstractContainerMenu
{
    @ObjectHolder("enderrift:generator")
    public static MenuType<GeneratorContainer> TYPE;

    private final Player player;
    private final ContainerLevelAccess levelAccess;

    public final ContainerData fields;

    public GeneratorContainer(int id, Inventory playerInventory)
    {
        this(id, playerInventory, new ItemStackHandler(1), new ClientFields(), ContainerLevelAccess.NULL);
    }

    public GeneratorContainer(int id, Inventory playerInventory, IItemHandlerModifiable inventory, ContainerData data, final ContainerLevelAccess levelAccess)
    {
        super(TYPE, id);

        this.player = playerInventory.player;
        this.levelAccess = levelAccess;
        this.fields = data;

        addSlot(new SlotItemHandler(inventory, 0, 80, 53));

        bindPlayerInventory(playerInventory);

        addDataSlots(data);
    }

    private void bindPlayerInventory(Inventory playerInventory)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlot(new Slot(playerInventory,
                        x + y * 9 + 9,
                        8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
        {
            addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player playerIn)
    {
        return levelAccess.evaluate((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!blockState.is(EnderRiftMod.EnderRiftBlocks.GENERATOR)) return false;

            return playerIn.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex)
    {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex == 0)
        {
            startIndex = 1;
            endIndex = 1 + 4 * 9;
        }
        else
        {
            if (GeneratorBlockEntity.getBurnTime(slot.getItem()) <= 0)
                return ItemStack.EMPTY;

            startIndex = 0;
            endIndex = 1;
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