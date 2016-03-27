package gigaherz.enderRift.network;

import gigaherz.enderRift.gui.ContainerBrowser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetFilterText
        implements IMessage
{
    public int windowId;
    public String filterText;

    public SetFilterText()
    {
    }

    public SetFilterText(int windowId, String filterText)
    {
        this.windowId = windowId;
        this.filterText = filterText;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        filterText = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        ByteBufUtils.writeUTF8String(buf, filterText);
    }

    public static class Handler implements IMessageHandler<SetFilterText, IMessage>
    {
        @Override
        public IMessage onMessage(final SetFilterText message, MessageContext ctx)
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
                            && player.openContainer instanceof ContainerBrowser)
                    {
                        ((ContainerBrowser) player.openContainer).setFilterText(message.filterText);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
