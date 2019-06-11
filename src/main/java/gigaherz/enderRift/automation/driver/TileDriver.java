package gigaherz.enderRift.automation.driver;

import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.common.EnergyBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileDriver extends TileAggregator
{
    @ObjectHolder("enderrift:driver")
    public static TileEntityType<?> TYPE;

    public static final int POWER_LIMIT = 100000;

    final EnergyBuffer energyBuffer = new EnergyBuffer(POWER_LIMIT);
    final LazyOptional<EnergyBuffer> energyBufferGetter = LazyOptional.of(() -> energyBuffer);

    public TileDriver()
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
    public void read(CompoundNBT compound)
    {
        super.read(compound);

        CapabilityEnergy.ENERGY.readNBT(energyBuffer, null, compound.getCompound("storedEnergy"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.put("storedEnergy", CapabilityEnergy.ENERGY.writeNBT(energyBuffer, null));

        return compound;
    }
}
