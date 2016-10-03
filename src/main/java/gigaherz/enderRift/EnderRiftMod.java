package gigaherz.enderRift;

import gigaherz.enderRift.automation.browser.BlockBrowser;
import gigaherz.enderRift.automation.browser.TileBrowser;
import gigaherz.enderRift.automation.iface.BlockInterface;
import gigaherz.enderRift.automation.iface.TileInterface;
import gigaherz.enderRift.automation.proxy.BlockProxy;
import gigaherz.enderRift.automation.proxy.TileProxy;
import gigaherz.enderRift.common.BlockRegistered;
import gigaherz.enderRift.common.GuiHandler;
import gigaherz.enderRift.generator.BlockGenerator;
import gigaherz.enderRift.generator.TileGenerator;
import gigaherz.enderRift.network.ClearCraftingGrid;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.SetVisibleSlots;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.rift.RecipeRiftDuplication;
import gigaherz.enderRift.rift.*;
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
        dependencies = "after:Waila;required-after:Forge@[12.16.0.1825,)",
        updateJSON = "https://raw.githubusercontent.com/gigaherz/Ender-Rift/master/update.json")
public class EnderRiftMod
{
    public static final String NAME = "Ender-Rift";
    public static final String MODID = "enderrift";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "enderrift";

    public static BlockEnderRift rift;
    public static BlockStructure structure;
    public static BlockRegistered riftInterface;
    public static BlockRegistered generator;
    public static BlockBrowser browser;
    public static BlockRegistered extension;
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

        riftOrb = new ItemEnderRift("itemEnderRift");
        GameRegistry.register(riftOrb);

        rift = new BlockEnderRift("blockEnderRift");
        GameRegistry.register(rift);
        GameRegistry.register(rift.createItemBlock());
        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");

        structure = new BlockStructure("blockStructure");
        GameRegistry.register(structure);
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, "tileStructureCorner");

        riftInterface = new BlockInterface("blockInterface");
        GameRegistry.register(riftInterface);
        GameRegistry.register(riftInterface.createItemBlock());
        GameRegistry.registerTileEntity(TileInterface.class, "tileInterface");

        browser = new BlockBrowser("blockBrowser");
        GameRegistry.register(browser);
        GameRegistry.register(browser.createItemBlock());
        GameRegistry.registerTileEntity(TileBrowser.class, "tileBrowser");

        extension = new BlockProxy("blockProxy");
        GameRegistry.register(extension);
        GameRegistry.register(extension.createItemBlock());
        GameRegistry.registerTileEntity(TileProxy.class, "tileProxy");

        if (ConfigValues.EnableRudimentaryGenerator)
        {
            generator = new BlockGenerator("blockGenerator");
            GameRegistry.register(generator);
            GameRegistry.register(generator.createItemBlock());
            GameRegistry.registerTileEntity(TileGenerator.class, "tileGenerator");
        }

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SendSlotChanges.Handler.class, SendSlotChanges.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(SetVisibleSlots.Handler.class, SetVisibleSlots.class, messageNumber++, Side.SERVER);
        channel.registerMessage(UpdateField.Handler.class, UpdateField.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(ClearCraftingGrid.Handler.class, ClearCraftingGrid.class, messageNumber++, Side.SERVER);
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
                'a', Items.MAGMA_CREAM,
                'b', Items.ENDER_PEARL,
                'c', Items.ENDER_EYE);

        GameRegistry.addRecipe(new ItemStack(rift),
                "oho",
                "r r",
                "oco",
                'o', Blocks.OBSIDIAN,
                'h', Blocks.HOPPER,
                'r', Blocks.REDSTONE_BLOCK,
                'c', Blocks.ENDER_CHEST);

        GameRegistry.addRecipe(new ItemStack(extension),
                "iri",
                "rhr",
                "iri",
                'h', Blocks.HOPPER,
                'r', Items.REDSTONE,
                'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(riftInterface),
                "ir ",
                "rer",
                "ir ",
                'e', extension,
                'r', Items.REDSTONE,
                'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(browser),
                "ig ",
                "geg",
                "ig ",
                'e', extension,
                'g', Items.GLOWSTONE_DUST,
                'i', Items.IRON_INGOT);

        if (ConfigValues.EnableRudimentaryGenerator)
        {
            GameRegistry.addRecipe(new ItemStack(generator),
                    "iri",
                    "rwr",
                    "ifi",
                    'f', Blocks.FURNACE,
                    'w', Items.WATER_BUCKET,
                    'r', Items.REDSTONE,
                    'i', Items.IRON_INGOT);
        }

        GameRegistry.addRecipe(new ItemStack(browser, 1, 1),
                "gdg",
                "dbd",
                "gcg",
                'g', Items.GOLD_INGOT,
                'd', Items.DIAMOND,
                'c', Blocks.CRAFTING_TABLE,
                'b', new ItemStack(browser, 1, 0));

        GameRegistry.addRecipe(new RecipeRiftDuplication());
        RecipeSorter.register(MODID + ":rift_duplication", RecipeRiftDuplication.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.integration.WailaProviders.callbackRegister");
    }
}
