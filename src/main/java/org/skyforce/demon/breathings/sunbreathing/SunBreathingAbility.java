package org.skyforce.demon.breathings.sunbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SunBreathingAbility {
    private final Player player;

    public SunBreathingAbility(Player player) {
        this.player = player;
    }

    /**
     * First Form: Dance (壱ノ型 円舞)
     * A single concentrated slash with the power of the sun
     */
    Random random = new Random();

    public void useFirstForm() {
        player.sendMessage("§6☀ §c壱ノ型 円舞 §6(First Form: Dance)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial sun effect and sound
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f);

        // Create initial sun particles
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 16) {
            double x = Math.cos(i) * 2;
            double z = Math.sin(i) * 2;
            Location particleLoc = startLoc.clone().add(x, 1, z);
            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
        }

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private double angle = 0;
            private Location lastPlayerLoc = player.getLocation();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                // Update player rotation
                Location playerLoc = player.getLocation();
                float yaw = playerLoc.getYaw() + 20;
                playerLoc.setYaw(yaw);
                player.teleport(playerLoc);

                // Calculate spin radius and progression
                double progress = time / MAX_DURATION;
                double radius = 3.0 + (progress * 1.5);
                angle += Math.PI / 8;

                // Create spinning sun blade effect
                for (double offset = -0.5; offset <= 0.5; offset += 0.25) {
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 16) {
                        double currentAngle = i + angle;
                        double x = Math.cos(currentAngle) * radius;
                        double z = Math.sin(currentAngle) * radius;

                        Location flameLoc = playerLoc.clone().add(x, 1 + offset, z);

                        // Main flame effect
                        world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.1, 0.1, 0.1, 0);

                        // Sun particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.FIREWORK, flameLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }

                        // Glowing trail
                        world.spawnParticle(Particle.DUST, flameLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1.0f));
                    }
                }

                // Create rising sun particles
                for (int i = 0; i < 3; i++) {
                    Location baseLoc = playerLoc.clone().add(
                            random.nextDouble() * 4 - 2,
                            0,
                            random.nextDouble() * 4 - 2
                    );

                    for (double y = 0; y < 3; y += 0.5) {
                        Location particleLoc = baseLoc.clone().add(0, y, 0);
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0, 0.1, 0.02);
                    }
                }

                // Damage entities in range
                for (Entity entity : world.getNearbyEntities(playerLoc, radius, 3, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double distance = target.getLocation().distance(playerLoc);
                        double damage = 8.0 * (1 - (distance / (radius + 1)));

                        // Apply damage and effects
                        target.damage(damage, player);
                        hitEntities.add(entity);

                        // Create hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 20, 0.3, 0.3, 0.3, 0.1);
                        world.spawnParticle(Particle.FIREWORK, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);
                        world.spawnParticle(Particle.FLASH, hitLoc, 1, 0, 0, 0, 0);

                        // Sun strike sound
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.2f);

                        // Apply fire effect
                        target.setFireTicks(60);

                        // Knockback
                        Vector knockback = target.getLocation().toVector()
                                .subtract(playerLoc.toVector())
                                .normalize()
                                .multiply(1.2)
                                .setY(0.2);
                        target.setVelocity(knockback);
                    }
                }

                // Movement trail
                Vector movement = playerLoc.toVector().subtract(lastPlayerLoc.toVector());
                if (movement.lengthSquared() > 0.01) {
                    createSunTrail(lastPlayerLoc, movement);
                }

                // Update tracking
                lastPlayerLoc = playerLoc.clone();
                time += 0.05;
            }

            private void createSunTrail(Location start, Vector movement) {
                double length = movement.length();
                Vector direction = movement.normalize();

                for (double d = 0; d < length; d += 0.5) {
                    Location trailLoc = start.clone().add(direction.clone().multiply(d));

                    // Sun trail particles
                    world.spawnParticle(Particle.FLAME, trailLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.0f));
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, false));

        addCooldown(player, "FirstForm", 10);
    }

    /**
     * Second Form: Clear Blue Sky (弐ノ型 碧羅天)
     * A powerful rising slash that represents the sun ascending into a clear sky
     */
    public void useSecondForm() {
        player.sendMessage("§6☀ §c弐ノ型 碧羅天 §6(Second Form: Clear Blue Sky)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();
        Vector direction = startLoc.getDirection().normalize();

        // Initial sun burst effect
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);

        // Initial leap
        Vector jumpVector = direction.clone().multiply(1.2).setY(1.3);
        player.setVelocity(jumpVector);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.2;
            private Location lastPos = startLoc.clone();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector currentDir = currentLoc.getDirection();

                // Create rising sun pillar
                double height = 8.0;
                for (double y = 0; y < height; y += 0.5) {
                    double radius = 2.0 * (1 - y/height); // Narrowing radius as it goes up

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location pillarLoc = currentLoc.clone().add(x, y, z);

                        // Sun pillar particles
                        world.spawnParticle(Particle.FLAME, pillarLoc, 1, 0.1, 0.1, 0.1, 0);

                        // Glowing effect
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.FIREWORK, pillarLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }

                    // Central beam
                    Location beamLoc = currentLoc.clone().add(0, y, 0);
                    world.spawnParticle(Particle.END_ROD, beamLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, beamLoc, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 223, 89), 1.0f));
                }

                // Create upward slash effect
                double slashProgress = time / MAX_DURATION;
                createUpwardSlash(currentLoc, slashProgress);

                // Check for entities to damage
                double damageRadius = 3.5;
                for (Entity entity : world.getNearbyEntities(currentLoc, damageRadius, 8, damageRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply heavy upward damage
                        target.damage(10.0, player);
                        hitEntities.add(entity);

                        // Create impact effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 25, 0.3, 0.3, 0.3, 0.15);
                        world.spawnParticle(Particle.FLASH, hitLoc, 2, 0.1, 0.1, 0.1, 0);

                        // Impact sounds
                        world.playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.2f, 1.0f);

                        // Strong upward knockback
                        Vector knockback = new Vector(0, 1.8, 0)
                                .add(target.getLocation().toVector()
                                        .subtract(currentLoc.toVector())
                                        .normalize()
                                        .multiply(0.5));
                        target.setVelocity(knockback);

                        // Apply fire effect
                        target.setFireTicks(80);
                    }
                }

                // Create movement trail
                if (currentLoc.distance(lastPos) > 0.1) {
                    Vector between = currentLoc.toVector().subtract(lastPos.toVector());
                    double length = between.length();
                    Vector step = between.normalize().multiply(0.5);

                    for (double d = 0; d < length; d += 0.5) {
                        Location trailLoc = lastPos.clone().add(step.multiply(d));

                        // Trail particles
                        world.spawnParticle(Particle.FLAME, trailLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Update position
                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void createUpwardSlash(Location center, double progress) {
                double slashHeight = 8.0 * progress;
                double slashWidth = 3.0;

                for (double y = 0; y < slashHeight; y += 0.3) {
                    double width = slashWidth * (1 - y/slashHeight);

                    for (double x = -width; x <= width; x += 0.3) {
                        Location slashLoc = center.clone().add(x, y, 0);

                        // Slash particles
                        world.spawnParticle(Particle.FLAME, slashLoc, 1, 0.1, 0.1, 0.1, 0);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.FIREWORK, slashLoc, 1, 0, 0, 0, 0.1);
                        }

                        // Sun energy particles
                        world.spawnParticle(Particle.DUST, slashLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.0f));
                    }
                }

                // Slash sound effects
                if (time * 20 % 2 == 0) {
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 25, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));

        addCooldown(player, "SecondForm", 12);
    }

    /**
     * Third Form: Raging Sun (参ノ型 烈日紅)
     * A series of powerful sun-imbued strikes resembling a raging inferno
     */
    public void useThirdForm() {
        player.sendMessage("§6☀ §c参ノ型 烈日紅 §6(Third Form: Raging Sun)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();
        Vector direction = startLoc.getDirection().normalize();

        // Initial effects
        world.playSound(startLoc, Sound.ENTITY_BLAZE_AMBIENT, 1.2f, 0.8f);
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.5f);
        world.spawnParticle(Particle.FLASH, startLoc, 3, 0.3, 0.3, 0.3, 0);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.5;
            private Location lastPos = startLoc.clone();
            private int comboCount = 0;
            private final int MAX_COMBOS = 5;

            @Override
            public void run() {
                if (time >= MAX_DURATION || comboCount >= MAX_COMBOS) {
                    // Final explosion effect
                    createSunExplosion(player.getLocation(), 5.0);
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Trigger combo attacks
                if (time * 20 % 10 == 0) {
                    performComboAttack(currentLoc);
                    comboCount++;
                }

                // Constant flame aura
                createFlameAura(currentLoc);

                // Create movement trail
                if (currentLoc.distance(lastPos) > 0.1) {
                    createSunTrail(lastPos, currentLoc);
                }

                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void performComboAttack(Location center) {
                // Dash forward
                Vector dashDir = center.getDirection().multiply(2.0);
                player.setVelocity(dashDir);

                // Create raging sun effect
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 16) {
                    double radius = 4.0;
                    double x = Math.cos(i) * radius;
                    double z = Math.sin(i) * radius;

                    Location flameLoc = center.clone().add(x, 1, z);
                    Vector toCenter = center.clone().subtract(flameLoc).toVector().normalize();

                    // Launch flames toward center
                    for (double d = 0; d < radius; d += 0.5) {
                        Location particleLoc = flameLoc.clone().add(toCenter.clone().multiply(d));

                        // Intense flame particles
                        world.spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Sun energy particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 50, 0), 1.2f));
                        }
                    }
                }

                // Create impact wave
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);

                // Damage nearby entities
                for (Entity entity : world.getNearbyEntities(center, 4.0, 3.0, 4.0)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply increasing damage with combo
                        double damage = 4.0 + (comboCount * 1.5);
                        target.damage(damage, player);
                        hitEntities.add(entity);

                        // Create hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 20, 0.3, 0.3, 0.3, 0.15);

                        // Knockback
                        Vector kb = target.getLocation().subtract(center).toVector().normalize()
                                .multiply(1.2).setY(0.3);
                        target.setVelocity(kb);

                        // Apply fire
                        target.setFireTicks(100);
                    }
                }
            }

            private void createFlameAura(Location center) {
                double radius = 1.5;
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.cos(i) * radius;
                    double z = Math.sin(i) * radius;

                    Location auraLoc = center.clone().add(x, 1, z);
                    world.spawnParticle(Particle.FLAME, auraLoc, 1, 0.1, 0.1, 0.1, 0.02);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, auraLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            private void createSunTrail(Location start, Location end) {
                Vector between = end.toVector().subtract(start.toVector());
                double length = between.length();
                Vector step = between.normalize().multiply(0.5);

                for (double d = 0; d < length; d += 0.5) {
                    Location trailLoc = start.clone().add(step.multiply(d));

                    world.spawnParticle(Particle.FLAME, trailLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1.0f));
                }
            }

            private void createSunExplosion(Location center, double radius) {
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
                world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.5f);

                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 16) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 16) {
                        double x = radius * Math.sin(theta) * Math.cos(phi);
                        double y = radius * Math.sin(theta) * Math.sin(phi);
                        double z = radius * Math.cos(theta);

                        Location explosionLoc = center.clone().add(x, z, y);

                        world.spawnParticle(Particle.FLAME, explosionLoc, 1, 0, 0, 0, 0.1);
                        world.spawnParticle(Particle.FIREWORK, explosionLoc, 1, 0, 0, 0, 0.1);
                    }
                }

                // Final damage wave
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(6.0, player);
                        target.setFireTicks(120);
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 1, false, false));

        addCooldown(player, "ThirdForm", 15);
    }

    /**
     * Fourth Form: Burning Bones, Summer Sun (肆ノ型 灼骨炎陽)
     * A devastating technique that embodies the peak of summer's heat
     */
    public void useFourthForm() {
        player.sendMessage("§6☀ §c肆ノ型 灼骨炎陽 §6(Fourth Form: Burning Bones, Summer Sun)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial heat wave effect
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 0.5f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.8f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);

        Set<Entity> hitEntities = new HashSet<>();
        Map<Entity, Integer> burnStacks = new HashMap<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private Location lastPos = startLoc.clone();
            private double heatRadius = 2.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    // Final heat explosion
                    createHeatExplosion(player.getLocation(), 8.0);
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                heatRadius = 2.0 + (time * 2); // Expanding heat radius

                // Create intense heat aura
                createHeatAura(currentLoc, heatRadius);

                // Burning bones effect
                if (time * 20 % 5 == 0) {
                    performBurningBonesAttack(currentLoc);
                }

                // Damage and effect nearby entities
                for (Entity entity : world.getNearbyEntities(currentLoc, heatRadius, heatRadius, heatRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply or increase burn stacks
                        int currentStacks = burnStacks.getOrDefault(entity, 0);
                        if (currentStacks < 5 && !hitEntities.contains(entity)) {
                            burnStacks.put(entity, currentStacks + 1);
                            hitEntities.add(entity);

                            // Damage increases with burn stacks
                            double damage = 3.0 + (currentStacks * 1.5);
                            target.damage(damage, player);

                            // Extended fire duration with stacks
                            int fireTicks = 60 + (currentStacks * 40);
                            target.setFireTicks(Math.max(target.getFireTicks(), fireTicks));

                            // Visual effects for burn stacks
                            Location targetLoc = target.getLocation().add(0, 1, 0);
                            createBurnStackEffect(targetLoc, currentStacks);
                        }
                    }
                }

                // Allow entities to be hit again after delay
                if (time * 20 % 10 == 0) {
                    hitEntities.clear();
                }

                // Create summer sun particles
                createSummerSunEffects(currentLoc);

                // Movement trail
                if (currentLoc.distance(lastPos) > 0.1) {
                    createHeatTrail(lastPos, currentLoc);
                }

                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void createHeatAura(Location center, double radius) {
                for (double y = 0; y < 3; y += 0.5) {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location heatLoc = center.clone().add(x, y, z);

                        // Intense heat particles
                        world.spawnParticle(Particle.FLAME, heatLoc, 1, 0.1, 0.1, 0.1, 0);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.LAVA, heatLoc, 1, 0.1, 0.1, 0.1, 0);
                            world.spawnParticle(Particle.DUST, heatLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 40, 0), 1.5f));
                        }
                    }
                }
            }

            private void performBurningBonesAttack(Location center) {
                // Create circular flame wave
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 32) {
                    double x = Math.cos(angle) * heatRadius;
                    double z = Math.sin(angle) * heatRadius;

                    Location flameLoc = center.clone().add(x, 0, z);
                    Vector direction = flameLoc.toVector().subtract(center.toVector()).normalize();

                    // Launch flame projectiles
                    for (double d = 0; d < 3; d += 0.5) {
                        Location projectileLoc = flameLoc.clone().add(direction.clone().multiply(d));
                        world.spawnParticle(Particle.FLAME, projectileLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, projectileLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Attack sound
                world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.6f);
                world.playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.5f);
            }

            private void createBurnStackEffect(Location center, int stacks) {
                // Visual indication of burn stacks
                float pitch = 0.8f + (stacks * 0.1f);
                world.playSound(center, Sound.BLOCK_FIRE_AMBIENT, 1.0f, pitch);

                // Stack-based particles
                for (int i = 0; i < stacks; i++) {
                    world.spawnParticle(Particle.FLAME, center, 10, 0.2, 0.2, 0.2, 0.1);
                    world.spawnParticle(Particle.LAVA, center, 3, 0.2, 0.2, 0.2, 0);
                }
            }

            private void createSummerSunEffects(Location center) {
                // Rising heat particles
                for (int i = 0; i < 3; i++) {
                    Location baseLoc = center.clone().add(
                            random.nextDouble() * heatRadius * 2 - heatRadius,
                            0,
                            random.nextDouble() * heatRadius * 2 - heatRadius
                    );

                    for (double y = 0; y < 4; y += 0.5) {
                        Location particleLoc = baseLoc.clone().add(0, y, 0);
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0, 0.1, 0.02);
                        }
                    }
                }
            }

            private void createHeatExplosion(Location center, double radius) {
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                world.playSound(center, Sound.ENTITY_BLAZE_DEATH, 1.5f, 0.6f);

                // Spherical explosion effect
                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 16) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 16) {
                        double x = radius * Math.sin(theta) * Math.cos(phi);
                        double y = radius * Math.sin(theta) * Math.sin(phi);
                        double z = radius * Math.cos(theta);

                        Location explosionLoc = center.clone().add(x, z, y);
                        world.spawnParticle(Particle.EXPLOSION, explosionLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, explosionLoc, 3, 0.2, 0.2, 0.2, 0.1);
                        world.spawnParticle(Particle.LAVA, explosionLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Final damage wave
                for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        int stacks = burnStacks.getOrDefault(entity, 0);
                        double finalDamage = 8.0 + (stacks * 2.0);
                        target.damage(finalDamage, player);
                        target.setFireTicks(200);
                    }
                }
            }

            private void createHeatTrail(Location start, Location end) {
                Vector between = end.toVector().subtract(start.toVector());
                double length = between.length();
                Vector step = between.normalize().multiply(0.5);

                for (double d = 0; d < length; d += 0.5) {
                    Location trailLoc = start.clone().add(step.multiply(d));
                    world.spawnParticle(Particle.FLAME, trailLoc, 2, 0.1, 0.1, 0.1, 0.02);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.LAVA, trailLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false));

        addCooldown(player, "FourthForm", 18);
    }

    /**
     * Fifth Form: Setting Sun Transformation (斜陽転身)
     * A precise decapitation technique with an aerial backflip
     */
    public void useFifthForm() {
        player.sendMessage("§6☀ §c斜陽転身 §6(Fifth Form: Setting Sun Transformation)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();
        Vector direction = startLoc.getDirection();

        // Initial sound effects
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.5f);

        // Initial backflip jump
        Vector backflipVelocity = direction.clone()
                .multiply(-1.0) // Backward movement
                .setY(1.2);    // Upward force
        player.setVelocity(backflipVelocity);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0;
            private boolean hasSlashed = false;
            private Location peakLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Track the peak of the jump for the slash
                if (peakLocation == null && player.getVelocity().getY() <= 0) {
                    peakLocation = currentLoc.clone();
                    performSlash(currentLoc);
                    hasSlashed = true;
                }

                // Create trailing sun particles during backflip
                if (!hasSlashed) {
                    createBackflipTrail(currentLoc);
                }

                time += 0.05;
            }

            private void performSlash(Location slashLoc) {
                // Powerful slash sound
                world.playSound(slashLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.8f);
                world.playSound(slashLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);

                Vector slashDirection = slashLoc.getDirection();
                double slashLength = 5.0; // Length of the slash

                // Create horizontal slash effect
                for (double d = -slashLength; d <= slashLength; d += 0.2) {
                    Vector right = slashDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();
                    Location slashPoint = slashLoc.clone().add(right.multiply(d));

                    // Main slash particles
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashPoint, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.FLAME, slashPoint, 2, 0.1, 0.1, 0.1, 0.05);

                    // Sun energy particles
                    world.spawnParticle(Particle.DUST, slashPoint, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1.2f));

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, slashPoint, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Check for entities in slash range
                for (Entity entity : world.getNearbyEntities(slashLoc, slashLength, 2, slashLength)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Critical hit damage
                        target.damage(12.0, player);
                        hitEntities.add(entity);

                        // Create hit effects
                        Location hitLoc = target.getLocation().add(0, 1.6, 0); // Head height
                        world.spawnParticle(Particle.FLASH, hitLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);

                        // Critical hit sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);

                        // Brief fire effect
                        target.setFireTicks(40);
                    }
                }
            }

            private void createBackflipTrail(Location current) {
                // Create trailing sun particles during the backflip
                for (int i = 0; i < 2; i++) {
                    double offset = random.nextDouble() * 0.5;
                    Location trailLoc = current.clone().add(
                            random.nextDouble() * 0.5 - 0.25,
                            offset,
                            random.nextDouble() * 0.5 - 0.25
                    );

                    // Trail particles
                    world.spawnParticle(Particle.FLAME, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 165, 0), 0.8f));
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply brief effects during the technique
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 25, 0, false, false));

        addCooldown(player, "FifthForm", 12);
    }

    /**
     * Sixth Form: Solar Heat Haze (陸ノ型 陽炎眩)
     * Creates a deceptive heat haze effect to mask the true strike location
     */
    public void useSixthForm() {
        player.sendMessage("§6☀ §c陸ノ型 陽炎眩 §6(Sixth Form: Solar Heat Haze)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial effects
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 1.2f);

        // Create initial heat distortion
        createHeatHaze(startLoc, 3.0);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private Location lastPos = startLoc.clone();
            private boolean hasStruck = false;
            private List<Location> illusionPoints = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Initial rush phase
                if (time < 0.3 && !hasStruck) {
                    // Create multiple illusion points
                    createIllusions(currentLoc);

                    // Rush forward
                    Vector velocity = direction.clone().multiply(1.2).setY(0.1);
                    player.setVelocity(velocity);
                }

                // Strike phase
                if (time >= 0.3 && !hasStruck) {
                    performHeatHazeStrike(currentLoc, direction);
                    hasStruck = true;
                }

                // Create heat haze effects
                createHeatDistortion(currentLoc);

                // Update illusion positions
                updateIllusions();

                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void createIllusions(Location center) {
                illusionPoints.clear();

                // Create multiple illusion points around the player
                for (int i = 0; i < 3; i++) {
                    double angle = (Math.PI * 2 * i / 3) + (time * 5);
                    double radius = 2.0;

                    Location illusion = center.clone().add(
                            Math.cos(angle) * radius,
                            0,
                            Math.sin(angle) * radius
                    );

                    illusionPoints.add(illusion);
                }
            }

            private void updateIllusions() {
                for (Location illusion : illusionPoints) {
                    // Create illusory blade effects
                    createIllusoryBlade(illusion);

                    // Heat haze particles
                    world.spawnParticle(Particle.CRIT, illusion, 5, 0.2, 0.2, 0.2, 0);
                    world.spawnParticle(Particle.DUST, illusion, 2, 0.2, 0.2, 0.2,
                            new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.0f));
                }
            }

            private void createHeatDistortion(Location center) {
                double radius = 3.0;
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.cos(i) * radius;
                    double z = Math.sin(i) * radius;

                    Location distortLoc = center.clone().add(x, random.nextDouble() * 2, z);

                    // Heat distortion particles
                    world.spawnParticle(Particle.FLAME, distortLoc, 1, 0.1, 0.1, 0.1, 0);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, distortLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            private void createIllusoryBlade(Location location) {
                Vector direction = location.getDirection();
                double length = 2.5;

                for (double d = 0; d < length; d += 0.2) {
                    Location bladeLoc = location.clone().add(direction.clone().multiply(d));

                    // Illusory blade particles
                    world.spawnParticle(Particle.DUST, bladeLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 140, 0), 0.8f));

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.FLAME, bladeLoc, 1, 0.05, 0.05, 0.05, 0.01);
                    }
                }
            }

            private void performHeatHazeStrike(Location center, Vector direction) {
                // True strike effect
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.5f);
                world.playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);

                // Calculate true strike area
                double strikeLength = 4.5; // Actual strike length
                Vector strikeDirection = direction.clone().normalize();

                for (double d = 0; d < strikeLength; d += 0.2) {
                    Location strikeLoc = center.clone().add(strikeDirection.clone().multiply(d));

                    // True strike particles
                    world.spawnParticle(Particle.SWEEP_ATTACK, strikeLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.FLAME, strikeLoc, 3, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.END_ROD, strikeLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }

                // Check for entities in true strike range
                for (Entity entity : world.getNearbyEntities(center, strikeLength, 2, strikeLength)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate angle to target
                        Vector toTarget = target.getLocation().subtract(center).toVector();
                        double angle = direction.angle(toTarget);

                        if (angle < Math.PI / 3) { // 60-degree cone
                            // Apply damage
                            target.damage(8.0, player);
                            hitEntities.add(entity);

                            // Hit effects
                            Location hitLoc = target.getLocation().add(0, 1, 0);
                            world.spawnParticle(Particle.FLASH, hitLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.FLAME, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);

                            // Hit sounds
                            world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);

                            // Apply fire effect
                            target.setFireTicks(60);

                            // Knockback
                            Vector knockback = direction.clone().multiply(1.2).setY(0.2);
                            target.setVelocity(knockback);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));

        addCooldown(player, "SixthForm", 14);
    }

    private void createHeatHaze(Location center, double radius) {
        for (int i = 0; i < 50; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = random.nextDouble() * radius;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            double y = random.nextDouble() * 2;

            Location hazeLoc = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.WHITE_SMOKE, hazeLoc, 1, 0.1, 0.1, 0.1, 0);
        }
    }

    /**
     * Seventh Form: Sunflower Thrust (漆ノ型 陽華突)
     * A powerful thrusting technique that follows the movement of a sunflower
     */
    public void useSeventhForm() {
        player.sendMessage("§6☀ §c漆ノ型 陽華突 §6(Seventh Form: Sunflower Thrust)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial sun effect
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.2f);

        createSunflowerEffect(startLoc, world);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0;
            private Location lastPos = startLoc.clone();
            private boolean hasThrust = false;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Initial sunflower stance
                if (time < 0.3 && !hasThrust) {
                    createSunflowerStance(currentLoc);
                }

                // Execute thrust
                if (time >= 0.3 && !hasThrust) {
                    performSunflowerThrust(currentLoc, direction);
                    hasThrust = true;
                }

                // Create trailing effects
                if (hasThrust) {
                    createThrustTrail(currentLoc);
                }

                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void createSunflowerStance(Location center) {
                // Create sunflower petals effect
                double radius = 1.5;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location petalLoc = center.clone().add(x, 1, z);

                    // Petal particles
                    world.spawnParticle(Particle.DUST, petalLoc, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, petalLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            private void performSunflowerThrust(Location center, Vector direction) {
                // Thrust forward
                Vector thrustVelocity = direction.clone().multiply(2.0);
                player.setVelocity(thrustVelocity);

                // Thrust effect
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.5f);
                world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);

                // Create thrust particles
                double thrustLength = 6.0;
                Vector thrustDir = direction.clone().normalize();

                for (double d = 0; d < thrustLength; d += 0.2) {
                    Location thrustLoc = center.clone().add(thrustDir.clone().multiply(d));

                    // Thrust particles
                    world.spawnParticle(Particle.SWEEP_ATTACK, thrustLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.FLAME, thrustLoc, 2, 0.1, 0.1, 0.1, 0.05);

                    // Sunflower energy particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, thrustLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, thrustLoc, 2, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.0f));
                    }
                }

                // Check for hits
                for (Entity entity : world.getNearbyEntities(center, thrustLength, 2, thrustLength)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate if entity is in thrust direction
                        Vector toTarget = target.getLocation().subtract(center).toVector();
                        double angle = direction.angle(toTarget);

                        if (angle < Math.PI / 4) { // 45-degree cone
                            // Apply thrust damage
                            target.damage(10.0, player);
                            hitEntities.add(entity);

                            // Create hit effects
                            Location hitLoc = target.getLocation().add(0, 1, 0);
                            world.spawnParticle(Particle.FLASH, hitLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.FLAME, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);

                            // Hit sounds
                            world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                            world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.5f);

                            // Apply effects
                            target.setFireTicks(60);

                            // Strong knockback in thrust direction
                            Vector knockback = direction.clone().multiply(2.0).setY(0.2);
                            target.setVelocity(knockback);
                        }
                    }
                }
            }

            private void createThrustTrail(Location center) {
                // Create trailing sunflower petals
                for (int i = 0; i < 3; i++) {
                    Location trailLoc = center.clone().add(
                            random.nextDouble() * 2 - 1,
                            random.nextDouble() * 2,
                            random.nextDouble() * 2 - 1
                    );

                    world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, trailLoc, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f));
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 25, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 2, false, false));

        addCooldown(player, "SeventhForm", 12);
    }

    private void createSunflowerEffect(Location center, World world) {
        // Create initial sunflower burst
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
            double radius = 2.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location petalLoc = center.clone().add(x, 1, z);

            world.spawnParticle(Particle.FLAME, petalLoc, 2, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticle(Particle.DUST, petalLoc, 2, 0.1, 0.1, 0.1,
                    new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
        }
    }


    /**
     * Eighth Form: Roaring Flash Sun (捌ノ型 耀閃陽)
     * A series of thunderous strikes with the speed and intensity of solar flares
     */
    public void useEighthForm() {
        player.sendMessage("§6☀ §c捌ノ型 耀閃陽 §6(Eighth Form: Roaring Flash Sun)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder effect
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.8f);

        Set<Entity> hitEntities = new HashSet<>();
        int[] strikeCount = {0};
        final int MAX_STRIKES = 4;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private Location lastPos = startLoc.clone();
            private long lastStrikeTime = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION || strikeCount[0] >= MAX_STRIKES) {
                    // Final thunder effect
                    createThunderEffect(player.getLocation());
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Execute thunder strikes
                if (System.currentTimeMillis() - lastStrikeTime > 300 && strikeCount[0] < MAX_STRIKES) { // 300ms between strikes
                    performThunderStrike(currentLoc);
                    strikeCount[0]++;
                    lastStrikeTime = System.currentTimeMillis();
                }

                // Continuous effects
                createRoaringEffects(currentLoc);

                lastPos = currentLoc.clone();
                time += 0.05;
            }

            private void performThunderStrike(Location center) {
                // Thunder sound effects
                world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
                world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);

                // Get target location
                Vector direction = center.getDirection();
                double strikeRange = 4.0;

                // Create thunder strike effect
                for (double d = 0; d < strikeRange; d += 0.2) {
                    Location strikeLoc = center.clone().add(direction.clone().multiply(d));

                    // Main strike beam
                    world.spawnParticle(Particle.FLASH, strikeLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.FLAME, strikeLoc, 3, 0.1, 0.1, 0.1, 0.05);

                    // Thunder energy particles
                    world.spawnParticle(Particle.END_ROD, strikeLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticle(Particle.DUST, strikeLoc, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 0), 1.2f));
                }

                // Dash effect
                Vector dashVelocity = direction.clone().multiply(1.5).setY(0.2);
                player.setVelocity(dashVelocity);

                // Check for hits
                for (Entity entity : world.getNearbyEntities(center, strikeRange, 2, strikeRange)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate if entity is in strike direction
                        Vector toTarget = target.getLocation().subtract(center).toVector();
                        double angle = direction.angle(toTarget);

                        if (angle < Math.PI / 3) { // 60-degree cone
                            // Apply increasing damage with each strike
                            double damage = 5.0 + (strikeCount[0] * 1.5);
                            target.damage(damage, player);
                            hitEntities.add(entity);

                            // Create hit effects
                            Location hitLoc = target.getLocation().add(0, 1, 0);
                            createThunderImpact(hitLoc);

                            // Knockback effect
                            Vector knockback = direction.clone().multiply(1.0).setY(0.2);
                            target.setVelocity(knockback);

                            // Apply fire
                            target.setFireTicks(40);
                        }
                    }
                }
            }

            private void createRoaringEffects(Location center) {
                // Create roaring sun aura
                double radius = 2.0;
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.cos(i) * radius;
                    double z = Math.sin(i) * radius;

                    Location auraLoc = center.clone().add(x, 1, z);

                    // Roaring sun particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLAME, auraLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, auraLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.0f));
                    }
                }
            }

            private void createThunderImpact(Location location) {
                // Thunder impact particles
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.FLASH, location, 2, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.FLAME, location, 15, 0.2, 0.2, 0.2, 0.1);

                // Thunder impact sounds
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.8f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
            }

            private void createThunderEffect(Location location) {
                // Final thunder visual effects
                for (double radius = 0; radius < 4; radius += 0.5) {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location effectLoc = location.clone().add(x, 0.5, z);
                        world.spawnParticle(Particle.FLASH, effectLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, effectLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Final thunder sound
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1.5f);
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, false, false));

        addCooldown(player, "EighthForm", 15);
    }

    public void useNinthForm() {
        player.sendMessage("§6☀ §c伍ノ型 炎環日輪 §6(Fifth Form: Sun Halo Dragon Sun)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial summoning effects
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);

        // Create initial sun halo
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 32) {
            Location haloLoc = startLoc.clone().add(
                    Math.cos(i) * 3,
                    2,
                    Math.sin(i) * 3
            );
            world.spawnParticle(Particle.FLASH, haloLoc, 1, 0, 0, 0, 0);
        }

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 4.0;
            private double dragonAngle = 0;
            private List<Location> dragonPoints = new ArrayList<>();
            private double spiralRadius = 3.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    // Final sun explosion
                    createSunHaloExplosion(player.getLocation());
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                dragonAngle += Math.PI / 16;

                // Update dragon body points
                updateDragonPoints(currentLoc);

                // Create sun dragon
                createSunDragon();

                // Create sun halo
                createSunHalo(currentLoc);

                // Damage entities
                checkAndDamageEntities(currentLoc);

                // Clear hit entities periodically
                if (time * 20 % 10 == 0) {
                    hitEntities.clear();
                }

                time += 0.05;
            }

            private void updateDragonPoints(Location center) {
                dragonPoints.clear();
                double height = 2.0;

                // Create dragon spine points
                for (double t = 0; t < 2; t += 0.1) {
                    double spiralX = spiralRadius * Math.cos(dragonAngle + (t * Math.PI));
                    double spiralZ = spiralRadius * Math.sin(dragonAngle + (t * Math.PI));
                    double spiralY = height + Math.sin(t * Math.PI * 2) * 1.5;

                    dragonPoints.add(center.clone().add(spiralX, spiralY, spiralZ));
                }
            }

            private void createSunDragon() {
                // Create dragon body
                for (int i = 0; i < dragonPoints.size(); i++) {
                    Location point = dragonPoints.get(i);
                    double size = 1.0;
                    if (i == 0) size = 1.5; // Larger head

                    // Dragon body particles
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * size;
                        double z = Math.sin(angle) * size;
                        Location dragonLoc = point.clone().add(x, 0, z);

                        // Main dragon body
                        world.spawnParticle(Particle.FLAME, dragonLoc, 1, 0.1, 0.1, 0.1, 0);

                        // Energy particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, dragonLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            world.spawnParticle(Particle.DUST, dragonLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.2f));
                        }
                    }

                    // Dragon roar effects
                    if (i == 0 && time * 20 % 20 == 0) {
                        world.playSound(point, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
                        world.spawnParticle(Particle.FLASH, point, 2, 0.1, 0.1, 0.1, 0);
                    }
                }
            }

            private void createSunHalo(Location center) {
                double haloRadius = 3.0;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle + dragonAngle) * haloRadius;
                    double z = Math.sin(angle + dragonAngle) * haloRadius;

                    Location haloLoc = center.clone().add(x, 2, z);

                    // Halo particles
                    world.spawnParticle(Particle.FLAME, haloLoc, 1, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.DUST, haloLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.0f));

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, haloLoc, 1, 0, 0, 0, 0.05);
                    }
                }
            }

            private void checkAndDamageEntities(Location center) {
                // Check for entities near the dragon body
                for (Location point : dragonPoints) {
                    for (Entity entity : world.getNearbyEntities(point, 2, 2, 2)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply damage
                            target.damage(6.0, player);
                            hitEntities.add(entity);

                            // Dragon strike effects
                            Location hitLoc = target.getLocation().add(0, 1, 0);
                            world.spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.FLAME, hitLoc, 20, 0.3, 0.3, 0.3, 0.2);

                            // Strike sounds
                            world.playSound(hitLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.5f);

                            // Apply fire and knockback
                            target.setFireTicks(100);
                            Vector knockback = target.getLocation().subtract(point).toVector()
                                    .normalize().multiply(1.5).setY(0.5);
                            target.setVelocity(knockback);
                        }
                    }
                }
            }

            private void createSunHaloExplosion(Location center) {
                // Final explosion effects
                world.playSound(center, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.5f, 0.8f);
                world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.2f);

                // Create expanding ring
                for (double radius = 0; radius < 8; radius += 0.5) {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location explosionLoc = center.clone().add(x, 1, z);
                        world.spawnParticle(Particle.FLAME, explosionLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.END_ROD, explosionLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Final damage wave
                for (Entity entity : world.getNearbyEntities(center, 8, 4, 8)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(8.0, player);
                        target.setFireTicks(140);

                        // Knockback from center
                        Vector kb = target.getLocation().subtract(center).toVector()
                                .normalize().multiply(2.0).setY(0.5);
                        target.setVelocity(kb);
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 0, false, false));

        addCooldown(player, "FifthForm", 20);
    }


    /**
     * Tenth Form: Fire Wheel (拾ノ型 火車)
     * A high aerial flip followed by a precise landing and vertical circular slash
     */
    public void useTenthForm() {
        player.sendMessage("§6☀ §c拾ノ型 火車 §6(Tenth Form: Fire Wheel)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial leap sound
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.2f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicReference<Entity> target = new AtomicReference<>(null);

        // Find nearest target
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : world.getNearbyEntities(startLoc, 4, 4, 4)) {
            if (entity instanceof LivingEntity && entity != player) {
                double distance = entity.getLocation().distance(startLoc);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    target.set(entity);
                }
            }
        }

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.2;
            private boolean hasFlipped = false;
            private boolean hasLanded = false;
            private boolean hasSlashed = false;
            private Location targetLoc = null;
            private Vector landingSpot = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION || (hasSlashed && time > 0.4)) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (target.get() != null) {
                    targetLoc = target.get().getLocation();

                    // Phase 1: Higher initial flip
                    if (!hasFlipped) {
                        executeHighFlip(currentLoc, targetLoc);
                        hasFlipped = true;
                    }
                    // Phase 2: Guide to landing
                    else if (hasFlipped && !hasLanded) {
                        if (player.isOnGround() || time >= 0.8) { // Längere Zeit in der Luft
                            landBehindTarget(currentLoc, targetLoc);
                            hasLanded = true;
                        } else {
                            guidePath(currentLoc, targetLoc);
                        }
                    }
                    // Phase 3: Execute slash after landing
                    else if (hasLanded && !hasSlashed) {
                        executeCircularSlash(currentLoc, targetLoc);
                        hasSlashed = true;
                    }
                }

                // Continuous effects
                createFireWheelEffects(currentLoc);

                time += 0.05;
            }

            private void executeHighFlip(Location current, Location targetLoc) {
                // Deutlich höherer Sprung
                Vector upVector = new Vector(0, 2.5, 0); // Erhöhte Sprunghöhe
                landingSpot = targetLoc.clone().add(targetLoc.getDirection().multiply(-1)).toVector();

                player.setVelocity(upVector);

                // Intensivere Sprungeffekte
                for (int i = 0; i < 12; i++) {
                    Location flipLoc = current.clone().add(
                            random.nextDouble() * 0.8 - 0.4,
                            random.nextDouble() * 1.2, // Höhere Partikeleffekte
                            random.nextDouble() * 0.8 - 0.4
                    );

                    world.spawnParticle(Particle.FLAME, flipLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.END_ROD, flipLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }

                // Intensiverer Sprungsound
                world.playSound(current, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.8f);
                world.playSound(current, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);
            }

            // [Rest der Methoden bleiben gleich wie in der vorherigen Version]

            private void guidePath(Location current, Location targetLoc) {
                if (landingSpot != null) {
                    Vector toTarget = landingSpot.clone().subtract(current.toVector());
                    Vector guidance = toTarget.normalize().multiply(0.3);
                    guidance.setY(Math.min(guidance.getY(), -0.2)); // Sanfter Abstieg

                    player.setVelocity(guidance);
                }
            }

            private void landBehindTarget(Location current, Location targetLoc) {
                Vector toTarget = targetLoc.toVector().subtract(current.toVector());
                if (toTarget.length() > 1) {
                    Vector approach = toTarget.normalize().multiply(0.5);
                    approach.setY(0);
                    player.setVelocity(approach);
                }

                world.playSound(current, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
            }

            private void executeCircularSlash(Location current, Location targetLoc) {
                if (current.distance(targetLoc) > 3) {
                    return;
                }

                // [Rest der Slash-Logik bleibt gleich]
                world.playSound(current, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.8f);
                world.playSound(current, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);

                double radius = 1.8;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    for (double y = 0; y < 2.5; y += 0.2) {
                        Location slashLoc = current.clone().add(x, y, z);

                        world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, slashLoc, 2, 0.1, 0.1, 0.1, 0.05);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, slashLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            world.spawnParticle(Particle.DUST, slashLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 69, 0), 1.2f));
                        }
                    }
                }

                // Damage and effects
                for (Entity entity : world.getNearbyEntities(current, radius, 2.5, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity targetEntity = (LivingEntity) entity;

                        targetEntity.damage(16.0, player);
                        hitEntities.add(entity);

                        Location hitLoc = targetEntity.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLASH, hitLoc, 2, 0.1, 0.1, 0.1, 0);

                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.5f);

                        targetEntity.setFireTicks(100);
                        Vector knockback = targetEntity.getLocation().subtract(current).toVector()
                                .normalize().multiply(0.8).setY(0.2);
                        targetEntity.setVelocity(knockback);
                    }
                }
            }

            private void createFireWheelEffects(Location center) {
                for (int i = 0; i < 4; i++) {
                    Location trailLoc = center.clone().add(
                            random.nextDouble() * 1.5 - 0.75,
                            random.nextDouble() * 1.5,
                            random.nextDouble() * 1.5 - 0.75
                    );

                    world.spawnParticle(Particle.FLAME, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 25, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0, false, false)); // Bessere Luftkontrolle

        addCooldown(player, "TenthForm", 14);
    }

    /**
     * Eleventh Form: Fake Rainbow (拾壱ノ型 幻日虹)
     * Creates high-speed movements with beautiful rainbow afterimages
     */
    public void useEleventhForm() {
        player.sendMessage("§6☀ §c拾壱ノ型 幻日虹 §6(Eleventh Form: Fake Rainbow)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial effect sounds
        world.playSound(startLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.8f);

        List<Location> afterimageLocations = new ArrayList<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.5;
            private double rotationAngle = 0;
            private Location lastLocation = startLoc.clone();
            private int afterimageCount = 0;
            private final int MAX_AFTERIMAGES = 15;
            private int movementPhase = 0; // 0=vorwärts, 1=links, 2=rückwärts, 3=rechts
            private int movementTicks = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    fadeOutAfterimages();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Execute movement pattern
                performMovement(currentLoc);

                // Create and manage afterimages
                manageAfterimages(currentLoc);

                // Update rotation for visual effects only
                rotationAngle += Math.PI / 8;
                time += 0.05;
                movementTicks++;
                lastLocation = currentLoc.clone();
            }

            private void performMovement(Location current) {
                // Wechsel der Bewegungsrichtung alle 10 Ticks
                if (movementTicks >= 10) {
                    movementPhase = (movementPhase + 1) % 3;
                    movementTicks = 0;
                    // Sound für Richtungswechsel und Nachbild erstellen
                    world.playSound(current, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f + (movementPhase * 0.2f));
                    // Hier wird bei jedem Richtungswechsel ein Nachbild erzwungen
                    createAfterimage(current.clone());
                    afterimageCount++;
                }

                Vector direction;
                double speed = 0.5; // Kontrollierte Geschwindigkeit

                // Bestimme Bewegungsrichtung
                switch (movementPhase) {
                    case 0: // Vorwärts
                        direction = current.getDirection();
                        break;
                    case 1: // Links
                        direction = current.getDirection().rotateAroundY(Math.PI / 2);
                        break;
                    case 2: // Rückwärts
                        direction = current.getDirection().multiply(-1);
                        break;
                    case 3: // Rechts
                    default:
                        direction = current.getDirection().rotateAroundY(-Math.PI / 2);
                        break;
                }

                // Kleine Wellenbewegung hinzufügen
                direction.setY(Math.sin(time * 8) * 0.1);
                direction.multiply(speed);

                // Bewegung anwenden
                player.setVelocity(direction);

                // Die visuellen Effekte aus der ursprünglichen Version beibehalten
                createRainbowTrail(current);
            }

            // [Der Rest des Codes bleibt identisch zur ersten Version]
            private void createRainbowTrail(Location location) {
                double radius = 0.8;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    double x = Math.cos(angle + rotationAngle) * radius;
                    double z = Math.sin(angle + rotationAngle) * radius;

                    Location particleLoc = location.clone().add(x, 0.1, z);

                    // Rainbow color effect using different colored particles
                    switch ((int) ((angle / (Math.PI * 2)) * 6)) {
                        case 0: // Red
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f));
                            break;
                        case 1: // Orange
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.0f));
                            break;
                        case 2: // Yellow
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(255, 255, 0), 1.0f));
                            break;
                        case 3: // Green
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f));
                            break;
                        case 4: // Blue
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0, 0, 255), 1.0f));
                            break;
                        case 5: // Purple
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(148, 0, 211), 1.0f));
                            break;
                    }
                }
            }

            private void manageAfterimages(Location currentLoc) {
                // Zusätzliche Nachbilder während der Bewegung
                if (lastLocation.distance(currentLoc) > 0.35 && afterimageCount < MAX_AFTERIMAGES) {
                    // Optional: Hier können Sie auch einen zusätzlichen Sound hinzufügen
                    if (movementTicks % 5 == 0) { // Alle 5 Ticks während der Bewegung
                        world.playSound(currentLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 2.0f);
                        createAfterimage(lastLocation.clone());
                        afterimageCount++;
                    }
                }

                // Update existing afterimages
                Iterator<Location> iterator = afterimageLocations.iterator();
                while (iterator.hasNext()) {
                    Location afterimageLoc = iterator.next();
                    createAfterimageEffect(afterimageLoc);

                    // Remove old afterimages
                    if (random.nextFloat() < 0.01) {
                        iterator.remove();
                        afterimageCount--;
                    }
                }
            }

            private void createAfterimage(Location location) {
                afterimageLocations.add(location);

                // Initial afterimage creation effect
                world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.END_ROD, location, 5, 0.2, 0.4, 0.2, 0.02);

                // Afterimage sound
                world.playSound(location, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.5f, 2.0f);
            }

            private void createAfterimageEffect(Location location) {
                double height = 2.0;
                for (double y = 0; y < height; y += 0.25) {
                    double radius = (y < 0.8 || y > 1.6) ? 0.15 : 0.3; // Thinner at legs and head
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location particleLoc = location.clone().add(x, y, z);

                        // Rainbow colored afterimage
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(
                                        (int)(Math.sin(time * 5) * 127 + 128),
                                        (int)(Math.sin(time * 5 + 2) * 127 + 128),
                                        (int)(Math.sin(time * 5 + 4) * 127 + 128)
                                ), 0.8f));
                    }
                }
            }

            private void fadeOutAfterimages() {
                new BukkitRunnable() {
                    private int fadeSteps = 10;

                    @Override
                    public void run() {
                        if (fadeSteps <= 0 || afterimageLocations.isEmpty()) {
                            this.cancel();
                            return;
                        }

                        Iterator<Location> iterator = afterimageLocations.iterator();
                        while (iterator.hasNext()) {
                            Location loc = iterator.next();
                            world.spawnParticle(Particle.END_ROD, loc, 5, 0.4, 0.8, 0.4, 0.02);
                            if (random.nextFloat() < 0.3) {
                                iterator.remove();
                            }
                        }

                        fadeSteps--;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 2L);
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 50, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 1, false, false));

        addCooldown(player, "EleventhForm", 16);
    }
    /**
     * Twelfth Form: Flame Dance (拾弐ノ型 炎舞)
     * A powerful two-strike combo: vertical downward slash transitioning into a horizontal sweep
     */
    public void useTwelfthForm() {
        player.sendMessage("§6☀ §c拾弐ノ型 炎舞 §6(Twelfth Form: Flame Dance)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial effect sounds
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.6f);
        world.playSound(startLoc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0; // Faster execution
            private boolean isSecondStrike = false;
            private Location lastPlayerLoc = startLoc.clone();
            private Vector slashDirection = startLoc.getDirection();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // First strike (0-0.4 seconds)
                if (time < 0.4) {
                    performFirstStrike(currentLoc);
                }
                // Transition phase (0.4-0.5 seconds)
                else if (time < 0.5) {
                    transitionPhase(currentLoc);
                }
                // Second strike (0.5-1.0 seconds)
                else {
                    performSecondStrike(currentLoc);
                }

                lastPlayerLoc = currentLoc.clone();
                time += 0.05;
            }

            private void performFirstStrike(Location current) {
                // Quick upward motion followed by downward slash
                if (time < 0.2) {
                    // Initial leap
                    Vector upward = new Vector(0, 0.8, 0);
                    player.setVelocity(upward);

                    // Rising flame effects
                    createRisingFlames(current);
                } else {
                    // Downward slash with forward momentum
                    Vector downward = current.getDirection().multiply(0.5).setY(-1.2);
                    player.setVelocity(downward);

                    // Vertical slash effects
                    createVerticalSlashEffects(current);

                    // Check for hits
                    checkVerticalHits(current);
                }
            }

            private void createRisingFlames(Location location) {
                // Spiral of flames rising upward
                double height = 2.5;
                for (double y = 0; y < height; y += 0.2) {
                    double radius = 0.8 - (y / height * 0.5);
                    double angleOffset = y * 2;

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 6) {
                        double x = Math.cos(angle + angleOffset) * radius;
                        double z = Math.sin(angle + angleOffset) * radius;

                        Location flameLoc = location.clone().add(x, y, z);

                        // Main flame
                        world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.05, 0.05, 0.05, 0.02);

                        // Ember effect
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, flameLoc, 2, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 120 + random.nextInt(50), 0), 1.2f));
                        }
                    }
                }
            }

            private void createVerticalSlashEffects(Location location) {
                Vector direction = location.getDirection();
                double slashHeight = 3.0;

                // Main slash trail
                for (double y = 0; y < slashHeight; y += 0.15) {
                    Location slashLoc = location.clone().add(0, slashHeight - y, 0);

                    // Concentrated flame core
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.FLAME, slashLoc, 2, 0.1, 0, 0.1, 0.05);

                    // Wide flame wave
                    double waveWidth = 1.2;
                    for (double offset = -waveWidth; offset <= waveWidth; offset += 0.3) {
                        Vector perpendicular = direction.clone().rotateAroundY(Math.PI/2).multiply(offset);
                        Location waveLoc = slashLoc.clone().add(perpendicular);

                        // Flame particles
                        world.spawnParticle(Particle.DUST, waveLoc, 1, 0.05, 0.05, 0.05,
                                new Particle.DustOptions(Color.fromRGB(255, 50 + random.nextInt(100), 0), 1.0f));
                    }
                }
            }

            private void transitionPhase(Location current) {
                // Brief pause with flame surge
                player.setVelocity(new Vector(0, 0, 0));

                // Transition effect
                double radius = 1.5;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location effectLoc = current.clone().add(x, 1, z);
                    world.spawnParticle(Particle.FLAME, effectLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.DUST, effectLoc, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.2f));
                }

                // Transition sound
                world.playSound(current, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.8f);
            }

            private void performSecondStrike(Location current) {
                // Store direction for consistent sweep
                if (!isSecondStrike) {
                    slashDirection = current.getDirection();
                    isSecondStrike = true;
                }

                // Horizontal movement
                Vector movement = slashDirection.clone().multiply(1.5);
                movement.setY(0);
                player.setVelocity(movement);

                // Create horizontal slash effects
                createHorizontalSlashEffects(current);

                // Check for hits
                checkHorizontalHits(current);
            }

            private void createHorizontalSlashEffects(Location location) {
                double slashWidth = 3.0;
                Vector right = slashDirection.clone().rotateAroundY(Math.PI/2);

                // Main horizontal slash wave
                for (double offset = -slashWidth; offset <= slashWidth; offset += 0.2) {
                    Vector slashOffset = right.clone().multiply(offset);
                    Location slashLoc = location.clone().add(slashOffset).add(0, 1, 0);

                    // Core slash effect
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);

                    // Flame wave
                    double flameSpread = 0.8;
                    for (double spread = -flameSpread; spread <= flameSpread; spread += 0.2) {
                        Location flameLoc = slashLoc.clone().add(slashDirection.clone().multiply(spread));

                        // Main flame
                        world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.05, 0.05, 0.05, 0.02);

                        // Ember trail
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, flameLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 100 + random.nextInt(100), 0), 1.0f));
                        }
                    }
                }
            }

            private void checkVerticalHits(Location location) {
                double radius = 2.5;
                double height = 3.0;

                for (Entity entity : world.getNearbyEntities(location, radius, height, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // First strike damage
                        target.damage(10.0, player);
                        hitEntities.add(entity);

                        // Hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 12, 0.3, 0.3, 0.3, 0.1);

                        // Impact sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);

                        // Vertical knockback
                        Vector kb = new Vector(0, 0.3, 0).add(location.getDirection().multiply(0.5));
                        target.setVelocity(kb);
                    }
                }
            }

            private void checkHorizontalHits(Location location) {
                double radius = 3.0;

                for (Entity entity : world.getNearbyEntities(location, radius, 2.0, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Second strike damage with bonus
                        target.damage(12.0, player);
                        hitEntities.add(entity);

                        // Enhanced hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 2, 0.2, 0.2, 0.2, 0);
                        world.spawnParticle(Particle.FLAME, hitLoc, 15, 0.4, 0.4, 0.4, 0.12);

                        // Impact sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.4f);
                        world.playSound(hitLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.2f);

                        // Horizontal knockback in slash direction
                        Vector kb = slashDirection.clone().multiply(1.2).setY(0.2);
                        target.setVelocity(kb);

                        // Fire effect
                        target.setFireTicks(60);
                    }
                }
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply brief effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, false, false));

        addCooldown(player, "TwelfthForm", 12);
    }

    public void addCooldown(Player player, String formName, int seconds) {
        // Implement cooldown logic here
        // This is a placeholder method; actual implementation may vary
        player.sendMessage("§6" + formName + " is now on cooldown for " + seconds + " seconds.");
    }

    /**
     * Thirteenth Form: Burning Spirit (拾参ノ型 燃魂)
     * A continuous flowing combination of all twelve forms in succession
     */

    public void useThirteenthForm() {
        player.sendMessage("§6☀ §c拾参ノ型 燃魂 §6(Thirteenth Form: Burning Spirit)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial effect sounds
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger currentForm = new AtomicInteger(1);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 12.0; // Longer duration for all forms
            private Location lastLocation = startLoc.clone();
            private int comboCount = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    fadeOutEffects();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Execute current form and manage transitions
                executeFormSequence(currentLoc);

                // Create continuous spirit effects
                createSpiritEffects(currentLoc);

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void executeFormSequence(Location current) {
                // Calculate which form to execute based on time
                int formIndex = (int)(time / 1.0) + 1; // Switch form every 1 second

                if (formIndex != currentForm.get() && formIndex <= 12) {
                    // Transition to next form
                    currentForm.set(formIndex);

                    // Announce form change
                    String formName = getFormName(formIndex);
                    player.sendMessage("§6➤ §e" + formName);

                    // Transition effects
                    createFormTransitionEffects(current);
                }

                // Execute current form's movement and effects
                executeCurrentForm(current, formIndex);
            }

            private String getFormName(int form) {
                return switch (form) {
                    case 1 -> "Dance";
                    case 2 -> "Clear Blue Sky";
                    case 3 -> "Raging Sun";
                    case 4 -> "Burning Bones";
                    case 5 -> "Solar Heat Haze";
                    case 6 -> "Solar Heat Ray";
                    case 7 -> "Sunflower Thrust";
                    case 8 -> "Solar Dragon";
                    case 9 -> "Fire Wheel";
                    case 10 -> "Sunshine Arrow";
                    case 11 -> "Fake Rainbow";
                    case 12 -> "Flame Dance";
                    default -> "Burning Spirit";
                };
            }

            private void executeCurrentForm(Location current, int formIndex) {
                Vector direction = current.getDirection();
                double baseSpeed = 0.6;

                switch (formIndex) {
                    case 1 -> performDanceMovement(current, direction, baseSpeed);
                    case 2 -> performSkyMovement(current, direction, baseSpeed);
                    case 3 -> performSunMovement(current, direction, baseSpeed);
                    case 4 -> performBonesMovement(current, direction, baseSpeed);
                    case 5 -> performHazeMovement(current, direction, baseSpeed);
                    case 6 -> performRayMovement(current, direction, baseSpeed);
                    case 7 -> performThrustMovement(current, direction, baseSpeed);
                    case 8 -> performDragonMovement(current, direction, baseSpeed);
                    case 9 -> performWheelMovement(current, direction, baseSpeed);
                    case 10 -> performArrowMovement(current, direction, baseSpeed);
                    case 11 -> performRainbowMovement(current, direction, baseSpeed);
                    case 12 -> performFlameDanceMovement(current, direction, baseSpeed);
                }
            }

            private void createSpiritEffects(Location location) {
                // Create burning spirit aura
                double radius = 1.5;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spiritLoc = location.clone().add(x, 1, z);

                    // Spiraling flame effect
                    world.spawnParticle(Particle.FLAME, spiritLoc, 1, 0.1, 0.1, 0.1, 0.02);

                    // Spirit energy particles
                    if (random.nextFloat() < 0.3) {
                        Color spiritColor = switch (currentForm.get()) {
                            case 1 -> Color.fromRGB(255, 100, 0);  // Orange
                            case 2 -> Color.fromRGB(135, 206, 235); // Sky Blue
                            case 3 -> Color.fromRGB(255, 50, 0);   // Bright Red
                            case 4 -> Color.fromRGB(255, 140, 0);  // Dark Orange
                            case 5 -> Color.fromRGB(255, 215, 0);  // Golden
                            case 6 -> Color.fromRGB(255, 165, 0);  // Pure Orange
                            case 7 -> Color.fromRGB(255, 200, 0);  // Yellow-Orange
                            case 8 -> Color.fromRGB(255, 69, 0);   // Red-Orange
                            case 9 -> Color.fromRGB(255, 127, 80); // Coral
                            case 10 -> Color.fromRGB(255, 140, 0); // Dark Orange
                            case 11 -> Color.fromRGB(
                                    (int)(Math.sin(time * 5) * 127 + 128),
                                    (int)(Math.sin(time * 5 + 2) * 127 + 128),
                                    (int)(Math.sin(time * 5 + 4) * 127 + 128)
                            ); // Rainbow
                            case 12 -> Color.fromRGB(255, 80, 0);  // Bright Orange
                            default -> Color.fromRGB(255, 165, 0); // Default Orange
                        };

                        world.spawnParticle(Particle.DUST, spiritLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(spiritColor, 1.2f));
                    }
                }
            }

            private void createFormTransitionEffects(Location location) {
                // Transition flash
                world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);

                // Spiral effect
                double radius = 2.0;
                for (double y = 0; y < 3; y += 0.2) {
                    double angleOffset = y * 2;
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle + angleOffset) * radius;
                        double z = Math.sin(angle + angleOffset) * radius;

                        Location effectLoc = location.clone().add(x, y, z);
                        world.spawnParticle(Particle.FLAME, effectLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Transition sounds
                world.playSound(location, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.5f);
                world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
            }

            // Individual form movements
            private void performDanceMovement(Location current, Vector direction, double baseSpeed) {
                // Simple spinning slash movement
                Vector movement = direction.clone().multiply(baseSpeed);
                movement.setY(Math.sin(time * 8) * 0.2);
                player.setVelocity(movement);
            }
            // Fügen Sie diese Methoden in die ThirteenthForm-Klasse ein, innerhalb des BukkitRunnable

            private void performSkyMovement(Location current, Vector direction, double baseSpeed) {
                // Clear Blue Sky - Schnelle Aufwärtsbewegung gefolgt von Abwärtsschlag
                if (time % 1.0 < 0.5) {
                    // Aufwärtsbewegung
                    Vector upward = new Vector(0, baseSpeed * 1.5, 0);
                    player.setVelocity(upward);

                    // Aufsteigende Partikel
                    for (int i = 0; i < 4; i++) {
                        Location particleLoc = current.clone().add(
                                (random.nextDouble() - 0.5) * 2,
                                random.nextDouble() * 2,
                                (random.nextDouble() - 0.5) * 2
                        );
                        world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                } else {
                    // Abwärtsschlag
                    Vector downward = direction.clone().multiply(baseSpeed).setY(-baseSpeed);
                    player.setVelocity(downward);

                    // Slash-Effekt
                    world.spawnParticle(Particle.SWEEP_ATTACK, current, 1, 0.5, 0.5, 0.5, 0);
                }
            }

            private void performSunMovement(Location current, Vector direction, double baseSpeed) {
                // Raging Sun - Kreisförmige Bewegung mit Feuereffekten
                double angle = time * 10;
                Vector movement = direction.clone().rotateAroundY(angle).multiply(baseSpeed);
                movement.setY(Math.sin(time * 4) * 0.2);
                player.setVelocity(movement);

                // Sonnenfeuerspiral
                for (double a = 0; a < Math.PI * 2; a += Math.PI / 8) {
                    double radius = 1.5;
                    double x = Math.cos(a + angle) * radius;
                    double z = Math.sin(a + angle) * radius;
                    Location flameLoc = current.clone().add(x, 1, z);
                    world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            private void performBonesMovement(Location current, Vector direction, double baseSpeed) {
                // Burning Bones - Aggressive Vorwärtsbewegung
                Vector movement = direction.clone().multiply(baseSpeed * 1.2);
                movement.setY(Math.sin(time * 6) * 0.3);
                player.setVelocity(movement);

                // Intensive Flammeneffekte
                for (int i = 0; i < 3; i++) {
                    Location flameLoc = current.clone().add(
                            (random.nextDouble() - 0.5) * 2,
                            random.nextDouble() * 2,
                            (random.nextDouble() - 0.5) * 2
                    );
                    world.spawnParticle(Particle.FLAME, flameLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }
            }

            private void performHazeMovement(Location current, Vector direction, double baseSpeed) {
                // Solar Heat Haze - Schnelle Seitwärtsbewegungen
                double sideStep = Math.sin(time * 8) * baseSpeed;
                Vector side = direction.clone().rotateAroundY(Math.PI / 2).multiply(sideStep);
                Vector movement = direction.clone().multiply(baseSpeed * 0.5).add(side);
                player.setVelocity(movement);

                // Hitzeflimmer-Effekt
                for (int i = 0; i < 5; i++) {
                    Location hazeLoc = current.clone().add(
                            (random.nextDouble() - 0.5) * 3,
                            random.nextDouble() * 2,
                            (random.nextDouble() - 0.5) * 3
                    );
                    world.spawnParticle(Particle.DUST, hazeLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 200, 0), 1.0f));
                }
            }

            private void performRayMovement(Location current, Vector direction, double baseSpeed) {
                // Solar Heat Ray - Gezielte Vorwärtsbewegung
                Vector movement = direction.clone().multiply(baseSpeed * 1.5);
                player.setVelocity(movement);

                // Strahleffekt
                Vector rayDir = direction.clone().multiply(2);
                Location rayEnd = current.clone().add(rayDir);
                for (double d = 0; d < 2; d += 0.2) {
                    Location rayLoc = current.clone().add(direction.clone().multiply(d));
                    world.spawnParticle(Particle.FLAME, rayLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            private void performThrustMovement(Location current, Vector direction, double baseSpeed) {
                // Sunflower Thrust - Stoßbewegung
                Vector movement = direction.clone().multiply(baseSpeed * 1.8);
                if (time % 1.0 < 0.3) {
                    movement.multiply(2); // Zusätzlicher Schub
                }
                player.setVelocity(movement);

                // Stoßeffekt
                world.spawnParticle(Particle.SWEEP_ATTACK, current.clone().add(direction), 1, 0, 0, 0, 0);
            }

            private void performDragonMovement(Location current, Vector direction, double baseSpeed) {
                // Solar Dragon - Wellenförmige Bewegung
                double wave = Math.sin(time * 4) * baseSpeed;
                Vector up = new Vector(0, wave, 0);
                Vector movement = direction.clone().multiply(baseSpeed).add(up);
                player.setVelocity(movement);

                // Dracheneffekt
                double radius = 1.5;
                double angle = time * 8;
                for (double a = 0; a < Math.PI * 2; a += Math.PI / 8) {
                    double x = Math.cos(a + angle) * radius;
                    double z = Math.sin(a + angle) * radius;
                    Location dragonLoc = current.clone().add(x, wave + 1, z);
                    world.spawnParticle(Particle.FLAME, dragonLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }
            }

            private void performWheelMovement(Location current, Vector direction, double baseSpeed) {
                // Fire Wheel - Rotierende Bewegung
                double rotationSpeed = time * 12;
                Vector movement = direction.clone().rotateAroundY(rotationSpeed).multiply(baseSpeed);
                player.setVelocity(movement);

                // Radeffekt
                createFireWheel(current, rotationSpeed);
            }

            private void performArrowMovement(Location current, Vector direction, double baseSpeed) {
                // Sunshine Arrow - Pfeilartige Bewegung
                Vector movement = direction.clone().multiply(baseSpeed * 2);
                if (time % 1.0 < 0.2) {
                    movement.multiply(1.5); // Burst of speed
                }
                player.setVelocity(movement);

                // Pfeileffekt
                Location arrowTip = current.clone().add(direction.clone().multiply(2));
                world.spawnParticle(Particle.FLAME, arrowTip, 5, 0.1, 0.1, 0.1, 0.05);
            }

            private void performRainbowMovement(Location current, Vector direction, double baseSpeed) {
                // Fake Rainbow - Komplexe Ausweichbewegung
                double angle = time * 8;
                Vector movement = direction.clone().rotateAroundY(Math.sin(angle) * Math.PI / 4).multiply(baseSpeed);
                movement.setY(Math.sin(time * 6) * 0.3);
                player.setVelocity(movement);

                // Regenbogeneffekt
                createRainbowTrail(current, angle);
            }

            private void performFlameDanceMovement(Location current, Vector direction, double baseSpeed) {
                // Flame Dance - Zweischlag-Kombo
                if (time % 1.0 < 0.5) {
                    // Erster Schlag - Vertikal
                    Vector vertical = new Vector(0, Math.cos(time * 8) * baseSpeed, 0);
                    Vector movement = direction.clone().multiply(baseSpeed * 0.5).add(vertical);
                    player.setVelocity(movement);
                    world.spawnParticle(Particle.SWEEP_ATTACK, current, 1, 0, 0, 0, 0);
                } else {
                    // Zweiter Schlag - Horizontal
                    Vector movement = direction.clone().multiply(baseSpeed * 1.5);
                    player.setVelocity(movement);
                    world.spawnParticle(Particle.SWEEP_ATTACK, current.clone().add(direction), 1, 0.5, 0, 0.5, 0);
                }
            }

            // Hilfsmethoden für spezielle Effekte
            private void createFireWheel(Location center, double rotation) {
                double radius = 1.5;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle + rotation) * radius;
                    double z = Math.sin(angle + rotation) * radius;
                    Location flameLoc = center.clone().add(x, 1, z);
                    world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            private void createRainbowTrail(Location location, double angle) {
                double radius = 1.2;
                for (double a = 0; a < Math.PI * 2; a += Math.PI / 8) {
                    double x = Math.cos(a + angle) * radius;
                    double z = Math.sin(a + angle) * radius;
                    Location particleLoc = location.clone().add(x, 1, z);

                    // Regenbogenfarben
                    Color color = Color.fromRGB(
                            (int)(Math.sin(a) * 127 + 128),
                            (int)(Math.sin(a + 2) * 127 + 128),
                            (int)(Math.sin(a + 4) * 127 + 128)
                    );

                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(color, 1.0f));
                }
            }

            private void fadeOutEffects() {
                new BukkitRunnable() {
                    private int fadeSteps = 20;

                    @Override
                    public void run() {
                        if (fadeSteps <= 0) {
                            this.cancel();
                            return;
                        }

                        Location loc = player.getLocation();
                        double radius = fadeSteps * 0.2;

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location fadeLoc = loc.clone().add(x, 1, z);
                            world.spawnParticle(Particle.FLAME, fadeLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }

                        fadeSteps--;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply overall effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 240, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 240, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 1, false, false));

        addCooldown(player, "ThirteenthForm", 30);
    }
}
