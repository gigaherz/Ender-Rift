package dev.gigaherz.enderrift.client;

import dev.gigaherz.enderrift.rift.RiftRenderer;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.AbstractBrowserContainer;
import dev.gigaherz.enderrift.automation.browser.BrowserContainer;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.rift.RiftBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EnderRiftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHelper
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.addSpecialModel(EnderRiftMod.location("block/sphere"));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        BlockEntityRenderers.register(RiftBlockEntity.TYPE, RiftRenderer::new);
    }

    public static void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {
            Player entityplayer = minecraft.player;

            if (entityplayer != null && entityplayer.containerMenu != null && entityplayer.containerMenu.containerId == message.windowId)
            {
                ((AbstractBrowserContainer) entityplayer.containerMenu).slotsChanged(message.slotCount, message.indices, message.stacks);
            }
        });
    }
}