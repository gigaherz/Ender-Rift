package gigaherz.enderRift.automation.driver;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.BlockAggregator;
import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.automation.capability.AutomationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDriver extends BlockAggregator<TileDriver>
{
    public static final PropertyEnum<ConnectionType> NORTH = PropertyEnum.create("north", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> SOUTH = PropertyEnum.create("south", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> WEST = PropertyEnum.create("west", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> EAST = PropertyEnum.create("east", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> UP = PropertyEnum.create("up", ConnectionType.class);
    public static final PropertyEnum<ConnectionType> DOWN = PropertyEnum.create("down", ConnectionType.class);

    public static final AxisAlignedBB BOUNDS = new AxisAlignedBB(3.5f/16,3.5f/16,3.5f/16,12.5f/16,12.5f/16,12.5f/16);
    public static final AxisAlignedBB BOUNDS_NORTH = new AxisAlignedBB(6/16f,6/16f,0/16f,10/16f,10/16f,10/16f);
    public static final AxisAlignedBB BOUNDS_SOUTH = new AxisAlignedBB(6/16f,6/16f,6/16f,10/16f,10/16f,16/16f);
    public static final AxisAlignedBB BOUNDS_EAST = new AxisAlignedBB(6/16f,6/16f,6/16f,16/16f,10/16f,10/16f);
    public static final AxisAlignedBB BOUNDS_WEST = new AxisAlignedBB(0/16f,6/16f,6/16f,10/16f,10/16f,10/16f);
    public static final AxisAlignedBB BOUNDS_UP = new AxisAlignedBB(6/16f,6/16f,6/16f,10/16f,16/16f,10/16f);
    public static final AxisAlignedBB BOUNDS_DOWN = new AxisAlignedBB(6/16f,0/16f,6/16f,10/16f,10/16f,10/16f);

    public BlockDriver(String name)
    {
        super(name, Material.IRON, MapColor.GRAY);
        setSoundType(SoundType.METAL);
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setDefaultState(blockState.getBaseState()
                .withProperty(NORTH, ConnectionType.NONE)
                .withProperty(SOUTH, ConnectionType.NONE)
                .withProperty(WEST, ConnectionType.NONE)
                .withProperty(EAST, ConnectionType.NONE)
                .withProperty(UP, ConnectionType.NONE)
                .withProperty(DOWN, ConnectionType.NONE));
        setHardness(3.0F);
        setResistance(8.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileDriver createTileEntity(World world, IBlockState state)
    {
        return new TileDriver();
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        state = state.getActualState(source, pos);

        AxisAlignedBB bb = BOUNDS;
        if (state.getValue(NORTH) != ConnectionType.NONE) bb=bb.union(BOUNDS_NORTH);
        if (state.getValue(SOUTH) != ConnectionType.NONE) bb=bb.union(BOUNDS_SOUTH);
        if (state.getValue(EAST) != ConnectionType.NONE) bb=bb.union(BOUNDS_EAST);
        if (state.getValue(WEST) != ConnectionType.NONE) bb=bb.union(BOUNDS_WEST);
        if (state.getValue(UP) != ConnectionType.NONE) bb=bb.union(BOUNDS_UP);
        if (state.getValue(DOWN) != ConnectionType.NONE) bb=bb.union(BOUNDS_DOWN);
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
                .withProperty(NORTH, getConnectionType(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, getConnectionType(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, getConnectionType(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, getConnectionType(worldIn, pos, EnumFacing.EAST))
                .withProperty(UP, getConnectionType(worldIn, pos, EnumFacing.UP))
                .withProperty(DOWN, getConnectionType(worldIn, pos, EnumFacing.DOWN));
    }

    private ConnectionType getConnectionType(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te instanceof TileAggregator)
            return ConnectionType.INVENTORY;

        if (AutomationHelper.isPowerSource(te, facing.getOpposite()))
            return ConnectionType.POWER;

        return ConnectionType.NONE;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        super.neighborChanged(state, worldIn, pos, blockIn);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null)
            te.markDirty();
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange(world, pos, neighbor);
        if (isUpdateSource(world, pos, fromNeighbour(pos, neighbor)))
            ((TileDriver) world.getTileEntity(pos)).broadcastDirty();
    }

    private boolean isUpdateSource(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));
        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    private EnumFacing fromNeighbour(BlockPos a, BlockPos b)
    {
        BlockPos diff = b.subtract(a);
        return EnumFacing.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
    }

    public enum ConnectionType implements IStringSerializable
    {
        NONE("none"),
        INVENTORY("inventory"),
        POWER("power");

        private final String name;

        ConnectionType(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }
}
