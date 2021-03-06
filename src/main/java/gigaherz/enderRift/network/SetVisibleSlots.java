package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.AbstractBrowserContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public SetVisibleSlots(PacketBuffer buf)
    {
        windowId = buf.readInt();
        int num = buf.readInt();
        visible = new int[num];
        for (int i = 0; i < num; i++)
        {
            visible[i] = buf.readInt();
        }
    }

    public void encode(PacketBuffer buf)
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
        final ServerPlayerEntity player = context.get().getSender();

        context.get().enqueueWork(() ->
        {
            if (player != null)
            {
                if (player.openContainer instanceof AbstractBrowserContainer && player.openContainer.windowId == this.windowId)
                {
                    ((AbstractBrowserContainer) player.openContainer).setVisibleSlots(this.visible);
                }
            }
        });
        return true;
    }
}