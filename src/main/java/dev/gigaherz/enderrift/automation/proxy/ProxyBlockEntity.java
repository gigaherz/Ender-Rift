package dev.gigaherz.enderrift.automation.proxy;

import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

public class ProxyBlockEntity extends AggregatorBlockEntity
{
    @ObjectHolder("enderrift:proxy")
    public static BlockEntityType<ProxyBlockEntity> TYPE;

    public ProxyBlockEntity(BlockPos pos, BlockState state)
    {
        super(TYPE, pos, state);
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