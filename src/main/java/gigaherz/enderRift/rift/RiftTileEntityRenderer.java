package gigaherz.enderRift.rift;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class RiftTileEntityRenderer extends TileEntityRenderer<RiftTileEntity>
{
    private final Random random = new Random();

    private static final List<Direction> DIRECTIONS_AND_NULL = Lists.newArrayList(Direction.values());
    {
        DIRECTIONS_AND_NULL.add(null);
    }

    @Override
    public void render(RiftTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (!te.getBlockState().get(RiftBlock.ASSEMBLED))
            return;

        float lastPoweringState = te.getLastPoweringState();
        float nextPoweringState = te.getPoweringState();
        float poweringState = lerp(lastPoweringState, nextPoweringState, partialTicks);

        PlayerEntity player = Minecraft.getInstance().player;

        Vec3d eyePosition = player.getEyePosition(partialTicks);
        BlockPos tePos = te.getPos();
        double ty = eyePosition.getY() - (tePos.getY() + 0.5);
        double tx = eyePosition.getX() - (tePos.getX() + 0.5);
        double tz = eyePosition.getZ() - (tePos.getZ() + 0.5);
        float yaw = (float) Math.atan2(tz, tx);
        float xz = (float) Math.sqrt(tx * tx + tz * tz);
        float pitch = (float) Math.atan2(ty, xz);

        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.translated(0.5, 0.5, 0.5);
        GlStateManager.rotatef((float) Math.toDegrees(-yaw), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef((float) Math.toDegrees(pitch), 0.0F, 0.0F, 1.0F);
        GlStateManager.translated(-0.5, -0.5, -0.5);

        int step_time = 20;
        int steps = 5;
        int time_loop = step_time * steps;

        long time = te.getWorld().getGameTime();
        int tm = (int) (time % step_time);

        float c0 = 1.0f / steps;
        float c1 = 1.0f / (1 - c0);

        GlStateManager.color4f(1,1,1,1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(EnderRiftMod.location("block/sphere"));

        for (int i = 0; i < steps; i++)
        {
            float progress0 = ((tm + i * step_time) % time_loop + partialTicks) / time_loop;
            float progress1 = (progress0 - c0) * c1;

            float scale = (1.0f + poweringState) + (0.6f + poweringState) * progress0;

            GlStateManager.pushMatrix();
            GlStateManager.translated(0.5, 0.5, 0.5);
            GlStateManager.scalef(scale, scale, scale);
            GlStateManager.translated(-0.5, -0.5, -0.5);

            int a = Math.round(Math.min(255, Math.max(0, (1 - progress1) * 255)));
            int rgb = Math.round(Math.min(255, Math.max(0, progress1 * 255)));
            int color = (a << 24) | (rgb << 16) | (rgb << 8) | (rgb);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
            for (Direction d : DIRECTIONS_AND_NULL)
            {
                for (BakedQuad bakedquad : model.getQuads(null, d, random, EmptyModelData.INSTANCE))
                {
                    //buffer.addVertexData(bakedquad.getVertexData());
                    LightUtil.renderQuadColor(buffer, bakedquad, color);
                }
            }
            tessellator.draw();

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private float lerp(float a, float b, float p)
    {
        return a + p * (b - a);
    }
}
