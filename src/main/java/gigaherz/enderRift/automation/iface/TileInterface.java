package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.common.IPoweredAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileInterface extends TileAggregator implements IPoweredAutomation
{
    private static final int FilterCount = 9;

    private FilterInventory filters = new FilterInventory(FilterCount);
    private ItemStackHandler outputs = new ItemStackHandler(FilterCount)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            markDirty();
        }
    };

    private EnumFacing facing = null;

    @Nullable
    public EnumFacing getFacing()
    {
        if (facing == null && world != null)
        {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == EnderRiftMod.riftInterface)
            {
                facing = state.getValue(BlockInterface.FACING).getOpposite();
            }
        }
        return facing;
    }

    public IItemHandler inventoryOutputs()
    {
        return outputs;
    }

    public IItemHandler inventoryFilter()
    {
        return filters;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (facing == getFacing())
        {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (facing == getFacing())
        {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                return (T) outputs;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void markDirty()
    {
        facing = null;
        super.markDirty();
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return side == getFacing().getOpposite();
    }

    @Override
    public void update()
    {
        super.update();

        if (world.isRemote)
            return;

        if (getCombinedInventory() == null)
            return;

        boolean anyChanged = false;

        for (int i = 0; i < FilterCount; i++)
        {
            ItemStack inFilter = filters.getStackInSlot(i);
            ItemStack inSlot = outputs.getStackInSlot(i);
            if (inFilter.getCount() > 0)
            {
                if (inSlot.getCount() <= 0)
                {
                    int free = 64;
                    inSlot = AutomationHelper.extractItems(getCombinedInventory(), inFilter, free, false);
                    outputs.setStackInSlot(i, inSlot);
                    if (inSlot.getCount() > 0)
                        anyChanged = true;
                }
                else if (inSlot.isItemEqual(inFilter))
                {
                    int free = inSlot.getMaxStackSize() - inSlot.getCount();
                    if (free > 0)
                    {
                        ItemStack extracted = AutomationHelper.extractItems(getCombinedInventory(), inFilter, free, false);
                        if (extracted.getCount() > 0)
                        {
                            inSlot.grow(extracted.getCount());
                            anyChanged = true;
                        }
                    }
                }
                else
                {
                    int stackSize = inSlot.getCount();
                    inSlot = AutomationHelper.insertItems(getCombinedInventory(), inSlot);
                    outputs.setStackInSlot(i, inSlot);
                    if (stackSize != inSlot.getCount())
                        anyChanged = true;
                }
            }
            else if (inSlot.getCount() > 0)
            {
                int stackSize = inSlot.getCount();
                inSlot = AutomationHelper.insertItems(getCombinedInventory(), inSlot);
                outputs.setStackInSlot(i, inSlot);
                if (stackSize != inSlot.getCount())
                    anyChanged = true;
            }
        }

        if (anyChanged)
            markDirty();
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
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
                filters.setStackInSlot(j, new ItemStack(nbttagcompound));
            }
        }

        NBTTagList _outputs = compound.getTagList("Outputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.tagCount(); ++i)
        {
            NBTTagCompound slot = _outputs.getCompoundTagAt(i);
            int j = slot.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.getSlots())
            {
                outputs.setStackInSlot(j, new ItemStack(slot));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        NBTTagList _filters = new NBTTagList();
        for (int i = 0; i < filters.getSlots(); ++i)
        {
            ItemStack stack = filters.getStackInSlot(i);
            if (stack.getCount() > 0)
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
            if (stack.getCount() > 0)
            {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte) i);
                stack.writeToNBT(slot);
                _outputs.appendTag(slot);
            }
        }

        compound.setTag("Outputs", _outputs);

        return compound;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    private class FilterInventory implements IItemHandlerModifiable
    {
        final NonNullList<ItemStack> filters;

        public FilterInventory(int slotCount)
        {
            filters = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        }

        @Override
        public int getSlots()
        {
            return FilterCount;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (slot < 0 || slot >= filters.size())
                return ItemStack.EMPTY;
            return filters.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (slot < 0 || slot >= filters.size())
                return stack;

            if (!simulate)
            {
                ItemStack cp = stack.copy();
                cp.setCount(1);
                filters.set(slot, cp);
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 1;
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack)
        {
            stack = stack.copy();
            stack.setCount(1);

            filters.set(index, stack);

            markDirty();
        }
    }
}
