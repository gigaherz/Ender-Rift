package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.automation.TileAggregator;
import net.minecraft.util.EnumFacing;

public class TileProxy extends TileAggregator
{
    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return true;
    }
}
