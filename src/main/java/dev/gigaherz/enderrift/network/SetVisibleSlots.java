package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.AbstractBrowserContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetVisibleSlots(int windowId, int[] visible) implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("set_visible_slots");
    public static final Type<SetVisibleSlots> TYPE = new Type<>(ID);

    private static <T extends ByteBuf> StreamCodec<T, int[]> intArray()
    {
        return new StreamCodec<>()
        {
            @Override
            public int[] decode(T pBuffer)
            {
                int count = ByteBufCodecs.VAR_INT.decode(pBuffer);
                var array = new int[count];
                for (int i = 0; i < count; i++)
                {
                    var element = ByteBufCodecs.VAR_INT.decode(pBuffer);
                    array[i] = element;
                }
                return array;
            }

            @Override
            public void encode(T pBuffer, int[] pValue)
            {
                int count = pValue.length;
                ByteBufCodecs.VAR_INT.encode(pBuffer, count);
                for (int i = 0; i < count; i++)
                {
                    var element = pValue[i];
                    ByteBufCodecs.VAR_INT.encode(pBuffer, element);
                }
            }
        };
    }

    public static final StreamCodec<ByteBuf, SetVisibleSlots> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetVisibleSlots::windowId,
            intArray(), SetVisibleSlots::visible,
            SetVisibleSlots::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            final Player player = context.player();

            if (player.containerMenu instanceof AbstractBrowserContainer browser && browser.containerId == this.windowId)
            {
                browser.setVisibleSlots(this.visible);
            }
        });
    }
}