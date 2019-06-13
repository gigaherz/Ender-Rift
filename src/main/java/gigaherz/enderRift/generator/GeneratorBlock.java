package gigaherz.enderRift.generator;

import gigaherz.enderRift.automation.iface.InterfaceBlock;
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

public class GeneratorBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

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

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof GeneratorTileEntity) || player.isSneaking())
            return false;

        if (worldIn.isRemote)
            return true;

        NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider()
        {
            @Override
            public ITextComponent getDisplayName()
            {
                return new TranslationTextComponent("text.enderrift.browser.title");
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
            {
                return new GeneratorContainer(id, pos, playerInventory);
            }
        }, pos);

        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

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