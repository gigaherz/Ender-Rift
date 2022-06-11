package dev.gigaherz.enderrift.generator;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class GeneratorBlock extends BaseEntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE_BASE = Block.box(0, 0, 0, 16, 4, 16);
    public static final VoxelShape SHAPE_CORE1_NS = Block.box(2, 4, 3, 14, 16, 13);
    public static final VoxelShape SHAPE_CORE1_WE = Block.box(3, 4, 2, 13, 16, 14);
    public static final VoxelShape SHAPE_CORE2_NS = Block.box(4, 7, 0, 11, 13, 16);
    public static final VoxelShape SHAPE_CORE2_WE = Block.box(0, 7, 4, 16, 13, 11);
    public static final VoxelShape SHAPE_SUPPORTS_NS = Block.box(1, 4, 5, 15, 11, 11);
    public static final VoxelShape SHAPE_SUPPORTS_WE = Block.box(5, 4, 1, 11, 11, 15);

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx)
    {
        Direction facing = state.getValue(FACING);
        boolean ns = facing == Direction.EAST || facing == Direction.WEST;

        return ns ? Shapes.or(SHAPE_BASE, SHAPE_CORE1_NS, SHAPE_CORE2_NS, SHAPE_SUPPORTS_NS)
                : Shapes.or(SHAPE_BASE, SHAPE_CORE1_WE, SHAPE_CORE2_WE, SHAPE_SUPPORTS_WE);
    }

    public GeneratorBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new GeneratorBlockEntity(pos, state);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        return BaseEntityBlock.createTickerHelper(pBlockEntityType, EnderRiftMod.GENERATOR_BLOCK_ENTITY.get(), GeneratorBlockEntity::tickStatic);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Deprecated
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
    {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if (!(tileEntity instanceof GeneratorBlockEntity be) || player.isShiftKeyDown())
            return InteractionResult.FAIL;

        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> new GeneratorContainer(id, playerInventory, be.inventory(), be.getFields(), ContainerLevelAccess.create(worldIn, pos)),
                Component.translatable("container.enderrift.generator")
        ));

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this)
        {
            super.onRemove(state, worldIn, pos, newState, isMoving);
            return;
        }

        BlockEntity tileentity = worldIn.getBlockEntity(pos);

        if (tileentity instanceof GeneratorBlockEntity)
        {
            InterfaceBlock.dropInventoryItems(worldIn, pos, ((GeneratorBlockEntity) tileentity).inventory());
            worldIn.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}