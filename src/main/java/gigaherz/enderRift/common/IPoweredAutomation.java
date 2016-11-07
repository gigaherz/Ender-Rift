package gigaherz.enderRift.common;

import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IPoweredAutomation
{
    @Nullable
    IItemHandler getInventory();

    IEnergyStorage getEnergyBuffer();

    World getWorld();

    void markDirty();
}
