package org.skyforce.demon.breathings.thunderbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThunderBreathingAbility {
    private final JavaPlugin plugin;

    public ThunderBreathingAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Erste Form: Thunderclap and Flash
    // Hilfsmethode: Nächstes Entity in Blickrichtung finden
    private LivingEntity findTarget(Player player, double maxDistance) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        World world = player.getWorld();
        LivingEntity closest = null;
        double closestDist = maxDistance;
        for (Entity e : world.getNearbyEntities(eye, maxDistance, maxDistance, maxDistance)) {
            if (!(e instanceof LivingEntity) || e == player) continue;
            Vector toEntity = e.getLocation().toVector().subtract(eye.toVector());
            double dot = toEntity.normalize().dot(dir);
            double dist = eye.distance(e.getLocation());
            if (dot > 0.98 && dist < closestDist && player.hasLineOfSight(e)) { // fast exakt in Blickrichtung und Sichtlinie
                closest = (LivingEntity) e;
                closestDist = dist;
            }
        }
        return closest;
    }

    // Neue Thunderclap and Flash: gezielt auf Entity
    public void thunderclapAndFlash(Player player) {
        double maxDistance = 15.0;
        LivingEntity target = findTarget(player, maxDistance);
        if (target != null) {
            Location targetLoc = target.getLocation().clone().add(target.getLocation().getDirection().multiply(-1));
            // Teleportiere Spieler direkt vor das Ziel
            player.teleport(targetLoc.setDirection(player.getLocation().getDirection()));
            // Sound und Partikel
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 30, 0.3, 0.3, 0.3, 0.15);
            // Blitz und Schaden
            player.getWorld().strikeLightningEffect(target.getLocation());
            target.damage(10.0, player); // 10 Schaden, anpassbar
            // Katana zurückstecken
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
        } else {
            // Kein Ziel: normaler Dash nach vorne
            dashForward(player, 10.0, 5);
        }
    }

    // Dash-Backup (wenn kein Ziel)
    private void dashForward(Player player, double dashDistance, int dashTicks) {
        Vector direction = player.getLocation().getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= dashTicks) {
                    this.cancel();
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
                    return;
                }
                player.setVelocity(direction.multiply(dashDistance / dashTicks));
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 20, 0.2, 0.2, 0.2, 0.1);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Sixfold
    public void sixfold(Player player) {
        int repeats = 6;
        long delay = 7L; // Ticks zwischen den Dashes
        runMultiThunderclap(player, repeats, delay);
    }

    // Eightfold
    public void eightfold(Player player) {
        int repeats = 8;
        long delay = 7L;
        runMultiThunderclap(player, repeats, delay);
    }

    // Hilfsmethode für mehrfachen Thunderclap
    private void runMultiThunderclap(Player player, int repeats, long delay) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= repeats) {
                    this.cancel();
                    return;
                }
                thunderclapAndFlash(player);
                count++;
            }
        }.runTaskTimer(plugin, 0L, delay);
    }

    // Neue Godspeed: Mehrfach gezielt auf Entities in Blickrichtung, bei jedem Treffer Blitz
    public void godspeed(Player player) {
        int repeats = 3; // Godspeed: 3 extrem schnelle Angriffe
        double maxDistance = 18.0;
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= repeats) {
                    this.cancel();
                    // Erschöpfung: Slowness für 4 Sekunden
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 80, 2));
                    return;
                }
                LivingEntity target = findTarget(player, maxDistance);
                if (target != null) {
                    Location targetLoc = target.getLocation().clone().add(target.getLocation().getDirection().multiply(-1));
                    player.teleport(targetLoc.setDirection(player.getLocation().getDirection()));
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), 40, 0.4, 0.4, 0.4, 0.2);
                    player.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(14.0, player); // Mehr Schaden bei Godspeed
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
                } else {
                    dashForward(player, 16.0, 2);
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 4L); // Sehr kurze Pause zwischen den Angriffen
    }

    Random random = new Random();

    /**
     * Second Form: Rice Spirit (弐ノ型 稲魂)
     * Creates five consecutive lightning-imbued slashes in rapid succession
     */
    public void useSecondForm(Player player) {
        player.sendMessage("§e雷 §b弐ノ型 稲魂 §e(Second Form: Rice Spirit)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder sounds
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        world.strikeLightningEffect(startLoc);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger slashCount = new AtomicInteger(0);
        final int TOTAL_SLASHES = 5;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private Location lastLocation = startLoc.clone();
            private double currentArc = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION || slashCount.get() >= TOTAL_SLASHES) {
                    createFinalEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Execute slash every 0.25 seconds
                if (time % 0.25 < 0.05 && slashCount.get() < TOTAL_SLASHES) {
                    executeThunderSlash(currentLoc, direction);
                    slashCount.incrementAndGet();
                }

                // Create continuous effects
                createLightningEffects(currentLoc);

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void executeThunderSlash(Location location, Vector direction) {
                // Calculate arc angle for each slash
                currentArc = (slashCount.get() - 2) * (Math.PI / 8); // Spread slashes in an arc

                // Rotate slash direction
                Vector slashDir = direction.clone().rotateAroundY(currentArc);

                // Apply movement
                player.setVelocity(slashDir.multiply(1.2));

                // Create thunder slash effect
                createThunderSlash(location, slashDir);

                // Thunder sound
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

                // Check for hits
                checkSlashHits(location, slashDir);
            }

            private void createThunderSlash(Location location, Vector direction) {
                double slashLength = 4.0;
                List<Location> slashPoints = new ArrayList<>();

                // Create main slash path
                for (double d = 0; d < slashLength; d += 0.2) {
                    Vector offset = direction.clone().multiply(d);
                    Location slashLoc = location.clone().add(offset);
                    slashPoints.add(slashLoc);

                    // Main slash particles
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, slashLoc, 3, 0.1, 0.1, 0.1, 0.05);
                }

                // Create lightning effects along slash path
                for (Location point : slashPoints) {
                    // Lightning branch effect
                    if (random.nextFloat() < 0.3) {
                        createLightningBranch(point);
                    }

                    // Thunder particles
                    world.spawnParticle(Particle.WAX_OFF, point, 2, 0.1, 0.1, 0.1, 0.05);

                    // Color particles for thunder visualization
                    world.spawnParticle(Particle.DUST, point, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 100), 1.0f));
                }
            }

            private void createLightningBranch(Location start) {
                Vector branchDir = new Vector(
                        random.nextDouble() - 0.5,
                        random.nextDouble() - 0.5,
                        random.nextDouble() - 0.5
                ).normalize();

                double branchLength = 1.0;
                for (double d = 0; d < branchLength; d += 0.2) {
                    Location branchLoc = start.clone().add(branchDir.clone().multiply(d));

                    // Lightning particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, branchLoc, 1, 0.05, 0.05, 0.05, 0);

                    // Thunder visual
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, branchLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 200), 0.8f));
                    }
                }
            }

            private void createLightningEffects(Location location) {
                // Continuous thunder aura
                double radius = 1.0;
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location effectLoc = location.clone().add(x, 1, z);

                    // Electric particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, effectLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }

                    // Thunder visual
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.DUST, effectLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 150), 1.0f));
                    }
                }
            }

            private void checkSlashHits(Location location, Vector direction) {
                double hitRadius = 2.5;
                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(6.0, player);
                        hitEntities.add(entity);

                        // Hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        createThunderHitEffect(hitLoc);

                        // Thunder sounds
                        world.playSound(hitLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6f, 1.8f);
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.2f);

                        // Knockback
                        Vector kb = direction.clone().multiply(0.8).setY(0.2);
                        target.setVelocity(kb);
                    }
                }
            }

            private void createThunderHitEffect(Location location) {
                // Lightning strike effect
                world.strikeLightningEffect(location);

                // Explosion of thunder particles
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 20, 0.3, 0.3, 0.3, 0.1);
                world.spawnParticle(Particle.WAX_OFF, location, 15, 0.2, 0.2, 0.2, 0.1);

                // Thunder visual burst
                for (int i = 0; i < 8; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 2; d += 0.2) {
                        Location burstLoc = location.clone().add(direction.clone().multiply(d));
                        world.spawnParticle(Particle.DUST, burstLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 150), 1.0f));
                    }
                }
            }

            private void createFinalEffect() {
                Location endLoc = player.getLocation();

                // Final thunder strike
                world.strikeLightningEffect(endLoc);

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

                        // Expanding thunder ring
                        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;

                            Location ringLoc = endLoc.clone().add(x, 0.1, z);

                            // Thunder particles
                            world.spawnParticle(Particle.ELECTRIC_SPARK, ringLoc, 1, 0.1, 0.1, 0.1, 0.02);
                            world.spawnParticle(Particle.DUST, ringLoc, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(255, 255, 200), 0.8f));
                        }

                        // Thunder sounds
                        if (ticks % 2 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 1.5f + (ticks / (float)MAX_TICKS));
                        }

                        radius += 0.3;
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));

        // Add cooldown
        //addCooldown(player, "SecondForm", 12);
    }
    /**
     * Third Form: Thunder Swarm (参ノ型 聚蚊成雷)
     * Creates a swarm of lightning arcs that surround and strike the target from multiple angles
     */
    public void useThirdForm(Player player) {
        player.sendMessage("§e雷 §b参ノ型 聚蚊成雷 §e(Third Form: Thunder Swarm)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder effect
        world.strikeLightningEffect(startLoc);
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        Map<Entity, Integer> hitCounts = new HashMap<>();
        final int MAX_HITS_PER_TARGET = 8;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 4.0;
            private List<ThunderArc> activeArcs = new ArrayList<>();
            private List<Location> swarmPoints = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Update and create swarm points
                updateSwarmPoints(currentLoc);

                // Generate new thunder arcs
                generateThunderArcs();

                // Update existing arcs
                updateThunderArcs();

                // Check for targets and execute strikes
                checkAndStrikeTargets(currentLoc);

                time += 0.05;
            }

            private void updateSwarmPoints(Location center) {
                // Clear old points periodically
                if (time % 0.5 < 0.05) {
                    swarmPoints.clear();
                }

                // Generate new swarm points in a sphere around the player
                if (random.nextFloat() < 0.3) {
                    double radius = 4.0;
                    double angle1 = random.nextDouble() * Math.PI * 2;
                    double angle2 = random.nextDouble() * Math.PI;

                    double x = radius * Math.sin(angle2) * Math.cos(angle1);
                    double y = radius * Math.sin(angle2) * Math.sin(angle1) + 1.5;
                    double z = radius * Math.cos(angle2);

                    Location swarmPoint = center.clone().add(x, y, z);
                    swarmPoints.add(swarmPoint);

                    // Create swarm point effect
                    createSwarmPointEffect(swarmPoint);
                }
            }

            private void createSwarmPointEffect(Location location) {
                // Electric particle effect
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 5, 0.1, 0.1, 0.1, 0.05);

                // Thunder visual
                world.spawnParticle(Particle.DUST, location, 3, 0.1, 0.1, 0.1,
                        new Particle.DustOptions(Color.fromRGB(255, 255, 150), 1.0f));

                // Occasional thunder sound
                if (random.nextFloat() < 0.1) {
                    world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 2.0f);
                }
            }

            private void generateThunderArcs() {
                if (swarmPoints.size() < 2) return;

                // Create new arcs between swarm points
                if (random.nextFloat() < 0.2) {
                    Location start = swarmPoints.get(random.nextInt(swarmPoints.size()));
                    Location end = swarmPoints.get(random.nextInt(swarmPoints.size()));

                    if (!start.equals(end)) {
                        ThunderArc arc = new ThunderArc(start.clone(), end.clone(), 0.5);
                        activeArcs.add(arc);
                    }
                }
            }

            private void updateThunderArcs() {
                Iterator<ThunderArc> iterator = activeArcs.iterator();
                while (iterator.hasNext()) {
                    ThunderArc arc = iterator.next();
                    arc.lifetime -= 0.05;

                    if (arc.lifetime <= 0) {
                        iterator.remove();
                        continue;
                    }

                    // Draw thunder arc
                    arc.render();
                }
            }

            private void checkAndStrikeTargets(Location center) {
                double detectionRadius = 5.0;

                for (Entity entity : world.getNearbyEntities(center, detectionRadius, detectionRadius, detectionRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Check hit count
                        int hits = hitCounts.getOrDefault(target, 0);
                        if (hits >= MAX_HITS_PER_TARGET) continue;

                        // Calculate strike chance based on proximity to swarm points
                        for (Location swarmPoint : swarmPoints) {
                            if (swarmPoint.distance(target.getLocation()) < 2.0 && random.nextFloat() < 0.3) {
                                executeThunderStrike(target, swarmPoint);
                                hitCounts.put(target, hits + 1);
                                break;
                            }
                        }
                    }
                }
            }

            private void executeThunderStrike(LivingEntity target, Location strikePoint) {
                Location targetLoc = target.getLocation().add(0, 1, 0);

                // Create lightning strike effect
                ThunderArc strikeArc = new ThunderArc(strikePoint, targetLoc, 0.3);
                strikeArc.render();

                // Apply damage
                target.damage(4.0, player);

                // Strike effects
                createStrikeEffect(targetLoc);

                // Knockback
                Vector kb = targetLoc.toVector().subtract(strikePoint.toVector()).normalize();
                kb.multiply(0.5).setY(0.2);
                target.setVelocity(target.getVelocity().add(kb));
            }

            private void createStrikeEffect(Location location) {
                // Thunder particles
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 15, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.WAX_OFF, location, 10, 0.2, 0.2, 0.2, 0.1);

                // Thunder visual
                for (int i = 0; i < 4; i++) {
                    Vector dir = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 1.5; d += 0.2) {
                        Location sparkLoc = location.clone().add(dir.clone().multiply(d));
                        world.spawnParticle(Particle.DUST, sparkLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 150), 0.8f));
                    }
                }

                // Thunder sounds
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.4f, 1.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final thunder burst
                for (Location point : swarmPoints) {
                    ThunderArc finalArc = new ThunderArc(endLoc, point, 0.5);
                    finalArc.render();
                }

                // Final strike effect
                world.strikeLightningEffect(endLoc);
                world.playSound(endLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
            }

            private class ThunderArc {
                private Location start;
                private Location end;
                private double lifetime;
                private List<Vector> arcPoints;

                public ThunderArc(Location start, Location end, double lifetime) {
                    this.start = start;
                    this.end = end;
                    this.lifetime = lifetime;
                    this.arcPoints = generateArcPoints();
                }

                private List<Vector> generateArcPoints() {
                    List<Vector> points = new ArrayList<>();
                    Vector direction = end.toVector().subtract(start.toVector());
                    double length = direction.length();
                    direction.normalize();

                    int segments = (int)(length * 2);
                    Vector perpendicular = direction.clone().rotateAroundY(Math.PI / 2);

                    points.add(start.toVector());
                    for (int i = 1; i < segments; i++) {
                        double t = i / (double)segments;
                        Vector pos = start.toVector().add(direction.clone().multiply(length * t));

                        // Add randomness
                        double offset = (random.nextDouble() - 0.5) * 0.5 * Math.sin(t * Math.PI);
                        pos.add(perpendicular.clone().multiply(offset));

                        points.add(pos);
                    }
                    points.add(end.toVector());

                    return points;
                }

                public void render() {
                    // Draw main arc
                    for (int i = 0; i < arcPoints.size() - 1; i++) {
                        Vector current = arcPoints.get(i);
                        Vector next = arcPoints.get(i + 1);
                        Vector between = next.clone().subtract(current).multiply(0.2);

                        for (double d = 0; d < 1; d += 0.2) {
                            Location arcLoc = current.toLocation(world).add(between.clone().multiply(d));

                            // Thunder particles
                            world.spawnParticle(Particle.ELECTRIC_SPARK, arcLoc, 1, 0.05, 0.05, 0.05, 0);

                            // Thunder visual
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.DUST, arcLoc, 1, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(255, 255, 200), 0.8f));
                            }
                        }
                    }

                    // Arc sound
                    if (random.nextFloat() < 0.1) {
                        world.playSound(start, Sound.BLOCK_BEACON_AMBIENT, 0.3f, 2.0f);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1, false, false));

        // Add cooldown
      //  addCooldown(player, "ThirdForm", 16);
    }

    /**
     * Fourth Form: Distant Thunder (肆ノ型 遠雷)
     * Creates an orb of lightning that releases multi-directional thunder strikes
     */
    public void useFourthForm(Player player) {
        player.sendMessage("§e雷 §b肆ノ型 遠雷 §e(Fourth Form: Distant Thunder)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder sound
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.8f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicReference<Location> orbLocation = new AtomicReference<>(startLoc.clone());
        AtomicBoolean isOrbLaunched = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 6.0;
            private final double LAUNCH_TIME = 1.0;
            private Vector orbVelocity;
            private List<ThunderBolt> activeBolts = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (!isOrbLaunched.get()) {
                    // Charge up phase
                    chargeUpOrb(currentLoc);

                    if (time >= LAUNCH_TIME) {
                        launchOrb(currentLoc);
                        isOrbLaunched.set(true);
                    }
                } else {
                    // Update orb position
                    updateOrb();

                    // Generate thunder bolts
                    generateThunderBolts();
                }

                // Update active thunder bolts
                updateThunderBolts();

                time += 0.05;
            }

            private void chargeUpOrb(Location location) {
                double chargeProgress = time / LAUNCH_TIME;

                // Orb formation effect
                createOrbEffect(location, chargeProgress);

                // Charging sounds
                if (random.nextFloat() < 0.2) {
         //           world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.5f + chargeProgress, 1.0f + chargeProgress);
                }
            }

            private void createOrbEffect(Location location, double progress) {
                double radius = 0.5 * progress;

                // Core particles
                for (int i = 0; i < 8; i++) {
                    Vector offset = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(radius);

                    Location particleLoc = location.clone().add(offset).add(0, 1.5, 0);

                    // Electric core
                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);

                    // Thunder visual
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 150), 1.0f));
                }

                // Orbital particles
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location orbitalLoc = location.clone().add(x, 1.5, z);
                    world.spawnParticle(Particle.WAX_OFF, orbitalLoc, 1, 0.05, 0.05, 0.05, 0);
                }
            }

            private void launchOrb(Location location) {
                // Set initial orb velocity
                orbVelocity = location.getDirection().multiply(1.2);
                orbLocation.set(location.clone().add(0, 1.5, 0));

                // Launch effect
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
                world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
            }

            private void updateOrb() {
                Location currentOrbLoc = orbLocation.get();

                // Update position
                currentOrbLoc.add(orbVelocity);

                // Check for collision
                if (!currentOrbLoc.getBlock().isPassable()) {
                    orbVelocity.multiply(-0.8); // Bounce with reduced velocity
                }

                // Apply slight gravity
                orbVelocity.setY(orbVelocity.getY() - 0.03);

                // Create orb visuals
                createOrbVisuals(currentOrbLoc);

                orbLocation.set(currentOrbLoc);
            }

            private void createOrbVisuals(Location location) {
                // Core effect
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 5, 0.2, 0.2, 0.2, 0.1);

                // Electric field
                for (int i = 0; i < 4; i++) {
                    Vector offset = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location fieldLoc = location.clone().add(offset);
                    world.spawnParticle(Particle.DUST, fieldLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.0f));
                }

                // Ambient sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.3f, 2.0f);
                }
            }

            private void generateThunderBolts() {
                if (random.nextFloat() < 0.2) {
                    // Generate random direction for bolt
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(8.0); // Maximum bolt length

                    ThunderBolt bolt = new ThunderBolt(
                            orbLocation.get().clone(),
                            orbLocation.get().clone().add(direction),
                            0.3 // Lifetime in seconds
                    );

                    activeBolts.add(bolt);

                    // Thunder sound
                    world.playSound(orbLocation.get(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.4f, 1.5f);
                }
            }

            private void updateThunderBolts() {
                Iterator<ThunderBolt> iterator = activeBolts.iterator();
                while (iterator.hasNext()) {
                    ThunderBolt bolt = iterator.next();
                    bolt.lifetime -= 0.05;

                    if (bolt.lifetime <= 0) {
                        iterator.remove();
                        continue;
                    }

                    // Render bolt
                    bolt.render();

                    // Check for hits
                    checkBoltHits(bolt);
                }
            }

            private void checkBoltHits(ThunderBolt bolt) {
                Vector direction = bolt.end.toVector().subtract(bolt.start.toVector());
                double length = direction.length();
                direction.normalize();

                for (double d = 0; d < length; d += 1.0) {
                    Location checkLoc = bolt.start.clone().add(direction.clone().multiply(d));

                    for (Entity entity : world.getNearbyEntities(checkLoc, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply damage
                            target.damage(7.0, player);
                            hitEntities.add(entity);

                            // Hit effects
                            createHitEffect(target.getLocation().add(0, 1, 0));

                            // Knockback in bolt direction
                            Vector kb = direction.clone().multiply(1.2).setY(0.4);
                            target.setVelocity(kb);
                        }
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Strike effect
                world.strikeLightningEffect(location);

                // Particles
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 20, 0.3, 0.3, 0.3, 0.2);
                world.spawnParticle(Particle.WAX_OFF, location, 15, 0.2, 0.2, 0.2, 0.1);

                // Thunder sounds
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8f, 1.2f);
            }

            private void createFinishingEffect() {
                Location finalLoc = orbLocation.get();

                // Final explosion
                world.spawnParticle(Particle.EXPLOSION, finalLoc, 1, 0, 0, 0, 0);
                world.playSound(finalLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                // Thunder storm effect
                for (int i = 0; i < 8; i++) {
                    Location strikeLoc = finalLoc.clone().add(
                            random.nextDouble() * 4 - 2,
                            0,
                            random.nextDouble() * 4 - 2
                    );

                    world.strikeLightningEffect(strikeLoc);
                }
            }

            private class ThunderBolt {
                private Location start;
                private Location end;
                private double lifetime;
                private List<Vector> boltPoints;

                public ThunderBolt(Location start, Location end, double lifetime) {
                    this.start = start;
                    this.end = end;
                    this.lifetime = lifetime;
                    this.boltPoints = generateBoltPoints();
                }

                private List<Vector> generateBoltPoints() {
                    List<Vector> points = new ArrayList<>();
                    Vector direction = end.toVector().subtract(start.toVector());
                    double length = direction.length();
                    direction.normalize();

                    int segments = (int)(length * 2);
                    Vector up = new Vector(0, 1, 0);
                    Vector perpendicular = direction.clone().crossProduct(up);

                    points.add(start.toVector());
                    for (int i = 1; i < segments; i++) {
                        double t = i / (double)segments;
                        Vector pos = start.toVector().add(direction.clone().multiply(length * t));

                        // Add randomness
                        double offsetX = (random.nextDouble() - 0.5) * 0.8;
                        double offsetY = (random.nextDouble() - 0.5) * 0.8;
                        pos.add(perpendicular.clone().multiply(offsetX));
                        pos.add(up.clone().multiply(offsetY));

                        points.add(pos);
                    }
                    points.add(end.toVector());

                    return points;
                }

                public void render() {
                    // Draw main bolt
                    for (int i = 0; i < boltPoints.size() - 1; i++) {
                        Vector current = boltPoints.get(i);
                        Vector next = boltPoints.get(i + 1);
                        Vector between = next.clone().subtract(current).multiply(0.2);

                        for (double d = 0; d < 1; d += 0.2) {
                            Location boltLoc = current.toLocation(world).add(between.clone().multiply(d));

                            // Thunder particles
                            world.spawnParticle(Particle.ELECTRIC_SPARK, boltLoc, 1, 0.05, 0.05, 0.05, 0);

                            // Thunder visual
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.DUST, boltLoc, 1, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(255, 255, 200), 0.8f));
                            }
                        }
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1, false, false));

        // Add cooldown
      //  addCooldown(player, "FourthForm", 18);
    }

    /**
     * Fifth Form: Heat Lightning (伍ノ型 熱界雷)
     * A powerful upward slash that combines heat and lightning in a long-range attack
     */
    public void useFifthForm(Player player) {
        player.sendMessage("§e雷 §b伍ノ型 熱界雷 §e(Fifth Form: Heat Lightning)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder effect
        world.strikeLightningEffect(startLoc);
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 1.6f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private Location lastLocation = startLoc.clone();
            private final double SLASH_HEIGHT = 8.0; // Maximum height of the slash
            private boolean hasExecutedMainSlash = false;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                if (!hasExecutedMainSlash && time >= 0.2) {
                    // Execute main upward slash
                    executeMainSlash(currentLoc, direction);
                    hasExecutedMainSlash = true;
                }

                // Create continuous effects
                createSlashEffects(currentLoc, direction);

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void executeMainSlash(Location location, Vector direction) {
                // Initial upward momentum
                player.setVelocity(new Vector(0, 1.2, 0).add(direction.multiply(0.8)));

                // Create the main slash effect
                createUpwardSlash(location, direction);

                // Thunder sound
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
            }

            private void createUpwardSlash(Location start, Vector direction) {
                List<Location> slashPoints = new ArrayList<>();
                Vector upward = new Vector(0, 1, 0);
                Vector slashDir = direction.clone().add(upward).normalize();

                // Generate slash path
                for (double h = 0; h < SLASH_HEIGHT; h += 0.5) {
                    Location point = start.clone().add(slashDir.clone().multiply(h));
                    slashPoints.add(point);

                    // Add slight arc to the path
                    double arc = Math.sin(h / SLASH_HEIGHT * Math.PI) * 2;
                    point.add(direction.clone().multiply(arc));
                }

                // Create the lightning effect along the path
                new BukkitRunnable() {
                    private int index = 0;

                    @Override
                    public void run() {
                        if (index >= slashPoints.size()) {
                            this.cancel();
                            return;
                        }

                        Location point = slashPoints.get(index);
                        createHeatLightningEffect(point, index / (double)slashPoints.size());

                        // Check for hits at current point
                        checkSlashHits(point, slashDir);

                        index++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void createHeatLightningEffect(Location location, double progress) {
                // Core lightning effect
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 5, 0.2, 0.2, 0.2, 0.1);

                // Heat effect (red/yellow particles)
                world.spawnParticle(Particle.DUST, location, 3, 0.2, 0.2, 0.2,
                        new Particle.DustOptions(Color.fromRGB(255, 200, 50), 1.0f));

                // Create branching lightning
                if (random.nextFloat() < 0.3) {
                    createLightningBranch(location, progress);
                }

                // Thunder and heat sounds
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.5f);
                    world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 2.0f);
                }
            }

            private void createLightningBranch(Location start, double progress) {
                Vector direction = new Vector(
                        random.nextDouble() - 0.5,
                        random.nextDouble() * 0.5, // Mostly upward
                        random.nextDouble() - 0.5
                ).normalize().multiply(2.0);

                for (double d = 0; d < 2.0; d += 0.2) {
                    Location branchLoc = start.clone().add(direction.clone().multiply(d));

                    // Lightning particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, branchLoc, 1, 0.05, 0.05, 0.05, 0);

                    // Heat particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, branchLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 150, 50), 0.8f));
                    }

                    // Add some randomness to the branch direction
                    direction.rotateAroundAxis(new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize(), random.nextDouble() * 0.5);
                }
            }

            private void createSlashEffects(Location location, Vector direction) {
                if (!hasExecutedMainSlash) {
                    // Charge-up effects
                    double radius = 1.0;
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location effectLoc = location.clone().add(x, 0.1, z);

                        // Pre-slash particles
                        world.spawnParticle(Particle.ELECTRIC_SPARK, effectLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, effectLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 200, 50), 0.8f));
                    }
                } else {
                    // Trail effects during slash
                    Location trailLoc = location.clone().add(0, 1, 0);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, trailLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }
            }

            private void checkSlashHits(Location location, Vector direction) {
                double hitRadius = 2.5;
                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(10.0, player);
                        hitEntities.add(entity);

                        // Apply fire effect
                        target.setFireTicks(60);

                        // Create hit effects
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Strong upward knockback
                        Vector knockback = direction.clone().multiply(1.5).setY(1.2);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Thunder strike
                world.strikeLightningEffect(location);

                // Explosion of particles
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 20, 0.3, 0.3, 0.3, 0.2);

                // Heat effects
                world.spawnParticle(Particle.LAVA, location, 5, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.DUST, location, 15, 0.3, 0.3, 0.3,
                        new Particle.DustOptions(Color.fromRGB(255, 150, 50), 1.2f));

                // Impact sounds
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final lightning pillar
                for (double y = 0; y < 8; y += 0.5) {
                    Location pillarLoc = endLoc.clone().add(0, y, 0);

                    // Lightning particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, pillarLoc, 3, 0.2, 0, 0.2, 0.05);

                    // Heat particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, pillarLoc, 2, 0.2, 0, 0.2,
                                new Particle.DustOptions(Color.fromRGB(255, 180, 50), 1.0f));
                    }
                }

                // Final thunder
                world.strikeLightningEffect(endLoc);
                world.playSound(endLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.0f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));

        // Add cooldown
       // addCooldown(player, "FifthForm", 14);
    }
    /**
     * Sixth Form: Rumble and Flash (陸ノ型 電轟雷轟)
     * Releases multiple powerful lightning strikes from range
     */
    public void useSixthForm(Player player) {
        player.sendMessage("§e雷 §b陸ノ型 電轟雷轟 §e(Sixth Form: Rumble and Flash)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial thunder effect
        world.strikeLightningEffect(startLoc);
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.4f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger strikeCount = new AtomicInteger(0);
        final int MAX_STRIKES = 6;

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 5.0;
            private List<LightningCharge> activeCharges = new ArrayList<>();
            private Map<Location, Double> targetedLocations = new HashMap<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION || strikeCount.get() >= MAX_STRIKES) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Generate new lightning charges
                if (time % 0.75 < 0.05 && strikeCount.get() < MAX_STRIKES) {
                    generateLightningCharge(currentLoc, direction);
                    strikeCount.incrementAndGet();
                }

                // Update and render active charges
                updateLightningCharges();

                // Update targeted locations
                updateTargetedLocations();

                time += 0.05;
            }

            private void generateLightningCharge(Location location, Vector direction) {
                // Calculate target location
                Location targetLoc = getTargetLocation(location, direction);
                if (targetLoc != null) {
                    LightningCharge charge = new LightningCharge(location.clone().add(0, 1.5, 0), targetLoc);
                    activeCharges.add(charge);

                    // Add to targeted locations
                    targetedLocations.put(targetLoc, 1.0); // 1.0 second warning time

                    // Charging sound
                    world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);
                }
            }

            private Location getTargetLocation(Location start, Vector direction) {
                Vector adjustedDir = direction.clone().setY(direction.getY() * 0.2); // Reduce vertical targeting
                RayTraceResult result = world.rayTraceBlocks(
                        start,
                        adjustedDir,
                        30, // Maximum range
                        FluidCollisionMode.NEVER,
                        true
                );

                if (result != null && result.getHitBlock() != null) {
                    return result.getHitPosition().toLocation(world);
                } else {
                    return start.clone().add(adjustedDir.multiply(30));
                }
            }

            private void updateLightningCharges() {
                Iterator<LightningCharge> iterator = activeCharges.iterator();
                while (iterator.hasNext()) {
                    LightningCharge charge = iterator.next();
                    if (charge.update()) {
                        // Execute lightning strike when charge reaches target
                        executeLightningStrike(charge.targetLocation);
                        iterator.remove();
                    }
                }
            }

            private void updateTargetedLocations() {
                Iterator<Map.Entry<Location, Double>> iterator = targetedLocations.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Location, Double> entry = iterator.next();
                    double timeLeft = entry.getValue() - 0.05;

                    if (timeLeft <= 0) {
                        iterator.remove();
                    } else {
                        entry.setValue(timeLeft);
                        createTargetWarning(entry.getKey(), timeLeft);
                    }
                }
            }

            private void createTargetWarning(Location location, double timeLeft) {
                double radius = 2.0 * (1.0 - timeLeft);

                // Warning circle
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location warnLoc = location.clone().add(x, 0.1, z);

                    // Electric warning particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, warnLoc, 1, 0.1, 0, 0.1, 0);

                    // Warning circle color (yellow to red)
                    Color warningColor = Color.fromRGB(
                            255,
                            (int)(255 * timeLeft),
                            0
                    );
                    world.spawnParticle(Particle.DUST, warnLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(warningColor, 1.0f));
                }

                // Warning sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 2.0f);
                }
            }

            private void executeLightningStrike(Location target) {
                // Create main strike
                world.strikeLightningEffect(target);

                // Thunder sound with echo effect
                world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                new BukkitRunnable() {
                    private int echo = 0;
                    @Override
                    public void run() {
                        if (echo >= 2) {
                            this.cancel();
                            return;
                        }
                        world.playSound(target, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f - (echo * 0.2f), 0.8f);
                        echo++;
                    }
                }.runTaskTimer(plugin, 4L, 4L);

                // Create strike effects
                createStrikeEffects(target);

                // Check for hits
                checkStrikeHits(target);
            }

            private void createStrikeEffects(Location location) {
                // Main strike pillar
                for (double y = 0; y < 8; y += 0.5) {
                    Location pillarLoc = location.clone().add(0, y, 0);

                    // Lightning particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, pillarLoc, 5, 0.2, 0, 0.2, 0.1);
                    world.spawnParticle(Particle.WAX_OFF, pillarLoc, 3, 0.2, 0, 0.2, 0.05);

                    // Thunder visual
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, pillarLoc, 2, 0.2, 0, 0.2,
                                new Particle.DustOptions(Color.fromRGB(255, 255, 200), 1.2f));
                    }
                }

                // Ground impact
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                    Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));
                    for (double d = 0; d < 3; d += 0.5) {
                        Location impactLoc = location.clone().add(direction.clone().multiply(d));
                        world.spawnParticle(Particle.ELECTRIC_SPARK, impactLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }

            private void checkStrikeHits(Location location) {
                double hitRadius = 3.0;
                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(8.0, player);
                        hitEntities.add(entity);

                        // Electrocution effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));

                        // Strong knockback
                        Vector knockback = target.getLocation().toVector()
                                .subtract(location.toVector())
                                .normalize()
                                .multiply(1.5)
                                .setY(0.8);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createFinishingEffect() {
                // Final thunder storm
                for (int i = 0; i < 4; i++) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location strikeLoc = player.getLocation().add(
                                    random.nextDouble() * 10 - 5,
                                    0,
                                    random.nextDouble() * 10 - 5
                            );
                            world.strikeLightningEffect(strikeLoc);
                        }
                    }.runTaskLater(plugin, i * 3);
                }
            }

            private class LightningCharge {
                private Location currentLocation;
                private Location targetLocation;
                private Vector direction;
                private double speed = 1.0;

                public LightningCharge(Location start, Location target) {
                    this.currentLocation = start;
                    this.targetLocation = target;
                    this.direction = target.toVector().subtract(start.toVector()).normalize();
                }

                public boolean update() {
                    // Move charge
                    currentLocation.add(direction.clone().multiply(speed));

                    // Create trail effect
                    createChargeTrail();

                    // Check if reached target
                    return currentLocation.distance(targetLocation) < speed;
                }

                private void createChargeTrail() {
                    // Main charge
                    world.spawnParticle(Particle.ELECTRIC_SPARK, currentLocation, 5, 0.1, 0.1, 0.1, 0.05);

                    // Thunder visual
                    world.spawnParticle(Particle.DUST, currentLocation, 2, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(255, 255, 150), 1.0f));

                    // Trail effect
                    if (random.nextFloat() < 0.3) {
                        Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
                        double offset = (random.nextDouble() - 0.5) * 0.5;
                        Location trailLoc = currentLocation.clone().add(perpendicular.multiply(offset));

                        world.spawnParticle(Particle.WAX_OFF, trailLoc, 1, 0.05, 0.05, 0.05, 0);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));

        // Add cooldown
     //   addCooldown(player, "SixthForm", 20);
    }
    /**
     * Seventh Form: Honoikazuchi no Kami (漆ノ型 火の雷の神)
     * A personal creation combining blinding speed with a devastating thunder-dragon slash
     */
    public void useSeventhForm(Player player) {
        player.sendMessage("§e雷 §b漆ノ型 火の雷の神 §e(Seventh Form: Flaming Thunder God)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial charging effect
        world.strikeLightningEffect(startLoc);
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasStruck = new AtomicBoolean(false);

        // Initial charge-up effect
        createChargeUpEffect(startLoc, world);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final double CHARGE_DURATION = 0.8;
            private Location lastLocation = startLoc.clone();
            private boolean isDashing = false;
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
                double progress = time / CHARGE_DURATION;

                // Rising electric aura
                for (double y = 0; y < 3 * progress; y += 0.2) {
                    double radius = 1.0 * (1 - y/3);
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location auraLoc = location.clone().add(x, y, z);

                        // Electric particles
                        world.spawnParticle(Particle.ELECTRIC_SPARK, auraLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Thunder visual
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, auraLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f));
                        }
                    }
                }

                // Prepare stance (slight crouch)
                player.setVelocity(new Vector(0, -0.1, 0));
            }

            private void initiateDash(Location location) {
                isDashing = true;

                // Initial burst effect
                world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 2.0f);

                // Initialize dragon segments
                dragonSegments.add(location.clone());

                // Apply extreme forward velocity
                Vector direction = location.getDirection();
                player.setVelocity(direction.multiply(3.0));
            }

            private void executeDashAndSlash(Location location) {
                Vector direction = location.getDirection();

                // Update dragon segments
                updateDragonSegments(location);

                // Create dragon effects
                createDragonEffects();

                // Check for hits
                if (!hasStruck.get()) {
                    checkDashHits(location, direction);
                }
            }

            private void updateDragonSegments(Location current) {
                dragonSegments.add(0, current.clone());

                // Limit segments
                if (dragonSegments.size() > 20) {
                    dragonSegments.remove(dragonSegments.size() - 1);
                }
            }

            private void createDragonEffects() {
                // Create dragon body
                for (int i = 0; i < dragonSegments.size(); i++) {
                    Location segment = dragonSegments.get(i);
                    double segmentSize = 1.0 - (i / (double)dragonSegments.size() * 0.5);

                    // Dragon body particles
                    createDragonSegment(segment, segmentSize, i == 0);

                    // Connect segments
                    if (i < dragonSegments.size() - 1) {
                        Location nextSegment = dragonSegments.get(i + 1);
                        connectDragonSegments(segment, nextSegment, segmentSize);
                    }
                }
            }

            private void createDragonSegment(Location location, double size, boolean isHead) {
                int particles = isHead ? 16 : 8;

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI * 2 / particles) {
                    double x = Math.cos(angle) * size;
                    double y = Math.sin(angle) * size;

                    Location particleLoc = location.clone().add(x, y + 1, 0);

                    if (isHead) {
                        // Dragon head
                        world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 3, 0.1, 0.1, 0.1, 0.1);
                        world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));

                        // Dragon eyes
                        if (angle == 0 || angle == Math.PI) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    } else {
                        // Dragon body
                        world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 223, 100), 1.0f));
                    }
                }
            }

            private void connectDragonSegments(Location start, Location end, double size) {
                Vector between = end.toVector().subtract(start.toVector());
                double length = between.length();
                between.normalize();

                for (double d = 0; d < length; d += 0.5) {
                    Location connectionLoc = start.clone().add(between.multiply(d));

                    // Thunder trail
                    world.spawnParticle(Particle.ELECTRIC_SPARK, connectionLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    // Golden lightning
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, connectionLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), (float)size));
                    }
                }
            }

            private void checkDashHits(Location location, Vector direction) {
                double hitRadius = 3.0;
                for (Entity entity : world.getNearbyEntities(location, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Massive damage
                        target.damage(20.0, player);
                        hitEntities.add(entity);
                        hasStruck.set(true);

                        // Strike effects
                        createStrikeEffect(target.getLocation());

                        // Powerful knockback
                        Vector kb = direction.clone().multiply(3.0).setY(0.5);
                        target.setVelocity(kb);

                        // Lightning effect
                        world.strikeLightningEffect(target.getLocation());
                    }
                }
            }

            private void createStrikeEffect(Location location) {
                // Massive thunder explosion
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.ELECTRIC_SPARK, location, 50, 1.0, 1.0, 1.0, 0.2);

                // Golden thunder burst
                for (int i = 0; i < 8; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 3; d += 0.2) {
                        Location burstLoc = location.clone().add(direction.multiply(d));
                        world.spawnParticle(Particle.DUST, burstLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f));
                    }
                }

                // Impact sounds
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.5f, 0.8f);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.6f);
                world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final thunder pillar
                for (double y = 0; y < 10; y += 0.5) {
                    Location pillarLoc = endLoc.clone().add(0, y, 0);

                    world.spawnParticle(Particle.ELECTRIC_SPARK, pillarLoc, 3, 0.2, 0, 0.2, 0.1);
                    world.spawnParticle(Particle.DUST, pillarLoc, 2, 0.2, 0, 0.2,
                            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f));
                }

                // Final thunder
                world.strikeLightningEffect(endLoc);
                world.playSound(endLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 3, false, false));

        // Add cooldown
        //addCooldown(player, "SeventhForm", 25);
    }

    private void createChargeUpEffect(Location center, World world) {
        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_TIME = 0.8;

            @Override
            public void run() {
                if (time >= MAX_TIME) {
                    this.cancel();
                    return;
                }

                double progress = time / MAX_TIME;
                double radius = 2.0 * progress;

                // Ground circle
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = center.clone().add(x, 0.1, z);

                    // Thunder particles
                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);

                    // Golden thunder visual
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.0f));
                    }
                }

                time += 0.05;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
