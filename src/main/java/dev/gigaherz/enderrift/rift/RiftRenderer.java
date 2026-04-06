package dev.gigaherz.enderrift.rift;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.client.ClientHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.slf4j.Logger;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value= Dist.CLIENT, modid = EnderRiftMod.MODID)
public class RiftRenderer
        implements BlockEntityRenderer<RiftBlockEntity, RiftRenderer.RiftRenderState>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int STEP_TIME = 20;
    private static final int STEPS = 5;
    private static final int TIME_LOOP = STEP_TIME * STEPS;

    public RiftRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    public static class RiftRenderState extends BlockEntityRenderState
    {
        public boolean canRender;
        public float lastPoweringState;
        public float nextPoweringState;
        public float poweringState;
        public Quaternionf rotation;
        public RenderType renderType;
        public long stepTime;
        public float partialTick;
        public @Nullable QuadCollection model;
    }

    @Override
    public RiftRenderState createRenderState()
    {
        return new RiftRenderState();
    }

    @Override
    public void extractRenderState(RiftBlockEntity be, RiftRenderState state, float partialTick, Vec3 pos, @Nullable ModelFeatureRenderer.CrumblingOverlay overlay)
    {
        BlockEntityRenderState.extractBase(be, state, overlay);

        state.canRender = true;
        if (!be.getBlockState().getValue(RiftBlock.ASSEMBLED))
        {
            state.canRender = false;
            return;
        }

        state.partialTick = partialTick;

        state.lastPoweringState = be.getLastPoweringState();
        state.nextPoweringState = be.getPoweringState();
        state.poweringState = lerp(state.lastPoweringState, state.nextPoweringState, partialTick);

        Minecraft minecraft = Minecraft.getInstance();

        Camera renderInfo = minecraft.gameRenderer.getMainCamera();

        BlockPos tePos = be.getBlockPos();
        Vec3 cameraPos = renderInfo.position();

        double ty = cameraPos.y() - (tePos.getY() + 0.5);
        double tx = cameraPos.x() - (tePos.getX() + 0.5);
        double tz = cameraPos.z() - (tePos.getZ() + 0.5);
        float yaw = (float) Math.atan2(tz, tx);
        float xz = (float) Math.sqrt(tx * tx + tz * tz);
        float pitch = (float) Math.atan2(ty, xz);

        state.rotation = Axis.YP.rotation(-yaw).mul(Axis.ZP.rotation(pitch));

        boolean isFabulous = minecraft.options.improvedTransparency().get();

        state.renderType = isFabulous ?
                RenderTypes.translucentMovingBlock() :
                RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

        state.model = minecraft.getModelManager().getStandaloneModel(ClientHelper.SPHERE);
        if (state.model == null)
        {
            state.canRender = false;
            return;
        }

        var time = be.getLevel().getGameTime();

        state.stepTime = (int) (time % STEP_TIME);
    }

    @Override
    public void submit(RiftRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState)
    {
        if (!state.canRender)
            return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(state.rotation);
        poseStack.translate(-0.5, -0.5, -0.5);

        float c0 = 1.0f / STEPS;
        float c1 = 1.0f / (1 - c0);

        for (int i = 0; i < STEPS; i++)
        {
            float progress0 = ((state.stepTime + i * STEP_TIME) % TIME_LOOP + state.partialTick) / TIME_LOOP;
            float progress1 = (progress0 - c0) * c1;

            float scale = (1.0f + state.poweringState) + (0.6f + state.poweringState) * progress0;

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, -0.5, -0.5);

            int a = Mth.floor(Mth.clamp(1 - progress1, 0, 1)*255);
            int rgb = Mth.floor(Mth.clamp(progress1, 0, 255) * 255);

            var qi = new QuadInstance();
            qi.setColor(ARGB.color(a,rgb,rgb,rgb));
            qi.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
            qi.setOverlayCoords(OverlayTexture.NO_OVERLAY);

            submitNodeCollector.submitCustomGeometry(poseStack, state.renderType, (pose, buffer) -> {
                if (state.model == null)
                    return;
                for (BakedQuad quad : state.model.getAll())
                {
                    buffer.putBakedQuad(pose, quad, qi);
                }
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private float lerp(float a, float b, float p)
    {
        return a + p * (b - a);
    }
}