package gigaherz.enderRift.automation.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityAutomation
{
    @CapabilityInject(IInventoryAutomation.class)
    public static Capability<IInventoryAutomation> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IInventoryAutomation.class, new Capability.IStorage<IInventoryAutomation>()
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
        }, () -> null);
    }
}
