package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class BrowserBlockEntity extends AggregatorBlockEntity
{
    private int changeCount = 1;

    private Direction facing = null;

    public BrowserBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.BROWSER_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public Direction getFacing()
    {
        if (facing == null && level != null)
        {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getProperties().contains(BrowserBlock.FACING))
            {
                facing = state.getValue(BrowserBlock.FACING).getOpposite();
            }
        }
        return facing;
    }

    @Override
    public void setChanged()
    {
        changeCount++;
        facing = null;
        super.setChanged();
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

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, BrowserBlockEntity te)
    {
        te.tick();
    }
}