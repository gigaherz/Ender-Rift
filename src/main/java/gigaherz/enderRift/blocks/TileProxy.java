package gigaherz.enderRift.blocks;

import com.google.common.collect.Sets;
import gigaherz.api.automation.AutomationHelper;
import gigaherz.api.automation.IInventoryAutomation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Set;

public class TileProxy extends TileEntity implements IBrowserExtension
{
    public static final int MAX_SCAN_DISTANCE = 32;

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

    @Override
    public void gatherNeighbours(List<IInventoryAutomation> seen, Set<BlockPos> scanned, EnumFacing faceFrom, int distance)
    {
        for (EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos pos2 = pos.offset(facing);
            if (!scanned.contains(pos2))
            {
                scanned.add(pos2);
                TileEntity te = worldObj.getTileEntity(pos2);
                if (te != null)
                {
                    if (distance < MAX_SCAN_DISTANCE)
                    {
                        if (te instanceof IBrowserExtension)
                        {
                            ((IBrowserExtension) te).gatherNeighbours(seen, scanned, facing.getOpposite(), distance + 1);
                        }
                        else
                        {
                            IInventoryAutomation automated = AutomationHelper.get(te, facing.getOpposite());
                            if (automated != null) seen.add(automated);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void markDirty(Set<BlockPos> scanned, int distance)
    {
        if (worldObj == null)
            return;

        for (EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos pos2 = pos.offset(facing);
            if (!scanned.contains(pos2))
            {
                scanned.add(pos2);
                TileEntity te = worldObj.getTileEntity(pos2);
                if (te != null)
                {
                    if (te instanceof IBrowserExtension)
                    {
                        if (distance < MAX_SCAN_DISTANCE)
                        {
                            ((IBrowserExtension) te).markDirty(scanned, distance + 1);
                        }
                    }
                }
            }
        }
    }
}
