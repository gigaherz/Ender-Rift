package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.automation.AggregatorTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class BrowserEntityTileEntity extends AggregatorTileEntity
{
    @ObjectHolder("enderrift:browser")
    public static TileEntityType<?> TYPE;

    private int changeCount = 1;

    Direction facing = null;

    public BrowserEntityTileEntity()
    {
        super(TYPE);
    }

    @Nullable
    public Direction getFacing()
    {
        if (facing == null && world != null)
        {
            BlockState state = world.getBlockState(pos);
            if (state.getProperties().contains(BrowserBlock.FACING))
            {
                facing = state.get(BrowserBlock.FACING).getOpposite();
            }
        }
        return facing;
    }

    @Override
    public void markDirty()
    {
        changeCount++;
        facing = null;
        super.markDirty();
    }

    @Override
    protected void lazyDirty()
    {
        changeCount++;
    }

    public int getChangeCount()
    {
        return changeCount;
    }

    @Override
    protected boolean canConnectSide(Direction side)
    {
        return side == getFacing().getOpposite();
    }
}
