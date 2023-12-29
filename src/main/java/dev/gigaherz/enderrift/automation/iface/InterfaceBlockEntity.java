package dev.gigaherz.enderrift.automation.iface;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import dev.gigaherz.enderrift.common.IPoweredAutomation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = EnderRiftMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class InterfaceBlockEntity extends AggregatorBlockEntity implements IPoweredAutomation
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                EnderRiftMod.INTERFACE_BLOCK_ENTITY.get(),
                (be, context) -> context == be.getFacing() ? be.inventoryOutputs() : null
        );
    }

    private static final int FilterCount = 9;

    private FilterInventory filters = new FilterInventory(FilterCount);
    private ItemStackHandler outputs = new ItemStackHandler(FilterCount)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }
    };

    private Direction facing = null;

    public InterfaceBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.INTERFACE_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public Direction getFacing()
    {
        if (facing == null && level != null)
        {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getProperties().contains(InterfaceBlock.FACING))
            {
                facing = state.getValue(InterfaceBlock.FACING).getOpposite();
            }
        }
        return facing;
    }

    public IItemHandlerModifiable inventoryOutputs()
    {
        return outputs;
    }

    public IItemHandlerModifiable inventoryFilter()
    {
        return filters;
    }

    @Override
    public void setChanged()
    {
        facing = null;
        super.setChanged();
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

        if (level.isClientSide)
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
            setChanged();
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        ListTag _filters = compound.getList("Filters", Tag.TAG_COMPOUND);
        for (int i = 0; i < _filters.size(); ++i)
        {
            CompoundTag nbttagcompound = _filters.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < filters.getSlots())
            {
                filters.setStackInSlot(j, ItemStack.of(nbttagcompound));
            }
        }

        ListTag _outputs = compound.getList("Outputs", Tag.TAG_COMPOUND);
        for (int i = 0; i < _outputs.size(); ++i)
        {
            CompoundTag slot = _outputs.getCompound(i);
            int j = slot.getByte("Slot") & 255;

            if (j >= 0 && j < outputs.getSlots())
            {
                outputs.setStackInSlot(j, ItemStack.of(slot));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        ListTag _filters = new ListTag();
        for (int i = 0; i < filters.getSlots(); ++i)
        {
            ItemStack stack = filters.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                CompoundTag nbttagcompound = new CompoundTag();
                nbttagcompound.putByte("Slot", (byte) i);
                stack.save(nbttagcompound);
                _filters.add(nbttagcompound);
            }
        }

        compound.put("Filters", _filters);

        ListTag _outputs = new ListTag();
        for (int i = 0; i < outputs.getSlots(); ++i)
        {
            ItemStack stack = outputs.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                CompoundTag slot = new CompoundTag();
                slot.putByte("Slot", (byte) i);
                stack.save(slot);
                _outputs.add(slot);
            }
        }

        compound.put("Outputs", _outputs);
    }

    public boolean isUseableByPlayer(Player player)
    {
        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, InterfaceBlockEntity te)
    {
        te.tick();
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

            setChanged();
        }
    }
}