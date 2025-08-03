package org.skyforce.demon.breathings.insectbreathing;

import org.bukkit.*;
import org.bukkit.block.Block;
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
 * Main class for Insect Breathing ability implementation
 * Created: 2025-06-19 09:06:37
 * @author SkyForce-6
 */
public class InsectBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final World world;
    private final Set<Entity> hitEntities;
    private final Random random;
    private final double[] rotationAngle;
    private int ticks;

    // Constants
    private static final double BASE_DAMAGE = 6.0;
    private static final int FORM_DURATION = 25;

    public InsectBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.world = player.getWorld();
        this.hitEntities = new HashSet<>();
        this.random = new Random();
        this.rotationAngle = new double[]{0.0};
        this.ticks = 0;
    }


    public void useFirstForm() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ticks >= FORM_DURATION) {
                    cancel();
                    hitEntities.clear();
                    return;
                }

                Location loc = player.getLocation();
                executeDanceOfTheButterfly(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Implementation of the first form of Insect Breathing - Dance of the Butterfly: Caprice
     * Created: 2025-06-19 09:08:12
     * @author SkyForce-6
     */
    private void executeDanceOfTheButterfly(Location loc) {
        double baseDamage = 6.0;
        double progress = (double) ticks / 25; // 25 ticks duration

        // Create butterfly wing effects
        createButterflyWings(loc, progress);

        // Execute multi-strike attack
        executeCapriceStrikes(loc, baseDamage, progress);
    }

    private void createButterflyWings(Location loc, double progress) {
        double wingSpan = 2.0;
        double flapSpeed = Math.sin(progress * Math.PI * 4); // Flapping animation

        // Create both wings
        for (int wing = 0; wing < 2; wing++) {
            double wingSign = wing * 2 - 1; // -1 or 1 for left/right wing

            // Create wing pattern
            for (double angle = 0; angle < Math.PI; angle += Math.PI / 16) {
                double length = wingSpan * Math.sin(angle); // Wing shape
                length *= 1 + flapSpeed * 0.2; // Flapping motion

                // Calculate wing positions
                Vector wingVector = player.getLocation().getDirection().clone()
                        .normalize()
                        .multiply(Math.cos(angle) * length)
                        .add(new Vector(
                                Math.sin(angle) * length * wingSign,
                                Math.sin(angle) * length * 0.5,
                                0
                        )).rotateAroundY(wingSign * Math.PI / 8);

                Location wingLoc = loc.clone().add(wingVector);

                // Wing particles
                Particle.DustOptions wingColor = new Particle.DustOptions(
                        Color.fromRGB(
                                clampColor(200 + (int)(55 * Math.sin(progress * Math.PI * 2))),
                                clampColor(150 + (int)(105 * Math.cos(progress * Math.PI * 2))),
                                clampColor(255)
                        ), 1.0f
                );

                world.spawnParticle(Particle.DUST, wingLoc, 1, 0, 0, 0, 0, wingColor);

                // Trail particles
                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.END_ROD, wingLoc, 1, 0, 0, 0, 0.02);
                    world.spawnParticle(Particle.WITCH, wingLoc, 1, 0, 0, 0, 0.01);
                }
            }
        }
    }

    private void executeCapriceStrikes(Location loc, double damage, double progress) {
        // Early phase: Jump into the air
        if (progress < 0.3) {
            Vector jumpVelocity = new Vector(0, 0.8, 0);
            player.setVelocity(jumpVelocity);
            return;
        }

        // Mid phase: Charge towards target
        if (progress < 0.6) {
            Vector chargeDirection = player.getLocation().getDirection().multiply(1.2);
            player.setVelocity(chargeDirection);
        }

        // Late phase: Execute multi-strikes
        double strikeRadius = 3.0;
        int maxStrikes = 5;

        for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate multi-strike damage
                for (int strike = 0; strike < maxStrikes; strike++) {
                    if (random.nextFloat() < 0.8) { // 80% chance per strike
                        double strikeProgress = (double) strike / maxStrikes;
                        double strikeDamage = damage * (0.8 + strikeProgress * 0.4); // Increasing damage

                        // Apply damage
                        target.damage(strikeDamage, player);

                        // Apply poison effect
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.POISON,
                                100 + strike * 20, // Duration increases with each strike
                                1 // Poison level II
                        ));

                        // Create strike effects
                        createStrikeEffect(target.getLocation(), strikeProgress);
                    }
                }

                hitEntities.add(target);
            }
        }
    }

    private void createStrikeEffect(Location loc, double progress) {
        // Strike flash
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

        // Strike particles
        for (int i = 0; i < 15; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble() * 0.8);

            Location particleLoc = loc.clone().add(spread);

            // Poison effect particles
            Particle.DustOptions poisonColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(150 + (int)(50 * Math.sin(progress * Math.PI))),
                            clampColor(255),
                            clampColor(150 + (int)(50 * Math.cos(progress * Math.PI)))
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, poisonColor);
            world.spawnParticle(Particle.WITCH, particleLoc, 1, 0.2, 0.2, 0.2, 0);
        }

        // Strike sounds
        world.playSound(loc, Sound.ENTITY_BEE_STING, 0.6f, 1.2f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Implementation of the second form of Insect Breathing - Dance of the Bee Sting: True Flutter
     * Created: 2025-06-19 09:25:08
     * @author SkyForce-6
     */


    public void useSecondForm() {
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
                executeDanceOfTheBeeStingTrueFlutter(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeDanceOfTheBeeStingTrueFlutter(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double baseDamage = BASE_DAMAGE * 1.5; // Higher base damage for this form

        // Create bee sting trail effects
        createBeeStingTrail(loc, progress);

        // Execute the blinding speed dash and thrust
        executeBlindingThrust(loc, baseDamage, progress);
    }

    private void createBeeStingTrail(Location loc, double progress) {
        // Create bee-like trail pattern
        double trailLength = 3.0;
        Vector direction = player.getLocation().getDirection();

        for (double i = 0; i < trailLength; i += 0.2) {
            double offset = i * (1 - progress); // Trail gets shorter as attack progresses

            // Calculate zigzag pattern for bee-like movement
            double zigzag = Math.sin(i * 4 + progress * Math.PI * 2) * 0.3;
            Vector zigzagOffset = direction.clone().crossProduct(new Vector(0, 1, 0)).multiply(zigzag);

            Location trailLoc = loc.clone().subtract(direction.clone().multiply(offset)).add(zigzagOffset);

            // Bee trail particles
            Particle.DustOptions beeColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(200 + (int)(55 * Math.sin(progress * Math.PI))),
                            clampColor(50)
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, beeColor);

            // Speed effect particles
            if (random.nextFloat() < 0.4) {
                world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0, 0, 0, 0.02);
                world.spawnParticle(Particle.WITCH, trailLoc, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Add wing effects during the dash
        if (progress < 0.7) {
            createBeeWings(loc, progress);
        }
    }

    private void createBeeWings(Location loc, double progress) {
        double wingSpan = 1.5;
        double flapSpeed = Math.sin(progress * Math.PI * 8); // Faster flapping for bee form

        // Create both wings
        for (int wing = 0; wing < 2; wing++) {
            double wingSign = wing * 2 - 1; // -1 or 1 for left/right wing

            for (double angle = 0; angle < Math.PI / 2; angle += Math.PI / 16) {
                double length = wingSpan * Math.sin(angle);
                length *= 1 + flapSpeed * 0.3;

                Vector wingVector = player.getLocation().getDirection().clone()
                        .normalize()
                        .multiply(Math.cos(angle) * length)
                        .add(new Vector(
                                Math.sin(angle) * length * wingSign,
                                Math.sin(angle) * length * 0.3,
                                0
                        )).rotateAroundY(wingSign * Math.PI / 6);

                Location wingLoc = loc.clone().add(wingVector);

                // Wing particles
                Particle.DustOptions wingColor = new Particle.DustOptions(
                        Color.fromRGB(
                                clampColor(255),
                                clampColor(200 + (int)(55 * Math.cos(progress * Math.PI * 4))),
                                clampColor(50)
                        ), 0.6f
                );

                world.spawnParticle(Particle.DUST, wingLoc, 1, 0, 0, 0, 0, wingColor);
            }
        }
    }

    private void executeBlindingThrust(Location loc, double damage, double progress) {
        // Early phase: Prepare for dash
        if (progress < 0.3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 3, false, false));
            return;
        }

        // Mid phase: Blinding speed dash
        if (progress < 0.7) {
            Vector dashVector = player.getLocation().getDirection().multiply(2.0);
            player.setVelocity(dashVector);

            // Create speed lines
            createSpeedLines(loc, progress);
        }

        // Late phase: Execute powerful thrust
        if (progress > 0.7) {
            double strikeRadius = 2.5;

            for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
                if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                    LivingEntity target = (LivingEntity) entity;

                    // Calculate thrust damage with momentum bonus
                    double momentumMultiplier = 1.0 + (player.getVelocity().length() * 0.5);
                    double finalDamage = damage * momentumMultiplier;

                    // Apply damage and effects
                    target.damage(finalDamage, player);

                    // Apply stronger poison effect
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON,
                            140, // Longer duration
                            2    // Poison level III
                    ));

                    // Create impact effect
                    createBeeStingImpact(target.getLocation(), progress);

                    hitEntities.add(target);
                }
            }
        }
    }

    private void createSpeedLines(Location loc, double progress) {
        double radius = 1.0;
        int lines = 8;

        for (int i = 0; i < lines; i++) {
            double angle = (Math.PI * 2 * i / lines) + rotationAngle[0];

            Vector offset = new Vector(
                    Math.cos(angle) * radius,
                    0,
                    Math.sin(angle) * radius
            ).rotateAroundY(progress * Math.PI * 2);

            Location lineLoc = loc.clone().add(offset);

            world.spawnParticle(Particle.END_ROD, lineLoc, 1, 0, 0, 0, 0.02);
        }

        rotationAngle[0] += Math.PI / 16;
    }

    private void createBeeStingImpact(Location loc, double progress) {
        // Impact flash
        world.spawnParticle(Particle.FLASH, loc, 2, 0.1, 0.1, 0.1, 0);

        // Impact particles
        for (int i = 0; i < 20; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble() * 1.2);

            Location impactLoc = loc.clone().add(spread);

            // Bee sting particles
            Particle.DustOptions stingColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(180 + (int)(75 * Math.sin(progress * Math.PI))),
                            clampColor(50)
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, stingColor);
            world.spawnParticle(Particle.CRIT, impactLoc, 2, 0.1, 0.1, 0.1, 0.2);
        }

        // Impact sounds
        world.playSound(loc, Sound.ENTITY_BEE_STING, 1.0f, 0.8f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.2f);
    }

    /**
     * Implementation of the third form of Insect Breathing - Dance of the Dragonfly: Compound Eye Hexagon
     * Created: 2025-06-19 09:30:01
     * @author SkyForce-6
     */
    public void useThirdForm() {
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
                executeDanceOfTheDragonflyCompoundEye(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeDanceOfTheDragonflyCompoundEye(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double baseDamage = BASE_DAMAGE * 1.2; // Moderate base damage due to multiple strikes

        // Create dragonfly effects
        createDragonflyPattern(loc, progress);

        // Execute the hexagonal strike pattern
        executeHexagonalStrikes(loc, baseDamage, progress);
    }

    private void createDragonflyPattern(Location loc, double progress) {
        // Create compound eye effect
        createCompoundEyes(loc, progress);

        // Create dragonfly wings
        createDragonflyWings(loc, progress);

        // Create targeting indicators
        createTargetingPatterns(loc, progress);
    }

    private void createCompoundEyes(Location loc, double progress) {
        double eyeRadius = 2.0;
        int hexagonPoints = 6;

        for (int i = 0; i < hexagonPoints; i++) {
            double angle = (Math.PI * 2 * i / hexagonPoints) + rotationAngle[0];

            // Create hexagonal pattern for each compound eye
            for (int j = 0; j < 6; j++) {
                double innerAngle = (Math.PI * 2 * j / 6) + (progress * Math.PI);
                double eyeX = Math.cos(angle) * eyeRadius + Math.cos(innerAngle) * 0.3;
                double eyeZ = Math.sin(angle) * eyeRadius + Math.sin(innerAngle) * 0.3;

                Location eyeLoc = loc.clone().add(eyeX, 1.5, eyeZ);

                // Compound eye particles
                Particle.DustOptions eyeColor = new Particle.DustOptions(
                        Color.fromRGB(
                                clampColor(200 + (int)(55 * Math.sin(progress * Math.PI))),
                                clampColor(50 + (int)(25 * Math.cos(progress * Math.PI))),
                                clampColor(255)
                        ), 0.5f
                );

                world.spawnParticle(Particle.DUST, eyeLoc, 1, 0, 0, 0, 0, eyeColor);
            }
        }

        rotationAngle[0] += Math.PI / 32;
    }

    private void createDragonflyWings(Location loc, double progress) {
        double wingSpan = 2.5;
        double flapSpeed = Math.sin(progress * Math.PI * 6); // Fast flapping for dragonfly

        // Create two pairs of wings
        for (int pair = 0; pair < 2; pair++) {
            double pairOffset = pair * 0.5; // Offset for front and back wings

            for (int wing = 0; wing < 2; wing++) {
                double wingSign = wing * 2 - 1; // -1 or 1 for left/right wing

                for (double t = 0; t < 1; t += 0.1) {
                    // Create elongated wing shape
                    double length = wingSpan * (1 - t * t);
                    length *= 1 + flapSpeed * 0.2;

                    Vector wingVector = player.getLocation().getDirection().clone()
                            .normalize()
                            .multiply(pairOffset)
                            .add(new Vector(
                                    Math.sin(t * Math.PI) * length * wingSign,
                                    Math.sin(t * Math.PI) * length * 0.2,
                                    Math.cos(t * Math.PI) * length * 0.3
                            )).rotateAroundY(wingSign * Math.PI / 8);

                    Location wingLoc = loc.clone().add(wingVector);

                    // Wing particles
                    Particle.DustOptions wingColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    clampColor(150 + (int)(105 * Math.sin(progress * Math.PI))),
                                    clampColor(200 + (int)(55 * Math.cos(progress * Math.PI))),
                                    clampColor(255)
                            ), 0.7f
                    );

                    world.spawnParticle(Particle.DUST, wingLoc, 1, 0, 0, 0, 0, wingColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, wingLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }
        }
    }

    private void createTargetingPatterns(Location loc, double progress) {
        double radius = 3.0;
        String[] targetPoints = {"HEAD", "NECK", "HEART", "TORSO", "ABDOMEN", "SPINE"};

        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 * i / 6) + (progress * Math.PI / 3);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location targetLoc = loc.clone().add(x, 1.0 + Math.sin(progress * Math.PI * 2) * 0.5, z);

            // Create targeting circle
            for (int j = 0; j < 8; j++) {
                double circleAngle = (Math.PI * 2 * j / 8);
                double circleX = Math.cos(circleAngle) * 0.3;
                double circleZ = Math.sin(circleAngle) * 0.3;

                Location circleLoc = targetLoc.clone().add(circleX, 0, circleZ);

                Particle.DustOptions targetColor = new Particle.DustOptions(
                        Color.fromRGB(255, 50, 50), 0.5f
                );

                world.spawnParticle(Particle.DUST, circleLoc, 1, 0, 0, 0, 0, targetColor);
            }
        }
    }

    private void executeHexagonalStrikes(Location loc, double baseDamage, double progress) {
        // Calculate which strike we're on (0-5)
        int currentStrike = (int) (progress * 6);
        double strikeProgress = (progress * 6) % 1;

        if (strikeProgress < 0.3) { // Execute strike at the start of each phase
            double strikeRadius = 3.0;

            for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
                if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                    LivingEntity target = (LivingEntity) entity;

                    // Calculate strike damage with multiplier based on strike number
                    double strikeMultiplier = 1.0 + (currentStrike * 0.1);
                    double finalDamage = baseDamage * strikeMultiplier;

                    // Apply damage
                    target.damage(finalDamage, player);

                    // Apply increasing poison effect
                    target.addPotionEffect(new PotionEffect(
                            PotionEffectType.POISON,
                            80 + currentStrike * 20, // Increasing duration
                            Math.min(1 + currentStrike / 2, 3) // Increasing level, max 4
                    ));

                    // Create strike effect
                    createPrecisionStrikeEffect(target.getLocation(), currentStrike, strikeProgress);

                    // Only add to hit entities on final strike
                    if (currentStrike == 5) {
                        hitEntities.add(target);
                    }
                }
            }
        }
    }

    private void createPrecisionStrikeEffect(Location loc, int strikeNumber, double progress) {
        // Strike flash
        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

        // Precision strike particles
        for (int i = 0; i < 12; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble());

            Location particleLoc = loc.clone().add(spread);

            // Strike color varies by strike number
            Particle.DustOptions strikeColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(200 + strikeNumber * 10),
                            clampColor(50 + strikeNumber * 20),
                            clampColor(255)
                    ), 0.8f
            );

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, strikeColor);
            world.spawnParticle(Particle.CRIT, particleLoc, 2, 0.1, 0.1, 0.1, 0.1);
        }

        // Strike sounds
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.0f + (strikeNumber * 0.1f));
        world.playSound(loc, Sound.BLOCK_POINTED_DRIPSTONE_LAND, 0.6f, 1.5f + (strikeNumber * 0.1f));
    }

    /**
     * Implementation of the fourth form of Insect Breathing - Dance of the Centipede: Hundred-Legged Zigzag
     * Created: 2025-06-19 09:33:21
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
                executeDanceOfTheCentipedeZigzag(loc);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeDanceOfTheCentipedeZigzag(Location loc) {
        double progress = (double) ticks / FORM_DURATION;
        double baseDamage = BASE_DAMAGE * 2.0; // High base damage due to momentum

        // Zigzag movement pattern
        executeZigzagPattern(loc, progress);

        // Create centipede effects
        createCentipedeEffect(loc, progress);

        // Execute the final strike if in range
        if (progress > 0.7) {
            executeNeckStrike(loc, baseDamage, progress);
        }
    }

    private void executeZigzagPattern(Location loc, double progress) {
        if (progress < 0.7) { // Zigzag phase
            // Calculate zigzag pattern
            double frequency = 8.0; // Higher = more zigzags
            double amplitude = 2.0; // Width of zigzags

            // Calculate movement direction
            Vector baseDirection = player.getLocation().getDirection().clone();
            Vector side = baseDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();

            // Calculate zigzag offset
            double zigzagOffset = Math.sin(progress * Math.PI * frequency) * amplitude;
            Vector zigzagDirection = baseDirection.clone().multiply(2.0) // Forward movement
                    .add(side.multiply(zigzagOffset)); // Side movement

            // Apply movement
            player.setVelocity(zigzagDirection);

            // Add speed effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 4, false, false));

            // Destroy wooden blocks in path (bridge destruction effect)
            destroyWoodenBlocks(loc, zigzagDirection);
        }
    }

    private void destroyWoodenBlocks(Location loc, Vector direction) {
        double radius = 1.5;
        for (double x = -radius; x <= radius; x += 0.5) {
            for (double y = -0.5; y <= 0.5; y += 0.5) {
                for (double z = -radius; z <= radius; z += 0.5) {
                    Location blockLoc = loc.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();

                    if (block.getType().name().contains("WOOD") ||
                            block.getType().name().contains("PLANKS") ||
                            block.getType().name().contains("LOG")) {

                        // Create particle effect before breaking
                        world.spawnParticle(Particle.BLOCK,
                                blockLoc,
                                10,
                                0.3, 0.3, 0.3,
                                0,
                                block.getBlockData());

                        // Break the block
                        block.breakNaturally();

                        // Play break sound
                        world.playSound(blockLoc, Sound.BLOCK_WOOD_BREAK, 0.5f, 1.0f);
                    }
                }
            }
        }
    }

    private void createCentipedeEffect(Location loc, double progress) {
        // Create centipede body segments
        int segments = 20;
        Vector direction = player.getLocation().getDirection();

        for (int i = 0; i < segments; i++) {
            double segmentProgress = (double) i / segments;
            double offset = segmentProgress * 3.0; // Length of centipede

            // Calculate segment position with zigzag pattern
            double frequency = 8.0;
            double amplitude = 0.3;
            double zigzagOffset = Math.sin((progress * Math.PI * frequency) + (segmentProgress * Math.PI * 2)) * amplitude;

            Vector segmentPos = direction.clone()
                    .multiply(-offset) // Behind player
                    .add(direction.clone().crossProduct(new Vector(0, 1, 0)).multiply(zigzagOffset));

            Location segmentLoc = loc.clone().add(segmentPos);

            // Segment particles
            Particle.DustOptions segmentColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(100 + (int)(50 * Math.sin(segmentProgress * Math.PI))),
                            clampColor(50 + (int)(25 * Math.cos(segmentProgress * Math.PI))),
                            clampColor(50)
                    ), 1.0f
            );

            world.spawnParticle(Particle.DUST, segmentLoc, 1, 0, 0, 0, 0, segmentColor);

            // Leg particles
            createCentipedeLegs(segmentLoc, progress + segmentProgress);
        }
    }

    private void createCentipedeLegs(Location loc, double progress) {
        int legsPerSide = 2;
        double legLength = 0.5;

        for (int side = 0; side < 2; side++) {
            double sideSign = side * 2 - 1; // -1 or 1

            for (int leg = 0; leg < legsPerSide; leg++) {
                double legProgress = (double) leg / legsPerSide;
                double legAngle = Math.sin(progress * Math.PI * 4 + legProgress * Math.PI) * Math.PI / 4;

                Vector legVector = new Vector(
                        Math.cos(legAngle) * legLength * sideSign,
                        Math.sin(legAngle) * legLength * 0.2,
                        0
                );

                Location legLoc = loc.clone().add(legVector);

                Particle.DustOptions legColor = new Particle.DustOptions(
                        Color.fromRGB(80, 40, 40),
                        0.5f
                );

                world.spawnParticle(Particle.DUST, legLoc, 1, 0, 0, 0, 0, legColor);
            }
        }
    }

    private void executeNeckStrike(Location loc, double damage, double progress) {
        double strikeRadius = 2.0;
        Vector playerVel = player.getVelocity();
        double speedMultiplier = 1.0 + (playerVel.length() * 0.8); // Momentum damage bonus

        for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
            if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                LivingEntity target = (LivingEntity) entity;

                // Calculate final damage with speed bonus
                double finalDamage = damage * speedMultiplier;

                // Apply damage
                target.damage(finalDamage, player);

                // Apply strong poison effect
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.POISON,
                        200, // Long duration
                        3    // Poison level IV
                ));

                // Apply additional effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2)); // Slowness
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0)); // Blindness

                // Create strike effect
                createNeckStrikeEffect(target.getLocation().add(0, 1.5, 0), progress);

                hitEntities.add(target);
            }
        }
    }

    private void createNeckStrikeEffect(Location loc, double progress) {
        // Main strike flash
        world.spawnParticle(Particle.FLASH, loc, 3, 0.1, 0.1, 0.1, 0);

        // Strike particles
        for (int i = 0; i < 30; i++) {
            Vector spread = new Vector(
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
            ).normalize().multiply(random.nextDouble() * 1.5);

            Location particleLoc = loc.clone().add(spread);

            // Critical hit particles
            Particle.DustOptions critColor = new Particle.DustOptions(
                    Color.fromRGB(
                            clampColor(255),
                            clampColor(50 + (int)(50 * Math.sin(progress * Math.PI))),
                            clampColor(50)
                    ), 1.0f
            );

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, critColor);
            world.spawnParticle(Particle.CRIT, particleLoc, 2, 0.1, 0.1, 0.1, 0.5);
        }

        // Strike sounds
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.5f);
        world.playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 0.8f, 2.0f);
        world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
    }

}