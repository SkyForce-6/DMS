package org.skyforce.demon.blooddemonart.Enmu;

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
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnmuAbility {

    private final Map<UUID, Long> sleepPowderCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sleepCoreCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> tentaclesCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> tentaclesSeriesCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> trainCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sleepDimensionCooldown = new ConcurrentHashMap<>();

    // Map to track players in sleep dimension
    private final Map<UUID, BukkitRunnable> sleepDimensionPlayers = new ConcurrentHashMap<>();

    // Map to store original locations before entering sleep dimension
    private final Map<UUID, Location> originalLocations = new ConcurrentHashMap<>();

    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long SLEEP_POWDER_COOLDOWN = 0;
    private static final long SLEEP_CORE_COOLDOWN = 0;
    private static final long TENTACLES_COOLDOWN = 0;
    private static final long TENTACLES_SERIES_COOLDOWN = 0;
    private static final long TRAIN_COOLDOWN = 0;
    private static final long SLEEP_DIMENSION_COOLDOWN = 0; // 3 minutes

    /**
     * Sleep - Powder AoE, stun (5s), blindness
     */
    public void activateSleepPowder(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (sleepPowderCooldown.containsKey(playerId)) {
            long timeLeft = (sleepPowderCooldown.get(playerId) + SLEEP_POWDER_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Sleep - Powder again!");
                return;
            }
        }

        sleepPowderCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lSleep - Powder§d activated!");
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 0.8F);

        Location playerLoc = player.getLocation();

        // Create the purple powder effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 100; // 5 seconds
            private final int radius = 7;

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks) {
                    this.cancel();
                    return;
                }

                // Spawn purple dust particles in an expanding circle
                for (int i = 0; i < 20; i++) {
                    double angle = 2 * Math.PI * random.nextDouble();
                    double distance = random.nextDouble() * radius;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    Location particleLoc = playerLoc.clone().add(x, 0.5 + random.nextDouble(), z);
                    player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1,
                            new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F)); // Purple dust
                }

                // Every 20 ticks (1 second) check for entities to apply effects
                if (ticks % 20 == 0) {
                    for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply stun (extreme slowness) and blindness
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 7)); // 5 seconds, very high level
                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1)); // 5 seconds
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // 5 seconds, nausea

                            // Visual effect
                            target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getEyeLocation(),
                                    15, 0.3, 0.3, 0.3, 0.05);

                            // Display messages
                            if (target instanceof Player) {
                                ((Player) target).sendMessage("§5You feel drowsy as Enmu's sleep powder affects you!");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Sleep: Core - Triggered version with extra stun (10s)
     */
    public void activateSleepCore(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (sleepCoreCooldown.containsKey(playerId)) {
            long timeLeft = (sleepCoreCooldown.get(playerId) + SLEEP_CORE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Sleep: Core again!");
                return;
            }
        }

        sleepCoreCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lSleep: Core§d - Deeper sleep induced!");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 0.5F);

        // Get target location (block player is looking at)
        Block targetBlock = player.getTargetBlock(null, 20);
        Location coreLocation = targetBlock.getLocation().add(0.5, 1.0, 0.5);

        // Create a visual core
        ArmorStand core = (ArmorStand) coreLocation.getWorld().spawnEntity(coreLocation, EntityType.ARMOR_STAND);
        core.setVisible(false);
        core.setGravity(false);
        core.setInvulnerable(true);
        core.getEquipment().setHelmet(new ItemStack(Material.PURPLE_STAINED_GLASS));

        temporaryEntities.add(core);

        // Create initial visual effect
        coreLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, coreLocation,
                40, 0.5, 0.5, 0.5, 0.1);
        coreLocation.getWorld().playSound(coreLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 0.7F);

        // Core pulsing effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 200; // 10 seconds
            private final int radius = 10;

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks || !core.isValid()) {
                    if (core.isValid()) {
                        core.remove();
                    }
                    this.cancel();
                    return;
                }

                // Create pulsing particles
                double pulseSize = 1.0 + Math.sin(ticks / 5.0) * 0.5;

                for (int i = 0; i < 10; i++) {
                    double angle = 2 * Math.PI * random.nextDouble();
                    double distance = pulseSize * 2;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;
                    double y = random.nextDouble() * 2;

                    Location particleLoc = coreLocation.clone().add(x, y, z);
                    core.getWorld().spawnParticle(Particle.DUST, particleLoc, 1,
                            new Particle.DustOptions(Color.fromRGB(200, 0, 200), 1.5F)); // Bright purple dust
                }

                // Every 20 ticks (1 second) check for entities to apply effects
                if (ticks % 20 == 0) {
                    core.getWorld().playSound(coreLocation, Sound.BLOCK_BEACON_AMBIENT, 0.5F, 0.5F);

                    for (Entity entity : coreLocation.getWorld().getNearbyEntities(coreLocation, radius, radius, radius)) {
                        if (entity instanceof LivingEntity && entity != player && !(entity instanceof ArmorStand)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Calculate distance for effect strength (closer = stronger)
                            double distance = target.getLocation().distance(coreLocation);
                            if (distance < radius) {
                                // Apply stronger stun and effects
                                int duration = (int) (200 * (1 - distance / radius)); // Up to 10 seconds based on proximity
                                int amplifier = (int) (3 * (1 - distance / radius)); // Up to level 3 based on proximity

                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 7)); // Extreme slowness
                                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 1));
                                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, amplifier));
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 1));

                                // Create a direct connection between core and target
                                drawParticleLine(coreLocation, target.getEyeLocation(), Particle.DRAGON_BREATH, 5, plugin);

                                // Display messages
                                if (target instanceof Player) {
                                    ((Player) target).sendMessage("§5§lYou feel overwhelmed by the Sleep Core's power!");
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Tentacles - 2 tentacles, 4 hits
     */
    public void activateTentacles(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (tentaclesCooldown.containsKey(playerId)) {
            long timeLeft = (tentaclesCooldown.get(playerId) + TENTACLES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Tentacles again!");
                return;
            }
        }

        tentaclesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lTentacles§d emerge!");
        player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 0.5F);

        // Create 2 tentacles
        Location playerLoc = player.getLocation();

        // Find up to 2 targets within range
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 15, 10, 15)) {
            if (entity instanceof LivingEntity && entity != player && !(entity instanceof ArmorStand)) {
                targets.add((LivingEntity) entity);
                if (targets.size() >= 2) break;
            }
        }

        if (targets.isEmpty()) {
            // If no targets, create tentacles in front of player
            Vector direction = player.getLocation().getDirection().normalize();
            Location tentacle1Start = playerLoc.clone().add(direction.clone().multiply(2).rotateAroundY(Math.PI/4));
            Location tentacle2Start = playerLoc.clone().add(direction.clone().multiply(2).rotateAroundY(-Math.PI/4));

            createTentacle(tentacle1Start, direction.clone(), player, plugin, 4);
            createTentacle(tentacle2Start, direction.clone(), player, plugin, 4);
        } else {
            // Create tentacles targeting enemies
            for (LivingEntity target : targets) {
                Vector direction = target.getLocation().subtract(playerLoc).toVector().normalize();
                Location tentacleStart = playerLoc.clone().add(direction.clone().multiply(2));

                createTentacle(tentacleStart, direction, player, plugin, 4);
            }
        }
    }

    /**
     * Tentacles: Series - 4 tentacles, 8 hits
     */
    public void activateTentaclesSeries(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (tentaclesSeriesCooldown.containsKey(playerId)) {
            long timeLeft = (tentaclesSeriesCooldown.get(playerId) + TENTACLES_SERIES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Tentacles: Series again!");
                return;
            }
        }

        tentaclesSeriesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lTentacles: Series§d unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 0.3F);

        // Create 4 tentacles
        Location playerLoc = player.getLocation();

        // Find up to 4 targets within range
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 20, 15, 20)) {
            if (entity instanceof LivingEntity && entity != player && !(entity instanceof ArmorStand)) {
                targets.add((LivingEntity) entity);
                if (targets.size() >= 4) break;
            }
        }

        if (targets.size() <= 1) {
            // If no or only one target, create tentacles in a pattern around player
            Vector direction = player.getLocation().getDirection().normalize();

            for (int i = 0; i < 4; i++) {
                double angle = (Math.PI / 2) * i;
                Vector rotatedDir = direction.clone().rotateAroundY(angle);
                Location tentacleStart = playerLoc.clone().add(rotatedDir.clone().multiply(2));

                createTentacle(tentacleStart, rotatedDir, player, plugin, 8);
            }
        } else {
            // Create tentacles targeting enemies
            for (LivingEntity target : targets) {
                Vector direction = target.getLocation().subtract(playerLoc).toVector().normalize();
                Location tentacleStart = playerLoc.clone().add(direction.clone().multiply(2));

                createTentacle(tentacleStart, direction, player, plugin, 8);
            }
        }
    }

    /**
     * Helper method to create and animate a tentacle
     */
    private void createTentacle(Location start, Vector direction, Player owner, Main plugin, int maxHits) {
        ArmorStand tentacleBase = (ArmorStand) start.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        tentacleBase.setVisible(false);
        tentacleBase.setGravity(false);
        tentacleBase.setInvulnerable(true);
        tentacleBase.getEquipment().setHelmet(new ItemStack(Material.PURPLE_TERRACOTTA));

        temporaryEntities.add(tentacleBase);

        // Create initial visual effect
        start.getWorld().spawnParticle(Particle.DUST, start,
                10, 0.3, 0.3, 0.3, new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F));

        new BukkitRunnable() {
            private int ticks = 0;
            private int hits = 0;
            private double length = 0;
            private final double maxLength = 15; // Maximum reach
            private final double speed = 0.5;
            private final List<Location> tentaclePath = new ArrayList<>();
            private Vector currentDirection = direction.clone();

            @Override
            public void run() {
                ticks++;

                if (hits >= maxHits || length >= maxLength || !tentacleBase.isValid()) {
                    // Fade out the tentacle
                    new BukkitRunnable() {
                        private int fadeTicks = 0;

                        @Override
                        public void run() {
                            fadeTicks++;

                            if (fadeTicks > 20 || !tentacleBase.isValid()) {
                                if (tentacleBase.isValid()) {
                                    tentacleBase.remove();
                                }
                                this.cancel();
                                return;
                            }

                            // Fade effect
                            for (int i = 0; i < tentaclePath.size(); i += 2) {
                                Location loc = tentaclePath.get(i);
                                loc.getWorld().spawnParticle(Particle.DUST, loc,
                                        1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F - fadeTicks/20.0F));
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1);

                    this.cancel();
                    return;
                }

                // Add some random movement to the tentacle direction
                if (ticks % 5 == 0) {
                    currentDirection.add(new Vector(
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1
                    )).normalize();
                }

                // Extend the tentacle
                length += speed;
                Location newSegment = start.clone().add(currentDirection.clone().multiply(length));
                tentaclePath.add(newSegment);

                // Draw the tentacle
                for (int i = Math.max(0, tentaclePath.size() - 20); i < tentaclePath.size(); i++) {
                    Location loc = tentaclePath.get(i);
                    loc.getWorld().spawnParticle(Particle.DUST, loc,
                            1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F));
                }

                // Check for entity collision
                for (Entity entity : newSegment.getWorld().getNearbyEntities(newSegment, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != owner && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Damage and apply effects
                        target.damage(3.0, owner);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2)); // 3 seconds, level 3
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1)); // 2 seconds, level 2

                        // Visual and sound effects
                        target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation().add(0, 1, 0),
                                15, 0.3, 0.5, 0.3, 0.05);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SQUID_SQUIRT, 1.0F, 1.0F);

                        // Change direction after hit
                        currentDirection = new Vector(
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1
                        ).normalize();

                        hits++;
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Train - Portal spawns train attack
     */
    public void activateTrain(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (trainCooldown.containsKey(playerId)) {
            long timeLeft = (trainCooldown.get(playerId) + TRAIN_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Train again!");
                return;
            }
        }

        trainCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lInfinite Train§d summoned!");
        player.playSound(player.getLocation(), Sound.ENTITY_MINECART_INSIDE, 1.0F, 0.5F);

        // Create a portal
        Location portalLoc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        createTrainPortal(portalLoc, player, plugin);
    }

    /**
     * Helper method to create a train portal and spawn the train
     */
    private void createTrainPortal(Location portalLoc, Player owner, Main plugin) {
        World world = portalLoc.getWorld();

        // Create portal visual effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int portalFormTime = 40; // 2 seconds to form
            private double size = 0.5;
            private final double maxSize = 3.0;

            @Override
            public void run() {
                ticks++;

                if (ticks > portalFormTime) {
                    // Portal fully formed, spawn the train
                    spawnTrain(portalLoc, owner.getLocation().getDirection(), owner, plugin);
                    this.cancel();
                    return;
                }

                // Increase portal size
                size = 0.5 + (maxSize - 0.5) * (ticks / (double) portalFormTime);

                // Create portal particles
                for (int i = 0; i < 20; i++) {
                    double angle = 2 * Math.PI * i / 20;
                    double x = Math.cos(angle) * size;
                    double y = Math.sin(angle) * size;

                    Location particleLoc = portalLoc.clone().add(x, y, 0);
                    world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F));
                }

                // Sound effect
                if (ticks % 5 == 0) {
                    world.playSound(portalLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.5F, 0.8F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Helper method to spawn and animate the train attack
     */
    private void spawnTrain(Location portalLoc, Vector direction, Player owner, Main plugin) {
        World world = portalLoc.getWorld();

        // Ensure direction is horizontal
        direction.setY(0);
        direction.normalize();

        // Create a sound effect
        world.playSound(portalLoc, Sound.ENTITY_MINECART_INSIDE, 1.0F, 0.5F);
        world.playSound(portalLoc, Sound.BLOCK_BELL_USE, 1.0F, 0.5F);

        // Spawn minecart
        Minecart train = (Minecart) world.spawnEntity(portalLoc, EntityType.MINECART);
        train.setVelocity(direction.multiply(2));
        train.setInvulnerable(true);

        // Add visual effects to the train
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 100; // 5 seconds maximum

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks || !train.isValid()) {
                    if (train.isValid()) {
                        world.spawnParticle(Particle.EXPLOSION, train.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
                        world.playSound(train.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                        train.remove();
                    }
                    this.cancel();
                    return;
                }

                // Maintain velocity
                train.setVelocity(direction.clone().multiply(2));

                // Add visual effects
                Location trainLoc = train.getLocation();
                world.spawnParticle(Particle.SMOKE, trainLoc, 5, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.DUST, trainLoc, 5, 0.3, 0.3, 0.3,
                        new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F));

                // Sound effects
                if (ticks % 10 == 0) {
                    world.playSound(trainLoc, Sound.ENTITY_MINECART_INSIDE, 1.0F, 0.5F);
                }

                // Check for entities to damage
                for (Entity entity : world.getNearbyEntities(trainLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != train) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        target.damage(8.0, owner);
                        target.setVelocity(direction.clone().multiply(2).add(new Vector(0, 0.5, 0)));

                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2)); // 3 seconds, level 3
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // 2 seconds

                        // Message
                        if (target instanceof Player) {
                            ((Player) target).sendMessage("§5You've been hit by Enmu's train!");
                        }

                        // Visual effect
                        world.spawnParticle(Particle.EXPLOSION, target.getLocation(), 2, 0.1, 0.1, 0.1, 0.1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Sleep Dimension - Locks target in dream for 5 mins (no kill, no abilities, zero gravity)
     */
    public void activateSleepDimension(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (sleepDimensionCooldown.containsKey(playerId)) {
            long timeLeft = (sleepDimensionCooldown.get(playerId) + SLEEP_DIMENSION_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§dYou must wait " + timeLeft + " seconds before using Sleep Dimension again!");
                return;
            }
        }

        sleepDimensionCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§5§lSleep Dimension§d - Ultimate ability activated!");
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.5F);

        // Find a target within 20 blocks
        LivingEntity target = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = entity.getLocation().distance(player.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    target = (LivingEntity) entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage("§cNo valid target found for Sleep Dimension!");
            return;
        }

        player.sendMessage("§5Trapping " + (target instanceof Player ? ((Player)target).getName() : target.getType().name()) + " in the Sleep Dimension!");

        // Create visual effect connecting player and target
        drawParticleLine(player.getEyeLocation(), target.getEyeLocation(), Particle.DRAGON_BREATH, 20, plugin);

        // Store original location für alle LivingEntitys
        originalLocations.put(target.getUniqueId(), target.getLocation().clone());

        // Teleport to dream dimension
        Location dreamLoc = new Location(target.getWorld(),
                target.getLocation().getX(),
                target.getWorld().getMaxHeight() - 50,
                target.getLocation().getZ());

        // Create a platform
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                dreamLoc.clone().add(x, -1, z).getBlock().setType(Material.LIGHT_BLUE_STAINED_GLASS);
            }
        }

        // Apply effects and teleport
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 6000, 255, false, false)); // No gravity
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // Initial blindness during teleport
        target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 6000, 1, false, false)); // See in the void

        // Teleport nach kurzer Verzögerung
        final LivingEntity finalTarget = target;
        // Teleport nach kurzer Verzögerung
        new BukkitRunnable() {
            @Override
            public void run() {
                finalTarget.teleport(dreamLoc);
                if (finalTarget instanceof Player) {
                    ((Player)finalTarget).sendMessage("§5§lYou've been trapped in Enmu's Sleep Dimension!");
                    ((Player)finalTarget).sendMessage("§5You can't use abilities or interact with the real world for 5 minutes!");
                }
                // Dream world effect (nur für Spieler)
                if (finalTarget instanceof Player) {
                    createDreamEnvironment((Player)finalTarget, dreamLoc, plugin);
                }
            }
        }.runTaskLater(plugin, 20);
        // Release-Task für alle LivingEntitys
            final LivingEntity finalTargetForRelease = target;
            BukkitRunnable releaseTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Wenn Spieler: spezielle Release-Methode, sonst nur Teleport und Effekte entfernen
                    if (finalTargetForRelease instanceof Player) {
                        if (((Player)finalTargetForRelease).isOnline()) {
                            releaseFromSleepDimension((Player)finalTargetForRelease);
                        }
                    } else {
                        // Für andere LivingEntitys: Teleport zurück, Effekte entfernen
                        Location originalLoc = originalLocations.get(finalTargetForRelease.getUniqueId());
                        if (originalLoc != null) {
                            finalTargetForRelease.teleport(originalLoc);
                            originalLocations.remove(finalTargetForRelease.getUniqueId());
                        }
                        finalTargetForRelease.removePotionEffect(PotionEffectType.LEVITATION);
                        finalTargetForRelease.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        finalTargetForRelease.removePotionEffect(PotionEffectType.BLINDNESS);
                        finalTargetForRelease.removePotionEffect(PotionEffectType.WEAKNESS);
                    }
                    sleepDimensionPlayers.remove(finalTargetForRelease.getUniqueId());
                }
            };
            releaseTask.runTaskLater(plugin, 500);
            sleepDimensionPlayers.put(finalTargetForRelease.getUniqueId(), releaseTask);
        }


    /**
     * Helper method to create dream-like environment
     */
    private void createDreamEnvironment(Player target, Location center, Main plugin) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // Check if player is still in sleep dimension
                if (!sleepDimensionPlayers.containsKey(target.getUniqueId()) || !target.isOnline()) {
                    this.cancel();
                    return;
                }

                // Create dream particles
                for (int i = 0; i < 10; i++) {
                    double angle = 2 * Math.PI * random.nextDouble();
                    double distance = 5 + random.nextDouble() * 10;
                    double height = random.nextDouble() * 5;

                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    Location particleLoc = center.clone().add(x, height, z);

                    // Alternate between purple and blue particles
                    if (i % 2 == 0) {
                        target.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.5F));
                    } else {
                        target.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0, 0, 128), 1.5F));
                    }
                }

                // Ambient sounds
                if (ticks % 40 == 0) { // Every 2 seconds
                    target.playSound(target.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.3F, 0.5F);
                }

                if (ticks % 100 == 0) { // Every 5 seconds
                    target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.2F, 0.5F);

                    // Send message to remind player they're in a dream
                    target.sendMessage("§5You are trapped in Enmu's dream world...");
                }

                // Create distant illusions occasionally
                if (ticks % 80 == 0) { // Every 4 seconds
                    createIllusion(target, center, plugin);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Create a visual illusion in the dream world
     */
    private void createIllusion(Player target, Location center, Main plugin) {
        // Random position around the player
        double angle = 2 * Math.PI * random.nextDouble();
        double distance = 15 + random.nextDouble() * 10;
        double x = Math.cos(angle) * distance;
        double z = Math.sin(angle) * distance;

        Location illusionLoc = center.clone().add(x, random.nextDouble() * 5, z);

        // Create armor stand for the illusion
        ArmorStand illusion = (ArmorStand) center.getWorld().spawnEntity(illusionLoc, EntityType.ARMOR_STAND);
        illusion.setVisible(false);
        illusion.setGravity(false);
        illusion.setInvulnerable(true);

        // Random appearance
        Material[] possibleHeads = {
                Material.PLAYER_HEAD,
                Material.SKELETON_SKULL,
                Material.WITHER_SKELETON_SKULL,
                Material.DRAGON_HEAD
        };

        illusion.getEquipment().setHelmet(new ItemStack(possibleHeads[random.nextInt(possibleHeads.length)]));

        temporaryEntities.add(illusion);

        // Make the illusion move and fade
        new BukkitRunnable() {
            private int ticks = 0;
            private final int lifetime = 60; // 3 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > lifetime || !illusion.isValid() || !target.isOnline()) {
                    if (illusion.isValid()) {
                        illusion.remove();
                    }
                    this.cancel();
                    return;
                }

                // Move slightly
                Location current = illusion.getLocation();
                current.add(
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2,
                        (random.nextDouble() - 0.5) * 0.2
                );
                illusion.teleport(current);

                // Particles only visible to target
                target.spawnParticle(Particle.DRAGON_BREATH, current, 5, 0.2, 0.4, 0.2, 0.01);

                // Sound only audible to target
                if (ticks % 20 == 0) {
                    target.playSound(current, Sound.ENTITY_ENDERMAN_STARE, 0.3F, 0.5F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Release a player from the sleep dimension
     */
    public void releaseFromSleepDimension(Player target) {
        UUID targetId = target.getUniqueId();

        if (originalLocations.containsKey(targetId)) {
            // Teleport back to original location
            target.teleport(originalLocations.get(targetId));
            originalLocations.remove(targetId);

            // Remove effects
            target.removePotionEffect(PotionEffectType.LEVITATION);
            target.removePotionEffect(PotionEffectType.NIGHT_VISION);

            // Apply brief blindness and nausea for transition effect
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));

            // Visual effect
            target.getWorld().spawnParticle(Particle.DRAGON_BREATH, target.getLocation(),
                    50, 1, 1, 1, 0.1);
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.5F);

            target.sendMessage("§5You've been released from Enmu's dream world.");

            // Cancel any active runnable
            if (sleepDimensionPlayers.containsKey(targetId)) {
                sleepDimensionPlayers.get(targetId).cancel();
                sleepDimensionPlayers.remove(targetId);
            }
        }
    }

    /**
     * Helper method to draw a particle line between two locations
     */
    private void drawParticleLine(Location start, Location end, Particle particle, int count, Main plugin) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 20; // 1 second

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks) {
                    this.cancel();
                    return;
                }

                Vector direction = end.toVector().subtract(start.toVector());
                double length = direction.length();
                direction.normalize();

                // Draw line of particles
                for (int i = 0; i < count; i++) {
                    double progress = (double) i / count;
                    Location point = start.clone().add(direction.clone().multiply(length * progress));

                    if (particle == Particle.DUST) {
                        start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(128, 0, 128), 1.0F));
                    } else {
                        start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Cleanup method to remove all temporary entities when the plugin is reloaded
     */
    public void cleanup() {
        // Remove temporary armor stands and other entities
        for (ArmorStand entity : temporaryEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        temporaryEntities.clear();

        // Release any players trapped in sleep dimension
        for (UUID playerId : new ArrayList<>(sleepDimensionPlayers.keySet())) {
            Player player = Main.getPlugin(Main.class).getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                releaseFromSleepDimension(player);
            }
        }

        sleepDimensionPlayers.clear();
        originalLocations.clear();
    }
}

