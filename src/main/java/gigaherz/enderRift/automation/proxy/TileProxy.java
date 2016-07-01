package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.automation.TileAggregator;
import net.minecraft.util.EnumFacing;

public class TileProxy extends TileAggregator
{
    boolean broadcasting = false;

    @Override
    public void markDirty()
    {
        if (!broadcasting)
        {
            broadcasting = true;
            broadcastDirty();
            broadcasting = false;
        }
        super.markDirty();
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return true;
    }
}
