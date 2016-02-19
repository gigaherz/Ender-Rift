package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gigaherz.api.automation.AutomationAggregator;
import gigaherz.api.automation.AutomationHelper;
import gigaherz.api.automation.IBrowsableInventory;
import gigaherz.api.automation.IInventoryAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Set;

public class TileBrowser extends TileEntity implements IBrowserExtension
{
    boolean needsRefresh = true;
    final AutomationAggregator aggregator = new AutomationAggregator();

    private int changeCount = 1;

    public IBrowsableInventory getBrowsable()
    {
        IInventoryAutomation automation = getAutomation();
        if (automation instanceof IBrowsableInventory)
        {
            return (IBrowsableInventory) automation;
        }
        return null;
    }

    public IInventoryAutomation getAutomation()
    {
        if (needsRefresh)
        {
            IBlockState state = worldObj.getBlockState(getPos());
            EnumFacing facing = state.getValue(BlockBrowser.FACING);
            TileEntity te = worldObj.getTileEntity(pos.offset(facing));
            if (te != null)
            {
                if (te instanceof IBrowserExtension)
                {
                    List<IInventoryAutomation> seen = Lists.newArrayList();
                    Set<BlockPos> browserSet = Sets.newHashSet();
                    browserSet.add(this.pos);

                    ((IBrowserExtension) te).gatherNeighbours(seen, browserSet, facing.getOpposite(), 1);

                    aggregator.addAll(seen);
                }
                else
                {
                    IInventoryAutomation automated = AutomationHelper.get(te, facing.getOpposite());
                    if (automated != null)
                        aggregator.add(automated);
                }
            }
            needsRefresh = false;
        }

        return aggregator;
    }

    @Override
    public void markDirty()
    {
        broadcastDirty();
        super.markDirty();
    }

    public void broadcastDirty()
    {
        Set<BlockPos> scanned = Sets.newHashSet();
        scanned.add(pos);
        this.markDirty(scanned, 0);
    }

    public int getChangeCount()
    {
        return changeCount;
    }

    @Override
    public void markDirty(Set<BlockPos> scanned, int distance)
    {
        changeCount++;
        needsRefresh = true;
        aggregator.clear();
    }

    @Override
    public void gatherNeighbours(List<IInventoryAutomation> seen, Set<BlockPos> scanned, EnumFacing faceFrom, int distance)
    {
        // Do nothing here, it's the proxies that matter
    }
}
