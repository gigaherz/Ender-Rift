package dev.gigaherz.enderrift.integration;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftSlot;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
                (blockEntity, context) -> new MEStorage()
                {
                    private final RiftInventory inv = blockEntity.getInventory();

                    @Override
                    public Component getDescription()
                    {
                        return Component.translatable("text.enderrift.rift.me_name");
                    }

                    @Override
                    public long insert(AEKey what, long amount, Actionable mode, IActionSource source)
                    {
                        if (inv == null)
                            return 0;

                        if (!(what instanceof AEItemKey itemKey))
                            return 0;

                        if (mode.isSimulate())
                            return amount;

                        int index = inv.findSlot(itemKey.getItem(), itemKey.getTag());

                        if (index < 0)
                        {
                            inv.append(itemKey.getItem(), itemKey.getTag(), amount);
                        }
                        else
                        {
                            RiftSlot slot = inv.getSlot(index);
                            Objects.requireNonNull(slot).addCount(amount);
                        }

                        return amount;
                    }

                    @Override
                    public long extract(AEKey what, long amount, Actionable mode, IActionSource source)
                    {
                        if (inv == null)
                            return 0;

                        if (!(what instanceof AEItemKey itemKey))
                            return 0;

                        int index = inv.findSlot(itemKey.getItem(), itemKey.getTag());

                        if (index < 0)
                        {
                            return 0;
                        }
                        else
                        {
                            RiftSlot slot = Objects.requireNonNull(inv.getSlot(index));

                            if (mode.isSimulate())
                                return Math.min(slot.getCount(), amount);

                            if (slot.getCount() > amount)
                            {
                                slot.subtractCount(amount);
                            }
                            else
                            {
                                inv.clearSlot(index);
                            }
                        }

                        return amount;
                    }

                    @Override
                    public void getAvailableStacks(KeyCounter out)
                    {
                        if (inv == null)
                            return;

                        for(int i=0;i<inv.getSlots();i++)
                        {
                            var slot = inv.getSlot(i);
                            if (slot == null || slot.getCount() == 0)
                                continue;

                            var meKey = (AEItemKey)slot.meKey;
                            if (meKey == null)
                            {
                                slot.meKey = meKey = AEItemKey.of(slot.getSample());
                            }

                            if (meKey != null)
                            {
                                out.add(meKey, slot.getCount());
                            }
                        }
                    }
                });
    }
}
