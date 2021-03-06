package gigaherz.enderRift.automation.driver;

import com.google.common.collect.ImmutableMap;
import gigaherz.enderRift.automation.AggregatorBlock;
import gigaherz.enderRift.automation.AggregatorTileEntity;
import gigaherz.enderRift.automation.AutomationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Map;

import net.minecraft.block.AbstractBlock.Properties;

public class DriverBlock extends AggregatorBlock<DriverTileEntity>
{
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape BOUNDS = Block.makeCuboidShape(4, 4, 4, 12, 12, 12);
    private static final VoxelShape BOUNDS_NORTH = Block.makeCuboidShape(6, 6, 0, 10, 10, 10);
    private static final VoxelShape BOUNDS_SOUTH = Block.makeCuboidShape(6, 6, 6, 10, 10, 16);
    private static final VoxelShape BOUNDS_EAST = Block.makeCuboidShape(6, 6, 6, 16, 10, 10);
    private static final VoxelShape BOUNDS_WEST = Block.makeCuboidShape(0, 6, 6, 10, 10, 10);
    private static final VoxelShape BOUNDS_UP = Block.makeCuboidShape(6, 6, 6, 10, 16, 10);
    private static final VoxelShape BOUNDS_DOWN = Block.makeCuboidShape(6, 0, 6, 10, 10, 10);

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
        setDefaultState(getStateContainer().getBaseState()
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(EAST, false)
                .with(UP, false)
                .with(DOWN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new DriverTileEntity();
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx)
    {
        return VoxelShapes.or(BOUNDS,
                SIDES.entrySet().stream()
                        .filter(kv -> state.get(kv.getKey()))
                        .map(Map.Entry::getValue)
                        .toArray(VoxelShape[]::new)
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx)
    {
        return getStateForConnections(getDefaultState(), ctx.getWorld(), ctx.getPos());
    }

    @Deprecated
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        return getStateForConnections(stateIn, worldIn, currentPos);
    }

    private BlockState getStateForConnections(BlockState state, IWorld world, BlockPos pos)
    {
        return state
                .with(NORTH, isConnectableAutomation(world, pos, Direction.NORTH))
                .with(SOUTH, isConnectableAutomation(world, pos, Direction.SOUTH))
                .with(WEST, isConnectableAutomation(world, pos, Direction.WEST))
                .with(EAST, isConnectableAutomation(world, pos, Direction.EAST))
                .with(UP, isConnectableAutomation(world, pos, Direction.UP))
                .with(DOWN, isConnectableAutomation(world, pos, Direction.DOWN));
    }

    @Override
    protected void recheckNeighbour(IBlockReader world, BlockPos pos, BlockPos neighbor)
    {
        // Do nothing, we don't connect with inventories.
    }

    private boolean isConnectablePower(IBlockReader worldIn, BlockPos pos, Direction facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        return te instanceof AggregatorTileEntity
                || (te != null && AutomationHelper.isPowerSource(te, facing.getOpposite()));
    }
}