package org.skyforce.demon.breathings.stonebreathing;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StoneBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final Random random;

    public StoneBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    /**
     * First Form: Serpentinite Bipolar (壱いちノ型かた　蛇じゃ紋もん双そう極きょく Ichi no kata: Jamonsokyoku)
     * A two-step thrust attack. The user delivers a thrust at their opponent and follows it with a second thrust attack with enhanced force.
     */
    public void useFirstForm() {
        player.sendMessage("§8石 §f壱ノ型 蛇紋双極 §8(First Form: Serpentinite Bipolar)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stone sound
        world.playSound(startLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
        world.playSound(startLoc, Sound.BLOCK_STONE_HIT, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();
        Vector direction = player.getLocation().getDirection();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private boolean secondThrust = false;
            private Location lastThrustLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < 0.4) {
                    // First thrust
                    executeFirstThrust(currentLoc);
                } else if (!secondThrust && time >= 0.8) {
                    // Second thrust
                    executeSecondThrust(currentLoc);
                    secondThrust = true;
                }

                time += 0.05;
            }

            private void executeFirstThrust(Location location) {
                // Create thrust effect
                createThrustEffect(location, 3.0, false);

                // Check for hits
                checkThrustHits(location, 3.0, 6.0, false);

                // Move player forward slightly
                player.setVelocity(direction.clone().multiply(0.8));
            }

            private void executeSecondThrust(Location location) {
                // Create enhanced thrust effect
                createThrustEffect(location, 4.0, true);

                // Check for enhanced hits
                checkThrustHits(location, 4.0, 8.0, true);

                // Strong forward momentum
                player.setVelocity(direction.clone().multiply(1.2));

                // Enhanced sound effects
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.4f);
            }

            private void createThrustEffect(Location location, double range, boolean enhanced) {
                Vector dir = location.getDirection();

                // Create stone particles along thrust path
                for (double d = 0; d < range; d += 0.5) {
                    Location particleLoc = location.clone().add(dir.clone().multiply(d));

                    // Stone particles
                    world.spawnParticle(Particle.BLOCK, particleLoc,
                            enhanced ? 8 : 5, 0.2, 0.2, 0.2, 0, org.bukkit.Material.STONE.createBlockData());

                    if (enhanced) {
                        // Additional effects for enhanced thrust
                        world.spawnParticle(Particle.CRIT, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.BLOCK, particleLoc,
                                    4, 0.1, 0.1, 0.1, 0, org.bukkit.Material.GRANITE.createBlockData());
                        }
                    }
                }

                // Create impact point
                Location impactLoc = location.clone().add(dir.clone().multiply(range));
                world.spawnParticle(Particle.EXPLOSION, impactLoc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.BLOCK, impactLoc,
                        enhanced ? 15 : 10, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());
            }

            private void checkThrustHits(Location location, double range, double damage, boolean enhanced) {
                Vector direction = location.getDirection();
                Location frontLoc = location.clone().add(direction.clone().multiply(2));

                for (Entity entity : world.getNearbyEntities(frontLoc, range, 2.0, range)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0), enhanced);

                        // Apply knockback
                        Vector knockback = direction.clone().multiply(enhanced ? 1.5 : 1.0);
                        target.setVelocity(knockback);

                        if (enhanced) {
                            // Additional effects for enhanced hit
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                        }
                    }
                }
            }

            private void createHitEffect(Location location, boolean enhanced) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, location, enhanced ? 2 : 1, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        enhanced ? 20 : 12, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());

                if (enhanced) {
                    world.spawnParticle(Particle.BLOCK, location,
                            10, 0.3, 0.3, 0.3, 0, org.bukkit.Material.GRANITE.createBlockData());
                }

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, enhanced ? 1.2f : 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, enhanced ? 0.8f : 1.0f);

                if (enhanced) {
                    world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.4f, 1.2f);
                }
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final stone dispersion
                for (double angle = 0; angle < 360; angle += 15) {
                    double radian = Math.toRadians(angle);
                    double radius = 3.0;
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location particleLoc = endLoc.clone().add(x, 0.1, z);

                    world.spawnParticle(Particle.BLOCK, particleLoc,
                            5, 0.2, 0.2, 0.2, 0, org.bukkit.Material.STONE.createBlockData());
                }

                // Final sound
                world.playSound(endLoc, Sound.BLOCK_STONE_BREAK, 0.8f, 0.6f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0));

        // Add cooldown
        addCooldown(player, "FirstForm", 12);
    }

    // Cooldown management method
    private void addCooldown(Player player, String ability, int seconds) {
        // Implement your cooldown system here
    }

    /**
     * Second Form: Upper Smash (弐ノ型 天面砕き)
     * A powerful downward strike that crushes opponents from above with the weight of a boulder
     */
    public void useSecondForm() {
        player.sendMessage("§8石 §f弐ノ型 天面砕き §8(Second Form: Upper Smash)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stone sound
        world.playSound(startLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
        world.playSound(startLoc, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasSmashed = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final double LEAP_DURATION = 0.5;
            private Location peakLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < LEAP_DURATION) {
                    // Initial leap phase
                    executeLeapPhase(currentLoc);
                } else if (!hasSmashed.get() && player.isOnGround()) {
                    // Execute smash on landing
                    executeSmash(currentLoc);
                    hasSmashed.set(true);
                } else if (!hasSmashed.get()) {
                    // Create falling effects
                    createFallingEffects(currentLoc);
                }

                time += 0.05;
            }

            private void executeLeapPhase(Location location) {
                double progress = time / LEAP_DURATION;

                // Create rising stone effect
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2 / 8);
                    double radius = 1.0 + progress;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location stoneLoc = location.clone().add(x, progress * 2, z);

                    // Stone particles
                    world.spawnParticle(Particle.BLOCK, stoneLoc,
                            5, 0.2, 0.2, 0.2, 0, org.bukkit.Material.STONE.createBlockData());

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.BLOCK, stoneLoc,
                                3, 0.1, 0.1, 0.1, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                    }
                }

                // Apply upward velocity with slight forward momentum
                Vector direction = location.getDirection().multiply(0.5).setY(1.8);
                player.setVelocity(direction);

                // Rising sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f + (float)progress);
                }

                // Store peak location
                if (progress > 0.9) {
                    peakLocation = location.clone();
                }
            }

            private void createFallingEffects(Location location) {
                // Create falling stone particles
                world.spawnParticle(Particle.BLOCK, location,
                        8, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());

                // Add weight to fall
                Vector velocity = player.getVelocity();
                velocity.setY(velocity.getY() - 0.3);
                player.setVelocity(velocity);

                // Falling sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_STONE_FALL, 0.6f, 0.5f);
                }
            }

            private void executeSmash(Location location) {
                // Create massive impact effect
                world.spawnParticle(Particle.EXPLOSION, location, 3, 0.5, 0, 0.5, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        50, 2.0, 0.5, 2.0, 0, org.bukkit.Material.STONE.createBlockData());
                world.spawnParticle(Particle.BLOCK, location,
                        30, 2.0, 0.5, 2.0, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                // Create shockwave
                createShockwave(location);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.5f, 0.5f);
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.6f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

                // Check for hits in larger radius
                checkSmashHits(location);
            }

            private void createShockwave(Location center) {
                new BukkitRunnable() {
                    private double radius = 0;
                    private final double MAX_RADIUS = 6.0;
                    private final double SPEED = 0.5;

                    @Override
                    public void run() {
                        if (radius >= MAX_RADIUS) {
                            this.cancel();
                            return;
                        }

                        for (double angle = 0; angle < 360; angle += 10) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location waveLoc = center.clone().add(x, 0, z);

                            // Shockwave particles
                            world.spawnParticle(Particle.BLOCK, waveLoc,
                                    3, 0.2, 0, 0.2, 0, org.bukkit.Material.STONE.createBlockData());

                            if (random.nextFloat() < 0.2) {
                                world.spawnParticle(Particle.BLOCK, waveLoc,
                                        2, 0.1, 0, 0.1, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                            }
                        }

                        radius += SPEED;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void checkSmashHits(Location center) {
                double hitRadius = 6.0;
                for (Entity entity : world.getNearbyEntities(center, hitRadius, 3.0, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double distance = target.getLocation().distance(center);
                        double damage = 14.0 * (1 - distance / (hitRadius + 2));

                        // Apply damage
                        target.damage(Math.max(8.0, damage), player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback and effects
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(2.0)
                                .setY(0.5);
                        target.setVelocity(knockback);

                        // Apply stun effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, location, 2, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());
                world.spawnParticle(Particle.BLOCK, location,
                        10, 0.3, 0.3, 0.3, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.7f);
                world.playSound(location, Sound.BLOCK_STONE_HIT, 1.0f, 0.5f);
            }

            private void createFinishingEffect() {
                if (!hasSmashed.get()) return;

                Location endLoc = player.getLocation();

                // Final stone dispersion
                new BukkitRunnable() {
                    private double radius = 6.0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        radius *= 0.9;

                        for (double angle = 0; angle < 360; angle += 30) {
                            double radian = Math.toRadians(angle + (ticks * 5));
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.1, z);

                            world.spawnParticle(Particle.BLOCK, particleLoc,
                                    3, 0.2, 0.2, 0.2, 0, org.bukkit.Material.STONE.createBlockData());
                        }

                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_STONE_BREAK, 0.4f, 0.5f);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0));

        // Add cooldown
        addCooldown(player, "SecondForm", 16);
    }

    /**
     * Third Form: Stone Skin (参ノ型 岩軀の膚)
     */
    public void useThirdForm() {
        player.sendMessage("§8石 §f参ノ型 岩軀の膚 §8(Third Form: Stone Skin)");

        final World world = player.getLocation().getWorld();
        final Location startLoc = player.getLocation();
        final Set<Entity> hitEntities = new HashSet<>();
        final Player playerRef = this.player;

        world.playSound(startLoc, Sound.BLOCK_STONE_PLACE, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_DEEPSLATE_PLACE, 1.0f, 0.8f);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 8.0;
            private double rotation = 0;
            private int stonesCreated = 0;
            private final int MAX_STONES = 12;
            private final Set<Location> stoneFragments = new HashSet<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = playerRef.getLocation();

                if (time < 0.5 && stonesCreated < MAX_STONES) {
                    createStoneFragment(currentLoc);
                    stonesCreated++;
                }

                updateStoneFragments(currentLoc);
                checkForIncomingDamage();

                rotation += 5;
                time += 0.05;
            }

            private void createStoneFragment(Location center) {
                double angle = (360.0 / MAX_STONES) * stonesCreated;
                double radian = Math.toRadians(angle);
                double radius = 2.0;

                double x = Math.cos(radian) * radius;
                double z = Math.sin(radian) * radius;

                Location stoneLoc = center.clone().add(x, 1, z);
                stoneFragments.add(stoneLoc);

                world.spawnParticle(Particle.BLOCK, stoneLoc,
                        15, 0.2, 0.2, 0.2, 0, org.bukkit.Material.STONE.createBlockData());
                world.spawnParticle(Particle.BLOCK, stoneLoc,
                        10, 0.2, 0.2, 0.2, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                world.playSound(stoneLoc, Sound.BLOCK_STONE_PLACE, 0.6f, 0.8f);
            }

            private void updateStoneFragments(Location center) {
                Set<Location> updatedFragments = new HashSet<>();

                for (Location stoneLoc : stoneFragments) {
                    double originalAngle = Math.atan2(
                            stoneLoc.getZ() - center.getZ(),
                            stoneLoc.getX() - center.getX()
                    );
                    double newAngle = originalAngle + Math.toRadians(rotation);

                    double radius = 2.0;
                    double newX = center.getX() + (Math.cos(newAngle) * radius);
                    double newZ = center.getZ() + (Math.sin(newAngle) * radius);

                    Location newLoc = new Location(world, newX, center.getY() + 1, newZ);
                    updatedFragments.add(newLoc);

                    createBarrierEffect(newLoc);
                    checkForCounterAttack(newLoc);
                }

                stoneFragments.clear();
                stoneFragments.addAll(updatedFragments);
            }

            private void createBarrierEffect(Location location) {
                world.spawnParticle(Particle.BLOCK, location,
                        3, 0.1, 0.1, 0.1, 0, org.bukkit.Material.STONE.createBlockData());

                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.BLOCK, location,
                            2, 0.1, 0.1, 0.1, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                }

                for (Location otherLoc : stoneFragments) {
                    if (!otherLoc.equals(location)) {
                        Vector direction = otherLoc.toVector().subtract(location.toVector());
                        double distance = direction.length();
                        direction.normalize().multiply(0.5);

                        for (double d = 0; d < distance; d += 0.5) {
                            Location lineLoc = location.clone().add(direction.clone().multiply(d));
                            world.spawnParticle(Particle.BLOCK, lineLoc,
                                    1, 0.1, 0.1, 0.1, 0, org.bukkit.Material.STONE.createBlockData());
                        }
                    }
                }
            }

            private void checkForIncomingDamage() {
                double defenseRadius = 2.5;

                for (Entity entity : world.getNearbyEntities(playerRef.getLocation(), defenseRadius, defenseRadius, defenseRadius)) {
                    if (entity instanceof LivingEntity && entity != playerRef && !hitEntities.contains(entity)) {
                        LivingEntity attacker = (LivingEntity) entity;

                        Location blockLoc = attacker.getLocation().add(0, 1, 0);
                        createBlockEffect(blockLoc);

                        attacker.damage(6.0, playerRef);
                        hitEntities.add(attacker);

                        Vector knockback = attacker.getLocation().subtract(playerRef.getLocation()).toVector()
                                .normalize()
                                .multiply(1.5)
                                .setY(0.2);
                        attacker.setVelocity(knockback);

                        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    }
                }
            }

            private void checkForCounterAttack(Location stoneLoc) {
                double attackRadius = 1.5;

                for (Entity entity : world.getNearbyEntities(stoneLoc, attackRadius, attackRadius, attackRadius)) {
                    if (entity instanceof LivingEntity && entity != playerRef && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        target.damage(5.0, playerRef);
                        hitEntities.add(target);

                        createHitEffect(target.getLocation().add(0, 1, 0));

                        Vector knockback = playerRef.getLocation().subtract(target.getLocation()).toVector()
                                .normalize()
                                .multiply(0.8)
                                .setY(0.2);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createBlockEffect(Location location) {
                world.spawnParticle(Particle.BLOCK, location,
                        20, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());
                world.spawnParticle(Particle.BLOCK, location,
                        15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                world.playSound(location, Sound.BLOCK_STONE_HIT, 1.0f, 0.8f);
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.2f);
            }

            private void createHitEffect(Location location) {
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        10, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());

                world.playSound(location, Sound.BLOCK_STONE_BREAK, 0.8f, 1.0f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 0.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = playerRef.getLocation();

                for (Location stoneLoc : stoneFragments) {
                    world.spawnParticle(Particle.EXPLOSION, stoneLoc, 1, 0.2, 0.2, 0.2, 0);
                    world.spawnParticle(Particle.BLOCK, stoneLoc,
                            15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());
                }

                world.playSound(endLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
                world.playSound(endLoc, Sound.BLOCK_STONE_FALL, 1.0f, 0.8f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 0));

        addCooldown(player, "ThirdForm", 20);
    }

    /**
     * Fourth Form: Volcanic Rock - Rapid Conquest (肆ノ型 流紋巌・速征)
     * Created: 2025-06-16 15:21:57
     * @author SkyForce-6
     *
     * A rapid succession of volcanic rock strikes that overwhelm the opponent through speed and heat
     */
    public void useFourthForm() {
        player.sendMessage("§8石 §f肆ノ型 流紋巌・速征 §8(Fourth Form: Volcanic Rock - Rapid Conquest)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial volcanic sound
        world.playSound(startLoc, Sound.BLOCK_LAVA_POP, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger comboCount = new AtomicInteger(0);
        final int MAX_COMBOS = 6;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private Location lastStrikeLocation = null;
            private double currentRadius = 1.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION || comboCount.get() >= MAX_COMBOS) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Execute strikes in rapid succession
                if (time % 0.3 < 0.05 && comboCount.get() < MAX_COMBOS) {
                    executeVolcanicStrike(currentLoc);
                    comboCount.incrementAndGet();
                }

                // Create volcanic effects
                createVolcanicEffects(currentLoc);

                time += 0.05;
            }

            private void executeVolcanicStrike(Location location) {
                // Calculate strike direction with slight variation
                Vector baseDirection = location.getDirection();
                double angleOffset = (random.nextDouble() - 0.5) * 30; // ±15 degrees
                Vector strikeDirection = rotateVector(baseDirection, angleOffset);

                // Create volcanic strike effect
                createStrikeEffect(location, strikeDirection);

                // Move player forward slightly
                player.setVelocity(strikeDirection.multiply(0.8));

                // Check for hits
                checkStrikeHits(location, strikeDirection);

                // Update last strike location
                lastStrikeLocation = location.clone();

                // Increase strike radius for combo effect
                currentRadius = Math.min(currentRadius + 0.3, 3.0);
            }

            private void createStrikeEffect(Location location, Vector direction) {
                // Create advancing line of volcanic particles
                for (double d = 0; d < 4.0; d += 0.2) {
                    Location particleLoc = location.clone().add(direction.clone().multiply(d));

                    // Volcanic rock particles
                    world.spawnParticle(Particle.BLOCK, particleLoc,
                            8, 0.2, 0.2, 0.2, 0, org.bukkit.Material.MAGMA_BLOCK.createBlockData());

                    // Lava particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }

                    // Fire particles
                    world.spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }

                // Strike sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 0.8f, 1.2f);
                world.playSound(location, Sound.BLOCK_LAVA_POP, 0.6f, 1.4f);

                // Create impact point
                Location impactLoc = location.clone().add(direction.clone().multiply(4.0));
                world.spawnParticle(Particle.EXPLOSION, impactLoc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.LAVA, impactLoc, 3, 0.2, 0.2, 0.2, 0);
            }

            private void checkStrikeHits(Location location, Vector direction) {
                Location hitboxCenter = location.clone().add(direction.clone().multiply(2));

                for (Entity entity : world.getNearbyEntities(hitboxCenter, currentRadius, 2.0, currentRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate combo damage
                        double comboDamage = 5.0 + (comboCount.get() * 1.5);

                        // Apply damage
                        target.damage(comboDamage, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply fire and effects
                        target.setFireTicks(60); // 3 seconds of fire
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));

                        // Knockback
                        Vector knockback = direction.clone().multiply(0.8 + (comboCount.get() * 0.2));
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createVolcanicEffects(Location location) {
                // Create ambient volcanic effects
                for (int i = 0; i < 3; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = random.nextDouble() * currentRadius;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location effectLoc = location.clone().add(x, random.nextDouble(), z);

                    // Magma particles
                    world.spawnParticle(Particle.BLOCK, effectLoc,
                            3, 0.1, 0.1, 0.1, 0, org.bukkit.Material.MAGMA_BLOCK.createBlockData());

                    // Fire effects
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.FLAME, effectLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Lava sound effects
                if (random.nextFloat() < 0.1) {
                    world.playSound(location, Sound.BLOCK_LAVA_POP, 0.3f, 1.0f);
                }
            }

            private void createHitEffect(Location location) {
                // Explosion particles
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0.2, 0.2, 0.2, 0);

                // Volcanic impact particles
                world.spawnParticle(Particle.BLOCK, location,
                        15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.MAGMA_BLOCK.createBlockData());
                world.spawnParticle(Particle.LAVA, location, 5, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.FLAME, location, 8, 0.2, 0.2, 0.2, 0.1);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
                world.playSound(location, Sound.BLOCK_LAVA_EXTINGUISH, 0.6f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.0f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final explosion
                new BukkitRunnable() {
                    private double radius = currentRadius;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        radius *= 0.9;

                        for (double angle = 0; angle < 360; angle += 20) {
                            double radian = Math.toRadians(angle + (ticks * 8));
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.1, z);

                            // Dispersing volcanic particles
                            world.spawnParticle(Particle.BLOCK, particleLoc,
                                    3, 0.2, 0.2, 0.2, 0, org.bukkit.Material.MAGMA_BLOCK.createBlockData());

                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            }
                        }

                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_LAVA_POP, 0.4f, 0.8f);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private Vector rotateVector(Vector vector, double degrees) {
                double rad = Math.toRadians(degrees);
                double currentX = vector.getX();
                double currentZ = vector.getZ();

                double cos = Math.cos(rad);
                double sin = Math.sin(rad);

                double newX = currentX * cos - currentZ * sin;
                double newZ = currentX * sin + currentZ * cos;

                return new Vector(newX, vector.getY(), newZ).normalize();
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));

        // Add cooldown
        addCooldown(player, "FourthForm", 141);
    }


    /**
     * Fifth Form: Arcs of Justice (伍ノ型 正義の石弧)
     * Created: 2025-06-16 15:29:05
     * @author SkyForce-6
     *
     * Creates multiple arcing stone paths that converge on the target, representing the weight of justice
     */
    public void useFifthForm() {
        player.sendMessage("§8石 §f伍ノ型 正義の石弧 §8(Fifth Form: Arcs of Justice)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stone formation sound
        world.playSound(startLoc, Sound.BLOCK_STONE_PLACE, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.BLOCK_BASALT_PLACE, 1.0f, 0.8f);

        Set<Entity> hitEntities = new HashSet<>();
        List<Location> arcPoints = new ArrayList<>();
        AtomicInteger arcCount = new AtomicInteger(0);
        final int MAX_ARCS = 5;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 4.0;
            private double baseRadius = 3.0;
            private boolean convergencePhase = false;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Arc creation phase
                if (!convergencePhase && arcCount.get() < MAX_ARCS && time % 0.4 < 0.05) {
                    createJusticeArc(currentLoc);
                    arcCount.incrementAndGet();
                }

                // Start convergence after all arcs are created
                if (!convergencePhase && arcCount.get() >= MAX_ARCS) {
                    convergencePhase = true;
                    executeConvergence(currentLoc);
                }

                // Update existing arcs
                updateArcs(currentLoc);

                time += 0.05;
            }

            private void createJusticeArc(Location center) {
                double angle = (360.0 / MAX_ARCS) * arcCount.get();
                double heightOffset = 2.0 + random.nextDouble();

                // Create base points for the arc
                for (double t = 0; t <= 1; t += 0.1) {
                    double radian = Math.toRadians(angle);
                    double x = Math.cos(radian) * baseRadius;
                    double z = Math.sin(radian) * baseRadius;
                    double y = Math.sin(Math.PI * t) * heightOffset;

                    // Prüfe auf NaN vor dem Hinzufügen
                    if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                        System.err.println("[StoneBreathingAbility] NaN in createJusticeArc: x=" + x + ", y=" + y + ", z=" + z + ", t=" + t + ", angle=" + angle);
                        continue;
                    }

                    Location arcPoint = center.clone().add(x, y, z);
                    arcPoints.add(arcPoint);

                    // Create initial arc effect
                    createArcEffect(arcPoint, true);
                }

                // Arc formation sound
                world.playSound(center, Sound.BLOCK_STONE_PLACE, 0.8f, 1.0f + (arcCount.get() * 0.1f));
                world.playSound(center, Sound.BLOCK_BASALT_BREAK, 0.6f, 0.8f);
            }

            private void updateArcs(Location center) {
                List<Location> updatedPoints = new ArrayList<>();

                for (Location point : arcPoints) {
                    if (convergencePhase) {
                        // Move points towards target during convergence
                        Vector toCenter = center.toVector().subtract(point.toVector());
                        if (toCenter.lengthSquared() == 0) {
                            // Verhindere Division durch 0
                            updatedPoints.add(point.clone());
                            continue;
                        }
                        Location newPoint = point.clone().add(toCenter.normalize().multiply(0.3));
                        if (!isFiniteLocation(newPoint)) {
                            System.err.println("[StoneBreathingAbility] NaN in updateArcs (convergence): " + newPoint);
                            continue;
                        }
                        updatedPoints.add(newPoint);
                        // Create trailing effect
                        createArcEffect(newPoint, false);
                    } else {
                        // Rotate points around center before convergence
                        double distance = point.distance(center);
                        double currentAngle = Math.atan2(
                                point.getZ() - center.getZ(),
                                point.getX() - center.getX()
                        );
                        double newAngle = currentAngle + Math.toRadians(3);
                        double newX = center.getX() + (Math.cos(newAngle) * distance);
                        double newZ = center.getZ() + (Math.sin(newAngle) * distance);
                        if (Double.isNaN(newX) || Double.isNaN(newZ)) {
                            System.err.println("[StoneBreathingAbility] NaN in updateArcs (rotation): newX=" + newX + ", newZ=" + newZ + ", distance=" + distance + ", currentAngle=" + currentAngle);
                            continue;
                        }
                        Location newPoint = new Location(world, newX, point.getY(), newZ);
                        updatedPoints.add(newPoint);
                        // Create stable arc effect
                        createArcEffect(newPoint, false);
                    }
                }

                // Update arc points
                arcPoints.clear();
                arcPoints.addAll(updatedPoints);

                // Check for hits
                checkArcHits(center);
            }

            private void createArcEffect(Location location, boolean isNew) {
                // Stone particles
                world.spawnParticle(Particle.BLOCK, location,
                        isNew ? 10 : 5, 0.1, 0.1, 0.1, 0, org.bukkit.Material.BASALT.createBlockData());

                // Justice energy particles
                world.spawnParticle(Particle.END_ROD, location, 2, 0.1, 0.1, 0.1, 0.02);

                if (isNew) {
                    world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
                }
            }

            private void executeConvergence(Location center) {
                // Convergence initiation effect
                world.spawnParticle(Particle.EXPLOSION, center, 3, 0.5, 0.5, 0.5, 0);
                world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 0.5f, 1.5f);

                // Add effects to player during convergence
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
            }

            private void checkArcHits(Location center) {
                for (Location arcPoint : arcPoints) {
                    // Prüfe, ob die Koordinaten endlich sind
                    if (!isFiniteLocation(arcPoint)) continue;
                    try {
                        for (Entity entity : world.getNearbyEntities(arcPoint, 1.5, 1.5, 1.5)) {
                            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                                LivingEntity target = (LivingEntity) entity;

                                // Calculate damage based on convergence phase
                                double damage = convergencePhase ? 12.0 : 6.0;

                                // Apply damage
                                target.damage(damage, player);
                                hitEntities.add(target);

                                // Create hit effect
                                createHitEffect(target.getLocation().add(0, 1, 0));

                                // Apply knockback and effects
                                Vector knockback = target.getLocation().subtract(center).toVector()
                                        .normalize()
                                        .multiply(convergencePhase ? 1.5 : 0.8)
                                        .setY(0.3);
                                target.setVelocity(knockback);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("[StoneBreathingAbility] Ungültige Location für getNearbyEntities: " + arcPoint + ", Fehler: " + e.getMessage());
                    }
                }
            }

            // Hilfsmethode zur Prüfung auf endliche Koordinaten
            private boolean isFiniteLocation(Location loc) {
                boolean finite = Double.isFinite(loc.getX()) && Double.isFinite(loc.getY()) && Double.isFinite(loc.getZ());
                if (!finite) {
                    System.err.println("[StoneBreathingAbility] Nicht-endliche Location erkannt: " + loc);
                }
                return finite;
            }

            private void createHitEffect(Location location) {
                // Justice impact particles
                world.spawnParticle(Particle.EXPLOSION, location, 2, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.END_ROD, location, 8, 0.3, 0.3, 0.3, 0.2);
                world.spawnParticle(Particle.BLOCK, location,
                        15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.BASALT.createBlockData());

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
                world.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.5f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.0f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final justice explosion
                new BukkitRunnable() {
                    private double radius = baseRadius;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        radius *= 0.9;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * radius;
                            double y = Math.sin(ticks * Math.PI / 10) * 0.5;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, y, z);

                            // Final particles
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            world.spawnParticle(Particle.BLOCK, particleLoc,
                                    2, 0.1, 0.1, 0.1, 0, org.bukkit.Material.BASALT.createBlockData());
                        }

                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_STONE_BREAK, 0.4f, 0.8f + (ticks * 0.02f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));

        // Add cooldown
        addCooldown(player, "FifthForm", 16);
    }

    /**
     * Sixth Form: Tectonic Crush (六ノ型 地殻粉砕)
     * Created: 2025-06-16 15:36:08
     * @author SkyForce-6
     *
     * A devastating ground-based technique that creates tectonic shockwaves and stone pillars
     */
    public void useSixthForm() {
        player.sendMessage("§8石 §f六ノ型 地殻粉砕 §8(Sixth Form: Tectonic Crush)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial earth-shaking sound
        world.playSound(startLoc, Sound.BLOCK_STONE_BREAK, 1.5f, 0.5f);
        world.playSound(startLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.8f);

        Set<Entity> hitEntities = new HashSet<>();
        List<Location> pillarLocations = new ArrayList<>();
        final double MAX_RADIUS = 12.0;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 5.0;
            private double currentRadius = 1.0;
            private boolean mainImpactExecuted = false;
            private int phase = 0; // 0: charging, 1: shockwave, 2: pillars

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                switch(phase) {
                    case 0: // Charging phase
                        executeChargingPhase(currentLoc);
                        if (time >= 1.5) {
                            phase = 1;
                            executeMainImpact(currentLoc);
                        }
                        break;

                    case 1: // Shockwave phase
                        executeShockwavePhase(currentLoc);
                        if (currentRadius >= MAX_RADIUS) {
                            phase = 2;
                            initiatePillarPhase(currentLoc);
                        }
                        break;

                    case 2: // Pillar phase
                        executePillarPhase(currentLoc);
                        break;
                }

                time += 0.05;
            }

            private void executeChargingPhase(Location location) {
                double progress = Math.min(time / 1.5, 1.0);

                // Create charging effect
                for (double angle = 0; angle < 360; angle += 15) {
                    double radian = Math.toRadians(angle + (time * 30));
                    double radius = 2.0 * progress;
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location chargeLoc = location.clone().add(x, 0, z);

                    // Stone gathering particles
                    world.spawnParticle(Particle.BLOCK, chargeLoc,
                            5, 0.2, 0.1, 0.2, 0, org.bukkit.Material.STONE.createBlockData());
                    world.spawnParticle(Particle.BLOCK, chargeLoc,
                            3, 0.1, 0, 0.1, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                }

                // Charging sounds
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_STONE_STEP, 0.5f, 0.5f + (float)progress);
                }
            }

            private void executeMainImpact(Location location) {
                // Massive impact effect
                world.spawnParticle(Particle.EXPLOSION, location, 3, 0.5, 0, 0.5, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        50, 2.0, 0.5, 2.0, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                // Impact sounds
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 2.0f, 0.3f);
                world.playSound(location, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.5f, 0.6f);

                // Ground deformation effect
                for (double r = 0; r < 3; r += 0.5) {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * r;
                        double z = Math.sin(radian) * r;

                        Location crackLoc = location.clone().add(x, 0, z);
                        world.spawnParticle(Particle.BLOCK, crackLoc,
                                10, 0.3, 0, 0.3, 0, org.bukkit.Material.STONE.createBlockData());
                    }
                }
            }

            private void executeShockwavePhase(Location location) {
                currentRadius += 0.5;

                // Create expanding shockwave
                for (double angle = 0; angle < 360; angle += 5) {
                    double radian = Math.toRadians(angle);
                    double x = Math.cos(radian) * currentRadius;
                    double z = Math.sin(radian) * currentRadius;

                    Location waveLoc = location.clone().add(x, 0, z);

                    // Shockwave particles
                    world.spawnParticle(Particle.BLOCK, waveLoc,
                            5, 0.2, 0.1, 0.2, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                    world.spawnParticle(Particle.EXPLOSION, waveLoc, 1, 0.1, 0, 0.1, 0);
                }

                // Check for hits
                checkShockwaveHits(location);

                // Shockwave sounds
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_STONE_BREAK, 0.8f, 0.5f);
                }
            }

            private void initiatePillarPhase(Location location) {
                // Calculate pillar positions
                for (int i = 0; i < 8; i++) {
                    double angle = (360.0 / 8) * i;
                    double radian = Math.toRadians(angle);
                    double distance = 6.0 + random.nextDouble() * 4.0;

                    double x = Math.cos(radian) * distance;
                    double z = Math.sin(radian) * distance;

                    Location pillarLoc = location.clone().add(x, 0, z);
                    pillarLocations.add(pillarLoc);
                }
            }

            private void executePillarPhase(Location location) {
                for (Location pillarLoc : pillarLocations) {
                    double height = 4.0;

                    // Create rising pillar effect
                    for (double y = 0; y < height; y += 0.5) {
                        Location particleLoc = pillarLoc.clone().add(0, y, 0);

                        world.spawnParticle(Particle.BLOCK, particleLoc,
                                8, 0.3, 0.3, 0.3, 0, org.bukkit.Material.STONE.createBlockData());

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.BLOCK, particleLoc,
                                    4, 0.2, 0.2, 0.2, 0, org.bukkit.Material.DEEPSLATE.createBlockData());
                        }
                    }

                    // Check for pillar hits
                    checkPillarHits(pillarLoc);
                }

                // Pillar sounds
                if (random.nextFloat() < 0.3) {
                    Location soundLoc = pillarLocations.get(random.nextInt(pillarLocations.size()));
                    world.playSound(soundLoc, Sound.BLOCK_STONE_PLACE, 1.0f, 0.6f);
                }
            }

            private void checkShockwaveHits(Location center) {
                for (Entity entity : world.getNearbyEntities(center, currentRadius + 1, 2.0, currentRadius + 1)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double distance = target.getLocation().distance(center);
                        double damage = 12.0 * (1 - distance / (MAX_RADIUS + 2));

                        // Apply damage and effects
                        target.damage(Math.max(6.0, damage), player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback and effects
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(2.0)
                                .setY(0.5);
                        target.setVelocity(knockback);

                        // Apply status effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    }
                }
            }

            private void checkPillarHits(Location pillarLoc) {
                for (Entity entity : world.getNearbyEntities(pillarLoc, 1.5, 4.0, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply pillar damage
                        target.damage(10.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Launch target upward
                        target.setVelocity(new Vector(0, 1.5, 0));

                        // Apply status effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, location, 2, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.BLOCK, location,
                        15, 0.3, 0.3, 0.3, 0, org.bukkit.Material.DEEPSLATE.createBlockData());

                // Impact sounds
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.6f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 0.7f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final ground collapse effect
                for (Location pillarLoc : pillarLocations) {
                    world.spawnParticle(Particle.EXPLOSION, pillarLoc, 1, 0.5, 0.5, 0.5, 0);
                    world.spawnParticle(Particle.BLOCK, pillarLoc,
                            20, 1.0, 1.0, 1.0, 0, org.bukkit.Material.STONE.createBlockData());
                }

                // Final sounds
                world.playSound(endLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.5f);
                world.playSound(endLoc, Sound.BLOCK_STONE_BREAK, 1.5f, 0.4f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));

        // Add cooldown
        addCooldown(player, "SixthForm", 25);
    }

}

