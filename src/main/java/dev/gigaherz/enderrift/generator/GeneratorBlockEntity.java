package dev.gigaherz.enderrift.generator;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.EnergyBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class GeneratorBlockEntity extends BlockEntity
{
    public static final int SLOT_COUNT = 1;
    public static final int POWER_LIMIT = 100000;
    public static final int MIN_HEAT = 100;
    public static final int MAX_HEAT = 1000;
    public static final int POWERGEN_MIN = 20;
    public static final int POWERGEN_MAX = 200;
    public static final int HEAT_INTERVAL = 20;
    public static final int POWER_TRANSFER_MAX = 800;

    private final EnergyBuffer energyBuffer = new EnergyBuffer(POWER_LIMIT);
    private final LazyOptional<EnergyBuffer> energyBufferGetter = LazyOptional.of(() -> energyBuffer);

    private final ItemStackHandler fuelSlot = new ItemStackHandler(SLOT_COUNT)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            setChanged();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (getBurnTime(stack) <= 0)
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    };
    public LazyOptional<IItemHandler> fuelSlotProvider = LazyOptional.of(() -> fuelSlot);

    public int heatLevel;
    public int burnTimeRemaining;
    public int currentItemBurnTime;
    public int timeInterval;

    public GeneratorBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
    {
        if (cap == Capabilities.ENERGY)
            return energyBufferGetter.cast();
        if (cap == Capabilities.ITEM_HANDLER)
            return fuelSlotProvider.cast();
        return super.getCapability(cap, side);
    }

    public void tick()
    {
        if (level.isClientSide)
            return;

        boolean anyChanged;

        anyChanged = updateGeneration();
        anyChanged |= transferPower();

        if (anyChanged)
            this.setChanged();
    }

    private boolean updateGeneration()
    {
        boolean anyChanged = false;

        int minHeatLevel = 0;
        int heatInterval = HEAT_INTERVAL;
        if (level.getBlockState(worldPosition.below()).getBlock() == Blocks.LAVA)
        {
            minHeatLevel = MIN_HEAT - 1;
            if (heatLevel < minHeatLevel)
                heatInterval = Math.max(1, heatInterval / 2);
        }

        if (timeInterval < HEAT_INTERVAL)
            timeInterval++;

        if (burnTimeRemaining > 0)
        {
            burnTimeRemaining -= Math.max(1, heatLevel / MIN_HEAT);
            if (burnTimeRemaining <= 0)
                timeInterval = 0;
            if (timeInterval >= heatInterval && heatLevel < MAX_HEAT)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }
        else if (heatLevel > minHeatLevel)
        {
            if (timeInterval >= HEAT_INTERVAL)
            {
                timeInterval = 0;
                heatLevel--;
                anyChanged = true;
            }
        }
        else if (minHeatLevel > 0 && heatLevel < minHeatLevel)
        {
            if (timeInterval >= HEAT_INTERVAL)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }

        if (heatLevel >= MIN_HEAT && energyBuffer.getEnergyStored() < POWER_LIMIT)
        {
            int powerGen = getGenerationPower();
            energyBuffer.setEnergy(Math.min(energyBuffer.getEnergyStored() + powerGen, POWER_LIMIT));
            anyChanged = true;
        }

        if (burnTimeRemaining <= 0 && energyBuffer.getEnergyStored() < POWER_LIMIT)
        {
            ItemStack stack = fuelSlot.getStackInSlot(0);
            if (stack.getCount() > 0)
            {
                currentItemBurnTime = burnTimeRemaining = getBurnTime(fuelSlot.getStackInSlot(0));
                timeInterval = 0;
                if (stack.getCount() == 1)
                    fuelSlot.setStackInSlot(0, stack.getItem().getCraftingRemainingItem(stack));
                else
                    stack.shrink(1);
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    private boolean transferPower()
    {
        boolean anyChanged = false;

        int sendPower = Math.min(POWER_TRANSFER_MAX, energyBuffer.getEnergyStored());
        if (sendPower > 0)
        {
            IEnergyStorage[] handlers = new IEnergyStorage[6];
            int[] wantedSide = new int[6];
            int accepted = 0;

            for (Direction neighbor : Direction.values())
            {
                BlockEntity e = level.getBlockEntity(worldPosition.relative(neighbor));
                Direction from = neighbor.getOpposite();

                if (e == null)
                    continue;

                IEnergyStorage handler = null;
                LazyOptional<IEnergyStorage> opt = e.getCapability(Capabilities.ENERGY, from);
                if (opt.isPresent())
                {
                    handler = opt.orElse(null);
                    if (!handler.canReceive())
                        handler = null;
                }

                if (handler != null)
                {
                    handlers[from.ordinal()] = handler;
                    int wanted = handler.receiveEnergy(sendPower, true);
                    wantedSide[from.ordinal()] = wanted;
                    accepted += wanted;
                }
            }

            if (accepted > 0)
            {
                for (Direction from : Direction.values())
                {
                    IEnergyStorage handler = handlers[from.ordinal()];
                    int wanted = wantedSide[from.ordinal()];
                    if (handler == null || wanted == 0)
                        continue;

                    int given = Math.min(Math.min(energyBuffer.getEnergyStored(), wanted), wanted * accepted / sendPower);
                    int received = Math.min(given, handler.receiveEnergy(given, false));
                    energyBuffer.setEnergy(energyBuffer.getEnergyStored() - received);
                    if (energyBuffer.getEnergyStored() <= 0)
                        break;
                }
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        heatLevel = compound.getInt("heatLevel");
        burnTimeRemaining = compound.getInt("burnTimeRemaining");
        currentItemBurnTime = compound.getInt("currentItemBurnTime");
        timeInterval = compound.getInt("timeInterval");
        energyBuffer.deserializeNBT(compound.get("storedEnergy"));
        fuelSlot.deserializeNBT(compound.getCompound("fuelSlot"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.putInt("heatLevel", heatLevel);
        compound.putInt("burnTimeRemaining", burnTimeRemaining);
        compound.putInt("currentItemBurnTime", currentItemBurnTime);
        compound.putInt("timeInterval", timeInterval);
        compound.put("storedEnergy", energyBuffer.serializeNBT());
        compound.put("fuelSlot", fuelSlot.serializeNBT());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        super.load(tag);
    }

    public boolean isUseableByPlayer(Player player)
    {
        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    private final ContainerData fields = new ContainerData()
    {
        @Override
        public int get(int field)
        {
            return switch (field)
                    {
                        case 0 -> burnTimeRemaining;
                        case 1 -> currentItemBurnTime;
                        case 2 -> energyBuffer.getEnergyStored() & 0xFFFF;
                        case 3 -> energyBuffer.getEnergyStored() >> 16;
                        case 4 -> heatLevel;
                        default -> 0;
                    };
        }

        @Override
        public void set(int field, int value)
        {
            switch (field)
            {
                case 0 -> burnTimeRemaining = value;
                case 1 -> currentItemBurnTime = value;
                case 2 -> energyBuffer.setEnergy((energyBuffer.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
                case 3 -> energyBuffer.setEnergy((energyBuffer.getEnergyStored() & 0xFFFF) | (value << 16));
                case 4 -> heatLevel = value;
            }
        }

        @Override
        public int getCount()
        {
            return 5;
        }
    };

    public ContainerData getFields()
    {
        return fields;
    }

    public boolean isBurning()
    {
        return burnTimeRemaining > 0;
    }

    public int getHeatValue()
    {
        return heatLevel;
    }

    public int getGenerationPower()
    {
        if (heatLevel < MIN_HEAT)
            return 0;
        return Math.max(0, Math.round(POWERGEN_MIN + (POWERGEN_MAX - POWERGEN_MIN) * (heatLevel - MIN_HEAT) / (float) (MAX_HEAT - MIN_HEAT)));
    }

    public static int calculateGenerationPower(int heatLevel)
    {
        if (heatLevel < MIN_HEAT)
            return 0;
        return Math.max(0, Math.round(POWERGEN_MIN + (POWERGEN_MAX - POWERGEN_MIN) * (heatLevel - MIN_HEAT) / (float) (MAX_HEAT - MIN_HEAT)));
    }

    public int getContainedEnergy()
    {
        return energyBuffer.getEnergyStored();
    }

    public IItemHandlerModifiable inventory()
    {
        return fuelSlot;
    }

    public int getCurrentItemBurnTime()
    {
        return currentItemBurnTime;
    }

    public int getBurnTimeRemaining()
    {
        return burnTimeRemaining;
    }

    public static int getBurnTime(ItemStack itemStack)
    {
        return CommonHooks.getBurnTime(itemStack, null);
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, GeneratorBlockEntity te)
    {
        te.tick();
    }
}