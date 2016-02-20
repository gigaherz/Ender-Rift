package gigaherz.enderRift;

import gigaherz.enderRift.blocks.*;
import gigaherz.enderRift.gui.GuiHandler;
import gigaherz.enderRift.items.ItemEnderRift;
import gigaherz.enderRift.network.SetScrollPosition;
import gigaherz.enderRift.network.SetSortMode;
import gigaherz.enderRift.network.SetSpecialSlot;
import gigaherz.enderRift.recipe.RecipesRiftDuplication;
import gigaherz.enderRift.rift.RiftStructure;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(name = EnderRiftMod.NAME,
        modid = EnderRiftMod.MODID,
        version = EnderRiftMod.VERSION,
        dependencies = "after:Waila")
public class EnderRiftMod
{
    public static final String NAME = "Ender-Rift";
    public static final String MODID = "enderrift";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "enderrift";

    public static BlockEnderRift rift;
    public static BlockStructure structure;
    public static Block riftInterface;
    public static Block generator;
    public static Block browser;
    public static Block extension;
    public static Item riftOrb;

    public static CreativeTabs tabEnderRift;

    @Mod.Instance(value = EnderRiftMod.MODID)
    public static EnderRiftMod instance;

    @SidedProxy(clientSide = "gigaherz.enderRift.client.ClientProxy", serverSide = "gigaherz.enderRift.server.ServerProxy")
    public static IModProxy proxy;

    public static SimpleNetworkWrapper channel;

    private GuiHandler guiHandler = new GuiHandler();

    public static final Logger logger = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        ConfigValues.readConfig(config);

        tabEnderRift = new CreativeTabs("tabEnderRift")
        {
            @Override
            public Item getTabIconItem()
            {
                return riftOrb;
            }
        };

        riftOrb = new ItemEnderRift().setUnlocalizedName(MODID + ".itemEnderRift");
        GameRegistry.registerItem(riftOrb, "itemEnderRift");

        rift = new BlockEnderRift();
        GameRegistry.registerBlock(rift, "blockEnderRift");

        structure = new BlockStructure();
        GameRegistry.registerBlock(structure, "blockStructure");

        riftInterface = new BlockInterface();
        GameRegistry.registerBlock(riftInterface, "blockInterface");

        browser = new BlockBrowser();
        GameRegistry.registerBlock(browser, "blockBrowser");

        generator = new BlockGenerator();
        GameRegistry.registerBlock(generator, "blockGenerator");

        extension = new BlockProxy();
        GameRegistry.registerBlock(extension, "blockProxy");

        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, "tileStructureCorner");
        GameRegistry.registerTileEntity(TileInterface.class, "tileInterface");
        GameRegistry.registerTileEntity(TileBrowser.class, "tileBrowser");
        GameRegistry.registerTileEntity(TileGenerator.class, "tileGenerator");
        GameRegistry.registerTileEntity(TileProxy.class, "tileProxy");

        RecipeSorter.register(MODID + ":rift_duplication", RecipesRiftDuplication.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SetSpecialSlot.Handler.class, SetSpecialSlot.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(SetScrollPosition.Handler.class, SetScrollPosition.class, messageNumber++, Side.SERVER);
        channel.registerMessage(SetSortMode.Handler.class, SetSortMode.class, messageNumber++, Side.SERVER);
        logger.debug("Final message number: " + messageNumber);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        RiftStructure.init();

        // Recipes
        GameRegistry.addRecipe(new ItemStack(riftOrb),
                "aba",
                "bcb",
                "aba",
                'a', Items.magma_cream,
                'b', Items.ender_pearl,
                'c', Items.ender_eye);

        GameRegistry.addRecipe(new ItemStack(rift),
                "oho",
                "r r",
                "oco",
                'o', Blocks.obsidian,
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'c', Blocks.ender_chest);

        GameRegistry.addRecipe(new ItemStack(riftInterface),
                "iri",
                "rhr",
                "iri",
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'i', Blocks.iron_block);

        GameRegistry.addRecipe(new ItemStack(browser),
                "igi",
                "gfg",
                "iri",
                'f', riftInterface,
                'g', Items.glowstone_dust,
                'r', Items.redstone,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(generator),
                "iri",
                "rwr",
                "ifi",
                'f', Blocks.furnace,
                'w', Items.water_bucket,
                'r', Items.redstone,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new RecipesRiftDuplication());

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.WailaProviders.callbackRegister");
    }
}
