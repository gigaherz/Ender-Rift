package gigaherz.enderRift.blocks;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBrowser
        extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockBrowser()
    {
        super(Material.iron, MapColor.stoneColor);
        setStepSound(soundTypeMetal);
        setUnlocalizedName(EnderRiftMod.MODID + ".blockBrowser");
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        setHardness(3.0F);
        setResistance(8.0F);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null)
            te.markDirty();
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        IBlockState state = world.getBlockState(pos);
        if (neighbor.equals(pos.offset(state.getValue(FACING))))
            ((TileBrowser) world.getTileEntity(pos)).broadcastDirty();
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileBrowser();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.VALUES[meta & 7]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileBrowser) || playerIn.isSneaking())
            return false;

        playerIn.openGui(EnderRiftMod.instance, GuiHandler.GUI_BROWSER, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getDefaultState().withProperty(BlockBrowser.FACING, facing.getOpposite());
    }
}
