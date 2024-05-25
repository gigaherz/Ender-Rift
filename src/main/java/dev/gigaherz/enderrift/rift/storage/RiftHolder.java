package dev.gigaherz.enderrift.rift.storage;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RiftHolder
{

    private final UUID id;
    private RiftInventory inventory;

    RiftHolder(UUID id)
    {
        this.id = id;
    }

    public UUID getId()
    {
        return id;
    }

    public RiftInventory getOrLoad(HolderLookup.Provider lookup)
    {
        if (inventory == null)
        {
            inventory = RiftStorage.load(this, lookup);
        }
        return inventory;
    }

    public boolean isDirty()
    {
        return inventory != null && inventory.isDirty();
    }

    public void clearDirty()
    {
        if (inventory != null)
            inventory.clearDirty();
    }

    @Nullable
    public RiftInventory getIfLoaded()
    {
        return inventory;
    }
}
