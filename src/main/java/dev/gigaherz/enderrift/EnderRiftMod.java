package dev.gigaherz.enderrift;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.enderrift.automation.browser.*;
import dev.gigaherz.enderrift.network.*;
import dev.gigaherz.enderrift.plugins.TheOneProbeProviders;
import dev.gigaherz.enderrift.rift.*;
import dev.gigaherz.enderrift.automation.driver.DriverBlock;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlock;
import dev.gigaherz.enderrift.automation.iface.InterfaceContainer;
import dev.gigaherz.enderrift.automation.iface.InterfaceScreen;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlockEntity;
import dev.gigaherz.enderrift.automation.proxy.ProxyBlock;
import dev.gigaherz.enderrift.automation.proxy.ProxyBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorBlock;
import dev.gigaherz.enderrift.generator.GeneratorContainer;
import dev.gigaherz.enderrift.generator.GeneratorScreen;
import dev.gigaherz.enderrift.generator.GeneratorBlockEntity;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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

import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

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
        public static final StructureCornerBlock STRUCTURE_CORNER = toBeInitializedLater();
        public static final StructureEdgeBlock STRUCTURE_EDGE = toBeInitializedLater();
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

    public static CreativeModeTab tabEnderRift = new CreativeModeTab("tabEnderRift")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(EnderRiftItems.RIFT_ORB);
        }
    };

    public static EnderRiftMod instance;

    private static final String PROTOCOL_VERSION = "1.1.0";
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

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(BlockEntityType.class, this::registerTEs);
        modEventBus.addGenericListener(MenuType.class, this::registerContainers);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::interComms);
        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.addListener(this::commandEvent);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigValues.SERVER_SPEC);
        //modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new RiftBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).dynamicShape()).setRegistryName("rift"),
                new StructureCornerBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noDrops()).setRegistryName("structure_corner"),
                new StructureEdgeBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noDrops()).setRegistryName("structure_edge"),
                new InterfaceBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("interface"),
                new BrowserBlock(false, BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("browser"),
                new BrowserBlock(true, BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("crafting_browser"),
                new ProxyBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("proxy"),
                new DriverBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("driver"),
                new GeneratorBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)).setRegistryName("generator")
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(EnderRiftBlocks.RIFT, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.RIFT.getRegistryName()),
                new BlockItem(EnderRiftBlocks.INTERFACE, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.INTERFACE.getRegistryName()),
                new BlockItem(EnderRiftBlocks.BROWSER, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.BROWSER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.CRAFTING_BROWSER, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.CRAFTING_BROWSER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.PROXY, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.PROXY.getRegistryName()),
                new BlockItem(EnderRiftBlocks.DRIVER, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.DRIVER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.GENERATOR, new Item.Properties().tab(tabEnderRift)).setRegistryName(EnderRiftBlocks.GENERATOR.getRegistryName()),
                new BlockItem(EnderRiftBlocks.STRUCTURE_CORNER, new Item.Properties().tab(tabEnderRift))
                {
                    @Override
                    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
                    {
                        // Do not offer in creative menu!
                    }
                }.setRegistryName(EnderRiftBlocks.STRUCTURE_CORNER.getRegistryName()),
                new BlockItem(EnderRiftBlocks.STRUCTURE_EDGE, new Item.Properties().tab(tabEnderRift))
                {
                    @Override
                    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
                    {
                        // Do not offer in creative menu!
                    }
                }.setRegistryName(EnderRiftBlocks.STRUCTURE_EDGE.getRegistryName()),
                new RiftItem(new Item.Properties().stacksTo(16).tab(tabEnderRift)).setRegistryName("rift_orb")
        );
    }

    public void registerTEs(RegistryEvent.Register<BlockEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                BlockEntityType.Builder.of(RiftBlockEntity::new, EnderRiftBlocks.RIFT).build(null).setRegistryName("rift"),
                BlockEntityType.Builder.of(StructureCornerBlockEntity::new, EnderRiftBlocks.STRUCTURE_CORNER).build(null).setRegistryName("structure"),
                BlockEntityType.Builder.of(InterfaceBlockEntity::new, EnderRiftBlocks.INTERFACE).build(null).setRegistryName("interface"),
                BlockEntityType.Builder.of(BrowserBlockEntity::new, EnderRiftBlocks.BROWSER, EnderRiftBlocks.CRAFTING_BROWSER).build(null).setRegistryName("browser"),
                BlockEntityType.Builder.of(ProxyBlockEntity::new, EnderRiftBlocks.PROXY).build(null).setRegistryName("proxy"),
                BlockEntityType.Builder.of(DriverBlockEntity::new, EnderRiftBlocks.DRIVER).build(null).setRegistryName("driver"),
                BlockEntityType.Builder.of(GeneratorBlockEntity::new, EnderRiftBlocks.GENERATOR).build(null).setRegistryName("generator")
        );
    }

    public void registerContainers(RegistryEvent.Register<MenuType<?>> event)
    {
        event.getRegistry().registerAll(
                new MenuType<>(BrowserContainer::new).setRegistryName("browser"),
                new MenuType<>(CraftingBrowserContainer::new).setRegistryName("crafting_browser"),
                new MenuType<>(InterfaceContainer::new).setRegistryName("interface"),
                new MenuType<>(GeneratorContainer::new).setRegistryName("generator")
        );
    }

    public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SimpleRecipeSerializer<>(OrbDuplicationRecipe::new).setRegistryName("orb_duplication")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(ClearCraftingGrid.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(ClearCraftingGrid::encode).decoder(ClearCraftingGrid::new).consumer(ClearCraftingGrid::handle).add();
        CHANNEL.messageBuilder(SendSlotChanges.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SendSlotChanges::encode).decoder(SendSlotChanges::new).consumer(SendSlotChanges::handle).add();
        CHANNEL.messageBuilder(SetVisibleSlots.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SetVisibleSlots::encode).decoder(SetVisibleSlots::new).consumer(SetVisibleSlots::handle).add();
        logger.debug("Final message number: " + messageNumber);

        RiftStructure.init();
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(GeneratorContainer.TYPE, GeneratorScreen::new);
        MenuScreens.register(InterfaceContainer.TYPE, InterfaceScreen::new);
        MenuScreens.register(BrowserContainer.TYPE, BrowserScreen::new);
        MenuScreens.register(CraftingBrowserContainer.TYPE, CraftingBrowserScreen::new);
    }

    public void interComms(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> TheOneProbeProviders.create());
    }

    public void commandEvent(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("enderrift")
                    .then(Commands.literal("locate")
                            .then(Commands.argument("riftId", IntegerArgumentType.integer(1))
                                    .requires(cs->cs.hasPermission(3)) //permission
                                    .executes(this::locateSpecificRift)
                            )
                            .requires(cs->cs.hasPermission(3)) //permission
                            .executes(this::locateAllRifts)
                    )
        );
    }

    private int locateSpecificRift(CommandContext<CommandSourceStack> context)
    {
        int riftId = context.getArgument("riftId", int.class);
        RiftInventory rift = RiftStorage.get(context.getSource().getLevel())
                .getRift(riftId);
        locateRiftById(context, riftId, rift);
        return 0;
    }

    private void locateRiftById(CommandContext<CommandSourceStack> context, int riftId, RiftInventory rift)
    {
        rift.locateListeners(pos ->
                context.getSource().sendSuccess(new TextComponent(String.format("Found rift with id %d at %s %s %s", riftId, pos.getX(), pos.getY(), pos.getZ())), true));
    }

    private int locateAllRifts(CommandContext<CommandSourceStack> context)
    {
        RiftStorage.get(context.getSource().getLevel())
                .walkExistingRifts((id, rift) -> locateRiftById(context, id, rift));
        return 0;
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
                gen.addProvider(new LootTableGen(gen));
                //gen.addProvider(new ItemTagGens(gen));
                //gen.addProvider(new BlockTagGens(gen));
            }
            if (event.includeClient())
            {
                //gen.addProvider(new BlockStates(gen, event));
            }
        }

        private static class LootTableGen extends LootTableProvider implements DataProvider
        {
            public LootTableGen(DataGenerator gen)
            {
                super(gen);
            }

            private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
                    Pair.of(BlockTables::new, LootContextParamSets.BLOCK)
                    //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                    //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                    //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                    //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
            );

            @Override
            protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
            {
                return tables;
            }

            @Override
            protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
            {
                map.forEach((p_218436_2_, p_218436_3_) -> {
                    LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
                });
            }

            public static class BlockTables extends BlockLoot
            {
                @Override
                protected void addTables()
                {
                    this.dropSelf(EnderRiftBlocks.GENERATOR);
                    this.dropSelf(EnderRiftBlocks.DRIVER);
                    this.dropSelf(EnderRiftBlocks.PROXY);
                    this.dropSelf(EnderRiftBlocks.INTERFACE);
                    this.dropSelf(EnderRiftBlocks.BROWSER);
                    this.dropSelf(EnderRiftBlocks.CRAFTING_BROWSER);
                    this.dropSelf(EnderRiftBlocks.RIFT);
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