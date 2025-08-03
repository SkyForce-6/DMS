package org.skyforce.demon.breathings.waterbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterBreathingAbility {
    public static void playWaterScratchEffect(Player player) {
        Location loc = player.getEyeLocation();
        Vector direction = loc.getDirection().normalize();
        boolean rightToLeft = Math.random() < 0.5;
        double arcDegrees = 80;
        double radius = 4.0;
        int steps = 12;
        double damage = 4.0;
        boolean soundPlayed = false;
        for (int i = 0; i < steps; i++) {
            int idx = rightToLeft ? (steps - 1 - i) : i;
            double angle = Math.toRadians(-arcDegrees / 2 + (arcDegrees / (steps - 1)) * idx);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vector dir = direction.clone();
            double x = dir.getX() * cos - dir.getZ() * sin;
            double z = dir.getX() * sin + dir.getZ() * cos;
            Vector arcDir = new Vector(x, dir.getY(), z).normalize();
            Location particleLoc = loc.clone().add(arcDir.multiply(radius));
            player.getWorld().spawnParticle(
                    Particle.DUST,
                    particleLoc,
                    10,
                    0.1, 0.05, 0.1, 0.02,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 120, 255), 1.5f)
            );
            if (!soundPlayed) {
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                soundPlayed = true;
            }
            player.getWorld().getNearbyEntities(particleLoc, 1.0, 1.0, 1.0).forEach(entity -> {
                if (entity != player && entity instanceof org.bukkit.entity.LivingEntity living) {
                    living.damage(damage, player);
                }
            });
            try {
                Thread.sleep(15);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void playWaterWheelold(Player player) {
        new Thread(() -> {
            try {
                double baseRadius = 2.5;
                int points = 36;
                int steps = 30;
                double yStep = 0.5 / steps;
                double velocityPerStep = 0.5 / (steps * 0.03);
                double yStart = player.getLocation().getY() + 1;
                for (int t = 0; t < steps; t++) {
                    Location center = player.getLocation().clone().add(0, 1, 0);
                    Vector look = player.getLocation().getDirection().normalize();
                    Vector up = new Vector(0, 1, 0);
                    Vector right = look.clone().crossProduct(up).normalize();
                    Vector axis = right;
                    int arc = points / 4;
                    double swirl = Math.PI * 2 * t / steps;
                    double y = t * yStep;
                    for (int i = 0; i < arc; i++) {
                        double angle = 2 * Math.PI * (i + t) / points + swirl;
                        double radius = baseRadius + 0.3 * Math.sin(angle * 2 + swirl * 2);
                        double x = Math.cos(angle) * radius;
                        double yCircle = Math.sin(angle) * radius;
                        Vector offset = axis.clone().multiply(x).add(up.clone().multiply(yCircle + y));
                        Location particleLoc = center.clone().add(offset).add(look.clone().multiply(0.2));
                        player.getWorld().spawnParticle(
                                Particle.DUST,
                                particleLoc,
                                8,
                                0.12, 0.12, 0.12, 0.01,
                                new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 120, 255), 1.5f)
                        );
                        if (i % 4 == 0) {
                            player.getWorld().spawnParticle(
                                    Particle.SPLASH,
                                    particleLoc,
                                    10,
                                    0.18, 0.12, 0.18, 0.04
                            );
                        }
                    }
                    player.setVelocity(new Vector(0, velocityPerStep, 0));
                    Thread.sleep(30);
                }
                long duration = 1000;
                int extraSteps = (int) (duration / 30);
                for (int t = 0; t < extraSteps; t++) {
                    Location center = player.getLocation().clone().add(0, 1, 0);
                    Vector look = player.getLocation().getDirection().normalize();
                    Vector up = new Vector(0, 1, 0);
                    Vector right = look.clone().crossProduct(up).normalize();
                    Vector axis = right;
                    int arc = points / 4;
                    double swirl = Math.PI * 2 * t / extraSteps + Math.PI * 2;
                    double y = 0.5;
                    for (int i = 0; i < arc; i++) {
                        double angle = 2 * Math.PI * (i + t) / points + swirl;
                        double radius = baseRadius + 0.3 * Math.sin(angle * 2 + swirl * 2);
                        double x = Math.cos(angle) * radius;
                        double yCircle = Math.sin(angle) * radius;
                        Vector offset = axis.clone().multiply(x).add(up.clone().multiply(yCircle + y));
                        Location particleLoc = center.clone().add(offset).add(look.clone().multiply(0.2));
                        player.getWorld().spawnParticle(
                                Particle.DUST,
                                particleLoc,
                                8,
                                0.12, 0.12, 0.12, 0.01,
                                new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 120, 255), 1.5f)
                        );
                        if (i % 4 == 0) {
                            player.getWorld().spawnParticle(
                                    Particle.SPLASH,
                                    particleLoc,
                                    10,
                                    0.18, 0.12, 0.18, 0.04
                            );
                        }
                    }
                    player.setVelocity(new Vector(0, 0, 0));
                    Thread.sleep(30);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.2f, 1.0f);
    }

    // Neue breite Wasserattacke: 4 kleine Kreise mit Soul Flames und Schaden für Entities
    public static void playWaterCircleAttack(Player player) {
        Location start = player.getLocation().clone();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        double distanceBetween = 2.5;
        double circleRadius = 1.2;
        int circlePoints = 28;
        double y = player.getLocation().getY();
        new BukkitRunnable() {
            int c = 1;

            @Override
            public void run() {
                if (c > 4 || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                Location circleCenter = start.clone().add(direction.clone().multiply(distanceBetween * c));
                circleCenter.setY(y);
                // Partikelkreis (jetzt DUST aqua-blau)
                for (int i = 0; i < circlePoints; i++) {
                    double angle = 2 * Math.PI * i / circlePoints;
                    double x = Math.cos(angle) * circleRadius;
                    double z = Math.sin(angle) * circleRadius;
                    Location particleLoc = circleCenter.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            particleLoc,
                            0,
                            0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.5f)
                    );
                }
                // Schaden und "Stahlstrahl" an Entities im Kreis (Radius 1 Block)
                player.getWorld().getNearbyEntities(circleCenter, 1.0, 1.0, 1.0).forEach(entity -> {
                    if (!entity.equals(player) && entity instanceof LivingEntity living) {
                        living.damage(4.0, player);
                        // Animierter "Stahlstrahl": von unten nach oben mit BukkitRunnable
                        Location startBeam = living.getLocation().clone();
                        double beamRadius = circleRadius;
                        new BukkitRunnable() {
                            double dy = 0;

                            @Override
                            public void run() {
                                if (dy > 4.0) {
                                    this.cancel();
                                    return;
                                }
                                for (int i = 0; i < 18; i++) {
                                    double angle = 2 * Math.PI * i / 18;
                                    double x = Math.cos(angle) * beamRadius;
                                    double z = Math.sin(angle) * beamRadius;
                                    Location beamLoc = startBeam.clone().add(x, dy, z);
                                    player.getWorld().spawnParticle(
                                            Particle.DUST,
                                            beamLoc,
                                            1,
                                            0.05, 0, 0.05, 0.01,
                                            new Particle.DustOptions(Color.fromRGB(0, 120, 255), 1.5f)
                                    );
                                    player.getWorld().spawnParticle(
                                            Particle.SMOKE,
                                            beamLoc,
                                            1,
                                            0.03, 0, 0.03, 0.01
                                    );
                                }
                                dy += 0.45;
                            }
                        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
                    }
                });
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SOUL_SOIL_HIT, 1.2f, 0.7f);
                c++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 5L); // 5 Ticks = 0,25s
    }

    // Neue Water Ability: Aqua Pulse Kreis
    public void playAquaPulse(org.bukkit.entity.Player player) {
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("Demon");
        if (plugin == null) return;
        final org.bukkit.Location center = player.getLocation().add(0, 1, 0);
        final int pulses = 7; // Anzahl der Kreise
        final double maxRadius = 5.0;
        final int pointsPerCircle = 32;
        final org.bukkit.Particle.DustOptions dustAqua = new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0, 180, 255), 1.7f);

        new org.bukkit.scheduler.BukkitRunnable() {
            int pulse = 0;
            boolean expanding = true;

            @Override
            public void run() {
                if (expanding && pulse > pulses) {
                    expanding = false;
                    pulse = pulses - 1;
                } else if (!expanding && pulse < 0) {
                    cancel();
                    return;
                }
                double radius = (maxRadius / pulses) * pulse;
                for (int i = 0; i < pointsPerCircle; i++) {
                    double angle = 2 * Math.PI * i / pointsPerCircle;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    org.bukkit.Location loc = center.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(
                            org.bukkit.Particle.DUST,
                            loc,
                            1,
                            0.01, 0, 0.01, 0.01,
                            dustAqua
                    );
                }
                if (expanding) {
                    pulse++;
                } else {
                    pulse--;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // jetzt alle 1 Tick ein neuer Kreis (schneller)
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 0.7f, 1.4f);
    }


    /**
     * First Form: Water Surface Slash
     * A powerful, momentum‑driven horizontal slash used to decapitate the Hand Demon.
     * The user generates enough momentum to create a concentrated slash :contentReference[oaicite:2]{index=2}.
     */
    public static void playWaterSurfaceSlash(Player player) {
        new BukkitRunnable() {
            final int arcPoints = 48;
            final int steps = 14; // schneller = weniger steps, mehr Speed
            int currentStep = 0;
            final double radius = 3.3;

            @Override
            public void run() {
                if (currentStep > steps) {
                    // 💥 Final Impact Splash
                    Location loc = player.getLocation().add(0, 1.2, 0);
                    player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 30, 1, 0.5, 1, 0.1);
                    player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.1f);
                    this.cancel();
                    return;
                }

                Location center = player.getLocation().clone().add(0, 1.3, 0);
                Vector look = player.getLocation().getDirection().normalize();
                Vector up = new Vector(0, 1, 0);
                Vector right = look.clone().crossProduct(up).normalize();
                double swirl = Math.PI * currentStep / steps;

                for (int i = 0; i < arcPoints; i++) {
                    double angle = Math.PI * i / arcPoints + swirl;
                    double x = Math.cos(angle) * radius;
                    double y = Math.sin(angle) * radius * 0.3;
                    Vector offset = right.clone().multiply(x).add(up.clone().multiply(y));
                    Location particleLoc = center.clone().add(offset).add(look.clone().multiply(0.7));

                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            particleLoc,
                            6,
                            0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 120, 255), 1.7f)
                    );

                    if (i % 4 == 0) {
                        player.getWorld().spawnParticle(
                                Particle.SPLASH,
                                particleLoc,
                                6,
                                0.12, 0.1, 0.12, 0.03
                        );
                    }
                }

                // 💢 Dash & Sound
                if (currentStep == 2) {
                    player.setVelocity(look.clone().multiply(1.1));
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);
                }

                // 💥 Knockback & Damage once at slash peak
                if (currentStep == 6) {
                    for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 3, 2, 3)) {
                        if (e instanceof LivingEntity && e != player) {
                            LivingEntity target = (LivingEntity) e;
                            Vector to = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                            if (look.dot(to) > 0.5) {
                                target.damage(10.0, player);
                                target.setVelocity(look.clone().multiply(0.8).setY(0.4));
                                player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.1f, 1.2f);
                            }
                        }
                    }
                }

                currentStep++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L); // schneller = 1 Tick Delay
    }


    public static void playWaterWheel(Player player, Plugin plugin, LivingEntity target) {
        // Sound beim Start
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.2f, 1.0f);

        final double forwardSpeed = 1.2;
        final double upwardSpeed = 0.8;
        final int totalSteps = 25;
        final Vector direction = player.getLocation().getDirection().normalize();
        final AtomicInteger tick = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                int t = tick.getAndIncrement();
                if (t >= totalSteps || !player.isOnline() || player.isDead()) {
                    // Slash-Effekt erscheint am Gegner
                    spawnRotatingWaterSlashAtTarget(target.getLocation().add(0, 1, 0), player, target, plugin);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
                    cancel();
                    return;
                }

                // Bewegung nach vorne mit Flip
                double vertical = upwardSpeed * Math.sin((Math.PI * 2 * t) / totalSteps);
                double forward = forwardSpeed * (1 - Math.abs(Math.sin((Math.PI * 2 * t) / totalSteps)) * 0.3);
                player.setVelocity(direction.clone().multiply(forward).add(new Vector(0, vertical, 0)));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnRotatingWaterSlashAtTarget(Location center, Player player, LivingEntity target, Plugin plugin) {
        final int points = 36;
        final double radius = 2.5;
        final Vector right = player.getLocation().getDirection().clone().crossProduct(new Vector(0, 1, 0)).normalize();
        final Vector up = new Vector(0, 1, 0);
        final World world = center.getWorld();

        // Schaden + Knockback einmalig zu Beginn
        Vector knockback = player.getLocation().getDirection().normalize().multiply(1.2).setY(0.4);
        target.setVelocity(knockback);
        target.damage(6.0, player);
        world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.2f);

        // Animation über 12 Ticks (~0.6s)
        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 12;

            @Override
            public void run() {
                if (tick >= maxTicks || !player.isOnline() || target.isDead()) {
                    cancel();
                    return;
                }

                double baseRotation = (Math.PI * 2) * tick / maxTicks;

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points + baseRotation;
                    double r = radius + 0.3 * Math.sin(angle * 2 + tick * 0.3);

                    Vector vec = right.clone().multiply(Math.cos(angle) * r)
                            .add(up.clone().multiply(Math.sin(angle) * r));

                    Location particleLoc = center.clone().add(vec);

                    world.spawnParticle(
                            Particle.DUST,
                            particleLoc,
                            2,
                            0.1, 0.1, 0.1,
                            0.01,
                            new Particle.DustOptions(Color.fromRGB(0, 150, 255), 1.5f)
                    );

                    if (i % 6 == 0) {
                        world.spawnParticle(
                                Particle.SPLASH,
                                particleLoc,
                                4,
                                0.1, 0.1, 0.1,
                                0.02
                        );
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }






    /**
     * Third Form: Flowing Dance
     * The user performs a fluid, dance-like motion, attacking in one continuous strike.
     */
        public static void playFlowingDance(Player player, LivingEntity target) {
            new BukkitRunnable() {
                final int duration = 60; // ticks (~3 Sekunden)
                int t = 0;
                final double orbitRadius = 2.5;
                final Location initialTargetLoc = target.getLocation().clone().add(0, 1, 0);

                @Override
                public void run() {
                    if (!target.isValid()) {
                     //   player.sendMessage("§cZiel nicht mehr vorhanden.");
                        cancel();
                        return;
                    }

                    Location targetLoc = target.getLocation().clone().add(0, 1, 0);

                    if (t >= duration) {
                        player.setVelocity(new Vector(0, 0, 0));

                        // Finale: Wasserexplosion + Knockback
                        Location impact = player.getLocation();
                        impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 40, 1, 0.5, 1, 0.1);
                        impact.getWorld().spawnParticle(Particle.DRAGON_BREATH, impact, 30, 0.3, 0.3, 0.3, 0.02);
                        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 1.1f);

                        target.damage(10.0, player);
                        target.setVelocity(player.getLocation().getDirection().normalize().multiply(0.6).setY(0.5));

                        cancel();
                        return;
                    }

                    // Zuerst in Richtung des Ziels ziehen (Frame 0–10)
                    if (t < 10) {
                        Vector toTarget = targetLoc.toVector().subtract(player.getLocation().toVector()).normalize();
                        player.setVelocity(toTarget.multiply(0.6));
                    } else {
                        // Danach: Orbitbewegung
                        double angle = 2 * Math.PI * (t - 10) / (duration - 10);
                        double x = Math.cos(angle) * orbitRadius;
                        double z = Math.sin(angle) * orbitRadius;
                        Location orbit = targetLoc.clone().add(x, 0, z);

                        Vector move = orbit.toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.5);
                        player.setVelocity(move);
                    }

                    // Spiraleffekt um Spieler
                    double swirl = t * 0.4;
                    for (int i = 0; i < 2; i++) {
                        double a = swirl + i * Math.PI;
                        double y = Math.sin(a * 2) * 0.4;
                        double sx = Math.cos(a) * 1.4;
                        double sz = Math.sin(a) * 1.4;
                        Location spiral = player.getLocation().clone().add(sx, 1.2 + y, sz);

                        player.getWorld().spawnParticle(
                                Particle.DUST,
                                spiral,
                                4, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0, 130, 255), 1.6f)
                        );
                        if (t % 4 == 0) {
                            player.getWorld().spawnParticle(Particle.SPLASH, spiral, 6, 0.1, 0.1, 0.1, 0.01);
                        }
                    }

                    // Partikel-Kreis auf Boden um Gegner
                    if (t % 3 == 0) {
                        for (int i = 0; i < 12; i++) {
                            double a = Math.toRadians(i * 30 + t * 6);
                            double rx = Math.cos(a) * 1.8;
                            double rz = Math.sin(a) * 1.8;
                            Location ring = targetLoc.clone().add(rx, 0.1, rz);
                            player.getWorld().spawnParticle(Particle.DRIPPING_WATER, ring, 1, 0, 0, 0, 0);
                        }
                    }

                    // Soundeffekte
                    if (t == 0) {
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1.2f, 1.1f);
                    } else if (t % 12 == 0) {
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WATER_AMBIENT, 0.8f, 1.3f);
                    }

                    t++;
                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
        }








    /**
         * Fourth Form: Striking Tide
         * A rapid series of slashes delivered in quick succession.
         */
    public static void playStrikingTide(Player player, LivingEntity target) {
        new BukkitRunnable() {
            final int duration = 40; // Ticks
            int t = 0;
            final Location origin = player.getLocation();

            @Override
            public void run() {
                if (!target.isValid()) {
                    cancel();
                    return;
                }

                if (t >= duration) {
                    player.setVelocity(new Vector(0, 0, 0));

                    // Overkill-Finalhit mit epischem Effekt
                    Location impact = player.getLocation();
                    World world = impact.getWorld();

                    world.spawnParticle(Particle.EXPLOSION, impact, 1);
                    world.spawnParticle(Particle.DRAGON_BREATH, impact, 40, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.SPLASH, impact, 60, 0.7, 0.5, 0.7, 0.1);
                    world.spawnParticle(Particle.FLASH, impact, 3);
                    world.spawnParticle(Particle.CLOUD, impact, 20, 0.4, 0.4, 0.4, 0.02);
                    world.spawnParticle(Particle.END_ROD, impact, 30, 0.3, 0.3, 0.3, 0.02);

                   // world.playSound(impact, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);
                    world.playSound(impact, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.7f, 0.9f);

                    // One-Hit-Style Schaden und Knockback
                    target.damage(50.0, player);
                    target.setVelocity(player.getLocation().getDirection().normalize().multiply(1.2).setY(1.0));


                    cancel();
                    return;
                }

                if (t % 3 == 0) {
                    Location targetLoc = target.getLocation().clone().add(0, 1, 0);
                    double angle = Math.toRadians(t * 45);
                    double distance = 2.5;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;
                    Location attackLoc = targetLoc.clone().add(x, 0, z);

                    Vector dash = attackLoc.toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.9);
                    player.setVelocity(dash);

                    World world = player.getWorld();

                    world.spawnParticle(Particle.DUST, attackLoc.clone().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.01,
                            new Particle.DustOptions(Color.fromRGB(0, 130, 255), 1.6f));
                    world.spawnParticle(Particle.SWEEP_ATTACK, attackLoc, 6, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.SPLASH, attackLoc, 16, 0.2, 0.1, 0.2, 0.03);
                    world.spawnParticle(Particle.END_ROD, attackLoc, 6, 0.1, 0.1, 0.1, 0.01);
                    world.spawnParticle(Particle.DRIPPING_WATER, attackLoc, 8, 0.2, 0.1, 0.2, 0.02);

                    world.playSound(attackLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.4f);

                    Location trail = player.getLocation().clone().subtract(dash.normalize().multiply(1.2)).add(0, 1, 0);
                    world.spawnParticle(Particle.CLOUD, trail, 10, 0.2, 0.05, 0.2, 0.01);
                    world.spawnParticle(Particle.DUST, trail, 4, 0.1, 0.1, 0.1, 0.005,
                            new Particle.DustOptions(Color.fromRGB(0, 180, 255), 1.0f));

                    target.damage(2.5, player);
                    target.getWorld().spawnParticle(Particle.DRIPPING_WATER, target.getLocation().add(0, 1, 0), 8, 0.3, 0.1, 0.3, 0.02);

                    for (int i = 0; i < 12; i++) {
                        double a = Math.toRadians(i * 30);
                        double rx = Math.cos(a) * 1.8;
                        double rz = Math.sin(a) * 1.8;
                        Location ringLoc = target.getLocation().clone().add(rx, 0.05, rz);
                        world.spawnParticle(Particle.DRIPPING_WATER, ringLoc, 1, 0, 0, 0, 0);
                    }
                }

                t++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
    }



/**
         * Fifth Form: Blessed Rain After the Drought
         * A graceful, painless decapitation strike used to offer mercy.
         */
public static void playBlessedRainAfterDrought(Player player, LivingEntity target) {
    Location lockedTargetLocation = target.getLocation().clone().add(0, 1, 0);
    new BukkitRunnable() {
        int t = 0;
        final int duration = 40;
        final Location start = player.getLocation().clone();
        final Vector direction = lockedTargetLocation.toVector().subtract(start.toVector()).normalize();
        final World world = player.getWorld();

        @Override
        public void run() {
            if (t == 0) {
                world.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            }

            if (t < duration / 2) {
                // Spieler bewegt sich schnell durch den Gegner hindurch
                player.setVelocity(direction.clone().multiply(1.2));

                // Spiraleffekt beim Flug
                double swirl = t * 0.35;
                for (int i = 0; i < 2; i++) {
                    double angle = swirl + i * Math.PI;
                    double x = Math.cos(angle) * 1.2;
                    double z = Math.sin(angle) * 1.2;
                    Location spiral = player.getLocation().clone().add(x, 1.3, z);

                    world.spawnParticle(
                            Particle.DUST,
                            spiral,
                            4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0, 180, 255), 1.5f)
                    );
                }
            } else if (t == duration / 2) {
                // Spieler stoppt nach dem Durchflug
                player.setVelocity(new Vector(0, 0, 0));

                // Gnadenvoller finaler Hit
                world.spawnParticle(Particle.FALLING_WATER, lockedTargetLocation, 40, 0.4, 0.2, 0.4, 0.05);
                world.spawnParticle(Particle.FLASH, lockedTargetLocation, 2);
                world.spawnParticle(Particle.DRAGON_BREATH, lockedTargetLocation, 20, 0.3, 0.3, 0.3, 0.02);

                world.playSound(lockedTargetLocation, Sound.BLOCK_BEACON_POWER_SELECT, 1.3f, 1.1f);
                world.playSound(lockedTargetLocation, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.9f, 0.7f);

                target.damage(20.0, player);
                target.setVelocity(new Vector(0, 0.5, 0));
            }

            if (t >= duration) {
                cancel();
            }

            t++;
        }
    }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
}

        /**
         * Sixth Form: Whirlpool
         * The user spins their body to create a whirlpool effect around them.
         */
        public static void playWhirlpool(Player player) {
            boolean underwater = player.getLocation().getBlock().isLiquid();
            new BukkitRunnable() {
                int t = 0;
                final int duration = underwater ? 60 : 40;
                final double radius = underwater ? 4.0 : 2.5;
                final double damage = underwater ? 4.0 : 2.0;
                final World world = player.getWorld();
                final Location center = player.getLocation().clone().add(0, 0.5, 0);

                @Override
                public void run() {
                    if (t >= duration) {
                        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.2f, 1.0f);
                        world.spawnParticle(Particle.EXPLOSION, center, 30, 0.5, 0.2, 0.5, 0.05);
                     //   player.sendActionBar("§b六ノ型 ねじれ渦 §f| §9Sixth Form: Whirlpool"); // Zeige Technik-Namen
                        cancel();
                        return;
                    }

                    // Spieler rotiert schnell
                    Location loc = player.getLocation();
                    loc.setYaw(loc.getYaw() + (underwater ? 45 : 30));
                    player.teleport(loc);

                    // Mehrere Spiralen
                    double mainAngle = Math.toRadians(t * (underwater ? 60 : 40));
                    for (int spiral = 0; spiral < 3; spiral++) {
                        double spiralOffset = spiral * Math.PI * 2 / 3.0;
                        for (int i = 0; i < 12; i++) {
                            double a = mainAngle + spiralOffset + i * Math.PI / 6;
                            double y = Math.sin(a + t * 0.2) * 0.5 + 1;
                            double r = radius - 0.5 + Math.cos(a * 2) * 0.2;
                            double x = Math.cos(a) * r;
                            double z = Math.sin(a) * r;
                            Location swirl = center.clone().add(x, y, z);

                            // Wasser-Luft-Mix
                            if (underwater) {
                                world.spawnParticle(Particle.DRIPPING_WATER, swirl, 1, 0, 0, 0, 0.01);
                                if (i % 3 == 0) world.spawnParticle(Particle.BUBBLE_COLUMN_UP, swirl, 2, 0, 0, 0, 0.01);
                            } else {
                                world.spawnParticle(Particle.CLOUD, swirl, 1, 0, 0, 0, 0.01);
                                if (i % 3 == 0) world.spawnParticle(Particle.CRIT, swirl, 1, 0, 0, 0, 0.01);
                            }
                            // Farbiges Dust
                            world.spawnParticle(Particle.DUST, swirl, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 110, 255), 1.3f));
                        }
                    }

                    // Glitzer/Glow um den Spieler
                    world.spawnParticle(Particle.ENCHANTED_HIT, center, 8, 0.3, 0.6, 0.3, 0.02);

                    // Slash-Effekt am Spieler
                    if (t % 4 == 0) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 5, 0.7, 0.2, 0.7, 0.02);
                    }

                    // Sounds und Wasser-Luft-Spezial
                    if (t % 3 == 0 && !underwater) {
                        world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.1f, 1.18f);
                    }
                    if (t % 3 == 0 && underwater) {
                        world.playSound(center, Sound.ITEM_TRIDENT_RIPTIDE_1, 1.2f, 1.15f);
                    }

                    // Gegner treffen
                    for (Entity entity : world.getNearbyEntities(center, radius, 2.5, radius)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            Vector pull = center.toVector().subtract(target.getLocation().toVector()).normalize().multiply(underwater ? 0.35 : 0.22);
                            pull.setY(0.23 + Math.random() * 0.12);
                            target.setVelocity(pull);
                            target.damage(damage, player);

                            // Slash-/Blut-Effekt
                            world.spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0,1,0), 3, 0.4, 0.2, 0.4, 0.04);
                            world.spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0,1,0), 6, 0.3, 0.2, 0.3, 0.13);
                        }
                    }

                    // Actionbar während der Attacke
                    if (t % 10 == 0) {
                   //     player.sendActionBar("§b六ノ型 ねじれ渦 §f| §9Sixth Form: Whirlpool");
                    }

                    t++;
                }
            }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
        }

    /**
     * Seventh Form: Drop Ripple Thrust, Curve
     * 雫波紋突き・曲 (Shizuku Hamon Tsuki - Kyoku)
     */
    public static void playDropRippleThrust(Player player, Entity targetEntity) {
        new BukkitRunnable() {
            int t = 0;
            final int duration = 15;
            final World world = player.getWorld();
            final Location startLoc = player.getEyeLocation().subtract(0, 0.2, 0);
            // Nutze die Blickrichtung des Spielers als Basis
            final Vector direction = player.getLocation().getDirection();
            Location targetLoc;
            boolean hasHit = false;
            Location impactLocation = null;

            @Override
            public void run() {
                if (t >= duration && !hasHit) {
                    world.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 2.0f);
              //      player.sendActionBar("§7七ノ型 §b雫波紋突き・曲 §f| §9Seventh Form: Drop Ripple Thrust, Curve");
                    cancel();
                    return;
                }

                if (hasHit && t >= duration + 10) {
                    cancel();
                    return;
                }

                if (!hasHit) {
                    double progress = t / (double) duration;
                    Vector currentDir = direction.clone().multiply(progress * 4.0);
                    Location currentLoc = startLoc.clone().add(currentDir);

                    // Bewegungseffekte
                    for (double d = 0; d <= progress * 4.0; d += 0.2) {
                        Location pointLoc = startLoc.clone().add(direction.clone().multiply(d));
                        double wave = Math.sin(d * Math.PI + t * 0.5) * 0.3;
                        pointLoc.add(direction.clone().crossProduct(new Vector(0, 1, 0)).multiply(wave));

                        world.spawnParticle(Particle.DUST, pointLoc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(130, 190, 255), 0.7f));

                        if (t % 2 == 0) {
                            world.spawnParticle(Particle.DRIPPING_WATER, pointLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    }

                    // Aufprall-Erkennung
                    for (Entity entity : world.getNearbyEntities(currentLoc, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;
                            hasHit = true;
                            impactLocation = target.getLocation().clone();

                            // Momentum-Neutralisation
                            Vector newVelocity = target.getVelocity().multiply(0.1);
                            target.setVelocity(newVelocity);
                            target.damage(4.0, player);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 5, false, false));

                            // Initial Aufprall-Effekt
                            world.spawnParticle(Particle.FLASH, target.getLocation(), 1, 0, 0, 0, 0);
                            world.playSound(target.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0f, 2.0f);
                            world.playSound(target.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.2f, 1.0f);
                            break;
                        }
                    }

                    // Block-Aufprall-Erkennung
                    if (!hasHit && currentLoc.getBlock().getType().isSolid()) {
                        hasHit = true;
                        impactLocation = currentLoc.clone();
                        world.playSound(currentLoc, Sound.BLOCK_WATER_AMBIENT, 1.0f, 2.0f);
                    }
                } else {
                    // Dichte Partikelwand nach Aufprall
                    if (impactLocation != null) {
                        Vector wallDirection = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();

                        // Erstelle eine dichte Partikelwand
                        for (double h = -2; h <= 2; h += 0.2) {
                            for (double w = -2; w <= 2; w += 0.2) {
                                Location particleLoc = impactLocation.clone().add(
                                        wallDirection.clone().multiply(w).add(new Vector(0, h, 0))
                                );

                                double wave = Math.sin(h * 2 + w * 2 + t * 0.3) * 0.3;
                                particleLoc.add(direction.multiply(wave));

                                if (Math.random() < 0.3) {
                                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                                }
                                if (Math.random() < 0.4) {
                                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.05, 0.05, 0.05, 0,
                                            new Particle.DustOptions(Color.fromRGB(130, 190, 255), 0.7f));
                                }
                                if (Math.random() < 0.2) {
                                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                                }
                                if (Math.random() < 0.05) {
                                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                                }
                            }
                        }

                        // Expandierende Welle
                        double expandingRadius = (t - duration) * 0.2;
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double x = Math.cos(angle) * expandingRadius;
                            double z = Math.sin(angle) * expandingRadius;
                            Location waveLoc = impactLocation.clone().add(x, 0, z);
                            world.spawnParticle(Particle.SPLASH, waveLoc, 1, 0.05, 0.05, 0.05, 0);
                        }

                        if (t % 2 == 0) {
                            world.playSound(impactLocation, Sound.BLOCK_WATER_AMBIENT, 0.5f, 1.5f);
                        }
                    }
                }

                t++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
    }


    /**
     * Eighth Form: Waterfall Basin
     * 八ノ型 滝壺 (Hachi no kata: Takitsubo)
     * A heavy downward strike empowered by gravity.
     */
    public static void playWaterfallBasin(Player player) {
        new BukkitRunnable() {
            int t = 0;
            final int duration = 25; // Längere Dauer für höheren Sprung
            final World world = player.getWorld();
            Location playerLoc = player.getLocation().clone();
            double height = 6.0; // Erhöhte Starthöhe des Wasserfalls
            boolean hasStruck = false;

            @Override
            public void run() {
                if (t >= duration) {
                    cancel();
                    return;
                }

                if (!hasStruck) {
                    // Aufsteigen und Wasserfall-Vorbereitung (0-12 ticks)
                    if (t < 12) { // Längere Aufstiegsphase
                        // Spieler deutlich höher und schneller anheben
                        player.setVelocity(new Vector(0, 1.2, 0)); // Erhöhte Aufstiegsgeschwindigkeit

                        // Intensivere aufsteigende Wasserpartikel
                        for (double h = 0; h < 4; h += 0.4) {
                            Location particleLoc = playerLoc.clone().add(0, h, 0);
                            world.spawnParticle(Particle.SPLASH, particleLoc, 6, 0.3, 0.1, 0.3, 0.1);
                            world.spawnParticle(Particle.DUST, particleLoc, 3, 0.3, 0.1, 0.3, 0,
                                    new Particle.DustOptions(Color.fromRGB(130, 190, 255), 1.0f));

                            // Zusätzliche Aufstiegs-Effekte
                            if (Math.random() < 0.3) {
                                world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            }
                        }

                        // Intensivere Soundeffekte beim Aufstieg
                        if (t % 2 == 0) {
                            world.playSound(playerLoc, Sound.BLOCK_WATER_AMBIENT, 0.7f, 1.4f);
                            world.playSound(playerLoc, Sound.ENTITY_PLAYER_SPLASH, 0.3f, 1.8f);
                        }
                    }
                    // Wasserfallschlag (nach 12 ticks)
                    else {
                        hasStruck = true;
                        // Deutlich stärkerer Abwärtsimpuls
                        player.setVelocity(new Vector(0, -2.5, 0)); // Erhöhte Fallgeschwindigkeit
                        world.playSound(playerLoc, Sound.ENTITY_PLAYER_SPLASH, 1.4f, 0.6f);
                        world.playSound(playerLoc, Sound.BLOCK_WATER_AMBIENT, 1.2f, 0.4f);
                        // Zusätzlicher dramatischer Sound
                        world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
                    }
                } else {
                    // Wasserfall-Effekte und Schaden
                    Location baseLoc = player.getLocation();

                    // Intensiverer Wasserfall-Haupteffekt
                    for (double h = 0; h < height; h += 0.2) {
                        Location waterLoc = baseLoc.clone().add(0, h, 0);

                        // Mehr Wasserfallpartikel
                        world.spawnParticle(Particle.FALLING_WATER, waterLoc, 4, 0.4, 0.1, 0.4, 0);
                        world.spawnParticle(Particle.SPLASH, waterLoc, 3, 0.4, 0.1, 0.4, 0);

                        if (Math.random() < 0.4) {
                            world.spawnParticle(Particle.DUST, waterLoc, 1, 0.4, 0.1, 0.4, 0,
                                    new Particle.DustOptions(Color.fromRGB(130, 190, 255), 1.2f));
                        }
                    }

                    // Größere Aufprall-Effekte am Boden
                    Location groundLoc = baseLoc.clone();
                    double radius = 4.0; // Erhöhter Radius
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 10) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location splashLoc = groundLoc.clone().add(x, 0, z);

                        world.spawnParticle(Particle.SPLASH, splashLoc, 6, 0.2, 0, 0.2, 0.15);
                        if (t % 2 == 0) {
                            world.spawnParticle(Particle.DUST, splashLoc, 3, 0.2, 0, 0.2, 0,
                                    new Particle.DustOptions(Color.fromRGB(130, 190, 255), 1.2f));
                        }
                    }

                    // Schaden und Knockback für Gegner im Bereich
                    for (Entity entity : world.getNearbyEntities(baseLoc, radius, 3, radius)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Erhöhter Schaden
                            target.damage(20.0, player);

                            // Stärkerer Knockback nach unten
                            Vector knockback = new Vector(0, -0.8, 0);
                            target.setVelocity(knockback);

                            // Intensivere Treffer-Effekte
                            world.spawnParticle(Particle.SPLASH,
                                    target.getLocation().add(0, 1, 0),
                                    30, 0.4, 0.4, 0.4, 0.3);
                            world.playSound(target.getLocation(),
                                    Sound.ENTITY_PLAYER_SPLASH, 1.2f, 0.8f);
                        }
                    }

                    // Höhe des Wasserfalls reduzieren
                    height = Math.max(0, height - 0.3);
                }

                // Actionbar-Anzeige
                if (t % 5 == 0) {
                //    player.sendActionBar("§7八ノ型 §b滝壺 §f| §9Eighth Form: Waterfall Basin");
                }

                t++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
    }

        /**
         * Ninth Form: Splashing Water Flow – Turbulent
         * Enhances movement and agility on unstable surfaces.
         */
        public static void playSplashingWaterFlowTurbulent(Player player, Plugin plugin) {
            World world = player.getWorld();

            // Wasser-Start-Effekt
            world.spawnParticle(Particle.SPLASH, player.getLocation(), 30, 0.5, 0.2, 0.5, 0.1);
            world.playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1f, 1.2f);

            // Leichter Dash zum Start
            Vector dash = player.getLocation().getDirection().normalize().multiply(1.2).setY(0.2);
            player.setVelocity(dash);

            // Bonus: Kein Fallschaden
            player.setFallDistance(0);

            // Status-Effekt für extreme Agilität
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 2, false, false, false));     // Bessere Luftsteuerung
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false, false));    // Schnelle Steps
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false, false)); // Schwebender Stil

            // Visuelle Trails für 5 Sekunden
            new BukkitRunnable() {
                int ticks = 0;
                final int maxTicks = 100;

                @Override
                public void run() {
                    if (ticks++ >= maxTicks || !player.isOnline() || player.isDead()) {
                        cancel();
                        return;
                    }

                    Location loc = player.getLocation();

                    world.spawnParticle(Particle.DRIPPING_WATER, loc, 8, 0.3, 0.1, 0.3, 0.01);
                    world.spawnParticle(Particle.SPLASH, loc, 4, 0.2, 0.05, 0.2, 0.01);

                    // Alle 10 Ticks ein kleiner „Step“-Sound
                    if (ticks % 10 == 0) {
                        world.playSound(loc, Sound.BLOCK_WET_GRASS_STEP, 0.6f, 1.4f);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    /**
     * Tenth Form: Constant Flux (拾ノ型 変化)
     * A continuous flowing attack that adapts its movements like a dragon made of water
     */

 //   public WaterBreathingAbility (Player player) {
   //     this.player = player;
  //  }

  public static Random random = new Random();

    public static void useTenthForm(Player player) {
        player.sendMessage("§b水 §3拾ノ型 変化 §b(Tenth Form: Constant Flux)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial effect sounds
        world.playSound(startLoc, Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private Location lastLocation = startLoc.clone();
            private int phase = 0;
            private double baseRadius = 2.0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinalSurge();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Update movement pattern every 0.5 seconds
                if (time % 0.5 == 0) {
                    phase = (phase + 1) % 4;
                    world.playSound(currentLoc, Sound.ENTITY_PLAYER_SPLASH, 0.6f, 2.0f);
                }

                // Execute movement pattern
                executeFluxMovement(currentLoc, direction);

                // Create water dragon effects
                createWaterDragonEffects(currentLoc, direction);

                // Check for hits
                checkHits(currentLoc, direction);

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void executeFluxMovement(Location current, Vector direction) {
                double speed = 0.8;
                Vector movement;

                switch (phase) {
                    case 0 -> { // Spiral ascent
                        movement = direction.clone().multiply(speed);
                        movement.setY(0.4);
                        movement.rotateAroundY(time * 4);
                    }
                    case 1 -> { // Horizontal weaving
                        movement = direction.clone().multiply(speed);
                        movement.rotateAroundY(Math.sin(time * 6) * Math.PI / 4);
                    }
                    case 2 -> { // Diving attack
                        movement = direction.clone().multiply(speed * 1.2);
                        movement.setY(-0.3 + Math.sin(time * 8) * 0.2);
                    }
                    default -> { // Rising surge
                        movement = direction.clone().multiply(speed);
                        movement.setY(0.2 + Math.cos(time * 5) * 0.3);
                    }
                }

                player.setVelocity(movement);
            }

            private void createWaterDragonEffects(Location location, Vector direction) {
                // Dragon body segments
                int segments = 8;
                Vector[] segmentPositions = new Vector[segments];

                // Calculate dragon curve
                for (int i = 0; i < segments; i++) {
                    double t = (double) i / segments;
                    double x = Math.sin(time * 8 + t * Math.PI * 2) * baseRadius * (1 - t);
                    double y = Math.cos(time * 6 + t * Math.PI * 2) * baseRadius * (1 - t);
                    double z = -t * 2;

                    segmentPositions[i] = new Vector(x, y, z);
                }

                // Create dragon body
                for (int i = 0; i < segments; i++) {
                    Vector segPos = segmentPositions[i];
                    Location segmentLoc = location.clone().add(segPos);

                    // Main body
                    createDragonSegment(segmentLoc, i, segments);

                    // Connect segments with water trail
                    if (i > 0) {
                        Vector prev = segmentPositions[i - 1];
                        createWaterConnection(location.clone().add(prev), segmentLoc);
                    }
                }
            }

            private void createDragonSegment(Location location, int segment, int totalSegments) {
                double segmentSize = 1.0 - ((double) segment / totalSegments * 0.5);
                int particles = segment == 0 ? 16 : 8; // More particles for head

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / particles) {
                    double x = Math.cos(angle) * segmentSize;
                    double y = Math.sin(angle) * segmentSize;

                    Location particleLoc = location.clone().add(x, y, 0);

                    // Dragon body particles
                    if (segment == 0) {
                        // Dragon head
                        world.spawnParticle(Particle.BUBBLE, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(32, 128, 255), 1.2f));
                    } else {
                        // Dragon body
                        world.spawnParticle(Particle.SPLASH, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(64, 164, 223), 1.0f));
                    }
                }
            }

            private void createWaterConnection(Location start, Location end) {
                Vector direction = end.toVector().subtract(start.toVector());
                double length = direction.length();
                direction.normalize();

                for (double d = 0; d < length; d += 0.5) {
                    Location connectionLoc = start.clone().add(direction.clone().multiply(d));

                    world.spawnParticle(Particle.DRIPPING_WATER, connectionLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, connectionLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(128, 200, 255), 0.8f));
                    }
                }
            }

            private void checkHits(Location location, Vector direction) {
                double hitRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on current phase
                        double damage = switch (phase) {
                            case 0 -> 6.0;  // Spiral ascent
                            case 1 -> 4.0;  // Weaving
                            case 2 -> 8.0;  // Diving attack
                            default -> 5.0; // Rising surge
                        };

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(entity);

                        // Hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SPLASH, hitLoc, 30, 0.3, 0.3, 0.3, 0.2);
                        world.spawnParticle(Particle.EXPLOSION, hitLoc, 5, 0.2, 0.2, 0.2, 0.05);

                        // Hit sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.5f);
                        world.playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);

                        // Dynamic knockback based on phase
                        Vector knockback = switch (phase) {
                            case 0 -> new Vector(0, 0.8, 0); // Upward
                            case 1 -> direction.clone().multiply(0.8); // Horizontal
                            case 2 -> direction.clone().multiply(1.2).setY(-0.2); // Downward
                            default -> direction.clone().multiply(0.6).setY(0.4); // Mixed
                        };

                        target.setVelocity(knockback);
                    }
                }
            }

            private void createFinalSurge() {
                // Final explosion of water
                Location finalLoc = player.getLocation();

                new BukkitRunnable() {
                    private int waves = 0;
                    private final int MAX_WAVES = 3;

                    @Override
                    public void run() {
                        if (waves >= MAX_WAVES) {
                            this.cancel();
                            return;
                        }

                        double radius = 3.0 * ((double) waves / MAX_WAVES + 1);

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location surgeLoc = finalLoc.clone().add(x, 0, z);

                            // Water surge effects
                            world.spawnParticle(Particle.SPLASH, surgeLoc, 5, 0.2, 0.4, 0.2, 0.1);
                            world.spawnParticle(Particle.DRIPPING_WATER, surgeLoc, 3, 0.2, 0.2, 0.2, 0.05);

                            if (waves == MAX_WAVES - 1) {
                                world.spawnParticle(Particle.EXPLOSION, surgeLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            }
                        }

                        // Wave sounds
                        world.playSound(finalLoc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f + (waves * 0.2f));

                        waves++;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 4L);
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, false, false));

        // Add cooldown
        addCooldown(player, "TenthForm", 16);
    }

    public static void addCooldown(Player player, String formName, int seconds) {
        // Implement cooldown logic here
        // This is a placeholder method; actual implementation may vary
        player.sendMessage("§6" + formName + " is now on cooldown for " + seconds + " seconds.");
    }

    /**
     * Eleventh Form: Dead Calm (拾壱ノ型 凪)
     * A still, silent form that unleashes instantaneous slashes with absolute precision
     */
    public static void useEleventhForm(Player player) {
        player.sendMessage("§b水 §3拾壱ノ型 凪 §b(Eleventh Form: Dead Calm)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial silent effect - very quiet water sounds
        world.playSound(startLoc, Sound.BLOCK_WATER_AMBIENT, 0.3f, 0.5f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger slashCount = new AtomicInteger(0);

        // Initial calm effect
        createCalmField(startLoc, world);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private Location lastLocation = startLoc.clone();
            private boolean isCalm = true;
            private final int MAX_SLASHES = 12;
            private List<Vector> slashDirections = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION || slashCount.get() >= MAX_SLASHES) {
                    createEndingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (isCalm) {
                    // Dead Calm state
                    maintainCalmState(currentLoc);

                    // Check for nearby entities to trigger slashes
                    checkForTargets(currentLoc);
                } else {
                    // Execute stored slashes
                    executeStoredSlashes(currentLoc);
                }

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void maintainCalmState(Location location) {
                // Create subtle water ripple effect
                double radius = 3.0;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location rippleLoc = location.clone().add(x, 0.1, z);

                    // Very subtle water particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DRIPPING_WATER, rippleLoc, 1, 0.05, 0, 0.05, 0);
                        world.spawnParticle(Particle.DUST, rippleLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(180, 220, 255), 0.7f));
                    }
                }
            }

            private void checkForTargets(Location location) {
                double detectionRadius = 5.0;

                for (Entity entity : world.getNearbyEntities(location, detectionRadius, detectionRadius, detectionRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        // Calculate direction to target
                        Vector toTarget = entity.getLocation().toVector().subtract(location.toVector()).normalize();

                        // Add slight variations for multiple slashes per target
                        for (int i = 0; i < 3; i++) {
                            Vector slashDir = toTarget.clone();
                            if (i > 0) {
                                // Add slight angle variations
                                slashDir.rotateAroundY(random.nextDouble() * 0.2 - 0.1);
                            }
                            slashDirections.add(slashDir);
                        }

                        if (!slashDirections.isEmpty()) {
                            isCalm = false; // Trigger slash execution
                        }
                    }
                }
            }

            private void executeStoredSlashes(Location location) {
                if (slashDirections.isEmpty()) {
                    isCalm = true;
                    return;
                }

                // Execute up to 3 slashes per tick
                for (int i = 0; i < Math.min(3, slashDirections.size()); i++) {
                    if (slashCount.get() >= MAX_SLASHES) break;

                    Vector slashDir = slashDirections.remove(0);
                    executeDeadCalmSlash(location, slashDir);
                    slashCount.incrementAndGet();
                }
            }

            private void executeDeadCalmSlash(Location location, Vector direction) {
                // Instantaneous movement
                player.setVelocity(direction.multiply(2.0));

                // Create slash effect
                double slashLength = 4.0;
                for (double d = 0; d < slashLength; d += 0.2) {
                    Location slashLoc = location.clone().add(direction.clone().multiply(d));

                    // Almost invisible water slice
                    world.spawnParticle(Particle.DRIPPING_WATER, slashLoc, 1, 0, 0, 0, 0.02);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(220, 240, 255), 0.5f));

                    // Delayed water trail
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            world.spawnParticle(Particle.SPLASH, slashLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        }
                    }.runTaskLater(Main.getPlugin(Main.class), 2L);
                }

                // Check for hits along the slash path
                for (Entity entity : world.getNearbyEntities(location, slashLength, 2.0, slashLength)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Precise damage
                        target.damage(12.0, player);
                        hitEntities.add(entity);

                        // Delayed hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Water burst effect
                                world.spawnParticle(Particle.SPLASH, hitLoc, 20, 0.2, 0.2, 0.2, 0.2);
                                world.spawnParticle(Particle.EXPLOSION, hitLoc, 3, 0.1, 0.1, 0.1, 0.05);

                                // Quiet hit sounds
                                world.playSound(hitLoc, Sound.ENTITY_PLAYER_SPLASH, 0.6f, 2.0f);
                                world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 1.5f);
                            }
                        }.runTaskLater(Main.getPlugin(Main.class), 3L);

                        // Minimal knockback
                        target.setVelocity(direction.clone().multiply(0.3));
                    }
                }

                // Extremely quiet slash sound
                world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, 0.3f, 2.0f);
            }

            private void createEndingEffect() {
                Location endLoc = player.getLocation();

                // Final calm ripple
                new BukkitRunnable() {
                    private double radius = 0.5;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location rippleLoc = endLoc.clone().add(x, 0.1, z);
                            world.spawnParticle(Particle.DRIPPING_WATER, rippleLoc, 1, 0.05, 0, 0.05, 0.02);
                        }

                        radius += 0.2;
                        ticks++;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply subtle effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, false, false));

        // Add cooldown
        addCooldown(player, "EleventhForm", 20);
    }

    private static void createCalmField(Location center, World world) {
        // Initial water field setup
        new BukkitRunnable() {
            private double radius = 0.5;
            private int ticks = 0;
            private final int MAX_TICKS = 10;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location fieldLoc = center.clone().add(x, 0.1, z);

                    // Subtle water field particles
                    world.spawnParticle(Particle.DRIPPING_WATER, fieldLoc, 1, 0.05, 0, 0.05, 0);
                    if (ticks == MAX_TICKS - 1) {
                        world.spawnParticle(Particle.DUST, fieldLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(200, 230, 255), 0.8f));
                    }
                }

                radius += 0.3;
                ticks++;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
    }

    /**
     * Ninth Form: Splashing Water Flow, Turbulent (玖ノ型 水流飛沫・乱)
     * Enables extremely agile movement with minimal landing time and surface contact
     */
    public static void useNinthForm(Player player) {
        player.sendMessage("§b水 §3玖ノ型 水流飛沫・乱 §b(Ninth Form: Splashing Water Flow, Turbulent)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial water sounds - subtle splash
        world.playSound(startLoc, Sound.ENTITY_PLAYER_SPLASH, 0.6f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_WATER_AMBIENT, 0.5f, 1.8f);

        Set<Entity> hitEntities = new HashSet<>();
        Map<Location, Long> recentStepLocations = new LinkedHashMap<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 8.0; // Longer duration for sustained agile movement
            private Location lastLocation = startLoc.clone();
            private Vector lastDirection = startLoc.getDirection();
            private boolean isInAir = false;
            private int comboCount = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinalSplash();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Check if player is in air or near ground
                boolean nearGround = isNearGround(currentLoc);

                // Execute movement and effects based on position
                if (nearGround) {
                    if (isInAir) {
                        // Landing effect
                        createLandingEffect(currentLoc);
                        isInAir = false;
                    }
                    executeGroundMovement(currentLoc, direction);
                } else {
                    isInAir = true;
                    executeAirMovement(currentLoc, direction);
                }

                // Track step locations for water trail
                updateStepLocations(currentLoc);

                // Create water effects
                createWaterEffects(currentLoc, direction);

                // Check for hits
                checkHits(currentLoc, direction);

                time += 0.05;
                lastLocation = currentLoc.clone();
                lastDirection = direction.clone();
            }

            private boolean isNearGround(Location location) {
                // Check for any solid block below
                Location below = location.clone().subtract(0, 0.1, 0);
                return !below.getBlock().isPassable();
            }

            private void executeGroundMovement(Location current, Vector direction) {
                // Calculate movement based on player's input
                Vector movement = direction.clone();

                // Add sideways motion based on strafe input
                if (player.isSneaking()) {
                    // Quick directional changes
                    movement.multiply(1.2).setY(0.1);
                } else {
                    // Normal movement with enhanced speed
                    movement.multiply(0.8).setY(0.2);
                }

                // Apply movement
                player.setVelocity(movement);

                // Create minimal contact effects
                createStepEffect(current);
            }

            private void executeAirMovement(Location current, Vector direction) {
                // More controlled air movement
                Vector movement = direction.clone();

                // Allow slight direction changes mid-air
                if (player.isSneaking()) {
                    movement.multiply(0.6);
                } else {
                    movement.multiply(0.8);
                }

                // Maintain some upward momentum
                movement.setY(Math.max(-0.4, movement.getY()));

                player.setVelocity(movement);

                // Create air trail effects
                createAirTrailEffect(current);
            }

            private void createStepEffect(Location location) {
                // Minimal water splash on step
                world.spawnParticle(Particle.SPLASH, location, 3, 0.1, 0, 0.1, 0.02);

                // Subtle ripple effect
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                    double radius = 0.4;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location rippleLoc = location.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.DRIPPING_WATER, rippleLoc, 1, 0.05, 0, 0.05, 0.01);
                }

                // Very quiet step sound
                if (random.nextFloat() < 0.3) {
                    world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, 0.2f, 2.0f);
                }
            }

            private void createAirTrailEffect(Location location) {
                // Water particles trailing in air
                world.spawnParticle(Particle.DRIPPING_WATER, location, 2, 0.1, 0.1, 0.1, 0.02);

                // Dust particles for water visualization
                world.spawnParticle(Particle.DUST, location, 1, 0.1, 0.1, 0.1,
                        new Particle.DustOptions(Color.fromRGB(130, 202, 255), 0.8f));
            }

            private void createLandingEffect(Location location) {
                // Minimal splash on landing
                world.spawnParticle(Particle.SPLASH, location, 8, 0.2, 0, 0.2, 0.05);
                world.spawnParticle(Particle.DRIPPING_WATER, location, 12, 0.3, 0.1, 0.3, 0.02);

                // Ripple effect
                new BukkitRunnable() {
                    private double radius = 0.5;
                    private int ticks = 0;
                    private final int MAX_TICKS = 5;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location rippleLoc = location.clone().add(x, 0.1, z);
                            world.spawnParticle(Particle.DRIPPING_WATER, rippleLoc, 1, 0.05, 0, 0.05, 0.01);
                        }

                        radius += 0.2;
                        ticks++;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

                // Subtle landing sound
                world.playSound(location, Sound.ENTITY_PLAYER_SPLASH, 0.4f, 1.8f);
            }

            private void updateStepLocations(Location current) {
                // Add current location to recent steps
                recentStepLocations.put(current.clone(), System.currentTimeMillis());

                // Remove old step locations (older than 1 second)
                recentStepLocations.entrySet().removeIf(entry ->
                        System.currentTimeMillis() - entry.getValue() > 1000);
            }

            private void createWaterEffects(Location current, Vector direction) {
                // Create water trail between recent step locations
                Location previousLoc = null;
                for (Location stepLoc : recentStepLocations.keySet()) {
                    if (previousLoc != null) {
                        createWaterConnection(previousLoc, stepLoc);
                    }
                    previousLoc = stepLoc;
                }
            }

            private void createWaterConnection(Location start, Location end) {
                Vector between = end.toVector().subtract(start.toVector());
                double length = between.length();
                between.normalize();

                for (double d = 0; d < length; d += 0.5) {
                    Location connectionLoc = start.clone().add(between.clone().multiply(d));

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DRIPPING_WATER, connectionLoc, 1, 0.05, 0.05, 0.05, 0.01);
                    }
                }
            }

            private void checkHits(Location location, Vector direction) {
                double hitRadius = 2.0;

                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Quick successive hits
                        target.damage(4.0, player);
                        hitEntities.add(entity);

                        // Hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SPLASH, hitLoc, 15, 0.2, 0.2, 0.2, 0.1);

                        // Subtle hit sounds
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_SPLASH, 0.5f, 1.8f);

                        // Light knockback
                        Vector knockback = direction.clone().multiply(0.4).setY(0.2);
                        target.setVelocity(knockback);

                        // Increment combo counter
                        comboCount++;

                        // Additional effects for combo hits
                        if (comboCount >= 3) {
                            world.spawnParticle(Particle.SPLASH, hitLoc, 25, 0.3, 0.3, 0.3, 0.15);
                            world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                            comboCount = 0;
                        }
                    }
                }
            }

            private void createFinalSplash() {
                Location endLoc = player.getLocation();

                new BukkitRunnable() {
                    private double radius = 0.5;
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location splashLoc = endLoc.clone().add(x, 0.1, z);
                            world.spawnParticle(Particle.SPLASH, splashLoc, 2, 0.1, 0, 0.1, 0.05);
                            world.spawnParticle(Particle.DRIPPING_WATER, splashLoc, 1, 0.05, 0, 0.05, 0.02);
                        }

                        radius += 0.3;
                        ticks++;
                    }
                }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
            }

        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);

        // Apply movement enhancing effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 160, 1, false, false));

        // Add cooldown
        addCooldown(player, "NinthForm", 14);
    }

}
