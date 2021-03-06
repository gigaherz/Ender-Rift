package gigaherz.enderRift.automation.proxy;

import gigaherz.enderRift.automation.AggregatorTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ObjectHolder;

public class ProxyTileEntity extends AggregatorTileEntity
{
    @ObjectHolder("enderrift:proxy")
    public static TileEntityType<?> TYPE;

    public ProxyTileEntity()
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