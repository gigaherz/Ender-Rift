package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
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
        setHardness(0.5F);
        setStepSound(Block.soundTypeMetal);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockEnderRift");
        setCreativeTab(EnderRiftMod.tabEnderRift);
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

    Block getBlockXYZ(IBlockAccess world, int x, int y, int z)
    {
        return world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    void setBlockXYZ(World world, int x, int y, int z, IBlockState state)
    {
        world.setBlockState(new BlockPos(x, y, z), state);
    }

    void setBlockCorner(World world, int x, int y, int z, BlockStructure.Corner corner, boolean base)
    {
        setBlockXYZ(world, x, y, z, EnderRiftMod.blockStructure.getDefaultState()
                .withProperty(BlockStructure.TYPE1, BlockStructure.Type1.CORNER)
                .withProperty(BlockStructure.CORNER, corner)
                .withProperty(BlockStructure.BASE, base));
    }

    private void setBlockOther(World world, int x, int y, int z, BlockStructure.Type2 type2, boolean base)
    {
        setBlockXYZ(world, x, y, z, EnderRiftMod.blockStructure.getDefaultState()
                .withProperty(BlockStructure.TYPE1, BlockStructure.Type1.NORMAL)
                .withProperty(BlockStructure.TYPE2, type2)
                .withProperty(BlockStructure.BASE, base));
    }

    public boolean tryCompleteStructure(World world, BlockPos pos, ItemStack itemStack)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.blockEnderRift)
            return false;

        if (state.getValue(ASSEMBLED))
            return false;

        if (getBlockXYZ(world, x - 1, y, z).isNormalCube(world, pos))
            return false;
        if (getBlockXYZ(world, x + 1, y, z).isNormalCube(world, pos))
            return false;
        if (getBlockXYZ(world, x, y - 1, z).isNormalCube(world, pos))
            return false;
        if (getBlockXYZ(world, x, y + 1, z).isNormalCube(world, pos))
            return false;
        if (getBlockXYZ(world, x, y, z - 1).isNormalCube(world, pos))
            return false;
        if (getBlockXYZ(world, x, y, z + 1).isNormalCube(world, pos))
            return false;

        if (getBlockXYZ(world, x - 1, y, z - 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y, z - 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x - 1, y, z + 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y, z + 1) != Blocks.redstone_block)
            return false;

        if (getBlockXYZ(world, x - 1, y - 1, z - 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x, y - 1, z - 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y - 1, z - 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x - 1, y - 1, z) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y - 1, z) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x - 1, y - 1, z + 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x, y - 1, z + 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y - 1, z + 1) != Blocks.iron_block)
            return false;

        if (getBlockXYZ(world, x - 1, y + 1, z - 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x, y + 1, z - 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y + 1, z - 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x - 1, y + 1, z) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y + 1, z) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x - 1, y + 1, z + 1) != Blocks.iron_block)
            return false;
        if (getBlockXYZ(world, x, y + 1, z + 1) != Blocks.redstone_block)
            return false;
        if (getBlockXYZ(world, x + 1, y + 1, z + 1) != Blocks.iron_block)
            return false;

        setBlockCorner(world, x - 1, y - 1, z - 1, BlockStructure.Corner.NW, true);
        setBlockCorner(world, x + 1, y - 1, z - 1, BlockStructure.Corner.NE, true);
        setBlockCorner(world, x - 1, y - 1, z + 1, BlockStructure.Corner.SW, true);
        setBlockCorner(world, x + 1, y - 1, z + 1, BlockStructure.Corner.SE, true);
        setBlockCorner(world, x - 1, y + 1, z - 1, BlockStructure.Corner.NW, false);
        setBlockCorner(world, x + 1, y + 1, z - 1, BlockStructure.Corner.NE, false);
        setBlockCorner(world, x - 1, y + 1, z + 1, BlockStructure.Corner.SW, false);
        setBlockCorner(world, x + 1, y + 1, z + 1, BlockStructure.Corner.SE, false);

        setBlockOther(world, x, y - 1, z - 1, BlockStructure.Type2.SIDE_EW, true);
        setBlockOther(world, x, y + 1, z - 1, BlockStructure.Type2.SIDE_EW, false);
        setBlockOther(world, x, y - 1, z + 1, BlockStructure.Type2.SIDE_EW, true);
        setBlockOther(world, x, y + 1, z + 1, BlockStructure.Type2.SIDE_EW, false);
        setBlockOther(world, x - 1, y, z - 1, BlockStructure.Type2.VERTICAL, false);
        setBlockOther(world, x + 1, y, z - 1, BlockStructure.Type2.VERTICAL, false);
        setBlockOther(world, x - 1, y, z + 1, BlockStructure.Type2.VERTICAL, false);
        setBlockOther(world, x + 1, y, z + 1, BlockStructure.Type2.VERTICAL, false);
        setBlockOther(world, x - 1, y - 1, z, BlockStructure.Type2.SIDE_NS, true);
        setBlockOther(world, x + 1, y - 1, z, BlockStructure.Type2.SIDE_NS, true);
        setBlockOther(world, x - 1, y + 1, z, BlockStructure.Type2.SIDE_NS, false);
        setBlockOther(world, x + 1, y + 1, z, BlockStructure.Type2.SIDE_NS, false);

        world.setBlockState(pos, state.withProperty(ASSEMBLED, true));

        TileEnderRift rift = (TileEnderRift) world.getTileEntity(pos);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("RiftId"))
        {
            rift.inventory = null;
            rift.markDirty();
            rift.riftId = tagCompound.getInteger("RiftId");
        }
        else
        {
            rift.inventory = null;
            rift.markDirty();
            rift.riftId = RiftStorageWorldData.get(world).getNextRiftId();
        }

        return true;
    }

    public void breakStructure(World world, BlockPos pos)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        restoreStructureBlockTo(world, x - 1, y, z - 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y, z - 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x - 1, y, z + 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y, z + 1, Blocks.redstone_block);

        restoreStructureBlockTo(world, x - 1, y - 1, z - 1, Blocks.iron_block);
        restoreStructureBlockTo(world, x, y - 1, z - 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y - 1, z - 1, Blocks.iron_block);

        restoreStructureBlockTo(world, x - 1, y - 1, z, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y - 1, z, Blocks.redstone_block);

        restoreStructureBlockTo(world, x - 1, y - 1, z + 1, Blocks.iron_block);
        restoreStructureBlockTo(world, x, y - 1, z + 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y - 1, z + 1, Blocks.iron_block);

        restoreStructureBlockTo(world, x - 1, y + 1, z - 1, Blocks.iron_block);
        restoreStructureBlockTo(world, x, y + 1, z - 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y + 1, z - 1, Blocks.iron_block);

        restoreStructureBlockTo(world, x - 1, y + 1, z, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y + 1, z, Blocks.redstone_block);

        restoreStructureBlockTo(world, x - 1, y + 1, z + 1, Blocks.iron_block);
        restoreStructureBlockTo(world, x, y + 1, z + 1, Blocks.redstone_block);
        restoreStructureBlockTo(world, x + 1, y + 1, z + 1, Blocks.iron_block);

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() == EnderRiftMod.blockEnderRift && state.getValue(ASSEMBLED))
        {
            world.setBlockState(pos, state.withProperty(ASSEMBLED, false));

            TileEnderRift rift = (TileEnderRift) world.getTileEntity(pos);

            rift.inventory = null;
            rift.markDirty();

            ItemStack stack = rift.getRiftItem();

            Entity entity = new EntityItem(world, x, y, z, stack);
            world.spawnEntityInWorld(entity);
        }
    }

    private void restoreStructureBlockTo(World world, int xx, int yy, int zz, Block bb)
    {
        if (getBlockXYZ(world, xx, yy, zz) == EnderRiftMod.blockStructure)
            setBlockXYZ(world, xx, yy, zz, bb.getDefaultState());
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(world, pos, player, false);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        breakStructure(worldIn, pos);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        super.harvestBlock(worldIn, player, pos, state, te);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        ArrayList<ItemStack> ret = Lists.newArrayList();

        ret.add(new ItemStack(Item.getItemFromBlock(this), 1, 0));

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderRift)
            ret.add(((TileEnderRift) te).getRiftItem());

        return ret;
    }

    public boolean tryDuplicateRift(World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEnderRift))
            return false;

        ItemStack stack = ((TileEnderRift) te).getRiftItem();

        Entity entity = new EntityItem(world, player.posX, player.posY + 0.5f, player.posZ, stack);
        world.spawnEntityInWorld(entity);

        return true;
    }
}
