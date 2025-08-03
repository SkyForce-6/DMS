package org.skyforce.demon.breathings.windbreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WindBreathingAbility {
    private final JavaPlugin plugin;

    public WindBreathingAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Beispiel-Techniken (Platzhalter)
    public void dustWhirlwindCutter(Player player) {
        double dashDistance = 5.0;
        org.bukkit.util.Vector direction = player.getLocation().getDirection().normalize();
        org.bukkit.Location start = player.getLocation();
        org.bukkit.Location dashLocation = start.clone().add(direction.multiply(dashDistance));
        dashLocation.setY(start.getY()); // Y bleibt gleich

        // Prüfe, ob Zielort frei ist (Boden und Kopf)
        org.bukkit.World world = player.getWorld();
        boolean isSafe = world.getBlockAt(dashLocation).isPassable() &&
                         world.getBlockAt(dashLocation.clone().add(0,1,0)).isPassable();
        if (!isSafe) {
            // Suche nach dem nächsten sicheren Ort rückwärts
            for (double d = dashDistance - 0.5; d > 0.5; d -= 0.5) {
                org.bukkit.Location testLoc = start.clone().add(direction.multiply(d));
                testLoc.setY(start.getY());
                if (world.getBlockAt(testLoc).isPassable() && world.getBlockAt(testLoc.clone().add(0,1,0)).isPassable()) {
                    dashLocation = testLoc;
                    isSafe = true;
                    break;
                }
            }
        }
        if (isSafe) {
            player.teleport(dashLocation);
        }
        // Partikeleffekt (z.B. SWEEP_ATTACK)
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, player.getLocation(), 30, 1, 1, 1, 0.1);
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 40, 1, 1, 1, 0.05);
        // Soundeffekt
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
        // Schaden an Entities im Umkreis vor dem Spieler
        double radius = 2.0;
        org.bukkit.util.Vector lookDir = player.getLocation().getDirection();
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(3, 2, 3)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                org.bukkit.util.Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                if (toEntity.normalize().dot(lookDir) > 0.5) {
                    ((org.bukkit.entity.LivingEntity) entity).damage(6.0, player);
                }
            }
        }
    }

    public void clawsPurifyingWind(Player player) {
        // Vier vertikale Slashes (Klauen)
        int hits = 4;
        double radius = 2.0;
        org.bukkit.util.Vector lookDir = player.getLocation().getDirection();
        for (int i = 0; i < hits; i++) {
            // Partikeleffekt (CRIT für Klauen)
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            // Soundeffekt
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.2f);
            // Schaden an Entities vor dem Spieler
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, 2, radius)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                    org.bukkit.util.Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (toEntity.normalize().dot(lookDir) > 0.5) {
                        ((org.bukkit.entity.LivingEntity) entity).damage(3.0, player);
                    }
                }
            }
        }
    }

    public void clearStormWindTree(Player player) {
        // Wirbelwind um den Spieler (Partikelkreis)
        double radius = 3.0;
        int particleCount = 60;
        org.bukkit.Location loc = player.getLocation().add(0, 1, 0);
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            org.bukkit.Location particleLoc = loc.clone().add(x, 0, z);
            player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, particleLoc, 2, 0, 0, 0, 0);
        }
        // Soundeffekt
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 1.0f);
        // Schaden und Knockback an Entities im Umkreis
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, 2, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player) {
                ((org.bukkit.entity.LivingEntity) entity).damage(4.0, player);
                // Knockback vom Spieler weg
                org.bukkit.util.Vector knockback = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.2);
                knockback.setY(0.4);
                entity.setVelocity(knockback);
            }
        }
    }

    public void risingCyclone(Player player) {
        // TODO: Implementiere Wind-Effekt, Partikel, Schaden, Sound
    }

    Random random = new Random();

    /**
     * First Form: Dust Whirlwind Cutter (壱いちノ型かた　塵じん旋せん風ぷう斬ざん Ichi no kata: Jinsen Pūzan?)
     * Creates a powerful whirlwind slash that can cut multiple targets simultaneously
     */
    public void useFirstForm(Player player) {
        player.sendMessage("§7風 §f壱ノ型 塵旋風斬 §7(First Form: Dust Whirlwind Cutter)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private Location lastLocation = startLoc.clone();
            private double currentRotation = 0;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                // Create whirlwind effect
                createWhirlwind(currentLoc, time);

                // Check for hits
                checkWhirlwindHits(currentLoc);

                // Movement enhancement
                if (time < 0.5) {
                    player.setVelocity(direction.multiply(0.8).setY(0.2));
                }

                time += 0.05;
                lastLocation = currentLoc.clone();
            }

            private void createWhirlwind(Location center, double time) {
                double radius = 3.0;
                currentRotation += 20; // Rotation speed

                // Create multiple layers of the whirlwind
                for (double y = 0; y < 4; y += 0.5) {
                    double layerRadius = radius * (1 - y/4);
                    double heightOffset = y + Math.sin(time * 10) * 0.2;

                    for (double angle = 0; angle < 360; angle += 30) {
                        double actualAngle = angle + currentRotation + (y * 30);
                        double radian = Math.toRadians(actualAngle);

                        double x = Math.cos(radian) * layerRadius;
                        double z = Math.sin(radian) * layerRadius;

                        Location particleLoc = center.clone().add(x, heightOffset, z);

                        // Wind particles
                        world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Dust particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(200, 200, 200), 1.0f));
                        }

                        // Sweep particles for slash visualization
                        if (random.nextFloat() < 0.1) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }

                // Wind sound effects
                if (random.nextFloat() < 0.2) {
                    world.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 2.0f);
                }
            }

            private void checkWhirlwindHits(Location center) {
                double hitRadius = 4.0;
                for (Entity entity : world.getNearbyEntities(center, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance
                        double distance = center.distance(target.getLocation());
                        double damage = 8.0 * (1 - distance/(hitRadius + 1));

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(entity);

                        // Create hit effects
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(1.2)
                                .setY(0.4);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Slash effect
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 3, 0.2, 0.2, 0.2, 0);

                // Wind burst
                for (int i = 0; i < 8; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 2; d += 0.2) {
                        Location burstLoc = location.clone().add(direction.multiply(d));
                        world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final wind burst
                new BukkitRunnable() {
                    private double radius = 1.0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, 0.1, z);

                            // Wind particles
                            world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);

                            // Dust particles
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.DUST, burstLoc, 1, 0.1, 0.1, 0.1,
                                        new Particle.DustOptions(Color.fromRGB(200, 200, 200), 1.0f));
                            }
                        }

                        // Expanding wind sound
                        world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 1.5f + (ticks / (float)MAX_TICKS));

                        radius += 0.3;
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));

        // Add cooldown
      //  addCooldown(player, "FirstForm", 10);
    }

    /**
     * Second Form: Claws-Purifying Wind (弐ノ型 爪々・科戸風)
     * Creates four simultaneous diagonal slashes that resemble claws in an X-pattern
     */
    public void useSecondForm(Player player) {
        player.sendMessage("§7風 §f弐ノ型 爪々・科戸風 §7(Second Form: Claws-Purifying Wind)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasExecutedSlash = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private final double SLASH_TIME = 0.3;
            private List<ClawSlash> activeSlashes = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < SLASH_TIME) {
                    // Preparation phase
                    createPreparationEffect(currentLoc);
                } else if (!hasExecutedSlash.get()) {
                    // Execute the four slashes
                    executeClawSlashes(currentLoc);
                    hasExecutedSlash.set(true);
                }

                // Update active slashes
                updateSlashes();

                time += 0.05;
            }

            private void createPreparationEffect(Location location) {
                // Lifting sword effect
                double progress = time / SLASH_TIME;
                double height = 2.5 * progress;

                Location swordLoc = location.clone().add(0, height, 0);
                Vector direction = location.getDirection();

                // Wind gathering effect
                for (int i = 0; i < 4; i++) {
                    double angle = i * (Math.PI / 2);
                    Vector offset = direction.clone().rotateAroundY(angle).multiply(progress);

                    Location particleLoc = swordLoc.clone().add(offset);

                    // Wind particles
                    world.spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                            new Particle.DustOptions(Color.fromRGB(220, 220, 220), (float)progress));
                }

                // Charging sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f + (float)progress);
                }
            }

            private void executeClawSlashes(Location location) {
                Vector direction = location.getDirection();

                // Create four diagonal slashes in X-pattern
                double spacing = 0.8;

                // First pair - top-left to bottom-right
                for (int i = 0; i < 2; i++) {
                    double offset = (i - 0.5) * spacing;
                    Vector slashOffset = direction.clone().rotateAroundY(Math.PI / 4)
                            .multiply(offset);
                    Location slashStart = location.clone()
                            .add(slashOffset)
                            .add(0, 2.5, 0)
                            .add(direction.clone().multiply(-1));

                    ClawSlash slash = new ClawSlash(
                            slashStart,
                            direction.clone().add(new Vector(0, -0.8, 0)).normalize(),
                            0.5
                    );
                    activeSlashes.add(slash);
                }

                // Second pair - top-right to bottom-left
                for (int i = 0; i < 2; i++) {
                    double offset = (i - 0.5) * spacing;
                    Vector slashOffset = direction.clone().rotateAroundY(-Math.PI / 4)
                            .multiply(offset);
                    Location slashStart = location.clone()
                            .add(slashOffset)
                            .add(0, 2.5, 0)
                            .add(direction.clone().multiply(-1));

                    ClawSlash slash = new ClawSlash(
                            slashStart,
                            direction.clone().add(new Vector(0, -0.8, 0)).normalize(),
                            0.5
                    );
                    activeSlashes.add(slash);
                }

                // More dramatic slash execution effects
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 2.0f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.2f);

                // Create initial slash burst
                for (int i = 0; i < 20; i++) {
                    Location burstLoc = location.clone().add(
                            random.nextDouble() * 2 - 1,
                            random.nextDouble() * 2 + 1,
                            random.nextDouble() * 2 - 1
                    );
                    world.spawnParticle(Particle.SWEEP_ATTACK, burstLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.CLOUD, burstLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }

                // More dramatic forward momentum
                player.setVelocity(direction.multiply(1.5).setY(0.2));
            }

            private void updateSlashes() {
                Iterator<ClawSlash> iterator = activeSlashes.iterator();
                while (iterator.hasNext()) {
                    ClawSlash slash = iterator.next();
                    slash.lifetime -= 0.05;

                    if (slash.lifetime <= 0) {
                        iterator.remove();
                        continue;
                    }

                    // Update and render slash
                    slash.update();

                    // Check for hits
                    checkSlashHits(slash);
                }
            }

            private void checkSlashHits(ClawSlash slash) {
                double hitWidth = 0.8;
                double hitHeight = 3.0;

                for (Entity entity : world.getNearbyEntities(slash.currentLocation, hitWidth, hitHeight, hitWidth)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(6.0, player);
                        hitEntities.add(entity);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback
                        Vector knockback = slash.direction.clone().multiply(1.5).setY(0.4);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Slash impact
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.1, 0.1, 0.1, 0);

                // Wind burst
                for (int i = 0; i < 8; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 1.5; d += 0.2) {
                        Location burstLoc = location.clone().add(direction.multiply(d));
                        world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();
                Vector direction = endLoc.getDirection();

                // Create final wind claw marks
                for (int i = 0; i < 4; i++) {
                    double offset = (i - 1.5) * 0.8;
                    Vector slashOffset = direction.clone().rotateAroundY(Math.PI / 2).multiply(offset);
                    Location markLoc = endLoc.clone().add(slashOffset);

                    // Create descending wind effect
                    new BukkitRunnable() {
                        private double height = 3.0;
                        private int ticks = 0;
                        private final int MAX_TICKS = 10;

                        @Override
                        public void run() {
                            if (ticks >= MAX_TICKS) {
                                this.cancel();
                                return;
                            }

                            Location currentMark = markLoc.clone().add(0, height, 0);

                            // Wind particles
                            world.spawnParticle(Particle.CLOUD, currentMark, 3, 0.1, 0.1, 0.1, 0.05);
                            world.spawnParticle(Particle.SWEEP_ATTACK, currentMark, 1, 0, 0, 0, 0);

                            height -= 0.3;
                            ticks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }

                // Final sound
                world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.4f);
            }

            private class ClawSlash {
                private Location currentLocation;
                private Vector direction;
                private double lifetime;
                private double speed = 1.2;
                private double initialDistance = 0;

                public ClawSlash(Location start, Vector direction, double lifetime) {
                    this.currentLocation = start;
                    this.direction = direction;
                    this.lifetime = lifetime;
                    this.initialDistance = start.distance(player.getLocation());
                }

                public void update() {
                    // Move slash forward and downward in an arc
                    double progress = 1 - (lifetime / 0.5);
                    Vector movement = direction.clone()
                            .multiply(speed)
                            .add(new Vector(
                                    0,
                                    -1.5 * Math.sin(progress * Math.PI),
                                    0
                            ));

                    currentLocation.add(movement);

                    // Create more dramatic slash effects
                    createSlashEffects(progress);
                }

                private void createSlashEffects(double progress) {
                    // Main slash effect
                    world.spawnParticle(Particle.SWEEP_ATTACK, currentLocation, 1, 0, 0, 0, 0);

                    // Create longer wind trails
                    Vector perpendicular = direction.clone().rotateAroundY(Math.PI / 2);
                    for (int i = 0; i < 5; i++) {
                        double trailOffset = (random.nextDouble() - 0.5) * 1.5;
                        Location trailLoc = currentLocation.clone().add(
                                perpendicular.clone().multiply(trailOffset)
                        );

                        // Wind trail
                        world.spawnParticle(Particle.CLOUD, trailLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // More visible slash trail
                        if (random.nextFloat() < 0.4) {
                            world.spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(220, 220, 220), 0.8f));
                        }
                    }

                    // Add occasional spark effects
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.WAX_OFF, currentLocation, 2, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 1, false, false));

        // Add cooldown
     //   addCooldown(player, "SecondForm", 12);
    }

    /**
     * Third Form: Clean Storm Wind (参さんノ型かた　清せい爽そう風かぜ San no kata: Seisō Kaze?)
     * The user creates a whirlwind around their sword by spinning it in a circular motion to deflect
     * and neutralize incoming attacks, slash through them and counter with a powerful strike.
     */
    public void useThirdForm(Player player) {
        player.sendMessage("§7風 §f参ノ型 清爽風 §7(Third Form: Clean Storm Wind)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.2f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean isSpinning = new AtomicBoolean(true);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 3.0;
            private double currentRotation = 0;
            private double spinSpeed = 30; // Degrees per tick
            private Vector lastDirection = player.getLocation().getDirection();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector currentDirection = currentLoc.getDirection();

                // Create spinning whirlwind effect
                if (isSpinning.get()) {
                    createWhirlwindEffect(currentLoc);
                    deflectProjectiles(currentLoc);

                    // Check for counter opportunity
                    if (checkForCounterOpportunity(currentLoc)) {
                        isSpinning.set(false);
                        executeCounterStrike(currentLoc);
                    }
                }

                lastDirection = currentDirection;
                time += 0.05;
                currentRotation += spinSpeed;
            }

            private void createWhirlwindEffect(Location center) {
                double radius = 2.0;
                double height = 3.0;

                // Create spinning whirlwind
                for (double y = 0; y < height; y += 0.3) {
                    double layerRotation = currentRotation + (y * 20); // Spiral effect
                    double layerRadius = radius * (1 - y/height * 0.3); // Slight cone shape

                    for (int i = 0; i < 4; i++) {
                        double angle = Math.toRadians(layerRotation + (i * 90));
                        double x = Math.cos(angle) * layerRadius;
                        double z = Math.sin(angle) * layerRadius;

                        Location particleLoc = center.clone().add(x, y, z);

                        // Wind particles
                        world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Sword trail
                        if (i % 2 == 0) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);
                        }

                        // Wind visual
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(220, 220, 220), 0.8f));
                        }
                    }
                }

                // Spinning sound effects
                if (random.nextFloat() < 0.2) {
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.8f);
                    world.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 2.0f);
                }
            }

            private void deflectProjectiles(Location center) {
                double deflectRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(center, deflectRadius, deflectRadius, deflectRadius)) {
                    if (entity instanceof Projectile && !hitEntities.contains(entity)) {
                        Projectile projectile = (Projectile) entity;

                        // Deflect projectile
                        Vector velocity = projectile.getVelocity();
                        Vector newVelocity = velocity.multiply(-1.2); // Reflect with increased speed
                        projectile.setVelocity(newVelocity);

                        // Mark as deflected
                        hitEntities.add(projectile);

                        // Deflection effect
                        createDeflectionEffect(projectile.getLocation());
                    }
                }
            }

            private void createDeflectionEffect(Location location) {
                // Deflection burst
                world.spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CLOUD, location, 10, 0.2, 0.2, 0.2, 0.1);

                // Deflection sound
                world.playSound(location, Sound.ITEM_SHIELD_BLOCK, 0.8f, 2.0f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
            }

            private boolean checkForCounterOpportunity(Location center) {
                // Check for nearby entities that could trigger a counter
                double checkRadius = 4.0;
                for (Entity entity : world.getNearbyEntities(center, checkRadius, checkRadius, checkRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Check if target is attacking (simplified)
                        if (target.getVelocity().length() > 0.2) {
                            return true;
                        }
                    }
                }
                return time > MAX_DURATION * 0.7; // Force counter near end of duration
            }

            private void executeCounterStrike(Location location) {
                Vector direction = location.getDirection();

                // Counter strike effect
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);

                // Create strike effect
                for (double d = 0; d < 4; d += 0.2) {
                    Location strikeLoc = location.clone().add(direction.clone().multiply(d));

                    world.spawnParticle(Particle.SWEEP_ATTACK, strikeLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.CLOUD, strikeLoc, 3, 0.1, 0.1, 0.1, 0.1);
                }

                // Check for hits with counter
                double strikeRadius = 3.0;
                for (Entity entity : world.getNearbyEntities(location, strikeRadius, strikeRadius, strikeRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply counter damage
                        target.damage(10.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createCounterHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply strong knockback
                        Vector knockback = direction.clone().multiply(2.0).setY(0.5);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createCounterHitEffect(Location location) {
                // Counter hit burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 3, 0.2, 0.2, 0.2, 0);

                // Wind burst
                for (int i = 0; i < 12; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize();

                    for (double d = 0; d < 2; d += 0.2) {
                        Location burstLoc = location.clone().add(direction.multiply(d));
                        world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final wind dispersion
                for (double radius = 0; radius < 4; radius += 0.2) {
                    for (double angle = 0; angle < 360; angle += 15) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location disperseLoc = endLoc.clone().add(x, 0.1, z);

                        world.spawnParticle(Particle.CLOUD, disperseLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Final sound
                world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.4f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false));

        // Add cooldown
      //  addCooldown(player, "ThirdForm", 15);
    }

    /**
     * Fourth Form: Rising Dust Storm (肆よんノ型かた　昇しょう塵じん嵐らん Yon no kata: Shōjin Ran?)
     * The user charges at their opponent at high speed while swinging their blade in a circular motion,
     * creating a powerful whirlwind that pulls opponents in.
     */
    public void useFourthForm(Player player) {
        player.sendMessage("§7風 §f肆ノ型 昇塵嵐 §7(Fourth Form: Rising Dust Storm)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.6f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.4f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean isDashing = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.5;
            private final double CHARGE_DURATION = 0.4;
            private double currentRotation = 0;
            private Location lastLocation = startLoc.clone();
            private List<Location> whirlwindCenters = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector direction = currentLoc.getDirection();

                if (time < CHARGE_DURATION) {
                    // Charging phase
                    createChargingEffect(currentLoc);
                } else if (!isDashing.get()) {
                    // Initialize dash
                    initiateDash(currentLoc);
                    isDashing.set(true);
                } else {
                    // Execute dash and whirlwind
                    executeDashAndWhirlwind(currentLoc);
                }

                time += 0.05;
                lastLocation = currentLoc.clone();
                currentRotation += 15;
            }

            private void createChargingEffect(Location location) {
                double progress = time / CHARGE_DURATION;
                double radius = 2.0 * progress;

                // Ground dust effect
                for (double angle = 0; angle < 360; angle += 15) {
                    double radian = Math.toRadians(angle + currentRotation);
                    double x = Math.cos(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location dustLoc = location.clone().add(x, 0.1, z);

                    // Dust particles
                    world.spawnParticle(Particle.CLOUD, dustLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DUST, dustLoc, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.fromRGB(200, 200, 200), 0.8f));
                    }
                }

                // Rising wind effect
                for (int i = 0; i < 4; i++) {
                    double angle = i * (Math.PI / 2) + (time * 10);
                    double heightOffset = progress * 2.5;

                    Location windLoc = location.clone().add(
                            Math.cos(angle) * radius * 0.5,
                            heightOffset,
                            Math.sin(angle) * radius * 0.5
                    );

                    world.spawnParticle(Particle.CLOUD, windLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }

                // Charge sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f + (float)progress);
                }
            }

            private void initiateDash(Location location) {
                // Initial burst effect
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CLOUD, location, 20, 0.5, 0.5, 0.5, 0.2);

                // Burst sounds
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 2.0f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

                // Apply dash velocity
                Vector dashVec = location.getDirection().multiply(2.0);
                player.setVelocity(dashVec);
            }

            private void executeDashAndWhirlwind(Location location) {
                // Add current location to whirlwind centers
                whirlwindCenters.add(location.clone());

                // Limit the number of whirlwind centers
                while (whirlwindCenters.size() > 10) {
                    whirlwindCenters.remove(0);
                }

                // Create whirlwind effect at each center
                for (int i = 0; i < whirlwindCenters.size(); i++) {
                    Location center = whirlwindCenters.get(i);
                    double ageMultiplier = (double)i / whirlwindCenters.size();
                    createWhirlwindAtLocation(center, ageMultiplier);
                }

                // Pull in nearby entities
                pullEntities(location);

                // Check for hits
                checkWhirlwindHits(location);

                // Maintain forward momentum
                if (player.getVelocity().length() < 1.0) {
                    player.setVelocity(location.getDirection().multiply(1.0));
                }
            }

            private void createWhirlwindAtLocation(Location center, double ageMultiplier) {
                double baseRadius = 2.0;
                double height = 4.0;

                for (double y = 0; y < height; y += 0.5) {
                    double layerRadius = baseRadius * (1 - y/height * 0.3) * (1 - ageMultiplier * 0.5);
                    double layerRotation = currentRotation + (y * 20);

                    for (int i = 0; i < 4; i++) {
                        double angle = Math.toRadians(layerRotation + (i * 90));
                        double x = Math.cos(angle) * layerRadius;
                        double z = Math.sin(angle) * layerRadius;

                        Location particleLoc = center.clone().add(x, y, z);

                        // Wind particles
                        world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                        // Dust particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0.1, 0.1, 0.1,
                                    new Particle.DustOptions(Color.fromRGB(200, 200, 200), 0.8f));
                        }

                        // Slash effects
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }

            private void pullEntities(Location center) {
                double pullRadius = 5.0;
                for (Entity entity : world.getNearbyEntities(center, pullRadius, pullRadius, pullRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate pull direction
                        Vector pullDirection = center.toVector().subtract(target.getLocation().toVector());
                        double distance = pullDirection.length();

                        if (distance > 0) {
                            pullDirection.normalize();
                            double pullStrength = 0.5 * (1 - distance/pullRadius);

                            // Apply pull effect
                            Vector currentVel = target.getVelocity();
                            target.setVelocity(currentVel.add(pullDirection.multiply(pullStrength)));
                        }
                    }
                }
            }

            private void checkWhirlwindHits(Location center) {
                double hitRadius = 3.0;
                for (Entity entity : world.getNearbyEntities(center, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(8.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply spiral knockback
                        Vector knockbackDir = target.getLocation().subtract(center).toVector();
                        knockbackDir.rotateAroundY(Math.PI / 2); // Rotate 90 degrees for spiral effect
                        knockbackDir.normalize().multiply(1.2).setY(0.4);
                        target.setVelocity(knockbackDir);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact burst
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.CLOUD, location, 10, 0.2, 0.2, 0.2, 0.1);

                // Wind spiral
                for (double angle = 0; angle < Math.PI * 4; angle += Math.PI / 8) {
                    double radius = angle / (Math.PI * 4) * 1.5;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spiralLoc = location.clone().add(x, angle * 0.1, z);
                    world.spawnParticle(Particle.CLOUD, spiralLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final wind explosion
                new BukkitRunnable() {
                    private double radius = 0;
                    private final double MAX_RADIUS = 5.0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        double currentRadius = MAX_RADIUS * progress;

                        for (double angle = 0; angle < 360; angle += 10) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * currentRadius;
                            double z = Math.sin(radian) * currentRadius;

                            Location burstLoc = endLoc.clone().add(x, 0.1, z);

                            // Wind particles
                            world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);

                            // Dust particles
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.DUST, burstLoc, 1, 0.1, 0.1, 0.1,
                                        new Particle.DustOptions(Color.fromRGB(200, 200, 200), 0.8f));
                            }
                        }

                        // Expanding sound
                        world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 1.5f + (float)progress);

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 50, 1, false, false));

        // Add cooldown
       // addCooldown(player, "FourthForm", 16);
    }

    /**
     * Fifth Form: Cold Mountain Wind (伍ノ型 木枯らし颪)
     * Creates ascending circular slashes that increase in size, resembling the cold winds descending from a mountain
     */
    public void useFifthForm(Player player) {
        player.sendMessage("§7風 §f伍ノ型 木枯らし颪 §7(Fifth Form: Cold Mountain Wind)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.6f);
        world.playSound(startLoc, Sound.BLOCK_SNOW_BREAK, 1.0f, 0.8f);

        Set<Entity> hitEntities = new HashSet<>();
        List<WindBreathingAbility.CircularSlash> activeSlashes = new ArrayList<>(); // Korrigierte Deklaration

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private int slashCount = 0;
            private final int TOTAL_SLASHES = 5;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Create new slashes periodically
                if (time > 0.2 * slashCount && slashCount < TOTAL_SLASHES) {
                    createNewSlash(currentLoc);
                    slashCount++;
                }

                // Update and render active slashes
                updateSlashes();

                // Cold wind ambient effects
                createAmbientEffects(currentLoc);

                time += 0.05;
            }

            private void createNewSlash(Location location) {
                double baseSize = 2.0 + (slashCount * 0.8); // Increasing size
                double baseHeight = 0.5 + (slashCount * 0.5); // Increasing height

                WindBreathingAbility.CircularSlash slash = new WindBreathingAbility.CircularSlash( // Korrigierter Konstruktoraufruf
                        location.clone(),
                        baseSize,
                        baseHeight,
                        0.8 // Duration for each slash
                );

                activeSlashes.add(slash);

                // Slash creation effects
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f - (slashCount * 0.1f));
                world.playSound(location, Sound.BLOCK_SNOW_BREAK, 0.8f, 0.6f + (slashCount * 0.1f));

                // Initial burst effect
                for (int i = 0; i < 12; i++) {
                    double angle = i * (Math.PI * 2 / 12);
                    Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));
                    Location burstLoc = location.clone().add(direction.multiply(baseSize * 0.5));

                    world.spawnParticle(Particle.CLOUD, burstLoc, 3, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.SNOWFLAKE, burstLoc, 2, 0.1, 0.1, 0.1, 0.05);
                }
            }

            private void updateSlashes() {
                Iterator<WindBreathingAbility.CircularSlash> iterator = activeSlashes.iterator(); // Korrigierter Iterator-Typ
                while (iterator.hasNext()) {
                    WindBreathingAbility.CircularSlash slash = iterator.next();
                    slash.duration -= 0.05;

                    if (slash.duration <= 0) {
                        iterator.remove();
                        continue;
                    }

                    slash.update();
                    checkSlashHits(slash);
                }
            }

            private void createAmbientEffects(Location center) {
                // Cold wind particles
                for (int i = 0; i < 3; i++) {
                    Location particleLoc = center.clone().add(
                            random.nextDouble() * 8 - 4,
                            random.nextDouble() * 4,
                            random.nextDouble() * 8 - 4
                    );

                    world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Ambient sound
                if (random.nextFloat() < 0.1) {
                    world.playSound(center, Sound.BLOCK_SNOW_BREAK, 0.3f, 1.5f);
                }
            }

            private void checkSlashHits(WindBreathingAbility.CircularSlash slash) { // Korrigierter Parametertyp
                Location center = slash.getCenter(); // Verwende Getter-Methode
                double hitRadius = slash.getCurrentSize(); // Verwende Getter-Methode
                double hitHeight = 2.0;

                for (Entity entity : world.getNearbyEntities(center, hitRadius, hitHeight, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on slash size
                        double damage = 4.0 + (slash.baseSize - 2.0);

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(1.0)
                                .setY(0.3);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Cold impact burst
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.SNOWFLAKE, location, 15, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.CLOUD, location, 8, 0.2, 0.2, 0.2, 0.05);

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
                world.playSound(location, Sound.BLOCK_SNOW_BREAK, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final cold wind burst
                new BukkitRunnable() {
                    private double radius = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        radius = 6.0 * progress;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, 0.1 + (progress * 2), z);

                            // Cold wind particles
                            world.spawnParticle(Particle.SNOWFLAKE, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            }
                        }

                        // Expanding sound
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.BLOCK_SNOW_BREAK, 0.4f, 0.6f + (float)progress);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private class CircularSlash {
                private Location center;
                private double baseSize;
                private double currentSize;
                private double height;
                private double duration;
                private double rotation = 0;

                public CircularSlash(Location center, double baseSize, double height, double duration) {
                    this.center = center;
                    this.baseSize = baseSize;
                    this.currentSize = 0; // Starts from 0 and expands
                    this.height = height;
                    this.duration = duration;
                }

                public void update() {
                    // Expand the slash
                    currentSize = baseSize * (1 - (duration / 0.8)); // Expands as duration decreases
                    rotation += 15; // Rotate the slash

                    // Create slash effects
                    createSlashEffects();
                }

                private void createSlashEffects() {
                    double particleCount = 36; // Number of particles in the circle
                    double heightVariation = Math.sin(rotation / 30.0) * 0.2; // Slight height variation

                    for (int i = 0; i < particleCount; i++) {
                        double angle = (i * (360.0 / particleCount) + rotation) * Math.PI / 180;
                        double x = Math.cos(angle) * currentSize;
                        double z = Math.sin(angle) * currentSize;

                        Location particleLoc = center.clone().add(x, height + heightVariation, z);

                        // Main slash effect
                        world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);

                        // Cold wind particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        }

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false));

        // Add cooldown
  //      addCooldown(player, "FifthForm", 14);
    }

    /**
     * Helper class for the Fifth Form's circular slashes
     */
    private static class CircularSlash {
        private Location center;
        private double baseSize;
        private double currentSize;
        private double height;
        private double duration;
        private double rotation = 0;
        private final World world;
        private final Random random;

        public CircularSlash(Location center, double baseSize, double height, double duration) {
            this.center = center;
            this.baseSize = baseSize;
            this.currentSize = 0; // Starts from 0 and expands
            this.height = height;
            this.duration = duration;
            this.world = center.getWorld();
            this.random = new Random();
        }

        public void update() {
            // Expand the slash
            currentSize = baseSize * (1 - (duration / 0.8)); // Expands as duration decreases
            rotation += 15; // Rotate the slash

            // Create slash effects
            createSlashEffects();
        }

        private void createSlashEffects() {
            double particleCount = 36; // Number of particles in the circle
            double heightVariation = Math.sin(rotation / 30.0) * 0.2; // Slight height variation

            for (int i = 0; i < particleCount; i++) {
                double angle = (i * (360.0 / particleCount) + rotation) * Math.PI / 180;
                double x = Math.cos(angle) * currentSize;
                double z = Math.sin(angle) * currentSize;

                Location particleLoc = center.clone().add(x, height + heightVariation, z);

                // Main slash effect
                world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0, 0, 0, 0);

                // Cold wind particles
                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }

        public Location getCenter() {
            return center;
        }

        public double getCurrentSize() {
            return currentSize;
        }

        public double getBaseSize() {
            return baseSize;
        }
    }

    /**
     * Sixth Form: Black Wind Mountain Mist (陸ノ型 黒風烟嵐)
     * The user performs a powerful uppercut slash while rotating, creating an arcing dark wind slash
     */
    public void useSixthForm(Player player) {
        player.sendMessage("§7風 §f陸ノ型 黒風烟嵐 §7(Sixth Form: Black Wind Mountain Mist)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.7f);
        world.playSound(startLoc, Sound.ENTITY_WITHER_SHOOT, 0.5f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        double[] currentRotation = {0};
        double[] currentHeight = {0};

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.5;
            private final double CHARGE_DURATION = 0.3;
            private boolean hasExecutedUppercut = false;
            private Location arcCenter = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < CHARGE_DURATION) {
                    // Charging phase
                    createChargingEffect(currentLoc);
                } else if (!hasExecutedUppercut) {
                    // Execute uppercut
                    executeUppercut(currentLoc);
                    hasExecutedUppercut = true;
                    arcCenter = currentLoc.clone();
                } else {
                    // Continue arc effect
                    updateArcEffect();
                }

                time += 0.05;
            }

            private void createChargingEffect(Location location) {
                double progress = time / CHARGE_DURATION;

                // Dark wind gathering effect
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2 / 8) + (time * 10);
                    double radius = 1.5 * progress;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = location.clone().add(x, 0.1, z);

                    // Dark wind particles
                    world.spawnParticle(Particle.SMOKE, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.SMOKE, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }

                // Charging sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.5f + (float)progress);
                }
            }

            private void executeUppercut(Location location) {
                // Initial burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.SMOKE, location, 20, 0.5, 0.5, 0.5, 0.1);

                // Sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.5f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.6f);

                // Apply upward velocity to player
                player.setVelocity(new Vector(0, 1.2, 0));

                // Initial slash effect
                createUppercutSlash(location);
            }

            private void createUppercutSlash(Location location) {
                double height = 4.0;
                double arcRadius = 3.0;

                for (double y = 0; y < height; y += 0.2) {
                    double progress = y / height;
                    double radius = arcRadius * Math.sin(progress * Math.PI);

                    for (double angle = -90; angle <= 90; angle += 10) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location slashLoc = location.clone().add(x, y, z);

                        // Slash effects
                        world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.SMOKE, slashLoc, 2, 0.1, 0.1, 0.1, 0);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.SMOKE, slashLoc, 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }
            }

            private void updateArcEffect() {
                if (arcCenter == null) return;

                double progress = (time - CHARGE_DURATION) / (MAX_DURATION - CHARGE_DURATION);
                double arcAngle = 180 * progress; // 180-degree arc

                // Create dark wind arc
                for (double angle = -90 + arcAngle; angle <= 90 + arcAngle; angle += 10) {
                    double radian = Math.toRadians(angle);
                    double radius = 3.0;
                    double x = Math.cos(radian) * radius;
                    double y = 2.0 + Math.sin(radian) * radius;
                    double z = Math.sin(radian) * radius;

                    Location arcLoc = arcCenter.clone().add(x, y, z);

                    // Arc particles
                    world.spawnParticle(Particle.SMOKE, arcLoc, 2, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.CLOUD, arcLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, arcLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Check for hits
                checkArcHits();
            }

            private void checkArcHits() {
                if (arcCenter == null) return;

                double hitRadius = 4.0;
                double hitHeight = 4.0;

                for (Entity entity : world.getNearbyEntities(arcCenter, hitRadius, hitHeight, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(9.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply strong upward knockback
                        Vector knockback = new Vector(
                                (target.getLocation().getX() - arcCenter.getX()) * 0.5,
                                1.5,
                                (target.getLocation().getZ() - arcCenter.getZ()) * 0.5
                        );
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Dark impact burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.SMOKE, location, 10, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.1, 0.1, 0.1, 0);

                // Create dark wind spiral
                for (double angle = 0; angle < Math.PI * 4; angle += Math.PI / 8) {
                    double radius = angle / (Math.PI * 4) * 1.5;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spiralLoc = location.clone().add(x, angle * 0.1, z);
                    world.spawnParticle(Particle.SMOKE, spiralLoc, 1, 0.1, 0.1, 0.1, 0);
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 0.7f);
                world.playSound(location, Sound.ENTITY_WITHER_SHOOT, 0.4f, 1.5f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final dark wind burst
                new BukkitRunnable() {
                    private double radius = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 15;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        radius = 5.0 * progress;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, 0.1 + (progress * 2), z);

                            // Dark wind particles
                            world.spawnParticle(Particle.SMOKE, burstLoc, 1, 0.1, 0.1, 0.1, 0);
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            }
                        }

                        // Finishing sounds
                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 0.5f + (float)progress);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));

        // Add cooldown
       // addCooldown(player, "SixthForm", 15);
    }

    /**
     * Seventh Form: Gale, Sudden Gusts (漆ノ型 頸風・天狗風)
     * Creates powerful gale-force winds through aerial blade swings that shred opponents
     */
    public void useSeventhForm(Player player) {
        player.sendMessage("§7風 §f漆ノ型 頸風・天狗風 §7(Seventh Form: Gale, Sudden Gusts)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.4f);

        Set<Entity> hitEntities = new HashSet<>();
        List<GaleSlash> activeGales = new ArrayList<>();

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final double LEAP_DURATION = 0.4;
            private boolean hasLeaped = false;
            private Location peakLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < LEAP_DURATION) {
                    // Initial leap phase
                    executeLepPhase(currentLoc);
                } else if (!hasLeaped) {
                    // Execute main gale attack
                    executeGaleAttack(currentLoc);
                    hasLeaped = true;
                    peakLocation = currentLoc.clone();
                } else {
                    // Update and create gale effects
                    updateGaleEffects(currentLoc);
                }

                // Update active gales
                updateGales();

                time += 0.05;
            }

            private void executeLepPhase(Location location) {
                double progress = time / LEAP_DURATION;

                // Create rising wind effect
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2 / 8) + (time * 10);
                    double radius = 1.0 + progress;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location windLoc = location.clone().add(x, progress * 2, z);

                    world.spawnParticle(Particle.CLOUD, windLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, windLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Apply upward velocity
                if (progress > 0.2) {
                    player.setVelocity(new Vector(0, 1.8 - progress, 0));
                }

                // Rising sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f + (float)progress);
                }
            }

            private void executeGaleAttack(Location location) {
                // Create initial gale burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CLOUD, location, 30, 1.0, 1.0, 1.0, 0.2);

                // Create multiple gale slashes
                for (int i = 0; i < 4; i++) {
                    double angle = i * (Math.PI / 2);
                    Vector direction = location.getDirection().clone().rotateAroundY(angle);

                    GaleSlash gale = new GaleSlash(
                            location.clone(),
                            direction,
                            0.8, // Duration
                            3.0 + i * 0.5 // Size
                    );
                    activeGales.add(gale);
                }

                // Burst sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.6f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.2f);
            }

            private void updateGales() {
                Iterator<GaleSlash> iterator = activeGales.iterator();
                while (iterator.hasNext()) {
                    GaleSlash gale = iterator.next();
                    gale.update();

                    if (gale.isDead()) {
                        iterator.remove();
                        continue;
                    }

                    // Check for hits
                    checkGaleHits(gale);
                }
            }

            private void checkGaleHits(GaleSlash gale) {
                double hitRadius = gale.size;

                for (Entity entity : world.getNearbyEntities(gale.currentLocation, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(7.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply knockback in gale direction
                        Vector knockback = gale.direction.clone().multiply(1.5).add(new Vector(0, 0.4, 0));
                        target.setVelocity(knockback);
                    }
                }
            }

            private void updateGaleEffects(Location location) {
                // Create ongoing wind effects
                for (int i = 0; i < 4; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double radius = random.nextDouble() * 3.0;
                    double x = Math.cos(angle) * radius;
                    double y = random.nextDouble() * 2.0;
                    double z = Math.sin(angle) * radius;

                    Location effectLoc = location.clone().add(x, y, z);

                    world.spawnParticle(Particle.CLOUD, effectLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, effectLoc, 1, 0, 0, 0, 0);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CLOUD, location, 15, 0.2, 0.2, 0.2, 0.1);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 3, 0.2, 0.2, 0.2, 0);

                // Create wind spiral
                for (double angle = 0; angle < Math.PI * 4; angle += Math.PI / 8) {
                    double radius = angle / (Math.PI * 4) * 1.5;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spiralLoc = location.clone().add(x, angle * 0.1, z);
                    world.spawnParticle(Particle.CLOUD, spiralLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 1.6f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final gale burst
                new BukkitRunnable() {
                    private double radius = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 15;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        radius = 5.0 * progress;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, 0.1 + (progress * 2), z);

                            // Wind particles
                            world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.SWEEP_ATTACK, burstLoc, 1, 0, 0, 0, 0);
                            }
                        }

                        // Finishing sounds
                        if (ticks % 5 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 1.2f + (float)progress);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));

        // Add cooldown
      //  addCooldown(player, "SeventhForm", 16);
    }

    /**
     * Helper class for the Seventh Form's gale slashes
     */
    private static class GaleSlash {

        private final Random random = new Random();

        private Location currentLocation;
        private Vector direction;
        private double duration;
        private double size;
        private double speed = 0.8;

        public GaleSlash(Location start, Vector direction, double duration, double size) {
            this.currentLocation = start;
            this.direction = direction;
            this.duration = duration;
            this.size = size;
        }

        public void update() {
            // Move gale forward
            currentLocation.add(direction.clone().multiply(speed));

            // Create gale effects
            createGaleEffects();

            // Reduce duration
            duration -= 0.05;
        }

        private void createGaleEffects() {
            World world = currentLocation.getWorld();

            // Create circular wind effect
            for (double angle = 0; angle < 360; angle += 30) {
                double radian = Math.toRadians(angle);
                double x = Math.cos(radian) * size;
                double z = Math.sin(radian) * size;

                Location effectLoc = currentLocation.clone().add(x, 0, z);

                // Wind particles
                world.spawnParticle(Particle.CLOUD, effectLoc, 1, 0.1, 0.1, 0.1, 0.05);

                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, effectLoc, 1, 0, 0, 0, 0);
                }
            }

            // Occasional sound
            if (random.nextFloat() < 0.1) {
                world.playSound(currentLocation, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 1.6f);
            }
        }

        public boolean isDead() {
            return duration <= 0;
        }
    }

    /**
     * Eighth Form: Primary Gale Slash (捌ノ型 初烈風斬)
     * Creates instant circular wind torrents through aerial blade swings that slice opponents
     */
    public void useEighthForm(Player player) {
        player.sendMessage("§7風 §f捌ノ型 初烈風斬 §7(Eighth Form: Primary Gale Slash)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.6f);

        Set<Entity> hitEntities = new HashSet<>();
        List<CircularTorrent> activeTorrents = new ArrayList<>();
        AtomicBoolean hasLanded = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.5;
            private final double LEAP_DURATION = 0.3;
            private boolean hasLeaped = false;
            private Location peakLocation = null;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time < LEAP_DURATION) {
                    // Initial leap phase
                    executeLepPhase(currentLoc);
                } else if (!hasLeaped) {
                    // Execute main torrent attack
                    executeTorrentAttack(currentLoc);
                    hasLeaped = true;
                    peakLocation = currentLoc.clone();
                } else {
                    // Update and create torrent effects
                    updateTorrentEffects(currentLoc, world);
                }

                // Check for landing
                if (!hasLanded.get() && player.isOnGround()) {
                    hasLanded.set(true);
                    createLandingEffect(currentLoc);
                }

                // Update active torrents
                updateTorrents();

                time += 0.05;
            }

            private void executeLepPhase(Location location) {
                double progress = time / LEAP_DURATION;

                // Create rising wind effect
                for (int i = 0; i < 12; i++) {
                    double angle = i * (Math.PI * 2 / 12) + (time * 15);
                    double radius = 1.2 + progress;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location windLoc = location.clone().add(x, progress * 2.5, z);

                    world.spawnParticle(Particle.CLOUD, windLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, windLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Apply upward velocity with forward momentum
                if (progress > 0.2) {
                    Vector direction = location.getDirection().multiply(0.8);
                    direction.setY(2.0 - progress);
                    player.setVelocity(direction);
                }

                // Rising sound
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f + (float)progress);
                }
            }

            private void executeTorrentAttack(Location location) {
                // Create initial burst
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.CLOUD, location, 40, 1.0, 1.0, 1.0, 0.2);

                // Create multiple circular torrents
                for (int i = 0; i < 3; i++) {
                    Vector direction = location.getDirection().clone();

                    // Create three layers of torrents
                    for (int layer = 0; layer < 3; layer++) {
                        CircularTorrent torrent = new CircularTorrent(
                                location.clone().add(0, layer * 1.5, 0),
                                direction,
                                0.7 + (i * 0.1), // Duration
                                2.5 + (i * 0.5), // Size
                                layer * 120 // Initial rotation
                        );
                        activeTorrents.add(torrent);
                    }
                }

                // Attack sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.4f);
            }

            private void updateTorrents() {
                Iterator<CircularTorrent> iterator = activeTorrents.iterator();
                while (iterator.hasNext()) {
                    CircularTorrent torrent = iterator.next();
                    torrent.update();

                    if (torrent.isDead()) {
                        iterator.remove();
                        continue;
                    }

                    // Check for hits
                    checkTorrentHits(torrent);
                }
            }

            private void checkTorrentHits(CircularTorrent torrent) {
                double hitRadius = torrent.size;

                for (Entity entity : world.getNearbyEntities(torrent.currentLocation, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply instant damage
                        target.damage(8.0, player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply spiral knockback
                        Vector knockback = torrent.direction.clone()
                                .multiply(1.2)
                                .add(new Vector(
                                        random.nextDouble() * 0.4 - 0.2,
                                        0.5,
                                        random.nextDouble() * 0.4 - 0.2
                                ));
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Slicing impact
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 4, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.CLOUD, location, 20, 0.3, 0.3, 0.3, 0.15);

                // Create wind cross
                for (int i = 0; i < 4; i++) {
                    double angle = i * Math.PI / 2;
                    Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));

                    for (double d = 0; d < 2; d += 0.2) {
                        Location sliceLoc = location.clone().add(direction.clone().multiply(d));
                        world.spawnParticle(Particle.CLOUD, sliceLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.4f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 2.0f);
            }

            private void createLandingEffect(Location location) {
                // Ground impact
                world.spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0);

                // Circular wind burst
                for (double radius = 0; radius < 4; radius += 0.2) {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location burstLoc = location.clone().add(x, 0.1, z);
                        world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Landing sounds
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.6f);
                world.playSound(location, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final wind dispersion
                new BukkitRunnable() {
                    private double radius = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        radius = 6.0 * progress;

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle);
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, 0.1 + (progress * 2), z);
                            world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);

                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.SWEEP_ATTACK, burstLoc, 1, 0, 0, 0, 0);
                            }
                        }

                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 1.6f + (float)progress);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 3, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 50, 0, false, false));

        // Add cooldown
       // addCooldown(player, "EighthForm", 15);
    }

    /**
     * Helper class for the Eighth Form's circular wind torrents
     */
    private static class CircularTorrent {
        private Location currentLocation;
        private Vector direction;
        private double duration;
        private double size;
        private double rotation;
        private double speed = 0.6;
        private final Random random = new Random();

        public CircularTorrent(Location start, Vector direction, double duration, double size, double initialRotation) {
            this.currentLocation = start;
            this.direction = direction;
            this.duration = duration;
            this.size = size;
            this.rotation = initialRotation;
        }

        public void update() {
            // Move torrent forward
            currentLocation.add(direction.clone().multiply(speed));

            // Create torrent effects
            createTorrentEffects();

            // Update rotation
            rotation += 15;

            // Reduce duration
            duration -= 0.05;
        }

        private void createTorrentEffects() {
            World world = currentLocation.getWorld();

            // Create circular wind effect
            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 30 + rotation);
                double x = Math.cos(angle) * size;
                double z = Math.sin(angle) * size;

                Location effectLoc = currentLocation.clone().add(x, 0, z);

                // Wind particles
                world.spawnParticle(Particle.CLOUD, effectLoc, 1, 0.1, 0.1, 0.1, 0.05);

                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, effectLoc, 1, 0, 0, 0, 0);
                }
            }

            // Occasional sound
            if (random.nextFloat() < 0.1) {
                world.playSound(currentLocation, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 2.0f);
            }
        }

        public boolean isDead() {
            return duration <= 0;
        }
    }

    private void updateTorrentEffects(Location currentLoc, World world) {
        // Create ongoing wind effects around the player
        for (int i = 0; i < 6; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 3.0;
            double x = Math.cos(angle) * radius;
            double y = random.nextDouble() * 2.0;
            double z = Math.sin(angle) * radius;

            Location effectLoc = currentLoc.clone().add(x, y, z);

            // Wind particles
            world.spawnParticle(Particle.CLOUD, effectLoc, 2, 0.1, 0.1, 0.1, 0.05);

            if (random.nextFloat() < 0.2) {
                world.spawnParticle(Particle.SWEEP_ATTACK, effectLoc, 1, 0, 0, 0, 0);
            }
        }

        // Create wind trail behind player
        Vector direction = currentLoc.getDirection();
        for (double d = 0; d < 2; d += 0.2) {
            Location trailLoc = currentLoc.clone().subtract(direction.clone().multiply(d));

            world.spawnParticle(Particle.CLOUD, trailLoc, 1, 0.1, 0.1, 0.1, 0.05);

            if (random.nextFloat() < 0.15) {
                world.spawnParticle(Particle.SWEEP_ATTACK, trailLoc, 1, 0, 0, 0, 0);
            }
        }

        // Add ambient sounds
        if (random.nextFloat() < 0.1) {
            world.playSound(currentLoc, Sound.ENTITY_PHANTOM_FLAP, 0.3f, 1.8f);
        }
    }

    /**
     * Ninth Form: Idaten Typhoon (玖ノ型 韋駄天台風)
     * Unleashes a powerful circular wind slash while performing an aerial backflip
     */
    public void useNinthForm(Player player) {
        player.sendMessage("§7風 §f玖ノ型 韋駄天台風 §7(Ninth Form: Idaten Typhoon)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial wind sound
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.6f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasReleasedTyphoon = new AtomicBoolean(false);
        AtomicReference<Location> typhoonCenter = new AtomicReference<>(null);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 2.0;
            private final double BACKFLIP_DURATION = 0.6;
            private double rotation = 0;
            private Vector initialDirection;

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                if (time == 0) {
                    initialDirection = currentLoc.getDirection().clone();
                }

                if (time < BACKFLIP_DURATION) {
                    // Execute backflip phase
                    executeBackflipPhase(currentLoc, time / BACKFLIP_DURATION);
                } else if (!hasReleasedTyphoon.get()) {
                    // Release typhoon at peak
                    releaseTyphoon(currentLoc);
                    hasReleasedTyphoon.set(true);
                    typhoonCenter.set(currentLoc.clone());
                } else {
                    // Update typhoon effects
                    updateTyphoonEffects(typhoonCenter.get());
                }

                time += 0.05;
                rotation += 18; // 360 degrees per second
            }

            private void executeBackflipPhase(Location location, double progress) {
                // Calculate backflip position
                double height = Math.sin(progress * Math.PI) * 3.0; // Peak height of 3 blocks

                // Apply upward and backward velocity
                Vector backflipVel = initialDirection.clone().multiply(-1.2); // Backward momentum
                backflipVel.setY(1.2 * (1 - progress)); // Upward momentum
                player.setVelocity(backflipVel);

                // Create trail effects
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2 / 8) + (rotation * Math.PI / 180);
                    double radius = 1.0 + progress;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location trailLoc = location.clone().add(x, height * 0.5, z);

                    // Wind particles
                    world.spawnParticle(Particle.CLOUD, trailLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, trailLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Backflip sound effects
                if (random.nextFloat() < 0.2) {
                    world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.8f + (float)progress);
                }
            }

            private void releaseTyphoon(Location location) {
                // Create initial typhoon burst
                world.spawnParticle(Particle.EXPLOSION, location, 2, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.CLOUD, location, 50, 1.0, 1.0, 1.0, 0.3);

                // Powerful sound effects
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2.0f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.5f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.7f);

                // Create expanding ring effect
                for (double radius = 0; radius < 8; radius += 0.5) {
                    for (double angle = 0; angle < 360; angle += 15) {
                        double radian = Math.toRadians(angle);
                        double x = Math.cos(radian) * radius;
                        double z = Math.sin(radian) * radius;

                        Location ringLoc = location.clone().add(x, 0, z);
                        world.spawnParticle(Particle.CLOUD, ringLoc, 1, 0.1, 0.1, 0.1, 0.1);
                    }
                }

                // Check for initial hits
                checkTyphoonHits(location, 8.0);
            }

            private void updateTyphoonEffects(Location center) {
                if (center == null) return;

                double progress = (time - BACKFLIP_DURATION) / (MAX_DURATION - BACKFLIP_DURATION);
                double currentRadius = 8.0 * (1 - progress * 0.5); // Slowly shrinking radius

                // Create rotating typhoon effect
                for (int layer = 0; layer < 3; layer++) {
                    double layerHeight = layer * 0.5;
                    double layerRadius = currentRadius * (1 - layer * 0.2);

                    for (double angle = 0; angle < 360; angle += 20) {
                        double radian = Math.toRadians(angle + rotation + (layer * 30));
                        double x = Math.cos(radian) * layerRadius;
                        double z = Math.sin(radian) * layerRadius;

                        Location typhoonLoc = center.clone().add(x, layerHeight, z);

                        // Typhoon particles
                        world.spawnParticle(Particle.CLOUD, typhoonLoc, 1, 0.1, 0.1, 0.1, 0.1);
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, typhoonLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }

                // Continuous damage check
                if (time % 0.2 < 0.05) { // Check every 0.2 seconds
                    checkTyphoonHits(center, currentRadius);
                }

                // Typhoon sound effects
                if (random.nextFloat() < 0.2) {
                    world.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 0.5f + (float)progress);
                }
            }

            private void checkTyphoonHits(Location center, double radius) {
                for (Entity entity : world.getNearbyEntities(center, radius, 4.0, radius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on distance from center
                        double distance = target.getLocation().distance(center);
                        double damage = 12.0 * (1 - distance / (radius + 2));

                        // Apply damage
                        target.damage(Math.max(6.0, damage), player);
                        hitEntities.add(target);

                        // Create hit effect
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Apply spiral knockback
                        Location targetLoc = target.getLocation();
                        Vector toTarget = targetLoc.toVector().subtract(center.toVector());
                        Vector knockback = toTarget.normalize()
                                .multiply(2.0)
                                .setY(0.8);
                        target.setVelocity(knockback);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Impact effects
                world.spawnParticle(Particle.EXPLOSION, location, 2, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 4, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.CLOUD, location, 15, 0.3, 0.3, 0.3, 0.2);

                // Create wind spiral
                for (double angle = 0; angle < Math.PI * 4; angle += Math.PI / 8) {
                    double radius = angle / (Math.PI * 4) * 2.0;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location spiralLoc = location.clone().add(x, angle * 0.1, z);
                    world.spawnParticle(Particle.CLOUD, spiralLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.8f, 0.6f);
            }

            private void createFinishingEffect() {
                Location endLoc = typhoonCenter.get() != null ? typhoonCenter.get() : player.getLocation();

                // Final typhoon dispersion
                new BukkitRunnable() {
                    private double radius = 0;
                    private int ticks = 0;
                    private final int MAX_TICKS = 20;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        double progress = (double)ticks / MAX_TICKS;
                        radius = 8.0 * (1 - progress);

                        for (double angle = 0; angle < 360; angle += 15) {
                            double radian = Math.toRadians(angle + (ticks * 10));
                            double x = Math.cos(radian) * radius;
                            double z = Math.sin(radian) * radius;

                            Location burstLoc = endLoc.clone().add(x, progress * 2, z);

                            // Dispersing particles
                            world.spawnParticle(Particle.CLOUD, burstLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            if (random.nextFloat() < 0.2) {
                                world.spawnParticle(Particle.SWEEP_ATTACK, burstLoc, 1, 0, 0, 0, 0);
                            }
                        }

                        // Finishing sounds
                        if (ticks % 4 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_PHANTOM_FLAP, 0.4f, 0.5f + (float)progress);
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 1, false, false));

        // Add cooldown
      //  addCooldown(player, "NinthForm", 18);
    }

}
