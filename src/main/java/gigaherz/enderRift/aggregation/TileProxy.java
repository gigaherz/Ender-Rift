package gigaherz.enderRift.aggregation;

import net.minecraft.util.EnumFacing;

public class TileProxy extends TileAggregator
{
    @Override
    public void markDirty()
    {
        broadcastDirty();
        super.markDirty();
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return true;
    }
}
