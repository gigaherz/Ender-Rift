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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TileInterface extends TileEntity
        implements IInventory, ITickable, IBrowserExtension
{
    static final int FilterCount = 9;

    final ItemStack[] filters = new ItemStack[FilterCount];
    final ItemStack[] outputs = new ItemStack[FilterCount];

    InvWrapper invHandler = new InvWrapper(this);

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
            return (T) invHandler;
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
            if (filters[i] != null)
            {
                if (outputs[i] == null)
                {
                    int free = 64;
                    outputs[i] = getAutomation().extractItems(filters[i], free, false);
                    if (outputs[i] != null)
                        anyChanged = true;
                }
                else if (outputs[i].isItemEqual(filters[i]))
                {
                    int free = outputs[i].getMaxStackSize() - outputs[i].stackSize;
                    if (free > 0)
                    {
                        ItemStack extracted = getAutomation().extractItems(filters[i], free, false);
                        if (extracted != null)
                        {
                            outputs[i].stackSize += extracted.stackSize;
                            anyChanged = true;
                        }
                    }
                }
                else if (outputs[i] != null)
                {
                    int stackSize = outputs[i].stackSize;
                    outputs[i] = getAutomation().pushItems(outputs[i]);
                    if (outputs[i] == null || stackSize != outputs[i].stackSize)
                        anyChanged = true;
                }
            }
            else if (outputs[i] != null)
            {
                int stackSize = outputs[i].stackSize;
                outputs[i] = getAutomation().pushItems(outputs[i]);
                if (outputs[i] == null || stackSize != outputs[i].stackSize)
                    anyChanged = true;
            }
        }

        if (anyChanged)
            markDirty();
    }

    @Override
    public int getSizeInventory()
    {
        return FilterCount;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (index < 0 || index >= outputs.length)
            return null;
        return outputs[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (index < 0 || index >= outputs.length)
            return null;

        if (outputs[index] == null)
            return null;

        if (count > outputs[index].stackSize)
            count = outputs[index].stackSize;

        ItemStack result = outputs[index].splitStack(count);

        if (outputs[index].stackSize <= 0)
        {
            outputs[index] = null;
        }

        this.markDirty();

        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (index < 0 || index >= outputs.length)
            return null;

        if (outputs[index] == null)
            return null;

        ItemStack itemstack = outputs[index];
        outputs[index] = null;
        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index < 0 || index >= outputs.length)
            return;

        outputs[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getName()
    {
        return "container." + EnderRiftMod.MODID + ".interface";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
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

            if (j >= 0 && j < filters.length)
            {
                filters[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        NBTTagList _outputs = compound.getTagList("Outputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = _outputs.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.length)
            {
                outputs[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagList _filters = new NBTTagList();
        for (int i = 0; i < filters.length; ++i)
        {
            if (filters[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                filters[i].writeToNBT(nbttagcompound);
                _filters.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Filters", _filters);

        NBTTagList _outputs = new NBTTagList();
        for (int i = 0; i < outputs.length; ++i)
        {
            if (outputs[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                outputs[i].writeToNBT(nbttagcompound);
                _outputs.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Outputs", _outputs);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    public void clear()
    {
        for (int i = 0; i < filters.length; ++i)
        {
            filters[i] = null;
        }
        for (int i = 0; i < outputs.length; ++i)
        {
            outputs[i] = null;
        }
        markDirty();
    }

    public IInventory inventoryFilter()
    {
        return new IInventory()
        {

            @Override
            public void markDirty()
            {
            }

            @Override
            public int getSizeInventory()
            {
                return FilterCount;
            }

            @Override
            public ItemStack getStackInSlot(int index)
            {
                if (index < 0 || index >= filters.length)
                    return null;
                return filters[index];
            }

            @Override
            public ItemStack decrStackSize(int index, int count)
            {
                if (index < 0 || index >= filters.length)
                    return null;

                if (filters[index] == null)
                    return null;

                if (count > filters[index].stackSize)
                    count = filters[index].stackSize;

                ItemStack result = filters[index].splitStack(count);

                if (filters[index].stackSize <= 0)
                {
                    filters[index] = null;
                }

                this.markDirty();

                return result;
            }

            @Override
            public ItemStack removeStackFromSlot(int index)
            {
                if (index < 0 || index >= filters.length)
                    return null;

                if (filters[index] == null)
                    return null;

                ItemStack itemstack = filters[index];
                filters[index] = null;
                return itemstack;
            }

            @Override
            public void setInventorySlotContents(int index, ItemStack stack)
            {
                filters[index] = stack;

                if (stack != null && stack.stackSize > this.getInventoryStackLimit())
                {
                    stack.stackSize = this.getInventoryStackLimit();
                }

                this.markDirty();
            }

            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public boolean hasCustomName()
            {
                return false;
            }

            @Override
            public IChatComponent getDisplayName()
            {
                return null;
            }

            @Override
            public int getInventoryStackLimit()
            {
                return 1;
            }

            @Override
            public boolean isUseableByPlayer(EntityPlayer player)
            {
                return true;
            }

            @Override
            public void openInventory(EntityPlayer player)
            {
            }

            @Override
            public void closeInventory(EntityPlayer player)
            {
            }

            @Override
            public boolean isItemValidForSlot(int index, ItemStack stack)
            {
                return true;
            }

            @Override
            public int getField(int id)
            {
                return 0;
            }

            @Override
            public void setField(int id, int value)
            {
            }

            @Override
            public int getFieldCount()
            {
                return 0;
            }

            @Override
            public void clear()
            {
                for (int i = 0; i < filters.length; ++i)
                {
                    filters[i] = null;
                }
            }
        };
    }
}
