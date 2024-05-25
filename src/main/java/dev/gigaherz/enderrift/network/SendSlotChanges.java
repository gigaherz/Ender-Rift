package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SendSlotChanges(
        int windowId,
        int slotCount,
        List<Integer> indices,
        List<ItemStack> stacks,
        List<Long> stackSizes
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("send_slot_changes");
    public static final Type<SendSlotChanges> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SendSlotChanges> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SendSlotChanges::windowId,
            ByteBufCodecs.VAR_INT, SendSlotChanges::slotCount,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.collection(ArrayList::new)), SendSlotChanges::indices,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), SendSlotChanges::stacks,
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.collection(ArrayList::new)), SendSlotChanges::stackSizes,
            SendSlotChanges::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientHelper.handleSendSlotChanges(this);
    }
}