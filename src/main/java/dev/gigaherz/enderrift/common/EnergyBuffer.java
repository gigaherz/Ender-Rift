package dev.gigaherz.enderrift.common;

import net.neoforged.neoforge.energy.EnergyStorage;

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