package gigaherz.enderRift.automation.driver;

import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.common.EnergyBuffer;
import gigaherz.enderRift.plugins.tesla.TeslaControllerBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class TileDriver extends TileAggregator
{
    public static final int POWER_LIMIT = 100000;

    EnergyBuffer energyBuffer = new EnergyBuffer(POWER_LIMIT);

    private Capability teslaConsumerCap;
    private Object teslaConsumerInstance;

    private Capability teslaHolderCap;
    private Object teslaHolderInstance;

    public TileDriver()
    {
        teslaConsumerCap = TeslaControllerBase.CONSUMER.getCapability();
        teslaConsumerInstance = TeslaControllerBase.CONSUMER.createInstance(energyBuffer);
        teslaHolderCap = TeslaControllerBase.HOLDER.getCapability();
        teslaHolderInstance = TeslaControllerBase.HOLDER.createInstance(energyBuffer);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return true;
        if (teslaConsumerCap != null && capability == teslaConsumerCap)
            return true;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return (T) energyBuffer;
        if (teslaConsumerCap != null && capability == teslaConsumerCap)
            return (T) teslaConsumerInstance;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return (T) teslaHolderInstance;
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return false;
    }

    @Override
    public IEnergyStorage getInternalBuffer()
    {
        return energyBuffer;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        CapabilityEnergy.ENERGY.readNBT(energyBuffer, null, compound.getTag("storedEnergy"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("storedEnergy", CapabilityEnergy.ENERGY.writeNBT(energyBuffer, null));

        return compound;
    }
}
