package org.skyforce.demon.breathings.lovebreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Love Breathing Implementation
 * Created: 2025-06-16 17:05:46
 * @author SkyForce-6
 */
public class LoveBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final Random random;

    public LoveBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    /**
     * First Form: Shivers of First Love (壱ノ型 初恋のわななき)
     * Created: 2025-06-16 17:10:16
     * @author SkyForce-6
     *
     * A swift, emotional strike that curves through targets with the excitement of first love
     */
    public void useFirstForm() {
        player.sendMessage("§d恋 §f壱ノ型 初恋のわななき §d(First Form: Shivers of First Love)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial love aura
        world.spawnParticle(Particle.HEART, startLoc.add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0);
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        List<Location> slashPath = new ArrayList<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private Location currentTarget = null;
            private int pathPoints = 0;
            private final int MAX_POINTS = 8;
            private boolean hasStartedSlash = false;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (!hasStartedSlash) {
                    // Initial dash
                    initiateSlash(currentLoc);
                    hasStartedSlash = true;
                } else {
                    // Execute curving slash
                    executeSlash(currentLoc);
                }

                time += 0.05;
            }

            private void initiateSlash(Location location) {
                // Initial dash effect
                Vector direction = location.getDirection();
                player.setVelocity(direction.multiply(1.5));

                // Starting effects
                world.spawnParticle(Particle.HEART, location.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

                // Add first path point
                slashPath.add(location.clone());
                pathPoints++;
            }

            private void executeSlash(Location location) {
                // Generate curving path
                if (pathPoints < MAX_POINTS && time % 0.1 < 0.05) {
                    // Calculate next point with slight curve
                    double curve = Math.sin(time * Math.PI) * 2;
                    Vector direction = location.getDirection();
                    Vector side = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                    Location nextPoint = location.clone().add(direction.multiply(2)).add(side.multiply(curve));
                    slashPath.add(nextPoint);
                    pathPoints++;

                    // Create path effects
                    createPathEffects(nextPoint);
                }

                // Update slash effects along path
                updateSlashEffects();

                // Check for hits along path
                checkPathHits();
            }

            private void createPathEffects(Location location) {
                // Love trail particles
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1.0f);
                world.spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dustPink);
                world.spawnParticle(Particle.HEART, location, 1, 0.1, 0.1, 0.1, 0);

                // Trailing sound effects
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.8f);
                if (random.nextFloat() < 0.3) {
                    world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 1.5f);
                }
            }

            private void updateSlashEffects() {
                if (slashPath.size() < 2) return;

                for (int i = 0; i < slashPath.size() - 1; i++) {
                    Location current = slashPath.get(i);
                    Location next = slashPath.get(i + 1);

                    // Create connecting effects between path points
                    Vector direction = next.toVector().subtract(current.toVector());
                    double distance = direction.length();
                    direction.normalize();

                    for (double d = 0; d < distance; d += 0.5) {
                        Location effectLoc = current.clone().add(direction.clone().multiply(d));

                        // Love slash particles
                        Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1.0f);
                        world.spawnParticle(Particle.DUST, effectLoc, 3, 0.1, 0.1, 0.1, 0, dustPink);

                        if (random.nextFloat() < 0.1) {
                            world.spawnParticle(Particle.HEART, effectLoc, 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }
            }

            private void checkPathHits() {
                for (Location pathLoc : slashPath) {
                    for (Entity entity : world.getNearbyEntities(pathLoc, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply damage
                            target.damage(8.0, player);
                            hitEntities.add(target);

                            // Create hit effect
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Apply love-based effects
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));

                            // Emotional impact knockback
                            Vector knockback = target.getLocation().subtract(pathLoc).toVector()
                                    .normalize()
                                    .multiply(0.8)
                                    .setY(0.2);
                            target.setVelocity(knockback);
                        }
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Love impact particles
                world.spawnParticle(Particle.HEART, location, 5, 0.3, 0.3, 0.3, 0);
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 182, 193), 2.0f);
                world.spawnParticle(Particle.DUST, location, 15, 0.3, 0.3, 0.3, 0, dustPink);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final love burst
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double radius = 3.0 * (1 - ticks / (double)MAX_TICKS);

                        for (double angle = 0; angle < 360; angle += 36) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.5, z);

                            // Dispersing love particles
                            world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                            Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1.0f);
                            world.spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0, dustPink);
                        }

                        // Fading love sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.5f - (ticks * 0.05f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0));

        // Add cooldown
     //   addCooldown(player, "FirstForm", 10);
    }

    /**
     * Second Form: Love Pangs (弐ノ型 懊悩巡る恋)
     * Created: 2025-06-16 17:15:17
     * @author SkyForce-6
     *
     * A whip-like technique that creates multiple cutting angles through love's anguish
     */
    public void useSecondForm() {
        player.sendMessage("§d恋 §f弐ノ型 懊悩巡る恋 §d(Second Form: Love Pangs)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial love surge
        world.spawnParticle(Particle.HEART, startLoc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 0.7f);

        Set<Entity> hitEntities = new HashSet<>();
        List<Vector> whipPoints = new ArrayList<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final int MAX_WHIP_POINTS = 12;
            private double whipLength = 0;
            private final double MAX_WHIP_LENGTH = 6.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Generate whip path
                if (whipPoints.size() < MAX_WHIP_POINTS) {
                    generateWhipPoint(currentLoc);
                }

                // Update whip movement
                updateWhipMotion(currentLoc);

                // Check for hits
                checkWhipHits(currentLoc);

                time += 0.05;
            }

            private void generateWhipPoint(Location location) {
                Vector direction = location.getDirection();

                // Calculate whip curve using sine wave
                double progress = whipPoints.size() / (double)MAX_WHIP_POINTS;
                double angle = Math.sin(progress * Math.PI * 2) * 90; // Angle variation
                Vector whipVector = rotateVector(direction, angle);

                whipPoints.add(whipVector);

                // Create initial whip effect
                createWhipEffect(location, whipVector, progress);
            }

            private void updateWhipMotion(Location center) {
                whipLength = Math.min(MAX_WHIP_LENGTH, whipLength + 0.4);

                // Update each whip segment
                for (int i = 0; i < whipPoints.size(); i++) {
                    double segmentProgress = i / (double)whipPoints.size();
                    Vector whipDir = whipPoints.get(i);

                    // Calculate segment position
                    double segmentLength = whipLength * (1 - segmentProgress * 0.3); // Whip tapers off
                    Location segmentLoc = center.clone().add(whipDir.clone().multiply(segmentLength));

                    // Create whip trail
                    createWhipTrail(center, segmentLoc, segmentProgress);
                }
            }

            private void createWhipEffect(Location location, Vector direction, double progress) {
                // Whip formation particles
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1.0f);
                world.spawnParticle(Particle.DUST, location, 5, 0.2, 0.2, 0.2, 0, dustPink);

                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.HEART, location, 1, 0.1, 0.1, 0.1, 0);
                }

                // Whip sound effects
                float pitch = 0.8f + (float)(progress * 0.7);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, pitch);
            }

            private void createWhipTrail(Location start, Location end, double progress) {
                Vector direction = end.toVector().subtract(start.toVector());
                double distance = direction.length();
                direction.normalize();

                // Create trail particles
                for (double d = 0; d < distance; d += 0.5) {
                    Location trailLoc = start.clone().add(direction.clone().multiply(d));

                    // Love whip particles
                    Particle.DustOptions dustPink = new Particle.DustOptions(
                            Color.fromRGB(255, 192 - (int)(progress * 50), 203), 1.0f);
                    world.spawnParticle(Particle.DUST, trailLoc, 2, 0.1, 0.1, 0.1, 0, dustPink);

                    if (random.nextFloat() < 0.05) {
                        world.spawnParticle(Particle.HEART, trailLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Trail sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(end, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, 1.5f + (float)progress);
                }
            }

            private void checkWhipHits(Location center) {
                for (int i = 0; i < whipPoints.size(); i++) {
                    Vector whipDir = whipPoints.get(i);
                    double segmentProgress = i / (double)whipPoints.size();
                    double segmentLength = whipLength * (1 - segmentProgress * 0.3);

                    Location hitboxCenter = center.clone().add(whipDir.clone().multiply(segmentLength));

                    for (Entity entity : world.getNearbyEntities(hitboxCenter, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Calculate damage based on segment position
                            double damage = 10.0 * (1 - segmentProgress * 0.5); // More damage near the base

                            // Apply damage
                            target.damage(Math.max(5.0, damage), player);
                            hitEntities.add(target);

                            // Create hit effect
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Apply love effects
                            applyLoveEffects(target, whipDir);

                            // Reset hit tracking after delay
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    hitEntities.remove(target);
                                }
                            }.runTaskLater(plugin, 10L);
                        }
                    }
                }
            }

            private void applyLoveEffects(LivingEntity target, Vector direction) {
                // Emotional impact effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));

                // Whip knockback
                Vector knockback = direction.clone()
                        .multiply(0.8)
                        .setY(0.2);
                target.setVelocity(knockback);
            }

            private void createHitEffect(Location location) {
                // Love impact particles
                world.spawnParticle(Particle.HEART, location, 3, 0.2, 0.2, 0.2, 0);
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 162, 193), 2.0f);
                world.spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dustPink);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.6f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final love burst
                new BukkitRunnable() {
                    private double angle = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create spiral effect
                        double radius = 3.0 * (1 - ticks / (double)MAX_TICKS);
                        angle += 20;

                        for (int i = 0; i < 2; i++) {
                            double currentAngle = angle + (i * 180);
                            double radian = Math.toRadians(currentAngle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.5, z);

                            // Finishing particles
                            world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                            Particle.DustOptions dustPink = new Particle.DustOptions(
                                    Color.fromRGB(255, 192, 203), 1.0f);
                            world.spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0, dustPink);
                        }

                        // Fading sound
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.4f, 1.2f - (ticks * 0.03f));
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));

        // Add cooldown
      //  addCooldown(player, "SecondForm", 14);
    }

    /**
     * Third Form: Catlove Shower (参ノ型 恋猫時雨)
     * Created: 2025-06-16 17:23:16
     * @author SkyForce-6
     *
     * A graceful aerial technique that creates multiple arching love slashes
     */
    public void useThirdForm() {
        player.sendMessage("§d恋 §f参ノ型 恋猫時雨 §d(Third Form: Catlove Shower)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial leap sound
        world.playSound(startLoc, Sound.ENTITY_CAT_PURR, 1.0f, 1.2f);
        world.spawnParticle(Particle.HEART, startLoc.add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);

        Set<Entity> hitEntities = new HashSet<>();
        List<Vector> archPaths = new ArrayList<>();

        // Initial leap
        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(1.2));

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private int slashCount = 0;
            private final int MAX_SLASHES = 7;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Create new arch every interval
                if (time > 0.3 && slashCount < MAX_SLASHES && time % 0.3 < 0.05) {
                    createNewArch(currentLoc);
                    slashCount++;
                }

                // Update existing arches
                updateArches(currentLoc);

                time += 0.05;
            }

            private void createNewArch(Location location) {
                Vector baseDirection = location.getDirection();
                double archAngle = (slashCount - (MAX_SLASHES/2)) * 25; // Spread slashes
                Vector archDirection = rotateVector(baseDirection, archAngle);

                archPaths.add(archDirection);

                // Initial arch creation effects
                world.playSound(location, Sound.ENTITY_CAT_AMBIENT, 0.5f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
            }

            private void updateArches(Location center) {
                for (Vector archDir : archPaths) {
                    // Create arch pattern
                    for (double t = 0; t <= 1; t += 0.1) {
                        // Calculate arch position
                        double horizontalDist = 4.0 * t;
                        double height = Math.sin(t * Math.PI) * 2.0;

                        Vector archVector = archDir.clone().multiply(horizontalDist);
                        Location archLoc = center.clone().add(archVector.setY(height));

                        // Create arch particles
                        createArchEffects(archLoc, t);

                        // Check for hits
                        checkArchHits(archLoc);
                    }
                }
            }

            private void createArchEffects(Location location, double progress) {
                // Love arch particles
                Particle.DustOptions dustPink = new Particle.DustOptions(
                        Color.fromRGB(255, (int)(192 + progress * 30), 203), 1.0f);
                world.spawnParticle(Particle.DUST, location, 3, 0.1, 0.1, 0.1, 0, dustPink);

                if (random.nextFloat() < 0.1) {
                    world.spawnParticle(Particle.HEART, location, 1, 0.1, 0.1, 0.1, 0);
                }

                // Cat paw particles occasionally
                if (random.nextFloat() < 0.05) {
                    createCatPawEffect(location);
                }
            }

            private void createCatPawEffect(Location location) {
                // Create paw print pattern
                double size = 0.3;
                Vector[] pawPoints = {
                        new Vector(0, 0, 0),  // Center pad
                        new Vector(size, 0, size),  // Top right toe
                        new Vector(size, 0, -size), // Bottom right toe
                        new Vector(-size, 0, size), // Top left toe
                        new Vector(-size, 0, -size) // Bottom left toe
                };

                for (Vector point : pawPoints) {
                    Location pawLoc = location.clone().add(point);
                    Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 182, 193), 0.7f);
                    world.spawnParticle(Particle.DUST, pawLoc, 1, 0.05, 0.05, 0.05, 0, dustPink);
                }
            }

            private void checkArchHits(Location archLocation) {
                for (Entity entity : world.getNearbyEntities(archLocation, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(7.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply cat-love effects
                        applyCatLoveEffects(target);

                        // Reset hit tracking after delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                hitEntities.remove(target);
                            }
                        }.runTaskLater(plugin, 20L);
                    }
                }
            }

            private void applyCatLoveEffects(LivingEntity target) {
                // Apply playful effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

                // Random small knockback (like cat batting at toys)
                double angle = random.nextDouble() * Math.PI * 2;
                Vector knockback = new Vector(
                        Math.cos(angle) * 0.5,
                        0.3,
                        Math.sin(angle) * 0.5
                );
                target.setVelocity(knockback);
            }

            private void createHitEffect(Location location) {
                // Cat-love impact particles
                world.spawnParticle(Particle.HEART, location, 3, 0.2, 0.2, 0.2, 0);
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 162, 193), 2.0f);
                world.spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dustPink);

                // Cat sounds
                world.playSound(location, Sound.ENTITY_CAT_PURR, 0.6f, 1.5f);
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final cat-love shower
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double radius = 3.0 * (1 - ticks / (double)MAX_TICKS);

                        // Create raining hearts effect
                        for (int i = 0; i < 3; i++) {
                            double angle = random.nextDouble() * Math.PI * 2;
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location particleLoc = endLoc.clone().add(x, 2.0 - (ticks * 0.1), z);

                            // Falling particles
                            world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                            createCatPawEffect(particleLoc);
                        }

                        // Gentle cat sounds
                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_PURR, 0.3f, 1.0f + (ticks * 0.05f));
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 2));

        // Add cooldown
      //  addCooldown(player, "ThirdForm", 12);
    }

    /**
     * Fifth Form: Swaying Love, Wildclaw (伍ノ型 揺らめく恋情・乱れ爪)
     * Created: 2025-06-16 17:33:20
     * @author SkyForce-6
     *
     * An aerial technique creating a tornado of passionate love slashes
     */
    public void useFifthForm() {
        player.sendMessage("§d恋 §f伍ノ型 揺らめく恋情・乱れ爪 §d(Fifth Form: Swaying Love, Wildclaw)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial leap effect
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);
        world.spawnParticle(Particle.HEART, startLoc.add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);

        Set<Entity> hitEntities = new HashSet<>();
        List<Vector> tornadoPaths = new ArrayList<>();
        AtomicInteger slashCount = new AtomicInteger(0);

        // Launch player upward for somersault
        player.setVelocity(new Vector(0, 1.8, 0));

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 4.0;
            private double tornadoRadius = 1.0;
            private double tornadoHeight = 0;
            private final double MAX_HEIGHT = 6.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < 0.5) {
                    // Somersault phase
                    executeSomersault(currentLoc);
                } else {
                    // Tornado phase
                    executeTornadoSlashes(currentLoc);
                }

                time += 0.05;
            }

            private void executeSomersault(Location location) {
                // Rotate player during somersault
                float yaw = player.getLocation().getYaw() + 36; // 360 degrees over 10 ticks
                Location newLoc = player.getLocation();
                newLoc.setYaw(yaw);
                player.teleport(newLoc);

                // Somersault particles
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 192, 203), 1.0f);
                for (double angle = 0; angle < 360; angle += 30) {
                    double rad = Math.toRadians(angle);
                    double x = Math.cos(rad) * 1.0;
                    double z = Math.sin(rad) * 1.0;
                    Location particleLoc = location.clone().add(x, 0, z);

                    world.spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0, dustPink);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
            }

            private void executeTornadoSlashes(Location location) {
                // Update tornado properties
                tornadoRadius = 2.0 + Math.sin(time * 3) * 0.5;
                tornadoHeight = Math.min(MAX_HEIGHT, tornadoHeight + 0.2);

                // Create new slash paths
                if (time % 0.1 < 0.05 && slashCount.get() < 50) {
                    createSlashPath(location);
                    slashCount.incrementAndGet();
                }

                // Update and render tornado
                updateTornado(location);

                // Create rising wind effect
                createWindEffect(location);
            }

            private void createSlashPath(Location center) {
                double angle = random.nextDouble() * Math.PI * 2;
                Vector slashVector = new Vector(
                        Math.cos(angle) * tornadoRadius,
                        random.nextDouble() * 2 - 1, // Varying heights
                        Math.sin(angle) * tornadoRadius
                ).normalize();

                tornadoPaths.add(slashVector);

                // Slash creation sound
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 1.5f + random.nextFloat() * 0.5f);
            }

            private void updateTornado(Location center) {
                Iterator<Vector> pathIterator = tornadoPaths.iterator();
                int pathIndex = 0;

                while (pathIterator.hasNext()) {
                    Vector path = pathIterator.next();
                    pathIndex++;

                    // Rotate slash path
                    double rotationSpeed = 10 + (pathIndex % 3) * 5; // Varying rotation speeds
                    double newAngle = Math.atan2(path.getZ(), path.getX()) + Math.toRadians(rotationSpeed);
                    double height = (pathIndex / (double)tornadoPaths.size()) * tornadoHeight;

                    // Update path position
                    path.setX(Math.cos(newAngle) * tornadoRadius);
                    path.setZ(Math.sin(newAngle) * tornadoRadius);
                    path.setY(height);

                    // Create slash effects
                    createSlashEffects(center.clone().add(path), pathIndex);

                    // Check for hits
                    checkSlashHits(center.clone().add(path));
                }
            }

            private void createSlashEffects(Location location, int pathIndex) {
                // Create love slash particles
                Particle.DustOptions dustPink = new Particle.DustOptions(
                        Color.fromRGB(255, 162 + (pathIndex % 3) * 10, 193), 1.0f);
                world.spawnParticle(Particle.DUST, location, 3, 0.2, 0.2, 0.2, 0, dustPink);

                if (random.nextFloat() < 0.1) {
                    world.spawnParticle(Particle.HEART, location, 1, 0.1, 0.1, 0.1, 0);
                }

                // Create claw mark particles occasionally
                if (random.nextFloat() < 0.05) {
                    createClawMarkEffect(location);
                }
            }

            private void createClawMarkEffect(Location location) {
                // Create claw slash pattern
                double size = 0.5;
                Vector[] clawPoints = {
                        new Vector(0, 0, 0),
                        new Vector(size, 0, size),
                        new Vector(size, 0, -size),
                        new Vector(-size, 0, size),
                        new Vector(-size, 0, -size)
                };

                for (Vector point : clawPoints) {
                    Location clawLoc = location.clone().add(point);
                    Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 150, 180), 0.7f);
                    world.spawnParticle(Particle.DUST, clawLoc, 1, 0.05, 0.05, 0.05, 0, dustPink);
                }
            }

            private void createWindEffect(Location center) {
                for (int i = 0; i < 3; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = tornadoRadius * (0.5 + random.nextDouble() * 0.5);
                    double height = random.nextDouble() * tornadoHeight;

                    Location windLoc = center.clone().add(
                            Math.cos(angle) * radius,
                            height,
                            Math.sin(angle) * radius
                    );

                    // Wind particles
                    world.spawnParticle(Particle.CLOUD, windLoc, 1, 0.2, 0.2, 0.2, 0.05);
                }

                // Wind sounds
                if (random.nextFloat() < 0.1) {
                    world.playSound(center, Sound.ENTITY_PHANTOM_AMBIENT, 0.3f, 1.5f);
                }
            }

            private void checkSlashHits(Location slashLocation) {
                for (Entity entity : world.getNearbyEntities(slashLocation, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage based on height
                        double heightMultiplier = Math.min(1.5, 0.5 + (slashLocation.getY() - target.getLocation().getY()) / 3);
                        double damage = 6.0 * heightMultiplier;

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply tornado effects
                        applyTornadoEffects(target, slashLocation);

                        // Reset hit tracking after delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                hitEntities.remove(target);
                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }
            }

            private void applyTornadoEffects(LivingEntity target, Location slashLoc) {
                // Calculate tornado lift
                Vector toCenter = target.getLocation().toVector().subtract(slashLoc.toVector());
                toCenter.setY(0.3 + random.nextDouble() * 0.3); // Random upward force
                toCenter.normalize();

                // Apply tornado velocity
                target.setVelocity(toCenter.multiply(0.5));

                // Apply effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            }

            private void createHitEffect(Location location) {
                // Love impact particles
                world.spawnParticle(Particle.HEART, location, 3, 0.2, 0.2, 0.2, 0);
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 150, 180), 2.0f);
                world.spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dustPink);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final tornado dissipation
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;
                    private double currentRadius = tornadoRadius;
                    private double currentHeight = tornadoHeight;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Tornado collapse effect
                        currentRadius *= 0.9;
                        currentHeight *= 0.9;

                        for (int i = 0; i < 5; i++) {
                            double angle = random.nextDouble() * Math.PI * 2;
                            double height = random.nextDouble() * currentHeight;

                            Location particleLoc = endLoc.clone().add(
                                    Math.cos(angle) * currentRadius,
                                    height,
                                    Math.sin(angle) * currentRadius
                            );

                            // Dissipating particles
                            world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                            world.spawnParticle(Particle.CLOUD, particleLoc, 2, 0.2, 0.2, 0.2, 0.05);
                        }

                        // Fading sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4f, 1.5f - (ticks * 0.05f));
                            world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.3f, 1.2f - (ticks * 0.03f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));

        // Add cooldown
    //    addCooldown(player, "FifthForm", 20);
    }

    /**
     * Sixth Form: Cat-Legged Winds of Love (陸ノ型 猫足恋風)
     * Created: 2025-06-16 17:38:46
     * @author SkyForce-6
     *
     * A graceful technique creating rapid extending and retracting love slashes
     */

    private static class SlashLayer {
        private final Vector direction;
        private double extension;
        private boolean extending;
        private final double angle;

        public SlashLayer(Vector direction, double angle) {
            this.direction = direction;
            this.extension = 1.0;
            this.extending = true;
            this.angle = angle;
        }
    }

    public void useSixthForm() {
        player.sendMessage("§d恋 §f陸ノ型 猫足恋風 §d(Sixth Form: Cat-Legged Winds of Love)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stance effect
        world.playSound(startLoc, Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.2f);
        world.spawnParticle(Particle.HEART, startLoc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);

        Set<Entity> hitEntities = new HashSet<>();
        List<SlashLayer> slashLayers = new ArrayList<>();
        AtomicInteger currentLayer = new AtomicInteger(0);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private final int MAX_LAYERS = 6;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Create new slash layer
                if (currentLayer.get() < MAX_LAYERS && time % 0.4 < 0.05) {
                    createSlashLayer(currentLoc);
                    currentLayer.incrementAndGet();
                }

                // Update existing layers
                updateSlashLayers(currentLoc);

                time += 0.05;
            }

            private void createSlashLayer(Location location) {
                double baseAngle = (currentLayer.get() * 60) % 360; // Spread layers evenly
                Vector baseDirection = location.getDirection();

                // Create main slash
                slashLayers.add(new SlashLayer(baseDirection.clone(), baseAngle));

                // Create complementary slashes
                slashLayers.add(new SlashLayer(baseDirection.clone(), baseAngle + 30));
                slashLayers.add(new SlashLayer(baseDirection.clone(), baseAngle - 30));

                // Layer creation effects
                world.playSound(location, Sound.ENTITY_CAT_PURR, 0.6f, 1.0f + (currentLayer.get() * 0.1f));
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.2f);
            }

            private void updateSlashLayers(Location center) {
                for (SlashLayer layer : slashLayers) {
                    // Update extension
                    if (layer.extending) {
                        layer.extension = Math.min(layer.extension + 0.8, 5.0);
                        if (layer.extension >= 5.0) layer.extending = false;
                    } else {
                        layer.extension = Math.max(layer.extension - 0.8, 1.0);
                        if (layer.extension <= 1.0) layer.extending = true;
                    }

                    // Create slash effects
                    createLayerEffects(center, layer);

                    // Check for hits
                    checkLayerHits(center, layer);
                }
            }

            private void createLayerEffects(Location center, SlashLayer layer) {
                Vector rotated = rotateVector(layer.direction.clone(), layer.angle);

                // Create arc effect
                for (double progress = 0; progress <= 1; progress += 0.1) {
                    double currentExtension = layer.extension * progress;
                    Location arcLoc = center.clone().add(rotated.clone().multiply(currentExtension));

                    // Arc particles
                    Particle.DustOptions dustPink = new Particle.DustOptions(
                            Color.fromRGB(255, (int)(192 + layer.extension * 10), 203), 1.0f);
                    world.spawnParticle(Particle.DUST, arcLoc, 2, 0.1, 0.1, 0.1, 0, dustPink);

                    if (random.nextFloat() < 0.1) {
                        world.spawnParticle(Particle.HEART, arcLoc, 1, 0.1, 0.1, 0.1, 0);
                    }

                    // Cat paw trail
                    if (random.nextFloat() < 0.05) {
                        createPawPrintEffect(arcLoc);
                    }
                }

                // Extension/retraction sound
                if (random.nextFloat() < 0.2) {
                    float pitch = layer.extending ? 1.5f : 1.2f;
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, pitch);
                }
            }

            private void createPawPrintEffect(Location location) {
                double size = 0.2;
                Vector[] pawPoints = {
                        new Vector(0, 0, 0),      // Center pad
                        new Vector(size, 0, size), // Top right toe
                        new Vector(-size, 0, size), // Top left toe
                        new Vector(size, 0, -size), // Bottom right toe
                        new Vector(-size, 0, -size)  // Bottom left toe
                };

                for (Vector point : pawPoints) {
                    Location pawLoc = location.clone().add(point);
                    Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 182, 193), 0.5f);
                    world.spawnParticle(Particle.DUST, pawLoc, 1, 0.02, 0.02, 0.02, 0, dustPink);
                }
            }

            private void checkLayerHits(Location center, SlashLayer layer) {
                Vector rotated = rotateVector(layer.direction.clone(), layer.angle);
                Location hitLoc = center.clone().add(rotated.multiply(layer.extension));

                for (Entity entity : world.getNearbyEntities(hitLoc, 1.8, 1.8, 1.8)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on extension
                        double extensionMultiplier = layer.extension / 5.0;
                        double damage = 6.0 + (extensionMultiplier * 4.0);

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply cat-love effects
                        applyCatLoveEffects(target, rotated);

                        // Reset hit tracking after delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                hitEntities.remove(target);
                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }
            }

            private void applyCatLoveEffects(LivingEntity target, Vector direction) {
                // Apply playful effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 0));

                // Knockback based on slash direction
                Vector knockback = direction.clone()
                        .multiply(0.5)
                        .setY(0.2);
                target.setVelocity(knockback);
            }

            private void createHitEffect(Location location) {
                // Love impact particles
                world.spawnParticle(Particle.HEART, location, 3, 0.2, 0.2, 0.2, 0);
                Particle.DustOptions dustPink = new Particle.DustOptions(Color.fromRGB(255, 162, 193), 2.0f);
                world.spawnParticle(Particle.DUST, location, 10, 0.3, 0.3, 0.3, 0, dustPink);

                // Impact sounds
                world.playSound(location, Sound.ENTITY_CAT_AMBIENT, 0.6f, 1.5f);
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final love burst
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create dispersing effect
                        for (int i = 0; i < 3; i++) {
                            double angle = (ticks * 18) + (i * 120); // 360 degrees over 20 ticks
                            double radius = 3.0 * (1 - ticks / (double)MAX_TICKS);
                            double x = Math.cos(Math.toRadians(angle)) * radius;
                            double z = Math.sin(Math.toRadians(angle)) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.5, z);

                            // Finishing particles
                            world.spawnParticle(Particle.HEART, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                            createPawPrintEffect(particleLoc);
                        }

                        // Fading sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_PURR, 0.4f, 1.0f + (ticks * 0.05f));
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.3f, 1.5f - (ticks * 0.05f));
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));

        // Add cooldown
       // addCooldown(player, "SixthForm", 16);
    }
}