package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockStructureInvisible
        extends Block
{
    public BlockStructureInvisible()
    {
        super(Material.rock);
        setBlockTextureName(EnderRiftMod.MODID.toLowerCase() + ":block_casing");
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z);

        int d = meta >> 2;
        switch (d)
        {
            case 0:
                setBlockBounds(0 / 16.0f, 0 / 16.0f, 0 / 16.0f, 16 / 16.0f, 4 / 16.0f, 16 / 16.0f);
                break;
            case 1:
                if ((meta & 1) == 1)
                    setBlockBounds(0 / 16.0f, 4 / 16.0f, 4 / 16.0f, 16 / 16.0f, 12 / 16.0f, 12 / 16.0f);
                else
                    setBlockBounds(0 / 16.0f, 0 / 16.0f, 0 / 16.0f, 16 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                break;
            case 2:
                setBlockBounds(4 / 16.0f, 0 / 16.0f, 4 / 16.0f, 12 / 16.0f, 16 / 16.0f, 12 / 16.0f);
                break;
            case 3:
                if ((meta & 2) == 2)
                    setBlockBounds(4 / 16.0f, 4 / 16.0f, 0 / 16.0f, 12 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                else
                    setBlockBounds(0 / 16.0f, 0 / 16.0f, 0 / 16.0f, 16 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                break;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_)
    {
        super.breakBlock(world, x, y, z, block, p_149749_6_);

        for (int yy = -1; yy <= 1; yy++)
        {
            for (int xx = -1; xx <= 1; xx++)
            {
                for (int zz = -1; zz <= 1; zz++)
                {
                    if (world.getBlock(x + xx, y + yy, z + zz) == EnderRiftMod.blockEnderRift)
                    {
                        EnderRiftMod.blockEnderRift.breakStructure(world, x + xx, y + yy, z + zz);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        if (world.getBlock(x + 1, y + 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));
        if (world.getBlock(x, y + 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y + 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));

        if (world.getBlock(x + 1, y + 1, z) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x, y + 1, z) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y + 1, z) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));

        if (world.getBlock(x + 1, y + 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));
        if (world.getBlock(x, y + 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y + 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));

        if (world.getBlock(x + 1, y - 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));
        if (world.getBlock(x, y - 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y - 1, z + 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));

        if (world.getBlock(x + 1, y - 1, z) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y - 1, z) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));

        if (world.getBlock(x + 1, y - 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));
        if (world.getBlock(x, y - 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.redstone_block)));
        if (world.getBlock(x - 1, y - 1, z - 1) == EnderRiftMod.blockEnderRift)
            ret.add(new ItemStack(Item.getItemFromBlock(Blocks.iron_block)));

        return ret;
    }
}
