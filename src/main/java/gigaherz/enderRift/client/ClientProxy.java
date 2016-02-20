package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.IModProxy;
import gigaherz.enderRift.blocks.TileEnderRift;
import gigaherz.enderRift.network.SetSpecialSlot;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        registerItemModel(EnderRiftMod.riftOrb, 0, "item_rift");
        registerBlockModelAsItem(EnderRiftMod.rift, "blockEnderRift");
        registerBlockModelAsItem(EnderRiftMod.structure, "blockStructure");
        registerBlockModelAsItem(EnderRiftMod.riftInterface, "blockInterface");
        registerBlockModelAsItem(EnderRiftMod.generator, "blockGenerator");
        registerBlockModelAsItem(EnderRiftMod.browser, "blockBrowser");
        registerBlockModelAsItem(EnderRiftMod.extension, "blockProxy");

        MinecraftForge.EVENT_BUS.register(this);
        RenderingStuffs.init();
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
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
    }

    @Override
    public void init()
    {
    }

    @Override
    public void handleSetSpecialSlot(final SetSpecialSlot message)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                ClientProxy.this.handleSetSpecialSlot2(message);
            }
        });
    }

    void handleSetSpecialSlot2(SetSpecialSlot message)
    {
        Minecraft gameController = Minecraft.getMinecraft();

        EntityPlayer entityplayer = gameController.thePlayer;

        if (message.windowId == -1)
        {
            entityplayer.inventory.setItemStack(message.stack);
        }
        else
        {
            boolean flag = false;

            if (gameController.currentScreen instanceof GuiContainerCreative)
            {
                GuiContainerCreative guicontainercreative = (GuiContainerCreative) gameController.currentScreen;
                flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (message.windowId == 0 && message.slot >= 36 && message.slot < 45)
            {
                ItemStack itemstack = entityplayer.inventoryContainer.getSlot(message.slot).getStack();

                if (message.stack != null && (itemstack == null || itemstack.stackSize < message.stack.stackSize))
                {
                    message.stack.animationsToGo = 5;
                }

                entityplayer.inventoryContainer.putStackInSlot(message.slot, message.stack);
            }
            else if (message.windowId == entityplayer.openContainer.windowId && (message.windowId != 0 || !flag))
            {
                entityplayer.openContainer.putStackInSlot(message.slot, message.stack);
            }
        }
    }
}
