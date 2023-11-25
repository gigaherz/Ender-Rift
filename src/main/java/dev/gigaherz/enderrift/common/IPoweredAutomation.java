package dev.gigaherz.enderrift.common;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IPoweredAutomation
{
    @Nullable
    IItemHandler getInventory();

    Optional<IEnergyStorage> getEnergyBuffer();

    boolean isRemote();

    void setDirty();

    void setLowOnPowerTemporary();
}