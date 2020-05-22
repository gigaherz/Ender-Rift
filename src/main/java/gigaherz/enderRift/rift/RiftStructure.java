package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.storage.RiftStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RiftStructure
{
    static Block[] StructurePattern;
    static BlockState[] StructureStates;

    public static void init()
    {
        StructurePattern = new Block[]{
                // Bottom
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,

                // Middle
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                null, EnderRiftMod.EnderRiftBlocks.RIFT, null,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,

                // Top
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
                Blocks.REDSTONE_BLOCK, null, Blocks.REDSTONE_BLOCK,
                Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.IRON_BLOCK,
        };

        StructureStates = new BlockState[]{

                // Bottom
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.NW, true),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_EW, true),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.NE, true),


                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_NS, true),
                null,
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_NS, true),

                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.SW, true),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_EW, true),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.SE, true),

                // Middle
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.VERTICAL, false),
                null,
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.VERTICAL, false),

                null, null, null,

                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.VERTICAL, false),
                null,
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.VERTICAL, false),

                // Top
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.NW, false),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_EW, false),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.NE, false),

                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_NS, false),
                null,
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_NS, false),

                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.SW, false),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.edgeState(StructureBlock.Type2.SIDE_EW, false),
                EnderRiftMod.EnderRiftBlocks.STRUCTURE.cornerState(StructureBlock.Corner.SE, false),
        };
    }

    public static boolean duplicateOrb(World world, BlockPos pos, PlayerEntity player)
    {
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof RiftTileEntity))
            return false;

        ItemStack stack = ((RiftTileEntity) te).getRiftItem();

        InventoryHelper.spawnItemStack(world, player.posX, player.posY + 0.5f, player.posZ, stack);

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
                    if (world.getBlockState(pos2).getBlock() == EnderRiftMod.EnderRiftBlocks.RIFT)
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
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.EnderRiftBlocks.RIFT)
            return false;

        if (state.get(RiftBlock.ASSEMBLED))
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
                            BlockState st = world.getBlockState(bp);
                            if (!w.isAir(st, world, bp))
                                return false;
                        }
                        else if (b != EnderRiftMod.EnderRiftBlocks.RIFT)
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

    private static void buildStructure(World world, BlockPos pos, ItemStack itemStack, BlockState state)
    {
        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    BlockState bs = StructureStates[yy * 9 + zz * 3 + xx];
                    if (bs == null)
                        continue;

                    BlockPos bp = pos.add(xx - 1, yy - 1, zz - 1);
                    world.setBlockState(bp, bs);
                }
            }
        }

        world.setBlockState(pos, state.with(RiftBlock.ASSEMBLED, true));

        RiftTileEntity rift = (RiftTileEntity) world.getTileEntity(pos);

        CompoundNBT tagCompound = itemStack.getTag();

        if (tagCompound != null && tagCompound.contains("RiftId"))
        {
            rift.assemble(tagCompound.getInt("RiftId"));
        }
        else
        {
            rift.assemble(RiftStorage.get(world).getNextRiftId());
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
                    if (b != null && b != Blocks.AIR && b != EnderRiftMod.EnderRiftBlocks.RIFT)
                    {
                        BlockPos bp = pos.add(xx - 1, yy - 1, zz - 1);
                        if (world.getBlockState(bp).getBlock() == EnderRiftMod.EnderRiftBlocks.STRUCTURE)
                            world.setBlockState(bp, b.getDefaultState());
                    }
                }
            }
        }

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == EnderRiftMod.EnderRiftBlocks.RIFT && state.get(RiftBlock.ASSEMBLED))
        {
            world.setBlockState(pos, state.with(RiftBlock.ASSEMBLED, false));

            RiftTileEntity rift = (RiftTileEntity) world.getTileEntity(pos);

            ItemStack stack = rift.getRiftItem();
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);

            rift.unassemble();
        }
    }

    public static Block getOriginalBlock(BlockState state)
    {
        if (state.getBlock() == EnderRiftMod.EnderRiftBlocks.STRUCTURE)
        {
            if (state.get(StructureBlock.TYPE1) == StructureBlock.Type1.CORNER)
            {
                return StructurePattern[0];
            }
            else
            {
                return StructurePattern[1];
            }
        }
        else
        {
            return StructurePattern[13];
        }
    }

    public static Block getOriginalBlock(IBlockReader worldIn, BlockPos pos)
    {
        for (int yy = 0; yy <= 2; yy++)
        {
            for (int zz = 0; zz <= 2; zz++)
            {
                for (int xx = 0; xx <= 2; xx++)
                {
                    BlockPos pos2 = pos.add(1 - xx, 1 - yy, 1 - zz);
                    if (worldIn.getBlockState(pos2).getBlock() == EnderRiftMod.EnderRiftBlocks.RIFT)
                    {
                        return StructurePattern[yy * 9 + zz * 3 + xx];
                    }
                }
            }
        }
        return null;
    }
}
