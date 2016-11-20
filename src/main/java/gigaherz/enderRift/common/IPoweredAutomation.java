package gigaherz.enderRift.common;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
}
