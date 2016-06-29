package gigaherz.enderRift.aggregation;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gigaherz.enderRift.automation.AutomationAggregator;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.IInventoryAutomation;
import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class TileAggregator extends TileEntity implements ITickable, GraphObject
{
    private Graph graph;
    private boolean firstUpdate = true;

    @Override
    public Graph getGraph()
    {
        return graph;
    }

    @Override
    public void setGraph(Graph graph)
    {
        this.graph = graph;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void validate()
    {
        super.validate();

        if (!firstUpdate)
            init();
    }

    @Override
    public void update()
    {
        if (firstUpdate)
        {
            firstUpdate = false;
            init();
        }
    }

    private void init()
    {
        Graph.integrate(this, getNeighbours());
    }

    @Override
    public void invalidate()
    {
        super.invalidate();

        Graph graph = this.getGraph();
        if (graph != null)
            graph.remove(this);
    }

    private List<GraphObject> getNeighbours()
    {
        List<GraphObject> neighbours = Lists.newArrayList();
        for (EnumFacing f : EnumFacing.VALUES)
        {
            TileEntity teOther = worldObj.getTileEntity(pos.offset(f));
            if (!(teOther instanceof TileAggregator))
                continue;
            GraphObject thingOther = ((TileAggregator) teOther);
            if (thingOther.getGraph() != null)
                neighbours.add(thingOther);
        }
        return neighbours;
    }

    public void updateNeighbours()
    {
        Graph graph = this.getGraph();
        if (graph != null)
        {
            graph.addNeighours(this, getNeighbours());
        }
    }

    protected IInventoryAutomation getAutomation(Block selfBlock)
    {
        AutomationAggregator aggregator = new AutomationAggregator();

        List<IInventoryAutomation> seen = Lists.newArrayList();
        Set<BlockPos> scanned = Sets.newHashSet();
        Queue<Triple<BlockPos, EnumFacing, Integer>> pending = Queues.newArrayDeque();

        IBlockState state = worldObj.getBlockState(getPos());
        if (state.getBlock() != selfBlock)
            return aggregator;

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
                if (te instanceof TileAggregator)
                {
                    ((TileAggregator) te).gatherNeighbours(pending, facing.getOpposite(), distance + 1);
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

    public abstract void markDirty(Set<BlockPos> scanned, int distance, Queue<Pair<BlockPos, Integer>> pending);

    public abstract void gatherNeighbours(Queue<Triple<BlockPos, EnumFacing, Integer>> pending, EnumFacing faceFrom, int distance);
}
