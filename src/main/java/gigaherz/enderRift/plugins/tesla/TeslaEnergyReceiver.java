package gigaherz.enderRift.plugins.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraftforge.energy.IEnergyStorage;

public class TeslaEnergyReceiver implements ITeslaConsumer
{
    final IEnergyStorage handler;

    public TeslaEnergyReceiver(IEnergyStorage handler)
    {
        this.handler = handler;
    }

    @Override
    public long givePower(long power, boolean simulated)
    {
        return handler.receiveEnergy(power > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) power, simulated);
    }
}
