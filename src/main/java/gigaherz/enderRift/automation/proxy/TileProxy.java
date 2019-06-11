package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.automation.TileAggregator;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ObjectHolder;

public class TileProxy extends TileAggregator
{
    @ObjectHolder("enderrift:proxy")
    public static TileEntityType<?> TYPE;

    public TileProxy()
    {
        super(TYPE);
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    protected boolean canConnectSide(Direction side)
    {
        return true;
    }
}
