package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.automation.TileAggregator;
import net.minecraft.util.EnumFacing;

public class TileProxy extends TileAggregator
{
    @Override
    public void markDirty()
    {
        markDirty(true);
    }

    @Override
    protected void markDirty(boolean sendBroadcast)
    {
        if (sendBroadcast)
        {
            broadcastDirty();
        }
        super.markDirty();
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return true;
    }
}
