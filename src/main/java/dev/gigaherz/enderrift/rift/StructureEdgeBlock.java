package dev.gigaherz.enderrift.rift;

import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public class StructureEdgeBlock extends Block
{
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty BASE = BooleanProperty.create("base");

    public static final VoxelShape SHAPE_BASE = Block.box(0, 0, 0, 16, 4, 16);
    public static final VoxelShape SHAPE_EW = Block.box(0, 4, 4, 16, 12, 12);
    public static final VoxelShape SHAPE_NS = Block.box(4, 4, 0, 12, 12, 16);
    public static final VoxelShape SHAPE_UD = Block.box(4, 0, 4, 12, 16, 12);
    public static final VoxelShape SHAPE_EWB = Shapes.or(SHAPE_EW, SHAPE_BASE);
    public static final VoxelShape SHAPE_NSB = Shapes.or(SHAPE_NS, SHAPE_BASE);


    public static final VoxelShape SHAPE_CORNER = Shapes.or(
            Block.box(0, 3, 3, 16, 13, 13),
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(3, 3, 0, 13, 13, 16)
    );
    public static final VoxelShape CORNER_WITH_BASE = Shapes.or(SHAPE_CORNER, SHAPE_BASE);

    private static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize((state) -> {
        return switch (state.getValue(AXIS))
        { // base center
            case X -> state.getValue(BASE) ? SHAPE_EWB : SHAPE_EW;
            case Y -> SHAPE_UD;
            case Z -> state.getValue(BASE) ? SHAPE_NSB : SHAPE_NS;
        };
    });

    public StructureEdgeBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(BASE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AXIS, BASE);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return SHAPE_CACHE.apply(state);
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this)
        {
            super.onRemove(state, worldIn, pos, newState, isMoving);
            return;
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
        RiftStructure.breakStructure(worldIn, pos);
    }

    @Deprecated
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        return Lists.newArrayList(new ItemStack(RiftStructure.getOriginalBlock(state)));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player)
    {
        return new ItemStack(RiftStructure.getOriginalBlock(world, pos));
    }

    public BlockState edgeState(Direction.Axis type2, boolean base)
    {
        return defaultBlockState()
                .setValue(StructureEdgeBlock.AXIS, type2)
                .setValue(StructureEdgeBlock.BASE, base);
    }
}