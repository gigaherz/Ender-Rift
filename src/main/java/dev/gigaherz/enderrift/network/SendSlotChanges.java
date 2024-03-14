package dev.gigaherz.enderrift.network;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.client.ClientHelper;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.function.Supplier;

public class SendSlotChanges implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("send_slot_changes");

    public int windowId;
    public int slotCount;
    public List<Integer> indices;
    public List<ItemStack> stacks;
    public LongList stackSizes;

    public SendSlotChanges(int windowId, int slotCount, List<Integer> indices, List<ItemStack> stacks, LongList stackSizes)
    {
        this.windowId = windowId;
        this.slotCount = slotCount;
        this.indices = indices;
        this.stacks = stacks;
        this.stackSizes = stackSizes;
    }

    public SendSlotChanges(FriendlyByteBuf buf)
    {
        indices = Lists.newArrayList();
        stacks = Lists.newArrayList();
        stackSizes = new LongArrayList();
        windowId = buf.readInt();
        slotCount = buf.readInt();

        int count = buf.readInt();
        while (count-- > 0)
        {
            indices.add(buf.readInt());
            stacks.add(buf.readItemWithLargeCount());
            stackSizes.add(buf.readVarLong());
        }
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slotCount);
        buf.writeInt(indices.size());
        for (int i = 0; i < indices.size(); i++)
        {
            buf.writeInt(indices.get(i));
            buf.writeItemWithLargeCount(stacks.get(i));
            buf.writeVarLong(stackSizes.getLong(i));
        }
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientHelper.handleSendSlotChanges(this);
    }
}