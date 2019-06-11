package gigaherz.enderRift.rift;

import com.google.common.collect.Lists;
import gigaherz.enderRift.automation.AggregatorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;

import javax.annotation.Nullable;
import java.util.List;

public class StructureBlock extends AggregatorBlock<StructureTileEntity>
{
    public static final EnumProperty<Type1> TYPE1 = EnumProperty.create("type1", Type1.class);
    public static final EnumProperty<Type2> TYPE2 = EnumProperty.create("type2", Type2.class);
    public static final EnumProperty<Corner> CORNER = EnumProperty.create("corner", Corner.class);
    public static final BooleanProperty BASE = BooleanProperty.create("base");

    public StructureBlock(Block.Properties properties)
    {
        super(properties);
        setDefaultState(this.getStateContainer().getBaseState()
                .with(TYPE1, StructureBlock.Type1.NORMAL).with(TYPE2, StructureBlock.Type2.NONE)
                .with(CORNER, StructureBlock.Corner.values[0]).with(BASE, false));
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return state.get(TYPE1) == Type1.CORNER;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new StructureTileEntity();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(TYPE1, TYPE2, CORNER, BASE);
    }

    @Deprecated
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos)
    {
        if (state.get(TYPE1) == Type1.NORMAL)
        {
            Type2 d = state.get(TYPE2);
            switch (d)
            {
                case NONE: // base center
                    return new AxisAlignedBB(
                            0 / 16.0f, 0 / 16.0f, 0 / 16.0f,
                            16 / 16.0f, 4 / 16.0f, 16 / 16.0f);
                case SIDE_EW:
                    if (!state.get(BASE))
                        return new AxisAlignedBB(
                                0 / 16.0f, 4 / 16.0f, 4 / 16.0f,
                                16 / 16.0f, 12 / 16.0f, 12 / 16.0f);
                    else
                        return new AxisAlignedBB(
                                0 / 16.0f, 0 / 16.0f, 0 / 16.0f,
                                16 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                case VERTICAL:
                    return new AxisAlignedBB(
                            4 / 16.0f, 0 / 16.0f, 4 / 16.0f,
                            12 / 16.0f, 16 / 16.0f, 12 / 16.0f);
                case SIDE_NS:
                    if (!state.get(BASE))
                        return new AxisAlignedBB(
                                4 / 16.0f, 4 / 16.0f, 0 / 16.0f,
                                12 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                    else
                        return new AxisAlignedBB(
                                0 / 16.0f, 0 / 16.0f, 0 / 16.0f,
                                16 / 16.0f, 12 / 16.0f, 16 / 16.0f);
            }
        }

        return new AxisAlignedBB(0,0,0,1,1,1);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this)
        {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            return;
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
        RiftStructure.breakStructure(worldIn, pos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        return Lists.newArrayList(new ItemStack(RiftStructure.getOriginalBlock(state)));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return new ItemStack(RiftStructure.getOriginalBlock(world, pos));
    }

    public BlockState cornerState(Corner corner, boolean base)
    {
        return getDefaultState()
                .with(StructureBlock.TYPE1, StructureBlock.Type1.CORNER)
                .with(StructureBlock.CORNER, corner)
                .with(StructureBlock.BASE, base);
    }

    public BlockState edgeState(Type2 type2, boolean base)
    {
        return getDefaultState()
                .with(StructureBlock.TYPE1, StructureBlock.Type1.NORMAL)
                .with(StructureBlock.TYPE2, type2)
                .with(StructureBlock.BASE, base);
    }

    public enum Type1 implements IStringSerializable
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
        public String getName()
        {
            return name;
        }

        public static Type1[] values = values();
    }

    public enum Type2 implements IStringSerializable
    {
        NONE("none"),
        VERTICAL("vertical"),
        SIDE_EW("side_ew"),
        SIDE_NS("side_ns");

        private final String name;

        Type2(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        @Override
        public String getName()
        {
            return name;
        }

        public static Type2[] values = values();
    }

    public enum Corner implements IStringSerializable
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
        public String getName()
        {
            return name;
        }

        public static Corner[] values = values();
    }
}
