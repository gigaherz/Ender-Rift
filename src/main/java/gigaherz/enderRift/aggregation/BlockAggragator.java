package gigaherz.enderRift.aggregation;

import gigaherz.enderRift.blocks.BlockRegistered;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockAggragator<T extends TileAggregator> extends BlockRegistered
{
    public BlockAggragator(String name, Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(name, blockMaterialIn, blockMapColorIn);
    }

    public BlockAggragator(String name, Material materialIn)
    {
        super(name, materialIn);
    }

    @Override
    public abstract boolean hasTileEntity(IBlockState state);

    @Override
    public abstract T createTileEntity(World world, IBlockState state);

    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        super.neighborChanged(state, worldIn, pos, blockIn);
        TileAggregator teSelf = (TileAggregator) worldIn.getTileEntity(pos);
        teSelf.updateNeighbours();
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(world, pos, neighbor);
        TileAggregator teSelf = (TileAggregator) world.getTileEntity(pos);
        teSelf.updateNeighbours();
    }
}
