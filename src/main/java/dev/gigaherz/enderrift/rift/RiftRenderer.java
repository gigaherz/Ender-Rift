package dev.gigaherz.enderrift.rift;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

public class RiftRenderer
        implements BlockEntityRenderer<RiftBlockEntity>
{
    private final RandomSource random = RandomSource.create();

    private static final List<Direction> DIRECTIONS_AND_NULL = Lists.newArrayList(Direction.values());

    static
    {
        DIRECTIONS_AND_NULL.add(null);
    }

    public RiftRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(RiftBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn)
    {
        if (!te.getBlockState().getValue(RiftBlock.ASSEMBLED))
            return;

        float lastPoweringState = te.getLastPoweringState();
        float nextPoweringState = te.getPoweringState();
        float poweringState = lerp(lastPoweringState, nextPoweringState, partialTicks);

        Minecraft minecraft = Minecraft.getInstance();

        Camera renderInfo = minecraft.gameRenderer.getMainCamera();

        BlockPos tePos = te.getBlockPos();
        Vec3 cameraPos = renderInfo.getPosition();

        double ty = cameraPos.y() - (tePos.getY() + 0.5);
        double tx = cameraPos.x() - (tePos.getX() + 0.5);
        double tz = cameraPos.z() - (tePos.getZ() + 0.5);
        float yaw = (float) Math.atan2(tz, tx);
        float xz = (float) Math.sqrt(tx * tx + tz * tz);
        float pitch = (float) Math.atan2(ty, xz);

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(Axis.YP.rotation(-yaw));
        matrixStack.mulPose(Axis.ZP.rotation(pitch));
        matrixStack.translate(-0.5, -0.5, -0.5);

        int step_time = 20;
        int steps = 5;
        int time_loop = step_time * steps;

        long time = te.getLevel().getGameTime();
        int tm = (int) (time % step_time);

        float c0 = 1.0f / steps;
        float c1 = 1.0f / (1 - c0);

        boolean isFabulous = minecraft.options.graphicsMode().get().equals(GraphicsStatus.FABULOUS);

        RenderType type = isFabulous ?
                RenderType.translucentMovingBlock() :
                RenderType.translucent();

        VertexConsumer buffer = iRenderTypeBuffer.getBuffer(type);
        BakedModel model = minecraft.getModelManager().getModel(ModelResourceLocation.standalone(EnderRiftMod.location("block/sphere")));

        for (int i = 0; i < steps; i++)
        {
            float progress0 = ((tm + i * step_time) % time_loop + partialTicks) / time_loop;
            float progress1 = (progress0 - c0) * c1;

            float scale = (1.0f + poweringState) + (0.6f + poweringState) * progress0;

            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.scale(scale, scale, scale);
            matrixStack.translate(-0.5, -0.5, -0.5);

            float a = Mth.clamp(1 - progress1, 0, 1);
            float rgb = Mth.clamp(progress1, 0, 1);

            for (Direction d : DIRECTIONS_AND_NULL)
            {
                for (BakedQuad quad : model.getQuads(null, d, random, ModelData.EMPTY, null))
                {
                    buffer.putBulkData(matrixStack.last(), quad, rgb, rgb, rgb, a, 0x00F000F0, OverlayTexture.NO_OVERLAY, true);
                }
            }

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    private float lerp(float a, float b, float p)
    {
        return a + p * (b - a);
    }
}