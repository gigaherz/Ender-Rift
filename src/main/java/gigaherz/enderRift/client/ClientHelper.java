package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.browser.BrowserContainer;
import gigaherz.enderRift.generator.GeneratorContainer;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.network.UpdatePowerStatus;
import gigaherz.enderRift.rift.RiftTileEntityRenderer;
import gigaherz.enderRift.rift.RiftTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientHelper
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(EnderRiftMod.MODID);
    }

    public static void initLate()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(RiftTileEntity.class, new RiftTileEntityRenderer());
        ModelHandle.init();
    }

    public static void registerBooks()
    {
        InterModComms.sendTo("gbook", "registerBook", () -> EnderRiftMod.location("xml/book.xml"));
    }

    public static void handleSendSlotChanges(final SendSlotChanges message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() ->
        {

            PlayerEntity entityplayer = minecraft.player;

            if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == message.windowId)
            {
                ((BrowserContainer) entityplayer.openContainer).slotsChanged(message.slotCount, message.indices, message.stacks);
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
