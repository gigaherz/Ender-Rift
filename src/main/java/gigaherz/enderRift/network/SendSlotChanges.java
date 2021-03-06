package gigaherz.enderRift.network;

import com.google.common.collect.Lists;
import gigaherz.enderRift.client.ClientHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SendSlotChanges
{
    public int windowId;
    public int slotCount;
    public List<Integer> indices;
    public List<ItemStack> stacks;

    public SendSlotChanges(int windowId, int slotCount, List<Integer> indices, List<ItemStack> stacks)
    {
        this.windowId = windowId;
        this.slotCount = slotCount;
        this.indices = indices;
        this.stacks = stacks;
    }

    public SendSlotChanges(PacketBuffer buf)
    {
        indices = Lists.newArrayList();
        stacks = Lists.newArrayList();
        windowId = buf.readInt();
        slotCount = buf.readInt();

        int count = buf.readInt();
        while (count-- > 0)
        {
            indices.add(buf.readInt());
            stacks.add(readLargeItemStack(buf));
        }
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slotCount);
        buf.writeInt(indices.size());
        for (int i = 0; i < indices.size(); i++)
        {
            buf.writeInt(indices.get(i));
            writeLargeItemStack(buf, stacks.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        ClientHelper.handleSendSlotChanges(this);
        return true;
    }

    public static ItemStack readLargeItemStack(PacketBuffer buf)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        int itemId = buf.readVarInt();

        if (itemId >= 0)
        {
            int count = buf.readVarInt();
            itemstack = new ItemStack(Item.getItemById(itemId), count);
            itemstack.readShareTag(buf.readCompoundTag());
        }

        return itemstack;
    }

    public static void writeLargeItemStack(PacketBuffer buf, ItemStack stack)
    {
        if (stack.getCount() <= 0)
        {
            buf.writeVarInt(-1);
        }
        else
        {
            buf.writeVarInt(Item.getIdFromItem(stack.getItem()));
            buf.writeVarInt(stack.getCount());
            CompoundNBT nbttagcompound = null;
            if (stack.isDamageable() || stack.getItem().shouldSyncTag())
            {
                nbttagcompound = stack.getShareTag();
            }
            buf.writeCompoundTag(nbttagcompound);
        }
    }
}