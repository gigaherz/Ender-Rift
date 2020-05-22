package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.browser.AbstractBrowserContainer;
import gigaherz.enderRift.automation.browser.BrowserContainer;
import gigaherz.enderRift.generator.GeneratorContainer;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.network.UpdatePowerStatus;
import gigaherz.enderRift.rift.RiftTileEntity;
import gigaherz.enderRift.rift.RiftTileEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EnderRiftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHelper
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(RiftTileEntity.TYPE, RiftTileEntityRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        ModelLoader.addSpecialModel(EnderRiftMod.location("block/sphere"));
    }

    public static void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {

            PlayerEntity entityplayer = minecraft.player;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((AbstractBrowserContainer) entityplayer.openContainer).slotsChanged(message.slotCount, message.indices, message.stacks);
            }
        });
    }

    public static void handleUpdateField(final UpdateField message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {
            PlayerEntity entityplayer = minecraft.player;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((GeneratorContainer) entityplayer.openContainer).updateFields(message.fields);
            }
        });
    }

    public static void handleUpdatePowerStatus(UpdatePowerStatus message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {
            PlayerEntity entityplayer = minecraft.player;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((BrowserContainer) entityplayer.openContainer).updatePowerStatus(message.status);
            }
        });
    }
}
