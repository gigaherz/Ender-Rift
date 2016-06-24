package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaConsumer;

public class TeslaConsumerWrapper implements IEnergyHandler
{
    private final ITeslaConsumer consumer;

    public TeslaConsumerWrapper(ITeslaConsumer consumer)
    {
        this.consumer = consumer;
    }

    @Override
    public int getCapacity()
    {
        return 0;
    }

    @Override
    public int getEnergy()
    {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        return 0;
    }

    @Override
    public int insertEnergy(int maxReceive, boolean simulate)
    {
        return (int)consumer.givePower(maxReceive, simulate);
    }
}
