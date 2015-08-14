package gigaherz.enderRift.client;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class TESREnderRift extends TileEntitySpecialRenderer
{
    IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(EnderRiftMod.MODID.toLowerCase(), "obj/sphere.obj"));
    ResourceLocation texture = new ResourceLocation(EnderRiftMod.MODID.toLowerCase(), "textures/blocks/rift_aura.png");

    @Override
    public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float f)
    {
        if (entity.getBlockMetadata() == 0)
            return;

        bindTexture(texture);

        long time = entity.getWorldObj().getWorldTime();

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        drawSphere(f, time % 20);
        drawSphere(f, time % 20 + 20);
        drawSphere(f, time % 20 + 40);
        drawSphere(f, time % 20 + 60);
        drawSphere(f, time % 20 + 80);

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    private void drawSphere(float f, long time)
    {
        float tickTime = (time % 100 + f) / 100.0f;

        float scale = 0.8f + 1.2f * tickTime;
        float alpha = 1.2f - 1.21f * tickTime;

        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        GL11.glScalef(scale, scale, scale);

        model.renderAll();

        GL11.glPopMatrix();
    }
}
