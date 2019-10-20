package gigaherz.enderRift.network;

import gigaherz.enderRift.client.ClientHelper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateField
{
    public int windowId;
    public int[] fields;

    public UpdateField(int windowId, int[] values)
    {
        this.windowId = windowId;
        this.fields = values;
    }

    public UpdateField(ByteBuf buf)
    {
        windowId = buf.readInt();
        fields = new int[buf.readByte()];
        for (int i = 0; i < fields.length; i++)
        {
            fields[i] = buf.readInt();
        }
    }

    public void encode(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeByte(fields.length);
        for (int i = 0; i < fields.length; i++)
        {
            buf.writeInt(fields[i]);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientHelper.handleUpdateField(this);
        return true;
    }
}
