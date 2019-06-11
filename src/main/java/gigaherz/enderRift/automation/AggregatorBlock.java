package gigaherz.enderRift.automation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AggregatorBlock<T extends AggregatorTileEntity> extends Block
{
    protected AggregatorBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos otherPos, boolean p_220069_6_)
    {
        super.neighborChanged(state, world, pos, otherBlock, otherPos, p_220069_6_);

        TileEntity teSelf = world.getTileEntity(pos);
        if (!(teSelf instanceof AggregatorTileEntity))
            return;
        ((AggregatorTileEntity) teSelf).updateNeighbours();
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(state, world, pos, neighbor);

        recheckNeighbour(world, pos, neighbor);
    }

    protected void recheckNeighbour(IBlockReader world, BlockPos pos, BlockPos neighbor)
    {
        Direction side = null;
        if (neighbor.equals(pos.east())) side = Direction.EAST;
        if (neighbor.equals(pos.west())) side = Direction.WEST;
        if (neighbor.equals(pos.north())) side = Direction.NORTH;
        if (neighbor.equals(pos.south())) side = Direction.SOUTH;
        if (neighbor.equals(pos.up())) side = Direction.UP;
        if (neighbor.equals(pos.down())) side = Direction.DOWN;

        if (side != null && isAutomatable(world, pos, side))
        {
            TileEntity teSelf = world.getTileEntity(pos);
            if (!(teSelf instanceof AggregatorTileEntity))
                return;
            ((AggregatorTileEntity) teSelf).updateConnectedInventories();
        }
    }

    protected boolean isAutomatable(IBlockReader worldIn, BlockPos pos, Direction facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te == null)
            return false;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    protected boolean isConnectableAutomation(IBlockReader worldIn, BlockPos pos, Direction facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te == null)
            return false;

        if (te instanceof AggregatorTileEntity)
            return true;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }
}
