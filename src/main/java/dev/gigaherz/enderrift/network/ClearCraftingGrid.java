package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.automation.browser.CraftingBrowserContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

public class ClearCraftingGrid
{
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

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeBoolean(toPlayer);
    }

    public void handle(NetworkEvent.Context context)
    {
        context.enqueueWork(() ->
        {
            final ServerPlayer player = context.getSender();

            if (player != null
                    && player.containerMenu instanceof CraftingBrowserContainer browser
                    && browser.containerId == this.windowId)
            {
                browser.clearCraftingGrid(player, toPlayer);
            }
        });
    }
}