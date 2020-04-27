package gigaherz.enderRift.generator;

import gigaherz.enderRift.automation.iface.InterfaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class GeneratorBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE_BASE = Block.makeCuboidShape(0, 0, 0, 16, 4, 16);
    public static final VoxelShape SHAPE_CORE1_NS = Block.makeCuboidShape(2, 4, 3, 14, 16, 13);
    public static final VoxelShape SHAPE_CORE1_WE = Block.makeCuboidShape(3, 4, 2, 13, 16, 14);
    public static final VoxelShape SHAPE_CORE2_NS = Block.makeCuboidShape(4, 7, 0, 11, 13, 16);
    public static final VoxelShape SHAPE_CORE2_WE = Block.makeCuboidShape(0, 7, 4, 16, 13, 11);
    public static final VoxelShape SHAPE_SUPPORTS_NS = Block.makeCuboidShape(1, 4, 5, 15, 11, 11);
    public static final VoxelShape SHAPE_SUPPORTS_WE = Block.makeCuboidShape(5, 4, 1, 11, 11, 15);

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx)
    {
        Direction facing = state.get(FACING);
        boolean ns = facing == Direction.EAST || facing == Direction.WEST;

        return ns ? VoxelShapes.or(SHAPE_BASE, SHAPE_CORE1_NS, SHAPE_CORE2_NS, SHAPE_SUPPORTS_NS)
                : VoxelShapes.or(SHAPE_BASE, SHAPE_CORE1_WE, SHAPE_CORE2_WE, SHAPE_SUPPORTS_WE);
    }

    public GeneratorBlock(Properties properties)
    {
        super(properties);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new GeneratorTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Deprecated
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof GeneratorTileEntity) || player.isShiftKeyDown())
            return ActionResultType.FAIL;

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        NetworkHooks.openGui((ServerPlayerEntity) player,
                new SimpleNamedContainerProvider((id, playerInventory, playerEntity) -> new GeneratorContainer(id, pos, playerInventory),
                        new TranslationTextComponent("container.enderrift.generator")
                ), pos);

        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this)
        {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            return;
        }

        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof GeneratorTileEntity)
        {
            InterfaceBlock.dropInventoryItems(worldIn, pos, ((GeneratorTileEntity) tileentity).inventory());
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
