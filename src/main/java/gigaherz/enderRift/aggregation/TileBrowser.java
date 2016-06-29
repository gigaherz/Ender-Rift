package gigaherz.enderRift.aggregation;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationAggregator;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.IInventoryAutomation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Queue;
import java.util.Set;

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
    public void markDirty(Set<BlockPos> scanned, int distance, Queue<Pair<BlockPos, Integer>> pending)
    {
        changeCount++;
    }

    @Override
    public void gatherNeighbours(Queue<Triple<BlockPos, EnumFacing, Integer>> pending, EnumFacing faceFrom, int distance)
    {
        EnumFacing f = getFacing();
        if (f != null)
        {
            pending.add(Triple.of(this.pos.offset(f.getOpposite()), faceFrom, distance));
        }
    }
}
