package org.skyforce.demon.blooddemonart.Zohakuten;

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

public class ZohakutenAbility {

    private final Map<UUID, Long> woodDragonsCooldown = new HashMap<>();
    private final Map<UUID, Long> woodDragonShockwaveCooldown = new HashMap<>();
    private final Map<UUID, Long> lightningWaveCooldown = new HashMap<>();
    private final Map<UUID, Long> rageModeToggleCooldown = new HashMap<>();
    private final Map<UUID, Long> fearCooldown = new HashMap<>();
    private final Map<UUID, Long> angerCooldown = new HashMap<>();
    private final Map<UUID, Long> pleasureCooldown = new HashMap<>();
    private final Map<UUID, Long> sorrowCooldown = new HashMap<>();
    private final Map<UUID, Long> joyCooldown = new HashMap<>();
    private final Map<UUID, Long> hatredCooldown = new HashMap<>();

    // Rage Mode tracking
    private final Map<UUID, Boolean> rageModeActive = new HashMap<>();
    private final Map<UUID, String> currentEmotion = new HashMap<>();

    private final List<Block> temporaryWoodBlocks = new ArrayList<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long WOOD_DRAGONS_COOLDOWN = 25;
    private static final long WOOD_DRAGON_SHOCKWAVE_COOLDOWN = 30;
    private static final long LIGHTNING_WAVE_COOLDOWN = 20;
    private static final long RAGE_MODE_TOGGLE_COOLDOWN = 60;
    private static final long FEAR_COOLDOWN = 15;
    private static final long ANGER_COOLDOWN = 20;
    private static final long PLEASURE_COOLDOWN = 18;
    private static final long SORROW_COOLDOWN = 25;
    private static final long JOY_COOLDOWN = 45;
    private static final long HATRED_COOLDOWN = 35;

    /**
     * Wood Dragons - Creates 3 ground slams with wooden dragon attacks
     */
    public void activateWoodDragons(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (woodDragonsCooldown.containsKey(playerId)) {
            long timeLeft = (woodDragonsCooldown.get(playerId) + WOOD_DRAGONS_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Wood Dragons wieder einsetzen kannst!");
                return;
            }
        }

        woodDragonsCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§6§lWood Dragons§6 werden beschworen!");
        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0F, 0.5F);

        Location playerLoc = player.getLocation();

        // Create 3 ground slams with delay
        for (int i = 0; i < 3; i++) {
            final int slamIndex = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    createWoodDragonSlam(playerLoc, slamIndex, player, plugin);
                }
            }.runTaskLater(plugin, i * 20); // 1 second delay between slams
        }
    }

    /**
     * Helper method to create a wood dragon ground slam
     */
    private void createWoodDragonSlam(Location center, int slamIndex, Player owner, Main plugin) {
        // Calculate slam location based on index
        Location slamLoc = center.clone().add(
                (slamIndex - 1) * 3, // X offset
                0,
                random.nextDouble() * 6 - 3 // Random Z offset
        );

        // Ground slam effect
        slamLoc.getWorld().playSound(slamLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F);
        slamLoc.getWorld().spawnParticle(Particle.EXPLOSION, slamLoc.clone().add(0, 1, 0), 1, 0, 0, 0, 0);

        // Create wooden dragon structure
        createWoodenDragonStructure(slamLoc, owner, plugin);

        // Damage nearby enemies
        for (Entity entity : slamLoc.getWorld().getNearbyEntities(slamLoc, 4, 4, 4)) {
            if (entity instanceof LivingEntity && entity != owner) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(8.0, owner);
                target.setVelocity(target.getVelocity().add(new Vector(0, 0.5, 0)));

                // Wood particle effect
                target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                        20, 0.5, 1, 0.5, 0.1, Material.OAK_WOOD.createBlockData());
            }
        }
    }

    /**
     * Helper method to create wooden dragon structure
     */
    private void createWoodenDragonStructure(Location center, Player owner, Main plugin) {
        // Create dragon body pattern
        int[][] dragonPattern = {
                {0, 0}, {1, 0}, {2, 0}, {3, 0}, // Body
                {0, 1}, {1, 1}, // Head
                {-1, 0}, {-2, 0}, // Tail
                {1, -1}, {1, 1}, {2, -1}, {2, 1} // Wings
        };

        List<Block> dragonBlocks = new ArrayList<>();

        for (int[] offset : dragonPattern) {
            Block block = center.clone().add(offset[0], 1, offset[1]).getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.OAK_WOOD);
                dragonBlocks.add(block);
                temporaryWoodBlocks.add(block);

                // Wood particle effect
                block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5),
                        5, 0.3, 0.3, 0.3, 0.02, Material.OAK_WOOD.createBlockData());
            }
        }

        // Remove wood blocks after 8 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : dragonBlocks) {
                    if (block.getType() == Material.OAK_WOOD) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5),
                                8, 0.3, 0.3, 0.3, 0.05, Material.OAK_WOOD.createBlockData());
                    }
                }
                dragonBlocks.clear();
                temporaryWoodBlocks.removeAll(dragonBlocks);
            }
        }.runTaskLater(plugin, 160); // 8 seconds
    }

    /**
     * Wood Dragon Shockwave - Creates a stunning shockwave
     */
    public void activateWoodDragonShockwave(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (woodDragonShockwaveCooldown.containsKey(playerId)) {
            long timeLeft = (woodDragonShockwaveCooldown.get(playerId) + WOOD_DRAGON_SHOCKWAVE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Wood Dragon Shockwave wieder einsetzen kannst!");
                return;
            }
        }

        woodDragonShockwaveCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§6§lWood Dragon Shockwave§6 wird entfesselt!");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.5F);

        Location playerLoc = player.getLocation();

        // Create expanding shockwave effect
        new BukkitRunnable() {
            private int radius = 1;
            private final int maxRadius = 10;

            @Override
            public void run() {
                if (radius > maxRadius) {
                    this.cancel();
                    return;
                }

                // Create shockwave ring
                createShockwaveRing(playerLoc, radius, player);
                radius++;
            }
        }.runTaskTimer(plugin, 0, 3); // Every 3 ticks
    }

    /**
     * Helper method to create shockwave ring
     */
    private void createShockwaveRing(Location center, int radius, Player owner) {
        for (int angle = 0; angle < 360; angle += 15) {
            double x = Math.cos(Math.toRadians(angle)) * radius;
            double z = Math.sin(Math.toRadians(angle)) * radius;
            Location particleLoc = center.clone().add(x, 0.5, z);

            // Wood and explosion particles
            center.getWorld().spawnParticle(Particle.BLOCK, particleLoc,
                    5, 0.2, 0.2, 0.2, 0.1, Material.OAK_WOOD.createBlockData());
            center.getWorld().spawnParticle(Particle.EXPLOSION, particleLoc, 1, 0, 0, 0, 0);
        }

        // Stun enemies in ring
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 3, radius)) {
            double distance = entity.getLocation().distance(center);
            if (Math.abs(distance - radius) <= 2 && entity instanceof LivingEntity && entity != owner) {
                LivingEntity target = (LivingEntity) entity;

                // Stun effect (slowness + mining fatigue)
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 4)); // 3 seconds, Level 5
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 2)); // 3 seconds, Level 3
                target.damage(4.0, owner);

                target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                        15, 0.5, 1, 0.5, 0.1, Material.OAK_WOOD.createBlockData());
            }
        }
    }

    /**
     * Lightning Wave - Burst AoE lightning attack
     */
    public void activateLightningWave(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (lightningWaveCooldown.containsKey(playerId)) {
            long timeLeft = (lightningWaveCooldown.get(playerId) + LIGHTNING_WAVE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Lightning Wave wieder einsetzen kannst!");
                return;
            }
        }

        lightningWaveCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§e§lLightning Wave§e explodiert!");
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.2F);

        Location playerLoc = player.getLocation();

        // Lightning burst effect
        playerLoc.getWorld().spawnParticle(Particle.FLASH, playerLoc.clone().add(0, 1, 0), 3, 1, 1, 1, 0);
        playerLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, playerLoc.clone().add(0, 1, 0), 50, 2, 1, 2, 0.1);

        // Create multiple lightning strikes
        for (int i = 0; i < 8; i++) {
            double x = random.nextDouble() * 12 - 6;
            double z = random.nextDouble() * 12 - 6;
            Location lightningLoc = playerLoc.clone().add(x, 0, z);

            // Delayed lightning strikes
            final Location strikeLoc = lightningLoc;
            new BukkitRunnable() {
                @Override
                public void run() {
                    createLightningStrike(strikeLoc, player);
                }
            }.runTaskLater(plugin, i * 2);
        }
    }

    /**
     * Helper method to create lightning strike
     */
    private void createLightningStrike(Location location, Player owner) {
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location.clone().add(0, 5, 0), 30, 0.5, 5, 0.5, 0.2);
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8F, 1.0F);

        // Damage enemies near lightning
        for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 5, 2)) {
            if (entity instanceof LivingEntity && entity != owner) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(10.0, owner);

                // Lightning effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0),
                        20, 0.5, 1, 0.5, 0.1);
            }
        }
    }

    /**
     * Toggle Rage Mode
     */
    public void toggleRageMode(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (rageModeToggleCooldown.containsKey(playerId)) {
            long timeLeft = (rageModeToggleCooldown.get(playerId) + RAGE_MODE_TOGGLE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Rage Mode wieder umschalten kannst!");
                return;
            }
        }

        boolean isActive = rageModeActive.getOrDefault(playerId, false);

        if (!isActive) {
            // Activate Rage Mode
            rageModeActive.put(playerId, true);
            rageModeToggleCooldown.put(playerId, System.currentTimeMillis());

            player.sendMessage("§4§l⚡ RAGE MODE AKTIVIERT ⚡");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.5F);

            // Visual effect
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 2, 0), 20, 1, 1, 1, 0.1);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.05);

            // Auto-deactivate after 30 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (rageModeActive.getOrDefault(playerId, false)) {
                        rageModeActive.put(playerId, false);
                        currentEmotion.remove(playerId);
                        player.sendMessage("§6Rage Mode deaktiviert.");
                    }
                }
            }.runTaskLater(plugin, 600); // 30 seconds

        } else {
            // Deactivate Rage Mode
            rageModeActive.put(playerId, false);
            currentEmotion.remove(playerId);
            player.sendMessage("§6Rage Mode deaktiviert.");
        }
    }

    /**
     * Fear Emotion - Speed buff
     */
    public void activateFear(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (fearCooldown.containsKey(playerId)) {
            long timeLeft = (fearCooldown.get(playerId) + FEAR_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Fear wieder einsetzen kannst!");
                return;
            }
        }

        fearCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Fear");

        player.sendMessage("§5§lFEAR§5 - Geschwindigkeit erhöht!");
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.0F, 1.5F);

        // Speed buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2)); // 10 seconds, Level 3

        // Visual effect
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
    }

    /**
     * Anger Emotion - Lightning M1 attacks
     */
    public void activateAnger(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (angerCooldown.containsKey(playerId)) {
            long timeLeft = (angerCooldown.get(playerId) + ANGER_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Anger wieder einsetzen kannst!");
                return;
            }
        }

        angerCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Anger");

        player.sendMessage("§c§lANGER§c - Blitzangriffe aktiviert!");
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 1.5F);

        // Visual effect
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
    }

    /**
     * Pleasure Emotion - Knockback Uchiwa fan
     */
    public void activatePleasure(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (pleasureCooldown.containsKey(playerId)) {
            long timeLeft = (pleasureCooldown.get(playerId) + PLEASURE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Pleasure wieder einsetzen kannst!");
                return;
            }
        }

        pleasureCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Pleasure");

        player.sendMessage("§d§lPLEASURE§d - Uchiwa Fan aktiviert!");
        player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_SAPLING_BREAK, 1.0F, 0.8F);

        // Create fan wind effect
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        for (int i = 1; i <= 10; i++) {
            Location windLoc = startLoc.clone().add(direction.clone().multiply(i));
            windLoc.getWorld().spawnParticle(Particle.CLOUD, windLoc, 5, 0.5, 0.5, 0.5, 0.1);

            // Knockback enemies
            for (Entity entity : windLoc.getWorld().getNearbyEntities(windLoc, 1.5, 1.5, 1.5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;
                    Vector knockback = direction.clone().multiply(2);
                    knockback.setY(0.5);
                    target.setVelocity(knockback);
                    target.damage(5.0, player);
                }
            }
        }
    }

    /**
     * Sorrow Emotion - Strength buff
     */
    public void activateSorrow(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (sorrowCooldown.containsKey(playerId)) {
            long timeLeft = (sorrowCooldown.get(playerId) + SORROW_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Sorrow wieder einsetzen kannst!");
                return;
            }
        }

        sorrowCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Sorrow");

        player.sendMessage("§9§lSORROW§9 - Stärke erhöht!");
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0F, 0.5F);

        // Strength buff
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2)); // 15 seconds, Level 3

        // Visual effect
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
    }

    /**
     * Joy Emotion - Flight
     */
    public void activateJoy(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (joyCooldown.containsKey(playerId)) {
            long timeLeft = (joyCooldown.get(playerId) + JOY_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Joy wieder einsetzen kannst!");
                return;
            }
        }

        joyCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Joy");

        player.sendMessage("§a§lJOY§a - Flug aktiviert!");
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0F, 1.2F);

        // Grant flight
        player.setAllowFlight(true);
        player.setFlying(true);

        // Visual effect
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);

        // Remove flight after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage("§6Flug deaktiviert.");
                }
            }
        }.runTaskLater(plugin, 200); // 10 seconds
    }

    /**
     * Hatred Emotion - Wood dragon summon support
     */
    public void activateHatred(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cDu musst im Rage Mode sein, um Emotionen zu nutzen!");
            return;
        }

        if (hatredCooldown.containsKey(playerId)) {
            long timeLeft = (hatredCooldown.get(playerId) + HATRED_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Hatred wieder einsetzen kannst!");
                return;
            }
        }

        hatredCooldown.put(playerId, System.currentTimeMillis());
        currentEmotion.put(playerId, "Hatred");

        player.sendMessage("§4§lHATRED§4 - Holzdrache beschworen!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.8F);

        // Summon support wood dragon
        summonSupportWoodDragon(player, plugin);
    }

    /**
     * Helper method to summon support wood dragon
     */
    private void summonSupportWoodDragon(Player owner, Main plugin) {
        Location summonLoc = owner.getLocation().add(2, 1, 2);

        // Create dragon entity
        ArmorStand dragon = (ArmorStand) summonLoc.getWorld().spawnEntity(summonLoc, EntityType.ARMOR_STAND);
        dragon.setVisible(false);
        dragon.setGravity(false);
        dragon.setInvulnerable(true);
        dragon.getEquipment().setHelmet(new ItemStack(Material.OAK_LOG));

        temporaryEntities.add(dragon);

        // Visual effect
        summonLoc.getWorld().spawnParticle(Particle.BLOCK, summonLoc, 30, 1, 1, 1, 0.1, Material.OAK_WOOD.createBlockData());

        // Dragon AI
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 300; // 15 seconds

            @Override
            public void run() {
                ticks++;

                if (ticks > duration || !dragon.isValid() || !owner.isOnline()) {
                    dragon.remove();
                    this.cancel();
                    return;
                }

                // Find nearest enemy
                LivingEntity target = null;
                double minDistance = Double.MAX_VALUE;

                for (Entity entity : dragon.getLocation().getWorld().getNearbyEntities(dragon.getLocation(), 15, 15, 15)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != dragon) {
                        double distance = entity.getLocation().distance(dragon.getLocation());
                        if (distance < minDistance) {
                            minDistance = distance;
                            target = (LivingEntity) entity;
                        }
                    }
                }

                if (target != null) {
                    // Move towards target
                    Vector direction = target.getLocation().toVector().subtract(dragon.getLocation().toVector()).normalize().multiply(0.3);
                    Location newLoc = dragon.getLocation().add(direction);
                    ensureFiniteLocation(newLoc);
                    dragon.teleport(newLoc);

                    // Attack if close
                    if (dragon.getLocation().distance(target.getLocation()) < 2) {
                        target.damage(6.0, owner);
                        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                15, 0.5, 1, 0.5, 0.1, Material.OAK_WOOD.createBlockData());
                    }

                    // Particle trail
                    dragon.getWorld().spawnParticle(Particle.BLOCK, dragon.getLocation(),
                            5, 0.2, 0.2, 0.2, 0.02, Material.OAK_WOOD.createBlockData());
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Handle lightning-enhanced attacks for Anger emotion
     */
    public void handleAngerAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();

        if (!rageModeActive.getOrDefault(playerId, false)) return;
        if (!"Anger".equals(currentEmotion.get(playerId))) return;

        // Enhance damage with lightning
        double damage = event.getDamage() * 1.5;
        event.setDamage(damage);

        // Lightning effect
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0),
                15, 0.3, 0.8, 0.3, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.5F);

        // Chain lightning to nearby enemies
        for (Entity entity : target.getLocation().getWorld().getNearbyEntities(target.getLocation(), 3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player && entity != target) {
                LivingEntity chainTarget = (LivingEntity) entity;
                chainTarget.damage(damage * 0.3, player);
                chainTarget.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, chainTarget.getLocation().add(0, 1, 0),
                        10, 0.2, 0.5, 0.2, 0.05);
            }
        }
    }

    /**
     * Check if player is in rage mode
     */
    public boolean isInRageMode(UUID playerId) {
        return rageModeActive.getOrDefault(playerId, false);
    }

    /**
     * Get current emotion
     */
    public String getCurrentEmotion(UUID playerId) {
        return currentEmotion.get(playerId);
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

        // Remove temporary wood blocks
        for (Block block : temporaryWoodBlocks) {
            if (block != null && (block.getType() == Material.OAK_WOOD || block.getType() == Material.OAK_LOG)) {
                block.setType(Material.AIR);
            }
        }
        temporaryWoodBlocks.clear();

        // Clear all maps
        rageModeActive.clear();
        currentEmotion.clear();
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