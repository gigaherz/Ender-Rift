package dev.gigaherz.enderrift.automation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.gigaherz.enderrift.common.AutomationEnergyWrapper;
import dev.gigaherz.enderrift.common.IPoweredAutomation;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import dev.gigaherz.graph3.Mergeable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AggregatorBlockEntity extends BlockEntity
        implements GraphObject<Mergeable.Dummy>, IPoweredAutomation
{
    private Graph<Mergeable.Dummy> graph;
    private boolean firstUpdate = true;
    private final List<IItemHandler> connectedInventories = Lists.newArrayList();

    private final AutomationEnergyWrapper wrapper = new AutomationEnergyWrapper(this);

    private int temporaryLowOnPowerTicks = 0;

    public AggregatorBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    // =============================================================================================
    // Graph API bindings

    @Nullable
    @Override
    public Graph<Mergeable.Dummy> getGraph()
    {
        return graph;
    }

    @Override
    public void setGraph(Graph<Mergeable.Dummy> graph)
    {
        this.graph = graph;
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();

        /*if (!firstUpdate)
            init();*/
    }

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
    public void setRemoved()
    {
        super.setRemoved();

        Graph<Mergeable.Dummy> graph = this.getGraph();
        if (graph != null)
            graph.remove(this);
    }

    void updateNeighbours()
    {
        Graph<Mergeable.Dummy> graph = this.getGraph();
        if (graph != null)
        {
            graph.addDirectedEdges(this, getNeighbours());
        }

        updateConnectedInventories();
    }

    private List<GraphObject<Mergeable.Dummy>> getNeighbours()
    {
        List<GraphObject<Mergeable.Dummy>> neighbours = Lists.newArrayList();
        for (Direction f : Direction.values())
        {
            BlockEntity teOther = level.getBlockEntity(worldPosition.relative(f));
            if (!(teOther instanceof AggregatorBlockEntity))
                continue;
            GraphObject<Mergeable.Dummy> thingOther = ((AggregatorBlockEntity) teOther);
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

        for (GraphObject<Mergeable.Dummy> object : getGraph().getObjects())
        {
            if (!(object instanceof AggregatorBlockEntity other))
                continue;

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

            BlockEntity teOther = level.getBlockEntity(worldPosition.relative(f));
            if (teOther == null)
                continue;

            if (teOther instanceof AggregatorBlockEntity)
                continue;

            if (AutomationHelper.isAutomatable(teOther, f.getOpposite()))
            {
                teOther.getCapability(Capabilities.ITEM_HANDLER, f.getOpposite())
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
        for (GraphObject<Mergeable.Dummy> object : getGraph().getObjects())
        {
            if (!(object instanceof AggregatorBlockEntity proxy))
                continue;

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

        for (GraphObject<Mergeable.Dummy> object : getGraph().getObjects())
        {
            if (!(object instanceof AggregatorBlockEntity proxy))
                continue;

            proxy.getInternalBuffer().ifPresent(energy::add);
        }

        return Optional.of(energy);
    }

    @Override
    public boolean isRemote()
    {
        return getLevel().isClientSide;
    }

    @Override
    public void setDirty()
    {
        setChanged();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
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