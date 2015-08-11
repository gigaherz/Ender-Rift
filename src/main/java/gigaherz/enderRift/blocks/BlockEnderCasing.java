package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderCasing
        extends Block
{

    public BlockEnderCasing()
    {
        super(Material.rock);
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
        this.setBlockTextureName("enderrift:block_casing");
    }

    public boolean tryCompleteStructure(World world, int x, int y, int z, ItemStack itemStack)
    {
        if(world.getBlock(x, y, z) != EnderRiftMod.blockEnderCasing)
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

        world.setBlock(x, y - 1, z, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x - 1, y, z - 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x + 1, y, z - 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x - 1, y, z + 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x + 1, y, z + 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x, y - 1, z - 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x - 1, y - 1, z, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x + 1, y - 1, z, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x, y - 1, z + 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x, y + 1, z - 1, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x - 1, y + 1, z, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x + 1, y + 1, z, EnderRiftMod.blockStructureInvisible);
        world.setBlock(x, y + 1, z + 1, EnderRiftMod.blockStructureInvisible);

        world.setBlock(x, y, z, EnderRiftMod.blockEnderRift);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if(tagCompound != null && tagCompound.hasKey("RiftId"))
        {
            TileEnderRift rift = (TileEnderRift)world.getTileEntity(x, y, z);
            rift.riftId = tagCompound.getInteger("RiftId");
        }

        return true;
    }
}
