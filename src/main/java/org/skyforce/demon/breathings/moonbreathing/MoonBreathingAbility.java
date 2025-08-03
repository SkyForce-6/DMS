package org.skyforce.demon.breathings.moonbreathing;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Moon Breathing Implementation
 * Created: 2025-06-18 19:23:15
 * @author SkyForce-6
 *
 * Moon Breathing (月ノ呼吸, Tsuki no kokyū) is a Breathing Style derived from Sun Breathing.
 * It focuses on graceful movements mimicking the phases of the moon and utilizing its light.
 */
public class MoonBreathingAbility {
    private final Main plugin;
    private final Player player;
    private final Random random;

    // PhantomData als öffentliche statische Klasse
    public static class PhantomData {
        public Location location;
        public double angle;
        public Vector direction;

        public PhantomData(Location location, double angle, Vector direction) {
            this.location = location;
            this.angle = angle;
            this.direction = direction;
        }
    }


    public MoonBreathingAbility(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    /**
     * Helper method to add cooldown to abilities
     */
    protected void addCooldown(Player player, String technique, int seconds) {
        // Implementierung des Cooldown-Systems
    }


    /**
     * First Form: Dark Moon - Evening Palace (壱ノ型 暗月 宵の御所)
     * Created: 2025-06-18 19:25:52
     * @author SkyForce-6
     *
     * A crescent moon-shaped slash that combines speed and grace,
     * mimicking the appearance of the dark moon at dusk.
     */
    public void useFirstForm() {
        player.sendMessage("§9月 §f壱ノ型 暗月 宵の御所 §9(First Form: Dark Moon - Evening Palace)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();
        Vector direction = player.getLocation().getDirection();

        // Initial moon effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);

        // Store hit entities to prevent multiple hits
        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 30; // 1.5 seconds duration
            private double arcProgress = 0;
            private Location lastLocation = startLoc;
            private final double DASH_SPEED = 1.2;
            private final double DAMAGE = 12.0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                // Calculate movement
                arcProgress += Math.PI / 15;
                double horizontalOffset = Math.sin(arcProgress) * 2;

                // Move player in arc
                Vector moveDir = direction.clone().multiply(DASH_SPEED);
                Vector sideDir = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                Vector combined = moveDir.add(sideDir.multiply(horizontalOffset));

                player.setVelocity(combined);

                // Create slash effects
                Location currentLoc = player.getLocation();
                createMoonSlash(currentLoc, lastLocation);

                // Check for hits
                checkHits(currentLoc);

                lastLocation = currentLoc;
                ticks++;
            }

            private void createMoonSlash(Location current, Location last) {
                // Calculate slash path
                Vector between = current.toVector().subtract(last.toVector());
                double distance = between.length();
                Vector step = between.normalize().multiply(0.2);

                for (double d = 0; d < distance; d += 0.2) {
                    Location slashLoc = last.clone().add(step.clone().multiply(d));

                    // Create crescent moon effect
                    createCrescentEffect(slashLoc);

                    // Add moon particles
                    if (random.nextFloat() < 0.3) {
                        createMoonParticles(slashLoc);
                    }
                }
            }

            private void createCrescentEffect(Location loc) {
                // Main slash color (pale blue)
                Particle.DustOptions slashColor = new Particle.DustOptions(
                        Color.fromRGB(180, 200, 255), 1.2f);
                world.spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0, slashColor);

                // Moon energy (white)
                Particle.DustOptions moonColor = new Particle.DustOptions(
                        Color.fromRGB(255, 255, 255), 0.8f);
                world.spawnParticle(Particle.DUST, loc, 1, 0.2, 0.2, 0.2, 0, moonColor);

                // Sword trail
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                }
            }

            private void createMoonParticles(Location loc) {
                // Sparkling moon dust
                world.spawnParticle(Particle.END_ROD, loc, 2, 0.1, 0.1, 0.1, 0.02);

                // Moon glow
                if (random.nextFloat() < 0.15) {
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }

            private void checkHits(Location current) {
                for (Entity entity : current.getWorld().getNearbyEntities(current, 2, 2, 2)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply moonlight effect
                        applyMoonlightEffect(target);

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void applyMoonlightEffect(LivingEntity target) {
                // Apply weakness and glowing
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));

                // Moonlight visual effect
                new BukkitRunnable() {
                    private int particleTicks = 0;

                    @Override
                    public void run() {
                        if (particleTicks >= 20) {
                            this.cancel();
                            return;
                        }

                        Location targetLoc = target.getLocation();

                        // Create moonlight beam
                        for (double y = 0; y < 3; y += 0.2) {
                            Location beamLoc = targetLoc.clone().add(0, y, 0);

                            Particle.DustOptions beamColor = new Particle.DustOptions(
                                    Color.fromRGB(220, 220, 255), 0.7f);
                            world.spawnParticle(Particle.DUST, beamLoc, 1, 0.2, 0, 0.2, 0, beamColor);
                        }

                        particleTicks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void createHitEffect(Location loc) {
                // Impact particles
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.3, 0.3, 0.1);

                // Moon energy burst
                for (int i = 0; i < 12; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 0.8);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(180, 200, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.2f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add movement effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 1));

        // Add cooldown
        addCooldown(player, "FirstForm", 12);
    }

    /**
     * Second Form: Pearl Flower Moongazing (弐ノ型 珠華の見月)
     * Created: 2025-06-18 19:29:44
     * @author SkyForce-6
     *
     * A defensive technique that creates a sphere of moonlight,
     * blooming like a pearl flower while reflecting attacks.
     */
    public void useSecondForm() {
        player.sendMessage("§9月 §f弐ノ型 珠華の見月 §9(Second Form: Pearl Flower Moongazing)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial moonlight effects
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 0.8f, 1.8f);

        // Store deflected entities
        Set<Entity> deflectedEntities = new HashSet<>();
        AtomicInteger petalCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 80; // 4 seconds duration
            private double rotationAngle = 0;
            private final double SPHERE_RADIUS = 3.0;
            private final List<Vector> petalVectors = new ArrayList<>();
            private final double DAMAGE = 8.0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Update rotation
                rotationAngle += Math.PI / 16;

                // Generate petal positions if needed
                if (petalVectors.isEmpty()) {
                    generatePetalVectors();
                }

                // Create moonlight sphere
                createMoonSphere(playerLoc);

                // Create pearl flower petals
                createPearlFlower(playerLoc);

                // Check for incoming attacks/entities
                checkDeflection(playerLoc);

                ticks++;
            }

            private void generatePetalVectors() {
                // Create flower petal arrangement
                int petalSets = 3;
                int petalsPerSet = 5;

                for (int set = 0; set < petalSets; set++) {
                    double setAngle = (Math.PI * 2 * set) / petalSets;
                    double setHeight = 0.5 + set * 0.5;

                    for (int petal = 0; petal < petalsPerSet; petal++) {
                        double petalAngle = setAngle + ((Math.PI * 2 * petal) / petalsPerSet);

                        Vector petalVector = new Vector(
                                Math.cos(petalAngle) * SPHERE_RADIUS * 0.8,
                                setHeight,
                                Math.sin(petalAngle) * SPHERE_RADIUS * 0.8
                        );

                        petalVectors.add(petalVector);
                    }
                }
            }

            private void createMoonSphere(Location center) {
                // Create sphere surface
                for (int i = 0; i < 8; i++) {
                    double phi = Math.PI * 2 * random.nextDouble();
                    double theta = Math.PI * random.nextDouble();

                    double x = SPHERE_RADIUS * Math.sin(theta) * Math.cos(phi);
                    double y = SPHERE_RADIUS * Math.sin(theta) * Math.sin(phi);
                    double z = SPHERE_RADIUS * Math.cos(theta);

                    Location particleLoc = center.clone().add(x, y + 1, z);

                    // Moonlight barrier
                    Particle.DustOptions sphereColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, sphereColor);

                    // Sparkling effects
                    if (random.nextFloat() < 0.1) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void createPearlFlower(Location center) {
                // Rotate and display petals
                for (Vector petalBase : petalVectors) {
                    // Rotate petal position
                    Vector rotated = rotateVector(petalBase, rotationAngle);
                    Location petalLoc = center.clone().add(rotated).add(0, 1, 0);

                    // Create petal effect
                    createPetalEffect(petalLoc, rotated.normalize());
                }
            }

            private void createPetalEffect(Location loc, Vector direction) {
                // Pearl petal core
                Particle.DustOptions petalColor = new Particle.DustOptions(
                        Color.fromRGB(255, 255, 255), 1.0f);
                world.spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0, petalColor);

                // Petal glow
                Particle.DustOptions glowColor = new Particle.DustOptions(
                        Color.fromRGB(200, 220, 255), 0.7f);
                world.spawnParticle(Particle.DUST, loc, 1, 0.15, 0.15, 0.15, 0, glowColor);

                // Moonlight trails
                if (random.nextFloat() < 0.2) {
                    Location trailLoc = loc.clone().add(
                            direction.clone().multiply(0.3)
                    );
                    world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }

            private void checkDeflection(Location center) {
                for (Entity entity : center.getWorld().getNearbyEntities(center, SPHERE_RADIUS, SPHERE_RADIUS, SPHERE_RADIUS)) {
                    if ((entity instanceof Projectile || entity instanceof LivingEntity)
                            && entity != player
                            && !deflectedEntities.contains(entity)) {

                        // Calculate deflection
                        Vector awayVector = entity.getLocation().toVector()
                                .subtract(center.toVector())
                                .normalize()
                                .multiply(1.5);

                        if (entity instanceof Projectile) {
                            // Reflect projectiles
                            entity.setVelocity(awayVector);
                            deflectedEntities.add(entity);
                        } else if (entity instanceof LivingEntity) {
                            // Damage and knock back living entities
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(DAMAGE, player);
                            target.setVelocity(awayVector.setY(0.2));
                            deflectedEntities.add(target);

                            // Apply moonlight effect
                            applyMoonlightEffect(target);
                        }

                        // Create deflection effect
                        createDeflectionEffect(entity.getLocation());
                    }
                }
            }

            private void applyMoonlightEffect(LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            }

            private void createDeflectionEffect(Location loc) {
                // Deflection flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                // Pearl flower burst
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location burstLoc = loc.clone().add(spread);

                    // Pearl particles
                    Particle.DustOptions pearlColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, pearlColor);

                    // Moonlight sparkles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, burstLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Deflection sounds
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
            }

            private Vector rotateVector(Vector vec, double angle) {
                double x = vec.getX() * Math.cos(angle) - vec.getZ() * Math.sin(angle);
                double z = vec.getX() * Math.sin(angle) + vec.getZ() * Math.cos(angle);
                return new Vector(x, vec.getY(), z);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add defensive effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));

        // Add cooldown
        addCooldown(player, "SecondForm", 18);
    }

    /**
     * Third Form: Loathsome Moon - Chains (参ノ型 厭忌月の鎖)
     * Created: 2025-06-18 19:32:30
     * @author SkyForce-6
     *
     * A technique that creates chains of moonlight to bind and damage opponents,
     * restricting their movement while dealing continuous damage.
     */
    public void useThirdForm() {
        player.sendMessage("§9月 §f参ノ型 厭忌月の鎖 §9(Third Form: Loathsome Moon - Chains)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial chain creation effects
        world.playSound(startLoc, Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);

        // Store bound entities
        Map<LivingEntity, List<Location>> boundEntities = new HashMap<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 100; // 5 seconds duration
            private final double BIND_RADIUS = 12.0;
            private final double CHAIN_DAMAGE = 2.0;
            private final Map<LivingEntity, Integer> bindDurations = new HashMap<>();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    // Release all bound entities
                    releaseBoundEntities();
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Check for new entities to bind
                if (ticks % 10 == 0) {
                    checkForNewTargets(playerLoc);
                }

                // Update chain effects and damage
                updateChainEffects();

                ticks++;
            }

            private void checkForNewTargets(Location center) {
                for (Entity entity : center.getWorld().getNearbyEntities(center, BIND_RADIUS, BIND_RADIUS, BIND_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player && !boundEntities.containsKey(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Create binding points around entity
                        List<Location> chainPoints = createChainPoints(target.getLocation());
                        boundEntities.put(target, chainPoints);
                        bindDurations.put(target, 60); // 3 seconds bind duration

                        // Initial bind effect
                        createBindEffect(target);
                    }
                }
            }

            private List<Location> createChainPoints(Location targetLoc) {
                List<Location> points = new ArrayList<>();
                int chainCount = 4 + random.nextInt(3); // 4-6 chains

                for (int i = 0; i < chainCount; i++) {
                    double angle = (Math.PI * 2 * i) / chainCount;
                    double radius = 1.5;

                    Location chainStart = targetLoc.clone().add(
                            Math.cos(angle) * radius,
                            2.0,
                            Math.sin(angle) * radius
                    );

                    points.add(chainStart);
                }

                return points;
            }

            private void updateChainEffects() {
                Iterator<Map.Entry<LivingEntity, List<Location>>> it = boundEntities.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<LivingEntity, List<Location>> entry = it.next();
                    LivingEntity target = entry.getKey();
                    List<Location> chainPoints = entry.getValue();

                    if (!target.isValid() || target.isDead()) {
                        it.remove();
                        continue;
                    }

                    // Update bind duration
                    int duration = bindDurations.get(target) - 1;
                    if (duration <= 0) {
                        it.remove();
                        bindDurations.remove(target);
                        continue;
                    }
                    bindDurations.put(target, duration);

                    // Apply chain effects
                    Location targetLoc = target.getLocation();
                    createChainEffects(targetLoc, chainPoints);

                    // Apply damage and restrictions
                    if (ticks % 20 == 0) { // Damage every second
                        applyChainEffects(target);
                    }
                }
            }

            private void createChainEffects(Location targetLoc, List<Location> chainPoints) {
                for (Location chainStart : chainPoints) {
                    // Update chain start position relative to target
                    Vector offset = chainStart.clone().subtract(targetLoc).toVector();
                    Location currentStart = targetLoc.clone().add(offset);

                    // Create chain to ground
                    Location groundEnd = currentStart.clone();
                    groundEnd.setY(groundEnd.getBlockY());

                    drawMoonChain(currentStart, groundEnd);
                }
            }

            private void drawMoonChain(Location start, Location end) {
                Vector direction = end.clone().subtract(start).toVector();
                double distance = direction.length();
                direction.normalize();

                // Create chain segments
                for (double d = 0; d < distance; d += 0.2) {
                    Location chainLoc = start.clone().add(direction.clone().multiply(d));

                    // Chain core
                    Particle.DustOptions chainColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.7f);
                    world.spawnParticle(Particle.DUST, chainLoc, 1, 0.05, 0.05, 0.05, 0, chainColor);

                    // Moon energy
                    if (random.nextFloat() < 0.1) {
                        Particle.DustOptions moonColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.5f);
                        world.spawnParticle(Particle.DUST, chainLoc, 1, 0.1, 0.1, 0.1, 0, moonColor);
                    }

                    // Sparkle effects
                    if (random.nextFloat() < 0.05) {
                        world.spawnParticle(Particle.END_ROD, chainLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void applyChainEffects(LivingEntity target) {
                // Apply damage
                target.damage(CHAIN_DAMAGE, player);

                // Restrict movement
                target.setVelocity(new Vector(0, -0.1, 0));

                // Apply effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));

                // Chain constriction effect
                createConstrictionEffect(target.getLocation());
            }

            private void createBindEffect(LivingEntity target) {
                Location loc = target.getLocation();

                // Initial bind flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                // Moon energy burst
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Bind sounds
                world.playSound(loc, Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 0.8f);
            }

            private void createConstrictionEffect(Location loc) {
                // Constriction particles
                world.spawnParticle(Particle.CRIT, loc, 5, 0.3, 0.5, 0.3, 0.1);

                // Constriction sounds
                world.playSound(loc, Sound.BLOCK_CHAIN_HIT, 0.6f, 1.2f);
                world.playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.4f, 1.5f);
            }

            private void releaseBoundEntities() {
                for (LivingEntity target : boundEntities.keySet()) {
                    if (target.isValid()) {
                        // Release effect
                        Location loc = target.getLocation();
                        world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.5, 0.3, 0.1);
                        world.playSound(loc, Sound.BLOCK_CHAIN_BREAK, 1.0f, 1.2f);
                    }
                }
                boundEntities.clear();
                bindDurations.clear();
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add cast effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));

        // Add cooldown
        addCooldown(player, "ThirdForm", 22);
    }

    /**
     * Fourth Form: Mirror of Misfortune - Moonlit (肆ノ型 厄映え月)
     * Created: 2025-06-18 19:36:15
     * @author SkyForce-6
     *
     * A technique that creates mirror-like reflections of moonlight,
     * confusing enemies and striking from multiple angles.
     */
    public void useFourthForm() {
        player.sendMessage("§9月 §f肆ノ型 厄映え月 §9(Fourth Form: Mirror of Misfortune - Moonlit)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial mirror creation effects
        world.playSound(startLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);

        // Store mirror locations and hit entities
        List<Location> mirrorLocations = new ArrayList<>();
        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final int MIRROR_COUNT = 8;
            private final double MIRROR_RADIUS = 5.0;
            private final double DAMAGE = 8.0;
            private double rotationAngle = 0;
            private boolean attackPhase = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    // Final mirror shatter effect
                    shatterAllMirrors();
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Initialize mirrors if needed
                if (mirrorLocations.isEmpty()) {
                    initializeMirrors(playerLoc);
                }

                // Update mirror positions and effects
                updateMirrors(playerLoc);

                // Check for attack phase
                if (ticks >= MAX_TICKS / 2 && !attackPhase) {
                    attackPhase = true;
                    beginAttackPhase();
                }

                ticks++;
            }

            private void initializeMirrors(Location center) {
                for (int i = 0; i < MIRROR_COUNT; i++) {
                    double angle = (Math.PI * 2 * i) / MIRROR_COUNT;

                    Location mirrorLoc = center.clone().add(
                            Math.cos(angle) * MIRROR_RADIUS,
                            1.5,
                            Math.sin(angle) * MIRROR_RADIUS
                    );

                    mirrorLocations.add(mirrorLoc);
                    createMirrorFormationEffect(mirrorLoc);
                }
            }

            private void updateMirrors(Location center) {
                rotationAngle += Math.PI / 32;

                for (int i = 0; i < mirrorLocations.size(); i++) {
                    double angle = rotationAngle + ((Math.PI * 2 * i) / MIRROR_COUNT);

                    Location newLoc = center.clone().add(
                            Math.cos(angle) * MIRROR_RADIUS,
                            1.5 + Math.sin(ticks * 0.1) * 0.5,
                            Math.sin(angle) * MIRROR_RADIUS
                    );

                    mirrorLocations.set(i, newLoc);
                    createMirrorEffect(newLoc, angle);
                }
            }

            private void createMirrorEffect(Location loc, double angle) {
                // Mirror frame
                for (int i = 0; i < 8; i++) {
                    double frameAngle = (Math.PI * 2 * i) / 8;
                    Vector offset = new Vector(
                            Math.cos(frameAngle) * 0.8,
                            Math.sin(frameAngle) * 1.2,
                            0
                    );
                    offset = rotateVector(offset, angle);

                    Location frameLoc = loc.clone().add(offset);

                    Particle.DustOptions frameColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, frameLoc, 1, 0, 0, 0, 0, frameColor);
                }

                // Mirror surface
                Particle.DustOptions surfaceColor = new Particle.DustOptions(
                        Color.fromRGB(240, 240, 255), 1.2f);
                world.spawnParticle(Particle.DUST, loc, 3, 0.3, 0.5, 0.3, 0, surfaceColor);

                // Moonlight reflection
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.END_ROD, loc, 2, 0.2, 0.3, 0.2, 0.02);
                }
            }

            private void createMirrorFormationEffect(Location loc) {
                // Formation burst
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Formation sound
                world.playSound(loc, Sound.BLOCK_GLASS_PLACE, 0.8f, 1.2f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.5f);
            }

            private void beginAttackPhase() {
                for (Location mirrorLoc : mirrorLocations) {
                    // Mirror activation effect
                    world.spawnParticle(Particle.FLASH, mirrorLoc, 1, 0, 0, 0, 0);
                    world.playSound(mirrorLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 2.0f);
                }

                // Start mirror attacks
                new BukkitRunnable() {
                    private int attackTicks = 0;
                    private final int MAX_ATTACK_TICKS = 20;

                    @Override
                    public void run() {
                        if (attackTicks >= MAX_ATTACK_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Random mirror attacks
                        for (int i = 0; i < 2; i++) {
                            if (!mirrorLocations.isEmpty()) {
                                Location attackMirror = mirrorLocations.get(random.nextInt(mirrorLocations.size()));
                                performMirrorAttack(attackMirror);
                            }
                        }

                        attackTicks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void performMirrorAttack(Location mirrorLoc) {
                Vector attackDir = player.getLocation().getDirection();
                Location targetLoc = mirrorLoc.clone().add(attackDir.multiply(MIRROR_RADIUS));

                // Create moonlight beam
                drawMoonBeam(mirrorLoc, targetLoc);

                // Check for hits
                for (Entity entity : world.getNearbyEntities(targetLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply confusion effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));

                        // Hit effect
                        createMirrorHitEffect(target.getLocation());
                    }
                }
            }

            private void drawMoonBeam(Location start, Location end) {
                Vector direction = end.clone().subtract(start).toVector();
                double distance = direction.length();
                direction.normalize();

                for (double d = 0; d < distance; d += 0.2) {
                    Location beamLoc = start.clone().add(direction.multiply(d));

                    // Beam core
                    Particle.DustOptions beamColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, beamLoc, 1, 0.05, 0.05, 0.05, 0, beamColor);

                    // Beam aura
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, beamLoc, 1, 0.05, 0.05, 0.05, 0.02);
                    }
                }

                // Beam sound
                world.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 2.0f);
            }

            private void createMirrorHitEffect(Location loc) {
                // Impact burst
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.END_ROD, loc, 10, 0.3, 0.3, 0.3, 0.1);

                // Mirror shards
                for (int i = 0; i < 8; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location shardLoc = loc.clone().add(spread);

                    Particle.DustOptions shardColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 0.6f);
                    world.spawnParticle(Particle.DUST, shardLoc, 1, 0, 0, 0, 0, shardColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
            }

            private void shatterAllMirrors() {
                for (Location mirrorLoc : mirrorLocations) {
                    // Shatter effect
                    world.spawnParticle(Particle.FLASH, mirrorLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, mirrorLoc, 20, 0.5, 0.5, 0.5, 0.1);

                    // Glass shards
                    for (int i = 0; i < 12; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 2);

                        Location shardLoc = mirrorLoc.clone().add(spread);

                        Particle.DustOptions shardColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, shardLoc, 1, 0, 0, 0, 0, shardColor);
                    }

                    // Shatter sounds
                    world.playSound(mirrorLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                    world.playSound(mirrorLoc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.8f, 1.2f);
                }
            }

            private Vector rotateVector(Vector vec, double angle) {
                double x = vec.getX() * Math.cos(angle) - vec.getZ() * Math.sin(angle);
                double z = vec.getX() * Math.sin(angle) + vec.getZ() * Math.cos(angle);
                return new Vector(x, vec.getY(), z);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add cast effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));

        // Add cooldown
        addCooldown(player, "FourthForm", 20);
    }

    /**
     * Fifth Form: Moon Spirit Awakening (伍ノ型 月魄覚醒)
     * Created: 2025-06-18 19:51:16
     * @author SkyForce-6
     *
     * A technique that awakens the spirit of the moon within the user,
     * creating phantom images that strike simultaneously from multiple angles.
     */
    public void useFifthForm() {
        player.sendMessage("§9月 §f伍ノ型 月魄覚醒 §9(Fifth Form: Moon Spirit Awakening)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial awakening effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.5f);

        // Store phantom data and hit entities
        final List<MoonBreathingAbility.PhantomData> phantoms = new ArrayList<>();
        final Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 80; // 4 seconds duration
            private final int PHANTOM_COUNT = 4;
            private final double DAMAGE = 6.0;
            private double awakenedPower = 0.0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    // Final spirit dispersion
                    dispersePhantoms();
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Initialize phantoms if needed
                if (phantoms.isEmpty()) {
                    initializePhantoms(playerLoc);
                }

                // Update phantom movements and effects
                updatePhantoms(playerLoc);

                // Increase awakened power
                if (awakenedPower < 1.0) {
                    awakenedPower += 0.05;
                }

                ticks++;
            }

            private void initializePhantoms(Location center) {
                for (int i = 0; i < PHANTOM_COUNT; i++) {
                    double angle = (Math.PI * 2 * i) / PHANTOM_COUNT;
                    Vector offset = new Vector(
                            Math.cos(angle) * 3,
                            0,
                            Math.sin(angle) * 3
                    );

                    MoonBreathingAbility.PhantomData phantom = new MoonBreathingAbility.PhantomData(
                            center.clone().add(offset),
                            angle,
                            player.getLocation().getDirection()
                    );

                    phantoms.add(phantom);

                    // Phantom formation effect
                    Location loc = phantom.location;
                    world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                    for (int j = 0; j < 15; j++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble());

                        Location burstLoc = loc.clone().add(spread);

                        Particle.DustOptions burstColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                    }

                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.5f);
                    world.playSound(loc, Sound.BLOCK_GLASS_PLACE, 0.8f, 1.2f);
                }
            }

            private void updatePhantoms(Location center) {
                for (MoonBreathingAbility.PhantomData phantom : phantoms) {
                    // Update phantom position
                    phantom.angle += Math.PI / 32;
                    phantom.direction = player.getLocation().getDirection();

                    Vector orbit = new Vector(
                            Math.cos(phantom.angle) * 3,
                            Math.sin(ticks * 0.1) * 0.5,
                            Math.sin(phantom.angle) * 3
                    );

                    phantom.location = center.clone().add(orbit);

                    // Create phantom effects
                    createPhantomEffect(phantom);

                    // Phantom attacks
                    if (ticks % 20 == 0) {
                        performPhantomAttack(phantom);
                    }
                }
            }

            private void createPhantomEffect(MoonBreathingAbility.PhantomData phantom) {
                Location loc = phantom.location;

                // Phantom body
                for (int i = 0; i < 6; i++) {
                    double height = i * 0.3;
                    Location bodyLoc = loc.clone().add(0, height, 0);

                    // Body particles
                    Particle.DustOptions bodyColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    200 + (int)(55 * awakenedPower),
                                    200 + (int)(55 * awakenedPower),
                                    255
                            ),
                            1.0f
                    );
                    world.spawnParticle(Particle.DUST, bodyLoc, 2, 0.1, 0.1, 0.1, 0, bodyColor);
                }

                // Spirit aura
                for (int i = 0; i < 3; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location auraLoc = loc.clone().add(spread);

                    world.spawnParticle(Particle.END_ROD, auraLoc, 1, 0, 0, 0, 0.02);
                }
            }

            private void performPhantomAttack(MoonBreathingAbility.PhantomData phantom) {
                Vector attackDir = phantom.direction.clone();
                Location targetLoc = phantom.location.clone().add(attackDir.multiply(4));

                // Create spirit strike effect
                drawSpiritStrike(phantom.location, targetLoc);

                // Check for hits
                for (Entity entity : world.getNearbyEntities(targetLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply enhanced damage based on awakened power
                        double totalDamage = DAMAGE * (1 + awakenedPower);
                        target.damage(totalDamage, player);
                        hitEntities.add(target);

                        // Apply spirit effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));

                        // Create hit effect
                        Location loc = target.getLocation();
                        world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.3, 0.3, 0.1);

                        for (int i = 0; i < 15; i++) {
                            Vector spread = new Vector(
                                    random.nextDouble() - 0.5,
                                    random.nextDouble() - 0.5,
                                    random.nextDouble() - 0.5
                            ).normalize().multiply(random.nextDouble());

                            Location burstLoc = loc.clone().add(spread);

                            Particle.DustOptions burstColor = new Particle.DustOptions(
                                    Color.fromRGB(220, 220, 255), 0.7f);
                            world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                        }

                        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.5f);
                        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.6f, 1.2f);
                    }
                }
            }

            private void drawSpiritStrike(Location start, Location end) {
                Vector direction = end.clone().subtract(start).toVector();
                double distance = direction.length();
                direction.normalize();

                for (double d = 0; d < distance; d += 0.2) {
                    Location strikeLoc = start.clone().add(direction.multiply(d));

                    // Spirit trail
                    Particle.DustOptions strikeColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, strikeLoc, 1, 0.05, 0.05, 0.05, 0, strikeColor);

                    // Spirit energy
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, strikeLoc, 1, 0.05, 0.05, 0.05, 0.02);
                    }
                }

                // Strike sound
                world.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 2.0f);
            }

            private void dispersePhantoms() {
                for (MoonBreathingAbility.PhantomData phantom : phantoms) {
                    Location loc = phantom.location;

                    // Spirit dispersion effect
                    world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, loc, 20, 0.3, 0.3, 0.3, 0.1);

                    // Dispersion particles
                    for (int i = 0; i < 15; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 2);

                        Location disperseLoc = loc.clone().add(spread);

                        Particle.DustOptions disperseColor = new Particle.DustOptions(
                                Color.fromRGB(200, 200, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, disperseLoc, 1, 0, 0, 0, 0, disperseColor);
                    }

                    // Dispersion sounds
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.8f, 1.5f);
                    world.playSound(loc, Sound.ENTITY_VEX_DEATH, 0.6f, 1.2f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add awakening effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));

        // Add cooldown
        addCooldown(player, "FifthForm", 25);
    }

    /**
     * Sixth Form: Moon Ring Dance (陸ノ型 月輪舞)
     * Created: 2025-06-18 19:55:28
     * @author SkyForce-6
     *
     * A technique that creates rings of moonlight around the user,
     * which can be manipulated for both offense and defense.
     */
    public void useSixthForm() {
        player.sendMessage("§9月 §f陸ノ型 月輪舞 §9(Sixth Form: Moon Ring Dance)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial ring formation effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.8f);

        // Store moon ring data and hit entities
        final List<Vector> ringVectors = new ArrayList<>();
        final Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 100; // 5 seconds duration
            private final int RING_COUNT = 3;
            private final double DAMAGE = 5.0;
            private double masterRingRotation = 0;
            private double[] ringRotations;
            private double[] ringHeights;
            private boolean offensive = false;

            @Override
            public void run() {
                if (ticks == 0) {
                    initializeRings();
                }

                if (ticks >= MAX_TICKS) {
                    disperseRings();
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Update ring positions and effects
                updateRings(playerLoc);

                // Check for sneak toggle
                if (player.isSneaking() && !offensive) {
                    offensive = true;
                    transformRings();
                } else if (!player.isSneaking() && offensive) {
                    offensive = false;
                    transformRings();
                }

                ticks++;
            }

            private void initializeRings() {
                ringRotations = new double[RING_COUNT];
                ringHeights = new double[RING_COUNT];

                // Create ring points
                for (int i = 0; i < 16; i++) {
                    double angle = (Math.PI * 2 * i) / 16;
                    ringVectors.add(new Vector(Math.cos(angle), 0, Math.sin(angle)));
                }

                // Initialize ring properties
                for (int i = 0; i < RING_COUNT; i++) {
                    ringRotations[i] = (Math.PI * 2 * i) / RING_COUNT;
                    ringHeights[i] = i * 0.5;
                }

                // Initial formation effect
                for (int i = 0; i < RING_COUNT; i++) {
                    Location ringLoc = startLoc.clone().add(0, 1 + i * 0.5, 0);
                    world.spawnParticle(Particle.FLASH, ringLoc, 1, 0, 0, 0, 0);
                    world.playSound(ringLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f + i * 0.2f);
                }
            }

            private void updateRings(Location center) {
                masterRingRotation += Math.PI / 64;

                for (int ring = 0; ring < RING_COUNT; ring++) {
                    ringRotations[ring] += (offensive ? Math.PI / 16 : Math.PI / 32) * (ring % 2 == 0 ? 1 : -1);

                    // Update ring height with wave motion
                    if (!offensive) {
                        ringHeights[ring] = ring * 0.5 + Math.sin(ticks * 0.1 + ring * Math.PI / 2) * 0.2;
                    }

                    double ringSize = offensive ? 3.0 - ring * 0.5 : 2.0;

                    // Create ring particles
                    for (Vector baseVector : ringVectors) {
                        Vector rotated = rotateVector(
                                rotateVector(baseVector, ringRotations[ring]),
                                masterRingRotation
                        );

                        Location particleLoc = center.clone().add(
                                rotated.getX() * ringSize,
                                ringHeights[ring],
                                rotated.getZ() * ringSize
                        );

                        // Ring particles
                        Particle.DustOptions ringColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, ringColor);

                        // Moon energy
                        if (random.nextFloat() < 0.1) {
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                        }

                        // Check for hits in offensive mode
                        if (offensive && ticks % 5 == 0) {
                            checkHits(particleLoc);
                        }
                    }
                }
            }

            private void checkHits(Location loc) {
                for (Entity entity : world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                // Moon energy burst
                for (int i = 0; i < 12; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.4f, 1.2f);
            }

            private void transformRings() {
                Location center = player.getLocation();

                // Transform effect
                for (int ring = 0; ring < RING_COUNT; ring++) {
                    Location ringLoc = center.clone().add(0, 1 + ring * 0.5, 0);

                    // Transform flash
                    world.spawnParticle(Particle.FLASH, ringLoc, 1, 0, 0, 0, 0);

                    // Transform particles
                    for (int i = 0; i < 20; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 2);

                        Location particleLoc = ringLoc.clone().add(spread);

                        Particle.DustOptions transformColor = new Particle.DustOptions(
                                Color.fromRGB(200, 200, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, transformColor);
                    }
                }

                // Transform sounds
                world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 2.0f);
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, offensive ? 1.2f : 1.8f);
            }

            private void disperseRings() {
                Location center = player.getLocation();

                for (int ring = 0; ring < RING_COUNT; ring++) {
                    Location ringLoc = center.clone().add(0, 1 + ring * 0.5, 0);

                    // Dispersion flash
                    world.spawnParticle(Particle.FLASH, ringLoc, 1, 0, 0, 0, 0);

                    // Dispersion particles
                    for (int i = 0; i < 30; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 3);

                        Location particleLoc = ringLoc.clone().add(spread);

                        Particle.DustOptions disperseColor = new Particle.DustOptions(
                                Color.fromRGB(200, 200, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, disperseColor);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Final dispersion sounds
                world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.8f, 1.2f);
            }

            private Vector rotateVector(Vector vec, double angle) {
                double x = vec.getX() * Math.cos(angle) - vec.getZ() * Math.sin(angle);
                double z = vec.getX() * Math.sin(angle) + vec.getZ() * Math.cos(angle);
                return new Vector(x, vec.getY(), z);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add cast effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));

        // Add cooldown
        addCooldown(player, "SixthForm", 20);
    }

    /**
     * Seventh Form: Mirror Step - Lunar Eclipse (漆ノ型 鏡歩・月蝕)
     * Created: 2025-06-18 20:01:13
     * @author SkyForce-6
     *
     * A technique that utilizes the moon's eclipse to create afterimages,
     * allowing for rapid movement and strikes from multiple angles.
     */
    public void useSeventhForm() {
        player.sendMessage("§9月 §f漆ノ型 鏡歩・月蝕 §9(Seventh Form: Mirror Step - Lunar Eclipse)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial eclipse formation effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.7f);

        // Store afterimage data and hit entities
        final List<Location> afterimages = new ArrayList<>();
        final Set<Entity> hitEntities = new HashSet<>();
        final Location[] lastPosition = {startLoc};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 120; // 6 seconds duration
            private final double DAMAGE = 4.0;
            private final int MAX_AFTERIMAGES = 5;
            private boolean eclipseActive = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    disperseAfterimages();
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Check for significant movement
                if (lastPosition[0].distance(currentLoc) > 0.5) {
                    createAfterimage(lastPosition[0]);
                    lastPosition[0] = currentLoc;
                }

                // Update afterimages and effects
                updateAfterimages();

                // Check for eclipse activation
                if (player.isSneaking() && !eclipseActive && ticks > 20) {
                    activateEclipse();
                }

                ticks++;
            }

            private void createAfterimage(Location loc) {
                afterimages.add(loc.clone());

                // Limit afterimage count
                while (afterimages.size() > MAX_AFTERIMAGES) {
                    afterimages.remove(0);
                }

                // Afterimage formation effect
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                for (int i = 0; i < 10; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location particleLoc = loc.clone().add(spread);

                    Particle.DustOptions afterimageColor = new Particle.DustOptions(
                            Color.fromRGB(180, 180, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, afterimageColor);
                }

                // Formation sound
                world.playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.4f, 1.5f);
            }

            private void updateAfterimages() {
                Iterator<Location> it = afterimages.iterator();
                int index = 0;

                while (it.hasNext()) {
                    Location afterimageLoc = it.next();
                    index++;

                    // Create afterimage effect
                    createAfterimageEffect(afterimageLoc, index);

                    // Check for hits if eclipse is active
                    if (eclipseActive && ticks % 10 == 0) {
                        performAfterimageAttack(afterimageLoc);
                    }
                }
            }

            private void createAfterimageEffect(Location loc, int age) {
                // Afterimage body
                for (int i = 0; i < 8; i++) {
                    double height = i * 0.25;
                    Location bodyLoc = loc.clone().add(0, height, 0);

                    // Fade based on age
                    float alpha = 1.0f - (age / (float)MAX_AFTERIMAGES);

                    Particle.DustOptions bodyColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    (int)(180 * alpha + 75),
                                    (int)(180 * alpha + 75),
                                    255
                            ),
                            0.8f
                    );

                    world.spawnParticle(Particle.DUST, bodyLoc, 1, 0.1, 0, 0.1, 0, bodyColor);
                }

                // Moon energy
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0.2, 0.5, 0.2, 0.02);
                }
            }

            private void performAfterimageAttack(Location loc) {
                // Create attack effect
                Vector attackDir = player.getLocation().getDirection();
                Location targetLoc = loc.clone().add(attackDir.multiply(2));

                // Draw attack line
                Vector line = targetLoc.clone().subtract(loc).toVector();
                double length = line.length();
                line.normalize();

                for (double d = 0; d < length; d += 0.2) {
                    Location strikePoint = loc.clone().add(line.clone().multiply(d));

                    Particle.DustOptions strikeColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.6f);
                    world.spawnParticle(Particle.DUST, strikePoint, 1, 0, 0, 0, 0, strikeColor);
                }

                // Check for hits
                for (Entity entity : world.getNearbyEntities(targetLoc, 1.0, 1.0, 1.0)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                // Moon energy burst
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 0.7f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.4f, 1.2f);
            }

            private void activateEclipse() {
                eclipseActive = true;
                Location center = player.getLocation();

                // Eclipse activation effect
                world.spawnParticle(Particle.FLASH, center, 2, 0, 0, 0, 0);

                for (int i = 0; i < 36; i++) {
                    double angle = (Math.PI * 2 * i) / 36;
                    Vector offset = new Vector(
                            Math.cos(angle) * 3,
                            0,
                            Math.sin(angle) * 3
                    );

                    Location eclipseLoc = center.clone().add(offset);

                    Particle.DustOptions eclipseColor = new Particle.DustOptions(
                            Color.fromRGB(100, 100, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, eclipseLoc, 1, 0, 0, 0, 0, eclipseColor);
                }

                // Eclipse sounds
                world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.5f);
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.7f);

                // Give player effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
            }

            private void disperseAfterimages() {
                for (Location loc : afterimages) {
                    // Dispersion effect
                    world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.5, 0.3, 0.1);

                    // Dispersion particles
                    for (int i = 0; i < 20; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 2);

                        Location disperseLoc = loc.clone().add(spread);

                        Particle.DustOptions disperseColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, disperseLoc, 1, 0, 0, 0, 0, disperseColor);
                    }

                    // Dispersion sounds
                    world.playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.6f, 0.7f);
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.4f, 1.2f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add initial effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 120, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));

        // Add cooldown
        addCooldown(player, "SeventhForm", 25);
    }

    /**
     * Eighth Form: Lunar Dragon Strike (八ノ型 月龍襲)
     * Created: 2025-06-18 20:09:14
     * @author SkyForce-6
     *
     * A technique that manifests the moon's power into a spectral dragon,
     * unleashing a devastating concentrated attack.
     */
    public void useEighthForm() {
        player.sendMessage("§9月 §f八ノ型 月龍襲 §9(Eighth Form: Lunar Dragon Strike)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial dragon formation effects
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);

        // Store dragon segments and hit entities
        final List<Vector> dragonPoints = new ArrayList<>();
        final Set<Entity> hitEntities = new HashSet<>();
        final Vector[] dragonDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double DAMAGE = 12.0;
            private boolean charging = true;
            private Location targetLocation = null;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    disperseDragon();
                    this.cancel();
                    return;
                }

                if (charging && ticks < 20) {
                    chargeDragon();
                } else if (charging && ticks == 20) {
                    charging = false;
                    launchDragon();
                } else if (!charging) {
                    updateDragon();
                }

                ticks++;
            }

            private void chargeDragon() {
                Location center = player.getLocation();
                dragonDirection[0] = player.getLocation().getDirection();

                // Charging particles
                double radius = 2.0 * (ticks / 20.0);
                for (int i = 0; i < 12; i++) {
                    double angle = (Math.PI * 2 * i) / 12;
                    Vector offset = new Vector(
                            Math.cos(angle) * radius,
                            1.5,
                            Math.sin(angle) * radius
                    );

                    Location chargeLoc = center.clone().add(offset);

                    // Converging particles
                    Vector toCenter = center.clone().subtract(chargeLoc).toVector().normalize().multiply(0.2);

                    Particle.DustOptions chargeColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, chargeLoc, 0, toCenter.getX(), toCenter.getY(), toCenter.getZ(), 0.5, chargeColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, chargeLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Charge sounds
                if (ticks % 5 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.5f + (ticks / 20.0f));
                }
            }

            private void launchDragon() {
                Location center = player.getLocation();
                Vector direction = dragonDirection[0];

                // Initialize dragon points
                dragonPoints.clear();
                for (int i = 0; i < 20; i++) {
                    dragonPoints.add(center.clone().add(direction.clone().multiply(-1)).toVector());
                }

                // Set target location
                targetLocation = center.clone().add(direction.multiply(20));

                // Launch effect
                world.spawnParticle(Particle.FLASH, center, 3, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.END_ROD, center, 30, 0.2, 0.2, 0.2, 0.5);

                // Launch sounds
                world.playSound(center, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.5f);
                world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.7f);
            }

            private void updateDragon() {
                if (dragonPoints.isEmpty() || targetLocation == null) return;

                // Update head position
                Vector headPos = dragonPoints.get(0);
                Vector toTarget = targetLocation.clone().subtract(headPos.toLocation(world)).toVector();

                if (toTarget.length() > 1) {
                    toTarget.normalize().multiply(1.5); // Speed
                    Vector newHead = headPos.clone().add(toTarget);
                    dragonPoints.add(0, newHead);

                    // Remove tail if too long
                    if (dragonPoints.size() > 20) {
                        dragonPoints.remove(dragonPoints.size() - 1);
                    }

                    // Create dragon particles
                    createDragonParticles();

                    // Check for hits
                    checkDragonHits(newHead.toLocation(world));
                }
            }

            private void createDragonParticles() {
                // Dragon body
                for (int i = 0; i < dragonPoints.size() - 1; i++) {
                    Vector current = dragonPoints.get(i);
                    Vector next = dragonPoints.get(i + 1);
                    Vector between = next.clone().subtract(current);
                    double length = between.length();
                    between.normalize();

                    // Body segments
                    for (double d = 0; d < length; d += 0.2) {
                        Location segmentLoc = current.clone().add(between.clone().multiply(d)).toLocation(world);

                        // Core
                        Particle.DustOptions coreColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.5f);
                        world.spawnParticle(Particle.DUST, segmentLoc, 1, 0, 0, 0, 0, coreColor);

                        // Aura
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, segmentLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }

                        // Dragon scales
                        if (random.nextFloat() < 0.1) {
                            Vector offset = new Vector(
                                    random.nextDouble() - 0.5,
                                    random.nextDouble() - 0.5,
                                    random.nextDouble() - 0.5
                            ).normalize().multiply(0.5);

                            Location scaleLoc = segmentLoc.clone().add(offset);

                            Particle.DustOptions scaleColor = new Particle.DustOptions(
                                    Color.fromRGB(180, 180, 255), 0.7f);
                            world.spawnParticle(Particle.DUST, scaleLoc, 1, 0, 0, 0, 0, scaleColor);
                        }
                    }
                }

                // Dragon head
                if (!dragonPoints.isEmpty()) {
                    Location headLoc = dragonPoints.get(0).toLocation(world);
                    world.spawnParticle(Particle.DRAGON_BREATH, headLoc, 3, 0.1, 0.1, 0.1, 0.02);
                }
            }

            private void checkDragonHits(Location loc) {
                for (Entity entity : world.getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));

                        // Create hit effect
                        createDragonHitEffect(target.getLocation());
                    }
                }
            }

            private void createDragonHitEffect(Location loc) {
                // Impact explosion
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);
                world.spawnParticle(Particle.DRAGON_BREATH, loc, 20, 0.3, 0.3, 0.3, 0.1);

                // Moon energy burst
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.2f);
            }

            private void disperseDragon() {
                if (dragonPoints.isEmpty()) return;

                for (Vector point : dragonPoints) {
                    Location loc = point.toLocation(world);

                    // Dispersion explosion
                    world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.DRAGON_BREATH, loc, 10, 0.3, 0.3, 0.3, 0.1);
                    world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.3, 0.3, 0.2);

                    // Dispersion particles
                    for (int i = 0; i < 20; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble() * 3);

                        Location disperseLoc = loc.clone().add(spread);

                        Particle.DustOptions disperseColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, disperseLoc, 1, 0, 0, 0, 0, disperseColor);
                    }

                    // Dispersion sounds
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.4f, 1.5f);
                }

                // Final dragon roar
                if (!dragonPoints.isEmpty()) {
                    Location finalLoc = dragonPoints.get(0).toLocation(world);
                    world.playSound(finalLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.7f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add cast effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));

        // Add cooldown
        addCooldown(player, "EighthForm", 30);
    }
    /**
     * Ninth Form: Moonlit Reality - Lunar Dreams (九ノ型 月光現実・月夢)
     * Created: 2025-06-18 20:13:54
     * @author SkyForce-6
     *
     * A technique that manipulates the boundary between reality and dreams,
     * creating a domain where the moon's influence distorts perception.
     */
    public void useNinthForm() {
        player.sendMessage("§9月 §f九ノ型 月光現実・月夢 §9(Ninth Form: Moonlit Reality - Lunar Dreams)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial domain formation effects
        world.playSound(startLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 0.7f);

        // Store domain data and affected entities
        final Set<Entity> affectedEntities = new HashSet<>();
        final List<Location> domainPoints = new ArrayList<>();
        final double DOMAIN_RADIUS = 8.0;

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 160; // 8 seconds duration
            private final double DAMAGE = 3.0;
            private double domainRotation = 0;
            private boolean domainEstablished = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    collapseDomain();
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();

                if (!domainEstablished && ticks < 40) {
                    establishDomain(center);
                } else {
                    domainEstablished = true;
                    updateDomain(center);
                }

                ticks++;
            }

            private void establishDomain(Location center) {
                double progress = ticks / 40.0;
                double currentRadius = DOMAIN_RADIUS * progress;

                // Domain formation particles
                for (int i = 0; i < 36; i++) {
                    double angle = (Math.PI * 2 * i) / 36;

                    // Ground circle
                    Vector groundOffset = new Vector(
                            Math.cos(angle) * currentRadius,
                            0,
                            Math.sin(angle) * currentRadius
                    );
                    Location groundLoc = center.clone().add(groundOffset);

                    Particle.DustOptions domainColor = new Particle.DustOptions(
                            Color.fromRGB(180, 180, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, groundLoc, 1, 0, 0, 0, 0, domainColor);

                    // Rising walls
                    for (double h = 0; h < currentRadius * 2; h += 0.5) {
                        Location wallLoc = groundLoc.clone().add(0, h, 0);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, wallLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Formation sounds
                if (ticks % 10 == 0) {
                    // Korrigiere die playSound Aufrufe durch Casting zu float
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.6f, 0.5f + ((float)progress * 0.5f));
                }

                // Store domain points
                if (ticks == 39) {
                    initializeDomainPoints(center);
                }
            }

            private void initializeDomainPoints(Location center) {
                domainPoints.clear();

                // Create dome shape
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 8) {
                    double y = Math.cos(phi) * DOMAIN_RADIUS;
                    double radius = Math.sin(phi) * DOMAIN_RADIUS;

                    for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 8) {
                        double x = Math.cos(theta) * radius;
                        double z = Math.sin(theta) * radius;

                        domainPoints.add(center.clone().add(x, y, z));
                    }
                }
            }

            private void updateDomain(Location center) {
                domainRotation += Math.PI / 64;

                // Update domain points
                for (int i = 0; i < domainPoints.size(); i++) {
                    Location point = domainPoints.get(i);
                    Vector toCenter = center.clone().subtract(point).toVector();

                    // Rotate point around center
                    double distance = toCenter.length();
                    toCenter.normalize();

                    double x = toCenter.getX() * Math.cos(domainRotation) - toCenter.getZ() * Math.sin(domainRotation);
                    double z = toCenter.getX() * Math.sin(domainRotation) + toCenter.getZ() * Math.cos(domainRotation);

                    Location newPoint = center.clone().add(
                            x * distance,
                            point.getY() - center.getY(),
                            z * distance
                    );

                    domainPoints.set(i, newPoint);

                    // Create domain particles
                    createDomainEffect(newPoint);
                }

                // Check for entities in domain
                for (Entity entity : world.getNearbyEntities(center, DOMAIN_RADIUS, DOMAIN_RADIUS, DOMAIN_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        if (!affectedEntities.contains(entity)) {
                            affectedEntities.add(entity);
                        }

                        // Apply domain effects
                        applyDomainEffects(target);
                    }
                }
            }

            private void createDomainEffect(Location loc) {
                // Domain barrier
                Particle.DustOptions barrierColor = new Particle.DustOptions(
                        Color.fromRGB(
                                180 + (int)(75 * Math.sin(domainRotation + ticks * 0.1)),
                                180 + (int)(75 * Math.sin(domainRotation + ticks * 0.1)),
                                255
                        ),
                        0.8f
                );
                world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, barrierColor);

                // Dream particles
                if (random.nextFloat() < 0.1) {
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0.02);
                }
            }

            private void applyDomainEffects(LivingEntity target) {
                // Periodic damage and effects
                if (ticks % 20 == 0) {
                    // Apply damage
                    target.damage(DAMAGE, player);

                    // Apply status effects
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));

                    // Create effect at target
                    Location targetLoc = target.getLocation();

                    // Dream impact effect
                    world.spawnParticle(Particle.FLASH, targetLoc, 1, 0, 0, 0, 0);

                    for (int i = 0; i < 15; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(random.nextDouble());

                        Location effectLoc = targetLoc.clone().add(spread);

                        Particle.DustOptions effectColor = new Particle.DustOptions(
                                Color.fromRGB(200, 200, 255), 0.7f);
                        world.spawnParticle(Particle.DUST, effectLoc, 1, 0, 0, 0, 0, effectColor);
                    }

                    // Effect sounds
                    world.playSound(targetLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.7f);
                }

                // Continuous disorientation effect
                if (ticks % 5 == 0) {
                    Location targetLoc = target.getLocation();
                    Vector offset = new Vector(
                            random.nextDouble() - 0.5,
                            0,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.1);

                    target.setVelocity(target.getVelocity().add(offset));
                }
            }

            private void collapseDomain() {
                Location center = player.getLocation();

                // Collapse effect
                for (Location point : domainPoints) {
                    Vector toCenter = center.clone().subtract(point).toVector().normalize();

                    // Collapsing particles
                    for (int i = 0; i < 5; i++) {
                        Location particleLoc = point.clone();

                        Particle.DustOptions collapseColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, particleLoc, 0,
                                toCenter.getX(), toCenter.getY(), toCenter.getZ(), 0.5, collapseColor);
                    }

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0.1);
                    }
                }

                // Final collapse effects
                world.spawnParticle(Particle.FLASH, center, 3, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.END_ROD, center, 50, 0.5, 0.5, 0.5, 0.3);

                // Collapse sounds
                world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 2.0f);
                world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.7f);

                // Clear effects from affected entities
                for (Entity entity : affectedEntities) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                        target.removePotionEffect(PotionEffectType.BLINDNESS);
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add cast effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 160, 0));

        // Add cooldown
        addCooldown(player, "NinthForm", 35);
    }

    /**
     * Tenth Form: Moonlit Divine Dance - Eclipse of Eternity (拾ノ型 月光神舞・永劫日蝕)
     * Created: 2025-06-18 20:18:25
     * @author SkyForce-6
     *
     * The ultimate technique of Moon Breathing, combining all aspects of the moon's power
     * into a divine dance that manifests the eternal eclipse's devastating force.
     */
    public void useTenthForm() {
        player.sendMessage("§9月 §f拾ノ型 月光神舞・永劫日蝕 §9(Tenth Form: Moonlit Divine Dance - Eclipse of Eternity)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial manifestation effects
        world.playSound(startLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 2.0f);

        // Store technique data and hit entities
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> eclipsePoints = new ArrayList<>();
        final double[] currentPhase = {0}; // 0: Charging, 1: Eclipse Rising, 2: Divine Dance
        final Vector[] lastDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200; // 10 seconds duration
            private final double DAMAGE = 15.0;
            private final double ECLIPSE_RADIUS = 12.0;
            private double rotationAngle = 0;
            private List<Vector> dancePositions = new ArrayList<>();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    finaleDivineEclipse();
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();

                // Update phase based on ticks
                if (ticks < 40) { // 2 seconds charging
                    chargeEclipsePower(center);
                } else if (ticks < 80) { // 2 seconds eclipse rising
                    raiseEternalEclipse(center);
                } else { // 6 seconds divine dance
                    performDivineDance(center);
                }

                ticks++;
            }

            private void chargeEclipsePower(Location center) {
                double progress = ticks / 40.0;

                // Charging particles
                for (int i = 0; i < 36; i++) {
                    double angle = (Math.PI * 2 * i) / 36;
                    Vector offset = new Vector(
                            Math.cos(angle) * (3 * progress),
                            progress * 2,
                            Math.sin(angle) * (3 * progress)
                    );

                    Location chargeLoc = center.clone().add(offset);

                    // Converging moon energy
                    Vector toCenter = center.clone().subtract(chargeLoc).toVector().normalize().multiply(0.2);

                    Particle.DustOptions chargeColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, chargeLoc, 0,
                            toCenter.getX(), toCenter.getY(), toCenter.getZ(), 0.5, chargeColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, chargeLoc, 1, 0, 0, 0, 0.05);
                    }
                }

                // Charging sounds
                if (ticks % 5 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.8f, 0.5f + ((float)progress * 0.5f));
                    world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.5f + ((float)progress * 1.0f));
                }
            }

            private void raiseEternalEclipse(Location center) {
                double progress = (ticks - 40) / 40.0;

                // Initialize eclipse points if needed
                if (eclipsePoints.isEmpty()) {
                    initializeEclipsePoints(center);
                }

                // Update eclipse points
                for (int i = 0; i < eclipsePoints.size(); i++) {
                    Location point = eclipsePoints.get(i);
                    double height = progress * 10;
                    point.setY(center.getY() + height);

                    // Eclipse formation particles
                    Particle.DustOptions eclipseColor = new Particle.DustOptions(
                            Color.fromRGB(100, 100, 255), 1.5f);
                    world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, eclipseColor);

                    // Energy streams
                    if (random.nextFloat() < 0.2) {
                        Vector stream = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble(),
                                random.nextDouble() - 0.5
                        ).normalize().multiply(2);

                        Location streamLoc = point.clone().add(stream);
                        world.spawnParticle(Particle.END_ROD, streamLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Eclipse rising sounds
                if (ticks % 10 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 0.5f + ((float)progress * 0.5f));
                }
            }

            private void performDivineDance(Location center) {
                rotationAngle += Math.PI / 32;

                // Update player position in divine dance
                if (ticks % 20 == 0) {
                    updateDancePosition(center);
                }

                // Create divine dance effects
                for (Vector dancePos : dancePositions) {
                    Location danceLoc = center.clone().add(dancePos);

                    // Divine trail
                    for (int i = 0; i < 5; i++) {
                        Vector spread = new Vector(
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5,
                                random.nextDouble() - 0.5
                        ).normalize().multiply(0.5);

                        Location trailLoc = danceLoc.clone().add(spread);

                        Particle.DustOptions trailColor = new Particle.DustOptions(
                                Color.fromRGB(200, 200, 255), 0.8f);
                        world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);
                    }

                    // Divine energy
                    world.spawnParticle(Particle.END_ROD, danceLoc, 2, 0.1, 0.1, 0.1, 0.02);
                }

                // Update eclipse effects
                updateEclipseEffects(center);

                // Check for hits
                if (ticks % 5 == 0) {
                    checkDivineHits(center);
                }
            }

            private void updateDancePosition(Location center) {
                Vector direction = player.getLocation().getDirection();
                Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                dancePositions.clear();

                // Create dance position pattern
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8 + rotationAngle;
                    Vector danceOffset = direction.clone().multiply(Math.cos(angle) * 3)
                            .add(perpendicular.multiply(Math.sin(angle) * 3));
                    dancePositions.add(danceOffset);
                }
            }

            private void updateEclipseEffects(Location center) {
                // Rotate eclipse points
                for (Location point : eclipsePoints) {
                    Vector toCenter = center.clone().subtract(point).toVector();
                    double distance = toCenter.length();
                    toCenter.normalize();

                    // Rotate around center
                    double x = toCenter.getX() * Math.cos(rotationAngle) - toCenter.getZ() * Math.sin(rotationAngle);
                    double z = toCenter.getX() * Math.sin(rotationAngle) + toCenter.getZ() * Math.cos(rotationAngle);

                    point.setX(center.getX() + x * distance);
                    point.setZ(center.getZ() + z * distance);

                    // Eclipse energy
                    Particle.DustOptions eclipseColor = new Particle.DustOptions(
                            Color.fromRGB(150, 150, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, eclipseColor);

                    if (random.nextFloat() < 0.1) {
                        world.spawnParticle(Particle.DRAGON_BREATH, point, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void checkDivineHits(Location center) {
                for (Entity entity : world.getNearbyEntities(center, ECLIPSE_RADIUS, ECLIPSE_RADIUS, ECLIPSE_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply divine damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply divine effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2));

                        // Create divine impact
                        createDivineImpact(target.getLocation());
                    }
                }
            }

            private void createDivineImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Divine burst
                for (int i = 0; i < 40; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 3);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, burstLoc, 1, 0, 0, 0, 0.05);
                    }
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.2f);
            }

            private void initializeEclipsePoints(Location center) {
                eclipsePoints.clear();

                // Create eclipse dome structure
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 12) {
                    double y = Math.cos(phi) * ECLIPSE_RADIUS;
                    double radius = Math.sin(phi) * ECLIPSE_RADIUS;

                    for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 12) {
                        double x = Math.cos(theta) * radius;
                        double z = Math.sin(theta) * radius;

                        eclipsePoints.add(center.clone().add(x, y, z));
                    }
                }
            }

            private void finaleDivineEclipse() {
                Location center = player.getLocation();

                // Final explosion effect
                world.spawnParticle(Particle.EXPLOSION, center, 3, 1, 1, 1, 0);

                for (Location point : eclipsePoints) {
                    // Divine collapse
                    Vector toCenter = center.clone().subtract(point).toVector().normalize();

                    for (int i = 0; i < 10; i++) {
                        Location collapseLoc = point.clone();

                        Particle.DustOptions collapseColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, collapseLoc, 0,
                                toCenter.getX(), toCenter.getY(), toCenter.getZ(), 0.8, collapseColor);

                        if (random.nextFloat() < 0.4) {
                            world.spawnParticle(Particle.END_ROD, collapseLoc, 1, 0, 0, 0, 0.1);
                        }
                    }
                }

                // Final divine burst
                for (int i = 0; i < 100; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * ECLIPSE_RADIUS);

                    Location burstLoc = center.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 0.9f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Final sounds
                world.playSound(center, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
                world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);

                // Clear effects from all affected entities
                for (Entity entity : hitEntities) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.removePotionEffect(PotionEffectType.LEVITATION);
                        target.removePotionEffect(PotionEffectType.GLOWING);
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add divine effects to user
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

        // Add cooldown
        addCooldown(player, "TenthForm", 45);
    }

    /**
     * Fourteenth Form: Catastrophe, Tenman Crescent Moon (拾じゅう肆しノ型かた 兇きょう変へん・天てん満まん繊せん月げつ)
     * Created: 2025-06-18 20:38:02
     * @author SkyForce-6
     *
     * A devastating technique that creates an omnidirectional vortex of expanding crescent moon blades,
     * with each subsequent slash growing larger than the last, forming a catastrophic dome of destruction.
     */
    public void useFourteenthForm() {
        player.sendMessage("§9月 §f拾じゅう肆しノ型かた 兇きょう変へん・天てん満まん繊せん月げつ §9(Fourteenth Form: Catastrophe, Tenman Crescent Moon)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial catastrophe effects
        world.playSound(startLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);

        // Store slash data and hit entities
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Vector> crescentVectors = new ArrayList<>();
        final double[] currentSize = {1.0}; // Starting size of crescents

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 140; // 7 seconds duration
            private final double DAMAGE = 18.0; // High damage due to catastrophic nature
            private double rotationAngle = 0;
            private int slashPhase = 0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    finaleCatastrophe();
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();

                // Update crescents every 2 ticks
                if (ticks % 2 == 0) {
                    createCrescentSlash(center);
                }

                // Update existing crescents
                updateCrescents(center);

                // Check for hits
                if (ticks % 3 == 0) {
                    checkCrescentHits(center);
                }

                // Increase size gradually
                if (ticks % 20 == 0) {
                    currentSize[0] = Math.min(currentSize[0] + 1.0, 8.0); // Max size of 8 blocks
                    slashPhase++;
                }

                ticks++;
            }

            private void createCrescentSlash(Location center) {
                // Create multiple crescents at different angles
                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 * i) / 4 + rotationAngle;

                    // Create crescent base vectors
                    Vector forward = new Vector(Math.cos(angle), 0, Math.sin(angle));
                    Vector right = new Vector(-forward.getZ(), 0, forward.getX());

                    // Add crescent points
                    for (double t = -Math.PI/3; t <= Math.PI/3; t += Math.PI/8) {
                        Vector crescentPoint = forward.clone().multiply(Math.cos(t))
                                .add(right.multiply(Math.sin(t)))
                                .multiply(currentSize[0]);

                        crescentVectors.add(crescentPoint);
                    }
                }

                // Rotate for next slash
                rotationAngle += Math.PI / 16;

                // Crescent creation sound
                world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
                world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.6f, 1.2f);
            }

            private void updateCrescents(Location center) {
                Iterator<Vector> it = crescentVectors.iterator();

                while (it.hasNext()) {
                    Vector vec = it.next();
                    Location crescentLoc = center.clone().add(vec);

                    // Create crescent particles
                    double particleSize = 0.8 + (slashPhase * 0.2);
                    Particle.DustOptions crescentColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), (float)particleSize);

                    world.spawnParticle(Particle.DUST, crescentLoc, 1, 0, 0, 0, 0, crescentColor);

                    // Add trail effects
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, crescentLoc, 1, 0, 0, 0, 0.02);
                    }

                    // Remove old crescents
                    if (random.nextFloat() < 0.1) {
                        it.remove();
                    }
                }
            }

            private void checkCrescentHits(Location center) {
                double hitRadius = currentSize[0] * 1.5;

                for (Entity entity : world.getNearbyEntities(center, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate distance-based damage
                        double distance = target.getLocation().distance(center);
                        double scaledDamage = DAMAGE * (1 - (distance / (hitRadius * 1.2)));

                        if (scaledDamage > 0) {
                            // Apply damage
                            target.damage(scaledDamage, player);
                            hitEntities.add(target);

                            // Apply effects
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));

                            // Create hit effect
                            createCrescentHitEffect(target.getLocation());
                        }
                    }
                }
            }

            private void createCrescentHitEffect(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Crescent shatter effect
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location shatterLoc = loc.clone().add(spread);

                    Particle.DustOptions shatterColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, shatterLoc, 1, 0, 0, 0, 0, shatterColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 0.7f);
            }

            private void finaleCatastrophe() {
                Location center = player.getLocation();

                // Final explosion effect
                world.spawnParticle(Particle.EXPLOSION, center, 5, 2, 2, 2, 0);

                // Create expanding ring of crescents
                for (int i = 0; i < 36; i++) {
                    double angle = (Math.PI * 2 * i) / 36;
                    Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));

                    for (double d = 0; d < 8; d += 0.5) {
                        Location crescentLoc = center.clone().add(direction.clone().multiply(d));

                        // Final crescent particles
                        Particle.DustOptions finalColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.2f);
                        world.spawnParticle(Particle.DUST, crescentLoc, 1, 0, 0, 0, 0, finalColor);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, crescentLoc, 1, 0, 0, 0, 0.05);
                        }
                    }
                }

                // Final sounds
                world.playSound(center, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);
                world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);

                // Clear effects from all affected entities
                for (Entity entity : hitEntities) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add catastrophic effects to user
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 140, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 140, 0));

        // Add cooldown
        addCooldown(player, "FourteenthForm", 50);
    }

    /**
     * Sixteenth Form: Eternal Moonlight Paradise (拾陸ノ型 永月楽土)
     * Created: 2025-06-18 21:02:38
     * @author SkyForce-6
     *
     * The ultimate and most refined form of Moon Breathing, creating a domain of pure moonlight
     * where reality itself bends to the eternal radiance of the moon.
     */
    public void useSixteenthForm() {
        player.sendMessage("§9月 §f拾陸ノ型 永月楽土 §9(Sixteenth Form: Eternal Moonlight Paradise)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial paradise manifestation effects
        world.playSound(startLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);

        // Store technique data
        final Set<Entity> affectedEntities = new HashSet<>();
        final List<Location> paradisePoints = new ArrayList<>();
        final double PARADISE_RADIUS = 15.0; // Largest radius of all forms

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 300; // 15 seconds duration
            private final double DAMAGE = 20.0; // Maximum damage potential
            private double rotationAngle = 0;
            private boolean paradiseEstablished = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    closeParadise();
                    this.cancel();
                    return;
                }

                Location center = player.getLocation();

                if (!paradiseEstablished && ticks < 60) {
                    manifestParadise(center);
                } else {
                    paradiseEstablished = true;
                    sustainParadise(center);
                }

                ticks++;
            }

            private void manifestParadise(Location center) {
                double progress = ticks / 60.0;
                double currentRadius = PARADISE_RADIUS * progress;

                // Create ascending moonlight pillars
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8;
                    Vector offset = new Vector(
                            Math.cos(angle) * currentRadius,
                            0,
                            Math.sin(angle) * currentRadius
                    );

                    Location pillarBase = center.clone().add(offset);

                    for (double h = 0; h < currentRadius * 2; h += 0.5) {
                        Location pillarLoc = pillarBase.clone().add(0, h, 0);

                        // Pillar particles
                        Particle.DustOptions pillarColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.5f);
                        world.spawnParticle(Particle.DUST, pillarLoc, 1, 0, 0, 0, 0, pillarColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, pillarLoc, 1, 0, 0, 0, 0.05);
                        }
                    }
                }

                // Manifestation sounds
                if (ticks % 10 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.8f, 0.5f + ((float)progress * 0.5f));
                    world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.8f);
                }

                // Initialize paradise points at completion
                if (ticks == 59) {
                    initializeParadisePoints(center);
                }
            }

            private void initializeParadisePoints(Location center) {
                paradisePoints.clear();

                // Create detailed paradise structure
                for (double phi = 0; phi < Math.PI; phi += Math.PI / 16) {
                    double y = Math.cos(phi) * PARADISE_RADIUS;
                    double radius = Math.sin(phi) * PARADISE_RADIUS;

                    for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 16) {
                        double x = Math.cos(theta) * radius;
                        double z = Math.sin(theta) * radius;

                        paradisePoints.add(center.clone().add(x, y, z));
                    }
                }
            }

            private void sustainParadise(Location center) {
                rotationAngle += Math.PI / 128;

                // Update paradise structure
                for (Location point : paradisePoints) {
                    Vector toCenter = center.clone().subtract(point).toVector();
                    double distance = toCenter.length();
                    toCenter.normalize();

                    // Rotate point
                    double x = toCenter.getX() * Math.cos(rotationAngle) - toCenter.getZ() * Math.sin(rotationAngle);
                    double z = toCenter.getX() * Math.sin(rotationAngle) + toCenter.getZ() * Math.cos(rotationAngle);

                    point.setX(center.getX() + x * distance);
                    point.setZ(center.getZ() + z * distance);

                    // Paradise barrier effects
                    createParadiseEffect(point);
                }

                // Check for entities in paradise
                if (ticks % 5 == 0) {
                    checkParadiseEffects(center);
                }

                // Create moonlight beams
                if (ticks % 20 == 0) {
                    createMoonlightBeams(center);
                }
            }

            private void createParadiseEffect(Location loc) {
                // Eternal moonlight particles
                Particle.DustOptions moonlightColor = new Particle.DustOptions(
                        Color.fromRGB(
                                200 + (int)(55 * Math.sin(rotationAngle + ticks * 0.05)),
                                200 + (int)(55 * Math.sin(rotationAngle + ticks * 0.05)),
                                255
                        ),
                        1.2f
                );
                world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, moonlightColor);

                // Paradise energy
                if (random.nextFloat() < 0.1) {
                    world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0.02);
                }

                if (random.nextFloat() < 0.05) {
                    world.spawnParticle(Particle.DRAGON_BREATH, loc, 1, 0, 0, 0, 0.02);
                }
            }

            private void createMoonlightBeams(Location center) {
                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 * i) / 4 + rotationAngle;
                    Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));

                    for (double d = 0; d < PARADISE_RADIUS; d += 0.5) {
                        Location beamLoc = center.clone().add(direction.clone().multiply(d));

                        // Beam particles
                        Particle.DustOptions beamColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, beamLoc, 1, 0, 0, 0, 0, beamColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, beamLoc, 1, 0.1, 0.1, 0.1, 0.02);
                        }
                    }

                    // Beam sound
                    Location beamEnd = center.clone().add(direction.multiply(PARADISE_RADIUS));
                    world.playSound(beamEnd, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4f, 1.5f);
                }
            }

            private void checkParadiseEffects(Location center) {
                for (Entity entity : world.getNearbyEntities(center, PARADISE_RADIUS, PARADISE_RADIUS, PARADISE_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        if (!affectedEntities.contains(entity)) {
                            affectedEntities.add(entity);
                        }

                        // Apply paradise effects
                        applyParadiseEffects(target);
                    }
                }
            }

            private void applyParadiseEffects(LivingEntity target) {
                // Periodic damage and effects
                if (ticks % 20 == 0) {
                    // Calculate distance-based damage
                    double distance = target.getLocation().distance(player.getLocation());
                    double scaledDamage = DAMAGE * (1 - (distance / (PARADISE_RADIUS * 1.2)));

                    if (scaledDamage > 0) {
                        // Apply damage
                        target.damage(scaledDamage, player);

                        // Apply divine effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

                        // Create impact effect
                        createParadiseImpact(target.getLocation());
                    }
                }
            }

            private void createParadiseImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Divine burst
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.4f, 1.2f);
            }

            private void closeParadise() {
                Location center = player.getLocation();

                // Final paradise collapse
                for (Location point : paradisePoints) {
                    Vector toCenter = center.clone().subtract(point).toVector().normalize();

                    // Collapse particles
                    for (int i = 0; i < 8; i++) {
                        Location collapseLoc = point.clone();

                        Particle.DustOptions collapseColor = new Particle.DustOptions(
                                Color.fromRGB(220, 220, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, collapseLoc, 0,
                                toCenter.getX(), toCenter.getY(), toCenter.getZ(), 0.8, collapseColor);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, collapseLoc, 1, 0, 0, 0, 0.1);
                        }
                    }
                }

                // Final divine burst
                for (int i = 0; i < 200; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * PARADISE_RADIUS);

                    Location burstLoc = center.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(200, 200, 255), 0.9f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Final sounds
                world.playSound(center, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
                world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.7f);
                world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.5f);

                // Clear effects from affected entities
                for (Entity entity : affectedEntities) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.removePotionEffect(PotionEffectType.LEVITATION);
                        target.removePotionEffect(PotionEffectType.GLOWING);
                        target.removePotionEffect(PotionEffectType.WEAKNESS);
                        target.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add paradise effects to user
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 300, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));

        // Add cooldown
        addCooldown(player, "SixteenthForm", 60);
    }
}