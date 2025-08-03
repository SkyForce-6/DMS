package org.skyforce.demon.blooddemonart.Gyutaro;

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

public class GyutaroAbility {

    private final Map<UUID, Long> bloodSicklesCooldown = new HashMap<>();
    private final Map<UUID, Long> flyingBloodSicklesCooldown = new HashMap<>();
    private final Map<UUID, Long> vengefulSlicesCooldown = new HashMap<>();
    private final Map<UUID, Long> summonUmeCooldown = new HashMap<>();
    private final Map<UUID, Long> bloodTornadoCooldown = new HashMap<>();

    private final Map<UUID, Integer> bloodSicklesCombo = new HashMap<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long BLOOD_SICKLES_COOLDOWN = 8;
    private static final long FLYING_BLOOD_SICKLES_COOLDOWN = 15;
    private static final long VENGEFUL_SLICES_COOLDOWN = 20;
    private static final long SUMMON_UME_COOLDOWN = 25;
    private static final long BLOOD_TORNADO_COOLDOWN = 30;

    // Poison constants
    private static final int POISON_DURATION = 200; // 10 seconds
    private static final int POISON_AMPLIFIER = 2; // Level 3 (high damage)

    /**
     * Blood Sickles - Basic Slash Combo (3-hit combo)
     */
    public void activateBloodSickles(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (bloodSicklesCooldown.containsKey(playerId)) {
            long timeLeft = (bloodSicklesCooldown.get(playerId) + BLOOD_SICKLES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Blood Sickles wieder einsetzen kannst!");
                return;
            }
        }

        // Get current combo count
        int comboCount = bloodSicklesCombo.getOrDefault(playerId, 0) + 1;
        bloodSicklesCombo.put(playerId, comboCount);

        if (comboCount >= 3) {
            // Reset combo and set cooldown
            bloodSicklesCombo.put(playerId, 0);
            bloodSicklesCooldown.put(playerId, System.currentTimeMillis());
        }

        player.sendMessage("§4§lBlood Sickles§4 - Combo " + comboCount + "/3!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F + (comboCount * 0.2F));

        // Execute slash based on combo count
        executeBloodSickleSlash(player, comboCount, plugin);

        // Reset combo after 3 seconds if not continued
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bloodSicklesCombo.getOrDefault(playerId, 0) == comboCount) {
                    bloodSicklesCombo.put(playerId, 0);
                }
            }
        }.runTaskLater(plugin, 60); // 3 seconds
    }

    /**
     * Execute individual slash attack
     */
    private void executeBloodSickleSlash(Player player, int comboCount, Main plugin) {
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Different patterns for each combo hit
        double[] angles = {-30, 0, 30}; // Left slash, center slash, right slash
        double baseAngle = angles[comboCount - 1];

        for (int i = 0; i < 5; i++) {
            double angle = baseAngle + (i - 2) * 15; // Spread pattern
            Vector slashDirection = rotateVector(direction, angle);
            Location slashLoc = startLoc.clone().add(slashDirection.multiply(2 + i * 0.5));

            // Blood particle effect
            slashLoc.getWorld().spawnParticle(Particle.BLOCK, slashLoc,
                    5, 0.2, 0.2, 0.2, 0.1, Material.REDSTONE_BLOCK.createBlockData());
            slashLoc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, slashLoc, 3, 0.1, 0.1, 0.1, 0);

            // Check for enemies
            for (Entity entity : slashLoc.getWorld().getNearbyEntities(slashLoc, 1, 1, 1)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;

                    // Base damage increases with combo
                    double damage = 5.0 + (comboCount * 2.0);
                    target.damage(damage, player);

                    // Apply poison
                    applyGyutaroPoison(target);

                    // Knockback effect
                    Vector knockback = slashDirection.clone().multiply(0.5);
                    knockback.setY(0.2);
                    target.setVelocity(target.getVelocity().add(knockback));

                    // Blood splash effect
                    target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                            15, 0.5, 1, 0.5, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 1.2F);
                }
            }
        }
    }

    /**
     * Flying Blood Sickles - Boomerang attack
     */
    public void activateFlyingBloodSickles(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (flyingBloodSicklesCooldown.containsKey(playerId)) {
            long timeLeft = (flyingBloodSicklesCooldown.get(playerId) + FLYING_BLOOD_SICKLES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Flying Blood Sickles wieder einsetzen kannst!");
                return;
            }
        }

        flyingBloodSicklesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lFlying Blood Sickles§4 werden geschleudert!");
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 0.6F);

        // Launch two sickles (left and right)
        Vector direction = player.getLocation().getDirection().normalize();
        Vector leftDirection = rotateVector(direction, -20);
        Vector rightDirection = rotateVector(direction, 20);

        launchBloodSickle(player.getEyeLocation(), leftDirection, player, plugin);

        // Slight delay for second sickle
        new BukkitRunnable() {
            @Override
            public void run() {
                launchBloodSickle(player.getEyeLocation(), rightDirection, player, plugin);
            }
        }.runTaskLater(plugin, 5);
    }

    /**
     * Launch a blood sickle boomerang
     */
    private void launchBloodSickle(Location startLoc, Vector direction, Player owner, Main plugin) {
        ArmorStand sickle = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        sickle.setVisible(false);
        sickle.setGravity(false);
        sickle.setInvulnerable(true);
        sickle.setSmall(true);
        sickle.getEquipment().setHelmet(new ItemStack(Material.IRON_HOE));

        temporaryEntities.add(sickle);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxDistance = 15;
            private boolean returning = false;
            private double currentDistance = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > 120 || !sickle.isValid()) { // 6 seconds max
                    sickle.remove();
                    this.cancel();
                    return;
                }

                Location currentLoc = sickle.getLocation();
                Vector moveDirection;

                if (!returning) {
                    // Moving outward
                    currentDistance += 0.8;
                    moveDirection = direction.clone().multiply(0.8);

                    if (currentDistance >= maxDistance) {
                        returning = true;
                    }
                } else {
                    // Returning to player
                    Vector toPlayer = owner.getLocation().toVector().subtract(currentLoc.toVector());
                    if (toPlayer.length() < 1.5) {
                        // Reached player
                        sickle.remove();
                        this.cancel();
                        return;
                    }
                    moveDirection = toPlayer.normalize().multiply(1.0);
                }

                // Move sickle
                sickle.teleport(currentLoc.add(moveDirection));

                // Spinning effect
                Location newLoc = sickle.getLocation();
                newLoc.setYaw(newLoc.getYaw() + 30);
                sickle.teleport(newLoc);

                // Blood trail particles
                sickle.getWorld().spawnParticle(Particle.BLOCK, sickle.getLocation(),
                        3, 0.1, 0.1, 0.1, 0.05, Material.REDSTONE_BLOCK.createBlockData());

                // Check for enemy hits
                for (Entity entity : sickle.getLocation().getWorld().getNearbyEntities(sickle.getLocation(), 1.2, 1.2, 1.2)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != sickle) {
                        LivingEntity target = (LivingEntity) entity;

                        target.damage(8.0, owner);
                        applyGyutaroPoison(target);

                        // Blood slash effect
                        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                20, 0.5, 1, 0.5, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Vengeful Slices - AoE sickle spin
     */
    public void activateVengefulSlices(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (vengefulSlicesCooldown.containsKey(playerId)) {
            long timeLeft = (vengefulSlicesCooldown.get(playerId) + VENGEFUL_SLICES_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Vengeful Slices wieder einsetzen kannst!");
                return;
            }
        }

        vengefulSlicesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lVengeful Slices§4 - Racheschnitte!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);

        Location playerLoc = player.getLocation();

        // Spinning slash attack
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 40; // 2 seconds
            private double currentAngle = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    this.cancel();
                    return;
                }

                // Increase spin speed over time
                currentAngle += 18 + (ticks * 2); // Accelerating spin

                // Create slash effects in all directions
                for (int i = 0; i < 8; i++) {
                    double angle = currentAngle + (i * 45);
                    double x = Math.cos(Math.toRadians(angle)) * 3;
                    double z = Math.sin(Math.toRadians(angle)) * 3;

                    Location slashLoc = playerLoc.clone().add(x, 1, z);

                    // Blood slash particles
                    slashLoc.getWorld().spawnParticle(Particle.BLOCK, slashLoc,
                            8, 0.3, 0.3, 0.3, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                    slashLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                }

                // Damage enemies every 10 ticks
                if (ticks % 10 == 0) {
                    for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 4, 3, 4)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            target.damage(6.0, player);
                            applyGyutaroPoison(target);

                            // Knockback
                            Vector knockback = target.getLocation().toVector().subtract(playerLoc.toVector()).normalize();
                            knockback.multiply(0.8);
                            knockback.setY(0.3);
                            target.setVelocity(knockback);

                            // Blood effect
                            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                    15, 0.5, 1, 0.5, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
                        }
                    }

                    // Spin sound
                    player.getWorld().playSound(playerLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8F, 1.0F + (ticks * 0.02F));
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Summon: Ume - Cone AoE obi hit
     */
    public void activateSummonUme(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (summonUmeCooldown.containsKey(playerId)) {
            long timeLeft = (summonUmeCooldown.get(playerId) + SUMMON_UME_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Summon: Ume wieder einsetzen kannst!");
                return;
            }
        }

        summonUmeCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§d§lSummon: Ume§d - Obi-Angriff!");
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.2F);

        // Summon Ume visual
        Location summonLoc = player.getLocation().add(2, 0, 2);
        ArmorStand ume = (ArmorStand) summonLoc.getWorld().spawnEntity(summonLoc, EntityType.ARMOR_STAND);
        ume.setVisible(false);
        ume.setGravity(false);
        ume.setInvulnerable(true);
        ume.getEquipment().setHelmet(new ItemStack(Material.PINK_CONCRETE));
        ume.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        temporaryEntities.add(ume);

        // Pink particles for Ume
        summonLoc.getWorld().spawnParticle(Particle.HEART, summonLoc.add(0, 1, 0), 20, 1, 1, 1, 0.1);

        // Ume's obi attack
        Vector direction = player.getLocation().getDirection().normalize();

        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > 60 || !ume.isValid()) { // 3 seconds
                    ume.remove();
                    this.cancel();
                    return;
                }

                // Obi attack wave every 20 ticks
                if (ticks % 20 == 0) {
                    executeObiAttack(ume.getLocation(), direction, player);
                }
            }
        }.runTaskTimer(plugin, 20, 1); // Start after 1 second
    }

    /**
     * Execute Ume's obi attack
     */
    private void executeObiAttack(Location startLoc, Vector direction, Player owner) {
        // Create cone of obi strikes
        for (int distance = 2; distance <= 10; distance += 2) {
            for (int spread = -2; spread <= 2; spread++) {
                Vector obiDirection = rotateVector(direction, spread * 15);
                Location obiLoc = startLoc.clone().add(obiDirection.multiply(distance));

                // Pink obi particles
                obiLoc.getWorld().spawnParticle(Particle.HEART, obiLoc, 5, 0.5, 0.5, 0.5, 0.05);
                obiLoc.getWorld().spawnParticle(Particle.BLOCK, obiLoc,
                        10, 0.3, 0.3, 0.3, 0.1, Material.PINK_CONCRETE.createBlockData());

                // Check for enemies
                for (Entity entity : obiLoc.getWorld().getNearbyEntities(obiLoc, 1.5, 2, 1.5)) {
                    if (entity instanceof LivingEntity && entity != owner) {
                        LivingEntity target = (LivingEntity) entity;

                        target.damage(7.0, owner);
                        applyGyutaroPoison(target);

                        // Obi wrap effect (slowness)
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));

                        // Pink effect
                        target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 1, 0),
                                15, 0.5, 1, 0.5, 0.1);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_HURT, 1.0F, 1.5F);
                    }
                }
            }
        }

        startLoc.getWorld().playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F);
    }

    /**
     * Blood Tornado - Tick damage spin
     */
    public void activateBloodTornado(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        if (bloodTornadoCooldown.containsKey(playerId)) {
            long timeLeft = (bloodTornadoCooldown.get(playerId) + BLOOD_TORNADO_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Blood Tornado wieder einsetzen kannst!");
                return;
            }
        }

        bloodTornadoCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§4§lBlood Tornado§4 entfesselt!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.5F);

        Location tornadoLoc = player.getTargetBlock(null, 15).getLocation().add(0, 1, 0);

        // Create tornado effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 100; // 5 seconds
            private double currentHeight = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    this.cancel();
                    return;
                }

                currentHeight = (ticks / 10.0) % 4; // Cycling height

                // Create spiral pattern
                for (int i = 0; i < 360; i += 20) {
                    double angle = i + (ticks * 15); // Spinning
                    double radius = 3 - (currentHeight * 0.5); // Tapering

                    double x = Math.cos(Math.toRadians(angle)) * radius;
                    double z = Math.sin(Math.toRadians(angle)) * radius;

                    Location particleLoc = tornadoLoc.clone().add(x, currentHeight, z);

                    // Blood tornado particles
                    particleLoc.getWorld().spawnParticle(Particle.BLOCK, particleLoc,
                            3, 0.1, 0.1, 0.1, 0.05, Material.REDSTONE_BLOCK.createBlockData());
                    particleLoc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, particleLoc, 1, 0, 0, 0, 0);
                }

                // Damage enemies every 10 ticks (0.5 seconds)
                if (ticks % 10 == 0) {
                    for (Entity entity : tornadoLoc.getWorld().getNearbyEntities(tornadoLoc, 4, 5, 4)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Tick damage
                            target.damage(4.0, player);
                            applyGyutaroPoison(target);

                            // Pull towards center
                            Vector pullVector = tornadoLoc.toVector().subtract(target.getLocation().toVector()).normalize();
                            pullVector.multiply(0.3);
                            pullVector.setY(0.2);
                            target.setVelocity(target.getVelocity().add(pullVector));

                            // Blood effect
                            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                    10, 0.3, 0.8, 0.3, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                        }
                    }

                    // Tornado sound
                    tornadoLoc.getWorld().playSound(tornadoLoc, Sound.ENTITY_WITHER_AMBIENT, 0.5F, 1.5F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Apply Gyutaro's signature poison effect (non-stackable, high damage)
     */
    private void applyGyutaroPoison(LivingEntity target) {
        // Remove existing poison to prevent stacking
        target.removePotionEffect(PotionEffectType.POISON);

        // Apply new poison effect
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, POISON_DURATION, POISON_AMPLIFIER));

        // Visual poison effect
        target.getWorld().spawnParticle(Particle.ITEM_SLIME, target.getLocation().add(0, 1, 0),
                10, 0.3, 0.8, 0.3, 0.05);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.8F, 1.5F);
    }

    /**
     * Handle poison application on any Gyutaro attack
     */
    public void handleGyutaroAttack(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        // All Gyutaro attacks inflict poison
        applyGyutaroPoison(target);

        // Blood effect on all attacks
        target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                5, 0.3, 0.5, 0.3, 0.05, Material.REDSTONE_BLOCK.createBlockData());

        player.sendMessage("§4Giftiges Blut vergiftet " + target.getName() + "!");
    }

    /**
     * Helper method to rotate a vector by degrees (horizontal rotation)
     */
    private Vector rotateVector(Vector vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;

        return new Vector(x, vector.getY(), z);
    }

    /**
     * Get current combo count for Blood Sickles
     */
    public int getCurrentCombo(UUID playerId) {
        return bloodSicklesCombo.getOrDefault(playerId, 0);
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

        // Clear all maps
        bloodSicklesCombo.clear();
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