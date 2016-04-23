package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationAggregator;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.IInventoryAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TileInterface extends TileEntity
        implements ITickable, IBrowserExtension
{
    static final int FilterCount = 9;

    FilterInventory filters = new FilterInventory(FilterCount);
    ItemStackHandler outputs = new ItemStackHandler(FilterCount)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            markDirty();
        }
    };

    public IItemHandler inventoryOutputs()
    {
        return outputs;
    }

    public IItemHandler inventoryFilter()
    {
        return filters;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T)outputs;
        return super.getCapability(capability, facing);
    }

    public IInventoryAutomation getAutomation()
    {
        AutomationAggregator aggregator = new AutomationAggregator();

        List<IInventoryAutomation> seen = Lists.newArrayList();
        Set<BlockPos> scanned = Sets.newHashSet();
        Queue<Triple<BlockPos, EnumFacing, Integer>> pending = Queues.newArrayDeque();

        IBlockState state = worldObj.getBlockState(getPos());
        if (state.getBlock() != EnderRiftMod.riftInterface)
            return aggregator;

        EnumFacing facing = state.getValue(BlockInterface.FACING);
        pending.add(Triple.of(this.pos, facing, 0));

        while (pending.size() > 0)
        {
            Triple<BlockPos, EnumFacing, Integer> pair = pending.remove();
            BlockPos pos2 = pair.getLeft();

            if (scanned.contains(pos2))
            {
                continue;
            }

            scanned.add(pos2);

            int distance = pair.getRight();

            if (distance >= TileProxy.MAX_SCAN_DISTANCE)
            {
                continue;
            }

            facing = pair.getMiddle();

            TileEntity te = worldObj.getTileEntity(pos2);
            if (te != null)
            {
                if (te instanceof IBrowserExtension)
                {
                    ((IBrowserExtension) te).gatherNeighbours(pending, facing.getOpposite(), distance + 1);
                }
                else
                {
                    IInventoryAutomation automated = AutomationHelper.get(te, facing.getOpposite());
                    if (automated != null) seen.add(automated);
                }
            }
        }

        aggregator.addAll(seen);

        return aggregator;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
    }

    @Override
    public void markDirty(Set<BlockPos> scanned, int distance, Queue<Pair<BlockPos, Integer>> pending)
    {
        // Do nothing here
    }

    @Override
    public void gatherNeighbours(Queue<Triple<BlockPos, EnumFacing, Integer>> pending, EnumFacing faceFrom, int distance)
    {
        pending.add(Triple.of(this.pos.offset(faceFrom.getOpposite()), faceFrom, distance));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void update()
    {
        if (getAutomation() == null)
            return;

        boolean anyChanged = false;

        for (int i = 0; i < FilterCount; i++)
        {
            ItemStack inFilter = filters.getStackInSlot(i);
            ItemStack inSlot = outputs.getStackInSlot(i);
            if (inFilter != null)
            {
                if (inSlot == null)
                {
                    int free = 64;
                    inSlot = getAutomation().extractItems(inFilter, free, false);
                    outputs.setStackInSlot(i, inSlot);
                    if (inSlot != null)
                        anyChanged = true;
                }
                else if (inSlot.isItemEqual(inFilter))
                {
                    int free = inSlot.getMaxStackSize() - inSlot.stackSize;
                    if (free > 0)
                    {
                        ItemStack extracted = getAutomation().extractItems(inFilter, free, false);
                        if (extracted != null)
                        {
                            inSlot.stackSize += extracted.stackSize;
                            anyChanged = true;
                        }
                    }
                }
                else
                {
                    int stackSize = inSlot.stackSize;
                    inSlot = getAutomation().insertItems(inSlot);
                    outputs.setStackInSlot(i, inSlot);
                    if (inSlot == null || stackSize != inSlot.stackSize)
                        anyChanged = true;
                }
            }
            else if (inSlot != null)
            {
                int stackSize = inSlot.stackSize;
                inSlot = getAutomation().insertItems(inSlot);
                outputs.setStackInSlot(i, inSlot);
                if (inSlot == null || stackSize != inSlot.stackSize)
                    anyChanged = true;
            }
        }

        if (anyChanged)
            markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList _filters = compound.getTagList("Filters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _filters.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = _filters.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < filters.getSlots())
            {
                filters.setStackInSlot(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
            }
        }

        NBTTagList _outputs = compound.getTagList("Outputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.tagCount(); ++i)
        {
            NBTTagCompound slot = _outputs.getCompoundTagAt(i);
            int j = slot.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.getSlots())
            {
                outputs.setStackInSlot(j, ItemStack.loadItemStackFromNBT(slot));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagList _filters = new NBTTagList();
        for (int i = 0; i < filters.getSlots(); ++i)
        {
            ItemStack stack = filters.getStackInSlot(i);
            if (stack != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                stack.writeToNBT(nbttagcompound);
                _filters.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Filters", _filters);

        NBTTagList _outputs = new NBTTagList();
        for (int i = 0; i < outputs.getSlots(); ++i)
        {
            ItemStack stack = outputs.getStackInSlot(i);
            if (stack != null)
            {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte) i);
                stack.writeToNBT(slot);
                _outputs.appendTag(slot);
            }
        }

        compound.setTag("Outputs", _outputs);
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    class FilterInventory implements IItemHandlerModifiable
    {
        final ItemStack[] filters;

        public FilterInventory(int slotCount)
        {
            filters = new ItemStack[slotCount];
        }

        @Override
        public int getSlots()
        {
            return FilterCount;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (slot < 0 || slot >= filters.length)
                return null;
            return filters[slot];
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (slot < 0 || slot >= filters.length)
                return stack;

            if (!simulate)
            {
                filters[slot] = stack.copy();
                filters[slot].stackSize = 1;
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return null;
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack)
        {
            filters[index] = stack;

            if (stack != null && stack.stackSize > this.getInventoryStackLimit())
            {
                stack.stackSize = this.getInventoryStackLimit();
            }

            markDirty();
        }

        public int getInventoryStackLimit()
        {
            return 1;
        }

        public void clear()
        {
            for (int i = 0; i < filters.length; ++i)
            {
                filters[i] = null;
            }
        }
    }
}
