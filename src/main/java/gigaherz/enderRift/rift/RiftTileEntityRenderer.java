package gigaherz.enderRift.rift;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

public class RiftTileEntityRenderer extends TileEntityRenderer<RiftTileEntity>
{
    private final RenderType renderType = RenderType.translucent();
    private final Random random = new Random();

    public RiftTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(RiftTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int num1, int num2)
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
            float b = MathHelper.clamp(progress1, 0, 1);
            float g = MathHelper.clamp(progress1, 0, 1);
            float r = MathHelper.clamp(progress1, 0, 1);

            IBakedModel model = Minecraft.getInstance().getModelManager().getModel(EnderRiftMod.location("block/sphere"));

            IVertexBuilder buffer = iRenderTypeBuffer.getBuffer(renderType);
            for(BakedQuad quad : model.getQuads(null, null, random, EmptyModelData.INSTANCE))
            {
                buffer.addVertexData(matrixStack.getLast(), quad, r, g, b, a, 0x00F000F0, 0, true);
            }
            for(Direction d : Direction.values())
            {
                for(BakedQuad quad : model.getQuads(null, d, random, EmptyModelData.INSTANCE))
                {
                    buffer.addVertexData(matrixStack.getLast(), quad, r, g, b, a, 0x00F000F0, 0, true);
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
