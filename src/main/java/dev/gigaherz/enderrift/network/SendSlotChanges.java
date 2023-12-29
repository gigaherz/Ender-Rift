package dev.gigaherz.enderrift.network;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.client.ClientHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public void handle(NetworkEvent.Context context)
    {
        ClientHelper.handleSendSlotChanges(this);
    }

    public static ItemStack readLargeItemStack(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item item = buf.readById(BuiltInRegistries.ITEM);
            int i = buf.readVarInt();
            return net.neoforged.neoforge.attachment.AttachmentInternals.reconstructItemStack(item, i, buf.readNbt());
        }
    }

    public static void writeLargeItemStack(FriendlyByteBuf buf, ItemStack stack) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            Item item = stack.getItem();
            buf.writeId(BuiltInRegistries.ITEM, item);
            buf.writeVarInt(stack.getCount());
            CompoundTag compoundtag = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundtag = stack.getTag();
            }
            compoundtag = net.neoforged.neoforge.attachment.AttachmentInternals.addAttachmentsToTag(compoundtag, stack, false);

            buf.writeNbt(compoundtag);
        }
    }
}