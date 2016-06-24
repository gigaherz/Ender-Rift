package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaHolder;

public class TeslaEnergyHolder implements ITeslaHolder
{
    final IEnergyHandler handler;

    public TeslaEnergyHolder(IEnergyHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public long getStoredPower()
    {
        return handler.getEnergy();
    }

    @Override
    public long getCapacity()
    {
        return handler.getCapacity();
    }
}
