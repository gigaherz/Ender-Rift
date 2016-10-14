package gigaherz.enderRift.client;

import gigaherz.common.client.ModelHandle;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.TileEnderRift;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class RenderRift extends TileEntitySpecialRenderer<TileEnderRift>
{
    private ModelHandle modelHandle = ModelHandle.of(EnderRiftMod.location("block/sphere.obj"));

    @Override
    public void renderTileEntityAt(TileEnderRift te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (te.getBlockMetadata() == 0)
            return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        double ty = y + 0.5 - player.getEyeHeight();
        double tx = x + 0.5;
        double tz = z + 0.5;
        float yaw = (float) Math.atan2(tz, tx);
        float xz = (float) Math.sqrt(tx * tx + tz * tz);
        float pitch = (float) Math.atan2(ty, xz);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.rotate((float) Math.toDegrees(-yaw), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) Math.toDegrees(pitch), 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        int step_time = 20;
        int steps = 5;
        int time_loop = step_time * steps;

        long time = te.getWorld().getTotalWorldTime();
        int tm = (int) (time % step_time);

        float c0 = 1.0f / steps;
        float c1 = 1.0f / (1 - c0);

        for (int i = 0; i < steps; i++)
        {
            float progress0 = ((tm + i * step_time) % time_loop + partialTicks) / time_loop;
            float progress1 = (progress0 - c0) * c1;

            float scale = 2.0f + 1.6f * progress0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(-0.5, -0.5, -0.5);

            int a = Math.round(Math.min(255, Math.max(0, (1 - progress1) * 255)));
            int b = Math.round(Math.min(255, Math.max(0, progress1 * 255)));
            int g = Math.round(Math.min(255, Math.max(0, progress1 * 255)));
            int r = Math.round(Math.min(255, Math.max(0, progress1 * 255)));
            int color = (a << 24) | (b << 16) | (g << 8) | (r);

            modelHandle.render(color);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
