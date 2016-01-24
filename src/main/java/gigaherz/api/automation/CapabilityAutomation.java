package gigaherz.api.automation;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class CapabilityAutomation
{
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IInventoryAutomation.class, new Storage(), new Callable<IInventoryAutomation>()
        {
            @Override
            public IInventoryAutomation call() throws Exception
            {
                return null;
            }
        });
    }

    private static class Storage
            implements Capability.IStorage<IInventoryAutomation>
    {
        @Override
        public NBTBase writeNBT(Capability<IInventoryAutomation> capability, IInventoryAutomation instance, EnumFacing side)
        {
            return null;
        }

        @Override
        public void readNBT(Capability<IInventoryAutomation> capability, IInventoryAutomation instance, EnumFacing side, NBTBase nbt)
        {

        }
    }
}
