package dev.gigaherz.enderrift.rift.storage;

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

    public RiftInventory getInventory()
    {
        return inventory;
    }

    public RiftInventory getInventoryOrCreate()
    {
        if (inventory != null)
        {
            return inventory;
        }
        return inventory = new RiftInventory(this);
    }

    public boolean isValid()
    {
        return inventory != null;
    }
}
