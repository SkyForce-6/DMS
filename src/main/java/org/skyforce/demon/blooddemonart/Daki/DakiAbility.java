package org.skyforce.demon.blooddemonart.Daki;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

public class DakiAbility {

    private final Map<UUID, Long> obiCageCooldown = new HashMap<>();
    private final Map<UUID, Long> obiAttackCooldown = new HashMap<>();
    private final Map<UUID, Long> obiShieldCooldown = new HashMap<>();
    private final Map<UUID, Long> obiGridCooldown = new HashMap<>();
    private final Map<UUID, Long> obiPunchCooldown = new HashMap<>();

    // Obi Shield tracking
    private final Map<UUID, Boolean> obiShieldActive = new HashMap<>();
    private final Map<UUID, BukkitRunnable> obiShieldTasks = new HashMap<>();

    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long OBI_CAGE_COOLDOWN = 20;
    private static final long OBI_ATTACK_COOLDOWN = 15;
    private static final long OBI_SHIELD_COOLDOWN = 25;
    private static final long OBI_GRID_COOLDOWN = 18;
    private static final long OBI_PUNCH_COOLDOWN = 12;

    /**
     * Obi Cage - Stun target for 5 seconds
     */
    public void activateObiCage(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (obiCageCooldown.containsKey(playerId)) {
            long timeLeft = (obiCageCooldown.get(playerId) + OBI_CAGE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Obi Cage wieder einsetzen kannst!");
                return;
            }
        }

        obiCageCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§d§lObi Cage§d wird erschaffen!");
        player.playSound(player.getLocation(), Sound.BLOCK_WOOL_PLACE, 1.0F, 0.8F);

        Location targetLoc = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);

        // Find target near the location
        LivingEntity target = null;
        double minDistance = Double.MAX_VALUE;
        for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, 3, 3, 3)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = entity.getLocation().distance(targetLoc);
                if (distance < minDistance) {
                    minDistance = distance;
                    target = (LivingEntity) entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage("§cKein Ziel für Obi Cage gefunden!");
            return;
        }

        createObiCage(target, player, plugin);
    }

    /**
     * Create an obi cage around the target
     */
    private void createObiCage(LivingEntity target, Player owner, Main plugin) {
        Location cageCenter = target.getLocation();
        List<ArmorStand> cageObi = new ArrayList<>();

        // Create obi cage structure (circular pattern)
        for (int angle = 0; angle < 360; angle += 45) {
            double x = Math.cos(Math.toRadians(angle)) * 2;
            double z = Math.sin(Math.toRadians(angle)) * 2;

            for (int y = 0; y <= 3; y++) {
                Location obiLoc = cageCenter.clone().add(x, y, z);
                ArmorStand obi = (ArmorStand) obiLoc.getWorld().spawnEntity(obiLoc, EntityType.ARMOR_STAND);
                obi.setVisible(false);
                obi.setGravity(false);
                obi.setInvulnerable(true);
                obi.setSmall(true);
                obi.getEquipment().setHelmet(new ItemStack(Material.MAGENTA_WOOL));

                cageObi.add(obi);
                temporaryEntities.add(obi);
            }
        }

        // Apply stun effects
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 10)); // 5 seconds, max slowness
        target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 3)); // Can't break blocks
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 128)); // Can't jump

        // Visual effects
        cageCenter.getWorld().spawnParticle(Particle.BLOCK, cageCenter.add(0, 1, 0),
                50, 2, 2, 2, 0.1, Material.MAGENTA_WOOL.createBlockData());
        cageCenter.getWorld().playSound(cageCenter, Sound.BLOCK_WOOL_PLACE, 1.0F, 1.0F);

        owner.sendMessage("§d" + target.getName() + " wurde in einem Obi-Käfig gefangen!");

        // Remove cage after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ArmorStand obi : cageObi) {
                    if (obi.isValid()) {
                        obi.getWorld().spawnParticle(Particle.BLOCK, obi.getLocation(),
                                5, 0.2, 0.2, 0.2, 0.05, Material.MAGENTA_WOOL.createBlockData());
                        obi.remove();
                    }
                }
                cageObi.clear();
                temporaryEntities.removeAll(cageObi);
            }
        }.runTaskLater(plugin, 100); // 5 seconds
    }

    /**
     * Obi Attack - 5x hit knockback combo
     */
    public void activateObiAttack(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (obiAttackCooldown.containsKey(playerId)) {
            long timeLeft = (obiAttackCooldown.get(playerId) + OBI_ATTACK_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Obi Attack wieder einsetzen kannst!");
                return;
            }
        }

        obiAttackCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§d§lObi Attack§d - 5-fach Kombo!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);

        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Execute 5 consecutive obi strikes
        for (int i = 0; i < 5; i++) {
            final int hitNumber = i + 1;
            new BukkitRunnable() {
                @Override
                public void run() {
                    executeObiStrike(startLoc, direction, hitNumber, player, plugin);
                }
            }.runTaskLater(plugin, i * 8); // 0.4 second delay between hits
        }
    }

    /**
     * Execute individual obi strike
     */
    private void executeObiStrike(Location startLoc, Vector direction, int hitNumber, Player owner, Main plugin) {
        // Create obi projectile
        ArmorStand obiStrike = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        obiStrike.setVisible(false);
        obiStrike.setGravity(false);
        obiStrike.setInvulnerable(true);
        obiStrike.setSmall(true);
        obiStrike.getEquipment().setHelmet(new ItemStack(Material.MAGENTA_BANNER));

        temporaryEntities.add(obiStrike);

        // Sound effect for each strike
        startLoc.getWorld().playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F + (hitNumber * 0.1F));

        new BukkitRunnable() {
            private int ticks = 0;
            private boolean hasHit = false;

            @Override
            public void run() {
                ticks++;

                if (ticks > 30 || hasHit || !obiStrike.isValid()) {
                    obiStrike.remove();
                    this.cancel();
                    return;
                }

                // Move obi forward
                obiStrike.teleport(obiStrike.getLocation().add(direction.clone().multiply(0.8)));

                // Obi trail particles
                obiStrike.getWorld().spawnParticle(Particle.BLOCK, obiStrike.getLocation(),
                        3, 0.1, 0.1, 0.1, 0.02, Material.MAGENTA_WOOL.createBlockData());

                // Check for enemy collision
                for (Entity entity : obiStrike.getLocation().getWorld().getNearbyEntities(obiStrike.getLocation(), 1, 1, 1)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != obiStrike) {
                        LivingEntity target = (LivingEntity) entity;

                        // Damage increases with each hit
                        double damage = 4.0 + (hitNumber * 1.0);
                        target.damage(damage, owner);

                        // Knockback increases with each hit
                        Vector knockback = direction.clone().multiply(0.5 + (hitNumber * 0.2));
                        knockback.setY(0.3 + (hitNumber * 0.1));
                        target.setVelocity(target.getVelocity().add(knockback));

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                15, 0.5, 1, 0.5, 0.1, Material.MAGENTA_WOOL.createBlockData());
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 1.2F);

                        owner.sendMessage("§dObi Treffer " + hitNumber + "/5!");
                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Obi Shield - Block damage for 5 seconds
     */
    public void activateObiShield(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (obiShieldCooldown.containsKey(playerId)) {
            long timeLeft = (obiShieldCooldown.get(playerId) + OBI_SHIELD_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Obi Shield wieder einsetzen kannst!");
                return;
            }
        }

        if (obiShieldActive.getOrDefault(playerId, false)) {
            player.sendMessage("§cObi Shield ist bereits aktiv!");
            return;
        }

        obiShieldCooldown.put(playerId, System.currentTimeMillis());
        obiShieldActive.put(playerId, true);

        player.sendMessage("§d§lObi Shield§d aktiviert!");
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F);

        // Create visual obi shield around player
        List<ArmorStand> shieldObi = new ArrayList<>();
        for (int angle = 0; angle < 360; angle += 30) {
            double x = Math.cos(Math.toRadians(angle)) * 2.5;
            double z = Math.sin(Math.toRadians(angle)) * 2.5;

            Location obiLoc = player.getLocation().add(x, 1, z);
            ArmorStand obi = (ArmorStand) obiLoc.getWorld().spawnEntity(obiLoc, EntityType.ARMOR_STAND);
            obi.setVisible(false);
            obi.setGravity(false);
            obi.setInvulnerable(true);
            obi.setSmall(true);
            obi.getEquipment().setHelmet(new ItemStack(Material.MAGENTA_CONCRETE));

            shieldObi.add(obi);
            temporaryEntities.add(obi);
        }

        // Shield visual effects
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0, 1, 0),
                30, 2, 1, 2, 0.1, Material.MAGENTA_WOOL.createBlockData());

        // Shield duration task
        BukkitRunnable shieldTask = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > 100 || !player.isOnline()) { // 5 seconds
                    // Deactivate shield
                    obiShieldActive.put(playerId, false);
                    obiShieldTasks.remove(playerId);

                    // Remove shield obi
                    for (ArmorStand obi : shieldObi) {
                        if (obi.isValid()) {
                            obi.remove();
                        }
                    }
                    shieldObi.clear();
                    temporaryEntities.removeAll(shieldObi);

                    if (player.isOnline()) {
                        player.sendMessage("§6Obi Shield deaktiviert.");
                    }
                    this.cancel();
                    return;
                }

                // Rotating shield effect
                if (ticks % 10 == 0) {
                    player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0, 1, 0),
                            10, 2, 1, 2, 0.05, Material.MAGENTA_WOOL.createBlockData());
                }

                // Rotate shield obi
                for (int i = 0; i < shieldObi.size(); i++) {
                    ArmorStand obi = shieldObi.get(i);
                    if (obi.isValid()) {
                        double angle = (360.0 / shieldObi.size()) * i + (ticks * 5);
                        double x = Math.cos(Math.toRadians(angle)) * 2.5;
                        double z = Math.sin(Math.toRadians(angle)) * 2.5;

                        Location newLoc = player.getLocation().add(x, 1, z);
                        obi.teleport(newLoc);
                    }
                }
            }
        };

        shieldTask.runTaskTimer(plugin, 0, 1);
        obiShieldTasks.put(playerId, shieldTask);
    }

    /**
     * Handle damage absorption for Obi Shield
     */
    public void handleObiShieldDamage(Player player, EntityDamageEvent event) {
        UUID playerId = player.getUniqueId();

        if (!obiShieldActive.getOrDefault(playerId, false)) return;

        // Block all damage
        event.setCancelled(true);

        // Shield block effect
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0, 1, 0),
                20, 1, 1, 1, 0.1, Material.MAGENTA_WOOL.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.2F);

        player.sendMessage("§dObi Shield hat den Schaden blockiert!");
    }

    /**
     * Obi Grid - Overhead slam attack
     */
    public void activateObiGrid(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (obiGridCooldown.containsKey(playerId)) {
            long timeLeft = (obiGridCooldown.get(playerId) + OBI_GRID_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Obi Grid wieder einsetzen kannst!");
                return;
            }
        }

        obiGridCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§d§lObi Grid§d wird entfesselt!");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F);

        Location targetLoc = player.getTargetBlock(null, 25).getLocation().add(0, 10, 0);

        // Create grid pattern of obi attacks from above
        createObiGrid(targetLoc, player, plugin);
    }

    /**
     * Create overhead obi grid slam
     */
    private void createObiGrid(Location centerLoc, Player owner, Main plugin) {
        // Create 5x5 grid pattern
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location slamLoc = centerLoc.clone().add(x * 3, 0, z * 3);

                // Delay each slam slightly for visual effect
                final Location finalSlamLoc = slamLoc;
                int delay = (Math.abs(x) + Math.abs(z)) * 5;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        executeObiSlam(finalSlamLoc, owner, plugin);
                    }
                }.runTaskLater(plugin, delay);
            }
        }
    }

    /**
     * Execute individual obi slam
     */
    private void executeObiSlam(Location slamLoc, Player owner, Main plugin) {
        // Create obi projectile falling from above
        ArmorStand obiSlam = (ArmorStand) slamLoc.getWorld().spawnEntity(slamLoc, EntityType.ARMOR_STAND);
        obiSlam.setVisible(false);
        obiSlam.setGravity(false);
        obiSlam.setInvulnerable(true);
        obiSlam.getEquipment().setHelmet(new ItemStack(Material.MAGENTA_CONCRETE));

        temporaryEntities.add(obiSlam);

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > 40 || !obiSlam.isValid()) {
                    obiSlam.remove();
                    this.cancel();
                    return;
                }

                // Move downward
                obiSlam.teleport(obiSlam.getLocation().add(0, -0.5, 0));

                // Trail particles
                obiSlam.getWorld().spawnParticle(Particle.BLOCK, obiSlam.getLocation(),
                        5, 0.2, 0.2, 0.2, 0.05, Material.MAGENTA_WOOL.createBlockData());

                // Check if hit ground or enemy
                if (obiSlam.getLocation().getY() <= slamLoc.getY() - 10 ||
                        obiSlam.getLocation().getBlock().getType().isSolid()) {

                    // Impact effect
                    Location impactLoc = obiSlam.getLocation();
                    impactLoc.getWorld().spawnParticle(Particle.EXPLOSION, impactLoc, 1, 0, 0, 0, 0);
                    impactLoc.getWorld().spawnParticle(Particle.BLOCK, impactLoc,
                            30, 2, 1, 2, 0.1, Material.MAGENTA_WOOL.createBlockData());
                    impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);

                    // Damage nearby enemies
                    for (Entity entity : impactLoc.getWorld().getNearbyEntities(impactLoc, 3, 3, 3)) {
                        if (entity instanceof LivingEntity && entity != owner) {
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(10.0, owner);

                            // Slam knockdown effect
                            target.setVelocity(new Vector(0, -1, 0));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

                            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                    20, 0.5, 1, 0.5, 0.1, Material.MAGENTA_WOOL.createBlockData());
                        }
                    }

                    obiSlam.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Obi Punch - Strong single hit
     */
    public void activateObiPunch(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (obiPunchCooldown.containsKey(playerId)) {
            long timeLeft = (obiPunchCooldown.get(playerId) + OBI_PUNCH_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Obi Punch wieder einsetzen kannst!");
                return;
            }
        }

        obiPunchCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§d§lObi Punch§d - Kraftschlag!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 0.8F);

        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Create massive obi fist
        ArmorStand obiPunch = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        obiPunch.setVisible(false);
        obiPunch.setGravity(false);
        obiPunch.setInvulnerable(true);
        obiPunch.getEquipment().setHelmet(new ItemStack(Material.MAGENTA_TERRACOTTA));

        temporaryEntities.add(obiPunch);

        // Charging effect
        startLoc.getWorld().spawnParticle(Particle.BLOCK, startLoc,
                50, 1, 1, 1, 0.1, Material.MAGENTA_WOOL.createBlockData());

        new BukkitRunnable() {
            private int ticks = 0;
            private boolean hasHit = false;

            @Override
            public void run() {
                ticks++;

                if (ticks > 20 || hasHit || !obiPunch.isValid()) {
                    obiPunch.remove();
                    this.cancel();
                    return;
                }

                // Move punch forward rapidly
                obiPunch.teleport(obiPunch.getLocation().add(direction.clone().multiply(1.5)));

                // Powerful trail particles
                obiPunch.getWorld().spawnParticle(Particle.BLOCK, obiPunch.getLocation(),
                        10, 0.3, 0.3, 0.3, 0.1, Material.MAGENTA_WOOL.createBlockData());
                obiPunch.getWorld().spawnParticle(Particle.CRIT, obiPunch.getLocation(), 5, 0.2, 0.2, 0.2, 0.1);

                // Check for collision
                for (Entity entity : obiPunch.getLocation().getWorld().getNearbyEntities(obiPunch.getLocation(), 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && entity != obiPunch) {
                        LivingEntity target = (LivingEntity) entity;

                        // Massive damage
                        target.damage(15.0, player);

                        // Strong knockback
                        Vector knockback = direction.clone().multiply(2.5);
                        knockback.setY(1.0);
                        target.setVelocity(knockback);

                        // Stun effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 1));

                        // Massive impact effect
                        target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 2, 0.5, 0.5, 0.5, 0);
                        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                50, 1, 2, 1, 0.2, Material.MAGENTA_WOOL.createBlockData());
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);

                        player.sendMessage("§dObi Punch trifft " + target.getName() + " mit voller Kraft!");
                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Check if player has Obi Shield active
     */
    public boolean hasObiShieldActive(UUID playerId) {
        return obiShieldActive.getOrDefault(playerId, false);
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

        // Cancel shield tasks
        for (BukkitRunnable task : obiShieldTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        obiShieldTasks.clear();

        // Clear all maps
        obiShieldActive.clear();
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