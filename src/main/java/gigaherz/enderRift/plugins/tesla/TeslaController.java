package gigaherz.enderRift.plugins.tesla;

import gigaherz.capabilities.api.energy.IEnergyHandler;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

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
        public Object createInstance(IEnergyHandler handler)
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
        public Object createInstance(IEnergyHandler handler)
        {
            return new TeslaEnergyReceiver(handler);
        }

        @Override
        public IEnergyHandler wrapReverse(TileEntity e, EnumFacing from)
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
        public Object createInstance(IEnergyHandler handler)
        {
            return new TeslaEnergyHolder(handler);
        }
    }
}
