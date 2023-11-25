package dev.gigaherz.enderrift.automation.driver;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class DriverBlockEntity extends AggregatorBlockEntity
{
    public static final int POWER_LIMIT = 100000;

    final EnergyStorage energyBuffer = new EnergyStorage(POWER_LIMIT);
    final LazyOptional<IEnergyStorage> energyBufferGetter = LazyOptional.of(() -> energyBuffer);

    public DriverBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.DRIVER_BLOCK_ENTITY.get(), pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == Capabilities.ENERGY)
            return energyBufferGetter.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    protected boolean canConnectSide(Direction side)
    {
        return false;
    }

    @Override
    public Optional<IEnergyStorage> getInternalBuffer()
    {
        return Optional.of(energyBuffer);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        energyBuffer.deserializeNBT(compound.get("storedEnergy"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.put("storedEnergy", energyBuffer.serializeNBT());
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, DriverBlockEntity te)
    {
        te.tick();
    }
}