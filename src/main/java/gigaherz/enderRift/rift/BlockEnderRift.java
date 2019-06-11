package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class BlockEnderRift extends Block
{
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(2f / 16, 2f / 16, 2f / 16, 14f / 16, 14f / 16, 14f / 16);

    public BlockEnderRift(Properties properties)
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
    public int getLightValue(BlockState state)
    {
        if (state.getBlock() != this)
            return super.getLightValue(state);
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
        return new TileEnderRift();
    }

    /*@Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, BlockState state, int fortune)
    {
        drops.add(new ItemStack(this));

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderRift)
            drops.add(((TileEnderRift) te).getRiftItem());
    }*/

    /*@Override
    public void breakBlock(World worldIn, BlockPos pos, BlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        RiftStructure.dismantle(worldIn, pos);
    }*/

    @Deprecated
    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit)
    {
        if (playerIn.isSneaking())
            return false;

        int slot = playerIn.inventory.currentItem;
        ItemStack stack = playerIn.inventory.getStackInSlot(slot);

        if (stack.getItem() == EnderRiftMod.riftOrb)
            return false;

        if (worldIn.isRemote)
            return true;

        if (state.getBlock() != this || !state.get(ASSEMBLED))
            return false;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEnderRift))
            return false;

        TileEnderRift rift = (TileEnderRift) te;

        int count = stack.getCount();
        ItemStack stackToPush = stack.split(count);
        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(() -> new RuntimeException("WAT")), stackToPush);
        stack.grow(remaining.getCount());

        if (stack.getCount() <= 0)
            stack = ItemStack.EMPTY;

        playerIn.inventory.setInventorySlotContents(slot, stack);

        return true;
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
        if (!(te instanceof TileEnderRift))
            return;

        TileEnderRift rift = (TileEnderRift) te;

        ItemStack stack = playerIn.getHeldItem(Hand.MAIN_HAND);
        if (stack.getCount() <= 0)
        {
            if (stack.getItem() == EnderRiftMod.riftOrb)
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
        if (!(te instanceof TileEnderRift))
            return;

        TileEnderRift rift = (TileEnderRift) te;

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
