package org.skyforce.demon.breathings.mistbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.*;

public class MistBreathingAbility {
    private final Player player;

    public MistBreathingAbility(Player player) {
        this.player = player;
    }

    public void useAbility() {
        // TODO: Implement the actual Mist Breathing ability effect
        player.sendMessage("You use Mist Breathing! (Effect not implemented yet)");
    }

    public void useFirstForm() {
        player.sendMessage("§7☁ §b壱ノ型: 垂天遠霞 §7(First Form: Low Clouds, Distant Haze)");

        World world = player.getWorld();
        Location baseLoc = player.getLocation();
        Vector direction = baseLoc.getDirection().normalize();

        // SFX - Nebelhafter Auftakt
        world.playSound(baseLoc, Sound.BLOCK_CANDLE_EXTINGUISH, 1.0f, 1.5f);
        world.playSound(baseLoc, Sound.ENTITY_ENDERMAN_STARE, 0.6f, 2.0f);
        world.playSound(baseLoc, Sound.ITEM_TRIDENT_RETURN, 0.8f, 2.0f);

        // Spieler wird "vom Nebel verschluckt"
        world.spawnParticle(Particle.CLOUD, baseLoc.add(0, 1, 0), 80, 1.0, 0.5, 1.0, 0.02);
        world.spawnParticle(Particle.LARGE_SMOKE, baseLoc.add(0, 1, 0), 60, 1.2, 0.4, 1.2, 0.01);
        world.spawnParticle(Particle.DUST, baseLoc.add(0, 1, 0), 40, 1.0, 0.3, 1.0, new Particle.DustOptions(Color.AQUA, 1.8f));

        // Lineare Nebelstoß-Animation (wie im GIF)
        for (double i = 0.6; i <= 6.0; i += 0.3) {
            Location pos = baseLoc.clone().add(direction.clone().multiply(i)).add(0, 1.0, 0);
            world.spawnParticle(Particle.LARGE_SMOKE, pos, 2, 0.2, 0, 0.05, 0.02);
            world.spawnParticle(Particle.DUST, pos, 2, 0.05, 0.01, 0.05, new Particle.DustOptions(Color.AQUA, 1.5f));
        }

        // Blitzartiger Aufprall (Motion Stop)
        Location impactCenter = baseLoc.clone().add(direction.clone().multiply(3)).add(0, 1.0, 0);
        world.spawnParticle(Particle.FLASH, impactCenter, 1);
        world.playSound(impactCenter, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 2.0f);

        // Gegner in gerader Linie treffen
        double range = 6.0;
        double width = 2.0;
        double baseDamage = 9.0;
        Location origin = player.getLocation();

        for (Entity entity : player.getNearbyEntities(range, 2.5, range)) {
            if (!(entity instanceof LivingEntity) || entity == player) continue;

            LivingEntity target = (LivingEntity) entity;
            Location targetLoc = target.getLocation();
            Vector toTarget = targetLoc.toVector().subtract(origin.toVector()).normalize();

            if (toTarget.dot(direction) > 0.6 &&
                    origin.distance(targetLoc) <= range &&
                    Math.abs(toTarget.crossProduct(direction).length()) < width) {

                target.damage(baseDamage, player);
                target.setVelocity(direction.clone().multiply(1.4).setY(0.2));

                // Kurzzeitige Zeitlupe beim Treffer
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 10, false, false));

                // Zusätzliche Trefferpartikel
                world.spawnParticle(Particle.SWEEP_ATTACK, targetLoc.add(0, 1, 0), 1, 0, 0, 0, 0);
            }
        }

        addCooldown(player, "FirstForm", 8);
    }

    /**
     * Second Form: Eight-Layered Mist (弐ノ型 八重霞)
     * Führt acht schnelle Nebelhiebe im Halbkreis vor dem Spieler aus.
     */
    public void useSecondForm() {
        player.sendMessage("§7☁ §b弐ノ型: 八重霞 §7(Second Form: Eight-Layered Mist)");

        World world = player.getWorld();
        Location baseLoc = player.getLocation();
        Vector forward = baseLoc.getDirection().normalize();

        // 💨 Dichte zentrale Nebelwolke – Austrittseffekt
        world.spawnParticle(Particle.CLOUD, baseLoc.add(0, 1, 0), 120, 1.4, 0.6, 1.4, 0.02);
        world.spawnParticle(Particle.DUST, baseLoc.add(0, 1, 0), 80, 1.2, 0.5, 1.2,
                new Particle.DustOptions(Color.AQUA, 2.0f));

        // 💥 Explosionseffekt / Entweich-Kick
        world.playSound(baseLoc, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.8f);
        world.playSound(baseLoc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.8f, 2.2f);

        // ⚔️ Acht Slashs in einem Bogen (vorne links → rechts)
        double radius = 3.5;
        double angleStep = Math.PI / 7;

        for (int i = 0; i < 8; i++) {
            double angle = -Math.PI / 2 + i * angleStep;
            Vector rotated = rotateAroundY(forward.clone(), angle).normalize();

            Location slashLoc = baseLoc.clone().add(rotated.multiply(radius)).add(0, 1.0, 0);

            // Nebel-Schlitz-Effekt
            world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 3, 0.2, 0.05, 0.2, 0);
            world.spawnParticle(Particle.DUST, slashLoc, 6, 0.2, 0.01, 0.2,
                    new Particle.DustOptions(Color.AQUA, 1.5f));
        }

        // 🎯 Gegner in einem 180°-Bogen vor dem Spieler treffen
        double hitRange = 4.0;
        for (Entity entity : world.getNearbyEntities(baseLoc, hitRange, 2, hitRange)) {
            if (!(entity instanceof LivingEntity) || entity == player) continue;

            LivingEntity target = (LivingEntity) entity;
            Vector toTarget = target.getLocation().toVector().subtract(baseLoc.toVector()).setY(0).normalize();
            double angle = forward.angle(toTarget);

            if (angle < Math.PI / 2) {
                target.damage(5.0, player);
                target.setVelocity(forward.clone().multiply(0.5).setY(0.2));

                // Aufschlags-Effekt
                world.spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
            }
        }

        addCooldown(player, "SecondForm", 6);
    }

    Random random = new Random();
    /**
     * Third Form: Scattering Mist Splash (参ノ型: 霞散の飛沫)
     * A rising spiral of mist that follows the user's line of sight
     */
    public void useThirdForm() {
        player.sendMessage("§7☁ §b参ノ型: 霞散の飛沫 §7(Third Form: Scattering Mist Splash)");

        World world = player.getWorld();
        Location base = player.getLocation();
        Vector direction = base.getDirection().normalize();

        // Enhanced sound effects
        world.playSound(base, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 1.2f);
        world.playSound(base, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.8f);
        world.playSound(base, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);

        BukkitRunnable spiralAnimation = new BukkitRunnable() {
            double time = 0;
            final double maxTime = 1.0;
            final double distance = 8.0; // Maximale Distanz in Blickrichtung

            @Override
            public void run() {
                if (time >= maxTime) {
                    this.cancel();
                    return;
                }

                // Calculate the current position along the line of sight
                double currentDistance = distance * (time/maxTime);
                Location currentBase = base.clone().add(direction.clone().multiply(currentDistance));

                // Create spiral effect along the line of sight
                for (double offset = -0.5; offset <= 0.5; offset += 0.25) {
                    double radius = 1.0 + (time * 2.0); // Expanding radius

                    // Create multiple spiral layers
                    for (int spiral = 0; spiral < 3; spiral++) {
                        double angleOffset = (spiral * (2 * Math.PI / 3)) + (time * 20);

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double currentAngle = angle + angleOffset;

                            // Calculate position in a circle perpendicular to the direction vector
                            Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
                            Vector up = right.clone().crossProduct(direction).normalize();

                            Vector circlePos = right.clone().multiply(Math.cos(currentAngle) * radius)
                                    .add(up.clone().multiply(Math.sin(currentAngle) * radius));

                            Location particleLoc = currentBase.clone().add(circlePos);
                            particleLoc.add(0, offset + (time * 2), 0); // Add some vertical variation

                            // Main mist particles
                            world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.05, 0.05, 0.05, 0);

                            // Blue-white color effect
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.05, 0.05, 0.05,
                                    new Particle.DustOptions(Color.fromRGB(170, 255, 255), 0.7f));

                            // Ethereal effects
                            if (random.nextFloat() < 0.2) {
                                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                        }
                    }
                }

                // Additional mist trail effect
                for (int i = 0; i < 8; i++) {
                    Vector randomOffset = new Vector(
                            (random.nextDouble() - 0.5) * 2,
                            (random.nextDouble() - 0.5) * 2,
                            (random.nextDouble() - 0.5) * 2
                    );
                    Location mistLoc = currentBase.clone().add(randomOffset);
                    world.spawnParticle(Particle.CLOUD, mistLoc, 2, 0.2, 0.2, 0.2, 0);
                }

                // Schaden und Effekte entlang der Linie
                for (Entity entity : world.getNearbyEntities(currentBase, 2.5, 2.5, 2.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Mehrfache kleinere Treffer
                        target.damage(2.0, player);

                        // Aufwärts-Knockback mit leichter Streuung
                        Vector knockback = direction.clone().multiply(0.3);
                        knockback.setY(0.2 + random.nextDouble() * 0.3);
                        target.setVelocity(knockback);

                        // Treffereffekte
                        Location targetLoc = target.getLocation();
                        world.spawnParticle(Particle.SWEEP_ATTACK, targetLoc.add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0);
                        world.spawnParticle(Particle.CLOUD, targetLoc, 15, 0.3, 0.3, 0.3, 0.05);

                        // Nebeleffekt auf getroffene Gegner
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    }
                }

                // Projektilabwehr entlang der Linie
                for (Entity entity : world.getNearbyEntities(currentBase, 2.5, 2.5, 2.5)) {
                    if (entity instanceof Projectile) {
                        Location projLoc = entity.getLocation();
                        entity.remove();
                        world.spawnParticle(Particle.CLOUD, projLoc, 15, 0.2, 0.2, 0.2, 0.05);
                        world.spawnParticle(Particle.END_ROD, projLoc, 5, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                time += 0.05;
            }
        };
        spiralAnimation.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        addCooldown(player, "ThirdForm", 8);
    }

    /**
     * Fourth Form: Shifting Flow Slash (肆ノ型 移流斬)
     * A swift dash attack with multiple slashes that appear like flowing mist
     */
    public void useFourthForm() {
        player.sendMessage("§7☁ §b肆ノ型 移流斬 §7(Fourth Form: Shifting Flow Slash)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();
        Vector direction = startLoc.getDirection().normalize();

        // Initial stance effects
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);

        // Pre-dash stance particles
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            double x = Math.cos(angle) * 1.2;
            double z = Math.sin(angle) * 1.2;
            Location particleLoc = startLoc.clone().add(x, 0.1, z);
            world.spawnParticle(Particle.CLOUD, particleLoc, 3, 0.1, 0, 0.1, 0.01);
        }

        // Main dash attack animation
        new BukkitRunnable() {
            private final double MAX_DISTANCE = 12.0;
            private final int SLASHES = 6;
            private double distance = 0;
            private int slashCount = 0;
            private Location lastPos = startLoc.clone();
            private final List<Entity> hitEntities = new ArrayList<>();

            @Override
            public void run() {
                if (distance >= MAX_DISTANCE || slashCount >= SLASHES) {
                    this.cancel();
                    return;
                }

                // Calculate new position
                double stepDistance = 0.8;
                distance += stepDistance;
                Location newPos = startLoc.clone().add(direction.clone().multiply(distance));

                // Create mist trail between positions
                Vector between = newPos.toVector().subtract(lastPos.toVector());
                double length = between.length();
                between.normalize().multiply(0.5);

                for (double d = 0; d < length; d += 0.5) {
                    Location trailLoc = lastPos.clone().add(between.clone().multiply(d));

                    // Main mist trail
                    world.spawnParticle(Particle.CLOUD, trailLoc, 3, 0.2, 0.2, 0.2, 0.01);
                    world.spawnParticle(Particle.DUST, trailLoc, 2, 0.2, 0.2, 0.2,
                            new Particle.DustOptions(Color.fromRGB(200, 255, 255), 0.7f));

                    // Occasional ethereal particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Slash effect at current position
                if (distance % 2.0 < 0.8) {
                    createSlashEffect(newPos);
                    slashCount++;
                }

                // Check for entities to damage
                for (Entity entity : world.getNearbyEntities(newPos, 2.5, 2.5, 2.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        target.damage(4.0, player);
                        hitEntities.add(entity);

                        // Slash impact effects
                        world.spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0),
                                2, 0.3, 0.3, 0.3, 0);
                        world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

                        // Knockback in attack direction
                        Vector knockback = direction.clone().multiply(0.8);
                        knockback.setY(0.2);
                        target.setVelocity(knockback);

                        // Additional hit effects
                        Location hitLoc = target.getLocation();
                        world.spawnParticle(Particle.CLOUD, hitLoc, 15, 0.3, 0.3, 0.3, 0.1);
                        world.spawnParticle(Particle.END_ROD, hitLoc, 5, 0.2, 0.2, 0.2, 0.05);
                    }
                }

                lastPos = newPos;
            }

            private void createSlashEffect(Location center) {
                // Play slash sound
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
                world.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.5f);

                // Create slash visualization
                double slashLength = 3.0;
                Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

                for (double t = -slashLength; t <= slashLength; t += 0.2) {
                    Location slashLoc = center.clone().add(right.clone().multiply(t));

                    // Main slash particles
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.CLOUD, slashLoc, 2, 0.1, 0.1, 0.1, 0.05);

                    // Colored trail
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(170, 255, 255), 1.0f));
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply speed effect to player during dash
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 2, false, false));

        addCooldown(player, "FourthForm", 10);
    }
    /**
     * Fifth Form: Sea of Clouds and Haze (伍ノ型 霞雲の海)
     * Creates a spherical sea of mist with omnidirectional slashes
     */
    public void useFifthForm() {
        player.sendMessage("§7☁ §b伍ノ型 霞雲の海 §7(Fifth Form: Sea of Clouds and Haze)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial burst effect
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private final double MAX_DURATION = 4.0;
            private double time = 0;
            private double radius = 0;
            private final double MAX_RADIUS = 5.0;
            private double rotation = 0;
            private Location currentCenter = startLoc.clone();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                // Update position to follow player
                currentCenter = player.getLocation();

                // Expand radius at start
                if (radius < MAX_RADIUS) {
                    radius = Math.min(MAX_RADIUS, radius + 0.3);
                }

                // Rotating sphere of mist
                rotation += Math.PI / 8;

                // Create spherical mist effect
                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 8) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 8) {
                        double x = radius * Math.sin(theta) * Math.cos(phi + rotation);
                        double y = radius * Math.sin(theta) * Math.sin(phi + rotation);
                        double z = radius * Math.cos(theta);

                        Location mistLoc = currentCenter.clone().add(x, z, y);

                        // Dense mist particles
                        world.spawnParticle(Particle.CLOUD, mistLoc, 1, 0.1, 0.1, 0.1, 0);
                        world.spawnParticle(Particle.DUST, mistLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(180, 255, 255), 0.8f));

                        // Random ethereal particles
                        if (random.nextFloat() < 0.1) {
                            world.spawnParticle(Particle.END_ROD, mistLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Create omnidirectional slash effects
                if (time * 20 % 2 == 0) {
                    createOmniSlash(currentCenter, radius);
                }

                // Damage and effects for nearby entities
                for (Entity entity : world.getNearbyEntities(currentCenter, radius, radius, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate distance-based damage
                        double distance = target.getLocation().distance(currentCenter);
                        double damage = 3.0 * (1 - (distance / (radius + 1)));
                        target.damage(damage, player);

                        // Add to hit list with delay removal
                        hitEntities.add(entity);
                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), () -> hitEntities.remove(entity), 10L);

                        // Intense hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 3, 0.3, 0.3, 0.3, 0);
                        world.spawnParticle(Particle.CLOUD, hitLoc, 20, 0.3, 0.3, 0.3, 0.1);
                        world.spawnParticle(Particle.END_ROD, hitLoc, 5, 0.2, 0.2, 0.2, 0.05);

                        // Multiple slash sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);

                        // Spiral knockback effect
                        Vector toEntity = target.getLocation().toVector().subtract(currentCenter.toVector());
                        toEntity.normalize().multiply(0.5);
                        toEntity.setY(0.2);
                        target.setVelocity(toEntity.rotateAroundY(Math.PI / 2));
                    }
                }

                time += 0.05;
            }

            private void createOmniSlash(Location center, double radius) {
                // Create multiple slash planes
                for (int i = 0; i < 3; i++) {
                    double verticalAngle = random.nextDouble() * Math.PI;
                    double horizontalAngle = random.nextDouble() * Math.PI * 2;

                    // Create a circular slash
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Vector slashVector = new Vector(x, 0, z);

                        // Rotate the slash plane
                        slashVector.rotateAroundX(verticalAngle);
                        slashVector.rotateAroundY(horizontalAngle);

                        Location slashLoc = center.clone().add(slashVector);

                        // Create slash effects
                        world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.CLOUD, slashLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(170, 255, 255), 1.0f));
                    }

                    // Slash sound effects
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, false, false));

        addCooldown(player, "FifthForm", 15);
    }

    /**
     * Sixth Form: Lunar Dispersing Mist (陸ノ型 月の霞消)
     * Rising spiral attack with multiple moon-shaped slashes
     */
    public void useSixthForm() {
        player.sendMessage("§7☁ §b陸ノ型 月の霞消 §7(Sixth Form: Lunar Dispersing Mist)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial effects
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 2.0f);

        Set<Entity> hitEntities = new HashSet<>();

        // Store initial direction for consistent spiral movement
        Vector initialDirection = player.getLocation().getDirection().normalize();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private double height = 0;
            private double spiralAngle = 0;
            private Location lastLocation = startLoc.clone();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    this.cancel();
                    return;
                }

                // Calculate spiral movement
                double spiralRadius = 2.0;
                spiralAngle += Math.PI / 8;
                height += 0.4;

                // Calculate new position on spiral
                Vector right = initialDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize();
                Vector forward = initialDirection.clone();

                Vector horizontalOffset = right.clone().multiply(Math.cos(spiralAngle) * spiralRadius)
                        .add(forward.multiply(Math.sin(spiralAngle) * spiralRadius));

                // Set new location
                Location newLoc = startLoc.clone().add(horizontalOffset).add(0, height, 0);
                player.teleport(newLoc);

                // Create the rising trail effect
                Vector between = newLoc.toVector().subtract(lastLocation.toVector());
                double length = between.length();
                Vector step = between.normalize().multiply(0.3);

                for (double d = 0; d < length; d += 0.3) {
                    Location trailLoc = lastLocation.clone().add(step.multiply(d));

                    // Dense mist trail
                    world.spawnParticle(Particle.CLOUD, trailLoc, 3, 0.2, 0.2, 0.2, 0.01);
                    world.spawnParticle(Particle.DUST, trailLoc, 2, 0.2, 0.2, 0.2,
                            new Particle.DustOptions(Color.fromRGB(180, 255, 255), 0.8f));
                }

                // Create multiple crescent moon slashes
                for (int i = 0; i < 3; i++) {
                    double slashAngle = spiralAngle + (2 * Math.PI / 3 * i);
                    createCrescentSlash(newLoc, slashAngle);
                }

                // Check for entities to damage in a spiral pattern
                for (Entity entity : world.getNearbyEntities(newLoc, 4, 4, 4)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        target.damage(5.0, player);
                        hitEntities.add(entity);

                        // Create impact effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 3, 0.3, 0.3, 0.3, 0);
                        world.spawnParticle(Particle.CLOUD, hitLoc, 15, 0.3, 0.3, 0.3, 0.1);

                        // Upward spiral knockback
                        Vector toCenter = target.getLocation().toVector()
                                .subtract(newLoc.toVector()).normalize();
                        Vector knockback = toCenter.clone()
                                .multiply(0.5)
                                .setY(0.8 + random.nextDouble() * 0.4);
                        target.setVelocity(knockback);

                        // Hit sound
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.8f);
                    }
                }

                // Update position and time
                lastLocation = newLoc;
                time += 0.05;
            }

            private void createCrescentSlash(Location center, double baseAngle) {
                double crescentSize = 3.0;

                // Create crescent moon shape
                for (double angle = -Math.PI/4; angle <= Math.PI/4; angle += Math.PI/16) {
                    double currentAngle = baseAngle + angle;
                    double x = Math.cos(currentAngle) * crescentSize;
                    double z = Math.sin(currentAngle) * crescentSize;

                    Location slashLoc = center.clone().add(x, 0, z);

                    // Crescent slash effects
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.CLOUD, slashLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(170, 255, 255), 1.0f));

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, slashLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                // Slash sound
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Initial jump velocity
        player.setVelocity(new Vector(0, 1.2, 0));

        // Apply effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));

        addCooldown(player, "SixthForm", 15);
    }

    /**
     * Seventh Form: Obscuring Clouds (漆ノ型 朧)
     * Muichiro's personal technique using tempo changes and mist illusions
     */
    public void useSeventhForm() {
        player.sendMessage("§7☁ §b漆ノ型 朧 §7(Seventh Form: Obscuring Clouds)");

        World world = player.getWorld();
        Location startLoc = player.getLocation();

        // Initial mist effect
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_AMBIENT, 0.7f, 1.2f);

        Set<Entity> affectedEntities = new HashSet<>();
        Map<Entity, Integer> disorientationLevels = new HashMap<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 8.0;
            private boolean isSlowPhase = true;
            private Location lastPosition = startLoc.clone();
            private Vector lastDirection = player.getLocation().getDirection();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    // Clear all lingering effects
                    for (Entity entity : affectedEntities) {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).removePotionEffect(PotionEffectType.BLINDNESS);
                            ((LivingEntity) entity).removePotionEffect(PotionEffectType.BLINDNESS);
                        }
                    }
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector currentDirection = currentLoc.getDirection();

                // Toggle between slow and fast phases
                if (time * 20 % 20 == 0) {
                    isSlowPhase = !isSlowPhase;

                    if (isSlowPhase) {
                        // Slow phase effects
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 1, false, false));
                        createMistCloud(currentLoc, 3.0, 0.3);
                    } else {
                        // Fast phase effects
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 4, false, false));
                        createMistCloud(currentLoc, 1.5, 0.8);
                    }
                }

                // Create mist trail based on movement
                Vector movement = currentLoc.toVector().subtract(lastPosition.toVector());
                double distanceMoved = movement.length();

                if (distanceMoved > 0.1) {
                    // Create mist particles along movement path
                    Vector step = movement.normalize().multiply(0.5);
                    for (double d = 0; d < distanceMoved; d += 0.5) {
                        Location trailLoc = lastPosition.clone().add(step.multiply(d));

                        // Dense mist effect
                        world.spawnParticle(Particle.CLOUD, trailLoc, isSlowPhase ? 5 : 2, 0.2, 0.2, 0.2, 0.01);
                        world.spawnParticle(Particle.DUST, trailLoc, isSlowPhase ? 3 : 1, 0.2, 0.2, 0.2,
                                new Particle.DustOptions(Color.fromRGB(200, 255, 255), 0.7f));
                    }
                }

                // Affect nearby entities
                for (Entity entity : world.getNearbyEntities(currentLoc, 5, 5, 5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Add to affected entities
                        if (!affectedEntities.contains(entity)) {
                            affectedEntities.add(entity);
                            disorientationLevels.put(entity, 0);
                        }

                        // Increase disorientation
                        int disorientationLevel = disorientationLevels.get(entity);
                        if (!isSlowPhase && random.nextFloat() < 0.3) {
                            disorientationLevel++;
                            disorientationLevels.put(entity, disorientationLevel);

                            // Apply stronger effects based on disorientation level
                            int confusionDuration = Math.min(100, 40 + (disorientationLevel * 20));
                            int blindnessDuration = Math.min(60, 20 + (disorientationLevel * 10));

                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, confusionDuration, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDuration, 0));

                            // Visual effects for disorientation
                            Location targetLoc = target.getLocation();
                            createMistCloud(targetLoc, 1.0, 0.5);
                            world.playSound(targetLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.5f);
                        }

                        // Damage during fast phase
                        if (!isSlowPhase && random.nextFloat() < 0.2) {
                            target.damage(2.0, player);
                            world.spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0),
                                    1, 0.3, 0.3, 0.3, 0);
                            world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                        }
                    }
                }

                // Update position tracking
                lastPosition = currentLoc.clone();
                lastDirection = currentDirection.clone();
                time += 0.05;
            }

            private void createMistCloud(Location center, double radius, double density) {
                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 8) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 8) {
                        double x = radius * Math.sin(theta) * Math.cos(phi);
                        double y = radius * Math.sin(theta) * Math.sin(phi);
                        double z = radius * Math.cos(theta);

                        Location mistLoc = center.clone().add(x, z, y);

                        if (random.nextDouble() < density) {
                            world.spawnParticle(Particle.CLOUD, mistLoc, 1, 0.1, 0.1, 0.1, 0);
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.END_ROD, mistLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        addCooldown(player, "SeventhForm", 20);
    }


    private void addCooldown(Player player, String ability, int seconds) {
        // Implementation of cooldown system
        // You'll need to implement this based on your plugin's cooldown system
    }

    private Vector rotateAroundY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.getX() * cos + v.getZ() * sin;
        double z = v.getZ() * cos - v.getX() * sin;
        return new Vector(x, v.getY(), z);
    }

}
