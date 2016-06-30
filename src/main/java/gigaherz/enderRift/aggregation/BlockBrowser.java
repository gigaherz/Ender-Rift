package gigaherz.enderRift.aggregation;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockBrowser extends BlockAggragator<TileBrowser>
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool CRAFTING = PropertyBool.create("crafting");

    private static final String unlocStandard = EnderRiftMod.MODID + ".blockBrowser";
    private static final String unlocCrafting = EnderRiftMod.MODID + ".blockCraftingBrowser";

    public BlockBrowser(String name)
    {
        super(name, Material.IRON, MapColor.STONE);
        setSoundType(SoundType.METAL);
        setUnlocalizedName(unlocStandard);
        setCreativeTab(EnderRiftMod.tabEnderRift);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setHardness(3.0F);
        setResistance(8.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileBrowser createTileEntity(World world, IBlockState state)
    {
        return new TileBrowser();
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
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
        IBlockState state = world.getBlockState(pos);
        if (neighbor.equals(pos.offset(state.getValue(FACING))))
            world.getTileEntity(pos).markDirty();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, CRAFTING);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.VALUES[meta & 7])
                .withProperty(CRAFTING, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).ordinal()
                | (state.getValue(CRAFTING) ? 8 : 0);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileBrowser) || playerIn.isSneaking())
            return false;

        int which = state.getValue(CRAFTING) ?
                GuiHandler.GUI_BROWSER_CRAFTING :
                GuiHandler.GUI_BROWSER;

        playerIn.openGui(EnderRiftMod.instance, which, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return getDefaultState().withProperty(BlockBrowser.FACING, facing.getOpposite()).withProperty(CRAFTING, meta != 0);
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new AsItem(this);
    }

    public static class AsItem extends ItemBlock
    {
        public AsItem(Block block)
        {
            super(block);
            setRegistryName(block.getRegistryName());
            setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage)
        {
            return damage & 1;
        }

        @Override
        public String getUnlocalizedName(ItemStack stack)
        {
            return "tile." + ((stack.getMetadata() != 0) ? unlocCrafting : unlocStandard);
        }

        @Override
        public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
        {
            subItems.add(new ItemStack(itemIn, 1, 0));
            subItems.add(new ItemStack(itemIn, 1, 1));
        }
    }
}
