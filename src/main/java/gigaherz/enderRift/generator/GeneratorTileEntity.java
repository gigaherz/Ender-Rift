package gigaherz.enderRift.generator;

import gigaherz.enderRift.common.EnergyBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class GeneratorTileEntity extends TileEntity implements ITickableTileEntity
{
    @ObjectHolder("enderrift:generator")
    public static TileEntityType<?> TYPE;

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
            markDirty();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (getBurnTime(stack) <= 0)
                return stack;

            int[] x = {1, 2, 3, 4};

            return super.insertItem(slot, stack, simulate);
        }
    };
    public LazyOptional<IItemHandler> fuelSlotProvider = LazyOptional.of(() -> fuelSlot);

    public int heatLevel;
    public int burnTimeRemaining;
    public int currentItemBurnTime;
    public int timeInterval;

    public GeneratorTileEntity()
    {
        super(TYPE);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityEnergy.ENERGY)
            return energyBufferGetter.cast();
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return fuelSlotProvider.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void tick()
    {
        if (world.isRemote)
            return;

        boolean anyChanged;

        anyChanged = updateGeneration();
        anyChanged |= transferPower();

        if (anyChanged)
            this.markDirty();
    }

    private boolean updateGeneration()
    {
        boolean anyChanged = false;

        int minHeatLevel = 0;
        int heatInterval = HEAT_INTERVAL;
        if (world.getBlockState(pos.down()).getBlock() == Blocks.LAVA)
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
                    fuelSlot.setStackInSlot(0, stack.getItem().getContainerItem(stack));
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
                TileEntity e = world.getTileEntity(pos.offset(neighbor));
                Direction from = neighbor.getOpposite();

                if (e == null)
                    continue;

                IEnergyStorage handler = null;
                LazyOptional<IEnergyStorage> opt = e.getCapability(CapabilityEnergy.ENERGY, from);
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
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);

        heatLevel = compound.getInt("heatLevel");
        burnTimeRemaining = compound.getInt("burnTimeRemaining");
        currentItemBurnTime = compound.getInt("currentItemBurnTime");
        timeInterval = compound.getInt("timeInterval");
        CapabilityEnergy.ENERGY.readNBT(energyBuffer, null, compound.get("storedEnergy"));
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(fuelSlot, null, compound.get("fuelSlot"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.putInt("heatLevel", heatLevel);
        compound.putInt("burnTimeRemaining", burnTimeRemaining);
        compound.putInt("currentItemBurnTime", currentItemBurnTime);
        compound.putInt("timeInterval", timeInterval);
        compound.put("storedEnergy", CapabilityEnergy.ENERGY.writeNBT(energyBuffer, null));
        compound.put("fuelSlot", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(fuelSlot, null));

        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        super.read(state, tag);
    }

    public boolean isUseableByPlayer(PlayerEntity player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public int[] getFields()
    {
        return new int[]{burnTimeRemaining, currentItemBurnTime, energyBuffer.getEnergyStored(), heatLevel};
    }

    public void setFields(int[] values)
    {
        burnTimeRemaining = values[0];
        currentItemBurnTime = values[1];
        energyBuffer.setEnergy(values[2]);
        heatLevel = values[3];
        //this.markDirty();
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

    public int getContainedEnergy()
    {
        return energyBuffer.getEnergyStored();
    }

    public IItemHandler inventory()
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

    public int getBurnTime(ItemStack itemStack)
    {
        return ForgeHooks.getBurnTime(itemStack);
    }
}
