package org.skyforce.demon.breathings.flowerbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Main class for Flower Breathing ability implementation
 * Created: 2025-06-19 09:39:10
 * @author SkyForce-6
 */
public class FlowerBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final World world;
    private final Set<Entity> hitEntities;
    private final Random random;
    private final double[] rotationAngle;
    private int ticks;

    // Constants
    private static final double BASE_DAMAGE = 5.0;
    private static final int FORM_DURATION = 25;

    public FlowerBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.world = player.getWorld();
        this.hitEntities = new HashSet<>();
        this.random = new Random();
        this.rotationAngle = new double[]{0.0};
        this.ticks = 0;
    }

    /**
     * First Form - Petal Whirlwind
     * A basic attack where the user swings their blade while releasing a graceful storm of flower petals.
     */
    public void useFirstForm() {
        ticks = 0;
        hitEntities.clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executePetalWhirlwind(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executePetalWhirlwind(Location loc) {
        double progress = (double) ticks / FORM_DURATION;

        // Create flower petal effects
        createPetalStorm(loc, progress);

        // Execute whirlwind attack
        executeWhirlwindStrike(loc, BASE_DAMAGE, progress);
    }

    private void createPetalStorm(Location loc, double progress) {
        int petalCount = 30;
        double radius = 3.0;

        for (int i = 0; i < petalCount; i++) {
            double angle = (Math.PI * 2 * i / petalCount) + (progress * Math.PI * 2);
            double height = Math.sin(progress * Math.PI * 2) * 1.5;

            // Calculate petal position in spiral pattern
            double x = Math.cos(angle) * (radius * progress);
            double z = Math.sin(angle) * (radius * progress);

            Location petalLoc = loc.clone().add(x, height, z);

            // Create petal particles with varying colors
            Particle.DustOptions petalColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255 - (int)(55 * Math.sin(angle))),
                            clampColor(182 - (int)(30 * Math.cos(angle))),
                            clampColor(193 - (int)(40 * Math.sin(angle + Math.PI/3)))
                    ), 1.0f
            );

            world.spawnParticle(Particle.DUST, petalLoc, 1, 0, 0, 0, 0, petalColor);

            // Add some falling petals
            if (random.nextFloat() < 0.2) {
                Location fallingLoc = petalLoc.clone().add(
                        random.nextDouble() - 0.5,
                        0.5,
                        random.nextDouble() - 0.5
                );

                world.spawnParticle(Particle.END_ROD, fallingLoc, 1, 0, -0.1, 0, 0.02);
            }
        }

        // Create circular slash effect
        createSlashEffect(loc, progress);
    }

    private void createSlashEffect(Location loc, double progress) {
        double radius = 2.5;
        int points = 16;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i / points) + (progress * Math.PI * 3);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(progress * Math.PI * 2) * 0.5;

            Location slashLoc = loc.clone().add(x, y, z);

            // Slash trail particles
            Particle.DustOptions slashColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(220 + (int)(35 * Math.sin(angle))),
                            clampColor(150 + (int)(30 * Math.cos(angle))),
                            clampColor(160 + (int)(40 * Math.sin(angle)))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);
        }
    }

    private void executeWhirlwindStrike(Location loc, double damage, double progress) {
        double radius = 3.0;

        for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate damage with style bonus
                double styleMultiplier = 1.0 + (Math.sin(progress * Math.PI) * 0.3);
                double finalDamage = damage * styleMultiplier;

                // Apply damage
                target.damage(finalDamage, player);

                // Apply graceful movement debuff
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        60,
                        1,
                        false,
                        true
                ));

                // Create hit effect
                createPetalBurstEffect(target.getLocation(), progress);

                hitEntities.add(target);
            }
        }
    }

    private void createPetalBurstEffect(Location loc, double progress) {
        // Burst flash
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

        // Petal burst
        for (int i = 0; i < 20; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble());

            Location burstLoc = loc.clone().add(spread);

            // Burst particles
            Particle.DustOptions burstColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255 - (int)(30 * Math.sin(progress * Math.PI))),
                            clampColor(182 - (int)(20 * Math.cos(progress * Math.PI))),
                            clampColor(193 - (int)(25 * Math.sin(progress * Math.PI)))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
            world.spawnParticle(Particle.END_ROD, burstLoc, 1, 0, 0, 0, 0.02);
        }

        // Burst sounds
        world.playSound(loc, Sound.BLOCK_FLOWERING_AZALEA_BREAK, 1.0f, 1.2f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Implementation of the Fourth Form of Flower Breathing - Crimson Hanagoromo
     * A graceful, curving single sword slash
     * Created: 2025-06-19 09:43:39
     * @author SkyForce-6
     */
    public void useFourthForm() {
        ticks = 0;
        hitEntities.clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executeCrimsonHanagoromo(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeCrimsonHanagoromo(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double damage = BASE_DAMAGE * 2.0; // High damage for single strike

        // Create the graceful curved slash effect
        createCurvedSlash(loc, progress);

        // Execute the precise strike
        executeGracefulStrike(loc, damage, progress);
    }

    private void createCurvedSlash(Location loc, double progress) {
        // Calculate the curved path using Bezier curve
        double t = progress;
        Vector direction = player.getLocation().getDirection();
        Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        // Control points for the Bezier curve
        Vector p0 = loc.toVector();
        Vector p1 = p0.clone().add(direction.clone().multiply(2).add(right.multiply(1)));
        Vector p2 = p0.clone().add(direction.clone().multiply(4).add(right.multiply(-1)));
        Vector p3 = p0.clone().add(direction.clone().multiply(6));

        // Create the curved slash effect
        for (double i = 0; i < 1; i += 0.05) {
            double u = i - (progress * 0.5); // Offset for animation
            if (u < 0 || u > 1) continue;

            // Cubic Bezier curve calculation
            Vector point = p0.clone().multiply(Math.pow(1-u, 3))
                    .add(p1.clone().multiply(3 * u * Math.pow(1-u, 2)))
                    .add(p2.clone().multiply(3 * Math.pow(u, 2) * (1-u)))
                    .add(p3.clone().multiply(Math.pow(u, 3)));

            Location slashLoc = point.toLocation(world);

            // Create main slash particles
            Particle.DustOptions slashColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor((int)(50 * (1-u))),
                            clampColor((int)(50 * (1-u)))
                    ), 1.0f
            );

            world.spawnParticle(Particle.DUST, slashLoc, 2, 0.1, 0.1, 0.1, 0, slashColor);

            // Create flower petal trail
            if (random.nextFloat() < 0.3) {
                createFlowerTrail(slashLoc, u, progress);
            }
        }
    }

    private void createFlowerTrail(Location loc, double u, double progress) {
        // Create falling flower petals
        for (int i = 0; i < 3; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(0.3);

            Location petalLoc = loc.clone().add(spread);

            // Petal particles with crimson color
            Particle.DustOptions petalColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(220 + (int)(35 * Math.sin(u * Math.PI))),
                            clampColor(50 + (int)(30 * Math.cos(u * Math.PI))),
                            clampColor(50 + (int)(30 * Math.sin(u * Math.PI)))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, petalLoc, 1, 0, 0, 0, 0, petalColor);

            // Add some sparkle effects
            if (random.nextFloat() < 0.2) {
                world.spawnParticle(Particle.END_ROD, petalLoc, 1, 0, 0, 0, 0.02);
            }
        }
    }

    private void executeGracefulStrike(Location loc, double damage, double progress) {
        // Only execute damage in the middle of the animation
        if (progress < 0.4 || progress > 0.6) return;

        double strikeRadius = 3.0;
        Vector direction = player.getLocation().getDirection();

        for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate precise strike damage
                double precisionMultiplier = 1.0 + (Math.abs(Math.sin(progress * Math.PI)) * 0.5);
                double finalDamage = damage * precisionMultiplier;

                // Apply damage
                target.damage(finalDamage, player);

                // Create impact effect
                createGracefulImpact(target.getLocation(), progress);

                hitEntities.add(target);
            }
        }
    }

    private void createGracefulImpact(Location loc, double progress) {
        // Create main impact flash
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

        // Create crimson burst effect
        for (int i = 0; i < 25; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble() * 1.5);

            Location burstLoc = loc.clone().add(spread);

            // Burst particles with crimson color
            Particle.DustOptions burstColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(50 + (int)(40 * Math.sin(progress * Math.PI))),
                            clampColor(50 + (int)(40 * Math.cos(progress * Math.PI)))
                    ), 0.7f
            );

            world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);

            // Add some sparkle effects
            if (random.nextFloat() < 0.3) {
                world.spawnParticle(Particle.END_ROD, burstLoc, 1, 0, 0, 0, 0.05);
            }
        }

        // Play impact sounds
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.0f);
        world.playSound(loc, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.0f, 0.8f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.2f);
    }

    /**
     * Implementation of the Fifth Form of Flower Breathing - Peonies of Futility
     * A flurry of nine consecutive flowing attacks
     * Created: 2025-06-19 09:46:32
     * @author SkyForce-6
     */
    public void useFifthForm() {
        ticks = 0;
        hitEntities.clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executePeoniesOfFutility(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executePeoniesOfFutility(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double damage = BASE_DAMAGE * 0.8; // Reduced per-hit damage due to multiple strikes

        // Calculate which strike we're on (0-8 for 9 strikes)
        int currentStrike = (int) (progress * 9);
        double strikeProgress = (progress * 9) % 1;

        // Create the flowing peony effects
        createPeonyPattern(loc, progress, currentStrike);

        // Execute the weaving strikes
        executeWeavingStrikes(loc, damage, progress, currentStrike, strikeProgress);
    }

    private void createPeonyPattern(Location loc, double progress, int currentStrike) {
        // Create flowing peony petals
        createFlowingPetals(loc, progress, currentStrike);

        // Create weaving strike trails
        createWeavingTrails(loc, progress, currentStrike);
    }

    private void createFlowingPetals(Location loc, double progress, int currentStrike) {
        int petalsPerStrike = 15;
        double radius = 3.0;

        for (int i = 0; i < petalsPerStrike; i++) {
            // Calculate spiral pattern
            double angle = (Math.PI * 2 * i / petalsPerStrike) + (progress * Math.PI * 4)
                    + (currentStrike * Math.PI / 4.5); // Divide by half of 9 for even distribution
            double height = Math.sin(progress * Math.PI * 2) * 1.5;

            double x = Math.cos(angle) * radius * progress;
            double z = Math.sin(angle) * radius * progress;

            Location petalLoc = loc.clone().add(x, height, z);

            // Create peony petal particles
            Particle.DustOptions petalColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255 - (currentStrike * 10)),
                            clampColor(182 - (currentStrike * 15)),
                            clampColor(193 - (currentStrike * 12))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, petalLoc, 1, 0, 0, 0, 0, petalColor);

            // Add sparkle effects
            if (random.nextFloat() < 0.2) {
                world.spawnParticle(Particle.END_ROD, petalLoc, 1, 0, 0, 0, 0.02);
            }
        }
    }

    private void createWeavingTrails(Location loc, double progress, int currentStrike) {
        // Create weaving pattern using Lissajous curves
        double frequency = 2.0;
        double amplitude = 2.0;
        int points = 20;

        for (int i = 0; i < points; i++) {
            double t = (double) i / points + (progress * 2);

            // Lissajous curve parameters
            double x = amplitude * Math.sin(frequency * t + (currentStrike * Math.PI / 4.5));
            double y = 1.0 + Math.sin(t * Math.PI * 2) * 0.5;
            double z = amplitude * Math.cos(frequency * 2 * t);

            Location trailLoc = loc.clone().add(x, y, z);

            // Create trail particles
            Particle.DustOptions trailColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(220 + (int)(35 * Math.sin(t * Math.PI))),
                            clampColor(150 + (int)(30 * Math.cos(t * Math.PI))),
                            clampColor(160 + (int)(40 * Math.sin(t * Math.PI)))
                    ), 0.6f
            );

            world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);
        }
    }

    private void executeWeavingStrikes(Location loc, double damage, double progress, int currentStrike, double strikeProgress) {
        // Only execute damage during the strike phase
        if (strikeProgress > 0.3) return;

        double strikeRadius = 3.0;

        for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate combo damage
                double comboMultiplier = 1.0 + (currentStrike * 0.1); // Damage increases with combo
                double finalDamage = damage * comboMultiplier;

                // Apply damage
                target.damage(finalDamage, player);

                // Create strike effect
                createPeonyStrikeEffect(target.getLocation(), progress, currentStrike);

                // Only add to hit entities on final strike
                if (currentStrike == 8) {
                    hitEntities.add(target);
                }
            }
        }
    }

    private void createPeonyStrikeEffect(Location loc, double progress, int currentStrike) {
        // Create strike flash
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

        // Create peony burst effect
        for (int i = 0; i < 15; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble());

            Location burstLoc = loc.clone().add(spread);

            // Burst particles with peony colors
            Particle.DustOptions burstColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255 - (currentStrike * 5)),
                            clampColor(182 - (currentStrike * 8)),
                            clampColor(193 - (currentStrike * 7))
                    ), 0.7f
            );

            world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);

            // Add sparkle effects
            if (random.nextFloat() < 0.3) {
                world.spawnParticle(Particle.END_ROD, burstLoc, 1, 0, 0, 0, 0.03);
            }
        }

        // Play strike sounds with increasing pitch
        float pitch = 0.8f + (currentStrike * 0.1f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, pitch);
        world.playSound(loc, Sound.BLOCK_FLOWERING_AZALEA_BREAK, 0.6f, pitch);
    }

    /**
     * Implementation of the Sixth Form of Flower Breathing - Whirling Peach
     * A spinning counter-attack technique used after evasion
     * Created: 2025-06-19 09:50:09
     * @author SkyForce-6
     */
    public void useSixthForm() {
        ticks = 0;
        hitEntities.clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executeWhirlingPeach(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeWhirlingPeach(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double damage = BASE_DAMAGE * 1.5; // Higher damage for counter-attack

        // Initial evasion movement
        if (progress < 0.3) {
            executeEvasiveMovement(loc, progress);
        }

        // Spinning attack phase
        if (progress >= 0.3) {
            executeSpinningAttack(loc, damage, progress);
        }

        // Create peach blossom effects throughout
        createPeachBlossomEffects(loc, progress);
    }

    private void executeEvasiveMovement(Location loc, double progress) {
        // Calculate evasion direction
        Vector direction = player.getLocation().getDirection();
        Vector side = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

        // Quick sideways dodge
        double dodgeStrength = Math.sin(progress * Math.PI) * 1.5;
        Vector dodgeVector = side.multiply(dodgeStrength);

        // Apply movement
        player.setVelocity(dodgeVector);

        // Add brief invulnerability effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5, 4, false, false));
    }

    private void executeSpinningAttack(Location loc, double damage, double progress) {
        // Calculate spin progress
        double spinProgress = (progress - 0.3) / 0.7; // Normalize remaining progress
        double spinAngle = spinProgress * Math.PI * 4; // Two full rotations

        // Create spinning attack radius
        double radius = 3.0;
        int points = 16;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i / points) + spinAngle;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location spinLoc = loc.clone().add(x, 0, z);

            // Create spin trail
            createSpinTrail(spinLoc, spinProgress, angle);

            // Deal damage to entities in range
            if (spinProgress > 0.2 && spinProgress < 0.8) {
                for (Entity entity : world.getNearbyEntities(spinLoc, 1.5, 2, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate momentum-based damage
                        double momentumMultiplier = 1.0 + (player.getVelocity().length() * 0.5);
                        double finalDamage = damage * momentumMultiplier;

                        // Apply damage
                        target.damage(finalDamage, player);

                        // Create impact effect
                        createPeachImpactEffect(target.getLocation(), spinProgress);

                        hitEntities.add(target);
                    }
                }
            }
        }
    }

    private void createSpinTrail(Location loc, double progress, double angle) {
        // Create spinning blade trail
        Particle.DustOptions trailColor = new Particle.DustOptions(
                Color.fromRGB(
                        clampColor(255),
                        clampColor(182 + (int)(73 * Math.sin(angle))),
                        clampColor(193 + (int)(62 * Math.cos(angle)))
                ), 0.8f
        );

        world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, trailColor);

        // Add motion blur effect
        if (random.nextFloat() < 0.3) {
            world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0.02);
        }
    }

    private void createPeachBlossomEffects(Location loc, double progress) {
        int petalCount = 20;
        double radius = 3.0;

        for (int i = 0; i < petalCount; i++) {
            // Calculate spiral pattern
            double angle = (Math.PI * 2 * i / petalCount) + (progress * Math.PI * 4);
            double height = Math.sin(progress * Math.PI * 2) * 2.0;

            double x = Math.cos(angle) * radius * progress;
            double z = Math.sin(angle) * radius * progress;

            Location petalLoc = loc.clone().add(x, height, z);

            // Create peach blossom particles
            Particle.DustOptions petalColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255 - (int)(20 * Math.sin(angle))),
                            clampColor(182 + (int)(20 * Math.cos(angle))),
                            clampColor(193 + (int)(20 * Math.sin(angle)))
                    ), 0.7f
            );

            world.spawnParticle(Particle.DUST, petalLoc, 1, 0, 0, 0, 0, petalColor);

            // Add some falling petals
            if (random.nextFloat() < 0.2) {
                Location fallingLoc = petalLoc.clone().add(
                        random.nextDouble() - 0.5,
                        0.5,
                        random.nextDouble() - 0.5
                );

                world.spawnParticle(Particle.END_ROD, fallingLoc, 1, 0, -0.1, 0, 0.02);
            }
        }
    }

    private void createPeachImpactEffect(Location loc, double progress) {
        // Create impact flash
        world.spawnParticle(Particle.FLASH, loc, 2, 0.1, 0.1, 0.1, 0);

        // Create peach blossom burst
        for (int i = 0; i < 20; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble() * 1.2);

            Location burstLoc = loc.clone().add(spread);

            // Burst particles with peach colors
            Particle.DustOptions burstColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(182 + (int)(30 * Math.sin(progress * Math.PI))),
                            clampColor(193 + (int)(30 * Math.cos(progress * Math.PI)))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
        }

        // Play impact sounds
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        world.playSound(loc, Sound.BLOCK_CHERRY_LEAVES_BREAK, 0.8f, 1.0f);
    }

    /**
     * Implementation of the Final Form of Flower Breathing - Equinoctial Vermilion Eye
     * A dangerous focusing technique that maximizes kinetic vision at the cost of potential blindness
     * Created: 2025-06-19 09:53:03
     * @author SkyForce-6
     */
    public void useFinalForm() {
        ticks = 0;
        hitEntities.clear();

        // Apply initial effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, FORM_DURATION, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, FORM_DURATION, 2, false, false));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    applyAftereffects();
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executeEquinoctialVermilionEye(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeEquinoctialVermilionEye(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double damage = BASE_DAMAGE * 2.5; // High damage due to enhanced perception

        // Create eye strain visual effects
        createEyeStrainEffects(loc, progress);

        // Execute enhanced perception attacks
        executeEnhancedAttacks(loc, damage, progress);

        // Apply strain effects to the user
        applyStrainEffects(progress);
    }

    private void createEyeStrainEffects(Location loc, double progress) {
        // Create vermilion eye effect
        createVermilionEyeEffect(loc, progress);

        // Create enhanced perception visuals
        createPerceptionEffects(loc, progress);
    }

    private void createVermilionEyeEffect(Location loc, double progress) {
        // Create eye visualization
        double eyeRadius = 0.3;
        Location eyeLoc = loc.clone().add(0, 1.6, 0); // Head height
        Vector direction = player.getLocation().getDirection();

        // Move eye effect in front of player
        eyeLoc.add(direction.multiply(0.5));

        // Create iris pattern
        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2 * i / 16) + (progress * Math.PI);
            double x = Math.cos(angle) * eyeRadius;
            double y = Math.sin(angle) * eyeRadius;

            Location irisLoc = eyeLoc.clone().add(x, y, 0);

            // Iris particles with increasing redness
            Particle.DustOptions irisColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor((int)(50 * (1 - progress))),
                            clampColor((int)(50 * (1 - progress)))
                    ), 0.5f
            );

            world.spawnParticle(Particle.DUST, irisLoc, 1, 0, 0, 0, 0, irisColor);
        }

        // Create blood vessel effects
        if (progress > 0.7) {
            createBloodVesselEffects(eyeLoc, progress);
        }
    }

    private void createBloodVesselEffects(Location eyeLoc, double progress) {
        int vessels = 8;
        double vesselLength = 0.5;

        for (int i = 0; i < vessels; i++) {
            double angle = (Math.PI * 2 * i / vessels);

            for (double d = 0; d < vesselLength; d += 0.1) {
                double x = Math.cos(angle) * (d + (random.nextDouble() * 0.1));
                double y = Math.sin(angle) * (d + (random.nextDouble() * 0.1));

                Location vesselLoc = eyeLoc.clone().add(x, y, 0);

                // Blood vessel particles
                Particle.DustOptions vesselColor = new Particle.DustOptions(
                        Color.fromRGB(
                                clampColor(255),
                                clampColor(0),
                                clampColor(0)
                        ), 0.3f
                );

                world.spawnParticle(Particle.DUST, vesselLoc, 1, 0, 0, 0, 0, vesselColor);
            }
        }
    }

    private void createPerceptionEffects(Location loc, double progress) {
        // Create time dilation effect
        double radius = 5.0;
        int lines = 32;

        for (int i = 0; i < lines; i++) {
            double angle = (Math.PI * 2 * i / lines);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location lineLoc = loc.clone().add(x, 0, z);

            // Create perception lines
            for (double h = 0; h < 3; h += 0.5) {
                Location heightLoc = lineLoc.clone().add(0, h, 0);

                Particle.DustOptions lineColor = new Particle.DustOptions(
                        Color.fromRGB(
                                clampColor(200 + (int)(55 * Math.sin(progress * Math.PI))),
                                clampColor(100 + (int)(82 * Math.cos(progress * Math.PI))),
                                clampColor(100 + (int)(93 * Math.sin(progress * Math.PI)))
                        ), 0.6f
                );

                world.spawnParticle(Particle.DUST, heightLoc, 1, 0, 0, 0, 0, lineColor);
            }
        }
    }

    private void executeEnhancedAttacks(Location loc, double damage, double progress) {
        double radius = 4.0;

        // Increased detection range due to enhanced perception
        for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate precision damage
                double precisionMultiplier = 1.0 + (Math.pow(progress, 2) * 0.5);
                double finalDamage = damage * precisionMultiplier;

                // Apply damage
                target.damage(finalDamage, player);

                // Create precision strike effect
                createPrecisionStrikeEffect(target.getLocation(), progress);

                hitEntities.add(target);
            }
        }
    }

    private void createPrecisionStrikeEffect(Location loc, double progress) {
        // Create precision strike flash
        world.spawnParticle(Particle.FLASH, loc, 2, 0, 0, 0, 0);

        // Create precision strike particles
        for (int i = 0; i < 25; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble());

            Location strikeLog = loc.clone().add(spread);

            // Strike particles with vermilion color
            Particle.DustOptions strikeColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(50 + (int)(30 * Math.sin(progress * Math.PI))),
                            clampColor(50 + (int)(30 * Math.cos(progress * Math.PI)))
                    ), 0.7f
            );

            world.spawnParticle(Particle.DUST, strikeLog, 1, 0, 0, 0, 0, strikeColor);
        }

        // Play precision strike sounds
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 2.0f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
    }

    private void applyStrainEffects(double progress) {
        // Apply increasing strain effects as the technique continues
        if (progress > 0.5) {
            // Add nausea effect to simulate eye strain
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    20,
                    0,
                    false,
                    false
            ));

            // Chance to apply blindness briefly
            if (random.nextFloat() < progress * 0.1) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        10,
                        0,
                        false,
                        false
                ));
            }
        }
    }

    private void applyAftereffects() {
        // Apply severe aftereffects when the technique ends
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 0, false, false));

        // Play ending sound effects
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 0.5f);
        world.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 2.0f);
    }

}