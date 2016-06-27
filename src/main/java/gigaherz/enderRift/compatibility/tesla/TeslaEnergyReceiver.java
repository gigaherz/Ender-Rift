package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaConsumer;

public class TeslaEnergyReceiver implements ITeslaConsumer
{
    final IEnergyHandler handler;

    public TeslaEnergyReceiver(IEnergyHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public long givePower(long power, boolean simulated)
    {
        return handler.insertEnergy(power > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)power, simulated);
    }
}
