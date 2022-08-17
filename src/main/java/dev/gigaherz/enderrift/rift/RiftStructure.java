package dev.gigaherz.enderrift.rift;

import java.util.UUID;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class RiftStructure {
    static Block[] StructurePattern;
    static BlockState[] StructureStates;

    public static void init() {
        StructurePattern = new Block[] {
            // Bottom
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            null,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,

            // Middle
            Blocks.REDSTONE_BLOCK,
            null,
            Blocks.REDSTONE_BLOCK,
            null,
            EnderRiftMod.RIFT.get(),
            null,
            Blocks.REDSTONE_BLOCK,
            null,
            Blocks.REDSTONE_BLOCK,

            // Top
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            null,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.IRON_BLOCK,
        };

        StructureStates = new BlockState[] {

            // Bottom
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.NW, true),
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.X, true),
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.NE, true),

            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Z, true),
            null,
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Z, true),

            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.SW, true),
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.X, true),
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.SE, true),

            // Middle
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Y, false),
            null,
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Y, false),

            null,
            null,
            null,

            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Y, false),
            null,
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Y, false),

            // Top
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.NW, false),
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.X, false),
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.NE, false),

            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Z, false),
            null,
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.Z, false),

            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.SW, false),
            EnderRiftMod.STRUCTURE_EDGE.get().edgeState(Direction.Axis.X, false),
            EnderRiftMod.STRUCTURE_CORNER.get().cornerState(StructureCornerBlock.Corner.SE, false),
        };
    }

    public static boolean duplicateOrb(Level world, BlockPos pos, Player player) {
        BlockEntity te = world.getBlockEntity(pos);

        if (!(te instanceof RiftBlockEntity))
            return false;

        ItemStack stack = ((RiftBlockEntity) te).getRiftItem();

        Containers.dropItemStack(world, player.getX(), player.getY() + 0.5f, player.getZ(), stack);

        return true;
    }

    public static void breakStructure(Level world, BlockPos pos) {
        for (int yy = -1; yy <= 1; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                for (int zz = -1; zz <= 1; zz++) {
                    BlockPos pos2 = pos.offset(xx, yy, zz);
                    if (world.getBlockState(pos2).getBlock() == EnderRiftMod.RIFT.get()) {
                        RiftStructure.dismantle(world, pos2);
                        return;
                    }
                }
            }
        }
    }

    public static boolean assemble(Level world, BlockPos pos, ItemStack itemStack) {
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.RIFT.get())
            return false;

        if (state.getValue(RiftBlock.ASSEMBLED))
            return false;

        for (int yy = 0; yy <= 2; yy++) {
            for (int zz = 0; zz <= 2; zz++) {
                for (int xx = 0; xx <= 2; xx++) {
                    BlockPos bp = pos.offset(xx - 1, yy - 1, zz - 1);
                    Block b = StructurePattern[yy * 9 + zz * 3 + xx];
                    Block w = world.getBlockState(bp).getBlock();
                    if (b != null) {
                        if (b == Blocks.AIR) {
                            BlockState st = world.getBlockState(bp);
                            if (!st.isAir())
                                return false;
                        } else if (b != EnderRiftMod.RIFT.get()) {
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

    private static void buildStructure(Level world, BlockPos pos, ItemStack itemStack, BlockState state) {
        for (int yy = 0; yy <= 2; yy++) {
            for (int zz = 0; zz <= 2; zz++) {
                for (int xx = 0; xx <= 2; xx++) {
                    BlockState bs = StructureStates[yy * 9 + zz * 3 + xx];
                    if (bs == null)
                        continue;

                    BlockPos bp = pos.offset(xx - 1, yy - 1, zz - 1);
                    world.setBlockAndUpdate(bp, bs);
                }
            }
        }

        world.setBlockAndUpdate(pos, state.setValue(RiftBlock.ASSEMBLED, true));

        RiftBlockEntity rift = (RiftBlockEntity) world.getBlockEntity(pos);

        CompoundTag tagCompound = itemStack.getTag();

        if (tagCompound != null && tagCompound.contains("RiftId")) {
            UUID id = tagCompound.getUUID("RiftId");
            if (id != null) {
                rift.assemble(RiftStorage.get().getRift(id));
                return;
            }
        }
        rift.assemble(RiftStorage.get().newRift());
    }

    public static void dismantle(Level world, BlockPos pos) {
        for (int yy = 0; yy <= 2; yy++) {
            for (int zz = 0; zz <= 2; zz++) {
                for (int xx = 0; xx <= 2; xx++) {
                    Block b = StructurePattern[yy * 9 + zz * 3 + xx];
                    if (b != null && b != Blocks.AIR && b != EnderRiftMod.RIFT.get()) {
                        BlockPos bp = pos.offset(xx - 1, yy - 1, zz - 1);
                        Block block = world.getBlockState(bp).getBlock();
                        if (block == EnderRiftMod.STRUCTURE_EDGE.get() || block == EnderRiftMod.STRUCTURE_CORNER.get())
                            world.setBlockAndUpdate(bp, b.defaultBlockState());
                    }
                }
            }
        }

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == EnderRiftMod.RIFT.get() && state.getValue(RiftBlock.ASSEMBLED)) {
            world.setBlockAndUpdate(pos, state.setValue(RiftBlock.ASSEMBLED, false));

            RiftBlockEntity rift = (RiftBlockEntity) world.getBlockEntity(pos);

            ItemStack stack = rift.getRiftItem();
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);

            rift.unassemble();
        }
    }

    public static Block getOriginalBlock(BlockState state) {
        if (state.getBlock() == EnderRiftMod.STRUCTURE_CORNER.get()) {
            return StructurePattern[0];
        } else if (state.getBlock() == EnderRiftMod.STRUCTURE_EDGE.get()) {
            return StructurePattern[1];
        } else {
            return StructurePattern[13];
        }
    }

    public static Block getOriginalBlock(BlockGetter worldIn, BlockPos pos) {
        for (int yy = 0; yy <= 2; yy++) {
            for (int zz = 0; zz <= 2; zz++) {
                for (int xx = 0; xx <= 2; xx++) {
                    BlockPos pos2 = pos.offset(1 - xx, 1 - yy, 1 - zz);
                    if (worldIn.getBlockState(pos2).getBlock() == EnderRiftMod.RIFT.get()) {
                        return StructurePattern[yy * 9 + zz * 3 + xx];
                    }
                }
            }
        }
        return null;
    }
}