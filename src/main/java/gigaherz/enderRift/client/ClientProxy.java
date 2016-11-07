package gigaherz.enderRift.client;

import gigaherz.common.client.ModelHandle;
import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.IModProxy;
import gigaherz.enderRift.automation.browser.ContainerBrowser;
import gigaherz.enderRift.generator.ContainerGenerator;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.rift.TileEnderRift;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static gigaherz.common.client.ModelHelpers.registerBlockModelAsItem;
import static gigaherz.common.client.ModelHelpers.registerItemModel;

@Mod.EventBusSubscriber
public class ClientProxy implements IModProxy
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(EnderRiftMod.MODID);

        registerItemModel(EnderRiftMod.riftOrb);
        registerBlockModelAsItem(EnderRiftMod.rift);
        registerBlockModelAsItem(EnderRiftMod.riftInterface);
        registerBlockModelAsItem(EnderRiftMod.browser, 0, "crafting=false,facing=south");
        registerBlockModelAsItem(EnderRiftMod.browser, 1, "crafting=true,facing=south");
        registerBlockModelAsItem(EnderRiftMod.extension);
        registerBlockModelAsItem(EnderRiftMod.generator);
        registerBlockModelAsItem(EnderRiftMod.driver);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderRift.class, new RenderRift());
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(new ResourceLocation(EnderRiftMod.MODID + ":blocks/rift_aura"));
    }

    @Override
    public void preInit()
    {
        ModelHandle.init();
    }

    @Override
    public void init()
    {
    }

    @Override
    public void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            Minecraft gameController = Minecraft.getMinecraft();

            EntityPlayer entityplayer = gameController.thePlayer;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((ContainerBrowser) entityplayer.openContainer).slotsChanged(message.slotCount, message.indices, message.stacks);
            }
        });
    }

    @Override
    public void handleUpdateField(final UpdateField message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            Minecraft gameController = Minecraft.getMinecraft();

            EntityPlayer entityplayer = gameController.thePlayer;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((ContainerGenerator) entityplayer.openContainer).updateFields(message.fields);
            }
        });
    }
}
