package gigaherz.enderRift.blocks;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Queue;
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
        Queue<Pair<BlockPos, Integer>> pending = Queues.newArrayDeque();

        pending.add(Pair.of(this.pos, 0));

        while (pending.size() > 0)
        {
            Pair<BlockPos, Integer> pair = pending.remove();
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

            TileEntity te = worldObj.getTileEntity(pos2);
            if (te instanceof IBrowserExtension)
            {
                ((IBrowserExtension) te).markDirty(scanned, distance + 1, pending);
            }
        }
    }

    @Override
    public void gatherNeighbours(Queue<Triple<BlockPos, EnumFacing, Integer>> pending, EnumFacing faceFrom, int distance)
    {
        if (worldObj == null)
            return;

        for (EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos pos2 = pos.offset(facing);

            pending.add(Triple.of(pos2, facing, distance));
        }
    }

    @Override
    public void markDirty(Set<BlockPos> scanned, int distance, Queue<Pair<BlockPos, Integer>> pending)
    {
        if (worldObj == null)
            return;

        for (EnumFacing facing : EnumFacing.VALUES)
        {
            BlockPos pos2 = pos.offset(facing);
            if (!scanned.contains(pos2))
            {
                if (distance < MAX_SCAN_DISTANCE)
                {
                    pending.add(Pair.of(pos2, distance));
                }
            }
        }
    }
}
