package dev.gigaherz.enderrift.rift.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RiftStorage extends SavedData
{
    private static final String DATA_NAME = "enderRiftStorageManager";

    private Map<Integer, RiftInventory> rifts = new HashMap<Integer, RiftInventory>();
    private int lastRiftId;

    public RiftStorage()
    {
    }

    public RiftStorage(CompoundTag nbt)
    {
        ListTag riftList = nbt.getList("Rifts", Constants.NBT.TAG_COMPOUND);

        rifts.clear();

        for (int i = 0; i < riftList.size(); ++i)
        {
            CompoundTag riftTag = riftList.getCompound(i);
            int j = riftTag.getByte("Rift");

            RiftInventory inventory = new RiftInventory(this);
            inventory.readFromNBT(riftTag);

            rifts.put(j, inventory);
        }

        lastRiftId = nbt.getInt("LastRiftId");
    }

    @Override
    public CompoundTag save(CompoundTag nbtTagCompound)
    {
        ListTag nbtTagList = new ListTag();

        for (Map.Entry<Integer, RiftInventory> entry : rifts.entrySet())
        {
            RiftInventory inventory = entry.getValue();

            CompoundTag nbtTagCompound1 = new CompoundTag();
            nbtTagCompound1.putInt("Rift", entry.getKey());
            inventory.writeToNBT(nbtTagCompound1);
            nbtTagList.add(nbtTagCompound1);
        }

        nbtTagCompound.put("Rifts", nbtTagList);
        nbtTagCompound.putInt("LastRiftId", lastRiftId);

        return nbtTagCompound;
    }

    public static RiftStorage get(Level world)
    {
        if (!(world instanceof ServerLevel))
        {
            throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
        }

        ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(RiftStorage::new, RiftStorage::new, DATA_NAME);
    }

    public RiftInventory getRift(int id)
    {
        RiftInventory rift = rifts.get(id);

        if (rift == null)
        {
            rift = new RiftInventory(this);
            rifts.put(id, rift);
            setDirty();
        }

        return rift;
    }

    public int getNextRiftId()
    {
        setDirty();

        return ++lastRiftId;
    }

    public void walkExistingRifts(BiConsumer<Integer, RiftInventory> consumer)
    {
        rifts.forEach(consumer);
    }

}