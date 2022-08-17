package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.automation.browser.CraftingBrowserContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            final ServerPlayer player = context.get().getSender();

            if (player.containerMenu instanceof CraftingBrowserContainer
                    && player.containerMenu.containerId == this.windowId)
            {
                ((CraftingBrowserContainer) player.containerMenu).clearCraftingGrid(player, toPlayer);
            }
        });
        return true;
    }
}