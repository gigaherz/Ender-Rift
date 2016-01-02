package gigaherz.enderRift.storage;

import com.google.common.base.Predicate;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.api.automation.IInventoryAutomation;
import gigaherz.enderRift.blocks.TileEnderRift;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RiftInventory implements IInventoryAutomation, IBrowsableInventory
{
    private final List<ItemStack> inventorySlots = new ArrayList<>();
    private final RiftStorageWorldData manager;

    final List<Reference<? extends TileEnderRift>> listeners = new ArrayList<>();
    final ReferenceQueue<TileEnderRift> deadListeners = new ReferenceQueue<>();

    RiftInventory(RiftStorageWorldData manager) {
        this.manager = manager;
    }

    public void addWeakListener(TileEnderRift e) {
        listeners.add(new WeakReference<>(e, deadListeners));
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

    public void markDirty()
    {
        for (Reference<? extends TileEnderRift>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll()) {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends TileEnderRift>> it = listeners.iterator(); it.hasNext(); ) {
            TileEnderRift rift = it.next().get();
            if (rift == null || rift.isInvalid()) {
                it.remove();
            } else {
                rift.setDirty();
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

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
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
    public IInventoryAutomation getInventoryForSide(@Nonnull EnumFacing face)
    {
        return this;
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
        if(remaining.stackSize > 0)
          inventorySlots.add(remaining);

        markDirty();

        return null;
    }

    @Override
    public ItemStack pullItems(int limit, Predicate<ItemStack> filter)
    {
        for(int i=0;i<inventorySlots.size();i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if(slot != null)
            {
                int available = Math.min(limit, slot.stackSize);
                if(filter.apply(slot) && available > 0)
                {
                    ItemStack pulled = slot.splitStack(available);
                    if(slot.stackSize <= 0)
                        inventorySlots.remove(i);
                    markDirty();
                    return pulled;
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack extractItems(@Nonnull ItemStack stack, int wanted)
    {
        ItemStack extracted = stack.copy();
        extracted.stackSize = 0;

        if(stack.stackSize <= 0)
            return null;

        for(int i=0;i<inventorySlots.size();i++)
        {
            ItemStack slot = inventorySlots.get(i);
            if(slot != null)
            {
                int available = Math.min(wanted, slot.stackSize);
                if (ItemStack.areItemsEqual(slot, stack) && ItemStack.areItemStackTagsEqual(slot, stack) && available > 0)
                {
                    slot.stackSize -= available;
                    extracted.stackSize += available;
                    if(slot.stackSize <= 0)
                        inventorySlots.remove(i);

                    wanted = extracted.stackSize - stack.stackSize;
                    if(wanted <= 0)
                        break;
                }
            }
        }

        if(extracted.stackSize <= 0)
            return null;

        markDirty();
        return extracted;
    }
}
