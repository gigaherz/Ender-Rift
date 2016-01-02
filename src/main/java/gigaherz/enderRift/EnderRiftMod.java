package gigaherz.enderRift;

import gigaherz.enderRift.blocks.*;
import gigaherz.enderRift.gui.GuiHandler;
import gigaherz.enderRift.items.ItemBlockInterface;
import gigaherz.enderRift.items.ItemEnderRift;
import gigaherz.enderRift.recipe.RecipesRiftDuplication;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EnderRiftMod.MODID, version = EnderRiftMod.VERSION, dependencies = "after:Waila;after:NotEnoughItems")
public class EnderRiftMod {
    public static final String MODID = "enderrift";
    public static final String VERSION = "@VERSION";

    public static BlockEnderRift blockEnderRift;
    public static Block blockStructure;
    public static Block blockInterface;
    public static Item itemEnderRift;
    public static CreativeTabs tabEnderRift;

    @Mod.Instance(value = EnderRiftMod.MODID)
    public static EnderRiftMod instance;

    @SidedProxy(clientSide = "gigaherz.enderRift.client.ClientProxy", serverSide = "gigaherz.enderRift.server.ServerProxy")
    public static IModProxy proxy;

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
            public Item getTabIconItem() {
                return itemEnderRift;
            }
        };

        itemEnderRift = new ItemEnderRift().setUnlocalizedName(MODID + ".itemEnderRift");
        GameRegistry.registerItem(itemEnderRift, "itemEnderRift");

        blockEnderRift = new BlockEnderRift();
        GameRegistry.registerBlock(blockEnderRift, "blockEnderRift");

        blockStructure = new BlockStructure();
        GameRegistry.registerBlock(blockStructure, "blockStructure");

        blockInterface = new BlockInterface();
        GameRegistry.registerBlock(blockInterface, ItemBlockInterface.class, "blockInterface");

        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, "tileStructureCorner");
        GameRegistry.registerTileEntity(TileInterface.class, "tileInterface");

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        // Recipes
        GameRegistry.addRecipe(new ItemStack(itemEnderRift),
                "aba",
                "bcb",
                "aba",
                'a', Items.magma_cream,
                'b', Items.ender_pearl,
                'c', Items.ender_eye);

        GameRegistry.addRecipe(new ItemStack(blockEnderRift),
                "oho",
                "r r",
                "oco",
                'o', Blocks.obsidian,
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'c', Blocks.ender_chest);

        GameRegistry.addRecipe(new ItemStack(blockInterface),
                "iri",
                "rhr",
                "iri",
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'i', Blocks.iron_block);

        GameRegistry.addRecipe(new RecipesRiftDuplication());

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.WailaProvider.callbackRegister");
    }
}
