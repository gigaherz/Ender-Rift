package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.storage.RiftStorageWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftStructure
{
    static Block[] StructurePattern;
    static IBlockState[] StructureStates;

    public static void init()
    {
        StructurePattern = new Block[]{
                // Bottom
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,

                // Middle
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                null, EnderRiftMod.rift, null,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,

                // Top
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
        };

        StructureStates = new IBlockState[]{

                // Bottom
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.NW, true),
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, true),
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.NE, true),


                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, true),
                null,
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, true),

                EnderRiftMod.structure.cornerState(BlockStructure.Corner.SW, true),
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, true),
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.SE, true),

                // Middle
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false),
                null,
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false),

                null, null, null,

                EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false),
                null,
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.VERTICAL, false),

                // Top
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.NW, false),
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, false),
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.NE, false),

                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, false),
                null,
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_NS, false),

                EnderRiftMod.structure.cornerState(BlockStructure.Corner.SW, false),
                EnderRiftMod.structure.edgeState(BlockStructure.Type2.SIDE_EW, false),
                EnderRiftMod.structure.cornerState(BlockStructure.Corner.SE, false),
        };
    }

    public static boolean duplicateOrb(World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof TileEnderRift))
            return false;

        ItemStack stack = ((TileEnderRift) te).getRiftItem();

        Entity entity = new EntityItem(world, player.posX, player.posY + 0.5f, player.posZ, stack);
        world.spawnEntity(entity);

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
                    BlockPos bp = pos.add(xx - 1, yy - 1, zz - 1);
                    Block b = StructurePattern[yy * 9 + zz * 3 + xx];
                    Block w = world.getBlockState(bp).getBlock();
                    if (b != null)
                    {
                        if (b == Blocks.AIR)
                        {
                            IBlockState st = world.getBlockState(bp);
                            if (!w.isAir(st, world, bp))
                                return false;
                        }
                        else if (b != EnderRiftMod.rift)
                        {
                            if (b != w)
                                return false;
                        }
                    }
                }
            }
        }

        buildStructure(world, pos, itemStack, state);

        return true;
    }

    private static void buildStructure(World world, BlockPos pos, ItemStack itemStack, IBlockState state)
    {
        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    IBlockState bs = StructureStates[yy * 9 + zz * 3 + xx];
                    if (bs == null)
                        continue;

                    BlockPos bp = pos.add(xx - 1, yy - 1, zz - 1);
                    world.setBlockState(bp, bs);
                }
            }
        }

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
                    if (b != null && b != Blocks.AIR && b != EnderRiftMod.rift)
                    {
                        BlockPos bp = pos.add(xx - 1, yy - 1, zz - 1);
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

            ItemStack stack = rift.getRiftItem();
            Entity entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(entity);

            rift.unassemble();
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
