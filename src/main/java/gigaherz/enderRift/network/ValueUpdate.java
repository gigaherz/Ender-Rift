package gigaherz.enderRift.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gigaherz.enderRift.blocks.TileEnderRift;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ValueUpdate
        implements IMessage {

    int dim;
    int posX;
    int posY;
    int posZ;

    public int barIndex;
    public int barValue;

    public ValueUpdate() {
    }

    public ValueUpdate(TileEntity tile, int bar, int value) {
        dim = tile.getWorldObj().provider.dimensionId;
        posX = tile.xCoord;
        posY = tile.yCoord;
        posZ = tile.zCoord;
        barIndex = bar;
        barValue = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        dim = buf.readInt();
        posX = buf.readInt();
        posY = buf.readInt();
        posZ = buf.readInt();
        barIndex = buf.readInt();
        barValue = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(dim);
        buf.writeInt(posX);
        buf.writeInt(posY);
        buf.writeInt(posZ);
        buf.writeInt(barIndex);
        buf.writeInt(barValue);
    }

    public TileEntity getTileEntityTarget() {
        World world = DimensionManager.getWorld(dim);
        return world.getTileEntity(posX, posY, posZ);
    }

    public static class Handler implements IMessageHandler<ValueUpdate, IMessage> {

        @Override
        public IMessage onMessage(ValueUpdate message, MessageContext ctx) {

            TileEntity tile = message.getTileEntityTarget();

            if (!(tile instanceof TileEnderRift)) {
                return null;
            }

            TileEnderRift rift = (TileEnderRift) tile;
            rift.updateValue(message.barIndex, message.barValue);

            return null;
        }
    }
}
