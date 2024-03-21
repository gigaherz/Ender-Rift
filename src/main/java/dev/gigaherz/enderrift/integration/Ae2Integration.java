package dev.gigaherz.enderrift.integration;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.RiftChangeHook;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftSlot;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ae2Integration
{
    private static final BlockCapability<MEStorage, @Nullable Direction> ME_STORAGE = BlockCapability
        .createSided(new ResourceLocation("ae2","me_storage"), MEStorage.class);

    public static void init(IEventBus modEventBus)
    {
        modEventBus.addListener(Ae2Integration::registerCaps);
    }

    private static void registerCaps(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                ME_STORAGE,
                EnderRiftMod.RIFT_BLOCK_ENTITY.get(),
                (blockEntity, context) -> {
                    if (!blockEntity.isPowered())
                        return null;
                    var inv = blockEntity.getInventory();
                    return inv != null ? inv.getOrCreateFeature(Ae2RiftStorage.class, Ae2RiftStorage::new) : null;
                }
        );
    }

    private static class Ae2RiftStorage implements MEStorage, RiftChangeHook
    {
        @NotNull
        private final RiftInventory inv;

        private final Multimap<AEItemKey, RiftSlot> slotLookup = ArrayListMultimap.create();

        public Ae2RiftStorage(RiftInventory inv)
        {
            this.inv = inv;
            inv.addHook(this);
        }

        @Override
        public void onClear()
        {
            slotLookup.clear();
        }

        @Override
        public void onAdd(RiftSlot slot)
        {
            var key = AEItemKey.of(slot.getSample());
            slotLookup.put(key, slot);
        }

        @Override
        public void onRemove(RiftSlot slot)
        {
            var key = AEItemKey.of(slot.getSample());
            slotLookup.remove(key, slot);
        }

        @Override
        public Component getDescription()
        {
            return Component.translatable("text.enderrift.rift.me_name");
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source)
        {
            if (!(what instanceof AEItemKey itemKey))
                return 0;

            if (mode.isSimulate())
                return amount;

            var slot = findSlot(itemKey);

            if (slot == null)
            {
                inv.append(itemKey.toStack(), amount);
            }
            else
            {
                slot.addCount(amount);
            }

            return amount;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source)
        {
            if (!(what instanceof AEItemKey itemKey))
                return 0;

            var slot = findSlot(itemKey);
            if (slot == null)
                return 0;

            var count = slot.getCount();

            if (mode.isSimulate())
                return Math.min(slot.getCount(), amount);

            if (count <= amount)
            {
                inv.clearSlot(slot);
                return count;
            }

            slot.subtractCount(amount);
            return amount;
        }

        @Override
        public void getAvailableStacks(KeyCounter out)
        {
            for(var keyValue : slotLookup.entries())
            {
                var key = keyValue.getKey();
                var slot = keyValue.getValue();

                if (slot == null || slot.getCount() == 0)
                    continue;

                out.add(key, slot.getCount());
            }
        }

        @Nullable
        public RiftSlot findSlot(AEItemKey key)
        {
            return slotLookup.get(key).stream().findFirst().orElse(null);
        }
    }
}
