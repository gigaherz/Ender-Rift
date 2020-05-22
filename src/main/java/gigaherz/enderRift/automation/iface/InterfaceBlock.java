package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.automation.AggregatorBlock;
import gigaherz.enderRift.automation.browser.BrowserBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class InterfaceBlock extends AggregatorBlock<InterfaceTileEntity>
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public InterfaceBlock(Properties properties)
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
        return new InterfaceTileEntity();
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_)
    {
        return BrowserBlock.SHAPES.get(state.get(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(InterfaceBlock.FACING, context.getFace().getOpposite());
    }

    @Deprecated
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof InterfaceTileEntity) || player.isSneaking())
            return ActionResultType.FAIL;

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (id, playerInventory, playerEntity) -> new InterfaceContainer(id, pos, playerInventory),
                new TranslationTextComponent("container.enderrift.interface")), pos);

        return ActionResultType.SUCCESS;
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

        if (tileentity instanceof InterfaceTileEntity)
        {
            dropInventoryItems(worldIn, pos, ((InterfaceTileEntity) tileentity).inventoryOutputs());
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

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
