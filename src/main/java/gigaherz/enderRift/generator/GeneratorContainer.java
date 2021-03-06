package gigaherz.enderRift.generator;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.network.UpdateField;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ObjectHolder;

public class GeneratorContainer extends Container
{
    @ObjectHolder("enderrift:generator")
    public static ContainerType<GeneratorContainer> TYPE;

    protected GeneratorTileEntity tile;
    private int[] prevFields;

    public GeneratorContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        this(id, extraData.readBlockPos(), playerInventory);
    }

    public GeneratorContainer(int id, BlockPos pos, PlayerInventory playerInventory)
    {
        super(TYPE, id);

        this.tile = (GeneratorTileEntity) playerInventory.player.world.getTileEntity(pos);
        prevFields = this.tile.getFields();
        for (int i = 0; i < prevFields.length; i++) { prevFields[i]--; }

        addSlot(new SlotItemHandler(tile.inventory(), 0, 80, 53));

        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(PlayerInventory playerInventory)
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
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        boolean needUpdate = false;

        int[] fields = this.tile.getFields();
        for (int i = 0; i < prevFields.length; i++)
        {
            if (prevFields[i] != fields[i])
            {
                prevFields[i] = fields[i];
                needUpdate = true;
            }
        }

        if (needUpdate)
        {
            this.listeners.stream().filter(watcher -> watcher instanceof ServerPlayerEntity).forEach(watcher ->
                    EnderRiftMod.CHANNEL.sendTo(new UpdateField(this.windowId, prevFields), ((ServerPlayerEntity) watcher).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void updateFields(int[] data)
    {
        this.tile.setFields(data);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex)
    {
        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
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
            if (tile.getBurnTime(slot.getStack()) <= 0)
                return ItemStack.EMPTY;

            startIndex = 0;
            endIndex = 1;
        }

        if (!this.mergeItemStack(stack, startIndex, endIndex, false))
        {
            return ItemStack.EMPTY;
        }

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
}