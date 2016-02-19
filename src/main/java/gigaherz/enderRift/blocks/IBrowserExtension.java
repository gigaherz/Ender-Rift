package gigaherz.enderRift.blocks;

import gigaherz.api.automation.IInventoryAutomation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Set;

public interface IBrowserExtension
{
    void markDirty(Set<BlockPos> scanned, int distance);

    void gatherNeighbours(List<IInventoryAutomation> seen, Set<BlockPos> scanned, EnumFacing faceFrom, int distance);
}
