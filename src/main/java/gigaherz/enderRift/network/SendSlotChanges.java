package gigaherz.enderRift.network;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.List;

public class SendSlotChanges
        implements IMessage
{
    public int windowId;
    public int slotCount;
    public List<Integer> indices;
    public List<ItemStack> stacks;

    public SendSlotChanges()
    {
        indices = Lists.newArrayList();
        stacks = Lists.newArrayList();
    }

    public SendSlotChanges(int windowId, int slotCount, List<Integer> indices, List<ItemStack> stacks)
    {
        this.windowId = windowId;
        this.slotCount = slotCount;
        this.indices = indices;
        this.stacks = stacks;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        slotCount = buf.readInt();

        int count = buf.readInt();
        while (count-- > 0)
        {
            indices.add(buf.readInt());
            stacks.add(readItemStackFromBuffer(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slotCount);
        buf.writeInt(indices.size());
        for(int i=0;i<indices.size();i++)
        {
            buf.writeInt(indices.get(i));
            writeItemStackToBuffer(buf, stacks.get(i));
        }
    }

    private ItemStack readItemStackFromBuffer(ByteBuf buf)
    {
        ItemStack itemstack = null;
        int i = buf.readShort();

        if (i >= 0)
        {
            int j = buf.readInt();
            int k = buf.readShort();
            itemstack = new ItemStack(Item.getItemById(i), j, k);
            itemstack.setTagCompound(ByteBufUtils.readTag(buf));
        }

        return itemstack;
    }

    private void writeItemStackToBuffer(ByteBuf buf, ItemStack stack)
    {
        if (stack == null)
        {
            buf.writeShort(-1);
        }
        else
        {
            buf.writeShort(Item.getIdFromItem(stack.getItem()));
            buf.writeInt(stack.stackSize);
            buf.writeShort(stack.getMetadata());
            NBTTagCompound nbttagcompound = null;

            if (stack.getItem().isDamageable() || stack.getItem().getShareTag())
            {
                nbttagcompound = stack.getTagCompound();
            }

            ByteBufUtils.writeTag(buf,nbttagcompound);
        }
    }

    public static class Handler implements IMessageHandler<SendSlotChanges, IMessage>
    {
        @Override
        public IMessage onMessage(SendSlotChanges message, MessageContext ctx)
        {
            EnderRiftMod.proxy.handleSendSlotChanges(message);

            return null; // no response in this case
        }
    }
}
