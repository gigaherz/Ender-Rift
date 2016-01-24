package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.BlockEnderRift;
import gigaherz.enderRift.blocks.BlockStructure;
import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.storage.RiftStorageWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class RiftStructure
{
    static Block[] StructurePattern;

    public static void init()
    {
        StructurePattern = new Block[] {
                // Bottom
                Blocks.iron_block,     Blocks.redstone_block, Blocks.iron_block,
                Blocks.redstone_block, Blocks.air,            Blocks.redstone_block,
                Blocks.iron_block,     Blocks.redstone_block, Blocks.iron_block,

                // Middle
                Blocks.redstone_block, Blocks.air,            Blocks.redstone_block,
                Blocks.air,            EnderRiftMod.rift,     Blocks.air,
                Blocks.redstone_block, Blocks.air,            Blocks.redstone_block,

                // Top
                Blocks.iron_block,     Blocks.redstone_block, Blocks.iron_block,
                Blocks.redstone_block, Blocks.air,            Blocks.redstone_block,
                Blocks.iron_block,     Blocks.redstone_block, Blocks.iron_block,
        };
    }

    public static boolean duplicateOrb(World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEnderRift))
            return false;

        ItemStack stack = ((TileEnderRift) te).getRiftItem();

        Entity entity = new EntityItem(world, player.posX, player.posY + 0.5f, player.posZ, stack);
        world.spawnEntityInWorld(entity);

        return true;
    }

    public static void breakStructure(World world, BlockPos pos)
    {
        for (int yy = -1; yy <= 1; yy++)
        {
            for (int xx = -1; xx <= 1; xx++)
            {
                for (int zz = -1; zz <= 1; zz++)
                {
                    BlockPos pos2 = pos.add(xx, yy, zz);
                    if (world.getBlockState(pos2).getBlock() == EnderRiftMod.rift)
                    {
                        RiftStructure.dismantle(world, pos2);
                        return;
                    }
                }
            }
        }
    }

    public static boolean assemble(World world, BlockPos pos, ItemStack itemStack)
    {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.rift)
            return false;

        if (state.getValue(BlockEnderRift.ASSEMBLED))
            return false;

        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    BlockPos bp = pos.add(xx-1, yy-1, zz-1);
                    Block b = StructurePattern[yy * 9 + zz * 3 + xx];
                    Block w = world.getBlockState(bp).getBlock();
                    if(b == Blocks.air)
                    {
                        if(!w.isAir(world, bp))
                            return false;
                    }
                    else if (b != EnderRiftMod.rift)
                    {
                        if(b != w)
                            return false;
                    }
                }
            }
        }

        buildStructure(world, pos, itemStack, state);

        return true;
    }

    private static void buildStructure(World world, BlockPos pos, ItemStack itemStack, IBlockState state)
    {
        world.setBlockState(pos.add(-1, -1, -1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.NW, true));
        world.setBlockState(pos.add(+1, -1, -1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.NE, true));
        world.setBlockState(pos.add(-1, -1, +1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.SW, true));
        world.setBlockState(pos.add(+1, -1, +1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.SE, true));
        world.setBlockState(pos.add(-1, +1, -1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.NW, false));
        world.setBlockState(pos.add(+1, +1, -1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.NE, false));
        world.setBlockState(pos.add(-1, +1, +1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.SW, false));
        world.setBlockState(pos.add(+1, +1, +1), EnderRiftMod.structure.cornerState(BlockStructure.Corner.SE, false));

        world.setBlockState(pos.add(0, -1, -1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, true));
        world.setBlockState(pos.add(0, -1, +1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, true));
        world.setBlockState(pos.add(-1, -1, 0), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, true));
        world.setBlockState(pos.add(+1, -1, 0), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, true));

        world.setBlockState(pos.add(-1, 0, -1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false));
        world.setBlockState(pos.add(+1, 0, -1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false));
        world.setBlockState(pos.add(-1, 0, +1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false));
        world.setBlockState(pos.add(+1, 0, +1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false));

        world.setBlockState(pos.add(0, +1, -1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, false));
        world.setBlockState(pos.add(0, +1, +1), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, false));
        world.setBlockState(pos.add(-1, +1, 0), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, false));
        world.setBlockState(pos.add(+1, +1, 0), EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, false));

        world.setBlockState(pos, state.withProperty(BlockEnderRift.ASSEMBLED, true));

        TileEnderRift rift = (TileEnderRift) world.getTileEntity(pos);

        NBTTagCompound tagCompound = itemStack.getTagCompound();

        if (tagCompound != null && tagCompound.hasKey("RiftId"))
        {
            rift.assemble(tagCompound.getInteger("RiftId"));
        }
        else
        {
            rift.assemble(RiftStorageWorldData.get(world).getNextRiftId());
        }
    }

    public static void dismantle(World world, BlockPos pos)
    {
        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    Block b = StructurePattern[yy * 9 + zz * 3 + xx];
                    if(b != Blocks.air && b != EnderRiftMod.rift)
                    {
                        BlockPos bp = pos.add(xx-1, yy-1, zz-1);
                        if (world.getBlockState(bp).getBlock() == EnderRiftMod.structure)
                            world.setBlockState(bp, b.getDefaultState());
                    }
                }
            }
        }

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() == EnderRiftMod.rift && state.getValue(BlockEnderRift.ASSEMBLED))
        {
            world.setBlockState(pos, state.withProperty(BlockEnderRift.ASSEMBLED, false));

            TileEnderRift rift = (TileEnderRift) world.getTileEntity(pos);

            rift.unassemble();

            ItemStack stack = rift.getRiftItem();

            Entity entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntityInWorld(entity);
        }
    }

    public static Block getOriginalBlock(World worldIn, BlockPos pos)
    {
        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    BlockPos pos2 = pos.add(1 - xx, 1 - yy, 1 - zz);
                    if (worldIn.getBlockState(pos2).getBlock() == EnderRiftMod.rift)
                    {
                        return StructurePattern[yy * 9 + zz * 3 + xx];
                    }
                }
            }
        }
        return null;
    }
}
