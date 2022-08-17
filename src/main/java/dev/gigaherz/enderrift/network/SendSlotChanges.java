package dev.gigaherz.enderrift.network;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.client.ClientHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SendSlotChanges
{
    public int windowId;
    public int slotCount;
    public List<Integer> indices;
    public List<ItemStack> stacks;

    public SendSlotChanges(int windowId, int slotCount, List<Integer> indices, List<ItemStack> stacks)
    {
        this.windowId = windowId;
        this.slotCount = slotCount;
        this.indices = indices;
        this.stacks = stacks;
    }

    public SendSlotChanges(FriendlyByteBuf buf)
    {
        indices = Lists.newArrayList();
        stacks = Lists.newArrayList();
        windowId = buf.readInt();
        slotCount = buf.readInt();

        int count = buf.readInt();
        while (count-- > 0)
        {
            indices.add(buf.readInt());
            stacks.add(readLargeItemStack(buf));
        }
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slotCount);
        buf.writeInt(indices.size());
        for (int i = 0; i < indices.size(); i++)
        {
            buf.writeInt(indices.get(i));
            writeLargeItemStack(buf, stacks.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientHelper.handleSendSlotChanges(this);
        return true;
    }

    public static ItemStack readLargeItemStack(FriendlyByteBuf buf)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        int itemId = buf.readVarInt();

        if (itemId >= 0)
        {
            int count = buf.readVarInt();
            itemstack = new ItemStack(Item.byId(itemId), count);
            itemstack.readShareTag(buf.readNbt());
        }

        return itemstack;
    }

    public static void writeLargeItemStack(FriendlyByteBuf buf, ItemStack stack)
    {
        if (stack.getCount() <= 0)
        {
            buf.writeVarInt(-1);
        }
        else
        {
            buf.writeVarInt(Item.getId(stack.getItem()));
            buf.writeVarInt(stack.getCount());
            CompoundTag nbttagcompound = null;
            if (stack.isDamageableItem() || stack.getItem().shouldOverrideMultiplayerNbt())
            {
                nbttagcompound = stack.getShareTag();
            }
            buf.writeNbt(nbttagcompound);
        }
    }
}