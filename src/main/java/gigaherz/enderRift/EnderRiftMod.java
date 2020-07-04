package gigaherz.enderRift;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import gigaherz.enderRift.automation.browser.*;
import gigaherz.enderRift.automation.driver.DriverBlock;
import gigaherz.enderRift.automation.driver.DriverTileEntity;
import gigaherz.enderRift.automation.iface.InterfaceBlock;
import gigaherz.enderRift.automation.iface.InterfaceContainer;
import gigaherz.enderRift.automation.iface.InterfaceScreen;
import gigaherz.enderRift.automation.iface.InterfaceTileEntity;
import gigaherz.enderRift.automation.proxy.ProxyBlock;
import gigaherz.enderRift.automation.proxy.ProxyTileEntity;
import gigaherz.enderRift.generator.GeneratorBlock;
import gigaherz.enderRift.generator.GeneratorContainer;
import gigaherz.enderRift.generator.GeneratorScreen;
import gigaherz.enderRift.generator.GeneratorTileEntity;
import gigaherz.enderRift.network.*;
import gigaherz.enderRift.rift.*;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.*;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
@Mod(EnderRiftMod.MODID)
public class EnderRiftMod
{
    /*
        dependencies = "after:Waila;after:gbook",
        updateJSON =
     */

    public static final String MODID = "enderrift";

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T> T toBeInitializedLater()
    {
        return null;
    }

    @ObjectHolder("enderrift")
    public static class EnderRiftBlocks
    {
        public static final Block RIFT = toBeInitializedLater();
        public static final StructureBlock STRUCTURE = toBeInitializedLater();
        public static final Block INTERFACE = toBeInitializedLater();
        public static final Block GENERATOR = toBeInitializedLater();
        public static final Block BROWSER = toBeInitializedLater();
        public static final Block CRAFTING_BROWSER = toBeInitializedLater();
        public static final Block PROXY = toBeInitializedLater();
        public static final Block DRIVER = toBeInitializedLater();
    }

    @ObjectHolder("enderrift")
    public static class EnderRiftItems
    {
        public static final Item RIFT_ORB = toBeInitializedLater();
    }

    public static ItemGroup tabEnderRift = new ItemGroup("tabEnderRift")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(EnderRiftItems.RIFT_ORB);
        }
    };

    public static EnderRiftMod instance;

    private static final String PROTOCOL_VERSION = "1.01";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, "main"))
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
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::interComms);
        modEventBus.addListener(this::gatherData);

        //modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigData.SERVER_SPEC);
        //modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new RiftBlock(Block.Properties.create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F).variableOpacity()).setRegistryName("rift"),
                new StructureBlock(Block.Properties.create(Material.ROCK).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F).noDrops()).setRegistryName("structure"),
                new InterfaceBlock(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("interface"),
                new BrowserBlock(false, Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("browser"),
                new BrowserBlock(true, Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("crafting_browser"),
                new ProxyBlock(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("proxy"),
                new DriverBlock(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("driver"),
                new GeneratorBlock(Block.Properties.create(Material.IRON, MaterialColor.STONE).sound(SoundType.METAL).hardnessAndResistance(3.0F, 8.0F)).setRegistryName("generator")
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(EnderRiftBlocks.RIFT, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.RIFT.getRegistryName()),
                new BlockItem(EnderRiftBlocks.INTERFACE, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.INTERFACE.getRegistryName()),
                new BlockItem(EnderRiftBlocks.BROWSER, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.BROWSER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.CRAFTING_BROWSER, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.CRAFTING_BROWSER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.PROXY, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.PROXY.getRegistryName()),
                new BlockItem(EnderRiftBlocks.DRIVER, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.DRIVER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.GENERATOR, new Item.Properties().group(tabEnderRift)).setRegistryName(EnderRiftBlocks.GENERATOR.getRegistryName()),
                new BlockItem(EnderRiftBlocks.STRUCTURE, new Item.Properties().group(tabEnderRift))
                {
                    @Override
                    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
                    {
                        // Do not offer in creative menu!
                    }
                }.setRegistryName(EnderRiftBlocks.STRUCTURE.getRegistryName()),

                new RiftItem(new Item.Properties().maxStackSize(16).group(tabEnderRift)).setRegistryName("rift_orb")
        );
    }

    public void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(RiftTileEntity::new, EnderRiftBlocks.RIFT).build(null).setRegistryName("rift"),
                TileEntityType.Builder.create(StructureTileEntity::new, EnderRiftBlocks.STRUCTURE).build(null).setRegistryName("structure"),
                TileEntityType.Builder.create(InterfaceTileEntity::new, EnderRiftBlocks.INTERFACE).build(null).setRegistryName("interface"),
                TileEntityType.Builder.create(BrowserTileEntity::new, EnderRiftBlocks.BROWSER, EnderRiftBlocks.CRAFTING_BROWSER).build(null).setRegistryName("browser"),
                TileEntityType.Builder.create(ProxyTileEntity::new, EnderRiftBlocks.PROXY).build(null).setRegistryName("proxy"),
                TileEntityType.Builder.create(DriverTileEntity::new, EnderRiftBlocks.DRIVER).build(null).setRegistryName("driver"),
                TileEntityType.Builder.create(GeneratorTileEntity::new, EnderRiftBlocks.GENERATOR).build(null).setRegistryName("generator")
        );
    }

    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                new ContainerType<>(BrowserContainer::new).setRegistryName("browser"),
                new ContainerType<>(CraftingBrowserContainer::new).setRegistryName("crafting_browser"),
                IForgeContainerType.create(InterfaceContainer::new).setRegistryName("interface"),
                IForgeContainerType.create(GeneratorContainer::new).setRegistryName("generator")
        );
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new OrbDuplicationRecipe.Serializer().setRegistryName("orb_duplication")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(ClearCraftingGrid.class, messageNumber++).encoder(ClearCraftingGrid::encode).decoder(ClearCraftingGrid::new).consumer(ClearCraftingGrid::handle).add();
        CHANNEL.messageBuilder(SendSlotChanges.class, messageNumber++).encoder(SendSlotChanges::encode).decoder(SendSlotChanges::new).consumer(SendSlotChanges::handle).add();
        CHANNEL.messageBuilder(SetVisibleSlots.class, messageNumber++).encoder(SetVisibleSlots::encode).decoder(SetVisibleSlots::new).consumer(SetVisibleSlots::handle).add();
        CHANNEL.messageBuilder(UpdateField.class, messageNumber++).encoder(UpdateField::encode).decoder(UpdateField::new).consumer(UpdateField::handle).add();
        CHANNEL.messageBuilder(UpdatePowerStatus.class, messageNumber++).encoder(UpdatePowerStatus::encode).decoder(UpdatePowerStatus::new).consumer(UpdatePowerStatus::handle).add();
        logger.debug("Final message number: " + messageNumber);

        RiftStructure.init();
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(GeneratorContainer.TYPE, GeneratorScreen::new);
        ScreenManager.registerFactory(InterfaceContainer.TYPE, InterfaceScreen::new);
        ScreenManager.registerFactory(BrowserContainer.TYPE, BrowserScreen::new);
        ScreenManager.registerFactory(CraftingBrowserContainer.TYPE, CraftingBrowserScreen::new);
    }

    public void interComms(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("gbook", "registerBook", () -> EnderRiftMod.location("xml/book.xml"));

        //InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> TheOneProbeProviders.create());
    }

    public void gatherData(GatherDataEvent event)
    {
        Data.gatherData(event);
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static class Data
    {
        public static void gatherData(GatherDataEvent event)
        {
            DataGenerator gen = event.getGenerator();

            if (event.includeServer())
            {
                //gen.addProvider(new Recipes(gen));
                gen.addProvider(new LootTables(gen));
                //gen.addProvider(new ItemTagGens(gen));
                //gen.addProvider(new BlockTagGens(gen));
            }
            if (event.includeClient())
            {
                //gen.addProvider(new BlockStates(gen, event));
            }
        }

        private static class LootTables extends LootTableProvider implements IDataProvider
        {
            public LootTables(DataGenerator gen)
            {
                super(gen);
            }

            private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
                    Pair.of(BlockTables::new, LootParameterSets.BLOCK)
                    //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                    //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                    //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                    //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
            );

            @Override
            protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
            {
                return tables;
            }

            @Override
            protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
            {
                map.forEach((p_218436_2_, p_218436_3_) -> {
                    LootTableManager.func_227508_a_(validationtracker, p_218436_2_, p_218436_3_);
                });
            }

            public static class BlockTables extends BlockLootTables
            {
                @Override
                protected void addTables()
                {
                    this.registerDropSelfLootTable(EnderRiftBlocks.GENERATOR);
                    this.registerDropSelfLootTable(EnderRiftBlocks.DRIVER);
                    this.registerDropSelfLootTable(EnderRiftBlocks.PROXY);
                    this.registerDropSelfLootTable(EnderRiftBlocks.INTERFACE);
                    this.registerDropSelfLootTable(EnderRiftBlocks.BROWSER);
                    this.registerDropSelfLootTable(EnderRiftBlocks.CRAFTING_BROWSER);
                    this.registerDropSelfLootTable(EnderRiftBlocks.RIFT);
                }

                @Override
                protected Iterable<Block> getKnownBlocks()
                {
                    return ForgeRegistries.BLOCKS.getValues().stream()
                            .filter(b -> b.getRegistryName().getNamespace().equals(MODID))
                            .collect(Collectors.toList());
                }
            }
        }
    }
}
