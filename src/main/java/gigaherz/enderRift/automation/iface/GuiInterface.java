package gigaherz.enderRift.automation.iface;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiInterface extends GuiContainer
{
    protected InventoryPlayer player;
    protected TileInterface tile;

    private static final ResourceLocation guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/interface.png");
    private static final String textFilters = "text." + EnderRiftMod.MODID + ".interface.filters";
    private static final String textName = "container." + EnderRiftMod.MODID + ".interface";

    public GuiInterface(InventoryPlayer playerInventory, TileInterface tileEntity)
    {
        super(new ContainerInterface(tileEntity, playerInventory));
        player = playerInventory;
        tile = tileEntity;
        ySize = 176;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = I18n.format(textName);
        fontRenderer.drawString(name, (xSize - fontRenderer.getStringWidth(name)) / 2, 6, 0x404040);
        fontRenderer.drawString(I18n.format(this.player.getName()), 8, ySize - 96 + 2, 0x404040);
        fontRenderer.drawString(I18n.format(textFilters), 8, 20, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(guiTextureLocation);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}