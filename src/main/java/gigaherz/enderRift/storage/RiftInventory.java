package gigaherz.enderRift.storage;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import gigaherz.enderRift.automation.IInventoryAutomation;
import gigaherz.enderRift.blocks.TileEnderRift;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class RiftInventory implements IInventoryAutomation
{
    private final RiftStorageWorldData manager;

    final List<Reference<? extends TileEnderRift>> listeners = Lists.newArrayList();
    final ReferenceQueue<TileEnderRift> deadListeners = new ReferenceQueue<TileEnderRift>();
    private final List<ItemStack> inventorySlots = Lists.newArrayList();

    RiftInventory(RiftStorageWorldData manager)
    {
        this.manager = manager;
    }

    public void addWeakListener(TileEnderRift e)
    {
        listeners.add(new WeakReference<TileEnderRift>(e, deadListeners));
    }

    private void onContentsChanged()
    {
        for (Reference<? extends TileEnderRift>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends TileEnderRift>> it = listeners.iterator(); it.hasNext(); )
        {
            TileEnderRift rift = it.next().get();
            if (rift == null || rift.isInvalid())
            {
                it.remove();
            }
            else
            {
                rift.markDirty();
            }
        }

        manager.markDirty();
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList itemList = nbtTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

        inventorySlots.clear();

        for (int i = 0; i < itemList.tagCount(); ++i)
        {
            NBTTagCompound slot = itemList.getCompoundTagAt(i);
            inventorySlots.add(ItemStack.loadItemStackFromNBT(slot));
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList itemList = new NBTTagList();

        for (ItemStack stack : inventorySlots)
        {
            if (stack != null)
            {
                NBTTagCompound slot = new NBTTagCompound();
                stack.writeToNBT(slot);
                itemList.appendTag(slot);
            }
        }

        nbtTagCompound.setTag("Items", itemList);
    }

    @Override
    public int getSizeInventory()
    {
        return inventorySlots.size();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventorySlots.get(index);
    }

    @Override
    public ItemStack pushItems(@Nonnull ItemStack stack)
    {
        ItemStack remaining = stack.copy();

        // Try to fill existing slots first
        for (ItemStack slot : inventorySlots)
        {
            if (slot != null)
            {
                int max = Math.min(remaining.getMaxStackSize(), 64);
                int transfer = Math.min(remaining.stackSize, max - slot.stackSize);
                if (transfer > 0 && ItemStack.areItemsEqual(slot, remaining) && ItemStack.areItemStackTagsEqual(slot, remaining))
                {
                    slot.stackSize += transfer;
                    remaining.stackSize -= transfer;
                    if (remaining.stackSize <= 0)
                        break;
                }
            }
        }

        // Then place any remaining items in the first available empty slot
        if (remaining.stackSize > 0)
            inventorySlots.add(remaining);

        onContentsChanged();

        return null;
    }

    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        for (int i = 0; i < inventorySlots.size(); i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if (slot != null)
            {
                int available = Math.min(limit, slot.stackSize);
                if (filter.apply(slot) && available > 0)
                {
                    ItemStack pulled = slot.splitStack(available);
                    if (slot.stackSize <= 0)
                        inventorySlots.remove(i);
                    onContentsChanged();
                    return pulled;
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack extractItems(@Nonnull ItemStack stack, int wanted, boolean simulate)
    {
        ItemStack extracted = stack.copy();
        extracted.stackSize = 0;

        if (stack.stackSize <= 0)
            return null;

        for (int i = 0; i < inventorySlots.size(); i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if (slot != null)
            {
                int available = Math.min(wanted, slot.stackSize);
                if (available > 0 && ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack))
                {
                    extracted.stackSize += available;

                    if (!simulate)
                    {
                        slot.stackSize -= available;
                        if (slot.stackSize <= 0)
                            inventorySlots.remove(i);
                    }

                    wanted -= available;
                    if (wanted <= 0)
                        break;
                }
            }
        }

        if (extracted.stackSize <= 0)
            return null;

        onContentsChanged();
        return extracted;
    }
}
