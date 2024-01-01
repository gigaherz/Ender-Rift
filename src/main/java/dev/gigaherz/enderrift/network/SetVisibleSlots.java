package dev.gigaherz.enderrift.network;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.AbstractBrowserContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class SetVisibleSlots implements CustomPacketPayload
{
    public static final ResourceLocation ID = EnderRiftMod.location("set_visible_slots");

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

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(visible.length);
        for (int i = 0; i < visible.length; i++)
        {
            buf.writeInt(visible[i]);
        }
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

            if (player.containerMenu instanceof AbstractBrowserContainer browser && browser.containerId == this.windowId)
            {
                browser.setVisibleSlots(this.visible);
            }
        });
    }
}