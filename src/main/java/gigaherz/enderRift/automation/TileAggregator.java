package gigaherz.enderRift.automation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.enderRift.common.AutomationEnergyWrapper;
import gigaherz.enderRift.common.IPoweredAutomation;
import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
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

public abstract class TileAggregator extends TileEntity implements ITickable, GraphObject, IPoweredAutomation
{
    private Graph graph;
    private boolean firstUpdate = true;
    private final List<IItemHandler> connectedInventories = Lists.newArrayList();

    private AutomationEnergyWrapper wrapper = new AutomationEnergyWrapper(this);

    private int temporaryLowOnPowerTicks = 0;

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

        if (temporaryLowOnPowerTicks > 0)
            temporaryLowOnPowerTicks--;
    }

    private void init()
    {
        Graph.integrate(this, getNeighbours());
        updateConnectedInventories();
        //if (graph.getContextData() == null)
        //    graph.setContextData(new InventoryNetwork());
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
            TileEntity teOther = world.getTileEntity(pos.offset(f));
            if (!(teOther instanceof TileAggregator))
                continue;
            GraphObject thingOther = ((TileAggregator) teOther);
            if (thingOther.getGraph() != null)
                neighbours.add(thingOther);
        }
        return neighbours;
    }

    // ==================================================================================================

    public void lazyNotifyDirty()
    {
        if (getGraph() == null)
            return;

        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof TileAggregator))
                continue;

            TileAggregator other = (TileAggregator) object;

            if (!other.isInvalid())
                other.lazyDirty();
        }
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return getCombinedInventoryInternal();
    }

    @Override
    public IEnergyStorage getEnergyBuffer()
    {
        return getCombinedPowerBuffer();
    }

    protected abstract void lazyDirty();

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    protected void updateConnectedInventories()
    {
        connectedInventories.clear();

        for (EnumFacing f : EnumFacing.VALUES)
        {
            if (!canConnectSide(f))
                continue;

            TileEntity teOther = world.getTileEntity(pos.offset(f));
            if (teOther == null)
                continue;

            if (teOther instanceof TileAggregator)
                continue;

            if (AutomationHelper.isAutomatable(teOther, f.getOpposite()))
            {
                if (teOther.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite()))
                {
                    this.connectedInventories.add(teOther.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite()));
                }
            }
        }

        lazyNotifyDirty();
    }

    protected abstract boolean canConnectSide(EnumFacing side);

    public IItemHandler getCombinedInventory()
    {
        return wrapper;
    }

    @Nullable
    public IEnergyStorage getInternalBuffer()
    {
        return null;
    }

    private IItemHandler getCombinedInventoryInternal()
    {
        InventoryAggregator aggregator = new InventoryAggregator();

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

    private IEnergyStorage getCombinedPowerBuffer()
    {
        EnergyAggregator energy = new EnergyAggregator();

        if (getGraph() == null)
            return energy;

        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof TileAggregator))
                continue;

            TileAggregator proxy = (TileAggregator) object;

            IEnergyStorage internalBuffer = proxy.getInternalBuffer();
            if (internalBuffer != null)
                energy.add(internalBuffer);
        }

        return energy;
    }

    @Override
    public boolean isRemote()
    {
        return getWorld().isRemote;
    }

    @Override
    public void setDirty()
    {
        markDirty();
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
    }

    @Override
    public void setLowOnPowerTemporary()
    {
        temporaryLowOnPowerTicks = 60;
    }

    public boolean isLowOnPower()
    {
        return temporaryLowOnPowerTicks > 0 || wrapper.isLowOnPower();
    }
}
