package dev.gigaherz.enderrift.common;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

public class EnergyBuffer extends SimpleEnergyHandler
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