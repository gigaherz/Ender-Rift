package gigaherz.enderRift.plugins;

import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

public class PluginProviders
{
    public static Function<ITheOneProbe, Void> createTOP()
    {
        //noinspection Convert2Lambda
        return new Function<ITheOneProbe, Void>()
        {
            @Override
            public Void apply(ITheOneProbe top)
            {
                TheOneProbeProviders.register(top);
                return null;
            }
        };
    }

}
