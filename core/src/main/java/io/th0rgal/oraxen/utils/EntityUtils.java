package io.th0rgal.oraxen.utils;

import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@SuppressWarnings({"unchecked", "unused", "deprecation"})
public class EntityUtils {
    private static Method spawnMethod;

    public static boolean isUnderWater(Entity entity) {
        if (VersionUtil.isPaperServer() && VersionUtil.atOrAbove("1.19")) {
            return entity.isUnderWater();
        } else return entity.isInWater();
    }

    public static boolean isFixed(ItemDisplay itemDisplay) {
        return itemDisplay.getItemDisplayTransform() == ItemDisplay.ItemDisplayTransform.FIXED;
    }

    public static boolean isNone(ItemDisplay itemDisplay) {
        return itemDisplay.getItemDisplayTransform() == ItemDisplay.ItemDisplayTransform.NONE;
    }

    public void teleport(@NotNull Location location, @NotNull Entity entity, PlayerTeleportEvent.TeleportCause cause) {
        // Fix precedence: call teleportAsync only on 1.19.4+ AND Paper/Folia
        if ((VersionUtil.isPaperServer() || VersionUtil.isFoliaServer()) && VersionUtil.atOrAbove("1.19.4")) {
            entity.teleportAsync(location, cause);
        } else entity.teleport(location);
    }

    /**
     * Teleports an entity to the given location
     * Uses teleportAsync on 1.19.4+ Paper/Folia servers and teleport on all other servers
     * @param location The location to teleport the entity to
     * @param entity The entity to teleport
     */
    public static void teleport(@NotNull Location location, @NotNull Entity entity) {
        if (VersionUtil.atOrAbove("1.19.4") && (VersionUtil.isPaperServer() || VersionUtil.isFoliaServer())) {
            entity.teleportAsync(location);
        } else entity.teleport(location);
    }

    static {
        try {
            // Try to find a spawn method on World with different consumer types depending on the server/API version.
            // Prefer the newest signature: spawn(Location, Class, java.util.function.Consumer)
            try {
                spawnMethod = World.class.getMethod("spawn", Location.class, Class.class, java.util.function.Consumer.class);
            } catch (NoSuchMethodException e1) {
                // Fallback to the older org.bukkit.util.Consumer
                try {
                    spawnMethod = World.class.getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
                } catch (NoSuchMethodException e2) {
                    // Fallback to spawn without consumer
                    try {
                        spawnMethod = World.class.getMethod("spawn", Location.class, Class.class);
                    } catch (NoSuchMethodException e3) {
                        Logs.logWarning("No suitable spawn method found via reflection: " + e3.getMessage());
                        spawnMethod = null;
                    }
                }
            }
        } catch (Exception e) {
            Logs.logWarning("Error while initializing spawnMethod: " + e.getMessage());
            spawnMethod = null;
        }
    }

    /**
     * Spawns an entity at the given location and applies the consumer to it based on server version
     * @param location The location to spawn the entity at
     * @param clazz The class of the entity to spawn
     * @param consumer The consumer to apply to the entity
     * @return The entity that was spawned
     */
    public static <T> T spawnEntity(@NotNull Location location, @NotNull Class<T> clazz, EntityConsumer<T> consumer) {
       try {
            T entity;
            World world = location.getWorld();
            Object wrappedConsumer;

            // Determine the consumer type and choose the appropriate wrapper
            if (VersionUtil.atOrAbove("1.20.2")) wrappedConsumer = new JavaConsumerWrapper<>(consumer);
            else wrappedConsumer = new BukkitConsumerWrapper<>(consumer);

            // If we found a spawnMethod during static init, try to use it.
            if (spawnMethod != null) {
                Class<?>[] params = spawnMethod.getParameterTypes();
                if (params.length == 3) {
                    // method with consumer parameter
                    entity = (T) spawnMethod.invoke(world, location, clazz, wrappedConsumer);
                    return entity;
                } else {
                    // method without consumer, spawn then apply consumer manually
                    entity = (T) spawnMethod.invoke(world, location, clazz);
                    if (entity != null && consumer != null) consumer.accept(entity);
                    return entity;
                }
            }

            // If spawnMethod is null, attempt to resolve at runtime on the actual world instance
            try {
                Method m = world.getClass().getMethod("spawn", Location.class, Class.class, java.util.function.Consumer.class);
                entity = (T) m.invoke(world, location, clazz, wrappedConsumer);
                return entity;
            } catch (NoSuchMethodException ex1) {
                try {
                    Method m2 = world.getClass().getMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
                    entity = (T) m2.invoke(world, location, clazz, wrappedConsumer);
                    return entity;
                } catch (NoSuchMethodException ex2) {
                    // Last resort: spawn without consumer then call consumer.accept(entity)
                    try {
                        Method m3 = world.getClass().getMethod("spawn", Location.class, Class.class);
                        entity = (T) m3.invoke(world, location, clazz);
                        if (entity != null && consumer != null) consumer.accept(entity);
                        return entity;
                    } catch (NoSuchMethodException ex3) {
                        Logs.logWarning("No spawn method available on World instance: " + ex3.getMessage());
                    }
                }
            }

        } catch (Exception e) {
           Logs.logWarning("Failed to spawn entity: " + e.getMessage());
        }
        return null;
    }



    public interface EntityConsumer<T> {
        void accept(T entity);
    }

    public static class JavaConsumerWrapper<T> implements java.util.function.Consumer<T> {
        private final EntityConsumer<T> entityConsumer;

        public JavaConsumerWrapper(EntityConsumer<T> entityConsumer) {
            this.entityConsumer = entityConsumer;
        }

        @Override
        public void accept(T entity) {
            entityConsumer.accept(entity);
        }
    }

    public static class BukkitConsumerWrapper<T> implements org.bukkit.util.Consumer<T> {
        private final EntityConsumer<T> entityConsumer;

        public BukkitConsumerWrapper(EntityConsumer<T> entityConsumer) {
            this.entityConsumer = entityConsumer;
        }

        @Override
        public void accept(T entity) {
            entityConsumer.accept(entity);
        }
    }

}
