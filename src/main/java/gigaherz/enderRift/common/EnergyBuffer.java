package gigaherz.enderRift.common;

import net.minecraftforge.energy.EnergyStorage;

public class EnergyBuffer extends EnergyStorage
{
    public EnergyBuffer(int capacity)
    {
        super(capacity);
    }

    public void setEnergy(int energy)
    {
        this.energy = energy;
    }
}
