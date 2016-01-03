package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.IModProxy;
import gigaherz.enderRift.blocks.TileEnderRift;
import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        OBJLoader.instance.addDomain(EnderRiftMod.MODID);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderRift.class, new TESREnderRift());
        registerItemModel(EnderRiftMod.itemEnderRift, 0, "item_rift");
        registerBlockModelAsItem(EnderRiftMod.blockEnderRift, "blockEnderRift");
        registerBlockModelAsItem(EnderRiftMod.blockStructure, "blockStructure");
        registerBlockModelAsItem(EnderRiftMod.blockInterface, "blockInterface");
        registerBlockModelAsItem(EnderRiftMod.blockGenerator, "blockGenerator");

        MinecraftForge.EVENT_BUS.register(this);
        RenderingStuffs.init();
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent event)
    {
        event.map.registerSprite(new ResourceLocation(EnderRiftMod.MODID + ":blocks/rift_aura"));
    }

    public void registerBlockModelAsItem(final Block block, final String blockName)
    {
        registerBlockModelAsItem(block, 0, blockName);
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String blockName)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(EnderRiftMod.MODID + ":" + blockName, "inventory"));
    }

    public void registerItemModel(final Item item, int meta, final String itemName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(EnderRiftMod.MODID + ":" + itemName, "inventory"));
        ModelBakery.addVariantName(item, EnderRiftMod.MODID + ":" + itemName);
    }

    @Override
    public void init()
    {
    }
}
