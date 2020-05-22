package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.CraftingBrowserContainer;
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

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            final ServerPlayerEntity player = context.get().getSender();

            if (player.openContainer instanceof CraftingBrowserContainer
                    && player.openContainer.windowId == this.windowId)
            {
                ((CraftingBrowserContainer) player.openContainer).clearCraftingGrid(player);
            }
        });
        return true;
    }
}
