package gigaherz.enderRift.automation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.enderRift.common.AutomationEnergyWrapper;
import gigaherz.enderRift.common.IPoweredAutomation;
import gigaherz.graph2.Graph;
import gigaherz.graph2.GraphObject;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AggregatorTileEntity extends TileEntity
        implements ITickableTileEntity, GraphObject, IPoweredAutomation
{
    private Graph graph;
    private boolean firstUpdate = true;
    private final List<IItemHandler> connectedInventories = Lists.newArrayList();

    private AutomationEnergyWrapper wrapper = new AutomationEnergyWrapper(this);

    private int temporaryLowOnPowerTicks = 0;

    public AggregatorTileEntity(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

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

        /*if (!firstUpdate)
            init();*/
    }

    @Override
    public void tick()
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
    public void remove()
    {
        super.remove();

        Graph graph = this.getGraph();
        if (graph != null)
            graph.remove(this);
    }

    void updateNeighbours()
    {
        Graph graph = this.getGraph();
        if (graph != null)
        {
            graph.addDirectedEdges(this, getNeighbours());
        }

        updateConnectedInventories();
    }

    private List<GraphObject> getNeighbours()
    {
        List<GraphObject> neighbours = Lists.newArrayList();
        for (Direction f : Direction.values())
        {
            TileEntity teOther = world.getTileEntity(pos.offset(f));
            if (!(teOther instanceof AggregatorTileEntity))
                continue;
            GraphObject thingOther = ((AggregatorTileEntity) teOther);
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
            if (!(object instanceof AggregatorTileEntity))
                continue;

            AggregatorTileEntity other = (AggregatorTileEntity) object;

            if (!other.isRemoved())
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
    public Optional<IEnergyStorage> getEnergyBuffer()
    {
        return getCombinedPowerBuffer();
    }

    protected abstract void lazyDirty();

    protected void updateConnectedInventories()
    {
        connectedInventories.clear();

        for (Direction f : Direction.values())
        {
            if (!canConnectSide(f))
                continue;

            TileEntity teOther = world.getTileEntity(pos.offset(f));
            if (teOther == null)
                continue;

            if (teOther instanceof AggregatorTileEntity)
                continue;

            if (AutomationHelper.isAutomatable(teOther, f.getOpposite()))
            {
                teOther.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())
                        .ifPresent(this.connectedInventories::add);
            }
        }

        lazyNotifyDirty();
    }

    protected abstract boolean canConnectSide(Direction side);

    public IItemHandler getCombinedInventory()
    {
        return wrapper;
    }

    public Optional<IEnergyStorage> getInternalBuffer()
    {
        return Optional.empty();
    }

    private IItemHandler getCombinedInventoryInternal()
    {
        InventoryAggregator aggregator = new InventoryAggregator();

        if (getGraph() == null)
            return aggregator;

        Set<IItemHandler> inventories = Sets.newHashSet();
        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof AggregatorTileEntity))
                continue;
            AggregatorTileEntity proxy = (AggregatorTileEntity) object;

            inventories.addAll(proxy.connectedInventories);
        }

        aggregator.addAll(inventories);

        return aggregator;
    }

    private Optional<IEnergyStorage> getCombinedPowerBuffer()
    {
        EnergyAggregator energy = new EnergyAggregator();

        if (getGraph() == null)
            return Optional.of(energy);

        for (GraphObject object : getGraph().getObjects())
        {
            if (!(object instanceof AggregatorTileEntity))
                continue;

            AggregatorTileEntity proxy = (AggregatorTileEntity) object;

            proxy.getInternalBuffer().ifPresent(energy::add);
        }

        return Optional.of(energy);
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
    public void handleUpdateTag(BlockState blockState, CompoundNBT tag)
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
