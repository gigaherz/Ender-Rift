package gigaherz.enderRift.client;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Map;

public class RenderingStuffs
{
    static Map<String, IBakedModel> loadedModels = Maps.newHashMap();

    public static void init()
    {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).registerReloadListener(new IResourceManagerReloadListener()
            {
                @Override
                public void onResourceManagerReload(IResourceManager ignored)
                {
                    loadedModels.clear();
                }
            });
        }
    }

    public static void renderModel(IBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (BakedQuad bakedquad : model.getQuads(null, null, 0))
        {
            buffer.addVertexData(bakedquad.getVertexData());
        }
        tessellator.draw();
    }

    public static void renderModel(IBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (BakedQuad bakedquad : model.getQuads(null, null, 0))
        {
            LightUtil.renderQuadColor(buffer, bakedquad, color);
        }
        tessellator.draw();
    }

    public static IBakedModel loadModel(String resourceName)
    {
        IBakedModel model = loadedModels.get(resourceName);
        if (model != null)
            return model;

        try
        {
            final TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
            IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation(resourceName));
            model = mod.bake(mod.getDefaultState(), DefaultVertexFormats.ITEM,
                    new Function<ResourceLocation, TextureAtlasSprite>()
                    {
                        @Nullable
                        @Override
                        public TextureAtlasSprite apply(@Nullable ResourceLocation location)
                        {
                            if (location == null)
                                return null;
                            return textures.getAtlasSprite(location.toString());
                        }
                    });
            return model;
        }
        catch (Exception e)
        {
            throw new ReportedException(new CrashReport("Error loading custom model " + resourceName, e));
        }
    }
}
