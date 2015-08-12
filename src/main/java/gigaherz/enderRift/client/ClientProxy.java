package gigaherz.enderRift.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import gigaherz.enderRift.IModProxy;
import gigaherz.enderRift.blocks.TileEnderRift;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        RenderingRegistry.registerBlockHandler(new SBRHEnderRift());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderRift.class, new TESREnderRift());
    }

    @Override
    public void init()
    {
    }
}
