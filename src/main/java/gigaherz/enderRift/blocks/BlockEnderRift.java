package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.client.SBRHEnderRift;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockEnderRift
        extends Block
        implements ITileEntityProvider
{
    public IIcon iconMain;
    public IIcon iconBase;

    public boolean isInventory;
    public final BlockEnderRift asInventory;

    public BlockEnderRift(boolean isInventory)
    {
        super(Material.rock);
        this.isInventory = isInventory;
        setBlockTextureName(EnderRiftMod.MODID.toLowerCase() + ":block_casing");
        asInventory = null;
    }

    public BlockEnderRift()
    {
        super(Material.rock);
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setBlockTextureName(EnderRiftMod.MODID.toLowerCase() + ":block_casing");
        asInventory = new BlockEnderRift(true);
    }

    @Override
    public void registerBlockIcons(IIconRegister register)
    {
        super.registerBlockIcons(register);
        if(!isInventory)
        {
            this.iconMain = register.registerIcon(EnderRiftMod.MODID.toLowerCase() + ":block_rift");
            this.iconBase = register.registerIcon(EnderRiftMod.MODID.toLowerCase() + ":block_rift_base");
        }
    }

    @Override
    public int getRenderType()
    {
        return isInventory ? super.getRenderType() : SBRHEnderRift.renderId;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEnderRift();
    }

    public boolean tryCompleteStructure(World world, int x, int y, int z, ItemStack itemStack)
    {
        if(world.getBlock(x, y, z) != EnderRiftMod.blockEnderRift)
            return false;

        if(world.getBlockMetadata(x, y, z) != 0)
            return false;

        if(world.getBlock(x - 1, y, z).isNormalCube(world, x, y, z))
            return false;
        if(world.getBlock(x + 1, y, z).isNormalCube(world, x, y, z))
            return false;
        if(world.getBlock(x, y + 1, z).isNormalCube(world, x, y, z))
            return false;
        if(world.getBlock(x, y, z - 1).isNormalCube(world, x, y, z))
            return false;
        if(world.getBlock(x, y, z + 1).isNormalCube(world, x, y, z))
            return false;

        if(world.getBlock(x - 1, y, z - 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y, z - 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x - 1, y, z + 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y, z + 1) != Blocks.redstone_block)
            return false;

        if(world.getBlock(x - 1, y - 1, z - 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x, y - 1, z - 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y - 1, z - 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x - 1, y - 1, z) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x, y - 1, z) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y - 1, z) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x - 1, y - 1, z + 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x, y - 1, z + 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y - 1, z + 1) != Blocks.iron_block)
            return false;

        if(world.getBlock(x - 1, y + 1, z - 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x, y + 1, z - 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y + 1, z - 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x - 1, y + 1, z) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y + 1, z) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x - 1, y + 1, z + 1) != Blocks.iron_block)
            return false;
        if(world.getBlock(x, y + 1, z + 1) != Blocks.redstone_block)
            return false;
        if(world.getBlock(x + 1, y + 1, z + 1) != Blocks.iron_block)
            return false;

        world.setBlock(x - 1, y - 1, z - 1, EnderRiftMod.blockStructureCorner,0,3);
        world.setBlock(x + 1, y - 1, z - 1, EnderRiftMod.blockStructureCorner,1,3);
        world.setBlock(x - 1, y + 1, z - 1, EnderRiftMod.blockStructureCorner,2,3);
        world.setBlock(x + 1, y + 1, z - 1, EnderRiftMod.blockStructureCorner,3,3);
        world.setBlock(x - 1, y - 1, z + 1, EnderRiftMod.blockStructureCorner,4,3);
        world.setBlock(x + 1, y - 1, z + 1, EnderRiftMod.blockStructureCorner,5,3);
        world.setBlock(x - 1, y + 1, z + 1, EnderRiftMod.blockStructureCorner,6,3);
        world.setBlock(x + 1, y + 1, z + 1, EnderRiftMod.blockStructureCorner,7,3);

        world.setBlock(x, y - 1, z,     EnderRiftMod.blockStructureInvisible,0,3);
        world.setBlock(x, y - 1, z - 1, EnderRiftMod.blockStructureInvisible,4,3);
        world.setBlock(x, y + 1, z - 1, EnderRiftMod.blockStructureInvisible,5,3);
        world.setBlock(x, y - 1, z + 1, EnderRiftMod.blockStructureInvisible,6,3);
        world.setBlock(x, y + 1, z + 1, EnderRiftMod.blockStructureInvisible,7,3);
        world.setBlock(x - 1, y, z - 1, EnderRiftMod.blockStructureInvisible,8,3);
        world.setBlock(x + 1, y, z - 1, EnderRiftMod.blockStructureInvisible,9,3);
        world.setBlock(x - 1, y, z + 1, EnderRiftMod.blockStructureInvisible,10,3);
        world.setBlock(x + 1, y, z + 1, EnderRiftMod.blockStructureInvisible,11,3);
        world.setBlock(x - 1, y - 1, z, EnderRiftMod.blockStructureInvisible,12,3);
        world.setBlock(x + 1, y - 1, z, EnderRiftMod.blockStructureInvisible,13,3);
        world.setBlock(x - 1, y + 1, z, EnderRiftMod.blockStructureInvisible,14,3);
        world.setBlock(x + 1, y + 1, z, EnderRiftMod.blockStructureInvisible,15,3);

        world.setBlockMetadataWithNotify(x, y, z, 1, 3);

        TileEnderRift rift = (TileEnderRift)world.getTileEntity(x, y, z);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if(tagCompound != null && tagCompound.hasKey("RiftId"))
        {
            rift.riftId = tagCompound.getInteger("RiftId");
        }
        else
        {
            rift.riftId = RiftStorageWorldData.get(world).getNextRiftId();
        }

        return true;
    }

    public void breakStructure(World world, int x, int y, int z)
    {
        restoreStructureBlockTo(world, x, y - 1, z, Blocks.redstone_block);

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

        if(world.getBlockMetadata(x,y,z) != 0)
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
    }

    private void restoreStructureBlockTo(World world, int xx, int yy, int zz, Block bb)
    {
        if(world.getBlock(xx, yy, zz) == EnderRiftMod.blockStructureInvisible
            || world.getBlock(xx, yy, zz) == EnderRiftMod.blockStructureCorner)
            world.setBlock(xx, yy, zz, bb);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta)
    {
        super.harvestBlock(world, player, x, y, z, meta);
        world.setBlockToAir(x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_)
    {
        super.breakBlock(world, x, y, z, block, p_149749_6_);
        breakStructure(world, x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        ret.add(new ItemStack(Item.getItemFromBlock(this), 1, 0));

        TileEntity te = world.getTileEntity(x, y, z);

        if(te instanceof TileEnderRift)
            ret.add(((TileEnderRift) te).getRiftItem());

        return ret;
    }

}
