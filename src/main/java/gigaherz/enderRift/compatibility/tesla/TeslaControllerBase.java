package gigaherz.enderRift.compatibility.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TeslaControllerBase
{
    public static TeslaControllerBase CONSUMER = new TeslaControllerBase();
    public static TeslaControllerBase PRODUCER = new TeslaControllerBase();
    public static TeslaControllerBase HOLDER = new TeslaControllerBase();

    public Capability getCapability()
    {
        return null;
    }

    public Object createInstance(IEnergyHandler handler)
    {
        return null;
    }

    public IEnergyHandler wrapReverse(TileEntity e, EnumFacing from)
    {
        return null;
    }
}
