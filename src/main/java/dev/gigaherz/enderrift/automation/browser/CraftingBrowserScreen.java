package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CraftingBrowserScreen extends AbstractBrowserScreen<CraftingBrowserMenu>
{
    private static final ResourceLocation BACKGROUND_TEXTURE = EnderRiftMod.location("textures/gui/crafting_browser.png");
    private final Inventory inventory;

    public CraftingBrowserScreen(CraftingBrowserMenu container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageWidth = 194;
        imageHeight = 226;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventory = playerInventory;
    }

    @Override
    protected ResourceLocation getBackgroundTexture()
    {
        return BACKGROUND_TEXTURE;
    }

    @Override
    public void init()
    {
        super.init();

        addRenderableWidget(Button.builder(Component.literal("\u25B4"), (btn) -> {
            clearCraftingGrid(false);
        }).pos(leftPos + 85, topPos + 75).size(9, 9).build());

        addRenderableWidget(Button.builder(Component.literal("\u25BE"), (btn) -> {
            clearCraftingGrid(true);
        }).pos(leftPos + 85, topPos + 75 + 45).size(9, 9).build());
    }

    private void clearCraftingGrid(boolean toPlayer)
    {
        getMenu().clearCraftingGrid(inventory.player, toPlayer);
    }
}