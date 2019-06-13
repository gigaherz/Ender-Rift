package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.AggregatorTileEntity;
import gigaherz.enderRift.common.IPoweredAutomation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InterfaceTileEntity extends AggregatorTileEntity implements IPoweredAutomation
{
    @ObjectHolder("enderrift:interface")
    public static TileEntityType<?> TYPE;

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
    public LazyOptional<IItemHandler> outputsProvider = LazyOptional.of(() -> outputs);

    private Direction facing = null;

    public InterfaceTileEntity()
    {
        super(TYPE);
    }

    @Nullable
    public Direction getFacing()
    {
        if (facing == null && world != null)
        {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == EnderRiftMod.Blocks.INTERFACE)
            {
                facing = state.get(InterfaceBlock.FACING).getOpposite();
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (facing == getFacing())
        {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                return outputsProvider.cast();
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
    protected boolean canConnectSide(Direction side)
    {
        return side == getFacing().getOpposite();
    }

    @Override
    public void tick()
    {
        super.tick();

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
                else if (ItemHandlerHelper.canItemStacksStack(inSlot, inFilter))
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
    public void read(CompoundNBT compound)
    {
        super.read(compound);

        ListNBT _filters = compound.getList("Filters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _filters.size(); ++i)
        {
            CompoundNBT nbttagcompound = _filters.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < filters.getSlots())
            {
                filters.setStackInSlot(j, ItemStack.read(nbttagcompound));
            }
        }

        ListNBT _outputs = compound.getList("Outputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.size(); ++i)
        {
            CompoundNBT slot = _outputs.getCompound(i);
            int j = slot.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.getSlots())
            {
                outputs.setStackInSlot(j, ItemStack.read(slot));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        ListNBT _filters = new ListNBT();
        for (int i = 0; i < filters.getSlots(); ++i)
        {
            ItemStack stack = filters.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                CompoundNBT nbttagcompound = new CompoundNBT();
                nbttagcompound.putByte("Slot", (byte) i);
                stack.write(nbttagcompound);
                _filters.add(nbttagcompound);
            }
        }

        compound.put("Filters", _filters);

        ListNBT _outputs = new ListNBT();
        for (int i = 0; i < outputs.getSlots(); ++i)
        {
            ItemStack stack = outputs.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                CompoundNBT slot = new CompoundNBT();
                slot.putByte("Slot", (byte) i);
                stack.write(slot);
                _outputs.add(slot);
            }
        }

        compound.put("Outputs", _outputs);

        return compound;
    }

    public boolean isUseableByPlayer(PlayerEntity player)
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
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            return true;
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
