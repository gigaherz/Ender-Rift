package dev.gigaherz.enderrift.automation.browser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.minecraft.client.gui.components.Button.OnPress;

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

        addRenderableWidget(new GuiButtonFlexible(leftPos + 85, topPos + 75, 9, 9, new TextComponent("▴"), (btn) -> {
            clearCraftingGrid(false);
        }));

        addRenderableWidget(new GuiButtonFlexible(leftPos + 85, topPos + 75 + 45, 9, 9, new TextComponent("▾"), (btn) -> {
            clearCraftingGrid(true);
        }));
    }

    private void clearCraftingGrid(boolean toPlayer)
    {
        getMenu().clearCraftingGrid(inventory.player, toPlayer);
    }

    private static class GuiButtonFlexible extends Button
    {
        public GuiButtonFlexible(int x, int y, int widthIn, int heightIn, Component buttonText, OnPress callback)
        {
            super(x, y, widthIn, heightIn, buttonText, callback);
        }

        @Override
        public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            Minecraft minecraft = Minecraft.getInstance();
            Font fontrenderer = minecraft.font;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);

            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

            int i = getYImage(isHovered);

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.blendFunc(770, 771);

            int halfwidth1 = this.width / 2;
            int halfwidth2 = this.width - halfwidth1;
            int halfheight1 = this.height / 2;
            int halfheight2 = this.height - halfheight1;
            blit(matrixStack, x, y, 0,
                    46 + i * 20, halfwidth1, halfheight1);
            blit(matrixStack, x + halfwidth1, y, 200 - halfwidth2,
                    46 + i * 20, halfwidth2, halfheight1);

            blit(matrixStack, x, y + halfheight1,
                    0, 46 + i * 20 + 20 - halfheight2, halfwidth1, halfheight2);
            blit(matrixStack, x + halfwidth1, y + halfheight1,
                    200 - halfwidth2, 46 + i * 20 + 20 - halfheight2, halfwidth2, halfheight2);

            int textColor = 14737632;

            if (packedFGColor != 0)
            {
                textColor = packedFGColor;
            }
            else if (!this.visible)
            {
                textColor = 10526880;
            }
            else if (this.isHovered)
            {
                textColor = 16777120;
            }

            this.drawCenteredString(matrixStack, fontrenderer, getMessage(), x + halfwidth2, y + (this.height - 8) / 2, textColor);
        }
    }
}