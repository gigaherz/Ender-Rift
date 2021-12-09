package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.automation.browser.AbstractBrowserContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetVisibleSlots
{
    public int windowId;
    public int[] visible;

    public SetVisibleSlots(int windowId, int[] visible)
    {
        this.windowId = windowId;
        this.visible = visible;
    }

    public SetVisibleSlots(FriendlyByteBuf buf)
    {
        windowId = buf.readInt();
        int num = buf.readInt();
        visible = new int[num];
        for (int i = 0; i < num; i++)
        {
            visible[i] = buf.readInt();
        }
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(visible.length);
        for (int i = 0; i < visible.length; i++)
        {
            buf.writeInt(visible[i]);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        final ServerPlayer player = context.get().getSender();

        context.get().enqueueWork(() ->
        {
            if (player != null)
            {
                if (player.containerMenu instanceof AbstractBrowserContainer && player.containerMenu.containerId == this.windowId)
                {
                    ((AbstractBrowserContainer) player.containerMenu).setVisibleSlots(this.visible);
                }
            }
        });
        return true;
    }
}