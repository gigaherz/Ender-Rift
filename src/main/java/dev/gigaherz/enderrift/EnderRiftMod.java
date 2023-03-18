package dev.gigaherz.enderrift;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.gigaherz.enderrift.automation.browser.*;
import dev.gigaherz.enderrift.automation.driver.DriverBlock;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlock;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlockEntity;
import dev.gigaherz.enderrift.automation.iface.InterfaceContainer;
import dev.gigaherz.enderrift.automation.iface.InterfaceScreen;
import dev.gigaherz.enderrift.automation.proxy.ProxyBlock;
import dev.gigaherz.enderrift.automation.proxy.ProxyBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorBlock;
import dev.gigaherz.enderrift.generator.GeneratorBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorContainer;
import dev.gigaherz.enderrift.generator.GeneratorScreen;
import dev.gigaherz.enderrift.network.ClearCraftingGrid;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.network.SetVisibleSlots;
import dev.gigaherz.enderrift.rift.*;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
@Mod(EnderRiftMod.MODID)
public class EnderRiftMod
{
    public static final String MODID = "enderrift";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static CreativeModeTab ENDERRIFT_CREATIVE_TAB;

    public static final RegistryObject<Block> RIFT = BLOCKS.register("rift", () -> new RiftBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).dynamicShape()));
    public static final RegistryObject<StructureCornerBlock> STRUCTURE_CORNER = BLOCKS.register("structure_corner", () -> new StructureCornerBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noLootTable()));
    public static final RegistryObject<StructureEdgeBlock> STRUCTURE_EDGE = BLOCKS.register("structure_edge", () -> new StructureEdgeBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noLootTable()));
    public static final RegistryObject<Block> INTERFACE = BLOCKS.register("interface", () -> new InterfaceBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final RegistryObject<Block> BROWSER = BLOCKS.register("browser", () -> new BrowserBlock(false, BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final RegistryObject<Block> CRAFTING_BROWSER = BLOCKS.register("crafting_browser", () ->new BrowserBlock(true, BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final RegistryObject<Block> PROXY = BLOCKS.register("proxy", () ->  new ProxyBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final RegistryObject<Block> DRIVER = BLOCKS.register("driver", () -> new DriverBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final RegistryObject<Block> GENERATOR = BLOCKS.register("generator", () -> new GeneratorBlock(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));

    public static final RegistryObject<Item> RIFT_ITEM = ITEMS.register("rift", () -> new BlockItem(RIFT.get(), new Item.Properties()));
    public static final RegistryObject<Item> STRUCTURE_CORNER_ITEM = ITEMS.register("structure_corner", () -> new BlockItem(STRUCTURE_CORNER.get(), new Item.Properties()));
    public static final RegistryObject<Item> STRUCTURE_EDGE_ITEM = ITEMS.register("structure_edge", () -> new BlockItem(STRUCTURE_EDGE.get(), new Item.Properties()));
    public static final RegistryObject<Item> INTERFACE_ITEM = ITEMS.register("interface", () -> new BlockItem(INTERFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> BROWSER_ITEM = ITEMS.register("browser", () -> new BlockItem(BROWSER.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRAFTING_BROWSER_ITEM = ITEMS.register("crafting_browser", () -> new BlockItem(CRAFTING_BROWSER.get(), new Item.Properties()));
    public static final RegistryObject<Item> PROXY_ITEM = ITEMS.register("proxy", () -> new BlockItem(PROXY.get(), new Item.Properties()));
    public static final RegistryObject<Item> DRIVER_ITEM = ITEMS.register("driver", () -> new BlockItem(DRIVER.get(), new Item.Properties()));
    public static final RegistryObject<Item> GENERATOR_ITEM = ITEMS.register("generator", () -> new BlockItem(GENERATOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> RIFT_ORB = ITEMS.register("rift_orb", () -> new RiftItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<BlockEntityType<RiftBlockEntity>> RIFT_BLOCK_ENTITY = BLOCK_ENTITIES.register("rift", () -> BlockEntityType.Builder.of(RiftBlockEntity::new, RIFT.get()).build(null));
    public static final RegistryObject<BlockEntityType<StructureCornerBlockEntity>> STRUCTURE_CORNER_BLOCK_ENTITY = BLOCK_ENTITIES.register("structure", () -> BlockEntityType.Builder.of(StructureCornerBlockEntity::new, STRUCTURE_CORNER.get()).build(null));
    public static final RegistryObject<BlockEntityType<InterfaceBlockEntity>> INTERFACE_BLOCK_ENTITY = BLOCK_ENTITIES.register("interface", () -> BlockEntityType.Builder.of(InterfaceBlockEntity::new, INTERFACE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BrowserBlockEntity>> BROWSER_BLOCK_ENTITY = BLOCK_ENTITIES.register("browser", () -> BlockEntityType.Builder.of(BrowserBlockEntity::new, BROWSER.get(), CRAFTING_BROWSER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ProxyBlockEntity>> PROXY_BLOCK_ENTITY = BLOCK_ENTITIES.register("proxy", () -> BlockEntityType.Builder.of(ProxyBlockEntity::new, PROXY.get()).build(null));
    public static final RegistryObject<BlockEntityType<DriverBlockEntity>> DRIVER_BLOCK_ENTITY = BLOCK_ENTITIES.register("driver", () -> BlockEntityType.Builder.of(DriverBlockEntity::new, DRIVER.get()).build(null));
    public static final RegistryObject<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("generator", () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR.get()).build(null));

    public static final RegistryObject<MenuType<BrowserContainer>> BROWSER_MENU = MENU_TYPES.register("browser", () -> new MenuType<>(BrowserContainer::new));
    public static final RegistryObject<MenuType<CraftingBrowserContainer>> CRAFTING_BROWSER_MENU = MENU_TYPES.register("crafting_browser", () -> new MenuType<>(CraftingBrowserContainer::new));
    public static final RegistryObject<MenuType<InterfaceContainer>> INTERFACE_MENU = MENU_TYPES.register("interface", () -> new MenuType<>(InterfaceContainer::new));
    public static final RegistryObject<MenuType<GeneratorContainer>> GENERATOR_MENU = MENU_TYPES.register("generator", () -> new MenuType<>(GeneratorContainer::new));

    public static final RegistryObject<SimpleCraftingRecipeSerializer<OrbDuplicationRecipe>> ORB_DUPLICATION = RECIPE_SERIALIZERS.register("orb_duplication", () -> new SimpleCraftingRecipeSerializer<>(OrbDuplicationRecipe::new));

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
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::interComms);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::registerTabs);

        MinecraftForge.EVENT_BUS.addListener(this::commandEvent);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigValues.SERVER_SPEC);
        //modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);
    }

    private void registerTabs(CreativeModeTabEvent.Register event)
    {
        ENDERRIFT_CREATIVE_TAB = event.registerCreativeModeTab(location("ender_rift_tab"), builder -> builder
                .icon(() -> new ItemStack(RIFT_ORB.get()))
                .title(Component.translatable("itemGroup.tabEnderRift"))
                .displayItems((featureFlags, output, hasOp) -> {
                    output.accept(RIFT_ITEM.get());
                    output.accept(RIFT_ORB.get());
                    output.accept(INTERFACE_ITEM.get());
                    output.accept(BROWSER_ITEM.get());
                    output.accept(CRAFTING_BROWSER_ITEM.get());
                    output.accept(PROXY_ITEM.get());
                    output.accept(DRIVER_ITEM.get());
                    output.accept(GENERATOR_ITEM.get());
                })
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(ClearCraftingGrid.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(ClearCraftingGrid::encode).decoder(ClearCraftingGrid::new).consumerNetworkThread(ClearCraftingGrid::handle).add();
        CHANNEL.messageBuilder(SendSlotChanges.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SendSlotChanges::encode).decoder(SendSlotChanges::new).consumerNetworkThread(SendSlotChanges::handle).add();
        CHANNEL.messageBuilder(SetVisibleSlots.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SetVisibleSlots::encode).decoder(SetVisibleSlots::new).consumerNetworkThread(SetVisibleSlots::handle).add();
        logger.debug("Final message number: " + messageNumber);

        RiftStructure.init();
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(GENERATOR_MENU.get(), GeneratorScreen::new);
        MenuScreens.register(INTERFACE_MENU.get(), InterfaceScreen::new);
        MenuScreens.register(BROWSER_MENU.get(), BrowserScreen::new);
        MenuScreens.register(CRAFTING_BROWSER_MENU.get(), CraftingBrowserScreen::new);
    }

    public void interComms(InterModEnqueueEvent event)
    {
        //InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> TheOneProbeProviders.create());
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
                context.getSource().sendSuccess(Component.literal(String.format("Found rift with id %d at %s %s %s", riftId, pos.getX(), pos.getY(), pos.getZ())), true));
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

            gen.addProvider(event.includeServer(), Loot.create(gen));
            gen.addProvider(event.includeServer(), new BlockTagGens(gen, event.getExistingFileHelper()));
        }

        private static class Loot
        {
            public static LootTableProvider create(DataGenerator gen)
            {
                return new LootTableProvider(gen.getPackOutput(), Set.of(), List.of(
                        new LootTableProvider.SubProviderEntry(Loot.BlockTables::new, LootContextParamSets.BLOCK)
                ));
            }

            public static class BlockTables extends BlockLootSubProvider
            {
                protected BlockTables()
                {
                    super(Set.of(), FeatureFlags.REGISTRY.allFlags());
                }

                @Override
                protected void generate()
                {
                    this.dropSelf(GENERATOR.get());
                    this.dropSelf(DRIVER.get());
                    this.dropSelf(PROXY.get());
                    this.dropSelf(INTERFACE.get());
                    this.dropSelf(BROWSER.get());
                    this.dropSelf(CRAFTING_BROWSER.get());
                    this.dropSelf(RIFT.get());
                }

                @Override
                protected Iterable<Block> getKnownBlocks()
                {
                    return ForgeRegistries.BLOCKS.getEntries().stream()
                            .filter(e -> e.getKey().location().getNamespace().equals(EnderRiftMod.MODID))
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList());
                }
            }
        }

        private static class BlockTagGens extends IntrinsicHolderTagsProvider<Block>
        {
            public BlockTagGens(DataGenerator gen, ExistingFileHelper existingFileHelper)
            {
                super(gen.getPackOutput(), Registries.BLOCK,
                        CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                        (p_255627_) -> p_255627_.builtInRegistryHolder().key(),
                        EnderRiftMod.MODID, existingFileHelper);
            }

            @Override
            protected void addTags(HolderLookup.Provider p_255662_)
            {
                tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .add(BROWSER.get())
                        .add(CRAFTING_BROWSER.get())
                        .add(INTERFACE.get())
                        .add(PROXY.get())
                        .add(DRIVER.get())
                        .add(RIFT.get())
                        .add(GENERATOR.get())
                        .add(STRUCTURE_CORNER.get())
                        .add(STRUCTURE_EDGE.get());
            }
        }
    }
}