package dev.gigaherz.enderrift.automation.iface;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import dev.gigaherz.enderrift.common.IPoweredAutomation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
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
                else if (ItemStack.isSameItemSameComponents(inSlot, inFilter))
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
    public void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        var _filters = input.childrenListOrEmpty("Filters");

        for(var entry : _filters)
        {
            var slot = entry.getInt("Slot");
            if (slot.isEmpty()) continue;
            int j = slot.get();
            if (j >= 0 && j < filters.getSlots())
            {
                filters.setStackInSlot(j, entry.read(ItemStack.MAP_CODEC).orElseThrow());
            }
        }

        var _outputs = input.childrenListOrEmpty("Outputs");
        for(var entry : _outputs)
        {
            var slot = entry.getInt("Slot");
            if (slot.isEmpty()) continue;
            int j = slot.get();
            if (j >= 0 && j < outputs.getSlots())
            {
                outputs.setStackInSlot(j, entry.read(ItemStack.MAP_CODEC).orElseThrow());
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        var _filters = output.childrenList("Filters");
        for (int i = 0; i < filters.getSlots(); ++i)
        {
            var entry = _filters.addChild();;
            ItemStack stack = filters.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                entry.putInt("Slot", i);
                entry.store(ItemStack.MAP_CODEC, stack);
            }
        }


        var _outputs = output.childrenList("Outputs");
        for (int i = 0; i < outputs.getSlots(); ++i)
        {
            var entry = _outputs.addChild();;
            ItemStack stack = outputs.getStackInSlot(i);
            if (stack.getCount() > 0)
            {
                entry.putInt("Slot", i);
                entry.store(ItemStack.MAP_CODEC, stack);
            }
        }
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);

        if (level != null)
        {
            IItemHandler inventory = this.inventoryOutputs();
            dropItems(level, pos, inventory);
        }
    }

    public static void dropItems(Level level, BlockPos pos, IItemHandler inventory)
    {
        for (int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack);
        }
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
                var sample = stack.copyWithCount(1);
                filters.set(slot, sample);
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
            var sample = stack.copyWithCount(1);
            filters.set(index, sample);

            setChanged();
        }
    }
}