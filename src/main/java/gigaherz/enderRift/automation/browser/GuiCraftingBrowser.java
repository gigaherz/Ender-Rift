package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiCraftingBrowser extends GuiBrowser
{
    private static final ResourceLocation backgroundTexture = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/crafting_browser.png");

    public GuiCraftingBrowser(EntityPlayer player, TileBrowser tileEntity)
    {
        super(new ContainerCraftingBrowser(tileEntity, player, true));
        this.player = player.inventory;
        xSize = 194;
        ySize = 226;
    }

    @Override
    protected ResourceLocation getBackgroundTexture()
    {
        return backgroundTexture;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        GuiButton btn = new GuiButtonFlexible(2, guiLeft + 85, guiTop + 75, 9, 9, "x");
        buttonList.add(btn);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == 2)
        {
            clearCraftingGrid();
        }
        else
        {
            super.actionPerformed(guibutton);
        }
    }

    private void clearCraftingGrid()
    {
        ((ContainerCraftingBrowser) inventorySlots).clearCraftingGrid(player.player);
    }

    private static class GuiButtonFlexible extends GuiButton
    {
        public GuiButtonFlexible(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (visible)
            {
                FontRenderer fontrenderer = mc.fontRenderer;
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

                int i = getHoverState(hovered);

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.blendFunc(770, 771);

                int halfwidth1 = this.width / 2;
                int halfwidth2 = this.width - halfwidth1;
                int halfheight1 = this.height / 2;
                int halfheight2 = this.height - halfheight1;
                drawTexturedModalRect(x, y, 0,
                        46 + i * 20, halfwidth1, halfheight1);
                drawTexturedModalRect(x + halfwidth1, y, 200 - halfwidth2,
                        46 + i * 20, halfwidth2, halfheight1);

                drawTexturedModalRect(x, y + halfheight1,
                        0, 46 + i * 20 + 20 - halfheight2, halfwidth1, halfheight2);
                drawTexturedModalRect(x + halfwidth1, y + halfheight1,
                        200 - halfwidth2, 46 + i * 20 + 20 - halfheight2, halfwidth2, halfheight2);

                int textColor = 14737632;

                if (packedFGColour != 0)
                {
                    textColor = packedFGColour;
                }
                else if (!this.enabled)
                {
                    textColor = 10526880;
                }
                else if (this.hovered)
                {
                    textColor = 16777120;
                }

                this.drawCenteredString(fontrenderer, displayString, x + halfwidth2, y + (this.height - 8) / 2, textColor);
            }
        }
    }
}
