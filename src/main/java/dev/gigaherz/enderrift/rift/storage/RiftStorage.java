package dev.gigaherz.enderrift.rift.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public class RiftStorage implements FilenameFilter {

    public static final LevelResource DATA_DIR = new LevelResource("enderrift");
    public static final String FILE_FORMAT = ".dat";
    public static final int FILE_FORMAT_LENGTH = FILE_FORMAT.length();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Function<UUID, RiftHolder> BUILDER = RiftHolder::new;

    private static RiftStorage STORAGE;

    public static RiftStorage get() {
        return STORAGE;
    }

    public static boolean isAvailable() {
        return STORAGE != null;
    }

    public static void init(MinecraftServer server) {
        if(STORAGE != null) {
            return;
        }
        STORAGE = new RiftStorage(server);
        STORAGE.loadAll();
    }

    public static void deinit() {
        if (STORAGE == null) {
            return;
        }
        STORAGE.saveAll();
        STORAGE = null;
    }

    private final HashMap<UUID, RiftHolder> rifts = new HashMap<>();

    private final Path dataDirectory;

    private RiftStorage(MinecraftServer server) {
        this.dataDirectory = server.getWorldPath(DATA_DIR);
    }

    public RiftHolder newRift() {
        UUID id;
        while (rifts.containsKey(id = UUID.randomUUID())) {
        }
        return getRift(id);
    }

    public RiftHolder getRift(UUID id) {
        if (id == null) {
            return null;
        }
        return rifts.computeIfAbsent(id, BUILDER);
    }

    public void loadAll() {
        File folder = dataDirectory.toFile();
        if (!folder.exists()) {
            return;
        }
        File[] files = folder.listFiles(this);
        for (File file : files) {
            String name = file.getName();
            name = name.substring(0, name.length() - FILE_FORMAT_LENGTH);
            UUID id;
            try {
                id = UUID.fromString(name);
            } catch (IllegalArgumentException iae) {
                LOGGER.warn("Found invalid rift name {}", name, iae);
                continue;
            }
            load(id, file);
        }
    }

    public void load(UUID id) {
        File file = dataDirectory.resolve(id.toString() + FILE_FORMAT).toFile();
        if (!file.exists()) {
            return;
        }
        load(id, file);
    }

    private void load(UUID id, File file) {
        RiftHolder holder = rifts.computeIfAbsent(id, BUILDER);
        holder.resetDirty();
        CompoundTag tag;
        try {
            tag = NbtIo.readCompressed(file);
        } catch (IOException e) {
            holder.getInventory().clear();
            LOGGER.error("Could not load rift {}", id, e);
            return;
        }
        holder.getInventoryOrCreate().load(tag);
    }

    public void saveAll() {
        RiftHolder[] holders = rifts.values().toArray(RiftHolder[]::new);
        for (RiftHolder holder : holders) {
            save(holder);
        }
    }

    public void save(UUID id) {
        save(rifts.get(id));
    }

    private void save(RiftHolder holder) {
        if (holder == null || !holder.isValid() || !holder.isDirty()) {
            return;
        }
        holder.resetDirty();
        String id = holder.getId().toString();
        RiftInventory inventory = holder.getInventory();
        File file = dataDirectory.resolve(id + FILE_FORMAT).toFile();
        if (!file.exists()) {
            File folder = file.getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
        try {
            NbtIo.writeCompressed(inventory.save(), file);
        } catch (IOException ioexception) {
            LOGGER.error("Could not rift {}", id, ioexception);
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(FILE_FORMAT);
    }

    public void walkRifts(BiConsumer<UUID, RiftInventory> riftConsumer) {
        rifts.forEach((id, holder) -> {
            if (!holder.isValid()) {
                return;
            }
            riftConsumer.accept(id, holder.getInventory());
        });
    }

}