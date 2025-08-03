package org.skyforce.demon.breathings.serpentbreathing;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Serpent Breathing Implementation
 * Created: 2025-06-16 17:55:25
 * @author SkyForce-6
 */
public class SerpentBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final Random random;

    public SerpentBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    // Grundlegende Partikelfunktionen für schlangenartige Effekte
    private void createSerpentParticles(Location location, double size) {
        Particle.DustOptions dustGreen = new Particle.DustOptions(Color.fromRGB(50, 205, 50), 1.0f);
        World world = location.getWorld();

        // Schlangenähnliche Partikel
        for (double t = 0; t < Math.PI * 2; t += 0.2) {
            double x = Math.cos(t) * size;
            double z = Math.sin(t) * size;
            Location particleLoc = location.clone().add(x, 0, z);

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustGreen);
        }
    }

    // Schlangenbewegungspfad berechnen
    private List<Location> calculateSerpentPath(Location start, Vector direction, double length, double amplitude, double frequency) {
        List<Location> path = new ArrayList<>();
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        for (double t = 0; t < length; t += 0.5) {
            double wave = Math.sin(t * frequency) * amplitude;
            Location point = start.clone().add(direction.clone().multiply(t))
                    .add(right.clone().multiply(wave));
            path.add(point);
        }

        return path;
    }

    /**
     * Scale pattern creation for Serpent Breathing abilities
     * @param location The location to create the scale effect
     * @param progress The progress value (0.0 to 1.0) affecting the scale size
     */
    private void createScaleEffect(Location location, double progress) {
        double size = 0.3 * (1 - progress * 0.5);
        Particle.DustOptions scaleColor = new Particle.DustOptions(
                Color.fromRGB(100, 255, 100), 0.5f);

        // Create diamond-shaped scale pattern
        Vector[] scalePoints = {
                new Vector(0, 0, size),    // Top
                new Vector(size, 0, 0),    // Right
                new Vector(0, 0, -size),   // Bottom
                new Vector(-size, 0, 0)    // Left
        };

        World world = location.getWorld();
        for (Vector point : scalePoints) {
            Location scaleLoc = location.clone().add(point);
            world.spawnParticle(Particle.DUST, scaleLoc, 1, 0.02, 0.02, 0.02, 0, scaleColor);
        }
    }

    /**
     * First Form: Winding Serpent Slash (壱ノ型 委蛇斬り)
     * Created: 2025-06-16 18:06:26
     * @author SkyForce-6
     *
     * A winding horizontal slash that mimics a serpent's strike
     */
    public void useFirstForm(Player player) {
        this.player.sendMessage("§2蛇 §f壱ノ型 委蛇斬り §2(First Form: Winding Serpent Slash)");

        World world = this.player.getLocation().getWorld();
        Location startLoc = this.player.getLocation();

        // Initial serpent hiss
        world.playSound(startLoc, Sound.ENTITY_CAT_HISS, 1.0f, 1.2f);
        createSerpentParticles(startLoc.add(0, 1, 0), 0.5);

        Set<Entity> hitEntities = new HashSet<>();
        final AtomicReference<List<Location>> slashPathRef = new AtomicReference<>(new ArrayList<>());

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0;
            private double currentLength = 0;
            private final double MAX_LENGTH = 6.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = SerpentBreathingAbility.this.player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Extend slash path
                currentLength = Math.min(MAX_LENGTH, currentLength + 1.0);

                // Calculate winding path
                slashPathRef.set(calculateSerpentPath(
                        currentLoc,
                        direction,
                        currentLength,
                        0.8,  // Amplitude
                        2.0   // Frequency
                ));

                // Create slash effects
                createSlashEffects();

                // Check for hits
                checkSlashHits();

                time += 0.05;
            }

            private void createSlashEffects() {
                List<Location> path = slashPathRef.get();
                if (path.isEmpty()) return;

                // Create serpentine trail
                for (int i = 0; i < path.size(); i++) {
                    Location pathLoc = path.get(i);
                    double progress = i / (double)path.size();

                    // Main slash particles
                    Particle.DustOptions dustGreen = new Particle.DustOptions(
                            Color.fromRGB(50, 205 - (int)(progress * 50), 50), 1.0f);
                    world.spawnParticle(Particle.DUST, pathLoc, 3, 0.1, 0.1, 0.1, 0, dustGreen);

                    // Snake scale particles
                    if (random.nextFloat() < 0.3) {
                        createScaleEffect(pathLoc, progress);
                    }

                    // Trailing sound effects
                    if (random.nextFloat() < 0.1) {
                        world.playSound(pathLoc, Sound.ENTITY_CAT_HISS, 0.3f, 1.5f + (float)progress);
                    }
                }
            }

            private void checkSlashHits() {
                List<Location> path = slashPathRef.get();
                for (Location hitLoc : path) {
                    for (Entity entity : world.getNearbyEntities(hitLoc, 1.2, 1.2, 1.2)) {
                        if (entity instanceof LivingEntity && entity != SerpentBreathingAbility.this.player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Calculate damage based on distance
                            double distance = target.getLocation().distance(SerpentBreathingAbility.this.player.getLocation());
                            double damage = 8.0 * (1 - distance / (MAX_LENGTH + 2));

                            // Apply damage
                            target.damage(Math.max(4.0, damage), SerpentBreathingAbility.this.player);
                            hitEntities.add(target);

                            // Create hit effect
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Apply serpent effects
                            applySerpentEffects(target);

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

            private void applySerpentEffects(LivingEntity target) {
                // Apply venom-like effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));

                // Calculate knockback direction
                Vector knockback = target.getLocation().subtract(SerpentBreathingAbility.this.player.getLocation()).toVector()
                        .normalize()
                        .multiply(0.5)
                        .setY(0.2);
                target.setVelocity(knockback);
            }

            private void createHitEffect(Location location) {
                // Serpent impact particles
                Particle.DustOptions dustGreen = new Particle.DustOptions(Color.fromRGB(50, 255, 50), 2.0f);
                world.spawnParticle(Particle.DUST, location, 15, 0.3, 0.3, 0.3, 0, dustGreen);

                // Create serpent scales burst
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    double x = Math.cos(angle) * 0.5;
                    double z = Math.sin(angle) * 0.5;
                    Location scaleLoc = location.clone().add(x, 0, z);
                    createScaleEffect(scaleLoc, 0);
                }

                // Impact sounds
                world.playSound(location, Sound.ENTITY_CAT_HISS, 0.8f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = SerpentBreathingAbility.this.player.getLocation();

                // Final serpent effect
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double size = 1.0 * (1 - ticks / (double)MAX_TICKS);

                        // Create dissolving serpent pattern
                        for (double angle = 0; angle < 360; angle += 45) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * size;
                            double z = Math.sin(radian) * size;

                            Location particleLoc = endLoc.clone().add(x, 0.5, z);

                            // Dissolving particles
                            Particle.DustOptions dustGreen = new Particle.DustOptions(
                                    Color.fromRGB(50, 205, 50), 1.0f);
                            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, dustGreen);

                            if (random.nextFloat() < 0.3) {
                                createScaleEffect(particleLoc, ticks / (double)MAX_TICKS);
                            }
                        }

                        // Fading serpent sounds
                        if (ticks % 2 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_HISS, 0.4f, 1.2f - (ticks * 0.05f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1));
        this.player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0));

        // Add cooldown
       // addCooldown(player, "FirstForm", 8);
    }

    /**
     * Second Form: Venom Fangs of the Narrow Head (弐ノ型 狭頭の毒牙)
     * Created: 2025-06-17 15:30:38
     * @author SkyForce-6
     *
     * A lightning-fast strike from behind, mimicking a serpent's deadly bite
     */
    public void useSecondForm() {
        player.sendMessage("§2蛇 §f弐ノ型 狭頭の毒牙 §2(Second Form: Venom Fangs of the Narrow Head)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial serpent stance
        world.playSound(startLoc, Sound.ENTITY_CAT_HISS, 1.0f, 0.8f);
        createSerpentParticles(startLoc.add(0, 1, 0), 0.3);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicReference<Location> targetLocation = new AtomicReference<>(null);
        AtomicBoolean hasStruck = new AtomicBoolean(false);

        // Find nearest target within range
        Entity target = null;
        double minDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = player.getLocation().distance(entity.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    target = entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage("§c対象が見つかりません! (No target found!)");
            return;
        }

        final Entity finalTarget = target;
        final Location originalLocation = player.getLocation().clone();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0;
            private int phase = 0; // 0: dash behind, 1: strike
            private final Vector originalDirection = player.getLocation().getDirection().clone();

            @Override
            public void run() {
                if (time >= MAX_DURATION || (phase == 1 && hasStruck.get())) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                if (phase == 0) {
                    executeDashPhase();
                } else {
                    executeStrikePhase();
                }

                time += 0.05;
            }

            private void executeDashPhase() {
                // Calculate position behind target
                Location targetLoc = finalTarget.getLocation();
                Vector direction = targetLoc.getDirection().multiply(-1); // Behind target
                Location behindTarget = targetLoc.clone().add(direction.multiply(1.5));

                // Create dash trail
                Vector dashVector = behindTarget.toVector().subtract(player.getLocation().toVector());
                double distance = dashVector.length();
                dashVector.normalize();

                for (double d = 0; d < distance; d += 0.5) {
                    Location trailLoc = player.getLocation().clone().add(dashVector.clone().multiply(d));
                    createDashEffect(trailLoc, d / distance);
                }

                // Teleport player behind target
                player.teleport(behindTarget);
                targetLocation.set(behindTarget);

                // Transition to strike phase
                phase = 1;

                // Dash completion effect
                world.playSound(behindTarget, Sound.ENTITY_CAT_HISS, 0.8f, 1.5f);
            }

            private void createDashEffect(Location location, double progress) {
                // Serpentine dash particles
                Particle.DustOptions dustGreen = new Particle.DustOptions(
                        Color.fromRGB(50, 205 - (int)(progress * 50), 50), 0.7f);
                world.spawnParticle(Particle.DUST, location, 2, 0.1, 0.1, 0.1, 0, dustGreen);

                if (random.nextFloat() < 0.2) {
                    createScaleEffect(location, progress);
                }
            }

            private void executeStrikePhase() {
                Location strikeStart = targetLocation.get();
                if (strikeStart == null) return;

                // Calculate strike path
                Vector strikeDir = finalTarget.getLocation().subtract(strikeStart).toVector().normalize();
                double strikeDistance = 2.0;

                // Create fang effect
                createFangEffect(strikeStart, strikeDir, strikeDistance);

                // Check for hit
                checkStrikeHit(strikeStart, strikeDir, strikeDistance);
            }

            private void createFangEffect(Location start, Vector direction, double distance) {
                // Create two fang paths (upper and lower jaw)
                double jawSpread = 0.3;
                Vector up = new Vector(0, jawSpread, 0);
                Vector down = new Vector(0, -jawSpread, 0);

                for (double d = 0; d < distance; d += 0.2) {
                    Location centerPoint = start.clone().add(direction.clone().multiply(d));

                    // Upper fang
                    Location upperFang = centerPoint.clone().add(up);
                    createFangParticle(upperFang, true);

                    // Lower fang
                    Location lowerFang = centerPoint.clone().add(down);
                    createFangParticle(lowerFang, false);

                    // Venom effect
                    if (random.nextFloat() < 0.3) {
                        Particle.DustOptions venomGreen = new Particle.DustOptions(
                                Color.fromRGB(0, 255, 0), 0.5f);
                        world.spawnParticle(Particle.DUST, centerPoint, 2, 0.1, 0.1, 0.1, 0, venomGreen);
                    }
                }
            }

            private void createFangParticle(Location location, boolean isUpper) {
                Particle.DustOptions fangColor = new Particle.DustOptions(
                        Color.fromRGB(220, 220, 220), 0.8f);
                world.spawnParticle(Particle.DUST, location, 1, 0.02, 0.02, 0.02, 0, fangColor);

                // Venom drip effect
                if (random.nextFloat() < 0.2) {
                    Location dripLoc = location.clone().add(0, isUpper ? -0.1 : 0.1, 0);
                    Particle.DustOptions venomColor = new Particle.DustOptions(
                            Color.fromRGB(0, 255, 0), 0.3f);
                    world.spawnParticle(Particle.DUST, dripLoc, 1, 0.02, 0.02, 0.02, 0, venomColor);
                }
            }

            private void checkStrikeHit(Location start, Vector direction, double distance) {
                if (hasStruck.get()) return;

                // Create hitbox
                for (double d = 0; d < distance; d += 0.5) {
                    Location hitLoc = start.clone().add(direction.clone().multiply(d));

                    for (Entity entity : world.getNearbyEntities(hitLoc, 1.0, 1.0, 1.0)) {
                        if (entity == finalTarget && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply massive damage (headshot attempt)
                            target.damage(12.0, player);
                            hitEntities.add(entity);
                            hasStruck.set(true);

                            // Create hit effect
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Apply venom effects
                            applyVenomEffects(target);

                            break;
                        }
                    }
                }
            }

            private void applyVenomEffects(LivingEntity target) {
                // Apply strong venom effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

                // Disorientating effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
            }

            private void createHitEffect(Location location) {
                // Venom burst
                for (int i = 0; i < 20; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location particleLoc = location.clone().add(direction);
                    Particle.DustOptions venomGreen = new Particle.DustOptions(
                            Color.fromRGB(0, 255, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.02, 0.02, 0.02, 0, venomGreen);
                }

                // Fang impact effect
                createScaleEffect(location, 0);
                world.spawnParticle(Particle.CRIT, location, 10, 0.3, 0.3, 0.3, 0.2);

                // Impact sounds
                world.playSound(location, Sound.ENTITY_CAT_HISS, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Return to original position if no hit
                if (!hasStruck.get()) {
                    player.teleport(originalLocation);
                }

                // Final venom dissipation
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create dissolving venom effect
                        for (int i = 0; i < 3; i++) {
                            double angle = random.nextDouble() * Math.PI * 2;
                            double radius = 1.0 * (1 - ticks / (double)MAX_TICKS);
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.5, z);

                            // Dissolving particles
                            Particle.DustOptions venomGreen = new Particle.DustOptions(
                                    Color.fromRGB(0, 255, 0), 0.7f);
                            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, venomGreen);
                        }

                        // Fading serpent sounds
                        if (ticks % 2 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_HISS, 0.3f, 1.2f - (ticks * 0.05f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));

        // Add cooldown
     //   addCooldown(player, "SecondForm", 15);
    }

    /**
     * Third Form: Coil Choke (参ノ型 塒締め)
     * Created: 2025-06-17 15:35:34
     * @author SkyForce-6
     *
     * A constricting technique that creates a deadly spiral of slashes
     */
    public void useThirdForm() {
        player.sendMessage("§2蛇 §f参ノ型 塒締め §2(Third Form: Coil Choke)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial coiling sound
        world.playSound(startLoc, Sound.ENTITY_CAT_HISS, 1.0f, 0.7f);
        createSerpentParticles(startLoc.add(0, 1, 0), 0.5);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicReference<Entity> targetRef = new AtomicReference<>(null);
        AtomicInteger rotationCount = new AtomicInteger(0);

        // Find nearest target
        Entity target = null;
        double minDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = player.getLocation().distance(entity.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    target = entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage("§c対象が見つかりません! (No target found!)");
            return;
        }

        targetRef.set(target);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private double radius = 2.5;
            private double angle = 0;
            private double verticalOffset = 0;
            private boolean ascending = true;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Entity target = targetRef.get();
                if (target == null || !target.isValid()) {
                    this.cancel();
                    return;
                }

                // Update coil position
                updateCoilPosition(target.getLocation());

                // Create coil effects
                createCoilEffects(target.getLocation());

                // Check for hits
                checkCoilHits(target.getLocation());

                time += 0.05;
            }

            private void updateCoilPosition(Location targetLoc) {
                // Calculate new position
                angle += 18; // 360 degrees in 20 ticks
                if (angle >= 360) {
                    angle = 0;
                    rotationCount.incrementAndGet();
                }

                // Update vertical position
                if (ascending) {
                    verticalOffset = Math.min(verticalOffset + 0.1, 2.0);
                    if (verticalOffset >= 2.0) ascending = false;
                } else {
                    verticalOffset = Math.max(verticalOffset - 0.1, 0);
                    if (verticalOffset <= 0) ascending = true;
                }

                // Calculate new position
                double radian = Math.toRadians(angle);
                double x = Math.cos(radian) * radius;
                double z = Math.sin(radian) * radius;

                Location newLoc = targetLoc.clone().add(x, verticalOffset, z);
                player.teleport(newLoc);

                // Face target
                Vector direction = targetLoc.toVector().subtract(newLoc.toVector()).normalize();
                newLoc.setDirection(direction);
                player.teleport(newLoc);

                // Gradually decrease radius
                radius = Math.max(1.5, radius - 0.02);
            }

            private void createCoilEffects(Location targetLoc) {
                // Create coil trail
                double trailAngle = angle - 18; // Previous position
                double radian = Math.toRadians(trailAngle);
                double x = Math.cos(radian) * radius;
                double z = Math.sin(radian) * radius;

                Location trailLoc = targetLoc.clone().add(x, verticalOffset, z);
                Vector direction = player.getLocation().toVector()
                        .subtract(trailLoc.toVector()).normalize();

                // Create serpentine trail
                for (double d = 0; d < 1.0; d += 0.2) {
                    Location particleLoc = trailLoc.clone().add(direction.clone().multiply(d));

                    // Trail particles
                    Particle.DustOptions dustGreen = new Particle.DustOptions(
                            Color.fromRGB(50, 205, 50), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 3, 0.1, 0.1, 0.1, 0, dustGreen);

                    if (random.nextFloat() < 0.3) {
                        createScaleEffect(particleLoc, d);
                    }
                }

                // Constricting effect
                if (random.nextFloat() < 0.2) {
                    createConstrictingEffect(targetLoc);
                }

                // Movement sounds
                if (random.nextFloat() < 0.1) {
                    world.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 0.4f, 1.2f);
                }
            }

            private void createConstrictingEffect(Location center) {
                double ringRadius = radius + 0.5;
                for (double a = 0; a < 360; a += 45) {
                    double rad = Math.toRadians(a);
                    double x = Math.cos(rad) * ringRadius;
                    double z = Math.sin(rad) * ringRadius;

                    Location particleLoc = center.clone().add(x, verticalOffset, z);

                    // Constricting particles
                    Particle.DustOptions constrictGreen = new Particle.DustOptions(
                            Color.fromRGB(0, 155, 0), 0.7f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.05, 0.05, 0.05, 0, constrictGreen);
                }
            }

            private void checkCoilHits(Location targetLoc) {
                for (Entity entity : world.getNearbyEntities(targetLoc, radius, 3.0, radius)) {
                    if (entity == targetRef.get() && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on rotations
                        double rotationMultiplier = Math.min(2.0, 1.0 + (rotationCount.get() * 0.2));
                        double damage = 3.0 * rotationMultiplier;

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(entity);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, verticalOffset, 0));

                        // Apply constricting effects
                        applyConstrictingEffects(target);

                        // Reset hit tracking after delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                hitEntities.remove(entity);
                            }
                        }.runTaskLater(plugin, 5L);
                    }
                }
            }

            private void applyConstrictingEffects(LivingEntity target) {
                // Apply constricting effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, rotationCount.get()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, rotationCount.get()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));

                // Pull target slightly
                Vector pull = player.getLocation().toVector()
                        .subtract(target.getLocation().toVector())
                        .normalize()
                        .multiply(0.2);
                target.setVelocity(pull);
            }

            private void createHitEffect(Location location) {
                // Constricting particles
                for (int i = 0; i < 8; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location particleLoc = location.clone().add(direction);
                    Particle.DustOptions constrictGreen = new Particle.DustOptions(
                            Color.fromRGB(0, 155, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, constrictGreen);
                }

                // Impact particles
                world.spawnParticle(Particle.CRIT, location, 5, 0.2, 0.2, 0.2, 0.1);

                // Hit sounds
                world.playSound(location, Sound.ENTITY_CAT_HISS, 0.6f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 1.0f);
            }

            private void createFinishingEffect() {
                Location endLoc = targetRef.get().getLocation();

                // Final constricting burst
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;
                    private double currentRadius = radius;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Spiral collapse effect
                        double spiralRadius = currentRadius * (1 - ticks / (double)MAX_TICKS);
                        int points = 8;
                        for (int i = 0; i < points; i++) {
                            double angle = (360.0 / points * i) + (ticks * 18);
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * spiralRadius;
                            double z = Math.sin(radian) * spiralRadius;

                            Location particleLoc = endLoc.clone().add(x, verticalOffset * (1 - ticks / (double)MAX_TICKS), z);

                            // Collapsing particles
                            Particle.DustOptions constrictGreen = new Particle.DustOptions(
                                    Color.fromRGB(0, 155, 0), 0.7f);
                            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, constrictGreen);

                            if (random.nextFloat() < 0.3) {
                                createScaleEffect(particleLoc, ticks / (double)MAX_TICKS);
                            }
                        }

                        // Collapsing sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_HISS, 0.4f, 0.7f + (ticks * 0.03f));
                        }

                        currentRadius *= 0.9;
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));

        // Add cooldown
   //     addCooldown(player, "ThirdForm", 18);
    }

    /**
     * Fourth Form: Twin-Headed Reptile (肆ノ型 頸蛇双生)
     * Created: 2025-06-17 15:39:18
     * @author SkyForce-6
     *
     * A lethal forward leap attack with a horizontal twin-headed slash
     */
    public void useFourthForm() {
        player.sendMessage("§2蛇 §f肆ノ型 頸蛇双生 §2(Fourth Form: Twin-Headed Reptile)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stance effect
        world.playSound(startLoc, Sound.ENTITY_CAT_HISS, 1.0f, 1.2f);
        createSerpentParticles(startLoc.add(0, 1, 0), 0.5);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasLeaped = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private int phase = 0; // 0: preparation, 1: leap, 2: slash
            private List<Location> leftHeadPath = new ArrayList<>();
            private List<Location> rightHeadPath = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                switch (phase) {
                    case 0:
                        prepareLeap();
                        break;
                    case 1:
                        executeLeap();
                        break;
                    case 2:
                        executeSlash();
                        break;
                }

                time += 0.05;
            }

            private void prepareLeap() {
                if (time >= 0.2) {
                    phase = 1;
                    return;
                }

                // Preparation particles
                Location prepLoc = player.getLocation();
                createTwinHeadEffect(prepLoc, 0.3, true);

                // Coiling sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(prepLoc, Sound.ENTITY_CAT_HISS, 0.5f, 0.8f);
                }
            }

            private void executeLeap() {
                if (!hasLeaped.get()) {
                    // Launch player forward
                    Vector direction = player.getLocation().getDirection().multiply(1.5).setY(0.2);
                    player.setVelocity(direction);
                    hasLeaped.set(true);

                    // Leap sound
                    world.playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 1.0f, 1.5f);
                }

                // Create leap trail
                Location leapLoc = player.getLocation();
                createLeapEffect(leapLoc);

                if (time >= 0.5) {
                    phase = 2;
                    // Calculate twin head paths
                    calculateTwinHeadPaths();
                }
            }

            private void executeSlash() {
                Location slashLoc = player.getLocation();

                // Update and render twin head paths
                updateTwinHeadPaths();

                // Check for hits
                checkSlashHits();
            }

            private void createTwinHeadEffect(Location location, double size, boolean preparing) {
                // Create two serpent head effects
                double offset = preparing ? size * Math.sin(time * 20) : size;
                Vector right = new Vector(-location.getDirection().getZ(), 0, location.getDirection().getX()).normalize();

                Location leftHead = location.clone().add(right.clone().multiply(-offset));
                Location rightHead = location.clone().add(right.clone().multiply(offset));

                // Create head particles
                Particle.DustOptions headColor = new Particle.DustOptions(Color.fromRGB(0, 200, 0), 1.0f);
                Particle.DustOptions eyeColor = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 0.5f);

                for (Location headLoc : Arrays.asList(leftHead, rightHead)) {
                    // Head shape
                    world.spawnParticle(Particle.DUST, headLoc, 3, 0.1, 0.1, 0.1, 0, headColor);

                    // Eyes
                    Vector forward = headLoc.getDirection().multiply(0.2);
                    Location eyeLoc = headLoc.clone().add(forward);
                    world.spawnParticle(Particle.DUST, eyeLoc, 1, 0.02, 0.02, 0.02, 0, eyeColor);

                    // Fang effect
                    if (random.nextFloat() < 0.3) {
                        Location fangLoc = headLoc.clone().add(forward.multiply(1.2));
                        Particle.DustOptions fangColor = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 0.5f);
                        world.spawnParticle(Particle.DUST, fangLoc, 2, 0.02, 0.02, 0.02, 0, fangColor);
                    }
                }
            }

            private void createLeapEffect(Location location) {
                // Create rushing wind effect
                Vector direction = location.getDirection();
                for (int i = 0; i < 3; i++) {
                    Location trailLoc = location.clone().subtract(direction.clone().multiply(i * 0.5));

                    // Trail particles
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(50, 205 - (i * 30), 50), 0.8f);
                    world.spawnParticle(Particle.DUST, trailLoc, 2, 0.1, 0.1, 0.1, 0, trailColor);

                    if (random.nextFloat() < 0.2) {
                        createScaleEffect(trailLoc, i / 3.0);
                    }
                }
            }

            private void calculateTwinHeadPaths() {
                Location center = player.getLocation();
                Vector direction = center.getDirection();
                Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                // Calculate paths for both heads
                for (double d = 0; d < 3.0; d += 0.2) {
                    // Left head path
                    Location leftLoc = center.clone().add(right.clone().multiply(-0.8))
                            .add(direction.clone().multiply(d));
                    leftHeadPath.add(leftLoc);

                    // Right head path
                    Location rightLoc = center.clone().add(right.clone().multiply(0.8))
                            .add(direction.clone().multiply(d));
                    rightHeadPath.add(rightLoc);
                }
            }

            private void updateTwinHeadPaths() {
                // Render and update both head paths
                for (int i = 0; i < leftHeadPath.size(); i++) {
                    double progress = i / (double)leftHeadPath.size();

                    // Update left head
                    Location leftLoc = leftHeadPath.get(i);
                    createHeadPathEffect(leftLoc, progress, true);

                    // Update right head
                    Location rightLoc = rightHeadPath.get(i);
                    createHeadPathEffect(rightLoc, progress, false);
                }
            }

            private void createHeadPathEffect(Location location, double progress, boolean isLeft) {
                // Create serpentine path effect
                Particle.DustOptions pathColor = new Particle.DustOptions(
                        Color.fromRGB(50, 205 - (int)(progress * 50), 50), 1.0f);
                world.spawnParticle(Particle.DUST, location, 2, 0.1, 0.1, 0.1, 0, pathColor);

                // Create fang trails
                if (random.nextFloat() < 0.2) {
                    Particle.DustOptions fangColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 255), 0.5f);
                    Location fangLoc = location.clone().add(0, 0.2 * (isLeft ? 1 : -1), 0);
                    world.spawnParticle(Particle.DUST, fangLoc, 1, 0.02, 0.02, 0.02, 0, fangColor);
                }
            }

            private void checkSlashHits() {
                Set<Location> hitLocations = new HashSet<>();
                hitLocations.addAll(leftHeadPath);
                hitLocations.addAll(rightHeadPath);

                for (Location hitLoc : hitLocations) {
                    for (Entity entity : world.getNearbyEntities(hitLoc, 1.0, 1.0, 1.0)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply twin-strike damage
                            target.damage(10.0, player);
                            hitEntities.add(entity);

                            // Create hit effect
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Apply twin venom effects
                            applyTwinVenomEffects(target);
                        }
                    }
                }
            }

            private void applyTwinVenomEffects(LivingEntity target) {
                // Apply double venom effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));

                // Apply stunning effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

                // Apply twin-strike knockback
                Vector knockback = player.getLocation().getDirection()
                        .multiply(0.8)
                        .setY(0.2);
                target.setVelocity(knockback);
            }

            private void createHitEffect(Location location) {
                // Twin strike particles
                for (int i = 0; i < 2; i++) {
                    double offset = (i == 0) ? -0.5 : 0.5;
                    Location strikeLocation = location.clone().add(offset, 0, offset);

                    // Strike burst
                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(0, 255, 0), 1.5f);
                    world.spawnParticle(Particle.DUST, strikeLocation, 10, 0.2, 0.2, 0.2, 0, burstColor);

                    // Fang particles
                    world.spawnParticle(Particle.CRIT, strikeLocation, 5, 0.1, 0.1, 0.1, 0.2);
                }

                // Strike sounds
                world.playSound(location, Sound.ENTITY_CAT_HISS, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final twin-serpent dissipation
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create dissolving twin-head effect
                        createTwinHeadEffect(endLoc,
                                1.0 * (1 - ticks / (double)MAX_TICKS),
                                false);

                        // Dissolving sounds
                        if (ticks % 2 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_HISS,
                                    0.4f, 1.5f - (ticks * 0.1f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1));

        // Add cooldown
      //  addCooldown(player, "FourthForm", 12);
    }

    /**
     * Fifth Form: Slithering Serpent (伍ノ型 蜿蜿長蛇)
     * Created: 2025-06-17 15:42:50
     * @author SkyForce-6
     *
     * A multi-target attack that curves and winds like a serpent to strike multiple foes
     */
    public void useFifthForm() {
        player.sendMessage("§2蛇 §f伍ノ型 蜿蜿長蛇 §2(Fifth Form: Slithering Serpent)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial serpent manifestation
        world.playSound(startLoc, Sound.ENTITY_CAT_HISS, 1.0f, 0.6f);
        createSerpentParticles(startLoc.add(0, 1, 0), 0.8);

        Set<Entity> hitEntities = new HashSet<>();
        List<Vector> curvePoints = new ArrayList<>();
        AtomicInteger hitCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.5;
            private Location lastPosition;
            private Vector currentDirection;
            private double currentCurve = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                if (lastPosition == null) {
                    lastPosition = player.getLocation();
                    currentDirection = player.getLocation().getDirection();
                }

                // Update movement
                updateMovement();

                // Create slithering effects
                createSlitherEffects();

                // Check for hits
                checkSlitherHits();

                time += 0.05;
            }

            private void updateMovement() {
                // Calculate new direction with sinusoidal curve
                currentCurve += 0.2;
                double angleChange = Math.sin(currentCurve) * 30; // Max 30 degree curve

                // Rotate current direction
                Vector right = new Vector(-currentDirection.getZ(), 0, currentDirection.getX()).normalize();
                currentDirection = rotateVector(currentDirection, angleChange);

                // Calculate new position
                Vector movement = currentDirection.clone().multiply(0.8);
                Location newPosition = lastPosition.clone().add(movement);

                // Store curve point
                curvePoints.add(movement);

                // Update player position
                player.teleport(newPosition);
                lastPosition = newPosition;

                // Add vertical oscillation
                double verticalOffset = Math.sin(time * 4) * 0.3;
                player.teleport(newPosition.add(0, verticalOffset, 0));
            }

            private void createSlitherEffects() {
                // Create main serpent body
                for (int i = 0; i < curvePoints.size(); i++) {
                    double progress = i / (double)curvePoints.size();
                    Location pointLoc = lastPosition.clone().subtract(
                            curvePoints.get(curvePoints.size() - 1 - i).multiply(progress)
                    );

                    // Body particles
                    Particle.DustOptions bodyColor = new Particle.DustOptions(
                            Color.fromRGB(50, 205 - (int)(progress * 100), 50), 1.2f);
                    world.spawnParticle(Particle.DUST, pointLoc, 3, 0.2, 0.2, 0.2, 0, bodyColor);

                    // Scales effect
                    if (random.nextFloat() < 0.3) {
                        createScaleEffect(pointLoc, progress);
                    }

                    // Slithering sound
                    if (random.nextFloat() < 0.1) {
                        world.playSound(pointLoc, Sound.ENTITY_CAT_HISS, 0.3f, 1.2f + (float)progress);
                    }
                }

                // Create cutting edge effect
                Location edgeLoc = player.getLocation();
                Vector edge = currentDirection.clone().multiply(1.5);
                for (double d = 0; d < 1.0; d += 0.2) {
                    Location slashLoc = edgeLoc.clone().add(edge.clone().multiply(d));

                    // Blade particles
                    Particle.DustOptions bladeColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 220), 0.8f);
                    world.spawnParticle(Particle.DUST, slashLoc, 2, 0.1, 0.1, 0.1, 0, bladeColor);
                }
            }

            private void checkSlitherHits() {
                double hitRadius = 2.5;
                for (Entity entity : world.getNearbyEntities(player.getLocation(), hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on hit count
                        double baseAmount = 7.0;
                        double multiplier = Math.max(0.6, 1.0 - (hitCount.get() * 0.1)); // Diminishing returns
                        double damage = baseAmount * multiplier;

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(entity);
                        hitCount.incrementAndGet();

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply serpent effects
                        applySlitherEffects(target);
                    }
                }
            }

            private void applySlitherEffects(LivingEntity target) {
                // Apply slithering effects
                int amplifier = Math.min(2, hitCount.get() / 2); // Scales with hits, max level 2

                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, amplifier));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, amplifier));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, amplifier));

                // Calculate knockback based on curve
                Vector knockback = currentDirection.clone()
                        .multiply(0.5)
                        .add(new Vector(0, 0.2, 0));
                target.setVelocity(knockback);

                // Reset hit tracking after delay
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hitEntities.remove(target);
                    }
                }.runTaskLater(plugin, 10L);
            }

            private void createHitEffect(Location location) {
                // Decapitation effect
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 1, 0.2, 0.2, 0.2, 0);

                // Blood effect
                Particle.DustOptions bloodColor = new Particle.DustOptions(
                        Color.fromRGB(180, 0, 0), 1.0f);
                for (int i = 0; i < 8; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location spreadLoc = location.clone().add(spread);
                    world.spawnParticle(Particle.DUST, spreadLoc, 2, 0.1, 0.1, 0.1, 0, bloodColor);
                }

                // Impact sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
                world.playSound(location, Sound.ENTITY_CAT_HISS, 0.8f, 1.0f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final serpent dissolution
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create dissolving serpent effect
                        double radius = 2.0 * (1 - ticks / (double)MAX_TICKS);
                        int points = 12;

                        for (int i = 0; i < points; i++) {
                            double angle = (360.0 / points * i) + (ticks * 18);
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.5 * (1 - ticks / (double)MAX_TICKS), z);

                            // Dissolving particles
                            Particle.DustOptions dissolveColor = new Particle.DustOptions(
                                    Color.fromRGB(50, 205 - (ticks * 8), 50), 1.0f);
                            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, dissolveColor);

                            if (random.nextFloat() < 0.3) {
                                createScaleEffect(particleLoc, ticks / (double)MAX_TICKS);
                            }
                        }

                        // Fading sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_CAT_HISS, 0.4f, 0.8f + (ticks * 0.02f));
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 1));

        // Add cooldown
        //addCooldown(player, "FifthForm", 16);
    }
}