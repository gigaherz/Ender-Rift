package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaProducer;

public class TeslaEnergyProducer implements ITeslaProducer
{
    final IEnergyHandler handler;

    public TeslaEnergyProducer(IEnergyHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public long takePower(long power, boolean simulated)
    {
        return handler.extractEnergy(power > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) power, simulated);
    }
}
