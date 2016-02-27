package gigaherz.capabilities.api.energy;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class CapabilityEnergy
{
    @CapabilityInject(IEnergyHandler.class)
    public static Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = null;

    private static boolean enabled = false;

    /**
     * Call this from pre-init if you want to use this capability.
     */
    public static void enable()
    {
        if (!enabled)
        {
            enabled = true;
            CapabilityManager.INSTANCE.register(IEnergyHandler.class, new Storage(), new Callable<IEnergyHandler>()
            {
                @Override
                public IEnergyHandler call() throws Exception
                {
                    return new EnergyBuffer();
                }
            });
        }
    }

    private static class Storage
            implements Capability.IStorage<IEnergyHandler>
    {
        @Override
        public NBTBase writeNBT(Capability<IEnergyHandler> capability, IEnergyHandler instance, EnumFacing side)
        {
            if (!(instance instanceof IEnergyPersist))
                return null;

            NBTTagCompound data = new NBTTagCompound();
            IEnergyPersist holder = (IEnergyPersist) instance;

            int energy = holder.getEnergy();

            if (energy < 0)
                energy = 0;

            data.setInteger("Energy", energy);

            return data;
        }

        @Override
        public void readNBT(Capability<IEnergyHandler> capability, IEnergyHandler instance, EnumFacing side, NBTBase nbt)
        {
            if (!(instance instanceof IEnergyPersist))
                return;

            NBTTagCompound data = (NBTTagCompound) nbt;
            IEnergyPersist holder = (IEnergyPersist) instance;

            int capacity = holder.getCapacity();
            int energy = data.getInteger("Energy");

            if (energy > capacity)
            {
                energy = capacity;
            }

            holder.setEnergy(energy);
        }
    }
}