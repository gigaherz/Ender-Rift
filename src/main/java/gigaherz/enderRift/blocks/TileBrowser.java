package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gigaherz.enderRift.automation.AutomationAggregator;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.IInventoryAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TileBrowser extends TileEntity implements IBrowserExtension
{
    private int changeCount = 1;

    public IInventoryAutomation getAutomation()
    {
        AutomationAggregator aggregator = new AutomationAggregator();

        List<IInventoryAutomation> seen = Lists.newArrayList();
        Set<BlockPos> scanned = Sets.newHashSet();
        Queue<Triple<BlockPos, EnumFacing, Integer>> pending = Queues.newArrayDeque();

        IBlockState state = worldObj.getBlockState(getPos());
        EnumFacing facing = state.getValue(BlockInterface.FACING);
        pending.add(Triple.of(this.pos, facing, 0));

        while (pending.size() > 0)
        {
            Triple<BlockPos, EnumFacing, Integer> pair = pending.remove();
            BlockPos pos2 = pair.getLeft();

            if (scanned.contains(pos2))
            {
                continue;
            }

            scanned.add(pos2);

            int distance = pair.getRight();

            if (distance >= TileProxy.MAX_SCAN_DISTANCE)
            {
                continue;
            }

            facing = pair.getMiddle();

            TileEntity te = worldObj.getTileEntity(pos2);
            if (te != null)
            {
                if (te instanceof IBrowserExtension)
                {
                    ((IBrowserExtension) te).gatherNeighbours(pending, facing.getOpposite(), distance + 1);
                }
                else
                {
                    IInventoryAutomation automated = AutomationHelper.get(te, facing.getOpposite());
                    if (automated != null) seen.add(automated);
                }
            }
        }

        aggregator.addAll(seen);

        return aggregator;
    }

    @Override
    public void markDirty()
    {
        changeCount++;
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
        pending.add(Triple.of(this.pos.offset(faceFrom.getOpposite()), faceFrom, distance));
    }
}
