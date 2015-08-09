package gigaherz.enderRift;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gigaherz.enderRift.blocks.BlockEnderRift;
import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.items.ItemEnderRift;
import gigaherz.enderRift.network.ValueUpdate;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EnderRiftMod.MODID, version = EnderRiftMod.VERSION, dependencies = "after:Waila;after:NotEnoughItems")
public class EnderRiftMod {
    public static final String MODID = "enderRift";
    public static final String VERSION = "1.0";

    public static final String CHANNEL = "EnderRift";

    public static Block blockEnderRift;
    public static Item itemEnderRift;
    public static CreativeTabs tabEnderRift;

    @Mod.Instance(value = EnderRiftMod.MODID)
    public static EnderRiftMod instance;

    @SidedProxy(clientSide = "gigaherz.enderRift.client.ClientProxy", serverSide = "gigaherz.enderRift.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper channel;

    public static final Logger logger = LogManager.getLogger(MODID);

    private void registerNetworkStuff() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        channel.registerMessage(ValueUpdate.Handler.class, ValueUpdate.class, 0, Side.CLIENT);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        tabEnderRift = new CreativeTabs("tabEnderRift") {

            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return itemEnderRift;
            }
        };

        itemEnderRift = new ItemEnderRift().setUnlocalizedName("itemEnderRift");
        GameRegistry.registerItem(itemEnderRift, "itemEnderRift");

        blockEnderRift = new BlockEnderRift().setHardness(0.5F).setStepSound(Block.soundTypeWood).setBlockName("blockEnderRift");
        GameRegistry.registerBlock(blockEnderRift, "blockEnderRift");

        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");

        registerNetworkStuff();


    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        proxy.registerRenderers();

        // Recipes
        GameRegistry.addRecipe(new ItemStack(itemEnderRift, 1),
                "aba",
                "bcb",
                "aba",
                'a', Items.magma_cream,
                'b', Items.ender_pearl,
                'c', Items.ender_eye);

        GameRegistry.addRecipe(new ItemStack(blockEnderRift, 1),
                "oho",
                "rer",
                "oco",
                'o', Blocks.obsidian,
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'c', Blocks.ender_chest,
                'e', itemEnderRift);

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.WailaProvider.callbackRegister");
    }
}
