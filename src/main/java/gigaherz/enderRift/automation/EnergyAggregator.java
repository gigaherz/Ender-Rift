package gigaherz.enderRift.automation;

import com.google.common.collect.Lists;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyAggregator implements IEnergyStorage
{
    private final List<IEnergyStorage> buffers = Lists.newArrayList();

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        int remaining = maxReceive;
        for (IEnergyStorage st : buffers)
        {
            if (st.canReceive())
                remaining = st.receiveEnergy(remaining, simulate);
        }
        return remaining;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int remaining = maxExtract;
        for (IEnergyStorage st : buffers)
        {
            if (st.canExtract())
                remaining = st.extractEnergy(remaining, simulate);
        }
        return remaining;
    }

    @Override
    public int getEnergyStored()
    {
        int total = 0;
        for (IEnergyStorage st : buffers)
        {
            total += st.getEnergyStored();
        }
        return total;
    }

    @Override
    public int getMaxEnergyStored()
    {
        int total = 0;
        for (IEnergyStorage st : buffers)
        {
            total += st.getMaxEnergyStored();
        }
        return total;
    }

    @Override
    public boolean canExtract()
    {
        return buffers.stream().anyMatch(IEnergyStorage::canExtract);
    }

    @Override
    public boolean canReceive()
    {
        return buffers.stream().anyMatch(IEnergyStorage::canReceive);
    }

    public void add(IEnergyStorage buffer)
    {
        buffers.add(buffer);
    }
}
