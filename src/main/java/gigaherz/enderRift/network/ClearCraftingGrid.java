package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.ContainerCraftingBrowser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearCraftingGrid
{
    public int windowId;

    public ClearCraftingGrid(int windowId)
    {
        this.windowId = windowId;
    }

    public ClearCraftingGrid(PacketBuffer buf)
    {
        windowId = buf.readInt();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowId);
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            final ServerPlayerEntity player = context.get().getSender();

            if (player.openContainer != null
                    && player.openContainer.windowId == this.windowId
                    && player.openContainer instanceof ContainerCraftingBrowser)
            {
                ((ContainerCraftingBrowser) player.openContainer).clearCraftingGrid(player);
            }
        });
    }
}
