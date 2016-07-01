package gigaherz.enderRift.network;

import gigaherz.enderRift.automation.browser.ContainerBrowser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetVisibleSlots
        implements IMessage
{
    public int windowId;
    public int[] visible;

    public SetVisibleSlots()
    {
    }

    public SetVisibleSlots(int windowId, int[] visible)
    {
        this.windowId = windowId;
        this.visible = visible;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        int num = buf.readInt();
        visible = new int[num];
        for (int i = 0; i < num; i++)
        { visible[i] = buf.readInt(); }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(visible.length);
        for (int i = 0; i < visible.length; i++)
        { buf.writeInt(visible[i]); }
    }

    public static class Handler implements IMessageHandler<SetVisibleSlots, IMessage>
    {
        @Override
        public IMessage onMessage(final SetVisibleSlots message, MessageContext ctx)
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
                        ((ContainerBrowser) player.openContainer).setVisibleSlots(message.visible);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
