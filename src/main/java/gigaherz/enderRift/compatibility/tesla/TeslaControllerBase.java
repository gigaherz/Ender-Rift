package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class TeslaControllerBase
{
    public static TeslaControllerBase CONSUMER = new TeslaControllerBase();
    public static TeslaControllerBase PRODUCER = new TeslaControllerBase();
    public static TeslaControllerBase HOLDER = new TeslaControllerBase();

    public Capability getCapability() {return null; }
    public Object createInstance(IEnergyHandler handler) { return null; }
}
