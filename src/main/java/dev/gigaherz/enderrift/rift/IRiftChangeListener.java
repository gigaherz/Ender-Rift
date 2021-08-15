package dev.gigaherz.enderrift.rift;

import net.minecraft.core.BlockPos;

import java.util.Optional;

public interface IRiftChangeListener
{
    boolean isInvalid();

    void onRiftChanged();

    Optional<BlockPos> getLocation();
}