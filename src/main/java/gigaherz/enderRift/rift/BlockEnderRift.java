package gigaherz.enderRift.rift;

import com.google.common.collect.Lists;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockEnderRift
        extends BlockRegistered
{
    public static final PropertyBool ASSEMBLED = PropertyBool.create("assembled");

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
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
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
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
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
        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stackToPush);
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

        ItemStack stack = playerIn.getHeldItem(EnumHand.MAIN_HAND);
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

        ItemStack extracted = AutomationHelper.extractItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack.copy(), numberToExtract, false);

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

        ItemStack remaining = AutomationHelper.insertItems(rift.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack);

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
