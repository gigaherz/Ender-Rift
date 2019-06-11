package gigaherz.enderRift.rift;

import gigaherz.enderRift.automation.BlockAggregator;
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

import javax.annotation.Nullable;

public class BlockStructure extends BlockAggregator<TileEnderRiftCorner>
{
    public static final EnumProperty<Type1> TYPE1 = EnumProperty.create("type1", Type1.class);
    public static final EnumProperty<Type2> TYPE2 = EnumProperty.create("type2", Type2.class);
    public static final EnumProperty<Corner> CORNER = EnumProperty.create("corner", Corner.class);
    public static final BooleanProperty BASE = BooleanProperty.create("base");

    public BlockStructure(Block.Properties properties)
    {
        super(properties);
        setDefaultState(this.getStateContainer().getBaseState()
                .with(TYPE1, BlockStructure.Type1.NORMAL).with(TYPE2, BlockStructure.Type2.NONE)
                .with(CORNER, BlockStructure.Corner.values[0]).with(BASE, false));
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
        return new TileEnderRiftCorner();
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

    /*@Override
    public void breakBlock(World worldIn, BlockPos pos, BlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        RiftStructure.breakStructure(worldIn, pos);
    }*/

    /*@Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, BlockState state, int fortune)
    {
        drops.add(new ItemStack(RiftStructure.getOriginalBlock((World) world, pos)));
    }*/

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return new ItemStack(RiftStructure.getOriginalBlock(world, pos));
    }

    public BlockState cornerState(Corner corner, boolean base)
    {
        return getDefaultState()
                .with(BlockStructure.TYPE1, BlockStructure.Type1.CORNER)
                .with(BlockStructure.CORNER, corner)
                .with(BlockStructure.BASE, base);
    }

    public BlockState edgeState(Type2 type2, boolean base)
    {
        return getDefaultState()
                .with(BlockStructure.TYPE1, BlockStructure.Type1.NORMAL)
                .with(BlockStructure.TYPE2, type2)
                .with(BlockStructure.BASE, base);
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
