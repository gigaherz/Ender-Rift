package gigaherz.enderRift.automation.driver;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import gigaherz.enderRift.automation.BlockAggregator;
import gigaherz.enderRift.automation.TileAggregator;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDriver extends BlockAggregator<TileDriver>
{
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(3.5f / 16, 3.5f / 16, 3.5f / 16, 12.5f / 16, 12.5f / 16, 12.5f / 16);
    private static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(6 / 16f, 6 / 16f, 0 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 16 / 16f);
    private static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 16 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(0 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_UP = new AxisAlignedBB(6 / 16f, 6 / 16f, 6 / 16f, 10 / 16f, 16 / 16f, 10 / 16f);
    private static final AxisAlignedBB BOUNDS_DOWN = new AxisAlignedBB(6 / 16f, 0 / 16f, 6 / 16f, 10 / 16f, 10 / 16f, 10 / 16f);

    public BlockDriver(String name)
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
    public TileDriver createTileEntity(World world, IBlockState state)
    {
        return new TileDriver();
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
                .withProperty(NORTH, isConnectablePower(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, isConnectablePower(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, isConnectablePower(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, isConnectablePower(worldIn, pos, EnumFacing.EAST))
                .withProperty(UP, isConnectablePower(worldIn, pos, EnumFacing.UP))
                .withProperty(DOWN, isConnectablePower(worldIn, pos, EnumFacing.DOWN));
    }

    @Override
    protected void recheckNeighbour(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        // Do nothing, we don't connect with inventories.
    }

    private boolean isConnectablePower(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        return te instanceof TileAggregator
                || (te != null && AutomationHelper.isPowerSource(te, facing.getOpposite()));
    }
}
