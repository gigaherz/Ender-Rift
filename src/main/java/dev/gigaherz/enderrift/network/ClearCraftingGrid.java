package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.CraftingBrowserContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ClearCraftingGrid implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("clear_crafting_grid");

    public final int windowId;
    public final boolean toPlayer;

    public ClearCraftingGrid(int windowId, boolean toPlayer)
    {
        this.windowId = windowId;
        this.toPlayer = toPlayer;
    }

    public ClearCraftingGrid(FriendlyByteBuf buf)
    {
        windowId = buf.readInt();
        toPlayer = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeBoolean(toPlayer);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        context.workHandler().execute(() ->
        {
            final Player player = context.player().orElseThrow();

            if (player.containerMenu instanceof CraftingBrowserContainer browser && browser.containerId == this.windowId)
            {
                browser.clearCraftingGrid(player, toPlayer);
            }
        });
    }
}