package dev.gigaherz.enderrift.rift.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.gigaherz.enderrift.rift.storage.migration.*;
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

    @SuppressWarnings("unchecked")
    private static final Class<? extends RiftMigration>[] MIGRATIONS = new Class[] {
        RiftMigration_17_08_2022.class
    };

    private static RiftStorage STORAGE;

    public static RiftStorage get() {
        return STORAGE;
    }

    public static boolean isAvailable() {
        return STORAGE != null;
    }

    public static void init(MinecraftServer server) {
        if (STORAGE != null) {
            return;
        }
        STORAGE = new RiftStorage(server);
        STORAGE.migrate();
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
    private final Map<Class<? extends RiftMigration>, RiftMigration> migrations;
    // ReadLock => Access to the rift data
    // WriteLock => Modification of rift data
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final File dataDirectory;

    private RiftStorage(MinecraftServer server) {
        this.dataDirectory = server.getWorldPath(DATA_DIR).toFile();
        Map<Class<? extends RiftMigration>, RiftMigration> migrations = new HashMap<>();
        for (Class<? extends RiftMigration> migration : MIGRATIONS) {
            if (migration == null) {
                continue;
            }
            try {
                migrations.put(migration, migration.cast(migration.getConstructor().newInstance()));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
                LOGGER.warn("Failed to load migration '{}'", migration.getSimpleName());
                continue;
            }
        }
        this.migrations = Collections.unmodifiableMap(migrations);
    }

    public <E extends RiftMigration> E getMigration(Class<E> type) {
        RiftMigration migration = migrations.get(type);
        if (migration == null) {
            return null;
        }
        return type.cast(migration);
    }

    private void migrate() {
        for (RiftMigration migration : migrations.values()) {
            if (!migration.isApplicable(this)) {
                continue;
            }
            LOGGER.info("Running migration '{}'", migration.getName());
            try {
                migration.migrate(this);
            } catch (Exception exp) {
                LOGGER.warn("Failed to run migration '{}'", migration.getName(), exp);
            }
        }
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    public RiftHolder newRift() {
        UUID id;
        lock.readLock().lock();
        try {
            while (rifts.containsKey(id = UUID.randomUUID())) {
            }
            return rifts.computeIfAbsent(id, BUILDER);
        } finally {
            lock.readLock().unlock();
        }
    }

    public RiftHolder getRift(UUID id) {
        if (id == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            return rifts.computeIfAbsent(id, BUILDER);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void loadAll() {
        // Prevent others from reading the map before every Rift is fully loaded
        lock.writeLock().lock();
        try {
            if (!dataDirectory.exists()) {
                return;
            }
            File[] files = dataDirectory.listFiles(this);
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void load(UUID id) {
        File file = new File(dataDirectory, id.toString() + FILE_FORMAT);
        if (!file.exists()) {
            return;
        }
        lock.writeLock().lock();
        try {
            load(id, file);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void load(UUID id, File file) {
        RiftHolder holder = rifts.computeIfAbsent(id, BUILDER);
        CompoundTag tag;
        LOGGER.info("Loading rift {}...", id);
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
        RiftHolder[] holders;
        lock.readLock().lock();
        try {
            holders = rifts.values().toArray(RiftHolder[]::new);
        } finally {
            lock.readLock().unlock();
        }
        for (RiftHolder holder : holders) {
            save(holder);
        }
    }

    public void save(UUID id) {
        lock.readLock().lock();
        RiftHolder holder;
        try {
            holder = rifts.get(id);
        } finally {
            lock.readLock().unlock();
        }
        save(holder);
    }

    private void save(RiftHolder holder) {
        if (holder == null || !holder.isValid()) {
            return;
        }
        String id = holder.getId().toString();
        RiftInventory inventory = holder.getInventory();
        File file = new File(dataDirectory, id + FILE_FORMAT);
        LOGGER.info("Saving rift {}...", id);
        try {
            if (!file.exists()) {
                File folder = file.getParentFile();
                if (folder != null && !folder.exists()) {
                    folder.mkdirs();
                }
                file.createNewFile();
            }
            NbtIo.writeCompressed(inventory.save(), file);
        } catch (IOException ioexception) {
            LOGGER.error("Could not save rift {}", id, ioexception);
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