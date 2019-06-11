package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.browser.ContainerBrowser;
import gigaherz.enderRift.generator.ContainerGenerator;
import gigaherz.enderRift.network.SendSlotChanges;
import gigaherz.enderRift.network.UpdateField;
import gigaherz.enderRift.network.UpdatePowerStatus;
import gigaherz.enderRift.rift.TileEnderRift;
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
public class ClientProxy
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModelHandle.init();

        OBJLoader.INSTANCE.addDomain(EnderRiftMod.MODID);

        /*registerItemModel(EnderRiftMod.riftOrb);
        registerBlockModelAsItem(EnderRiftMod.rift);
        registerBlockModelAsItem(EnderRiftMod.structure);
        registerBlockModelAsItem(EnderRiftMod.riftInterface);
        registerBlockModelAsItem(EnderRiftMod.browser, 0, "crafting=false,facing=south");
        registerBlockModelAsItem(EnderRiftMod.browser, 1, "crafting=true,facing=south");
        registerBlockModelAsItem(EnderRiftMod.extension);
        registerBlockModelAsItem(EnderRiftMod.generator);
        registerBlockModelAsItem(EnderRiftMod.driver);*/

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderRift.class, new RenderRift());
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
                ((ContainerBrowser) entityplayer.openContainer).slotsChanged(message.slotCount, message.indices, message.stacks);
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
                ((ContainerGenerator) entityplayer.openContainer).updateFields(message.fields);
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
                ((ContainerBrowser) entityplayer.openContainer).updatePowerStatus(message.status);
            }
        });
    }
}
