package dev.gigaherz.enderrift.rift.storage;

public abstract class RiftMigration
{

    protected abstract boolean isApplicable(RiftStorage storage);

    protected abstract void migrate(RiftStorage storage) throws Exception;

    protected abstract String getName();
}
