package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.BlockAggregator;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockProxy extends BlockAggregator<TileProxy>
{
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4 / 16f, 4 / 16f, 4 / 16f, 12 / 16f, 12 / 16f, 12 / 16f);
    private static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(6 / 16f, 6 / 16f, 0 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 16 / 16f);
    private static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 16 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(0 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_UP = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 16 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_DOWN = new AxisAlignedBB(6 / 16f, 0 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);

    public BlockProxy(String name)
    {
        super(name, Material.IRON, MapColor.GRAY);
        setSoundType(SoundType.METAL);
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setDefaultState(blockState.getBaseState()
                .withProperty(NORTH, false)
                .withProperty(SOUTH, false)
                .withProperty(WEST, false)
                .withProperty(EAST, false)
                .withProperty(UP, false)
                .withProperty(DOWN, false));
        setHardness(3.0F);
        setResistance(8.0F);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public TileProxy createTileEntity(World world, IBlockState state)
    {
        return new TileProxy();
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        state = state.getActualState(source, pos);

        AxisAlignedBB bb = BOUNDS;
        if (state.getValue(NORTH)) bb = bb.union(BOUNDS_NORTH);
        if (state.getValue(SOUTH)) bb = bb.union(BOUNDS_SOUTH);
        if (state.getValue(EAST)) bb = bb.union(BOUNDS_EAST);
        if (state.getValue(WEST)) bb = bb.union(BOUNDS_WEST);
        if (state.getValue(UP)) bb = bb.union(BOUNDS_UP);
        if (state.getValue(DOWN)) bb = bb.union(BOUNDS_DOWN);
        return bb;
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Deprecated
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state
                .withProperty(NORTH, isConnectableAutomation(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, isConnectableAutomation(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, isConnectableAutomation(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, isConnectableAutomation(worldIn, pos, EnumFacing.EAST))
                .withProperty(UP, isConnectableAutomation(worldIn, pos, EnumFacing.UP))
                .withProperty(DOWN, isConnectableAutomation(worldIn, pos, EnumFacing.DOWN));
    }
}
