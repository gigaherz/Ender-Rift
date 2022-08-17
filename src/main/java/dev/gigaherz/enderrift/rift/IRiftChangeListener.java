package dev.gigaherz.enderrift.rift;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Optional;

public interface IRiftChangeListener
{
    boolean isInvalid();

    void onRiftChanged();
    
    Optional<Level> getRiftLevel();

    Optional<BlockPos> getLocation();
}