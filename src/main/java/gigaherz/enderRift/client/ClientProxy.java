package gigaherz.enderRift.client;

import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.IModProxy;
import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.gui.ContainerBrowser;
import gigaherz.enderRift.gui.ContainerGenerator;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
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
        OBJLoader.INSTANCE.addDomain(EnderRiftMod.MODID);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderRift.class, new RenderRift());
        registerItemModel(EnderRiftMod.riftOrb, 0, "item_rift");
        registerBlockModelAsItem(EnderRiftMod.rift, "blockEnderRift");
        registerBlockModelAsItem(EnderRiftMod.riftInterface, "blockInterface");
        registerBlockModelAsItem(EnderRiftMod.browser, 0, "blockBrowser", "crafting=false,facing=south");
        registerBlockModelAsItem(EnderRiftMod.browser, 1, "blockBrowser", "crafting=true,facing=south");
        registerBlockModelAsItem(EnderRiftMod.extension, "blockProxy");

        if (ConfigValues.EnableRudimentaryGenerator)
        {
            registerBlockModelAsItem(EnderRiftMod.generator, "blockGenerator");
        }

        MinecraftForge.EVENT_BUS.register(this);
        RenderingStuffs.init();
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(new ResourceLocation(EnderRiftMod.MODID + ":blocks/rift_aura"));
    }

    public void registerBlockModelAsItem(final Block block, final String blockName)
    {
        registerBlockModelAsItem(block, 0, blockName, "inventory");
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String blockName, final String variant)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(EnderRiftMod.MODID + ":" + blockName, variant));
    }

    public void registerItemModel(final Item item, int meta, final String itemName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(EnderRiftMod.MODID + ":" + itemName, "inventory"));
    }

    @Override
    public void init()
    {
    }

    @Override
    public void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientProxy.this.handleSendSlotChanges_internal(message));
    }

    void handleSendSlotChanges_internal(SendSlotChanges message)
    {
        Minecraft gameController = Minecraft.getMinecraft();

        EntityPlayer entityplayer = gameController.thePlayer;

        if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
        {
            ((ContainerBrowser) entityplayer.openContainer).slotsChanged(message.slotCount, message.indices, message.stacks);
        }
    }

    @Override
    public void handleUpdateField(final UpdateField message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> ClientProxy.this.handleUpdateField_internal(message));
    }

    void handleUpdateField_internal(UpdateField message)
    {
        Minecraft gameController = Minecraft.getMinecraft();

        EntityPlayer entityplayer = gameController.thePlayer;

        if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
        {
            ((ContainerGenerator) entityplayer.openContainer).updateFields(message.fields);
        }
    }
}
