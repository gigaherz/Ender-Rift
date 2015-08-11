package gigaherz.enderRift.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import gigaherz.enderRift.IModProxy;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        RenderingRegistry.registerBlockHandler(new SBRHEnderRift());
    }

    @Override
    public void init()
    {
    }
}
