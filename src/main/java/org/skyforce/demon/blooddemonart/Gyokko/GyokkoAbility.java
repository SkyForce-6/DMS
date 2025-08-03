package org.skyforce.demon.blooddemonart.Gyokko;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GyokkoAbility {

    private final Map<UUID, Long> fishSummonCooldown = new HashMap<>();
    private final Map<UUID, Long> waterCageCooldown = new HashMap<>();
    private final Map<UUID, Long> poisonNeedlesCooldown = new HashMap<>();
    private final Map<UUID, Long> vaseTransportationCooldown = new HashMap<>();
    private final Map<UUID, Long> fishSwarmCooldown = new HashMap<>();
    private final Map<UUID, Long> moltingTransformationCooldown = new HashMap<>();

    // Molting Transformation tracking
    private final Map<UUID, Boolean> moltingActive = new HashMap<>();
    private final Map<UUID, Location> vaseLocations = new HashMap<>();

    private final List<Block> temporaryWaterBlocks = new ArrayList<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final List<ArmorStand> fishMobs = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long FISH_SUMMON_COOLDOWN = 15;
    private static final long WATER_CAGE_COOLDOWN = 25;
    private static final long POISON_NEEDLES_COOLDOWN = 12;
    private static final long VASE_TRANSPORTATION_COOLDOWN = 20;
    private static final long FISH_SWARM_COOLDOWN = 18;
    private static final long MOLTING_TRANSFORMATION_COOLDOWN = 60;

    /**
     * Fish Summon - Summons two-legged fish mobs
     */
    public void activateFishSummon(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (fishSummonCooldown.containsKey(playerId)) {
            long timeLeft = (fishSummonCooldown.get(playerId) + FISH_SUMMON_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Fish Summon wieder einsetzen kannst!");
                return;
            }
        }

        fishSummonCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§3§lFish Summon§3 - Fischkrieger erscheinen!");
        player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 1.0F, 1.2F);

        // Determine number of fish based on molting state
        int fishCount = moltingActive.getOrDefault(playerId, false) ? 6 : 3;

        // Summon fish around the player
        for (int i = 0; i < fishCount; i++) {
            double angle = (360.0 / fishCount) * i;
            double x = Math.cos(Math.toRadians(angle)) * 3;
            double z = Math.sin(Math.toRadians(angle)) * 3;
            Location fishLoc = player.getLocation().add(x, 0, z);

            summonFishMob(fishLoc, player, plugin);
        }

        // Water particle effect
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation().add(0, 1, 0),
                30, 2, 1, 2, 0.1);
    }

    /**
     * Helper method to summon a fish mob
     */
    private void summonFishMob(Location location, Player owner, Main plugin) {
        ArmorStand fishMob = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        fishMob.setVisible(false);
        fishMob.setGravity(false);
        fishMob.setInvulnerable(true);
        fishMob.setSmall(true);
        fishMob.getEquipment().setHelmet(new ItemStack(Material.COD));
        fishMob.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        temporaryEntities.add(fishMob);
        fishMobs.add(fishMob);

        // Visual effect
        location.getWorld().spawnParticle(Particle.SPLASH, location, 15, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_COD_FLOP, 1.0F, 1.0F);

        // Fish AI behavior
        new BukkitRunnable() {
            private int ticks = 0;
            private final int lifespan = 600; // 30 seconds
            private LivingEntity currentTarget = null;

            @Override
            public void run() {
                ticks++;

                if (ticks > lifespan || !fishMob.isValid() || !owner.isOnline()) {
                    fishMob.remove();
                    fishMobs.remove(fishMob);
                    this.cancel();
                    return;
                }

                // Find nearest enemy if no current target
                if (currentTarget == null || !currentTarget.isValid()) {
                    double minDistance = Double.MAX_VALUE;
                    for (Entity entity : fishMob.getLocation().getWorld().getNearbyEntities(fishMob.getLocation(), 15, 5, 15)) {
                        if (entity instanceof LivingEntity && entity != owner && entity != fishMob) {
                            double distance = entity.getLocation().distance(fishMob.getLocation());
                            if (distance < minDistance) {
                                minDistance = distance;
                                currentTarget = (LivingEntity) entity;
                            }
                        }
                    }
                }

                // Move towards and attack target
                if (currentTarget != null && currentTarget.isValid()) {
                    Vector direction = currentTarget.getLocation().toVector()
                            .subtract(fishMob.getLocation().toVector()).normalize().multiply(0.25);

                    Location newLoc = fishMob.getLocation().add(direction);
                    ensureFiniteLocation(newLoc);
                    fishMob.teleport(newLoc);

                    // Attack if close enough
                    if (fishMob.getLocation().distance(currentTarget.getLocation()) < 1.5) {
                        currentTarget.damage(4.0, owner);
                        currentTarget.getWorld().spawnParticle(Particle.SPLASH,
                                currentTarget.getLocation().add(0, 1, 0), 10, 0.3, 0.8, 0.3, 0.05);
                        currentTarget.getWorld().playSound(currentTarget.getLocation(),
                                Sound.ENTITY_COD_HURT, 1.0F, 1.2F);
                    }

                    // Water trail particles
                    fishMob.getWorld().spawnParticle(Particle.DRIPPING_WATER, fishMob.getLocation(),
                            2, 0.1, 0.1, 0.1, 0.01);
                }

                // Random movement if no target
                if (currentTarget == null && ticks % 40 == 0) {
                    Vector randomDirection = new Vector(
                            random.nextDouble() * 2 - 1,
                            0,
                            random.nextDouble() * 2 - 1
                    ).normalize().multiply(0.3);

                    Location newLoc = fishMob.getLocation().add(randomDirection);
                    ensureFiniteLocation(newLoc);
                    fishMob.teleport(newLoc);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Water Cage - Trap + stun + tick damage (10s)
     */
    public void activateWaterCage(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (waterCageCooldown.containsKey(playerId)) {
            long timeLeft = (waterCageCooldown.get(playerId) + WATER_CAGE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Water Cage wieder einsetzen kannst!");
                return;
            }
        }

        waterCageCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§1§lWater Cage§1 wird erschaffen!");
        player.playSound(player.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 0.8F);

        Location targetLoc = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);

        // Find target in area
        LivingEntity target = null;
        for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, 3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                target = (LivingEntity) entity;
                break;
            }
        }

        if (target == null) {
            player.sendMessage("§cKein Ziel für Water Cage gefunden!");
            return;
        }

        createWaterCage(target.getLocation(), target, player, plugin);
    }

    /**
     * Helper method to create water cage
     */
    private void createWaterCage(Location center, LivingEntity target, Player owner, Main plugin) {
        List<Block> cageBlocks = new ArrayList<>();

        // Create cage structure (5x5x3 hollow cube)
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    // Only place blocks on the edges (hollow inside)
                    if (Math.abs(x) == 2 || Math.abs(z) == 2 || y == 0 || y == 2) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.WATER);
                            cageBlocks.add(block);
                            temporaryWaterBlocks.add(block);
                        }
                    }
                }
            }
        }

        // Teleport target to center and apply effects
        target.teleport(center);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 10)); // 10 seconds, max slowness
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 200, 3)); // Mining fatigue

        // Water cage effects
        center.getWorld().spawnParticle(Particle.BUBBLE, center.clone().add(0, 1, 0),
                50, 2, 1, 2, 0.1);
        center.getWorld().playSound(center, Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.0F);

        owner.sendMessage("§1" + target.getName() + " wurde in einem Wasserkäfig gefangen!");

        // Tick damage over time
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 200; // 10 seconds

            @Override
            public void run() {
                ticks += 20;

                if (ticks > duration || !target.isValid()) {
                    this.cancel();
                    return;
                }

                // Deal tick damage every second
                target.damage(2.0, owner);
                target.getWorld().spawnParticle(Particle.BUBBLE, target.getLocation().add(0, 1, 0),
                        10, 0.5, 1, 0.5, 0.05);
            }
        }.runTaskTimer(plugin, 20, 20); // Every second

        // Remove cage after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : cageBlocks) {
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.SPLASH,
                                block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }
                cageBlocks.clear();
                temporaryWaterBlocks.removeAll(cageBlocks);
            }
        }.runTaskLater(plugin, 200); // 10 seconds
    }

    /**
     * Poison Needles - Cone AoE poison
     */
    public void activatePoisonNeedles(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (poisonNeedlesCooldown.containsKey(playerId)) {
            long timeLeft = (poisonNeedlesCooldown.get(playerId) + POISON_NEEDLES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Poison Needles wieder einsetzen kannst!");
                return;
            }
        }

        poisonNeedlesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§2§lPoison Needles§2 werden abgefeuert!");
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 0.6F);

        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Create cone of poison needles
        for (int i = 0; i < 12; i++) {
            // Create cone spread
            Vector needleDirection = direction.clone();
            double spread = 0.3;
            needleDirection.add(new Vector(
                    (random.nextDouble() - 0.5) * spread,
                    (random.nextDouble() - 0.5) * spread * 0.5,
                    (random.nextDouble() - 0.5) * spread
            )).normalize();

            // Slight delay for each needle
            final Vector finalDirection = needleDirection;
            final int delay = i * 2;

            new BukkitRunnable() {
                @Override
                public void run() {
                    shootPoisonNeedle(startLoc.clone(), finalDirection, player, plugin);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    /**
     * Helper method to shoot a poison needle
     */
    private void shootPoisonNeedle(Location startLoc, Vector direction, Player owner, Main plugin) {
        ArmorStand needle = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        needle.setVisible(false);
        needle.setGravity(false);
        needle.setInvulnerable(true);
        needle.setSmall(true);
        needle.getEquipment().setHelmet(new ItemStack(Material.CACTUS));

        temporaryEntities.add(needle);

        // Poison duration based on molting state
        int poisonDuration = moltingActive.getOrDefault(owner.getUniqueId(), false) ? 200 : 100;

        new BukkitRunnable() {
            private int ticks = 0;
            private boolean hasHit = false;

            @Override
            public void run() {
                ticks++;

                if (ticks > 40 || hasHit || !needle.isValid()) {
                    needle.remove();
                    this.cancel();
                    return;
                }

                // Move needle
                needle.teleport(needle.getLocation().add(direction.clone().multiply(0.8)));

                // Poison particle trail
                needle.getWorld().spawnParticle(Particle.ITEM_SLIME, needle.getLocation(),
                        2, 0.05, 0.05, 0.05, 0.01);

                // Check for collision with blocks
                Block block = needle.getLocation().getBlock();
                if (block.getType().isSolid()) {
                    hasHit = true;
                    createPoisonCloud(needle.getLocation(), owner, poisonDuration);
                    return;
                }

                // Check for collision with entities
                for (Entity entity : needle.getLocation().getWorld().getNearbyEntities(needle.getLocation(), 0.8, 0.8, 0.8)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != needle) {
                        LivingEntity target = (LivingEntity) entity;

                        target.damage(6.0, owner);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonDuration, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));

                        // Poison effect
                        target.getWorld().spawnParticle(Particle.ITEM_SLIME, target.getLocation().add(0, 1, 0),
                                20, 0.5, 1, 0.5, 0.1);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_HURT, 1.0F, 1.2F);

                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Helper method to create poison cloud on impact
     */
    private void createPoisonCloud(Location location, Player owner, int poisonDuration) {
        location.getWorld().spawnParticle(Particle.ITEM_SLIME, location, 30, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.BLOCK_SLIME_BLOCK_BREAK, 1.0F, 0.8F);

        // Poison nearby enemies
        for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (entity instanceof LivingEntity && entity != owner) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonDuration, 1));
                target.getWorld().spawnParticle(Particle.ITEM_SLIME, target.getLocation().add(0, 1, 0),
                        15, 0.3, 0.8, 0.3, 0.05);
            }
        }
    }

    /**
     * Vase Transportation - Teleport via vases
     */
    public void activateVaseTransportation(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (vaseTransportationCooldown.containsKey(playerId)) {
            long timeLeft = (vaseTransportationCooldown.get(playerId) + VASE_TRANSPORTATION_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Vase Transportation wieder einsetzen kannst!");
                return;
            }
        }

        // Check if player already has a vase location set
        if (vaseLocations.containsKey(playerId)) {
            // Teleport to existing vase
            Location vaseLoc = vaseLocations.get(playerId);

            // Teleport with effects
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0),
                    30, 0.5, 1, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

            player.teleport(vaseLoc);

            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0),
                    30, 0.5, 1, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.2F);

            player.sendMessage("§5§lVase Transportation§5 - Teleportiert!");

            // Clear vase location and set cooldown
            vaseLocations.remove(playerId);
            vaseTransportationCooldown.put(playerId, System.currentTimeMillis());

        } else {
            // Place vase at current location
            Location currentLoc = player.getLocation();
            vaseLocations.put(playerId, currentLoc.clone());

            // Create visual vase
            createVase(currentLoc, player, plugin);

            player.sendMessage("§5§lVase Transportation§5 - Vase platziert! Nutze es erneut zum Teleportieren.");
            player.playSound(currentLoc, Sound.BLOCK_STONE_PLACE, 1.0F, 0.8F);
        }
    }

    /**
     * Helper method to create a visual vase
     */
    private void createVase(Location location, Player owner, Main plugin) {
        ArmorStand vase = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        vase.setVisible(false);
        vase.setGravity(false);
        vase.setInvulnerable(true);
        vase.getEquipment().setHelmet(new ItemStack(Material.DECORATED_POT));

        temporaryEntities.add(vase);

        // Vase particles
        location.getWorld().spawnParticle(Particle.ENCHANT, location.add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);

        // Remove vase after 30 seconds if not used
        new BukkitRunnable() {
            @Override
            public void run() {
                if (vase.isValid()) {
                    vase.remove();
                    vaseLocations.remove(owner.getUniqueId());
                    owner.sendMessage("§6Deine Vase ist verschwunden.");
                }
            }
        }.runTaskLater(plugin, 600); // 30 seconds
    }

    /**
     * Fish Swarm - Cone AoE knockback
     */
    public void activateFishSwarm(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (fishSwarmCooldown.containsKey(playerId)) {
            long timeLeft = (fishSwarmCooldown.get(playerId) + FISH_SWARM_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Fish Swarm wieder einsetzen kannst!");
                return;
            }
        }

        fishSwarmCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§3§lFish Swarm§3 schwärmt aus!");
        player.playSound(player.getLocation(), Sound.ENTITY_COD_AMBIENT, 1.0F, 0.8F);

        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Create fish swarm effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 60; // 3 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    this.cancel();
                    return;
                }

                // Create fish particles in cone
                for (int i = 0; i < 8; i++) {
                    double spread = 0.8;
                    Vector fishDirection = direction.clone().add(new Vector(
                            (random.nextDouble() - 0.5) * spread,
                            (random.nextDouble() - 0.5) * spread * 0.5,
                            (random.nextDouble() - 0.5) * spread
                    )).normalize();

                    double distance = ticks * 0.3;
                    Location fishLoc = startLoc.clone().add(fishDirection.multiply(distance));

                    // Fish particles
                    fishLoc.getWorld().spawnParticle(Particle.SPLASH, fishLoc, 3, 0.2, 0.2, 0.2, 0.1);

                    // Every few ticks, check for enemies
                    if (ticks % 5 == 0) {
                        for (Entity entity : fishLoc.getWorld().getNearbyEntities(fishLoc, 1.5, 1.5, 1.5)) {
                            if (entity instanceof LivingEntity && entity != player) {
                                LivingEntity target = (LivingEntity) entity;

                                // Knockback
                                Vector knockback = fishDirection.clone().multiply(1.5);
                                knockback.setY(0.3);
                                target.setVelocity(target.getVelocity().add(knockback));

                                // Damage
                                target.damage(3.0, player);

                                // Fish attack effect
                                target.getWorld().spawnParticle(Particle.SPLASH,
                                        target.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.1);
                                target.getWorld().playSound(target.getLocation(),
                                        Sound.ENTITY_COD_FLOP, 1.0F, 1.2F);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Molting Transformation - Buffed state
     */
    public void activateMoltingTransformation(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (moltingTransformationCooldown.containsKey(playerId)) {
            long timeLeft = (moltingTransformationCooldown.get(playerId) + MOLTING_TRANSFORMATION_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Molting Transformation wieder einsetzen kannst!");
                return;
            }
        }

        moltingTransformationCooldown.put(playerId, System.currentTimeMillis());
        moltingActive.put(playerId, true);

        player.sendMessage("§4§l⚡ MOLTING TRANSFORMATION AKTIVIERT ⚡");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.5F);

        // Transformation effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 1)); // 30 seconds, Level 2
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // 30 seconds, Level 2
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 0)); // 30 seconds, Level 1

        // Visual transformation effect
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation().add(0, 1, 0), 3, 1, 1, 1, 0);
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation().add(0, 1, 0),
                50, 1, 2, 1, 0.2);

        // Ongoing visual effects
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 600; // 30 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > duration || !player.isOnline()) {
                    moltingActive.put(playerId, false);
                    player.sendMessage("§6Molting Transformation beendet.");
                    this.cancel();
                    return;
                }

                // Continuous particle effects
                if (ticks % 20 == 0) { // Every second
                    player.getWorld().spawnParticle(Particle.DRIPPING_WATER, player.getLocation().add(0, 2, 0),
                            10, 0.5, 0.5, 0.5, 0.02);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Enhanced attack for molting transformation
     */
    public void handleMoltingAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();

        if (!moltingActive.getOrDefault(playerId, false)) return;

        // Enhanced damage
        double damage = event.getDamage() * 1.5;
        event.setDamage(damage);

        // Water splash effect
        target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation().add(0, 1, 0),
                20, 0.5, 1, 0.5, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 1.0F, 1.2F);

        player.sendMessage("§4Molting Transformation verstärkt deinen Angriff!");
    }

    /**
     * Check if player is in molting transformation
     */
    public boolean isInMoltingTransformation(UUID playerId) {
        return moltingActive.getOrDefault(playerId, false);
    }

    /**
     * Cleanup method
     */
    public void cleanup() {
        // Remove temporary entities
        for (ArmorStand entity : temporaryEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        temporaryEntities.clear();
        fishMobs.clear();

        // Remove temporary water blocks
        for (Block block : temporaryWaterBlocks) {
            if (block != null && block.getType() == Material.WATER) {
                block.setType(Material.AIR);
            }
        }
        temporaryWaterBlocks.clear();

        // Clear all maps
        moltingActive.clear();
        vaseLocations.clear();
    }

    /**
     * Ensure location has finite values
     */
    private void ensureFiniteLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        double x = Double.isFinite(location.getX()) ? location.getX() : 0;
        double y = Double.isFinite(location.getY()) ? location.getY() : 0;
        double z = Double.isFinite(location.getZ()) ? location.getZ() : 0;

        float pitch = Float.isFinite(location.getPitch()) ? location.getPitch() : 0;
        float yaw = Float.isFinite(location.getYaw()) ? location.getYaw() : 0;

        location.setX(x);
        location.setY(y);
        location.setZ(z);
        location.setPitch(pitch);
        location.setYaw(yaw);
    }
}