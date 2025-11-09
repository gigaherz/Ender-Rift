package dev.gigaherz.enderrift.client;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.AbstractBrowserMenu;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.rift.RiftRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;


@EventBusSubscriber(value = Dist.CLIENT, modid = EnderRiftMod.MODID)
public class ClientHelper
{
    public static final StandaloneModelKey<QuadCollection> SPHERE = new StandaloneModelKey<>(() -> "EnderRift Sphere");

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterStandalone event)
    {
        event.register(SPHERE, SimpleUnbakedStandaloneModel.quadCollection(EnderRiftMod.location("block/sphere")));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        BlockEntityRenderers.register(EnderRiftMod.RIFT_BLOCK_ENTITY.get(), RiftRenderer::new);
    }

    public static void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {
            Player entityplayer = minecraft.player;

            if (entityplayer != null && entityplayer.containerMenu != null && entityplayer.containerMenu.containerId == message.windowId())
            {
                ((AbstractBrowserMenu) entityplayer.containerMenu).slotsChanged(message.slotCount(), message.indices(), message.stacks(), message.stackSizes());
            }
        });
    }
}