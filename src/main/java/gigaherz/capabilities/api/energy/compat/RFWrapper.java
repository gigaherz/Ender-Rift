package gigaherz.capabilities.api.energy.compat;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.minecraft.util.EnumFacing;

public class RFWrapper implements IEnergyHandler
{
    IEnergyReceiver recv;
    IEnergyProvider prov;
    EnumFacing side;

    public static RFWrapper wrap(Object obj, EnumFacing side)
    {
        IEnergyReceiver recv = null;
        IEnergyProvider prov = null;
        if(obj instanceof IEnergyReceiver)
        {
            recv = (IEnergyReceiver)obj;
            if(!recv.canConnectEnergy(side))
                recv = null;
        }
        if(obj instanceof IEnergyProvider)
        {
            prov = (IEnergyProvider) obj;
            if(!prov.canConnectEnergy(side))
                prov = null;
        }

        if(recv != null || prov != null)
            return new RFWrapper(recv, prov, side);

        return null;
    }

    public RFWrapper(IEnergyReceiver recv, IEnergyProvider prov, EnumFacing side)
    {
        this.side = side;
        this.recv = recv;
        this.prov = prov;
    }

    @Override
    public int getCapacity()
    {
        return recv != null
                ? recv.getMaxEnergyStored(side)
                : (prov != null
                    ? prov.getMaxEnergyStored(side)
                    : 0);
    }

    @Override
    public int getEnergy()
    {
        return recv != null
                ? recv.getEnergyStored(side)
                : (prov != null
                    ? prov.getEnergyStored(side)
                    : 0);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        return prov != null
                ? prov.extractEnergy(side, maxExtract, simulate)
                : 0;
    }

    @Override
    public int insertEnergy(int maxReceive, boolean simulate)
    {
        return recv != null
                ? recv.receiveEnergy(side, maxReceive, simulate)
                : 0;
    }
}
