package dev.gigaherz.enderrift.automation.driver;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlock;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

public class DriverBlock extends AggregatorBlock<DriverBlockEntity>
{
    public static final MapCodec<DriverBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Properties.CODEC.fieldOf("properties").forGetter(DriverBlock::properties)).apply(inst, DriverBlock::new));
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

    public DriverBlock(Properties properties)
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
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new DriverBlockEntity(pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        return BaseEntityBlock.createTickerHelper(pBlockEntityType, EnderRiftMod.DRIVER_BLOCK_ENTITY.get(), DriverBlockEntity::tickStatic);
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

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource randomSource)
    {
        return getStateForConnections(state, level, currentPos);
    }

    private BlockState getStateForConnections(BlockState state, LevelReader world, BlockPos pos)
    {
        return state
                .setValue(NORTH, isConnectablePower(world, pos, Direction.NORTH))
                .setValue(SOUTH, isConnectablePower(world, pos, Direction.SOUTH))
                .setValue(WEST, isConnectablePower(world, pos, Direction.WEST))
                .setValue(EAST, isConnectablePower(world, pos, Direction.EAST))
                .setValue(UP, isConnectablePower(world, pos, Direction.UP))
                .setValue(DOWN, isConnectablePower(world, pos, Direction.DOWN));
    }

    @Override
    protected void recheckNeighbour(BlockGetter world, BlockPos pos, BlockPos neighbor)
    {
        // Do nothing, we don't connect with inventories.
    }

    private boolean isConnectablePower(BlockGetter worldIn, BlockPos pos, Direction facing)
    {
        BlockEntity te = worldIn.getBlockEntity(pos.relative(facing));

        return te instanceof AggregatorBlockEntity
                || (te != null && AutomationHelper.isPowerSource(te, facing.getOpposite()));
    }
}