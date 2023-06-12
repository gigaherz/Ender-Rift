package dev.gigaherz.enderrift.automation.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CraftingBrowserScreen extends AbstractBrowserScreen<CraftingBrowserContainer>
{
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/crafting_browser.png");
    private final Inventory inventory;

    public CraftingBrowserScreen(CraftingBrowserContainer container, Inventory playerInventory, Component title)
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

        addRenderableWidget(Button.builder(Component.literal("▴"), (btn) -> {
            clearCraftingGrid(false);
        }).pos(leftPos + 85, topPos + 75).size(9, 9).build());

        addRenderableWidget(Button.builder(Component.literal("▾"), (btn) -> {
            clearCraftingGrid(true);
        }).pos(leftPos + 85, topPos + 75 + 45).size(9, 9).build());
    }

    private void clearCraftingGrid(boolean toPlayer)
    {
        getMenu().clearCraftingGrid(inventory.player, toPlayer);
    }
}