package gigaherz.enderRift;

import com.google.common.collect.Maps;
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
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

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

        riftOrb = new ItemEnderRift("rift_orb");
        GameRegistry.register(riftOrb);
        addAlternativeName(riftOrb, "itemEnderRift");

        rift = new BlockEnderRift("rift");
        GameRegistry.register(rift);
        GameRegistry.register(rift.createItemBlock());
        GameRegistry.registerTileEntity(TileEnderRift.class, rift.getRegistryName().toString());
        addAlternativeName(rift, "blockEnderRift");
        addAlternativeName(TileEnderRift.class, "tileEnderRift");

        structure = new BlockStructure("rift_structure");
        GameRegistry.register(structure);
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, location("rift_structure_corner").toString());
        addAlternativeName(structure, "blockStructure");
        addAlternativeName(TileEnderRiftCorner.class, "tileStructureCorner");

        riftInterface = new BlockInterface("interface");
        GameRegistry.register(riftInterface);
        GameRegistry.register(riftInterface.createItemBlock());
        GameRegistry.registerTileEntity(TileInterface.class, riftInterface.getRegistryName().toString());
        addAlternativeName(riftInterface, "blockInterface");
        addAlternativeName(TileInterface.class, "tileInterface");

        browser = new BlockBrowser("browser");
        GameRegistry.register(browser);
        GameRegistry.register(browser.createItemBlock());
        GameRegistry.registerTileEntity(TileBrowser.class, browser.getRegistryName().toString());
        addAlternativeName(browser, "blockBrowser");
        addAlternativeName(TileBrowser.class, "tileBrowser");

        extension = new BlockProxy("proxy");
        GameRegistry.register(extension);
        GameRegistry.register(extension.createItemBlock());
        GameRegistry.registerTileEntity(TileProxy.class, extension.getRegistryName().toString());
        addAlternativeName(extension, "blockProxy");
        addAlternativeName(TileProxy.class, "tileProxy");

        if (ConfigValues.EnableRudimentaryGenerator)
        {
            generator = new BlockGenerator("generator");
            GameRegistry.register(generator);
            GameRegistry.register(generator.createItemBlock());
            GameRegistry.registerTileEntity(TileGenerator.class, generator.getRegistryName().toString());
            addAlternativeName(generator, "blockGenerator");
            addAlternativeName(TileGenerator.class, "tileGenerator");
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

    Map<String, Class<? extends TileEntity >> nameToClassMap = ReflectionHelper.getPrivateValue(TileEntity.class, null, "field_145855_i", "nameToClassMap");
    private void addAlternativeName(Class<? extends TileEntity> clazz, String altName)
    {
        nameToClassMap.put(altName, clazz);
    }

    private Map<ResourceLocation, Item> upgradeItemNames = Maps.newHashMap();
    private void addAlternativeName(Item item, String altName)
    {
        upgradeItemNames.put(new ResourceLocation(MODID, altName), item);
    }

    private Map<ResourceLocation, Block> upgradeBlockNames = Maps.newHashMap();
    private void addAlternativeName(Block block, String altName)
    {
        upgradeBlockNames.put(new ResourceLocation(MODID, altName), block);
        Item item = Item.getItemFromBlock(block);
        if (item != null)
            addAlternativeName(item, altName);
    }

    @Mod.EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent ev)
    {
        for (FMLMissingMappingsEvent.MissingMapping missing : ev.get())
        {
            if (missing.type == GameRegistry.Type.ITEM
                    && upgradeItemNames.containsKey(missing.resourceLocation))
            {
                missing.remap(upgradeItemNames.get(missing.resourceLocation));
            }

            if (missing.type == GameRegistry.Type.BLOCK
                    && upgradeBlockNames.containsKey(missing.resourceLocation))
            {
                missing.remap(upgradeBlockNames.get(missing.resourceLocation));
            }
        }
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

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
