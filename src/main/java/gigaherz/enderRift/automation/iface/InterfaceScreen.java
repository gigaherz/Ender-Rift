package gigaherz.enderRift.automation.iface;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
        this.field_238745_s_ = this.ySize - 94;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
        font.drawString(matrixStack, I18n.format(textFilters), 8, 20, 0x404040);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        minecraft.textureManager.bindTexture(guiTextureLocation);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}