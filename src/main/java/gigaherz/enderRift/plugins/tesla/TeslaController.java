package gigaherz.enderRift.plugins.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.IEnergyStorage;

public class TeslaController
{
    @CapabilityInject(ITeslaProducer.class)
    public static void producer(Capability tesla)
    {
        TeslaControllerBase.PRODUCER = new Producer(tesla);
    }

    @CapabilityInject(ITeslaConsumer.class)
    public static void consumer(Capability tesla)
    {
        TeslaControllerBase.CONSUMER = new Consumer(tesla);
    }

    @CapabilityInject(ITeslaHolder.class)
    public static void holder(Capability tesla)
    {
        TeslaControllerBase.HOLDER = new Holder(tesla);
    }

    private static class Producer extends TeslaControllerBase
    {
        Capability tesla;

        public Producer(Capability tesla)
        {
            this.tesla = tesla;
        }

        @Override
        public Capability getCapability()
        {
            return tesla;
        }

        @Override
        public Object createInstance(IEnergyStorage handler)
        {
            return new TeslaEnergyProducer(handler);
        }
    }

    private static class Consumer extends TeslaControllerBase
    {
        Capability tesla;

        public Consumer(Capability tesla)
        {
            this.tesla = tesla;
        }

        @Override
        public Capability getCapability()
        {
            return tesla;
        }

        @Override
        public Object createInstance(IEnergyStorage handler)
        {
            return new TeslaEnergyReceiver(handler);
        }

        @Override
        public IEnergyStorage wrapReverse(TileEntity e, EnumFacing from)
        {
            if (e.hasCapability(TeslaCapabilities.CAPABILITY_CONSUMER, from))
                return new TeslaConsumerWrapper(e.getCapability(TeslaCapabilities.CAPABILITY_CONSUMER, from));
            return null;
        }
    }

    private static class Holder extends TeslaControllerBase
    {
        Capability tesla;

        public Holder(Capability tesla)
        {
            this.tesla = tesla;
        }

        @Override
        public Capability getCapability()
        {
            return tesla;
        }

        @Override
        public Object createInstance(IEnergyStorage handler)
        {
            return new TeslaEnergyHolder(handler);
        }
    }
}
