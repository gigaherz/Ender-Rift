package gigaherz.enderRift;

import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.enderRift.automation.CapabilityAutomation;
import gigaherz.enderRift.blocks.*;
import gigaherz.enderRift.gui.GuiHandler;
import gigaherz.enderRift.items.ItemEnderRift;
import gigaherz.enderRift.network.*;
import gigaherz.enderRift.recipe.RecipesRiftDuplication;
import gigaherz.enderRift.rift.RiftStructure;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
        dependencies = "after:Waila",
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

        CapabilityEnergy.enable();
        CapabilityAutomation.register();

        tabEnderRift = new CreativeTabs("tabEnderRift")
        {
            @Override
            public Item getTabIconItem()
            {
                return riftOrb;
            }
        };

        riftOrb = new ItemEnderRift("itemEnderRift").setUnlocalizedName(MODID + ".itemEnderRift");
        GameRegistry.registerItem(riftOrb);

        rift = new BlockEnderRift("blockEnderRift");
        GameRegistry.registerBlock(rift);
        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");

        structure = new BlockStructure("blockStructure");
        GameRegistry.registerBlock(structure, (Class<? extends ItemBlock>) null);
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, "tileStructureCorner");

        riftInterface = new BlockInterface("blockInterface");
        GameRegistry.registerBlock(riftInterface);
        GameRegistry.registerTileEntity(TileInterface.class, "tileInterface");

        browser = new BlockBrowser("blockBrowser");
        GameRegistry.registerBlock(browser, BlockBrowser.AsItem.class);
        GameRegistry.registerTileEntity(TileBrowser.class, "tileBrowser");

        generator = new BlockGenerator("blockGenerator");
        GameRegistry.registerBlock(generator);
        GameRegistry.registerTileEntity(TileGenerator.class, "tileGenerator");

        extension = new BlockProxy("blockProxy");
        GameRegistry.registerBlock(extension);
        GameRegistry.registerTileEntity(TileProxy.class, "tileProxy");

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

        GameRegistry.addRecipe(new ItemStack(extension),
                "iri",
                "rhr",
                "iri",
                'h', Blocks.hopper,
                'r', Items.redstone,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(riftInterface),
                "ir ",
                "rer",
                "ir ",
                'e', extension,
                'r', Items.redstone,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(browser),
                "ig ",
                "geg",
                "ig ",
                'e', extension,
                'g', Items.glowstone_dust,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(generator),
                "iri",
                "rwr",
                "ifi",
                'f', Blocks.furnace,
                'w', Items.water_bucket,
                'r', Items.redstone,
                'i', Items.iron_ingot);

        GameRegistry.addRecipe(new ItemStack(browser, 1, 1),
                "gdg",
                "dbd",
                "gcg",
                'g', Items.gold_ingot,
                'd', Items.diamond,
                'c', Blocks.crafting_table,
                'b', new ItemStack(browser, 1, 0));

        GameRegistry.addRecipe(new RecipesRiftDuplication());
        RecipeSorter.register(MODID + ":rift_duplication", RecipesRiftDuplication.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.WailaProviders.callbackRegister");
    }
}
