package gigaherz.enderRift.common;

import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

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