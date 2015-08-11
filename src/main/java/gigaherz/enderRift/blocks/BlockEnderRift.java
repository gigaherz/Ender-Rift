package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.client.SBRHEnderRift;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    public BlockEnderRift()
    {
        super(Material.rock);
    }

    @Override
    public void registerBlockIcons(IIconRegister register)
    {
        this.iconMain = register.registerIcon("enderrift:block_rift");
        this.iconBase = register.registerIcon("enderrift:block_rift_base");
    }

    @Override
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return this.iconMain;
    }

    @Override
    public int getRenderType()
    {
        return SBRHEnderRift.renderId;
    }


    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEnderRift();
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

        if(world.getBlock(x,y,z) == EnderRiftMod.blockEnderRift)
            world.setBlock(x, y, z, EnderRiftMod.blockEnderCasing);
    }

    private void restoreStructureBlockTo(World world, int xx, int yy, int zz, Block bb)
    {
        if(world.getBlock(xx, yy, zz) == EnderRiftMod.blockStructureInvisible)
            world.setBlock(xx, yy, zz, bb);
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

        ret.add(new ItemStack(Item.getItemFromBlock(EnderRiftMod.blockEnderCasing)));

        return ret;
    }
}
