package gigaherz.enderRift.automation;

import gigaherz.common.BlockRegistered;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockAggregator<T extends TileAggregator> extends BlockRegistered
{
    public BlockAggregator(String name, Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(name, blockMaterialIn, blockMapColorIn);
    }

    public BlockAggregator(String name, Material materialIn)
    {
        super(name, materialIn);
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public abstract T createTileEntity(World world, IBlockState state);

    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block otherBlock, BlockPos otherPos)
    {
        super.neighborChanged(state, worldIn, pos, otherBlock, otherPos);
        TileEntity teSelf = worldIn.getTileEntity(pos);
        if (!(teSelf instanceof TileAggregator))
            return;
        ((TileAggregator)teSelf).updateNeighbours();
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(world, pos, neighbor);

        recheckNeighbour(world, pos, neighbor);
    }

    protected void recheckNeighbour(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        EnumFacing side = null;
        if(neighbor.equals(pos.east())) side = EnumFacing.EAST;
        if(neighbor.equals(pos.west())) side = EnumFacing.WEST;
        if(neighbor.equals(pos.north())) side = EnumFacing.NORTH;
        if(neighbor.equals(pos.south())) side = EnumFacing.SOUTH;
        if(neighbor.equals(pos.up())) side = EnumFacing.UP;
        if(neighbor.equals(pos.down())) side = EnumFacing.DOWN;

        if (side != null && isAutomatable(world, pos, side))
        {
            TileEntity teSelf = world.getTileEntity(pos);
            if (!(teSelf instanceof TileAggregator))
                return;
            ((TileAggregator)teSelf).updateConnectedInventories();
        }
    }

    protected boolean isAutomatable(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te == null)
            return false;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    protected boolean isConnectableAutomation(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te == null)
            return false;

        if (te instanceof TileAggregator)
            return true;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

}
