package gigaherz.enderRift.client;

import gigaherz.enderRift.blocks.TileEnderRift;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.lwjgl.opengl.GL11;

public class TESREnderRift extends TileEntitySpecialRenderer<TileEnderRift>
{
    @Override
    public void renderTileEntityAt(TileEnderRift te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (te.getBlockMetadata() == 0)
            return;

        IFlexibleBakedModel model = RenderingStuffs.loadModel("enderrift:block/sphere.obj");

        bindTexture(TextureMap.locationBlocksTexture);

        long time = te.getWorld().getWorldTime();

        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        for(int i=0;i<100;i+=20)
        {
            float tickTime = ((time % 20 + i) % 100 + partialTicks) / 100.0f;

            float scale = 1.0f + 1.2f * tickTime;
            float alpha = 1.2f - 1.21f * tickTime;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(-0.5, -0.5, -0.5);

            int color = (Math.round(Math.min(255, Math.max(0, alpha*255))) << 24) | 0xFFFFFF;

            RenderingStuffs.renderModel(model, color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
