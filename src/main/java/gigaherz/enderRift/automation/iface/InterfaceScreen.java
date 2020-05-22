package gigaherz.enderRift.automation.iface;

import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class InterfaceScreen extends ContainerScreen<InterfaceContainer>
{
    private static final ResourceLocation guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/interface.png");
    private static final String textFilters = "text.enderrift.interface.filters";

    public InterfaceScreen(InterfaceContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        ySize = 176;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = title.getFormattedText();
        font.drawString(name, (xSize - font.getStringWidth(name)) / 2, 6, 0x404040);
        font.drawString(playerInventory.getName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);
        font.drawString(I18n.format(textFilters), 8, 20, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        minecraft.textureManager.bindTexture(guiTextureLocation);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}