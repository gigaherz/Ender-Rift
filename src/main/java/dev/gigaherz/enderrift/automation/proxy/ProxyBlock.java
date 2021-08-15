package dev.gigaherz.enderrift.automation.proxy;

import com.google.common.collect.ImmutableMap;
import dev.gigaherz.enderrift.automation.AggregatorBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.Map;

public class ProxyBlock extends AggregatorBlock<ProxyBlockEntity>
{
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape BOUNDS = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape BOUNDS_NORTH = Block.box(6, 6, 0, 10, 10, 10);
    private static final VoxelShape BOUNDS_SOUTH = Block.box(6, 6, 6, 10, 10, 16);
    private static final VoxelShape BOUNDS_EAST = Block.box(6, 6, 6, 16, 10, 10);
    private static final VoxelShape BOUNDS_WEST = Block.box(0, 6, 6, 10, 10, 10);
    private static final VoxelShape BOUNDS_UP = Block.box(6, 6, 6, 10, 16, 10);
    private static final VoxelShape BOUNDS_DOWN = Block.box(6, 0, 6, 10, 10, 10);

    private static final Map<BooleanProperty, VoxelShape> SIDES = ImmutableMap.<BooleanProperty, VoxelShape>builder()
            .put(NORTH, BOUNDS_NORTH)
            .put(SOUTH, BOUNDS_SOUTH)
            .put(WEST, BOUNDS_WEST)
            .put(EAST, BOUNDS_EAST)
            .put(UP, BOUNDS_UP)
            .put(DOWN, BOUNDS_DOWN)
            .build();

    public ProxyBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ProxyBlockEntity(pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_)
    {
        return BaseEntityBlock.createTickerHelper(p_153214_, ProxyBlockEntity.TYPE, ProxyBlockEntity::tickStatic);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx)
    {
        return Shapes.or(BOUNDS,
                SIDES.entrySet().stream()
                        .filter(kv -> state.getValue(kv.getKey()))
                        .map(Map.Entry::getValue)
                        .toArray(VoxelShape[]::new)
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return getStateForConnections(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return getStateForConnections(stateIn, worldIn, currentPos);
    }

    private BlockState getStateForConnections(BlockState state, LevelAccessor world, BlockPos pos)
    {
        return state
                .setValue(NORTH, isConnectableAutomation(world, pos, Direction.NORTH))
                .setValue(SOUTH, isConnectableAutomation(world, pos, Direction.SOUTH))
                .setValue(WEST, isConnectableAutomation(world, pos, Direction.WEST))
                .setValue(EAST, isConnectableAutomation(world, pos, Direction.EAST))
                .setValue(UP, isConnectableAutomation(world, pos, Direction.UP))
                .setValue(DOWN, isConnectableAutomation(world, pos, Direction.DOWN));
    }
}