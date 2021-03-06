package gigaherz.enderRift.rift;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.loot.LootContext;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;

public class RiftBlock extends Block
{
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    private static final VoxelShape SHAPE = Block.makeCuboidShape(5, 5, 5, 11, 11, 11);
    private static final VoxelShape SHAPE_ACTIVE = Block.makeCuboidShape(2, 2, 2, 14, 14, 14);

    public RiftBlock(Properties properties)
    {
        super(properties);
        setDefaultState(this.getStateContainer().getBaseState()
                .with(ASSEMBLED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ASSEMBLED);
    }

    @Deprecated
    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        if (state.getBlock() != this)
            return super.getLightValue(state, world, pos);
        return state.get(ASSEMBLED) ? 15 : 0;
    }

    @Deprecated
    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        if (state.getBlock() != this)
            return super.getOpacity(state, worldIn, pos);
        return state.get(ASSEMBLED) ? 1 : 15;
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
        return new RiftTileEntity();
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return state.get(ASSEMBLED) ? SHAPE_ACTIVE : SHAPE;
    }

    @Deprecated
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        return Lists.newArrayList(new ItemStack(this));
    }

    @Deprecated
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this || !state.get(ASSEMBLED))
        {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof RiftTileEntity)
        {
            spawnAsEntity(worldIn, pos, ((RiftTileEntity) te).getRiftItem());
            te.remove();
        }

        RiftStructure.dismantle(worldIn, pos);
    }

    @Deprecated
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit)
    {
        if (playerIn.isSneaking())
            return ActionResultType.PASS;

        ItemStack stack = playerIn.getHeldItem(handIn);

        if (stack.getItem() == EnderRiftMod.EnderRiftItems.RIFT_ORB)
            return ActionResultType.PASS;

        if (state.getBlock() != this || !state.get(ASSEMBLED))
            return ActionResultType.FAIL;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof RiftTileEntity))
            return ActionResultType.FAIL;

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        RiftTileEntity rift = (RiftTileEntity) te;

        int count = stack.getCount();
        ItemStack stackToPush = stack.split(count);
        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(() -> new RuntimeException("WAT")), stackToPush);
        stack.grow(remaining.getCount());

        if (stack.getCount() <= 0)
            stack = ItemStack.EMPTY;

        playerIn.setHeldItem(handIn, stack);

        return ActionResultType.SUCCESS;
    }

    @Deprecated
    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn)
    {
        if (worldIn.isRemote)
            return;

        if (state.getBlock() != this || !state.get(ASSEMBLED))
            return;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof RiftTileEntity))
            return;

        RiftTileEntity rift = (RiftTileEntity) te;

        ItemStack stack = playerIn.getHeldItem(Hand.MAIN_HAND);
        if (stack.getCount() <= 0)
        {
            if (stack.getItem() == EnderRiftMod.EnderRiftItems.RIFT_ORB)
                return;
        }
        else
        {
            stack = rift.chooseRandomStack();
            if (stack.getCount() <= 0)
                return;
        }

        int numberToExtract = playerIn.isSneaking() ? 1 : stack.getMaxStackSize();

        ItemStack extracted = AutomationHelper.extractItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(() -> new RuntimeException("WAT")), stack.copy(), numberToExtract, false);
        if (extracted.getCount() > 0)
        {
            spawnAsEntity(worldIn, pos, extracted);
        }
    }

    @Deprecated
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        if (worldIn.isRemote)
            return;

        if (!(entityIn instanceof ItemEntity))
            return;

        if (state.getBlock() != this || !state.get(ASSEMBLED))
            return;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof RiftTileEntity))
            return;

        RiftTileEntity rift = (RiftTileEntity) te;

        ItemEntity item = (ItemEntity) entityIn;
        ItemStack stack = item.getItem().copy();

        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(() -> new RuntimeException("WAT")), stack);

        if (remaining.getCount() <= 0)
        {
            entityIn.remove();
        }
        else
        {
            item.setItem(remaining);
        }
    }
}