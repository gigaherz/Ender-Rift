package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.CraftingBrowserMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearCraftingGrid(int containerId, boolean toPlayer) implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("clear_crafting_grid");
    public static final Type<ClearCraftingGrid> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ClearCraftingGrid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClearCraftingGrid::containerId,
            ByteBufCodecs.BOOL, ClearCraftingGrid::toPlayer,
            ClearCraftingGrid::new
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

            if (player.containerMenu instanceof CraftingBrowserMenu browser && browser.containerId == this.containerId)
            {
                browser.clearCraftingGrid(player, toPlayer);
            }
        });
    }
}