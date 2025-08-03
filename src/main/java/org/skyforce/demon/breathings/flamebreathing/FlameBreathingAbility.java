package org.skyforce.demon.breathings.flamebreathing;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameBreathingAbility {
    private final Map<Player, Long> activeUntil;
    private final Plugin plugin;

    public FlameBreathingAbility(Map<Player, Long> activeUntil, Plugin plugin) {
        this.activeUntil = activeUntil;
        this.plugin = plugin;
    }

    public void applyFlameEffect(Player player, Entity target, ItemStack item) {
        if (item == null || item.getType() != Material.STONE_SWORD) return;
        long now = System.currentTimeMillis();
        if (!activeUntil.containsKey(player) || activeUntil.get(player) < now) return;
        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.01);
        target.getWorld().playSound(target.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.2f, 1.0f);
        if (target instanceof LivingEntity) {
            target.setFireTicks(60);
        }
    }

   /* public void unknowingFire(Player player, LivingEntity firstTarget, ItemStack item) {
        int maxJumps = 4;
        double jumpRange = 4.0;
        java.util.Set<LivingEntity> hit = new java.util.HashSet<>();
        hit.add(player);
        player.setVelocity(player.getLocation().getDirection().multiply(2));
        chainAttack(player, firstTarget, hit, maxJumps, jumpRange, 0);
    }
*/

    public void unknowingFire(Player player, LivingEntity target, ItemStack sword) {
        if (target == null) {
            // Wenn kein Ziel, dann geradeaus angreifen
            performUnknowingFireDash(player, null, sword);
            return;
        }

        // Berechne Distanz zum Ziel
        double distance = player.getLocation().distance(target.getLocation());
        if (distance > 8.0) {
            player.sendMessage("§cZiel ist zu weit entfernt!");
            return;
        }

        performUnknowingFireDash(player, target, sword);
    }

    private void performUnknowingFireDash(Player player, LivingEntity target, ItemStack sword) {
        Location startLoc = player.getLocation().clone();
        Vector direction;
        Location endLoc;

        if (target != null) {
            // Richtung zum Ziel - Mit Null-Check
            Vector targetDirection = target.getLocation().subtract(startLoc).toVector();
            if (targetDirection.lengthSquared() == 0) {
                direction = player.getLocation().getDirection().normalize();
            } else {
                direction = targetDirection.normalize();
            }
            endLoc = target.getLocation().clone();
        } else {
            // Richtung der Sichtlinie des Spielers
            direction = player.getLocation().getDirection().normalize();
            endLoc = startLoc.clone().add(direction.multiply(6.0));
        }

        // Erstelle flammende Pfad-Effekte
        createFlameTrail(startLoc, endLoc, direction);

        // Teleportiere den Spieler zum Ziel (simuliert extreme Geschwindigkeit)
        player.teleport(endLoc);

        // Führe den verheerenden Angriff aus
        executeDevastatingStrike(player, target, endLoc, sword);

        // Erstelle den charakteristischen Hitze-Burst
        createHeatBurst(endLoc);
    }

    private void executeDevastatingStrike(Player player, LivingEntity target, Location strikeLocation, ItemStack sword) {
        World world = strikeLocation.getWorld();

        // Kraftvoller Schlag-Sound (wie im Anime)
        world.playSound(strikeLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        world.playSound(strikeLocation, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);
        world.playSound(strikeLocation, Sound.ITEM_FIRECHARGE_USE, 1.5f, 1.2f);

        // Finde alle Feinde in einem kleinen Radius (da es ein fokussierter Schlag ist)
        Collection<Entity> nearbyEntities = world.getNearbyEntities(strikeLocation, 2.5, 2.5, 2.5);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity victim = (LivingEntity) entity;

                // Massiver Schaden (charakteristisch für Unknowing Fire)
                victim.damage(12.0, player);

                // Brennender Effekt (simuliert das "Verbrennen des Fleisches")
                victim.setFireTicks(200); // 10 Sekunden

                // FIXED: Sicherer Knockback-Effekt mit Validierung
                Vector knockback = calculateSafeKnockback(player.getLocation(), victim.getLocation());
                if (knockback != null) {
                    victim.setVelocity(knockback);
                }

                // Spezielle Verbrennungspartikel um das Opfer
                createBurningEffect(victim.getLocation());
            }
        }
    }

    // NEUE METHODE: Sichere Knockback-Berechnung
    private Vector calculateSafeKnockback(Location attackerLoc, Location victimLoc) {
        try {
            // Berechne die Richtung vom Angreifer zum Opfer
            Vector direction = victimLoc.subtract(attackerLoc).toVector();

            // Prüfe ob der Vector gültig ist
            if (direction.lengthSquared() == 0) {
                // Fallback: Zufällige Richtung wenn sie am gleichen Ort sind
                direction = new Vector(
                        (Math.random() - 0.5) * 2,
                        0,
                        (Math.random() - 0.5) * 2
                );
            }

            // Normalisiere den Vector
            direction = direction.normalize();

            // Prüfe auf ungültige Werte (NaN oder Infinity)
            if (!isVectorValid(direction)) {
                return null; // Kein Knockback wenn der Vector ungültig ist
            }

            // Setze Y-Komponente und multipliziere für Knockback-Stärke
            direction.multiply(1.5);
            direction.setY(0.3);

            // Finale Validierung
            if (!isVectorValid(direction)) {
                return null;
            }

            return direction;

        } catch (Exception e) {
            // Bei jedem Fehler: Kein Knockback
            System.err.println("Fehler bei Knockback-Berechnung: " + e.getMessage());
            return null;
        }
    }

    // NEUE METHODE: Vector-Validierung
    private boolean isVectorValid(Vector vector) {
        return vector != null &&
                Double.isFinite(vector.getX()) &&
                Double.isFinite(vector.getY()) &&
                Double.isFinite(vector.getZ());
    }

    private void createFlameTrail(Location start, Location end, Vector direction) {
        World world = start.getWorld();
        if (world == null) return;

        double distance = start.distance(end);
        if (distance == 0 || !Double.isFinite(distance)) return;

        int steps = (int)(distance * 4); // 4 Partikel pro Block
        steps = Math.min(steps, 100); // Begrenze die Anzahl der Schritte

        for (int i = 0; i <= steps; i++) {
            double progress = (double)i / steps;
            Location particleLoc = start.clone().add(direction.clone().multiply(distance * progress));

            // Prüfe ob die Position gültig ist
            if (!isLocationValid(particleLoc)) continue;

            // Haupt-Flammeneffekt (großer, wilder Flammenstrahl)
            world.spawnParticle(Particle.FLAME, particleLoc, 8, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.LAVA, particleLoc, 3, 0.2, 0.2, 0.2, 0);

            // Hitze-Verzerrung Effekt
            world.spawnParticle(Particle.SMOKE, particleLoc, 5, 0.4, 0.4, 0.4, 0.05);

            // Funkenspur
            world.spawnParticle(Particle.LAVA, particleLoc, 2, 0.1, 0.1, 0.1, 0);

            // Verzögerung für den visuellen Effekt
            final Location finalLoc = particleLoc.clone();
            final int delay = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (isLocationValid(finalLoc)) {
                    world.spawnParticle(Particle.FLAME, finalLoc, 3, 0.1, 0.1, 0.1, 0);
                }
            }, delay); // Gestaffelter Effekt
        }
    }

    // NEUE METHODE: Location-Validierung
    private boolean isLocationValid(Location location) {
        return location != null &&
                location.getWorld() != null &&
                Double.isFinite(location.getX()) &&
                Double.isFinite(location.getY()) &&
                Double.isFinite(location.getZ());
    }

    private void createHeatBurst(Location center) {
        if (!isLocationValid(center)) return;

        World world = center.getWorld();

        // Zentraler Hitze-Explosion
        world.spawnParticle(Particle.EXPLOSION, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.FLAME, center, 60, 1.5, 1.5, 1.5, 0.3);
        world.spawnParticle(Particle.LAVA, center, 20, 1.0, 1.0, 1.0, 0);

        // Hitze-Wellen-Effekt (mehrere Ringe)
        for (int ring = 1; ring <= 3; ring++) {
            final int currentRing = ring;
            final Location safeCenter = center.clone();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                createHeatRing(safeCenter, currentRing * 1.5, currentRing * 10);
            }, ring * 3L);
        }

        // Anhaltende Hitze-Effekte
        final Location safeCenter = center.clone();
        for (int i = 0; i < 100; i++) {
            final int delay = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (delay % 5 == 0 && isLocationValid(safeCenter)) { // Alle 5 Ticks
                    world.spawnParticle(Particle.SMOKE, safeCenter, 3, 0.8, 0.5, 0.8, 0.02);
                    world.spawnParticle(Particle.FLAME, safeCenter, 5, 0.6, 0.3, 0.6, 0.05);
                }
            }, i);
        }
    }

    private void createHeatRing(Location center, double radius, int particleCount) {
        if (!isLocationValid(center) || !Double.isFinite(radius) || radius <= 0) return;

        World world = center.getWorld();
        particleCount = Math.min(particleCount, 50); // Begrenze Partikelanzahl

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            // Prüfe auf gültige Werte
            if (!Double.isFinite(x) || !Double.isFinite(z)) continue;

            Location ringLoc = center.clone().add(x, 0, z);
            if (isLocationValid(ringLoc)) {
                world.spawnParticle(Particle.FLAME, ringLoc, 2, 0.1, 0.1, 0.1, 0.05);
                world.spawnParticle(Particle.SMOKE, ringLoc, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    private void createBurningEffect(Location location) {
        if (!isLocationValid(location)) return;

        World world = location.getWorld();
        final Location safeLocation = location.clone();

        // Intensiver Verbrennungseffekt um das Opfer
        for (int i = 0; i < 50; i++) {
            final int delay = i * 2;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (isLocationValid(safeLocation)) {
                    world.spawnParticle(Particle.FLAME, safeLocation.clone().add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.1);
                    world.spawnParticle(Particle.SMOKE, safeLocation.clone().add(0, 1.5, 0), 2, 0.2, 0.3, 0.2, 0.05);
                }
            }, delay);
        }
    }

    private void chainAttack(Player player, LivingEntity current, java.util.Set<LivingEntity> hit, int maxJumps, double jumpRange, int count) {
        if (current == null || count >= maxJumps) return;
        org.bukkit.Location newLoc = new org.bukkit.Location(
                current.getLocation().getWorld(),
                current.getLocation().getX(),
                current.getLocation().getY() + 0.1,
                current.getLocation().getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch()
        );
        player.teleport(newLoc);
        current.getWorld().spawnParticle(Particle.FLAME, current.getLocation().add(0, 1, 0), 60, 0.5, 0.7, 0.5, 0.02);
        current.getWorld().playSound(current.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.5f, 0.8f);
        current.damage(8.0, player);
        current.setFireTicks(100);
        hit.add(current);
        LivingEntity next = null;
        double[] minDist = {jumpRange + 1};
        LivingEntity[] nextHolder = {null};
        for (Entity e : current.getNearbyEntities(jumpRange, jumpRange, jumpRange)) {
            if (e instanceof LivingEntity && !hit.contains(e)) {
                double dist = e.getLocation().distance(current.getLocation());
                if (dist < minDist[0]) {
                    minDist[0] = dist;
                    nextHolder[0] = (LivingEntity) e;
                }
            }
        }
        next = nextHolder[0];
        if (next != null) {
            final LivingEntity finalNext = next;
            final java.util.Set<LivingEntity> finalHit = new java.util.HashSet<>(hit);
            Bukkit.getScheduler().runTaskLater(plugin, () -> chainAttack(player, finalNext, finalHit, maxJumps, jumpRange, count + 1), 10L);
        }
    }

    public void risingScorchingSun(Player player, LivingEntity target, ItemStack item) {
        for (double y = 0; y <= 2.5; y += 0.2) {
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, y, 0), 12, 0.2, 0.01, 0.2, 0.01);
            target.getWorld().spawnParticle(Particle.LAVA, target.getLocation().add(0, y, 0), 2, 0.05, 0.01, 0.05, 0.01);
        }
        org.bukkit.util.Vector look = player.getLocation().getDirection().normalize();
        org.bukkit.Location center = player.getLocation().add(look.clone().multiply(1.7 - 1.0)).add(0, 1.0, 0);
        double radius = 2.0;
        int points = 64;
        org.bukkit.util.Vector up = new org.bukkit.util.Vector(0, 1, 0);
        org.bukkit.util.Vector right = look.clone().crossProduct(up).normalize();
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            org.bukkit.util.Vector offset = up.clone().multiply(cos * radius).add(look.clone().multiply(sin * radius));
            org.bukkit.Location point = center.clone().add(offset);
            center.getWorld().spawnParticle(Particle.FLAME, point, 4, 0.08, 0.01, 0.08, 0.01);
            if (i % 8 == 0) {
                center.getWorld().spawnParticle(Particle.LAVA, point, 1, 0.01, 0.01, 0.01, 0.01);
            }
        }
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.3f, 1.2f);
        target.damage(10.0, player);
        target.setFireTicks(120);
        target.setVelocity(target.getVelocity().setY(1.1));
        player.sendMessage("§6Second Form: Rising Scorching Sun activated!");
    }


    public void blazingUniverse(Player player, LivingEntity target, ItemStack item) {
        player.setVelocity(player.getVelocity().setY(1.2));
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 18 || !player.isOnline()) return;
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 8, 0.18, 0.12, 0.18, 0.01);
                player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 2, 0.05, 0.01, 0.05, 0.01);
                ticks++;
            }
        }, 0L, 1L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            org.bukkit.util.Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            player.setVelocity(direction.multiply(2.2).setY(-0.5));
            Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                int dashTicks = 0;

                @Override
                public void run() {
                    if (dashTicks > 10 || !player.isOnline()) return;
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 10, 0.22, 0.10, 0.22, 0.01);
                    player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 2, 0.05, 0.01, 0.05, 0.01);
                    dashTicks++;
                }
            }, 0L, 1L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                org.bukkit.Location loc = target.getLocation().add(0, 1, 0);
                for (double y = 1.5; y >= 0; y -= 0.15) {
                    target.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, y, 0), 16, 0.3, 0.01, 0.3, 0.01);
                    target.getWorld().spawnParticle(Particle.LAVA, loc.clone().add(0, y, 0), 3, 0.05, 0.01, 0.05, 0.01);
                }
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.6f);
                target.damage(14.0, player);
                target.setFireTicks(140);
                org.bukkit.util.Vector velocity = target.getVelocity();
                velocity.setY(-1.2);
                target.setVelocity(velocity);
                player.sendMessage("§6Third Form: Blazing Universe activated!");
            }, 12L);
        }, 18L);
    }

    public void bloomingFlameUndulation(Player player) {
        final int maxTicks = 12;
        final int[] ticks = {0};
        final int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (ticks[0] > maxTicks || !player.isOnline()) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                int steps = 10;
                double angle = Math.toRadians((ticks[0] * (360.0 / steps)) % 360);
                double x = Math.cos(angle) * 2.2;
                double y = 1.1;
                double z = Math.sin(angle) * 2.2;
                for (int i = -2; i <= 2; i++) {
                    double subAngle = angle + Math.toRadians(i * 6);
                    double subX = Math.cos(subAngle) * 2.2;
                    double subZ = Math.sin(subAngle) * 2.2;
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(subX, y, subZ), 3, 0.05, 0, 0.05, 0.01);
                }
                player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(x, y, z), 1, 0, 0, 0, 0);
                ticks[0]++;
            }
        }, 0L, 1L);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.2f, 1.0f);
        org.bukkit.potion.PotionEffectType resistance = org.bukkit.potion.PotionEffectType.getByName("RESISTANCE");
        if (resistance != null) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(resistance, 40, 2, false, true, true));
        }
        for (Entity e : player.getNearbyEntities(2.5, 2.0, 2.5)) {
            if (e instanceof LivingEntity entity && e != player) {
                entity.setFireTicks(100);
                entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation().add(0, 1, 0), 30, 0.4, 0.5, 0.4, 0.03);
                entity.getWorld().spawnParticle(Particle.LAVA, entity.getLocation().add(0, 1, 0), 8, 0.15, 0.2, 0.15, 0.02);
                entity.getWorld().spawnParticle(Particle.SMOKE, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.01);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_BURN, 1.2f, 1.1f);
                org.bukkit.util.Vector knock = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2.0).setY(0.7);
                entity.setVelocity(knock);
                entity.damage(10.0, player);
            }
        }
        player.sendMessage("§6Fourth Form: TEST");
    }

    public void flameTiger(Player player, LivingEntity target, ItemStack item) {
        World world = player.getWorld();
        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();

        // Dash
        Location dashLoc = start.clone().add(direction.clone().multiply(7));
        dashLoc.setY(start.getY());
        if (world.getBlockAt(dashLoc).isPassable() && world.getBlockAt(dashLoc.clone().add(0, 1, 0)).isPassable()) {
            player.setVelocity(direction.clone().multiply(1.6));
        }

        // Sounds
        world.playSound(start, Sound.ENTITY_BLAZE_HURT, 1f, 1.4f);
        world.playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.3f, 1.0f);

        // Tigerform
        double[][] tiger3D = {
                {-0.6, 1.4, 0}, {-0.4, 1.6, 0}, {-0.2, 1.8, 0}, {0, 1.9, 0}, {0.2, 1.8, 0}, {0.4, 1.6, 0}, {0.6, 1.4, 0},
                {-0.5, 1.2, 0}, {0.5, 1.2, 0}, {-0.4, 1.0, 0}, {0.4, 1.0, 0}, {-0.2, 0.8, 0}, {0.2, 0.8, 0}, {0, 0.6, 0},
                {-0.3, 1.2, -1}, {0.3, 1.2, -1}, {-0.3, 1.0, -2}, {0.3, 1.0, -2}, {-0.2, 0.8, -3}, {0.2, 0.8, -3},
                {0, 0.6, -4}, {0, 0.4, -5}
        };

        // Animation (bis zu 30 Schritte oder bis Treffer)
        for (int step = 0; step <= 30; step++) {
            final int s = step;
            Bukkit.getScheduler().runTaskLater(org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class), () -> {
                Location base = player.getLocation().add(0, 1.2, 0).add(direction.clone().multiply(s * 0.6));
                Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                Vector forward = direction.clone();

                for (double[] point : tiger3D) {
                    Location loc = base.clone()
                            .add(right.clone().multiply(point[0]))
                            .add(0, point[1], 0)
                            .add(forward.clone().multiply(point[2]));

                    world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0.001);

                    if (point[1] > 1.8 && Math.abs(point[0]) < 0.25 && point[2] == 0) {
                        world.spawnParticle(Particle.DUST, loc, 1, new Particle.DustOptions(Color.ORANGE, 1));
                    }

                    if (point[1] < 1.0) {
                        world.spawnParticle(Particle.SMOKE, loc, 1, 0, 0, 0, 0.01);
                    }
                }

                // Gegner treffen
                for (Entity e : world.getNearbyEntities(base, 1.5, 1.5, 1.5)) {
                    if (e instanceof LivingEntity le && !le.equals(player)) {
                        // Biss + Explosion
                        world.spawnParticle(Particle.EXPLOSION, base, 1);
                        world.spawnParticle(Particle.FLAME, base, 60, 1, 0.5, 1, 0.05);
                        world.spawnParticle(Particle.LAVA, base, 20, 0.5, 0.2, 0.5, 0.01);
                        world.spawnParticle(Particle.FIREWORK, base, 40, 0.8, 0.4, 0.8, 0.01);
                        world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

                        le.damage(8.0, player);
                        le.setFireTicks(100);
                        Vector knock = le.getLocation().toVector().subtract(base.toVector()).normalize().multiply(1.2).setY(0.5);
                        le.setVelocity(knock);
                        return; // stop animation
                    }
                }

                // Explosion am Ende wenn niemand getroffen
                if (s == 30) {
                    Location finalLoc = player.getLocation().add(direction.clone().multiply(30 * 0.6)).add(0, 1.2, 0);
                    world.spawnParticle(Particle.EXPLOSION, finalLoc, 1);
                    world.playSound(finalLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
                    world.spawnParticle(Particle.FLAME, finalLoc, 60, 1, 0.5, 1, 0.05);
                    world.spawnParticle(Particle.LAVA, finalLoc, 20, 0.5, 0.2, 0.5, 0.01);
                    world.spawnParticle(Particle.FIREWORK, finalLoc, 40, 0.8, 0.4, 0.8, 0.01);

                    player.getNearbyEntities(2, 1.5, 2).forEach(e -> {
                        if (e instanceof LivingEntity le && !le.equals(player)) {
                            le.damage(6.0, player);
                        }
                    });
                }
            }, s * 2L);
        }

        player.sendTitle("§c炎虎", "§6Fifth Form: Flame Tiger!", 10, 30, 10);
    }


    /**
     * Ninth Form: Rengoku (玖ノ型 煉獄)
     * The ultimate Flame Breathing technique, featuring a devastating dragon-shaped flame dash
     */

    Random random = new Random();

    public void useNinthForm(Player player) {
        player.sendMessage("§6炎 §c玖ノ型 煉獄 §6(Ninth Form: Rengoku)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial charge-up sounds
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 0.5f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasStruck = new AtomicBoolean(false);

        // Initial charge-up effect
        createChargeUpEffect(startLoc, world);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final double CHARGE_DURATION = 1.0;
            private Location lastLocation = startLoc.clone();
            private boolean isDashing = false;
            private Vector dashDirection;
            private List<Location> dragonSegments = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < CHARGE_DURATION) {
                    // Charge-up phase
                    executeChargeUp(currentLoc);
                } else if (!isDashing) {
                    // Initialize dash
                    initiateDash(currentLoc);
                } else {
                    // Execute dash and slash
                    executeDashAndSlash(currentLoc);
                }

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void executeChargeUp(Location location) {
                // Rising flame effect
                double chargeProgress = time / CHARGE_DURATION;
                createRisingFlames(location, chargeProgress);

                // Prepare player stance
                player.setVelocity(new Vector(0, 0.1, 0));

                // Ground burning effect
                if (random.nextFloat() < 0.3) {
                    Location groundLoc = location.clone().subtract(0, 0.5, 0);
                    world.spawnParticle(Particle.FLAME, groundLoc, 5, 0.5, 0, 0.5, 0.05);
                }
            }

            private void createRisingFlames(Location location, double progress) {
                double radius = 2.0 * progress;
                double height = 3.0 * progress;

                for (double y = 0; y < height; y += 0.2) {
                    double circleRadius = radius * (1 - y/height);
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * circleRadius;
                        double z = Math.sin(angle) * circleRadius;

                        Location flameLoc = location.clone().add(x, y, z);

                        // Intense flame particles
                        world.spawnParticle(Particle.FLAME, flameLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Ember effects
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.LAVA, flameLoc, 1, 0.1, 0.1, 0.1, 0);
                        }

                        // Smoke for intensity
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.SMOKE, flameLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                    }
                }

                // Power-up sounds
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.5f + (float)progress);
                }
            }

            private void initiateDash(Location location) {
                isDashing = true;
                dashDirection = location.getDirection();

                // Initial dash effect
                world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);

                // Clear the path ahead
                createDashPath(location, dashDirection);
            }

            private void createDashPath(Location start, Vector direction) {
                double dashDistance = 20.0;
                for (double d = 0; d < dashDistance; d += 0.5) {
                    Location pathLoc = start.clone().add(direction.clone().multiply(d));

                    // Break blocks in path
                    if (pathLoc.getBlock().getType().isSolid() &&
                            !pathLoc.getBlock().getType().toString().contains("BEDROCK")) {
                        pathLoc.getBlock().breakNaturally();
                    }
                }
            }

            private void executeDashAndSlash(Location location) {
                // Extreme dash speed
                double dashSpeed = 2.5;
                player.setVelocity(dashDirection.clone().multiply(dashSpeed));

                // Create dragon segments
                createDragonSegments(location);

                // Update and render dragon
                updateDragonEffect();

                // Check for hits
                checkDashHits(location);
            }

            private void createDragonSegments(Location location) {
                dragonSegments.add(0, location.clone());

                // Limit segments for performance
                if (dragonSegments.size() > 20) {
                    dragonSegments.remove(dragonSegments.size() - 1);
                }
            }

            private void updateDragonEffect() {
                // Dragon body parameters
                double dragonLength = dragonSegments.size();

                for (int i = 0; i < dragonSegments.size(); i++) {
                    Location segment = dragonSegments.get(i);
                    double segmentSize = 1.0 - (i / dragonLength * 0.5);

                    // Create dragon body
                    createDragonSegment(segment, segmentSize, i == 0);

                    // Connect segments with flame trails
                    if (i < dragonSegments.size() - 1) {
                        Location nextSegment = dragonSegments.get(i + 1);
                        createFlameConnection(segment, nextSegment, segmentSize);
                    }
                }
            }

            private void createDragonSegment(Location location, double size, boolean isHead) {
                int particles = isHead ? 16 : 8;

                // Create circular dragon body
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / particles) {
                    double x = Math.cos(angle) * size;
                    double y = Math.sin(angle) * size;

                    Location particleLoc = location.clone().add(x, y + 1, 0);

                    if (isHead) {
                        // Dragon head particles
                        world.spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.1);
                        world.spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);

                        // Dragon eyes
                        if (angle == 0 || angle == Math.PI) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    } else {
                        // Dragon body particles
                        world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }

            private void createFlameConnection(Location start, Location end, double size) {
                Vector between = end.toVector().subtract(start.toVector());
                double length = between.length();
                between.normalize();

                for (double d = 0; d < length; d += 0.5) {
                    Location connectionLoc = start.clone().add(between.multiply(d));

                    // Flame trail
                    world.spawnParticle(Particle.FLAME, connectionLoc, 2, 0.2 * size, 0.2 * size, 0.2 * size, 0.05);

                    // Additional effects
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.LAVA, connectionLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
            }

            private void checkDashHits(Location location) {
                if (hasStruck.get()) return;

                double hitRadius = 3.0;
                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Massive damage
                        target.damage(25.0, player);
                        hitEntities.add(entity);
                        hasStruck.set(true);

                        // Dramatic hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        createExplosiveHitEffect(hitLoc);

                        // Strong knockback
                        Vector kb = dashDirection.clone().multiply(3.0).setY(1.0);
                        target.setVelocity(kb);

                        // Set target on fire
                        target.setFireTicks(200);
                    }
                }
            }

            private void createExplosiveHitEffect(Location location) {
                // Explosion particles
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.FLAME, location, 50, 1.0, 1.0, 1.0, 0.2);
                world.spawnParticle(Particle.LAVA, location, 20, 0.5, 0.5, 0.5, 0.1);

                // Explosive sounds
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
                world.playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.2f);

                // Ground destruction
                createGroundDestruction(location);
            }

            private void createGroundDestruction(Location location) {
                double radius = 3.0;
                for (double x = -radius; x <= radius; x += 0.5) {
                    for (double z = -radius; z <= radius; z += 0.5) {
                        if (x * x + z * z <= radius * radius) {
                            Location blockLoc = location.clone().add(x, -1, z);
                            Block block = blockLoc.getBlock();

                            if (block.getType().isSolid() &&
                                    !block.getType().toString().contains("BEDROCK")) {
                                block.breakNaturally();

                                // Flame particles from broken blocks
                                world.spawnParticle(Particle.FLAME, blockLoc, 3, 0.2, 0.2, 0.2, 0.05);
                            }
                        }
                    }
                }
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                new BukkitRunnable() {
                    private double radius = 1.0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Expanding ring of fire
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location flameLoc = endLoc.clone().add(x, 0.1, z);
                            world.spawnParticle(Particle.FLAME, flameLoc, 2, 0.1, 0.1, 0.1, 0.05);

                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.LAVA, flameLoc, 1, 0.1, 0.1, 0.1, 0);
                            }
                        }

                        // Fade-out sounds
                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_FIRE_AMBIENT, 0.8f, 0.5f + (ticks / (float)MAX_TICKS));
                        }

                        radius += 0.2;
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));

        // Add cooldown
       // addCooldown(player, "NinthForm", 30);
    }

    private void createChargeUpEffect(Location center, World world) {
        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_TIME = 1.0;

            @Override
            public void run() {
                if (time >= MAX_TIME) {
                    this.cancel();
                    return;
                }

                double progress = time / MAX_TIME;
                double radius = 2.0 * progress;

                // Ground fire circle
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location flameLoc = center.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.FLAME, flameLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.LAVA, flameLoc, 1, 0, 0, 0, 0);
                }

                // Rising flames
                if (random.nextFloat() < 0.3) {
                    Location randomLoc = center.clone().add(
                            (random.nextDouble() - 0.5) * radius * 2,
                            random.nextDouble() * 2,
                            (random.nextDouble() - 0.5) * radius * 2
                    );
                    world.spawnParticle(Particle.FLAME, randomLoc, 3, 0.1, 0.1, 0.1, 0.1);
                }

                time += 0.05;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }



}
