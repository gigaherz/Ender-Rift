package gigaherz.enderRift.network;

import gigaherz.enderRift.gui.ContainerBrowser;
import gigaherz.enderRift.misc.SortMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetSortMode
        implements IMessage
{
    public int windowId;
    public SortMode sortMode;

    public SetSortMode()
    {
    }

    public SetSortMode(int windowId, SortMode sortMode)
    {
        this.windowId = windowId;
        this.sortMode = sortMode;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        sortMode = SortMode.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(sortMode.ordinal());
    }

    public static class Handler implements IMessageHandler<SetSortMode, IMessage>
    {
        @Override
        public IMessage onMessage(final SetSortMode message, MessageContext ctx)
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
                        ((ContainerBrowser) player.openContainer).setSortMode(message.sortMode);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
