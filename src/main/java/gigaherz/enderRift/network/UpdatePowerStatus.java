package gigaherz.enderRift.network;

import gigaherz.enderRift.EnderRiftMod;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdatePowerStatus
        implements IMessage
{
    public int windowId;
    public boolean status;

    public UpdatePowerStatus()
    {
    }

    public UpdatePowerStatus(int windowId, boolean values)
    {
        this.windowId = windowId;
        this.status = values;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        status = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeBoolean(status);
    }

    public static class Handler implements IMessageHandler<UpdatePowerStatus, IMessage>
    {
        @Override
        public IMessage onMessage(UpdatePowerStatus message, MessageContext ctx)
        {
            EnderRiftMod.proxy.handleUpdatePowerStatus(message);

            return null; // no response in this case
        }
    }
}
