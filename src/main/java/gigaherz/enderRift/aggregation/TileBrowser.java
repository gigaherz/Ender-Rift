package gigaherz.enderRift.aggregation;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.IInventoryAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class TileBrowser extends TileAggregator
{
    private int changeCount = 1;

    EnumFacing facing = null;

    public EnumFacing getFacing()
    {
        if (facing == null && worldObj != null)
        {
            IBlockState state = worldObj.getBlockState(pos);
            if (state.getBlock() == EnderRiftMod.browser)
            {
                facing = state.getValue(BlockInterface.FACING).getOpposite();
            }
        }
        return facing;
    }

    public IInventoryAutomation getAutomation()
    {
        return super.getAutomation(EnderRiftMod.browser);
    }

    @Override
    public void markDirty()
    {
        changeCount++;
        facing = null;
        super.markDirty();
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
