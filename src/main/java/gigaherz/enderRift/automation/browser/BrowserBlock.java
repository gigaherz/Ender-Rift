package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class BrowserBlock extends AggregatorBlock<BrowserEntityTileEntity>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final String unlocStandard = EnderRiftMod.MODID + ".browser";
    private static final String unlocCrafting = EnderRiftMod.MODID + ".crafting_browser";

    public final boolean crafting;

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

    /*@Deprecated
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face)
    {
        Direction st = state.getValue(FACING);

        if (st == face)
            return BlockFaceShape.CENTER;

        Direction op = face.getOpposite();
        if (st == op)
            return BlockFaceShape.SOLID;

        return BlockFaceShape.UNDEFINED;
    }*/

    /*@Deprecated
    @Override
    public boolean isSideSolid(BlockState base_state, IBlockReader world, BlockPos pos, Direction side)
    {
        return base_state.getValue(FACING) == side.getOpposite();
    }*/

    /*@Override
    public boolean canPlaceTorchOnTop(BlockState state, IBlockReader world, BlockPos pos)
    {
        return state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.DOWN;
    }*/

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
