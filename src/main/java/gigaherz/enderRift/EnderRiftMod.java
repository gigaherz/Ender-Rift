package gigaherz.enderRift;

import gigaherz.common.BlockRegistered;
import gigaherz.enderRift.automation.browser.BlockBrowser;
import gigaherz.enderRift.automation.browser.TileBrowser;
import gigaherz.enderRift.automation.driver.BlockDriver;
import gigaherz.enderRift.automation.driver.TileDriver;
import gigaherz.enderRift.automation.iface.BlockInterface;
import gigaherz.enderRift.automation.iface.TileInterface;
import gigaherz.enderRift.automation.proxy.BlockProxy;
import gigaherz.enderRift.automation.proxy.TileProxy;
import gigaherz.enderRift.common.GuiHandler;
import gigaherz.enderRift.generator.BlockGenerator;
import gigaherz.enderRift.generator.TileGenerator;
import gigaherz.enderRift.network.ClearCraftingGrid;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.SetVisibleSlots;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.rift.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = EnderRiftMod.MODID,
        version = EnderRiftMod.VERSION,
        dependencies = "after:Waila;after:gbook",
        updateJSON = "https://raw.githubusercontent.com/gigaherz/Ender-Rift/master/update.json")
public class EnderRiftMod
{
    public static final String MODID = "enderrift";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "enderrift";

    public static BlockEnderRift rift;
    public static BlockStructure structure;
    public static BlockRegistered riftInterface;
    public static BlockRegistered generator;
    public static BlockRegistered browser;
    public static BlockRegistered extension;
    public static BlockRegistered driver;
    public static Item riftOrb;

    public static CreativeTabs tabEnderRift = new CreativeTabs("tabEnderRift")
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(riftOrb);
        }
    };

    @Mod.Instance(value = EnderRiftMod.MODID)
    public static EnderRiftMod instance;

    @SidedProxy(clientSide = "gigaherz.enderRift.client.ClientProxy",
            serverSide = "gigaherz.enderRift.server.ServerProxy")
    public static IModProxy proxy;

    public static SimpleNetworkWrapper channel;

    public static final Logger logger = LogManager.getLogger(MODID);
    public static final GuiHandler guiHandler = new GuiHandler();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                rift = new BlockEnderRift("rift"),
                structure = new BlockStructure("rift_structure"),
                riftInterface = new BlockInterface("interface"),
                browser = new BlockBrowser("browser"),
                extension = new BlockProxy("proxy"),
                driver = new BlockDriver("driver"),
                generator = new BlockGenerator("generator")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                rift.createItemBlock(),
                structure.createItemBlock(),
                riftInterface.createItemBlock(),
                browser.createItemBlock(),
                extension.createItemBlock(),
                driver.createItemBlock(),
                generator.createItemBlock(),

                riftOrb = new ItemEnderRift("rift_orb")
        );
    }

    private static void registerTileEntities()
    {
        GameRegistry.registerTileEntityWithAlternatives(TileEnderRift.class, rift.getRegistryName().toString(), "tileEnderRift");
        GameRegistry.registerTileEntityWithAlternatives(TileEnderRiftCorner.class, location("rift_structure_corner").toString(), "tileStructureCorner");
        GameRegistry.registerTileEntityWithAlternatives(TileInterface.class, riftInterface.getRegistryName().toString(), "tileInterface");
        GameRegistry.registerTileEntityWithAlternatives(TileBrowser.class, browser.getRegistryName().toString(), "tileBrowser");
        GameRegistry.registerTileEntityWithAlternatives(TileProxy.class, extension.getRegistryName().toString(), "tileProxy");
        GameRegistry.registerTileEntityWithAlternatives(TileGenerator.class, generator.getRegistryName().toString(), "tileGenerator");
        GameRegistry.registerTileEntity(TileDriver.class, driver.getRegistryName().toString());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        ConfigValues.readConfig(config);

        registerTileEntities();

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

        GameRegistry.register(new RecipeRiftDuplication());

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLInterModComms.sendMessage("waila", "register", "gigaherz.enderRift.plugins.WailaProviders.callbackRegister");

        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "gigaherz.enderRift.plugins.TheOneProbeProviders");
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
