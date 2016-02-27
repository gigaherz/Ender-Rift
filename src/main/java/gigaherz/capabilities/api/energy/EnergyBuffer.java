package gigaherz.capabilities.api.energy;

public class EnergyBuffer implements IEnergyPersist
{
    protected int energy;
    protected int capacity = 10000;

    public EnergyBuffer()
    {
    }

    public EnergyBuffer(int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public int getCapacity()
    {
        return capacity;
    }

    public EnergyBuffer setCapacity(int capacity)
    {
        this.capacity = capacity;

        if (energy > capacity)
        {
            energy = capacity;
        }
        return this;
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public void setEnergy(int energy)
    {
        this.energy = energy;

        if (this.energy > capacity)
        {
            this.energy = capacity;
        }
        else if (this.energy < 0)
        {
            this.energy = 0;
        }
    }

    @Override
    public int insertEnergy(int maxReceive, boolean simulate)
    {
        int energyReceived = Math.min(capacity - energy, maxReceive);

        if (!simulate)
        {
            energy += energyReceived;
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int energyExtracted = Math.min(energy, maxExtract);

        if (!simulate)
        {
            energy -= energyExtracted;
        }

        return energyExtracted;
    }
}
