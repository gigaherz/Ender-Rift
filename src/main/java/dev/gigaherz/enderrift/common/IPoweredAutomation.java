package dev.gigaherz.enderrift.common;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.Optional;

public interface IPoweredAutomation
{
    @Nullable
    ResourceHandler<ItemResource> getInventory();

    Optional<EnergyHandler> getEnergyBuffer();

    boolean isRemote();

    void setDirty();

    void setLowOnPowerTemporary();
}