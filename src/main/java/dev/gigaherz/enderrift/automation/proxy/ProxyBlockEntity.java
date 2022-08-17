package dev.gigaherz.enderrift.automation.proxy;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ProxyBlockEntity extends AggregatorBlockEntity
{
    public ProxyBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.PROXY_BLOCK_ENTITY.get(), pos, state);
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

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, ProxyBlockEntity te)
    {
        te.tick();
    }
}