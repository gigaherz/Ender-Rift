package gigaherz.enderRift.network;

import gigaherz.enderRift.EnderRiftMod;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateField
        implements IMessage
{
    public int windowId;
    public int field;
    public int value;

    public UpdateField()
    {
    }

    public UpdateField(int windowId, int field, int value)
    {
        this.windowId = windowId;
        this.field = field;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        field = buf.readByte();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeByte(field);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<UpdateField, IMessage>
    {
        @Override
        public IMessage onMessage(UpdateField message, MessageContext ctx)
        {
            EnderRiftMod.proxy.handleUpdateField(message);

            return null; // no response in this case
        }
    }
}
