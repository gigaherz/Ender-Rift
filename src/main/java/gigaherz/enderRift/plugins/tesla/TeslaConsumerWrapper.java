package gigaherz.enderRift.plugins.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraftforge.energy.IEnergyStorage;

public class TeslaConsumerWrapper implements IEnergyStorage
{
    private final ITeslaConsumer consumer;

    public TeslaConsumerWrapper(ITeslaConsumer consumer)
    {
        this.consumer = consumer;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        return (int) consumer.givePower(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        return 0;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return 0;
    }

    @Override
    public boolean canExtract()
    {
        return false;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }
}
