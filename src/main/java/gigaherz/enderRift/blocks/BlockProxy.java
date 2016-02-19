package gigaherz.enderRift.blocks;

import gigaherz.api.automation.AutomationHelper;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockProxy extends Block
{
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    public BlockProxy()
    {
        super(Material.iron, MapColor.grayColor);
        setStepSound(soundTypeMetal);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockProxy");
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setDefaultState(blockState.getBaseState()
                .withProperty(NORTH, false)
                .withProperty(SOUTH, false)
                .withProperty(WEST, false)
                .withProperty(EAST, false)
                .withProperty(UP, false)
                .withProperty(DOWN, false));
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        setHardness(3.0F);
        setResistance(8.0F);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState updated = state
                .withProperty(NORTH, isConnectable(worldIn, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, isConnectable(worldIn, pos, EnumFacing.SOUTH))
                .withProperty(WEST, isConnectable(worldIn, pos, EnumFacing.WEST))
                .withProperty(EAST, isConnectable(worldIn, pos, EnumFacing.EAST))
                .withProperty(UP, isConnectable(worldIn, pos, EnumFacing.UP))
                .withProperty(DOWN, isConnectable(worldIn, pos, EnumFacing.DOWN));
        return updated;
    }

    private boolean isConnectable(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));

        if (te instanceof IBrowserExtension)
            return true;

        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null)
            te.markDirty();
    }

    private boolean isUpdateSource(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        TileEntity te = worldIn.getTileEntity(pos.offset(facing));
        return AutomationHelper.isAutomatable(te, facing.getOpposite());
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        if (isUpdateSource(world, pos, fromNeighbour(pos, neighbor)))
            ((TileProxy) world.getTileEntity(pos)).broadcastDirty();
    }

    EnumFacing fromNeighbour(BlockPos a, BlockPos b)
    {
        BlockPos diff = b.subtract(a);
        return EnumFacing.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileProxy();
    }
}
