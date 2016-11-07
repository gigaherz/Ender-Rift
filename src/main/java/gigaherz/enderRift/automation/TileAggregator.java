package gigaherz.enderRift.automation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.enderRift.automation.capability.AutomationAggregator;
import gigaherz.enderRift.automation.capability.AutomationHelper;
import gigaherz.enderRift.automation.driver.TileDriver;
import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public abstract class TileAggregator extends TileEntity implements ITickable, GraphObject
{
    private Graph graph;
    private boolean firstUpdate = true;
    private final List<IItemHandler> connectedInventories = Lists.newArrayList();

    // =============================================================================================
    // Graph API bindings

    @Nullable
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
        updateConnectedInventories();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();

        Graph graph = this.getGraph();
        if (graph != null)
            graph.remove(this);
    }

    void updateNeighbours()
    {
        Graph graph = this.getGraph();
        if (graph != null)
        {
            graph.addNeighours(this, getNeighbours());
        }

        updateConnectedInventories();
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

    // ==================================================================================================

    public void broadcastDirty()
    {
        if (getGraph() == null)
            return;

        for (GraphObject object : getGraph().getObjects())
        {
            if (object == this)
                continue;

            if (!(object instanceof TileAggregator))
                continue;

            TileAggregator other = (TileAggregator) object;

            if (!other.isInvalid())
                other.markDirty(false);
        }
    }

    protected abstract void markDirty(boolean sendBroadcast);

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    void updateConnectedInventories()
    {
        connectedInventories.clear();
        for (EnumFacing f : EnumFacing.VALUES)
        {
            if (!canConnectSide(f))
                continue;

            TileEntity teOther = worldObj.getTileEntity(pos.offset(f));
            if (teOther == null)
                continue;

            if (teOther instanceof TileAggregator)
                continue;

            if (AutomationHelper.isAutomatable(teOther, f.getOpposite()))
            {
                if (teOther.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite()))
                {
                    connectedInventories.add(teOther.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite()));
                }
            }
        }
    }

    protected abstract boolean canConnectSide(EnumFacing side);

    protected IItemHandler getAutomation(Block selfBlock)
    {
        AutomationAggregator aggregator = new AutomationAggregator();

        IBlockState state = worldObj.getBlockState(getPos());
        if (state.getBlock() != selfBlock)
            return aggregator;

        if (getGraph() == null)
            return aggregator;

        Set<IItemHandler> inventories = Sets.newHashSet();
        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof TileAggregator))
                continue;
            TileAggregator proxy = (TileAggregator) object;

            inventories.addAll(proxy.connectedInventories);
        }

        aggregator.addAll(inventories);

        return aggregator;
    }

    protected IEnergyStorage getCombinedPowerBuffer()
    {
        EnergyAggregator energy = new EnergyAggregator();

        if (getGraph() == null)
            return energy;

        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof TileDriver))
                continue;
            TileDriver proxy = (TileDriver) object;

            energy.add(proxy.getEnergyBuffer());
        }

        return energy;
    }

}
