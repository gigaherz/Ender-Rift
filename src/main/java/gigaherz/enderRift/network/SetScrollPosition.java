package gigaherz.enderRift.network;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.gui.ContainerBrowser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetScrollPosition
        implements IMessage
{
    public int windowId;
    public int position;

    public SetScrollPosition()
    {
    }

    public SetScrollPosition(int windowId, int position)
    {
        this.windowId = windowId;
        this.position = position;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        position = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(position);
    }

    public static class Handler implements IMessageHandler<SetScrollPosition, IMessage>
    {
        @Override
        public IMessage onMessage(final SetScrollPosition message, MessageContext ctx)
        {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final WorldServer world = (WorldServer)player.worldObj;

            world.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    if (player.openContainer != null
                            && player.openContainer.windowId == message.windowId
                            && player.openContainer instanceof ContainerBrowser)
                    {
                        ((ContainerBrowser)player.openContainer).setScrollPosition(message.position);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
