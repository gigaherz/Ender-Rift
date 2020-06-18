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
import gigaherz.enderRift.network.*;
import gigaherz.enderRift.rift.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = EnderRiftMod.MODID,
        version = EnderRiftMod.VERSION,
        acceptedMinecraftVersions = "[1.12.0,1.13.0)",
        dependencies = "after:Waila;after:gbook",
        updateJSON = "https://raw.githubusercontent.com/gigaherz/Ender-Rift/master/update.json")
public class EnderRiftMod
{
    public static final String MODID = "enderrift";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "enderrift";

    @GameRegistry.ObjectHolder("enderrift:rift")
    public static BlockEnderRift rift;
    @GameRegistry.ObjectHolder("enderrift:rift_structure")
    public static BlockStructure structure;
    @GameRegistry.ObjectHolder("enderrift:interface")
    public static BlockRegistered riftInterface;
    @GameRegistry.ObjectHolder("enderrift:generator")
    public static BlockRegistered generator;
    @GameRegistry.ObjectHolder("enderrift:browser")
    public static BlockRegistered browser;
    @GameRegistry.ObjectHolder("enderrift:proxy")
    public static BlockRegistered extension;
    @GameRegistry.ObjectHolder("enderrift:driver")
    public static BlockRegistered driver;

    @GameRegistry.ObjectHolder("enderrift:rift_orb")
    public static Item riftOrb;

    public static CreativeTabs tabEnderRift = new CreativeTabs("tabEnderRift")
    {
        @Override
        public ItemStack createIcon()
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
                new BlockEnderRift("rift"),
                new BlockStructure("rift_structure"),
                new BlockInterface("interface"),
                new BlockBrowser("browser"),
                new BlockProxy("proxy"),
                new BlockDriver("driver"),
                new BlockGenerator("generator")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        GameRegistry.registerTileEntity(TileEnderRift.class, rift.getRegistryName());
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, location("rift_structure_corner"));
        GameRegistry.registerTileEntity(TileInterface.class, riftInterface.getRegistryName());
        GameRegistry.registerTileEntity(TileBrowser.class, browser.getRegistryName());
        GameRegistry.registerTileEntity(TileProxy.class, extension.getRegistryName());
        GameRegistry.registerTileEntity(TileGenerator.class, generator.getRegistryName());
        GameRegistry.registerTileEntity(TileDriver.class, driver.getRegistryName());

        event.getRegistry().registerAll(
                rift.createItemBlock(),
                structure.createItemBlock(),
                riftInterface.createItemBlock(),
                browser.createItemBlock(),
                extension.createItemBlock(),
                driver.createItemBlock(),
                generator.createItemBlock(),

                new ItemEnderRift("rift_orb")
        );
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        ConfigValues.readConfig(config);

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SendSlotChanges.Handler.class, SendSlotChanges.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(SetVisibleSlots.Handler.class, SetVisibleSlots.class, messageNumber++, Side.SERVER);
        channel.registerMessage(UpdateField.Handler.class, UpdateField.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(ClearCraftingGrid.Handler.class, ClearCraftingGrid.class, messageNumber++, Side.SERVER);
        channel.registerMessage(UpdatePowerStatus.Handler.class, UpdatePowerStatus.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        RiftStructure.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        //FMLInterModComms.sendMessage("waila", "register", "gigaherz.enderRift.plugins.WailaProviders.callbackRegister");

        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "gigaherz.enderRift.plugins.TheOneProbeProviders");
    }

    private static class IMC
    {

    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
