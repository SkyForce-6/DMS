package org.skyforce.demon.breathings.soundbreathing;

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

/**
 * Sound Breathing Implementation
 * Created: 2025-06-16 15:40:24
 * @author SkyForce-6
 */
public class SoundBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final Random random;

    public SoundBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    /**
     * First Form: Roar (壱ノ型 轟)
     * Created: 2025-06-16 16:51:46
     * @author SkyForce-6
     *
     * A powerful downward strike that creates a thunderous roar, stunning and damaging enemies
     */
    public void useFirstForm() {
        player.sendMessage("§8音 §f壱ノ型 轟 §8(First Form: Roar)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial sound buildup
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_BASS, 2.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 2.0f, 0.7f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasStruck = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private final double STRIKE_TIME = 0.4;
            private Location strikeLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < STRIKE_TIME) {
                    executeWindup(currentLoc);
                } else if (!hasStruck.get()) {
                    executeStrike(currentLoc);
                    hasStruck.set(true);
                    strikeLocation = currentLoc.clone();
                } else {
                    createSoundwaveEffects(strikeLocation);
                }

                time += 0.05;
            }

            private void executeWindup(Location location) {
                // Create charging sound effect
                double progress = time / STRIKE_TIME;
                float pitch = 0.5f + (float)(progress * 0.5);

                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, pitch);

                // Rising particle effect
                for (double angle = 0; angle < 360; angle += 30) {
                    double radian = Math.toRadians(angle);
                    double radius = 1.0 + progress;
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location particleLoc = location.clone().add(x, progress * 2, z);

                    // Sound wave particles
                    world.spawnParticle(Particle.NOTE, particleLoc, 1, 0, 0, 0, 1);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            private void executeStrike(Location location) {
                // Thunder strike effect
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 1.5f, 0.5f);

                // Visual effects
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.SONIC_BOOM, location, 10, 0.5, 0.5, 0.5, 0);

                // Initial shockwave
                for (double radius = 0; radius <= 5; radius += 0.5) {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location waveLoc = location.clone().add(x, 0.1, z);
                        world.spawnParticle(Particle.NOTE, waveLoc, 1, 0, 0, 0, 1);
                        world.spawnParticle(Particle.END_ROD, waveLoc, 1, 0, 0, 0, 0.05);
                    }
                }

                // Check for hits
                checkStrikeHits(location);
            }

            private void createSoundwaveEffects(Location center) {
                double progress = (time - STRIKE_TIME) / (MAX_DURATION - STRIKE_TIME);
                double radius = 5.0 + (progress * 5.0);

                for (double angle = 0; angle < 360; angle += 20) {
                    double radian = Math.toRadians(angle + (time * 30));
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location waveLoc = center.clone().add(x, 0.1 + Math.sin(time * 5) * 0.5, z);

                    // Soundwave particles
                    world.spawnParticle(Particle.NOTE, waveLoc, 1, 0, 0, 0, 1);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, waveLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Continuous sound effects
                if (random.nextFloat() < 0.2) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f + (float)progress);
                }

                // Check for additional hits
                checkWaveHits(center, radius);
            }

            private void checkStrikeHits(Location center) {
                for (Entity entity : world.getNearbyEntities(center, 5.0, 3.0, 5.0)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double distance = target.getLocation().distance(center);
                        double damage = 15.0 * (1 - distance / 6.0);

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

                        // Apply stun effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    }
                }
            }

            private void checkWaveHits(Location center, double radius) {
                for (Entity entity : world.getNearbyEntities(center, radius, 3.0, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Reduced damage for soundwave hits
                        double distance = target.getLocation().distance(center);
                        double damage = 8.0 * (1 - distance / (radius + 2));

                        // Apply damage
                        target.damage(Math.max(4.0, damage), player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply weaker effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact particles
                world.spawnParticle(Particle.SONIC_BOOM, location, 1, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.NOTE, location, 5, 0.3, 0.3, 0.3, 1);
                world.spawnParticle(Particle.END_ROD, location, 8, 0.3, 0.3, 0.3, 0.05);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 0.7f);
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 0.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final soundwave dispersion
                new BukkitRunnable() {
                    private double radius = 10.0;
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
                            double radian = Math.toRadians(angle + (ticks * 5));
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, 0.1, z);

                            world.spawnParticle(Particle.NOTE, particleLoc, 1, 0, 0, 0, 1);
                        }

                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.4f, 0.5f + (ticks * 0.02f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));

        // Add cooldown
      //  addCooldown(player, "FirstForm", 12);
    }

    /**
     * Fourth Form: Constant Resounding Slashes (肆ノ型 響斬無間)
     * Created: 2025-06-16 16:55:55
     * @author SkyForce-6
     *
     * A rapid spinning defensive technique that creates destructive sound waves
     */
    public void useFourthForm() {
        player.sendMessage("§8音 §f肆ノ型 響斬無間 §8(Fourth Form: Constant Resounding Slashes)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial spin-up sound
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 6.0;
            private double rotation = 0;
            private double radius = 2.0;
            private int slashCount = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Execute spinning slashes
                executeSpinningSlashes(currentLoc);

                // Create defensive sound barrier
                createSoundBarrier(currentLoc);

                // Check for hits
                checkDefensiveHits(currentLoc);

                // Update rotation
                rotation += 20; // Degrees per tick
                time += 0.05;
            }

            private void executeSpinningSlashes(Location location) {
                // Create two blade positions (twin swords)
                for (int i = 0; i < 2; i++) {
                    double offsetAngle = i * 180; // Opposite sides
                    double currentAngle = Math.toRadians(rotation + offsetAngle);

                    // Blade positions
                    for (double r = 0; r < radius; r += 0.2) {
                        double x = Math.cos(currentAngle) * r;
                        double z = Math.sin(currentAngle) * r;

                        Location bladeLoc = location.clone().add(x, 1, z);

                        // Blade particles
                        world.spawnParticle(Particle.SWEEP_ATTACK, bladeLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.NOTE, bladeLoc, 1, 0, 0, 0, 1);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, bladeLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                    }

                    // Create sound slice effect
                    if (time % 0.2 < 0.05) {
                        createSoundSlice(location, currentAngle);
                    }
                }

                // Spinning sound effects
                if (random.nextFloat() < 0.2) {
                    float pitch = 1.0f + (float)(time * 0.1);
                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, Math.min(2.0f, pitch));
                }
            }

            private void createSoundSlice(Location location, double angle) {
                double sliceLength = radius + 1.0;

                // Create sound slice projectile
                for (double d = 0; d < sliceLength; d += 0.2) {
                    double x = Math.cos(angle) * d;
                    double z = Math.sin(angle) * d;

                    Location sliceLoc = location.clone().add(x, 1, z);

                    // Slice particles
                    world.spawnParticle(Particle.SONIC_BOOM, sliceLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.NOTE, sliceLoc, 2, 0.1, 0.1, 0.1, 1);
                }

                // Slice sound
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.2f);

                slashCount++;
            }

            private void createSoundBarrier(Location location) {
                double barrierHeight = 3.0;

                // Create vertical sound barrier
                for (double y = 0; y < barrierHeight; y += 0.3) {
                    double angle = Math.toRadians(rotation * 0.5 + (y * 30));

                    for (double a = 0; a < 360; a += 20) {
                        double radian = Math.toRadians(a);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location barrierLoc = location.clone().add(x, y, z);

                        // Barrier particles
                        world.spawnParticle(Particle.NOTE, barrierLoc, 1, 0, 0, 0, 1);

                        if (random.nextFloat() < 0.1) {
                            world.spawnParticle(Particle.END_ROD, barrierLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }
                }

                // Barrier sound
                if (random.nextFloat() < 0.15) {
                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.4f, 1.0f + (float)(Math.sin(time * 2) * 0.5));
                }
            }

            private void checkDefensiveHits(Location center) {
                for (Entity entity : world.getNearbyEntities(center, radius + 1, 3.0, radius + 1)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on slash count
                        double baseDamage = 4.0;
                        double slashBonus = Math.min(4.0, slashCount * 0.2);

                        // Apply damage
                        target.damage(baseDamage + slashBonus, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback and effects
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(1.5)
                                .setY(0.2);
                        target.setVelocity(knockback);

                        // Apply sound-based effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));

                        // Reset hit count after a delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                hitEntities.remove(target);
                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact particles
                world.spawnParticle(Particle.SONIC_BOOM, location, 1, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.NOTE, location, 5, 0.3, 0.3, 0.3, 1);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.2, 0.2, 0.2, 0);

                // Impact sounds
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.0f);
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final sound explosion
                new BukkitRunnable() {
                    private double expansionRadius = radius;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        expansionRadius += 0.3;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * expansionRadius;
                            double z = Math.sin(radian) * expansionRadius;

                            Location particleLoc = endLoc.clone().add(x, 0.1 + (Math.sin(radian) * 0.5), z);

                            // Dispersing sound particles
                            world.spawnParticle(Particle.NOTE, particleLoc, 1, 0, 0, 0, 1);

                            if (random.nextFloat() < 0.2) {
                                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                        }

                        // Fading sound
                        if (ticks % 4 == 0) {
                            float pitch = 1.5f - (ticks * 0.05f);
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4f, pitch);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1));

        // Add cooldown
     //   addCooldown(player, "FourthForm", 18);
    }

    /**
     * Fifth Form: String Performance (伍ノ型 弦楽演奏)
     * Created: 2025-06-16 17:00:04
     * @author SkyForce-6
     *
     * A graceful technique that creates musical sound waves in rapid succession
     */
    public void useFifthForm() {
        player.sendMessage("§8音 §f伍ノ型 弦楽演奏 §8(Fifth Form: String Performance)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial string resonance
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        world.playSound(startLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();
        List<Vector> soundWaves = new ArrayList<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 5.0;
            private int noteIndex = 0;
            private final float[] musicalScale = {0.5f, 0.6f, 0.8f, 1.0f, 1.2f, 1.5f, 1.8f, 2.0f}; // Musical scale pitches

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Create sound waves
                if (time % 0.2 < 0.05) {
                    createSoundWave(currentLoc);
                }

                // Update existing sound waves
                updateSoundWaves(currentLoc);

                // Create musical effects
                createMusicalEffects(currentLoc);

                time += 0.05;
            }

            private void createSoundWave(Location location) {
                Vector direction = location.getDirection();
                soundWaves.add(direction.clone());

                // Play note from scale
                float pitch = musicalScale[noteIndex % musicalScale.length];
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, pitch);

                if (random.nextFloat() < 0.5) {
                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, pitch * 1.2f);
                }

                noteIndex++;

                // Create initial wave effect
                for (double angle = -30; angle <= 30; angle += 10) {
                    Vector waveDirect = rotateVector(direction.clone(), angle);

                    for (double d = 0; d < 3; d += 0.5) {
                        Location waveLoc = location.clone().add(waveDirect.clone().multiply(d));

                        // Musical note particles
                        world.spawnParticle(Particle.NOTE, waveLoc, 1, 0, 0, 0, 1);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, waveLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }
                }
            }

            private void updateSoundWaves(Location center) {
                Iterator<Vector> waveIterator = soundWaves.iterator();
                double speed = 0.8;

                while (waveIterator.hasNext()) {
                    Vector wave = waveIterator.next();

                    // Update wave position
                    wave.multiply(1 + speed * 0.1);

                    // Create wave particles
                    for (double angle = -30; angle <= 30; angle += 10) {
                        Vector waveDir = rotateVector(wave.clone().normalize(), angle);
                        double distance = wave.length();

                        Location waveLoc = center.clone().add(waveDir.multiply(distance));

                        // Wave particles
                        world.spawnParticle(Particle.NOTE, waveLoc, 1, 0.1, 0.1, 0.1, 1);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, waveLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }

                    // Check for hits
                    checkWaveHits(center, wave);

                    // Remove old waves
                    if (wave.length() > 10) {
                        waveIterator.remove();
                    }
                }
            }

            private void createMusicalEffects(Location location) {
                // Create ambient musical particles
                for (int i = 0; i < 3; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = random.nextDouble() * 3;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, random.nextDouble() * 2, z);

                    // Musical particles
                    world.spawnParticle(Particle.NOTE, particleLoc, 1, 0, 0, 0, 1);
                }

                // Ambient sound effects
                if (random.nextFloat() < 0.1) {
                    float pitch = musicalScale[random.nextInt(musicalScale.length)];
                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.3f, pitch);
                }
            }

            private void checkWaveHits(Location center, Vector wave) {
                double distance = wave.length();
                Vector direction = wave.clone().normalize();
                Location hitboxCenter = center.clone().add(direction.multiply(distance));

                for (Entity entity : world.getNearbyEntities(hitboxCenter, 2.0, 2.0, 2.0)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double damage = 8.0 * (1 - distance / 12.0);

                        // Apply damage
                        target.damage(Math.max(3.0, damage), player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply musical effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));

                        // Apply directional knockback
                        target.setVelocity(direction.clone().multiply(0.5).setY(0.2));

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

            private void createHitEffect(Location location) {
                // Musical impact particles
                world.spawnParticle(Particle.NOTE, location, 5, 0.3, 0.3, 0.3, 1);
                world.spawnParticle(Particle.END_ROD, location, 8, 0.3, 0.3, 0.3, 0.05);

                // Impact sounds
                float hitPitch = musicalScale[random.nextInt(musicalScale.length)];
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, hitPitch);
                world.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, hitPitch * 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final musical flourish
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;
                    private int noteIndex = 0;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double radius = 5.0 * (1 - ticks / (double)MAX_TICKS);

                        for (double angle = 0; angle < 360; angle += 30) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * radius;
                            double y = Math.sin(ticks * Math.PI / 10) * 0.5;
                            double z = Math.sin(radian) * radius;

                            Location particleLoc = endLoc.clone().add(x, y, z);

                            // Final musical particles
                            world.spawnParticle(Particle.NOTE, particleLoc, 1, 0, 0, 0, 1);

                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                        }

                        // Final ascending notes
                        if (ticks % 2 == 0) {
                            float pitch = musicalScale[noteIndex % musicalScale.length];
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.6f, pitch);
                            world.playSound(endLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4f, pitch * 1.2f);
                            noteIndex++;
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));

        // Add cooldown
      //  addCooldown(player, "FifthForm", 15);
    }
}