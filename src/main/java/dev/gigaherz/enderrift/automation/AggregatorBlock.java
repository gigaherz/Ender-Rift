package dev.gigaherz.enderrift.automation;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AggregatorBlock<T extends AggregatorBlockEntity> extends BaseEntityBlock
{
    protected AggregatorBlock(Properties properties)
    {
        super(properties);
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pPos, BlockState pState);

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType);

    @Deprecated
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos otherPos, boolean isMoving)
    {
        super.neighborChanged(state, world, pos, otherBlock, otherPos, isMoving);

        BlockEntity teSelf = world.getBlockEntity(pos);
        if (!(teSelf instanceof AggregatorBlockEntity))
            return;
        ((AggregatorBlockEntity) teSelf).updateNeighbours();
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(state, world, pos, neighbor);

        recheckNeighbour(world, pos, neighbor);
    }

    protected void recheckNeighbour(BlockGetter world, BlockPos pos, BlockPos neighbor)
    {
        Direction side = null;
        if (neighbor.equals(pos.east())) side = Direction.EAST;
        if (neighbor.equals(pos.west())) side = Direction.WEST;
        if (neighbor.equals(pos.north())) side = Direction.NORTH;
        if (neighbor.equals(pos.south())) side = Direction.SOUTH;
        if (neighbor.equals(pos.above())) side = Direction.UP;
        if (neighbor.equals(pos.below())) side = Direction.DOWN;

        if (side != null && isAutomatable(world, pos, side))
        {
            BlockEntity teSelf = world.getBlockEntity(pos);
            if (!(teSelf instanceof AggregatorBlockEntity))
                return;
            ((AggregatorBlockEntity) teSelf).updateConnectedInventories();
        }
    }

    protected boolean isAutomatable(BlockGetter worldIn, BlockPos pos, Direction facing)
    {
        var te = worldIn.getBlockEntity(pos.relative(facing));

        return te != null && AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    protected boolean isConnectableAutomation(BlockGetter worldIn, BlockPos pos, Direction facing)
    {
        var te = worldIn.getBlockEntity(pos.relative(facing));

        if (te == null)
            return false;

        if (te instanceof AggregatorBlockEntity)
            return true;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }
}