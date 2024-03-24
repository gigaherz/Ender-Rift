package dev.gigaherz.enderrift.rift.storage;

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

    public RiftInventory getOrLoad()
    {
        if (inventory == null)
        {
            inventory = RiftStorage.load(this);
        }
        return inventory;
    }

    public boolean isDirty()
    {
        return inventory.isDirty();
    }

    public void clearDirty()
    {
        inventory.clearDirty();
    }

    @Nullable
    public RiftInventory getIfLoaded()
    {
        return inventory;
    }
}
