package gigaherz.enderRift.rift;

import gigaherz.common.BlockRegistered;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class BlockEnderRift
        extends BlockRegistered
{
    public static final PropertyBool ASSEMBLED = PropertyBool.create("assembled");

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(2f / 16, 2f / 16, 2f / 16, 14f / 16, 14f / 16, 14f / 16);

    public BlockEnderRift(String name)
    {
        super(name, Material.ROCK);
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setHardness(3.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(ASSEMBLED, false));
    }


    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ASSEMBLED);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(ASSEMBLED, meta != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(ASSEMBLED) ? 1 : 0;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public int getLightValue(IBlockState state)
    {
        if (state.getBlock() != this)
            return super.getLightValue(state);
        return state.getValue(ASSEMBLED) ? 15 : 0;
    }

    @Deprecated
    @Override
    public int getLightOpacity(IBlockState state)
    {
        if (state.getBlock() != this)
            return super.getLightOpacity(state);
        return state.getValue(ASSEMBLED) ? 1 : 15;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BOUNDS;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEnderRift createTileEntity(World world, IBlockState state)
    {
        return new TileEnderRift();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        drops.add(new ItemStack(this));

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderRift)
            drops.add(((TileEnderRift) te).getRiftItem());
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        RiftStructure.dismantle(worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (playerIn.isSneaking())
            return false;

        int slot = playerIn.inventory.currentItem;
        ItemStack stack = playerIn.inventory.getStackInSlot(slot);

        if (stack.getItem() == EnderRiftMod.riftOrb)
            return false;

        if (worldIn.isRemote)
            return true;

        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return false;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEnderRift))
            return false;

        TileEnderRift rift = (TileEnderRift) te;

        int count = stack.getCount();
        ItemStack stackToPush = stack.splitStack(count);
        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stackToPush);
        stack.grow(remaining.getCount());

        if (stack.getCount() <= 0)
            stack = ItemStack.EMPTY;

        playerIn.inventory.setInventorySlotContents(slot, stack);

        return true;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {

        if (worldIn.isRemote)
            return;

        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEnderRift))
            return;

        TileEnderRift rift = (TileEnderRift) te;

        ItemStack stack = playerIn.getHeldItem(EnumHand.MAIN_HAND);
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

        ItemStack extracted = AutomationHelper.extractItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack.copy(), numberToExtract, false);

        if (extracted.getCount() > 0)
        {
            EntityItem entityItem = new EntityItem(worldIn, playerIn.posX, playerIn.posY + playerIn.getEyeHeight() / 2, playerIn.posZ, extracted);
            worldIn.spawnEntity(entityItem);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        if (worldIn.isRemote)
            return;

        if (!(entityIn instanceof EntityItem))
            return;

        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEnderRift))
            return;

        TileEnderRift rift = (TileEnderRift) te;

        EntityItem item = (EntityItem) entityIn;
        ItemStack stack = item.getItem().copy();

        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack);

        if (remaining.getCount() <= 0)
        {
            entityIn.setDead();
        }
        else
        {
            item.setItem(remaining);
        }
    }
}
