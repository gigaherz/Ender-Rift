package gigaherz.enderRift.network;

import gigaherz.enderRift.client.ClientHelper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePowerStatus
{
    public int windowId;
    public boolean status;

    public UpdatePowerStatus(int windowId, boolean values)
    {
        this.windowId = windowId;
        this.status = values;
    }

    public UpdatePowerStatus(ByteBuf buf)
    {
        windowId = buf.readInt();
        status = buf.readBoolean();
    }

    public void encode(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeBoolean(status);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientHelper.handleUpdatePowerStatus(this);
        return true;
    }
}
