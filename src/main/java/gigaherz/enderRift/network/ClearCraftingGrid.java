package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.CraftingBrowserContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public ClearCraftingGrid(PacketBuffer buf)
    {
        windowId = buf.readInt();
        toPlayer = buf.readBoolean();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowId);
        buf.writeBoolean(toPlayer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            final ServerPlayerEntity player = context.get().getSender();

            if (player.openContainer instanceof CraftingBrowserContainer
                    && player.openContainer.windowId == this.windowId)
            {
                ((CraftingBrowserContainer) player.openContainer).clearCraftingGrid(player, toPlayer);
            }
        });
        return true;
    }
}