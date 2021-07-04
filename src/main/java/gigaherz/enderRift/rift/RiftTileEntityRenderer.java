package gigaherz.enderRift.rift;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import cpw.mods.modlauncher.api.INameMappingService;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class RiftTileEntityRenderer extends TileEntityRenderer<RiftTileEntity>
{
    private final Random random = new Random();

    private static final List<Direction> DIRECTIONS_AND_NULL = Lists.newArrayList(Direction.values());

    static
    {
        DIRECTIONS_AND_NULL.add(null);
    }

    public RiftTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(RiftTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn)
    {
        if (!te.getBlockState().get(RiftBlock.ASSEMBLED))
            return;

            float lastPoweringState = te.getLastPoweringState();
            float nextPoweringState = te.getPoweringState();
            float poweringState = lerp(lastPoweringState, nextPoweringState, partialTicks);

            Minecraft minecraft = Minecraft.getInstance();

            ActiveRenderInfo renderInfo = minecraft.gameRenderer.getActiveRenderInfo();

            BlockPos tePos = te.getPos();
            Vector3d cameraPos = renderInfo.getProjectedView();

            double ty = cameraPos.getY() - (tePos.getY() + 0.5);
            double tx = cameraPos.getX() - (tePos.getX() + 0.5);
            double tz = cameraPos.getZ() - (tePos.getZ() + 0.5);
            float yaw = (float) Math.atan2(tz, tx);
            float xz = (float) Math.sqrt(tx * tx + tz * tz);
            float pitch = (float) Math.atan2(ty, xz);

            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.rotate(Vector3f.YP.rotation(-yaw));
            matrixStack.rotate(Vector3f.ZP.rotation(pitch));
            matrixStack.translate(-0.5, -0.5, -0.5);

            int step_time = 20;
            int steps = 5;
            int time_loop = step_time * steps;

            long time = te.getWorld().getGameTime();
            int tm = (int) (time % step_time);

            float c0 = 1.0f / steps;
            float c1 = 1.0f / (1 - c0);

            boolean isFabulous = minecraft.gameSettings.graphicFanciness.equals(GraphicsFanciness.FABULOUS);

            RenderType type = isFabulous ?
                    RenderType.getTranslucentMovingBlock() :
                    RenderType.getTranslucentNoCrumbling();

            IVertexBuilder buffer = iRenderTypeBuffer.getBuffer(type);
            IBakedModel model = minecraft.getModelManager().getModel(EnderRiftMod.location("block/sphere"));

            for (int i = 0; i < steps; i++)
            {
                float progress0 = ((tm + i * step_time) % time_loop + partialTicks) / time_loop;
                float progress1 = (progress0 - c0) * c1;

                float scale = (1.0f + poweringState) + (0.6f + poweringState) * progress0;

                matrixStack.push();
                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(-0.5, -0.5, -0.5);

                float a = MathHelper.clamp(1 - progress1, 0, 1);
                float rgb = MathHelper.clamp(progress1, 0, 1);

                for (Direction d : DIRECTIONS_AND_NULL)
                {
                    for (BakedQuad quad : model.getQuads(null, d, random, EmptyModelData.INSTANCE))
                    {
                        buffer.addVertexData(matrixStack.getLast(), quad, rgb, rgb, rgb, a, 0x00F000F0, OverlayTexture.NO_OVERLAY, true);
                    }
                }

                matrixStack.pop();
            }

            matrixStack.pop();
    }

    private float lerp(float a, float b, float p)
    {
        return a + p * (b - a);
    }
}