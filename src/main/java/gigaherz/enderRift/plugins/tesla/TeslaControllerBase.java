package gigaherz.enderRift.plugins.tesla;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;

public class TeslaControllerBase
{
    public static TeslaControllerBase CONSUMER = new TeslaControllerBase();
    public static TeslaControllerBase PRODUCER = new TeslaControllerBase();
    public static TeslaControllerBase HOLDER = new TeslaControllerBase();

    public Capability getCapability()
    {
        return null;
    }

    public Object createInstance(IEnergyStorage handler)
    {
        return null;
    }

    public IEnergyStorage wrapReverse(TileEntity e, EnumFacing from)
    {
        return null;
    }
}
