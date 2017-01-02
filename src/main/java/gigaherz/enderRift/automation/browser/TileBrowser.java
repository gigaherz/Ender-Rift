package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.automation.iface.BlockInterface;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class TileBrowser extends TileAggregator
{
    private int changeCount = 1;

    EnumFacing facing = null;

    public EnumFacing getFacing()
    {
        if (facing == null && world != null)
        {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == EnderRiftMod.browser)
            {
                facing = state.getValue(BlockInterface.FACING).getOpposite();
            }
        }
        return facing;
    }

    @Override
    public void markDirty()
    {
        changeCount++;
        facing = null;
        super.markDirty();
    }

    @Override
    protected void lazyDirty()
    {
        changeCount++;
    }

    public int getChangeCount()
    {
        return changeCount;
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return side == getFacing().getOpposite();
    }
}
