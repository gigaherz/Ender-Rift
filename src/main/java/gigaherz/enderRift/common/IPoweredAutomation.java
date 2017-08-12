package gigaherz.enderRift.common;

import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IPoweredAutomation
{
    @Nullable
    IItemHandler getInventory();

    IEnergyStorage getEnergyBuffer();

    boolean isRemote();

    void setDirty();

    void setLowOnPowerTemporary();
}
