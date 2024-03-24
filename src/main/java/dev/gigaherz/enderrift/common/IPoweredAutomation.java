package dev.gigaherz.enderrift.common;

import dev.gigaherz.enderrift.rift.ILongItemHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IPoweredAutomation
{
    @Nullable
    ILongItemHandler getInventory();

    Optional<IEnergyStorage> getEnergyBuffer();

    boolean isRemote();

    void setDirty();

    void setLowOnPowerTemporary();
}