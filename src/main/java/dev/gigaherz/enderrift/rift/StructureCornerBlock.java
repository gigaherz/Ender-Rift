package dev.gigaherz.enderrift.rift;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.automation.AggregatorBlock;
import net.minecraft.Util;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;
import java.util.function.Function;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class StructureCornerBlock extends AggregatorBlock<StructureCornerBlockEntity>
{
    public static final EnumProperty<Corner> CORNER = EnumProperty.create("corner", Corner.class);
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
        return state.getValue(BASE) ? CORNER_WITH_BASE : SHAPE_CORNER;
    });

    public StructureCornerBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(CORNER, StructureCornerBlock.Corner.values[0])
                .setValue(BASE, false));
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new StructureCornerBlockEntity(pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_)
    {
        return BaseEntityBlock.createTickerHelper(p_153214_, StructureCornerBlockEntity.TYPE, StructureCornerBlockEntity::tickStatic);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(CORNER, BASE);
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        return Lists.newArrayList(new ItemStack(RiftStructure.getOriginalBlock(state)));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    {
        return new ItemStack(RiftStructure.getOriginalBlock(world, pos));
    }

    public BlockState cornerState(Corner corner, boolean base)
    {
        return defaultBlockState()
                .setValue(StructureCornerBlock.CORNER, corner)
                .setValue(StructureCornerBlock.BASE, base);
    }

    public enum Type1 implements StringRepresentable
    {
        NORMAL("normal"),
        CORNER("corner");

        private final String name;

        Type1(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }

        public static Type1[] values = values();
    }

    public enum Corner implements StringRepresentable
    {
        NE("ne"),
        NW("nw"),
        SE("se"),
        SW("sw");

        private final String name;

        Corner(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }

        public static Corner[] values = values();
    }
}