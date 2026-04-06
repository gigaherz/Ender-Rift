package dev.gigaherz.enderrift.automation.iface;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import dev.gigaherz.enderrift.common.IPoweredAutomation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
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
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class InterfaceBlockEntity extends AggregatorBlockEntity implements IPoweredAutomation
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                EnderRiftMod.INTERFACE_BLOCK_ENTITY.get(),
                (be, context) -> context == be.getFacing() ? VanillaContainerWrapper.of(be.inventoryOutputs()) : null
        );
    }

    private static final int FilterCount = 9;

    private final Container filters = new FilterInventory();
    private final Container outputs = new OutputsContainer();

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

    public Container inventoryOutputs()
    {
        return outputs;
    }

    public Container inventoryFilter()
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

        if (level.isClientSide())
            return;

        if (getCombinedInventory() == null)
            return;

        boolean anyChanged = false;

        for (int i = 0; i < FilterCount; i++)
        {
            ItemStack inFilter = filters.getItem(i);
            ItemStack inSlot = outputs.getItem(i);
            if (inFilter.getCount() > 0)
            {
                if (inSlot.getCount() <= 0)
                {
                    int free = 64;
                    inSlot = AutomationHelper.extractItems(getCombinedInventory(), inFilter, free, false);
                    outputs.setItem(i, inSlot);
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
                    outputs.setItem(i, inSlot);
                    if (stackSize != inSlot.getCount())
                        anyChanged = true;
                }
            }
            else if (inSlot.getCount() > 0)
            {
                int stackSize = inSlot.getCount();
                inSlot = AutomationHelper.insertItems(getCombinedInventory(), inSlot);
                outputs.setItem(i, inSlot);
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
            if (j >= 0 && j < filters.getContainerSize())
            {
                filters.setItem(j, entry.read(ItemStack.MAP_CODEC).orElseThrow());
            }
        }

        var _outputs = input.childrenListOrEmpty("Outputs");
        for(var entry : _outputs)
        {
            var slot = entry.getInt("Slot");
            if (slot.isEmpty()) continue;
            int j = slot.get();
            if (j >= 0 && j < outputs.getContainerSize())
            {
                outputs.setItem(j, entry.read(ItemStack.MAP_CODEC).orElseThrow());
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        var _filters = output.childrenList("Filters");
        for (int i = 0; i < filters.getContainerSize(); ++i)
        {
            var entry = _filters.addChild();;
            ItemStack stack = filters.getItem(i);
            if (stack.getCount() > 0)
            {
                entry.putInt("Slot", i);
                entry.store(ItemStack.MAP_CODEC, stack);
            }
        }


        var _outputs = output.childrenList("Outputs");
        for (int i = 0; i < outputs.getContainerSize(); ++i)
        {
            var entry = _outputs.addChild();;
            ItemStack stack = outputs.getItem(i);
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
            var inventory = this.inventoryOutputs();
            dropItems(level, pos, inventory);
        }
    }

    public static void dropItems(Level level, BlockPos pos, Container inventory)
    {
        for (int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack itemstack = inventory.getItem(i);
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack);
        }
    }

    private class FilterInventory extends SimpleContainer
    {
        public FilterInventory()
        {
            super(FilterCount);
        }

        @Override
        public void setItem(int index, ItemStack stack)
        {
            if (stack.getCount() > 1)
                super.setItem(index, stack.copyWithCount(1));
            else
                super.setItem(index, stack);
        }

        @Override
        public void setChanged()
        {
            InterfaceBlockEntity.this.setChanged();
        }
    }

    private class OutputsContainer extends SimpleContainer
    {
        public OutputsContainer()
        {
            super(InterfaceBlockEntity.FilterCount);
        }

        @Override
        public void setChanged()
        {
            InterfaceBlockEntity.this.setChanged();
        }
    }
}