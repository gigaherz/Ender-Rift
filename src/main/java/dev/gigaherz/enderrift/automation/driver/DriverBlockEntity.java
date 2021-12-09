package dev.gigaherz.enderrift.automation.driver;

import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class DriverBlockEntity extends AggregatorBlockEntity
{
    @ObjectHolder("enderrift:driver")
    public static BlockEntityType<DriverBlockEntity> TYPE;

    public static final int POWER_LIMIT = 100000;

    final EnergyStorage energyBuffer = new EnergyStorage(POWER_LIMIT);
    final LazyOptional<IEnergyStorage> energyBufferGetter = LazyOptional.of(() -> energyBuffer);

    public DriverBlockEntity(BlockPos pos, BlockState state)
    {
        super(TYPE, pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityEnergy.ENERGY)
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