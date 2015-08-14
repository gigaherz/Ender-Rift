package gigaherz.enderRift.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStructureInvisibleCorner
        extends BlockStructureInvisible
        implements ITileEntityProvider
{

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEnderRiftCorner();
    }
}
