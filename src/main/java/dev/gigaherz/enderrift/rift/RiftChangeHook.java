package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.rift.storage.RiftSlot;

public interface RiftChangeHook
{
    void onClear();

    void onAdd(RiftSlot slot);

    void onRemove(RiftSlot slot);
}
