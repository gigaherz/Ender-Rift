package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.automation.BlockAggregator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockInterface extends BlockAggregator<TileInterface>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockInterface(Properties properties)
    {
        super(properties);
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
        return new TileInterface();
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(BlockInterface.FACING, context.getFace().getOpposite());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileInterface) || playerIn.isSneaking())
            return false;

        //NetworkHooks.openGui(EnderRiftMod.instance, GuiHandler.GUI_INTERFACE, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    /*@Override
    public void breakBlock(World worldIn, BlockPos pos, BlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileInterface)
        {
            dropInventoryItems(worldIn, pos, ((TileInterface) tileentity).inventoryOutputs());
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }*/

    public static void dropInventoryItems(World worldIn, BlockPos pos, IItemHandler inventory)
    {
        for (int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack.getCount() > 0)
            {
                InventoryHelper.spawnItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), itemstack);
            }
        }
    }
}
