package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.RiftStructure;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockEnderRift
        extends Block
{
    public static final PropertyBool ASSEMBLED = PropertyBool.create("assembled");

    public BlockEnderRift()
    {
        super(Material.rock);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockEnderRift");
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setBlockBounds(0.2F, 0.2F, 0.2F, 0.8F, 0.8F, 0.8F);
        setHardness(0.5F);
        setStepSound(Block.soundTypeMetal);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(ASSEMBLED, false));
    }


    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, ASSEMBLED);
    }

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

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this)
            return super.getLightValue(world, pos);
        return world.getBlockState(pos).getValue(ASSEMBLED) ? 15 : 0;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this)
            return super.getLightOpacity(world, pos);
        return state.getValue(ASSEMBLED) ? 1 : 15;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEnderRift();
    }

    @Override
    public int getRenderType()
    {
        return super.getRenderType();
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(world, pos, player, false);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        ArrayList<ItemStack> ret = Lists.newArrayList();

        ret.add(new ItemStack(this));

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderRift)
            ret.add(((TileEnderRift) te).getRiftItem());

        return ret;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        super.harvestBlock(worldIn, player, pos, state, te);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        RiftStructure.dismantle(worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (playerIn.isSneaking())
            return false;

        int slot = playerIn.inventory.currentItem;
        ItemStack stack = playerIn.inventory.getStackInSlot(slot);

        if (stack == null || stack.getItem() == EnderRiftMod.riftOrb)
            return false;

        if (worldIn.isRemote)
            return true;

        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return false;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEnderRift))
            return false;

        TileEnderRift rift = (TileEnderRift) te;

        int count = stack.stackSize;
        ItemStack stackToPush = stack.splitStack(count);
        ItemStack remaining = rift.pushItems(stackToPush);
        if (remaining != null)
        {
            stack.stackSize += remaining.stackSize;
        }

        if (stack.stackSize <= 0)
            stack = null;

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

        ItemStack stack = playerIn.getHeldItem();
        if (stack != null)
        {
            if (stack.getItem() == EnderRiftMod.riftOrb)
                return;
        }
        else
        {
            stack = rift.chooseRandomStack();
            if (stack == null)
                return;
        }

        int numberToExtract = playerIn.isSneaking() ? 1 : stack.getMaxStackSize();

        ItemStack extracted = rift.extractItems(stack.copy(), numberToExtract, false);

        if (extracted != null && extracted.stackSize > 0)
        {
            EntityItem entityItem = new EntityItem(worldIn, playerIn.posX, playerIn.posY + playerIn.getEyeHeight() / 2, playerIn.posZ, extracted);
            worldIn.spawnEntityInWorld(entityItem);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
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
        ItemStack stack = item.getEntityItem().copy();

        ItemStack remaining = rift.pushItems(stack);

        if (remaining == null)
        {
            entityIn.setDead();
        }
        else
        {
            item.setEntityItemStack(remaining);
        }
    }
}
