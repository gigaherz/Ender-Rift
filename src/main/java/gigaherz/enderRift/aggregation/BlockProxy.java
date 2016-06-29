package gigaherz.enderRift.aggregation;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AutomationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockProxy extends BlockAggragator<TileProxy>
{
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    public BlockProxy(String name)
    {
        super(name, Material.IRON, MapColor.GRAY);
        setSoundType(SoundType.METAL);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockProxy");
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
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileProxy createTileEntity(World world, IBlockState state)
    {
        return new TileProxy();
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
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
                .withProperty(NORTH, isConnectable(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, isConnectable(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, isConnectable(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, isConnectable(worldIn, pos, EnumFacing.EAST))
                .withProperty(UP, isConnectable(worldIn, pos, EnumFacing.UP))
                .withProperty(DOWN, isConnectable(worldIn, pos, EnumFacing.DOWN));
    }

    private boolean isConnectable(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te instanceof TileAggregator)
            return true;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
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
            ((TileProxy) world.getTileEntity(pos)).broadcastDirty();
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
}
