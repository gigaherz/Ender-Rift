package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.BlockAggregator;
import gigaherz.enderRift.common.GuiHandler;
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

import javax.annotation.Nullable;
import java.util.List;

public class BlockBrowser extends BlockAggregator<TileBrowser>
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool CRAFTING = PropertyBool.create("crafting");

    private static final String unlocStandard = EnderRiftMod.MODID + ".browser";
    private static final String unlocCrafting = EnderRiftMod.MODID + ".crafting_browser";

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
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, CRAFTING);
    }

    @Override
    public TileBrowser createTileEntity(World world, IBlockState state)
    {
        return new TileBrowser();
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return base_state.getValue(FACING) == side.getOpposite();
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack)
    {
        return getDefaultState().withProperty(BlockBrowser.FACING, facing.getOpposite()).withProperty(CRAFTING, meta != 0);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
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
