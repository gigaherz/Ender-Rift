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
    public int[] fields;

    public UpdateField()
    {
    }

    public UpdateField(int windowId, int[] values)
    {
        this.windowId = windowId;
        this.fields = values;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        fields = new int[buf.readByte()];
        for (int i = 0; i < fields.length; i++)
        {
            fields[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeByte(fields.length);
        for (int i = 0; i < fields.length; i++)
        {
            buf.writeInt(fields[i]);
        }
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
