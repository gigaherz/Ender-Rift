package gigaherz.enderRift;

import gigaherz.enderRift.automation.browser.BlockBrowser;
import gigaherz.enderRift.automation.browser.ContainerBrowser;
import gigaherz.enderRift.automation.browser.ContainerCraftingBrowser;
import gigaherz.enderRift.automation.browser.TileBrowser;
import gigaherz.enderRift.automation.driver.BlockDriver;
import gigaherz.enderRift.automation.driver.TileDriver;
import gigaherz.enderRift.automation.iface.BlockInterface;
import gigaherz.enderRift.automation.iface.ContainerInterface;
import gigaherz.enderRift.automation.iface.TileInterface;
import gigaherz.enderRift.automation.proxy.BlockProxy;
import gigaherz.enderRift.automation.proxy.TileProxy;
import gigaherz.enderRift.generator.BlockGenerator;
import gigaherz.enderRift.generator.ContainerGenerator;
import gigaherz.enderRift.generator.TileGenerator;
import gigaherz.enderRift.network.*;
import gigaherz.enderRift.rift.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod.EventBusSubscriber
@Mod(EnderRiftMod.MODID)
public class EnderRiftMod
{
    /*
        dependencies = "after:Waila;after:gbook",
        updateJSON = "https://raw.githubusercontent.com/gigaherz/Ender-Rift/master/update.json"
     */
    
    public static final String MODID = "enderrift";

    @ObjectHolder("enderrift:rift")
    public static Block rift;
    @ObjectHolder("enderrift:rift_structure")
    public static BlockStructure structure;
    @ObjectHolder("enderrift:interface")
    public static Block riftInterface;
    @ObjectHolder("enderrift:generator")
    public static Block generator;
    @ObjectHolder("enderrift:browser")
    public static Block browser;
    @ObjectHolder("enderrift:proxy")
    public static Block extension;
    @ObjectHolder("enderrift:driver")
    public static Block driver;
    @ObjectHolder("enderrift:rift_orb")
    public static Item riftOrb;

    public static ItemGroup tabEnderRift = new ItemGroup("tabEnderRift")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(riftOrb);
        }
    };

    public static EnderRiftMod instance;

    public static final String CHANNEL = MODID;
    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, CHANNEL))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static final Logger logger = LogManager.getLogger(MODID);

    public EnderRiftMod()
    {
        instance = this;

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(TileEntityType.class, this::registerTEs);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainers);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addListener(this::commonSetup);

        //modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        //modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new BlockEnderRift(Block.Properties.create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F).variableOpacity()
                ).setRegistryName("rift"),
                new BlockStructure(Block.Properties.create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("rift_structure"),
                new BlockInterface(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("interface"),
                new BlockBrowser(false, Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("browser"),
                new BlockBrowser(true, Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("crafting_browser"),
                new BlockProxy(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("proxy"),
                new BlockDriver(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("driver"),
                new BlockGenerator(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F,8.0F)
                ).setRegistryName("generator")
        );

        BlockState state;

    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(rift, new Item.Properties().group(tabEnderRift)).setRegistryName(rift.getRegistryName()),
                new BlockItem(structure, new Item.Properties().group(tabEnderRift)).setRegistryName(structure.getRegistryName()),
                new BlockItem(riftInterface, new Item.Properties().group(tabEnderRift)).setRegistryName(riftInterface.getRegistryName()),
                new BlockItem(browser, new Item.Properties().group(tabEnderRift)).setRegistryName(browser.getRegistryName()),
                new BlockItem(extension, new Item.Properties().group(tabEnderRift)).setRegistryName(extension.getRegistryName()),
                new BlockItem(driver, new Item.Properties().group(tabEnderRift)).setRegistryName(driver.getRegistryName()),
                new BlockItem(generator, new Item.Properties().group(tabEnderRift)).setRegistryName(generator.getRegistryName()),

                new ItemEnderRift(new Item.Properties().maxStackSize(16).group(tabEnderRift))
        );
    }

    public void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(TileEnderRift::new, rift).build(null).setRegistryName("rift"),
                TileEntityType.Builder.create(TileEnderRiftCorner::new, structure).build(null).setRegistryName("corner"),
                TileEntityType.Builder.create(TileInterface::new, riftInterface).build(null).setRegistryName("interface"),
                TileEntityType.Builder.create(TileBrowser::new, browser).build(null).setRegistryName("browser"),
                TileEntityType.Builder.create(TileProxy::new, extension).build(null).setRegistryName("proxy"),
                TileEntityType.Builder.create(TileDriver::new, driver).build(null).setRegistryName("driver"),
                TileEntityType.Builder.create(TileGenerator::new, generator).build(null).setRegistryName("generator")
        );
    }

    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeContainerType.create(ContainerBrowser::new).setRegistryName("browser"),
                IForgeContainerType.create(ContainerCraftingBrowser::new).setRegistryName("crafting_browser"),
                IForgeContainerType.create(ContainerInterface::new).setRegistryName("crafting_browser"),
                IForgeContainerType.create(ContainerGenerator::new).setRegistryName("crafting_browser")
        );
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new RecipeRiftDuplication.Serializer().setRegistryName("rift_duplication_recipe")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        //Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        //ConfigValues.readConfig(config);

        int messageNumber = 0;
        channel.registerMessage(messageNumber++, ClearCraftingGrid.class, ClearCraftingGrid::encode, ClearCraftingGrid::new, ClearCraftingGrid::handle);
        channel.registerMessage(messageNumber++, SendSlotChanges.class, SendSlotChanges::encode, SendSlotChanges::new, SendSlotChanges::handle);
        channel.registerMessage(messageNumber++, SetVisibleSlots.class, SetVisibleSlots::encode, SetVisibleSlots::new, SetVisibleSlots::handle);
        channel.registerMessage(messageNumber++, UpdateField.class, UpdateField::encode, UpdateField::new, UpdateField::handle);
        channel.registerMessage(messageNumber++, UpdatePowerStatus.class, UpdatePowerStatus::encode, UpdatePowerStatus::new, UpdatePowerStatus::handle);
        logger.debug("Final message number: " + messageNumber);

        RiftStructure.init();

        //FMLInterModComms.sendMessage("waila", "register", "gigaherz.enderRift.plugins.WailaProviders.callbackRegister");

        //FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "gigaherz.enderRift.plugins.TheOneProbeProviders");
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
