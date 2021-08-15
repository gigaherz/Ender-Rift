package dev.gigaherz.enderrift.automation.browser;

import com.google.common.collect.ImmutableMap;
import dev.gigaherz.enderrift.automation.AggregatorBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class BrowserBlock extends AggregatorBlock<BrowserBlockEntity>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public final boolean crafting;

    public static final VoxelShape NORTH_PANEL_PART = Block.box(0, 0, 12, 16, 16, 16);
    public static final VoxelShape NORTH_CENTER_PART1 = Block.box(2, 2, 8, 14, 14, 12);
    public static final VoxelShape NORTH_CENTER_PART2 = Block.box(4, 4, 4, 12, 12, 8);
    public static final VoxelShape NORTH_PIPE_PART = Block.box(6, 6, 0, 10, 10, 4);

    public static final VoxelShape SOUTH_PANEL_PART = Block.box(0, 0, 0, 16, 16, 4);
    public static final VoxelShape SOUTH_CENTER_PART1 = Block.box(2, 2, 4, 14, 14, 8);
    public static final VoxelShape SOUTH_CENTER_PART2 = Block.box(4, 4, 8, 12, 12, 12);
    public static final VoxelShape SOUTH_PIPE_PART = Block.box(6, 6, 12, 10, 10, 16);

    public static final VoxelShape WEST_PANEL_PART = Block.box(12, 0, 0, 16, 16, 16);
    public static final VoxelShape WEST_CENTER_PART1 = Block.box(8, 2, 2, 12, 14, 14);
    public static final VoxelShape WEST_CENTER_PART2 = Block.box(4, 4, 4, 8, 12, 12);
    public static final VoxelShape WEST_PIPE_PART = Block.box(0, 6, 6, 4, 10, 10);

    public static final VoxelShape EAST_PANEL_PART = Block.box(0, 0, 0, 4, 16, 16);
    public static final VoxelShape EAST_CENTER_PART1 = Block.box(4, 2, 2, 8, 14, 14);
    public static final VoxelShape EAST_CENTER_PART2 = Block.box(8, 4, 4, 12, 12, 12);
    public static final VoxelShape EAST_PIPE_PART = Block.box(12, 6, 6, 16, 10, 10);

    public static final VoxelShape DOWN_PANEL_PART = Block.box(0, 12, 0, 16, 16, 16);
    public static final VoxelShape DOWN_CENTER_PART1 = Block.box(2, 8, 2, 14, 12, 14);
    public static final VoxelShape DOWN_CENTER_PART2 = Block.box(4, 4, 4, 12, 8, 12);
    public static final VoxelShape DOWN_PIPE_PART = Block.box(6, 0, 6, 10, 4, 10);

    public static final VoxelShape UP_PANEL_PART = Block.box(0, 0, 0, 16, 4, 16);
    public static final VoxelShape UP_CENTER_PART1 = Block.box(2, 4, 2, 14, 8, 14);
    public static final VoxelShape UP_CENTER_PART2 = Block.box(4, 8, 4, 12, 12, 12);
    public static final VoxelShape UP_PIPE_PART = Block.box(6, 12, 6, 10, 16, 10);

    public static final ImmutableMap<Direction, VoxelShape> SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
            .put(Direction.WEST, Shapes.or(WEST_CENTER_PART1, WEST_CENTER_PART2, WEST_PANEL_PART, WEST_PIPE_PART))
            .put(Direction.EAST, Shapes.or(EAST_CENTER_PART1, EAST_CENTER_PART2, EAST_PANEL_PART, EAST_PIPE_PART))
            .put(Direction.SOUTH, Shapes.or(SOUTH_CENTER_PART1, SOUTH_CENTER_PART2, SOUTH_PANEL_PART, SOUTH_PIPE_PART))
            .put(Direction.NORTH, Shapes.or(NORTH_CENTER_PART1, NORTH_CENTER_PART2, NORTH_PANEL_PART, NORTH_PIPE_PART))
            .put(Direction.DOWN, Shapes.or(DOWN_CENTER_PART1, DOWN_CENTER_PART2, DOWN_PANEL_PART, DOWN_PIPE_PART))
            .put(Direction.UP, Shapes.or(UP_CENTER_PART1, UP_CENTER_PART2, UP_PANEL_PART, UP_PIPE_PART))
            .build();

    public BrowserBlock(boolean crafting, Properties properties)
    {
        super(properties);
        this.crafting = crafting;
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BrowserBlockEntity(pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_)
    {
        return BaseEntityBlock.createTickerHelper(p_153214_, BrowserBlockEntity.TYPE, BrowserBlockEntity::tickStatic);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(BrowserBlock.FACING, context.getClickedFace().getOpposite());
    }

    @Deprecated
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
    {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if (!(tileEntity instanceof BrowserBlockEntity) || player.isShiftKeyDown())
            return InteractionResult.FAIL;

        if (player.level.isClientSide)
            return InteractionResult.SUCCESS;

        if (crafting)
            openCraftingBrowser(player, (BrowserBlockEntity) tileEntity);
        else
            openBrowser(player, (BrowserBlockEntity) tileEntity);

        return InteractionResult.SUCCESS;
    }

    private void openBrowser(Player player, BrowserBlockEntity tileEntity)
    {
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> new BrowserContainer(id, tileEntity, playerInventory),
                new TranslatableComponent("container.enderrift.browser")));
    }

    private void openCraftingBrowser(Player player, BrowserBlockEntity tileEntity)
    {
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> new CraftingBrowserContainer(id, tileEntity, playerInventory),
                new TranslatableComponent("container.enderrift.crafting_browser")));
    }
}