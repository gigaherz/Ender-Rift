package dev.gigaherz.enderrift;

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
import dev.gigaherz.enderrift.integration.Ae2Integration;
import dev.gigaherz.enderrift.network.ClearCraftingGrid;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.network.SetVisibleSlots;
import dev.gigaherz.enderrift.rift.*;
import dev.gigaherz.enderrift.rift.storage.RiftHolder;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod(EnderRiftMod.MODID)
public class EnderRiftMod
{
    public static final String MODID = "enderrift";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> RIFT = BLOCKS.register("rift", () -> new RiftBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).dynamicShape()));
    public static final DeferredBlock<StructureCornerBlock> STRUCTURE_CORNER = BLOCKS.register("structure_corner", () -> new StructureCornerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noLootTable()));
    public static final DeferredBlock<StructureEdgeBlock> STRUCTURE_EDGE = BLOCKS.register("structure_edge", () -> new StructureEdgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F).noLootTable()));
    public static final DeferredBlock<Block> INTERFACE = BLOCKS.register("interface", () -> new InterfaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final DeferredBlock<Block> BROWSER = BLOCKS.register("browser", () -> new BrowserBlock(false, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final DeferredBlock<Block> CRAFTING_BROWSER = BLOCKS.register("crafting_browser", () -> new BrowserBlock(true, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final DeferredBlock<Block> PROXY = BLOCKS.register("proxy", () -> new ProxyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final DeferredBlock<Block> DRIVER = BLOCKS.register("driver", () -> new DriverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));
    public static final DeferredBlock<Block> GENERATOR = BLOCKS.register("generator", () -> new GeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.METAL).strength(3.0F, 8.0F)));

    public static final DeferredItem<Item> RIFT_ITEM = ITEMS.register("rift", () -> new BlockItem(RIFT.get(), new Item.Properties()));
    public static final DeferredItem<Item> STRUCTURE_CORNER_ITEM = ITEMS.register("structure_corner", () -> new BlockItem(STRUCTURE_CORNER.get(), new Item.Properties()));
    public static final DeferredItem<Item> STRUCTURE_EDGE_ITEM = ITEMS.register("structure_edge", () -> new BlockItem(STRUCTURE_EDGE.get(), new Item.Properties()));
    public static final DeferredItem<Item> INTERFACE_ITEM = ITEMS.register("interface", () -> new BlockItem(INTERFACE.get(), new Item.Properties()));
    public static final DeferredItem<Item> BROWSER_ITEM = ITEMS.register("browser", () -> new BlockItem(BROWSER.get(), new Item.Properties()));
    public static final DeferredItem<Item> CRAFTING_BROWSER_ITEM = ITEMS.register("crafting_browser", () -> new BlockItem(CRAFTING_BROWSER.get(), new Item.Properties()));
    public static final DeferredItem<Item> PROXY_ITEM = ITEMS.register("proxy", () -> new BlockItem(PROXY.get(), new Item.Properties()));
    public static final DeferredItem<Item> DRIVER_ITEM = ITEMS.register("driver", () -> new BlockItem(DRIVER.get(), new Item.Properties()));
    public static final DeferredItem<Item> GENERATOR_ITEM = ITEMS.register("generator", () -> new BlockItem(GENERATOR.get(), new Item.Properties()));
    public static final DeferredItem<Item> RIFT_ORB = ITEMS.register("rift_orb", () -> new RiftItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftBlockEntity>> RIFT_BLOCK_ENTITY = BLOCK_ENTITIES.register("rift", () -> BlockEntityType.Builder.of(RiftBlockEntity::new, RIFT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StructureCornerBlockEntity>> STRUCTURE_CORNER_BLOCK_ENTITY = BLOCK_ENTITIES.register("structure", () -> BlockEntityType.Builder.of(StructureCornerBlockEntity::new, STRUCTURE_CORNER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InterfaceBlockEntity>> INTERFACE_BLOCK_ENTITY = BLOCK_ENTITIES.register("interface", () -> BlockEntityType.Builder.of(InterfaceBlockEntity::new, INTERFACE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrowserBlockEntity>> BROWSER_BLOCK_ENTITY = BLOCK_ENTITIES.register("browser", () -> BlockEntityType.Builder.of(BrowserBlockEntity::new, BROWSER.get(), CRAFTING_BROWSER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProxyBlockEntity>> PROXY_BLOCK_ENTITY = BLOCK_ENTITIES.register("proxy", () -> BlockEntityType.Builder.of(ProxyBlockEntity::new, PROXY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DriverBlockEntity>> DRIVER_BLOCK_ENTITY = BLOCK_ENTITIES.register("driver", () -> BlockEntityType.Builder.of(DriverBlockEntity::new, DRIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("generator", () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<BrowserContainer>> BROWSER_MENU = MENU_TYPES.register("browser", () -> new MenuType<>(BrowserContainer::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<CraftingBrowserContainer>> CRAFTING_BROWSER_MENU = MENU_TYPES.register("crafting_browser", () -> new MenuType<>(CraftingBrowserContainer::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<InterfaceContainer>> INTERFACE_MENU = MENU_TYPES.register("interface", () -> new MenuType<>(InterfaceContainer::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<GeneratorContainer>> GENERATOR_MENU = MENU_TYPES.register("generator", () -> new MenuType<>(GeneratorContainer::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<OrbDuplicationRecipe>> ORB_DUPLICATION = RECIPE_SERIALIZERS.register("orb_duplication", () -> new SimpleCraftingRecipeSerializer<>(OrbDuplicationRecipe::new));

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> ENDERRIFT_CREATIVE_TAB = CREATIVE_TABS.register("ender_rift_tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
                    .icon(() -> new ItemStack(RIFT_ORB.get()))
                    .title(Component.translatable("itemGroup.tabEnderRift"))
                    .displayItems((featureFlags, output) -> {
                        output.accept(RIFT_ITEM.get());
                        output.accept(RIFT_ORB.get());
                        output.accept(INTERFACE_ITEM.get());
                        output.accept(BROWSER_ITEM.get());
                        output.accept(CRAFTING_BROWSER_ITEM.get());
                        output.accept(PROXY_ITEM.get());
                        output.accept(DRIVER_ITEM.get());
                        output.accept(GENERATOR_ITEM.get());
                    }).build()
            );

    public EnderRiftMod(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::interComms);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::registerPackets);

        NeoForge.EVENT_BUS.addListener(this::commandEvent);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigValues.SERVER_SPEC);
        //modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigData.CLIENT_SPEC);

        if (ModList.get().isLoaded("ae2"))
        {
            Ae2Integration.init(modEventBus);
        }
    }

    private void registerPackets(RegisterPayloadHandlerEvent event)
    {
        final IPayloadRegistrar registrar = event.registrar(MODID).versioned("1.1.0");
        registrar.play(ClearCraftingGrid.ID, ClearCraftingGrid::new, play -> play.server(ClearCraftingGrid::handle));
        registrar.play(SendSlotChanges.ID, SendSlotChanges::new, play -> play.client(SendSlotChanges::handle));
        registrar.play(SetVisibleSlots.ID, SetVisibleSlots::new, play -> play.server(SetVisibleSlots::handle));
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
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
                                .then(Commands.argument("riftId", UuidArgument.uuid())
                                        .requires(cs -> cs.hasPermission(3)) //permission
                                        .executes(this::locateSpecificRift)
                                )
                                .requires(cs -> cs.hasPermission(3)) //permission
                                .executes(this::locateAllRifts)
                        )
        );
    }

    private int locateSpecificRift(CommandContext<CommandSourceStack> context)
    {
        if (!RiftStorage.isAvailable())
        {
            context.getSource().sendFailure(Component.literal("Failed to retrieve RiftStorage"));
            return 0;
        }
        UUID id = context.getArgument("riftId", UUID.class);
        RiftHolder holder = RiftStorage.get().getRift(id);
        if (holder == null || !holder.isValid())
        {
            context.getSource().sendFailure(Component.literal(String.format("Couldn't find rift with id '%s'", id)));
            return 0;
        }
        locateRiftById(context, id, holder.getInventory());
        return 0;
    }

    private void locateRiftById(CommandContext<CommandSourceStack> context, UUID riftId, RiftInventory rift)
    {
        rift.locateListeners(context.getSource().getLevel(), (pos) ->
                context.getSource().sendSuccess(
                        () -> Component.literal(String.format("Found rift with id '%s' at %s %s %s", riftId, pos.getX(), pos.getY(), pos.getZ()))
                                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(Action.RUN_COMMAND, String.format("/tp %s %s %s", pos.getX(), pos.getY(), pos.getZ())))), true));
    }

    private int locateAllRifts(CommandContext<CommandSourceStack> context)
    {
        RiftStorage.get().walkRifts((id, rift) -> locateRiftById(context, id, rift));
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
                    return BuiltInRegistries.BLOCK.entrySet().stream()
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

