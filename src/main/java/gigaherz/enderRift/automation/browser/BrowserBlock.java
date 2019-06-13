package gigaherz.enderRift.automation.browser;

import com.google.common.collect.ImmutableMap;
import gigaherz.enderRift.automation.AggregatorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class BrowserBlock extends AggregatorBlock<BrowserEntityTileEntity>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public final boolean crafting;

    public static final VoxelShape WEST_PANEL_PART = Block.makeCuboidShape(12,0,0,16,16,16);
    public static final VoxelShape WEST_PIPE_PART = Block.makeCuboidShape(0,6,6,4,10,10);

    public static final VoxelShape EAST_PANEL_PART = Block.makeCuboidShape(0,0,0,4,16,16);
    public static final VoxelShape EAST_PIPE_PART = Block.makeCuboidShape(12,6,6,16,10,10);

    public static final VoxelShape SOUTH_PANEL_PART = Block.makeCuboidShape(0,0,0,16,16,4);
    public static final VoxelShape SOUTH_PIPE_PART = Block.makeCuboidShape(6,6,12,10,10,16);

    public static final VoxelShape NORTH_PANEL_PART = Block.makeCuboidShape(0,0,12,16,16,16);
    public static final VoxelShape NORTH_PIPE_PART = Block.makeCuboidShape(6,6,0,10,10,4);

    public static final VoxelShape DOWN_PANEL_PART = Block.makeCuboidShape(0,12,0,16,16,16);
    public static final VoxelShape DOWN_PIPE_PART = Block.makeCuboidShape(6,0,6,10,4,10);

    public static final VoxelShape UP_PANEL_PART = Block.makeCuboidShape(0,0,0,16,4,16);
    public static final VoxelShape UP_PIPE_PART = Block.makeCuboidShape(6,12,6,10,16,10);

    public static final VoxelShape CENTER_PART = Block.makeCuboidShape(4,4,4,12,12,12);

    public static final ImmutableMap<Direction, VoxelShape> SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
            .put(Direction.WEST,  VoxelShapes.or(CENTER_PART, VoxelShapes.or(WEST_PANEL_PART, WEST_PIPE_PART)))
            .put(Direction.EAST,  VoxelShapes.or(CENTER_PART, VoxelShapes.or(EAST_PANEL_PART, EAST_PIPE_PART)))
            .put(Direction.SOUTH, VoxelShapes.or(CENTER_PART, VoxelShapes.or(SOUTH_PANEL_PART, SOUTH_PIPE_PART)))
            .put(Direction.NORTH, VoxelShapes.or(CENTER_PART, VoxelShapes.or(NORTH_PANEL_PART, NORTH_PIPE_PART)))
            .put(Direction.DOWN,  VoxelShapes.or(CENTER_PART, VoxelShapes.or(DOWN_PANEL_PART, DOWN_PIPE_PART)))
            .put(Direction.UP,    VoxelShapes.or(CENTER_PART, VoxelShapes.or(UP_PANEL_PART, UP_PIPE_PART)))
            .build();

    public BrowserBlock(boolean crafting, Properties properties)
    {
        super(properties);
        this.crafting = crafting;
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new BrowserEntityTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_)
    {
        return SHAPES.get(state.get(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(BrowserBlock.FACING, context.getFace().getOpposite());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof BrowserEntityTileEntity) || player.isSneaking())
            return false;

        if (player.world.isRemote)
            return true;

        NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider()
        {
            @Override
            public ITextComponent getDisplayName()
            {
                return crafting ? new TranslationTextComponent("text.enderrift.crafting_browser.title")
                                : new TranslationTextComponent("text.enderrift.browser.title");
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
            {
                return crafting ? new CraftingBrowserContainer(id, pos, playerInventory)
                                : new BrowserContainer(id, pos, playerInventory);
            }
        }, pos);

        return true;
    }
}
