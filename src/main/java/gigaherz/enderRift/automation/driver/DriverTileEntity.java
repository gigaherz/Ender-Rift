package gigaherz.enderRift.automation.driver;

import gigaherz.enderRift.automation.AggregatorTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class DriverTileEntity extends AggregatorTileEntity
{
    @ObjectHolder("enderrift:driver")
    public static TileEntityType<?> TYPE;

    public static final int POWER_LIMIT = 100000;

    final EnergyStorage energyBuffer = new EnergyStorage(POWER_LIMIT);
    final LazyOptional<IEnergyStorage> energyBufferGetter = LazyOptional.of(() -> energyBuffer);

    public DriverTileEntity()
    {
        super(TYPE);
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
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);

        CapabilityEnergy.ENERGY.readNBT(energyBuffer, null, compound.get("storedEnergy"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.put("storedEnergy", CapabilityEnergy.ENERGY.writeNBT(energyBuffer, null));

        return compound;
    }
}
