package gigaherz.enderRift.blocks;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.RiftStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockStructure
        extends BlockRegistered
{
    public static final PropertyEnum<Type1> TYPE1 = PropertyEnum.create("type1", Type1.class);
    public static final PropertyEnum<Type2> TYPE2 = PropertyEnum.create("type2", Type2.class);
    public static final PropertyEnum<Corner> CORNER = PropertyEnum.create("corner", Corner.class);
    public static final PropertyBool BASE = PropertyBool.create("base");

    public BlockStructure(String name)
    {
        super(name, Material.ROCK);
        setSoundType(SoundType.METAL);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockStructure");
        setDefaultState(this.blockState.getBaseState()
                .withProperty(TYPE1, Type1.NORMAL).withProperty(TYPE2, Type2.NONE)
                .withProperty(CORNER, Corner.values[0]).withProperty(BASE, false));
        setLightOpacity(0);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return state.getValue(TYPE1) == Type1.CORNER;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEnderRiftCorner();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE1, TYPE2, CORNER, BASE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        Type1 type1 = Type1.values[meta >> 3];

        IBlockState state = getDefaultState().withProperty(TYPE1, type1);

        int type2 = (meta >> 1) & 3;
        if (type1 == Type1.CORNER)
            state = state.withProperty(CORNER, Corner.values[type2]);
        else
            state = state.withProperty(TYPE2, Type2.values[type2]);

        return state.withProperty(BASE, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        Type1 type1 = state.getValue(TYPE1);

        int type1i = type1.ordinal();

        int type2;
        if (type1 == Type1.CORNER)
            type2 = state.getValue(CORNER).ordinal();
        else
            type2 = state.getValue(TYPE2).ordinal();

        int base = state.getValue(BASE) ? 1 : 0;

        return (type1i << 3) | (type2 << 1) | base;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        if (state.getValue(TYPE1) == Type1.NORMAL)
        {
            Type2 d = state.getValue(TYPE2);
            switch (d)
            {
                case NONE: // base center
                    return new AxisAlignedBB(
                            0 / 16.0f, 0 / 16.0f, 0 / 16.0f,
                            16 / 16.0f, 4 / 16.0f, 16 / 16.0f);
                case SIDE_EW:
                    if (!state.getValue(BASE))
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
                    if (!state.getValue(BASE))
                        return new AxisAlignedBB(
                                4 / 16.0f, 4 / 16.0f, 0 / 16.0f,
                                12 / 16.0f, 12 / 16.0f, 16 / 16.0f);
                    else
                        return new AxisAlignedBB(
                                0 / 16.0f, 0 / 16.0f, 0 / 16.0f,
                                16 / 16.0f, 12 / 16.0f, 16 / 16.0f);
            }
        }

        return FULL_BLOCK_AABB;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        super.breakBlock(worldIn, pos, state);
        RiftStructure.breakStructure(worldIn, pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        List<ItemStack> ret = Lists.newArrayList();
        ret.add(new ItemStack(RiftStructure.getOriginalBlock((World) world, pos)));
        return ret;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        return new ItemStack(RiftStructure.getOriginalBlock(world, pos));
    }

    public IBlockState cornerState(Corner corner, boolean base)
    {
        return getDefaultState()
                .withProperty(BlockStructure.TYPE1, BlockStructure.Type1.CORNER)
                .withProperty(BlockStructure.CORNER, corner)
                .withProperty(BlockStructure.BASE, base);
    }

    public IBlockState edgeState(Type2 type2, boolean base)
    {
        return getDefaultState()
                .withProperty(BlockStructure.TYPE1, BlockStructure.Type1.NORMAL)
                .withProperty(BlockStructure.TYPE2, type2)
                .withProperty(BlockStructure.BASE, base);
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
