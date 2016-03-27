package gigaherz.enderRift.blocks;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Queue;
import java.util.Set;

public interface IBrowserExtension
{
    void markDirty(Set<BlockPos> scanned, int distance, Queue<Pair<BlockPos, Integer>> pending);

    void gatherNeighbours(Queue<Triple<BlockPos, EnumFacing, Integer>> pending, EnumFacing faceFrom, int distance);
}
