package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.BlockAggregator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBrowser extends BlockAggregator<TileBrowser>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final String unlocStandard = EnderRiftMod.MODID + ".browser";
    private static final String unlocCrafting = EnderRiftMod.MODID + ".crafting_browser";

    public final boolean crafting;

    public BlockBrowser(boolean crafting, Properties properties)
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
        return new TileBrowser();
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
        return getDefaultState().with(BlockBrowser.FACING, context.getFace().getOpposite());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileBrowser) || player.isSneaking())
            return false;

        /*
        int which = crafting ?
                GuiHandler.GUI_BROWSER_CRAFTING :
                GuiHandler.GUI_BROWSER;

        player.openGui(EnderRiftMod.instance, which, worldIn, pos.getX(), pos.getY(), pos.getZ());
         */

        return true;
    }
}
