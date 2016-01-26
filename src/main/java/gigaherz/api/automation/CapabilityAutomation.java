package gigaherz.api.automation;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class CapabilityAutomation
{
    @CapabilityInject(IInventoryAutomation.class)
    public static Capability<IInventoryAutomation> AUTOMATION_CAPABILITY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IInventoryAutomation.class, new Storage(), new Callable<IInventoryAutomation>()
        {
            @Override
            public IInventoryAutomation call() throws Exception
            {
                return new AutomationHolder();
            }
        });
    }

    private static class Storage
            implements Capability.IStorage<IInventoryAutomation>
    {
        @Override
        public NBTBase writeNBT(Capability<IInventoryAutomation> capability, IInventoryAutomation instance, EnumFacing side)
        {
            if (!(instance instanceof AutomationHolder))
                return null;

            NBTTagCompound data = new NBTTagCompound();
            AutomationHolder holder = (AutomationHolder) instance;

            holder.writeToNBT(data);

            return data;
        }

        @Override
        public void readNBT(Capability<IInventoryAutomation> capability, IInventoryAutomation instance, EnumFacing side, NBTBase nbt)
        {
            if (!(instance instanceof AutomationHolder))
                return;

            NBTTagCompound data = (NBTTagCompound) nbt;
            AutomationHolder holder = (AutomationHolder) instance;

            holder.readFromNBT(data);
        }
    }
}
