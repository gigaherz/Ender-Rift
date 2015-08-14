package gigaherz.enderRift;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gigaherz.enderRift.blocks.*;
import gigaherz.enderRift.items.ItemEnderRift;
import gigaherz.enderRift.recipe.RecipesRiftDuplication;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EnderRiftMod.MODID, version = EnderRiftMod.VERSION, dependencies = "after:Waila;after:NotEnoughItems")
public class EnderRiftMod
{
    public static final String MODID = "enderRift";
    public static final String VERSION = "@VERSION";

    public static final String CHANNEL = "EnderRift";

    public static BlockEnderRift blockEnderRift;
    public static Block blockStructureInvisible;
    public static Block blockStructureCorner;
    public static Item itemEnderRift;
    public static CreativeTabs tabEnderRift;

    @Mod.Instance(value = EnderRiftMod.MODID)
    public static EnderRiftMod instance;

    @SidedProxy(clientSide = "gigaherz.enderRift.client.ClientProxy", serverSide = "gigaherz.enderRift.server.ServerProxy")
    public static IModProxy proxy;

    public static final Logger logger = LogManager.getLogger(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        ConfigValues.readConfig(config);

        tabEnderRift = new CreativeTabs("tabEnderRift")
        {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem()
            {
                return itemEnderRift;
            }
        };

        itemEnderRift = new ItemEnderRift().setUnlocalizedName(MODID + ".itemEnderRift");
        GameRegistry.registerItem(itemEnderRift, "itemEnderRift");

        blockEnderRift = (BlockEnderRift) new BlockEnderRift().setHardness(0.5F).setStepSound(Block.soundTypeMetal).setBlockName(MODID + ".blockEnderRift");
        GameRegistry.registerBlock(blockEnderRift, "blockEnderRift");

        blockStructureInvisible = new BlockStructureInvisible().setHardness(0.5F).setStepSound(Block.soundTypeMetal).setBlockName(MODID + ".blockStructureInvisible");
        GameRegistry.registerBlock(blockStructureInvisible, "blockStructureInvisible");

        blockStructureCorner = new BlockStructureInvisibleCorner().setHardness(0.5F).setStepSound(Block.soundTypeMetal).setBlockName(MODID + ".blockStructureCorner");
        GameRegistry.registerBlock(blockStructureCorner, "blockStructureCorner");

        GameRegistry.registerTileEntity(TileEnderRift.class, "tileEnderRift");
        GameRegistry.registerTileEntity(TileEnderRiftCorner.class, "tileStructureCorner");

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

        proxy.init();

        // Recipes
        GameRegistry.addRecipe(new ItemStack(itemEnderRift, 1),
                "aba",
                "bcb",
                "aba",
                'a', Items.magma_cream,
                'b', Items.ender_pearl,
                'c', Items.ender_eye);

        GameRegistry.addRecipe(new ItemStack(blockEnderRift, 1),
                "oho",
                "r r",
                "oco",
                'o', Blocks.obsidian,
                'h', Blocks.hopper,
                'r', Blocks.redstone_block,
                'c', Blocks.ender_chest);

        GameRegistry.addRecipe(new RecipesRiftDuplication());

        FMLInterModComms.sendMessage("Waila", "register", "gigaherz.enderRift.WailaProvider.callbackRegister");
    }
}
