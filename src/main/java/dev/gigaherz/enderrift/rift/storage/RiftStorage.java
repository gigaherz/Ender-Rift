package dev.gigaherz.enderrift.rift.storage;

import com.mojang.logging.LogUtils;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.storage.migration.RiftMigration_17_08_2022;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid= EnderRiftMod.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class RiftStorage implements FilenameFilter
{
    @SubscribeEvent
    public static void serverStart(ServerAboutToStartEvent event)
    {
        RiftStorage.init(event.getServer());
    }

    @SubscribeEvent
    public static void serverSave(LevelEvent.Save event)
    {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel) || !((ServerLevel) levelAccessor).dimension().equals(Level.OVERWORLD) || !RiftStorage.isAvailable())
        {
            return;
        }
        RiftStorage.get().saveAll();
    }

    @SubscribeEvent
    public static void serverStop(ServerStoppingEvent event)
    {
        RiftStorage.deinit();
    }


    public static final LevelResource DATA_DIR = new LevelResource("enderrift");
    public static final String FILE_FORMAT = ".dat";
    public static final int FILE_FORMAT_LENGTH = FILE_FORMAT.length();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Function<UUID, RiftHolder> BUILDER = RiftHolder::new;

    @SuppressWarnings("unchecked")
    private static final Class<? extends RiftMigration>[] MIGRATIONS = new Class[]
            {RiftMigration_17_08_2022.class};

    private static RiftStorage STORAGE;

    public static RiftStorage get()
    {
        return STORAGE;
    }

    public static boolean isAvailable()
    {
        return STORAGE != null;
    }

    public static void init(MinecraftServer server)
    {
        if (STORAGE != null)
        {
            return;
        }
        STORAGE = new RiftStorage(server);
        STORAGE.migrate();
        STORAGE.loadAll();
    }

    public static void deinit()
    {
        if (STORAGE == null)
        {
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
    private final Path dataDirectory;

    private RiftStorage(MinecraftServer server)
    {
        this.dataDirectory = server.getWorldPath(DATA_DIR);
        Map<Class<? extends RiftMigration>, RiftMigration> migrations = new HashMap<>();
        for (Class<? extends RiftMigration> migration : MIGRATIONS)
        {
            if (migration == null)
            {
                continue;
            }
            try
            {
                migrations.put(migration, migration.cast(migration.getConstructor().newInstance()));
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                LOGGER.warn("Failed to load migration '{}'", migration.getSimpleName());
                continue;
            }
        }
        this.migrations = Collections.unmodifiableMap(migrations);
    }

    public <E extends RiftMigration> E getMigration(Class<E> type)
    {
        RiftMigration migration = migrations.get(type);
        if (migration == null)
        {
            return null;
        }
        return type.cast(migration);
    }

    private void migrate()
    {
        for (RiftMigration migration : migrations.values())
        {
            if (!migration.isApplicable(this))
            {
                continue;
            }
            LOGGER.info("Running migration '{}'", migration.getName());
            try
            {
                migration.migrate(this);
            }
            catch (Exception exp)
            {
                LOGGER.warn("Failed to run migration '{}'", migration.getName(), exp);
            }
        }
    }

    public Path getDataDirectory()
    {
        return dataDirectory;
    }

    public RiftHolder newRift()
    {
        UUID id;
        lock.readLock().lock();
        try
        {
            while (rifts.containsKey(id = UUID.randomUUID()))
            {
            }
            return rifts.computeIfAbsent(id, BUILDER);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public RiftHolder getRift(@Nullable UUID id)
    {
        if (id == null)
        {
            return newRift();
        }
        lock.readLock().lock();
        try
        {
            return rifts.computeIfAbsent(id, BUILDER);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public Optional<RiftHolder> findRift(@Nullable UUID id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        lock.readLock().lock();
        try
        {
            var rift = rifts.get(id);
            return Optional.ofNullable(rift);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public void loadAll()
    {
        // Prevent others from reading the map before every Rift is fully loaded
        lock.writeLock().lock();
        try
        {
            if (!Files.exists(dataDirectory))
            {
                return;
            }
            try(var fileList = Files.list(dataDirectory))
            {
                fileList.forEach(file -> {

                    String name = file.getFileName().toString();
                    if (!name.endsWith(FILE_FORMAT))
                        return;
                    name = name.substring(0, name.length() - FILE_FORMAT_LENGTH);
                    UUID id;
                    try
                    {
                        id = UUID.fromString(name);
                    }
                    catch (IllegalArgumentException iae)
                    {
                        LOGGER.warn("Found invalid rift name {}", name, iae);
                        return;
                    }
                    load(id, file);
                });
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to enumerate rifts");
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void load(UUID id)
    {
        Path file = dataDirectory.resolve(id.toString() + FILE_FORMAT);
        if (!Files.exists(file))
        {
            return;
        }
        lock.writeLock().lock();
        try
        {
            load(id, file);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private void load(UUID id, Path file)
    {
        RiftHolder holder = rifts.computeIfAbsent(id, BUILDER);
        CompoundTag tag;
        LOGGER.info("Loading rift {}...", id);
        try
        {
            tag = NbtIo.readCompressed(file, NbtAccounter.create(0x6400000L));
        }
        catch (IOException e)
        {
            holder.getInventory().clear();
            LOGGER.error("Could not load rift {}", id, e);
            return;
        }
        holder.getInventoryOrCreate().load(tag);
    }

    public void saveAll()
    {
        RiftHolder[] holders;
        lock.readLock().lock();
        try
        {
            holders = rifts.values().toArray(RiftHolder[]::new);
        }
        finally
        {
            lock.readLock().unlock();
        }
        for (RiftHolder holder : holders)
        {
            save(holder);
        }
    }

    public void save(UUID id)
    {
        lock.readLock().lock();
        RiftHolder holder;
        try
        {
            holder = rifts.get(id);
        }
        finally
        {
            lock.readLock().unlock();
        }
        save(holder);
    }

    private void save(RiftHolder holder)
    {
        if (holder == null || !holder.isValid())
        {
            return;
        }
        String id = holder.getId().toString();
        RiftInventory inventory = holder.getInventory();
        var file = dataDirectory.resolve(id + FILE_FORMAT);
        LOGGER.info("Saving rift {}...", id);
        try
        {
            if (!Files.exists(file))
            {
                var folder = file.getParent();
                if (folder != null && !Files.exists(folder))
                {
                    Files.createDirectories(folder);
                }
                Files.createFile(file);
            }
            NbtIo.writeCompressed(inventory.save(), file);
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Could not save rift {}", id, ioexception);
        }
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return name.endsWith(FILE_FORMAT);
    }

    public void walkRifts(BiConsumer<UUID, RiftInventory> riftConsumer)
    {
        rifts.forEach((id, holder) -> {
            if (!holder.isValid())
            {
                return;
            }
            riftConsumer.accept(id, holder.getInventory());
        });
    }
}