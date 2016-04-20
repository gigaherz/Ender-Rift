package gigaherz.enderRift.network;

import gigaherz.enderRift.EnderRiftMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetSpecialSlot
        implements IMessage
{
    public int windowId;
    public int slot;
    public ItemStack stack;

    public SetSpecialSlot()
    {
    }

    public SetSpecialSlot(int windowId, int slot, ItemStack stack)
    {
        this.windowId = windowId;
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        slot = buf.readInt();
        int count = buf.readInt();
        if (count > 0)
        {
            int meta = buf.readInt();
            Item item = Item.REGISTRY.getObject(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
            boolean hasTag = buf.readBoolean();
            stack = new ItemStack(item, count, meta);
            if (hasTag)
            {
                NBTTagCompound tag = ByteBufUtils.readTag(buf);
                stack.setTagCompound(tag);
            }
        }
        else
        {
            stack = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slot);
        if (stack != null && stack.stackSize > 0)
        {
            buf.writeInt(stack.stackSize);
            buf.writeInt(stack.getItemDamage());
            ByteBufUtils.writeUTF8String(buf, Item.REGISTRY.getNameForObject(stack.getItem()).toString());
            NBTTagCompound tag = stack.getTagCompound();
            buf.writeBoolean(tag != null);
            if (tag != null)
            {
                ByteBufUtils.writeTag(buf, tag);
            }
        }
        else
        {
            buf.writeInt(0);
        }
    }

    public static class Handler implements IMessageHandler<SetSpecialSlot, IMessage>
    {
        @Override
        public IMessage onMessage(SetSpecialSlot message, MessageContext ctx)
        {
            EnderRiftMod.proxy.handleSetSpecialSlot(message);

            return null; // no response in this case
        }
    }
}
