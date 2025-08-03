package org.skyforce.demon.blooddemonart.Muzan;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuzanAbility {

    private final Map<UUID, Long> demonKingCooldown = new HashMap<>();
    private final Map<UUID, Long> infinityCastleCooldown = new HashMap<>();
    private final Map<UUID, Long> upperMoonsCooldown = new HashMap<>();
    private final Map<UUID, Long> demonKingsWrathCooldown = new HashMap<>();

    // Track portals and summoned entities
    private final Map<UUID, List<Location>> activePortals = new HashMap<>();
    private final List<Entity> summonedEntities = new ArrayList<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();

    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long DEMON_KING_COOLDOWN = 30;
    private static final long INFINITY_CASTLE_COOLDOWN = 60;
    private static final long UPPER_MOONS_COOLDOWN = 120; // 2 minutes
    private static final long DEMON_KINGS_WRATH_COOLDOWN = 45;

    // Map to store original locations before entering Infinity Castle
    private final Map<UUID, Location> originalLocations = new ConcurrentHashMap<>();

    /**
     * Demon King – Shockwave, Weakened (10s), Stun (1s), Slowness (5s)
     */
    public void activateDemonKing(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (demonKingCooldown.containsKey(playerId)) {
            long timeLeft = (demonKingCooldown.get(playerId) + DEMON_KING_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§4You must wait " + timeLeft + " seconds before using Demon King again!");
                return;
            }
        }

        demonKingCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lDemon King§c's power unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0F, 0.5F);

        Location playerLoc = player.getLocation();
        World world = player.getWorld();

        // Create initial shockwave effect
        world.spawnParticle(Particle.EXPLOSION, playerLoc, 3, 0.1, 0.1, 0.1, 0);
        world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.7F);

        // Red particle ring expanding outward
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 30; // 1.5 seconds
            private double radius = 1.0;
            private final double maxRadius = 15.0;

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks) {
                    this.cancel();
                    return;
                }

                // Expand ring radius
                radius = 1.0 + (maxRadius - 1.0) * (ticks / (double) maxTicks);

                // Create ring particles
                for (int i = 0; i < 36; i++) {
                    double angle = 2 * Math.PI * i / 36;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = playerLoc.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.RED, 2.0F));

                    // Add some vertical particles for effect
                    if (i % 3 == 0) {
                        for (double y = 0; y < 2; y += 0.25) {
                            Location vertLoc = particleLoc.clone().add(0, y, 0);
                            world.spawnParticle(Particle.DUST, vertLoc, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.RED, 1.5F));
                        }
                    }
                }

                // Every 5 ticks, check for entities to apply effects
                if (ticks % 5 == 0) {
                    double currentRadius = radius - 2; // Affect entities slightly before the visual wave
                    if (currentRadius > 0) {
                        for (Entity entity : world.getNearbyEntities(playerLoc, currentRadius, 5, currentRadius)) {
                            // Only affect entities that haven't been hit yet and are within 2 blocks of the wave
                            double distance = Math.sqrt(
                                    Math.pow(entity.getLocation().getX() - playerLoc.getX(), 2) +
                                            Math.pow(entity.getLocation().getZ() - playerLoc.getZ(), 2));

                            if (entity instanceof LivingEntity && entity != player &&
                                    Math.abs(distance - currentRadius) <= 2.0) {

                                LivingEntity target = (LivingEntity) entity;

                                // Apply effects
                                target.damage(8.0, player);

                                // Stun (extreme slowness for 1 second)
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 7));

                                // Slowness for 5 seconds
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));

                                // Weakness for 10 seconds
                                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1));

                                // Poison for 3 seconds
                                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));

                                // Visual effect
                                world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                                        20, 0.5, 1, 0.5, new Particle.DustOptions(Color.RED, 1.5F));

                                // Knockback effect
                                Vector knockback = target.getLocation().toVector()
                                        .subtract(playerLoc.toVector())
                                        .normalize()
                                        .multiply(2.0);
                                knockback.setY(0.5);
                                target.setVelocity(target.getVelocity().add(knockback));

                                // Message
                                if (target instanceof Player) {
                                    ((Player) target).sendMessage("§4Muzan's power weakens you!");
                                }
                            }
                        }
                    }
                }

                // Sound effects
                if (ticks % 5 == 0) {
                    world.playSound(playerLoc, Sound.ENTITY_WITHER_HURT, 0.7F, 0.5F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Infinity Castle – Opens portals to the Infinity Castle dimension
     */
    public void activateInfinityCastle(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (infinityCastleCooldown.containsKey(playerId)) {
            long timeLeft = (infinityCastleCooldown.get(playerId) + INFINITY_CASTLE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§4You must wait " + timeLeft + " seconds before using Infinity Castle again!");
                return;
            }
        }

        infinityCastleCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lInfinity Castle§c opens its doors!");
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.7F);

        // Create portals
        List<Location> portalLocations = new ArrayList<>();

        // Create 3 portals around the player
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();

        // First portal near player
        Location portal1 = playerLoc.clone().add(player.getLocation().getDirection().multiply(5));
        portalLocations.add(portal1);

        // Two more portals at random locations
        for (int i = 0; i < 2; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = 10 + random.nextDouble() * 10;
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;

            Location portalLoc = playerLoc.clone().add(x, 0, z);
            // Adjust Y to find suitable ground
            portalLoc.setY(world.getHighestBlockYAt(portalLoc));

            portalLocations.add(portalLoc);
        }

        // Store active portals
        activePortals.put(playerId, portalLocations);

        // Create the portal effects
        for (Location portalLoc : portalLocations) {
            createPortal(portalLoc, player, plugin);
        }

        // Schedule portal teleportation system
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 600; // 30 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    // Close portals
                    for (Location portalLoc : portalLocations) {
                        world.spawnParticle(Particle.EXPLOSION, portalLoc, 1, 0, 0, 0, 0);
                        world.playSound(portalLoc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 0.5F);
                    }

                    activePortals.remove(playerId);
                    this.cancel();
                    return;
                }

                // Every second, check if entities are near portals
                if (ticks % 20 == 0) {
                    checkPortalTeleportations(player, portalLocations, plugin);
                }

                // Animate portals
                if (ticks % 5 == 0) {
                    for (Location portalLoc : portalLocations) {
                        animatePortal(portalLoc);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Helper method to create a portal effect
     */
    private void createPortal(Location location, Player owner, Main plugin) {
        World world = location.getWorld();

        // Initial portal effect
        world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
        world.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 0.5F);

        // Create portal visuals
        new BukkitRunnable() {
            private int ticks = 0;
            private final int formationTime = 40; // 2 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > formationTime) {
                    // Portal fully formed
                    this.cancel();
                    return;
                }

                // Growing portal effect
                double progress = ticks / (double) formationTime;
                double size = 2.0 * progress;

                // Create circular portal
                for (int i = 0; i < 16; i++) {
                    double angle = 2 * Math.PI * i / 16;
                    double x = Math.cos(angle) * size;
                    double y = Math.sin(angle) * size;

                    Location particleLoc = location.clone().add(x, y + 1, 0);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.RED, 1.5F));

                    // Inner portal effect
                    if (progress > 0.5) {
                        Location innerLoc = location.clone().add(
                                x * 0.5, y * 0.5 + 1, 0);
                        world.spawnParticle(Particle.PORTAL, innerLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Sound effects
                if (ticks % 10 == 0) {
                    world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 0.5F, 0.7F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        // Create invisible marker (without visible name)
        ArmorStand portalMarker = (ArmorStand) world.spawnEntity(location.clone().add(0, -0.5, 0), EntityType.ARMOR_STAND);
        portalMarker.setVisible(false);
        portalMarker.setGravity(false);
        portalMarker.setInvulnerable(true);
        portalMarker.setCustomNameVisible(false); // Name is not visible

        temporaryEntities.add(portalMarker);
    }

    /**
     * Helper method to check for portal teleportations
     */
    private void checkPortalTeleportations(Player owner, List<Location> portalLocations, Main plugin) {
        if (portalLocations.size() < 2) return;

        // Check each portal for nearby entities
        for (int i = 0; i < portalLocations.size(); i++) {
            Location portalLoc = portalLocations.get(i);
            World world = portalLoc.getWorld();

            // Find nearby entities
            for (Entity entity : world.getNearbyEntities(portalLoc, 2, 3, 2)) {
                if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                    LivingEntity target = (LivingEntity) entity;

                    // Special case: if the entity is Muzan (owner), teleport to Infinity Castle dimension
                    if (target == owner) {
                        // Teleportation effect at source
                        world.spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0),
                                30, 0.5, 1, 0.5, 0.1);
                        world.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

                        // Teleport to Infinity Castle dimension
                        teleportToInfinityCastle((Player)target, owner, plugin);
                        return;
                    }
                    // For other entities, teleport between portals
                    else {
                        // Determine destination portal (random different portal)
                        List<Location> possibleDestinations = new ArrayList<>(portalLocations);
                        possibleDestinations.remove(portalLoc);
                        Location destination = possibleDestinations.get(random.nextInt(possibleDestinations.size()));

                        // Teleport effect at source
                        world.spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0),
                                30, 0.5, 1, 0.5, 0.1);
                        world.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

                        // Perform teleport after short delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!target.isValid()) return;

                                // Teleport to destination portal
                                target.teleport(destination.clone().add(0, 1, 0));

                                // Teleport effect at destination
                                world.spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0),
                                        30, 0.5, 1, 0.5, 0.1);
                                world.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

                                // Apply effects
                                if (target instanceof Player) {
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
                                    ((Player) target).sendMessage("§4You've been transported through Muzan's Infinity Castle!");
                                }
                            }
                        }.runTaskLater(plugin, 5);

                        // Only teleport one entity per check
                        return;
                    }
                }
            }
        }
    }

    /**
     * Helper method to teleport player to Infinity Castle dimension
     */
    private void teleportToInfinityCastle(Player player, Player owner, Main plugin) {
        // Store original location
        UUID playerId = player.getUniqueId();
        originalLocations.put(playerId, player.getLocation().clone());

        // Apply effects before teleport
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));

        // Teleport after brief delay
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get or create Infinity Castle world
                // This is a conceptual implementation - you'll need to adapt this to your server setup
                World infinityCastle = getInfinityCastleWorld(plugin);

                if (infinityCastle != null) {
                    // Find safe spawn location in Infinity Castle
                    Location spawnLoc = findSafeLocation(infinityCastle);

                    // Teleport to Infinity Castle
                    player.teleport(spawnLoc);

                    // Special effects after teleport
                    infinityCastle.spawnParticle(Particle.PORTAL, spawnLoc.add(0, 1, 0),
                            30, 0.5, 1, 0.5, 0.1);
                    infinityCastle.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

                    // Send message
                    player.sendMessage("§4§lYou've been transported to Muzan's Infinity Castle!");

                    // Schedule return to normal world after some time (only if not the owner)
                    if (player != owner) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline() && originalLocations.containsKey(playerId)) {
                                    returnFromInfinityCastle(player);
                                }
                            }
                        }.runTaskLater(plugin, 1200); // 60 seconds
                    }
                } else {
                    // Fallback if dimension doesn't exist
                    player.sendMessage("§cCould not access the Infinity Castle dimension!");
                }
            }
        }.runTaskLater(plugin, 20); // 1 second delay
    }

    /**
     * Helper method to animate existing portals
     */
    private void animatePortal(Location location) {
        World world = location.getWorld();

        // Portal ambient effects
        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            double size = 2.0;
            double x = Math.cos(angle) * size;
            double y = Math.sin(angle) * size;

            Location particleLoc = location.clone().add(x, y + 1, 0);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                    new Particle.DustOptions(Color.RED, 1.5F));
        }

        // Inner portal swirl
        for (int i = 0; i < 5; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            double size = random.nextDouble() * 1.5;
            double x = Math.cos(angle) * size;
            double y = Math.sin(angle) * size;

            Location particleLoc = location.clone().add(x, y + 1, 0);
            world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Helper method to get or create the Infinity Castle world
     */
    private World getInfinityCastleWorld(Main plugin) {
        // This is a conceptual implementation
        // In a real implementation, you would either:
        // 1. Use an existing world that's configured as your Infinity Castle
        // 2. Create a new world using WorldCreator if it doesn't exist

        // Check if world already exists
        World infinityCastle = plugin.getServer().getWorld("infinity_castle");

        if (infinityCastle == null) {
            // The logic to create or access the world would depend on your server setup
            // For example, you might use a predefined world on your server
            // or use MultiVerse or similar plugin API

            // Example using default method (would need to be adapted to your setup)
        /*
        WorldCreator creator = new WorldCreator("infinity_castle");
        creator.environment(World.Environment.THE_END); // Using End for unique appearance
        creator.type(WorldType.NORMAL);
        infinityCastle = creator.createWorld();
        */

            // For this example, we'll just use the Nether as a fallback
            infinityCastle = plugin.getServer().getWorld("world_nether");

            // If still null, use the normal world as last resort
            if (infinityCastle == null) {
                infinityCastle = plugin.getServer().getWorld("world");
            }
        }

        return infinityCastle;
    }

    /**
     * Helper method to find a safe location in the Infinity Castle world
     */
    private Location findSafeLocation(World world) {
        // This would need to be adapted based on how your Infinity Castle world is set up
        // For example, you might have predefined safe locations

        // Simple implementation - find a safe spot near spawn
        Location spawn = world.getSpawnLocation();

        // Check for a safe location nearby
        for (int x = -10; x <= 10; x += 5) {
            for (int z = -10; z <= 10; z += 5) {
                Location loc = spawn.clone().add(x, 0, z);
                loc.setY(world.getHighestBlockYAt(loc));

                // Check if location is safe
                Block block = loc.getBlock();
                Block below = block.getRelative(0, -1, 0);
                Block above = block.getRelative(0, 1, 0);

                if (!block.getType().isSolid() && !above.getType().isSolid() && below.getType().isSolid()) {
                    return loc.add(0.5, 1, 0.5); // Center on block and stand on top
                }
            }
        }

        // If no safe location found, return spawn with y+1 (to avoid suffocation)
        return spawn.clone().add(0, 1, 0);
    }

    /**
     * Helper method to return player from Infinity Castle
     */
    private void returnFromInfinityCastle(Player player) {
        UUID playerId = player.getUniqueId();

        if (originalLocations.containsKey(playerId)) {
            // Teleport back to original location
            Location originalLoc = originalLocations.get(playerId);

            // Apply effects before teleport
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));

            // Teleport effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0),
                    30, 0.5, 1, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

            // Teleport after brief delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(originalLoc);

                    // Effects after return
                    originalLoc.getWorld().spawnParticle(Particle.PORTAL, originalLoc.add(0, 1, 0),
                            30, 0.5, 1, 0.5, 0.1);
                    originalLoc.getWorld().playSound(originalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);

                    player.sendMessage("§4You've returned from the Infinity Castle!");

                    // Remove from tracking
                    originalLocations.remove(playerId);
                }
            }.runTaskLater(Main.getPlugin(Main.class), 20); // 1 second delay
        }
    }
    /**
     * Upper Moons – Summons all Upper Moons as mobs
     */
    public void activateUpperMoons(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (upperMoonsCooldown.containsKey(playerId)) {
            long timeLeft = (upperMoonsCooldown.get(playerId) + UPPER_MOONS_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§4You must wait " + timeLeft + " seconds before using Upper Moons again!");
                return;
            }
        }

        upperMoonsCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lUpper Moons§c, attend to me!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.5F);

        // Define Upper Moon data
        String[] upperMoonNames = {
                "§4Upper Moon 1: Kokushibo",
                "§4Upper Moon 2: Doma",
                "§4Upper Moon 3: Akaza",
                "§4Upper Moon 4: Nakime",
                "§4Upper Moon 5: Gyokko",
                "§4Upper Moon 6: Daki & Gyutaro"
        };

        // Create a circle of summoning portals
        Location centerLoc = player.getLocation();
        World world = player.getWorld();
        List<Location> summonLocations = new ArrayList<>();

        for (int i = 0; i < upperMoonNames.length; i++) {
            double angle = 2 * Math.PI * i / upperMoonNames.length;
            double distance = 5;
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;

            Location summonLoc = centerLoc.clone().add(x, 0, z);
            summonLocations.add(summonLoc);

            // Create portal effect for each summon location
            createPortal(summonLoc, player, plugin);
        }

        // Initial summoning effect
        world.spawnParticle(Particle.EXPLOSION, centerLoc, 1, 0, 0, 0, 0);
        world.playSound(centerLoc, Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.5F);

        // Red particles connecting all portals to center
        new BukkitRunnable() {
            private int ticks = 0;
            private final int preparationTime = 60; // 3 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > preparationTime) {
                    // Summon the Upper Moons
                    summonUpperMoons(player, summonLocations, upperMoonNames, plugin);
                    this.cancel();
                    return;
                }

                // Connect center to each portal with red beam
                for (Location summonLoc : summonLocations) {
                    drawBeam(centerLoc.clone().add(0, 1, 0), summonLoc.clone().add(0, 1, 0), Color.RED);
                }

                // Sound effects
                if (ticks % 20 == 0) {
                    world.playSound(centerLoc, Sound.ENTITY_WITHER_AMBIENT, 0.7F, 0.5F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Helper method to summon the Upper Moons
     */
    private void summonUpperMoons(Player owner, List<Location> locations, String[] names, Main plugin) {
        for (int i = 0; i < locations.size(); i++) {
            Location summonLoc = locations.get(i);
            String name = names[i];

            // Summon effect
            summonLoc.getWorld().spawnParticle(Particle.EXPLOSION, summonLoc, 1, 0, 0, 0, 0);
            summonLoc.getWorld().playSound(summonLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0F, 0.5F);

            // Summon the appropriate entity type
            EntityType entityType;
            if (i < 3) {
                // Upper Moons 1-3 are wither skeletons (stronger)
                entityType = EntityType.WITHER_SKELETON;
            } else {
                // Upper Moons 4-6 are skeletons
                entityType = EntityType.SKELETON;
            }

            // Create the Upper Moon
            LivingEntity upperMoon = (LivingEntity) summonLoc.getWorld().spawnEntity(summonLoc, entityType);

            // Set name
            upperMoon.setCustomName(name);
            upperMoon.setCustomNameVisible(true);
            upperMoon.setRemoveWhenFarAway(false);

            // Equipment based on rank
            upperMoon.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));

            if (i < 3) {
                // Upper Moons 1-3 get netherite equipment
                upperMoon.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                upperMoon.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                upperMoon.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                upperMoon.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            } else {
                // Upper Moons 4-6 get diamond equipment
                upperMoon.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                upperMoon.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                upperMoon.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                upperMoon.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
            }

            // Set equipment drop chances to 0
            upperMoon.getEquipment().setHelmetDropChance(0);
            upperMoon.getEquipment().setChestplateDropChance(0);
            upperMoon.getEquipment().setLeggingsDropChance(0);
            upperMoon.getEquipment().setBootsDropChance(0);
            upperMoon.getEquipment().setItemInMainHandDropChance(0);

            // Apply effects based on rank
            int strength = Math.max(1, 3 - i/2); // Level 3 for UM1, down to 1 for UM6
            int resistance = Math.max(0, 2 - i/2); // Level 2 for UM1, down to 0 for UM5-6
            int speed = 1; // Level 2 for all

            upperMoon.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, strength));
            upperMoon.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, resistance));
            upperMoon.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, speed));
            upperMoon.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0));

            // Make them target the nearest enemy
            findTargetForSummon(upperMoon, owner);

            summonedEntities.add(upperMoon);

            // Schedule removal after 2 minutes
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (upperMoon.isValid()) {
                        // Disappearance effect
                        upperMoon.getWorld().spawnParticle(Particle.EXPLOSION, upperMoon.getLocation(), 1, 0, 0, 0, 0);
                        upperMoon.getWorld().playSound(upperMoon.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.7F, 0.5F);

                        upperMoon.remove();
                        summonedEntities.remove(upperMoon);
                    }
                }
            }.runTaskLater(plugin, 2400); // 2 minutes
        }
    }

    /**
     * Helper method to find targets for summoned entities
     */
    private void findTargetForSummon(LivingEntity summon, Player owner) {
        for (Entity entity : summon.getWorld().getNearbyEntities(summon.getLocation(), 30, 20, 30)) {
            if (entity instanceof LivingEntity && entity != owner && entity != summon &&
                    !(entity instanceof ArmorStand) && !summonedEntities.contains(entity)) {

                if (summon instanceof Skeleton) {
                    ((Skeleton) summon).setTarget((LivingEntity) entity);
                } else if (summon instanceof WitherSkeleton) {
                    ((WitherSkeleton) summon).setTarget((LivingEntity) entity);
                }

                return;
            }
        }
    }

    /**
     * Demon King's Wrath – AoE red tentacle strike
     */
    public void activateDemonKingsWrath(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (demonKingsWrathCooldown.containsKey(playerId)) {
            long timeLeft = (demonKingsWrathCooldown.get(playerId) + DEMON_KINGS_WRATH_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§4You must wait " + timeLeft + " seconds before using Demon King's Wrath again!");
                return;
            }
        }

        demonKingsWrathCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lDemon King's Wrath§c unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.5F);

        // Create initial effect
        Location playerLoc = player.getLocation();
        World world = player.getWorld();

        world.spawnParticle(Particle.EXPLOSION, playerLoc, 2, 0.1, 0.1, 0.1, 0);
        world.playSound(playerLoc, Sound.ENTITY_WITHER_SHOOT, 1.0F, 0.5F);

        // Launch multiple tentacles in different directions
        int tentacleCount = 8;
        for (int i = 0; i < tentacleCount; i++) {
            double angle = 2 * Math.PI * i / tentacleCount;
            Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));

            // Delay each tentacle slightly
            new BukkitRunnable() {
                @Override
                public void run() {
                    createTentacle(playerLoc, direction, player, plugin);
                }
            }.runTaskLater(plugin, i * 3);
        }
    }

    /**
     * Helper method to create a tentacle attack
     */
    private void createTentacle(Location start, Vector direction, Player owner, Main plugin) {
        World world = start.getWorld();

        // Initial effect
        world.playSound(start, Sound.ENTITY_SQUID_SQUIRT, 1.0F, 0.5F);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 40; // 2 seconds
            private double length = 0;
            private final double maxLength = 15; // Maximum reach
            private final double speed = 0.8;
            private final List<Location> tentaclePath = new ArrayList<>();
            private Vector currentDirection = direction.clone();
            private final List<LivingEntity> hitEntities = new ArrayList<>(); // Track hit entities

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks || length >= maxLength) {
                    // Fade out the tentacle
                    new BukkitRunnable() {
                        private int fadeTicks = 0;

                        @Override
                        public void run() {
                            fadeTicks++;

                            if (fadeTicks > 20) {
                                this.cancel();
                                return;
                            }

                            // Fade effect
                            for (int i = 0; i < tentaclePath.size(); i += 2) {
                                Location loc = tentaclePath.get(i);
                                world.spawnParticle(Particle.DUST, loc,
                                        1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.RED, 1.0F - fadeTicks/20.0F));
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1);

                    this.cancel();
                    return;
                }

                // Add some random movement to the tentacle direction
                if (ticks % 5 == 0) {
                    currentDirection.add(new Vector(
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2
                    )).normalize();
                }

                // Extend the tentacle
                length += speed;
                Location newSegment = start.clone().add(currentDirection.clone().multiply(length));
                tentaclePath.add(newSegment);

                // Draw the tentacle
                for (int i = Math.max(0, tentaclePath.size() - 20); i < tentaclePath.size(); i++) {
                    Location loc = tentaclePath.get(i);
                    world.spawnParticle(Particle.DUST, loc,
                            1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.RED, 2.0F));

                    // Add some fleshy texture particles
                    if (i % 3 == 0) {
                        world.spawnParticle(Particle.CRIMSON_SPORE, loc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }

                // Check for entity collision
                for (Entity entity : world.getNearbyEntities(newSegment, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != owner &&
                            !(entity instanceof ArmorStand) && !hitEntities.contains(entity)) {

                        LivingEntity target = (LivingEntity) entity;
                        hitEntities.add(target); // Mark as hit

                        // Deal significant damage
                        target.damage(10.0, owner);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 2)); // 5 seconds, level 3
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1)); // 6 seconds, level 2
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2)); // 4 seconds, level 3

                        // Visual and sound effects
                        world.spawnParticle(Particle.EXPLOSION, target.getLocation().add(0, 1, 0),
                                5, 0.3, 0.5, 0.3, 0.05);
                        world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                                30, 0.5, 1, 0.5, new Particle.DustOptions(Color.RED, 2.0F));
                        world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 0.5F);

                        // Push target back
                        Vector knockback = currentDirection.clone().multiply(1.5);
                        knockback.setY(0.5);
                        target.setVelocity(target.getVelocity().add(knockback));

                        // Display message
                        if (target instanceof Player) {
                            ((Player) target).sendMessage("§4Muzan's tentacle strikes you with immense power!");
                        }

                        // Change direction to target another entity
                        currentDirection = new Vector(
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1
                        ).normalize();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Apply Muzan attack effects - poison and one-hit kill against demons
     */
    public void applyMuzanAttackEffects(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getEntity();

        // Apply poison to all attacks
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1)); // 3 seconds, level 2

        // Visual effect
        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                15, 0.3, 0.5, 0.3, new Particle.DustOptions(Color.RED, 1.5F));

        // Increase damage slightly
        event.setDamage(event.getDamage() * 1.2);

        // Check if target is a demon for one-hit kill
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;

            try {
                // Check if target is a demon
                PlayerProfile targetProfile = Main.getPlugin(Main.class).getPlayerDataManager().loadProfile(targetPlayer.getUniqueId());
                if (targetProfile.getPlayerClass() == PlayerClass.DEMON) {
                    // One-hit kill
                    event.setDamage(1000); // Essentially a one-hit kill

                    // Special effect
                    target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0F, 0.5F);

                    player.sendMessage("§4§lYou executed a demon with your blood!");
                    targetPlayer.sendMessage("§4§lMuzan's blood is fatal to other demons!");
                }
            } catch (Exception e) {
                // Handle any errors gracefully
            }
        }
    }

    /**
     * Helper method to draw a beam between two locations
     */
    private void drawBeam(Location start, Location end, Color color) {
        World world = start.getWorld();
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        // Draw beam particles
        for (double i = 0; i < length; i += 0.5) {
            Location point = start.clone().add(direction.clone().multiply(i));
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0,
                    new Particle.DustOptions(color, 1.5F));
        }
    }

    /**
     * Cleanup method to remove all temporary entities when plugin is reloaded
     */
    public void cleanup() {
        // Remove temporary armor stands
        for (ArmorStand entity : temporaryEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        temporaryEntities.clear();

        // Remove summoned entities
        for (Entity entity : summonedEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        summonedEntities.clear();

        // Clear portal locations
        activePortals.clear();
    }
}