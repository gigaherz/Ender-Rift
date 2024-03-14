package dev.gigaherz.enderrift.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.browser.AbstractBrowserContainer;
import dev.gigaherz.enderrift.network.SendSlotChanges;
import dev.gigaherz.enderrift.rift.RiftRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = EnderRiftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHelper
{
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event)
    {
        event.register(EnderRiftMod.location("block/sphere"));
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

            if (entityplayer != null && entityplayer.containerMenu != null && entityplayer.containerMenu.containerId == message.windowId)
            {
                ((AbstractBrowserContainer) entityplayer.containerMenu).slotsChanged(message.slotCount, message.indices, message.stacks, message.stackSizes);
            }
        });
    }
}