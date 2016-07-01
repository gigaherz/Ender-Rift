package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.ContainerCraftingBrowser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClearCraftingGrid
        implements IMessage
{
    public int windowId;

    public ClearCraftingGrid()
    {
    }

    public ClearCraftingGrid(int windowId)
    {
        this.windowId = windowId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
    }

    public static class Handler implements IMessageHandler<ClearCraftingGrid, IMessage>
    {
        @Override
        public IMessage onMessage(final ClearCraftingGrid message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer) player.worldObj;

            world.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    if (player.openContainer != null
                            && player.openContainer.windowId == message.windowId
                            && player.openContainer instanceof ContainerCraftingBrowser)
                    {
                        ((ContainerCraftingBrowser) player.openContainer).clearCraftingGrid(player);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
