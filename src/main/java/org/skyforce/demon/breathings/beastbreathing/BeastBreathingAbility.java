package org.skyforce.demon.breathings.beastbreathing;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Beast Breathing Implementation
 * Created: 2025-06-17 17:24:23
 * @author SkyForce-6
 */
public class BeastBreathingAbility {
    private final Main plugin;
    private final Player player;
    private final Random random;

    public BeastBreathingAbility(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();
    }

    // Helper method for creating beast-like particle effects
    private void createBeastParticles(Location location, double size, Color color) {
        World world = location.getWorld();
        Particle.DustOptions beastDust = new Particle.DustOptions(color, 1.0f);

        // Create beast-like aura
        for (double t = 0; t < Math.PI * 2; t += 0.2) {
            double x = Math.cos(t) * size;
            double z = Math.sin(t) * size;
            Location particleLoc = location.clone().add(x, 0, z);

            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, beastDust);
        }
    }

    // Helper method for creating claw marks
    private void createClawMarks(Location location, Vector direction, double size, Color color) {
        World world = location.getWorld();
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // Create three parallel claw marks
        for (int i = -1; i <= 1; i++) {
            Vector offset = right.clone().multiply(i * 0.3);
            Location clawStart = location.clone().add(offset);

            // Create individual claw mark
            for (double d = 0; d < size; d += 0.1) {
                Location markLoc = clawStart.clone().add(direction.clone().multiply(d));
                Particle.DustOptions clawDust = new Particle.DustOptions(color, 0.7f);
                world.spawnParticle(Particle.DUST, markLoc, 1, 0.02, 0.02, 0.02, 0, clawDust);
            }
        }
    }

    // Helper method for beast roar effects
    private void createRoarEffect(Location location, double radius, Color color) {
        World world = location.getWorld();

        // Create expanding rings
        for (double r = 0; r < radius; r += 0.5) {
            for (double t = 0; t < Math.PI * 2; t += 0.2) {
                double x = Math.cos(t) * r;
                double z = Math.sin(t) * r;
                Location ringLoc = location.clone().add(x, r * 0.2, z);

                Particle.DustOptions roarDust = new Particle.DustOptions(color, 1.0f);
                world.spawnParticle(Particle.DUST, ringLoc, 1, 0.02, 0.02, 0.02, 0, roarDust);
            }
        }

        // Roar sound effects
        world.playSound(location, Sound.ENTITY_WOLF_GROWL, 1.0f, 0.8f);
        world.playSound(location, Sound.ENTITY_RAVAGER_ROAR, 0.5f, 1.2f);
    }

    // Helper method for beast movement particles
    private void createBeastMovementTrail(Location location, Vector direction, Color color) {
        World world = location.getWorld();

        // Create paw print effects
        for (int i = 0; i < 4; i++) {
            Vector offset = direction.clone().multiply(-i * 0.5);
            Location pawLoc = location.clone().add(offset);

            createPawPrint(pawLoc, direction, color);
        }
    }

    // Helper method for creating paw prints
    private void createPawPrint(Location location, Vector direction, Color color) {
        World world = location.getWorld();
        Particle.DustOptions pawDust = new Particle.DustOptions(color, 0.8f);

        // Main pad
        world.spawnParticle(Particle.DUST, location, 3, 0.1, 0, 0.1, 0, pawDust);

        // Toes
        double angle = Math.atan2(direction.getZ(), direction.getX());
        for (int i = 0; i < 4; i++) {
            double toeAngle = angle + (i - 1.5) * 0.3;
            double x = Math.cos(toeAngle) * 0.2;
            double z = Math.sin(toeAngle) * 0.2;

            Location toeLoc = location.clone().add(x, 0, z);
            world.spawnParticle(Particle.DUST, toeLoc, 2, 0.05, 0, 0.05, 0, pawDust);
        }
    }
    /**
     * First Fang: Pierce (壱ノ牙 穿ち抜き)
     * Created: 2025-06-17 17:26:44
     * @author SkyForce-6
     *
     * A powerful dual-blade neck pierce attack in the style of a beast's fangs
     */
    public void useFirstFang() {
        player.sendMessage("§6獣 §f壱ノ牙 穿ち抜き §6(First Fang: Pierce)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial beast stance
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.2f);
        createBeastParticles(startLoc.add(0, 1, 0), 0.5, Color.fromRGB(255, 165, 0));

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasStruck = new AtomicBoolean(false);

        new BukkitRunnable() {
            private double time = 0;
            private final double MAX_DURATION = 1.0;
            private int phase = 0; // 0: preparation, 1: lunge, 2: pierce
            private Location targetLocation = null;
            private final List<Location> leftFangPath = new ArrayList<>();
            private final List<Location> rightFangPath = new ArrayList<>();

            @Override
            public void run() {
                if (time >= MAX_DURATION) {
                    createFinishingEffect();
                    this.cancel();
                    return;
                }

                switch (phase) {
                    case 0:
                        prepareLunge();
                        break;
                    case 1:
                        executeLunge();
                        break;
                    case 2:
                        executePierce();
                        break;
                }

                time += 0.05;
            }

            private void prepareLunge() {
                if (time >= 0.2) {
                    phase = 1;
                    return;
                }

                // Beast preparation stance
                Location prepLoc = player.getLocation();
                Vector direction = prepLoc.getDirection();

                // Create beast aura
                createBeastParticles(prepLoc.add(0, 1, 0), 0.8, Color.fromRGB(255, 140, 0));

                // Create anticipation effects
                if (random.nextFloat() < 0.3) {
                    world.playSound(prepLoc, Sound.ENTITY_WOLF_AMBIENT, 0.5f, 1.5f);
                    createClawMarks(prepLoc, direction, 0.5, Color.fromRGB(255, 200, 0));
                }
            }

            private void executeLunge() {
                // Find nearest target
                if (targetLocation == null) {
                    Entity target = null;
                    double minDistance = Double.MAX_VALUE;

                    for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            double distance = player.getLocation().distance(entity.getLocation());
                            if (distance < minDistance) {
                                minDistance = distance;
                                target = entity;
                            }
                        }
                    }

                    if (target != null) {
                        targetLocation = target.getLocation();
                        // Launch player towards target
                        Vector direction = targetLocation.toVector()
                                .subtract(player.getLocation().toVector())
                                .normalize()
                                .multiply(1.2);
                        player.setVelocity(direction.setY(0.2));

                        // Lunge sound
                        world.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.2f);
                    } else {
                        phase = 2; // Skip to pierce if no target
                        return;
                    }
                }

                // Create lunge effects
                createBeastMovementTrail(player.getLocation(),
                        player.getLocation().getDirection(),
                        Color.fromRGB(255, 140, 0));

                // Check if close to target
                if (targetLocation != null &&
                        player.getLocation().distance(targetLocation) < 2.0) {
                    phase = 2;
                    calculateFangPaths();
                }
            }

            private void calculateFangPaths() {
                Location pierceStart = player.getLocation();
                Vector direction = pierceStart.getDirection();
                Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                // Calculate paths for both fangs
                for (double d = 0; d < 2.0; d += 0.1) {
                    // Left fang path
                    Location leftLoc = pierceStart.clone()
                            .add(right.clone().multiply(-0.3))
                            .add(direction.clone().multiply(d))
                            .add(0, 1.0 + d * 0.2, 0);
                    leftFangPath.add(leftLoc);

                    // Right fang path
                    Location rightLoc = pierceStart.clone()
                            .add(right.clone().multiply(0.3))
                            .add(direction.clone().multiply(d))
                            .add(0, 1.0 + d * 0.2, 0);
                    rightFangPath.add(rightLoc);
                }
            }

            private void executePierce() {
                if (leftFangPath.isEmpty() || rightFangPath.isEmpty()) return;

                // Create fang effects
                for (int i = 0; i < leftFangPath.size(); i++) {
                    double progress = i / (double)leftFangPath.size();

                    // Left fang
                    createFangEffect(leftFangPath.get(i), progress, true);

                    // Right fang
                    createFangEffect(rightFangPath.get(i), progress, false);

                    // Check for hits
                    checkPierceHits(leftFangPath.get(i), rightFangPath.get(i));
                }
            }

            private void createFangEffect(Location location, double progress, boolean isLeft) {
                // Fang particles
                Particle.DustOptions fangColor = new Particle.DustOptions(
                        Color.fromRGB(255 - (int)(progress * 100), 140 - (int)(progress * 70), 0),
                        0.8f);
                world.spawnParticle(Particle.DUST, location, 2, 0.05, 0.05, 0.05, 0, fangColor);

                // Trailing effects
                if (random.nextFloat() < 0.3) {
                    Vector offset = new Vector(
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2,
                            (random.nextDouble() - 0.5) * 0.2
                    );
                    Location trailLoc = location.clone().add(offset);

                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 0), 0.5f);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0.02, 0.02, 0.02, 0, trailColor);
                }
            }

            private void checkPierceHits(Location left, Location right) {
                if (hasStruck.get()) return;

                // Check both fang locations
                for (Location pierceLoc : Arrays.asList(left, right)) {
                    for (Entity entity : world.getNearbyEntities(pierceLoc, 0.8, 0.8, 0.8)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply pierce damage
                            target.damage(12.0, player);
                            hitEntities.add(entity);
                            hasStruck.set(true);

                            // Create hit effect
                            createPierceHitEffect(target.getLocation().add(0, 1.5, 0));

                            // Apply pierce effects
                            applyPierceEffects(target);
                        }
                    }
                }
            }

            private void createPierceHitEffect(Location location) {
                // Pierce impact
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location particleLoc = location.clone().add(spread);

                    // Blood effect
                    Particle.DustOptions bloodColor = new Particle.DustOptions(
                            Color.fromRGB(180, 0, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, bloodColor);
                }

                // Pierce sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
                world.playSound(location, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.2f);
            }

            private void applyPierceEffects(LivingEntity target) {
                // Apply bleeding and weakness
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));

                // Apply stun effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

                // Knockback
                Vector knockback = player.getLocation().getDirection()
                        .multiply(0.5)
                        .setY(0.2);
                target.setVelocity(knockback);
            }

            private void createFinishingEffect() {
                Location endLoc = player.getLocation();

                // Final beast manifestation
                new BukkitRunnable() {
                    private int ticks = 0;
                    private final int MAX_TICKS = 10;

                    @Override
                    public void run() {
                        if (ticks >= MAX_TICKS) {
                            this.cancel();
                            return;
                        }

                        // Create dissolving beast aura
                        double size = 1.0 * (1 - ticks / (double)MAX_TICKS);
                        createBeastParticles(endLoc.add(0, 0.1, 0),
                                size,
                                Color.fromRGB(255 - (ticks * 10), 140 - (ticks * 5), 0));

                        // Fading beast sounds
                        if (ticks % 2 == 0) {
                            world.playSound(endLoc, Sound.ENTITY_WOLF_AMBIENT,
                                    0.4f, 1.2f - (ticks * 0.05f));
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Apply effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 30, 1));

        // Add cooldown
      //  addCooldown(player, "FirstFang", 10);
    }

    /**
     * Second Fang: Slice (弐ノ牙 切り裂き)
     * Created: 2025-06-17 17:54:02
     * @author SkyForce-6
     */
    public void useSecondFang() {
        player.sendMessage("§6獣 §f弐ノ牙 切り裂き §6(Second Fang: Slice)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial sound
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.3f);

        Set<Entity> hitEntities = new HashSet<>();

        // Initial dash velocity
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(1.5).setY(0.1)); // Reduzierte Höhe, mehr Vorwärtsbewegung

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 10; // Kürzere Dauer

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector playerDir = playerLoc.getDirection();

                // Create horizontal slashes
                double slashLength = 3.0;
                for (double height : new double[]{0.5, 1.5}) { // Zwei Höhen für die Schnitte
                    Location slashStart = playerLoc.clone().add(0, height, 0);

                    // Create slash particles
                    for (double d = -slashLength/2; d <= slashLength/2; d += 0.2) {
                        Vector right = new Vector(-playerDir.getZ(), 0, playerDir.getX()).normalize();
                        Location slashLoc = slashStart.clone().add(right.multiply(d));

                        // Slash particle
                        Particle.DustOptions slashColor = new Particle.DustOptions(
                                Color.fromRGB(255, 140, 0), 1.0f);
                        world.spawnParticle(Particle.DUST, slashLoc, 1, 0.1, 0.1, 0.1, 0, slashColor);
                    }

                    // Sweep particle at the center
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashStart, 1, 0, 0, 0, 0);
                }

                // Check for hits
                for (Entity entity : player.getNearbyEntities(2.5, 2.5, 2.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Damage
                        target.damage(8.0, player);
                        hitEntities.add(entity);

                        // Hit effects
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        world.spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 2, 0.3, 0.3, 0.3, 0);

                        // Blood effect
                        Particle.DustOptions bloodColor = new Particle.DustOptions(
                                Color.fromRGB(180, 0, 0), 1.0f);
                        world.spawnParticle(Particle.DUST, hitLoc, 10, 0.3, 0.3, 0.3, 0, bloodColor);

                        // Hit sound
                        world.playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
                        world.playSound(hitLoc, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.4f);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                    }
                }

                // Movement particles
                createBeastMovementTrail(playerLoc, playerDir, Color.fromRGB(255, 140, 0));

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Add effects to player
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2));

        // Add cooldown
        //addCooldown(player, "SecondFang", 8);
    }

    /**
     * Third Fang: Devour (参ノ牙 喰い裂き)
     * Created: 2025-06-17 17:56:42
     * @author SkyForce-6
     *
     * A vicious throat-targeting dual horizontal slash attack
     */
    public void useThirdFang() {
        player.sendMessage("§6獣 §f参ノ牙 喰い裂き §6(Third Fang: Devour)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial sound
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.3f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicBoolean hasHit = new AtomicBoolean(false);

        // Initial leap velocity
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(1.2).setY(0.2)); // Leichter Sprung nach vorne

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 8; // Sehr kurze Dauer für schnellen Angriff

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector playerDir = playerLoc.getDirection();

                // Zwei horizontale Schnitte auf Kehlhöhe
                double throatHeight = 1.6; // Zielhöhe für den Hals
                double slashLength = 2.5;

                // Create parallel slashes
                for (double offset : new double[]{-0.2, 0.2}) { // Zwei leicht versetzte Schnitte
                    Location slashStart = playerLoc.clone().add(0, throatHeight, 0)
                            .add(playerDir.clone().multiply(offset));

                    // Slash particles
                    Vector right = new Vector(-playerDir.getZ(), 0, playerDir.getX()).normalize();
                    for (double d = -slashLength/2; d <= slashLength/2; d += 0.1) {
                        Location slashLoc = slashStart.clone().add(right.multiply(d));

                        // Main slash
                        Particle.DustOptions slashColor = new Particle.DustOptions(
                                Color.fromRGB(255, 100, 0), 1.0f);
                        world.spawnParticle(Particle.DUST, slashLoc, 1, 0.05, 0.05, 0.05, 0, slashColor);

                        // Sharp edge effect
                        if (random.nextFloat() < 0.3) {
                            Location edgeLoc = slashLoc.clone().add(0, random.nextDouble() * 0.2 - 0.1, 0);
                            Particle.DustOptions edgeColor = new Particle.DustOptions(
                                    Color.fromRGB(255, 200, 0), 0.5f);
                            world.spawnParticle(Particle.DUST, edgeLoc, 1, 0.02, 0.02, 0.02, 0, edgeColor);
                        }
                    }

                    // Sweep effect
                    world.spawnParticle(Particle.SWEEP_ATTACK, slashStart, 1, 0, 0, 0, 0);
                }

                // Check for throat hits
                for (Entity entity : player.getNearbyEntities(2.0, 2.0, 2.0)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;
                        Location targetThroat = target.getLocation().add(0, 1.6, 0); // Zielen auf den Hals

                        // Überprüfen ob der Treffer auf Kehlhöhe ist
                        if (Math.abs(targetThroat.getY() - (playerLoc.getY() + throatHeight)) < 0.5) {
                            // Critical hit - Kehle getroffen
                            target.damage(12.0, player);
                            hitEntities.add(entity);
                            hasHit.set(true);

                            // Enhanced hit effects for throat hit
                            createThroatHitEffect(targetThroat);

                            // Apply severe effects
                            applyDevourEffects(target);
                        }
                    }
                }

                // Beast movement particles
                if (!hasHit.get()) {
                    createBeastMovementTrail(playerLoc, playerDir, Color.fromRGB(255, 100, 0));
                }

                ticks++;
            }

            private void createThroatHitEffect(Location location) {
                // Blood spray effect
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() * 0.2, // Mostly upward spray
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location particleLoc = location.clone().add(spread);

                    // Blood particles
                    Particle.DustOptions bloodColor = new Particle.DustOptions(
                            Color.fromRGB(180, 0, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 0, bloodColor);
                }

                // Impact effects
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 4, 0.3, 0.1, 0.3, 0);

                // Sharp cutting sounds
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.4f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                world.playSound(location, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.5f);
            }

            private void applyDevourEffects(LivingEntity target) {
                // Severe bleeding
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2));

                // Weakness from blood loss
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));

                // Disorientation from trauma
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));

                // Slowed from injury
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));

                // Slight backward knockback
                Vector knockback = player.getLocation().getDirection()
                        .multiply(-0.3) // Rückwärts
                        .setY(0.2);
                target.setVelocity(knockback);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Brief speed boost for execution
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 2));

        // Add cooldown
     //   addCooldown(player, "ThirdFang", 12);

    }

    /**
     * Fourth Form: Slice 'n' Dice (肆ノ牙 切り細裂き)
     * Created: 2025-06-18 15:10:02
     * @author SkyForce-6
     *
     * A rapid series of diagonal slashes with both blades, creating a devastating attack pattern.
     */
    public void useFourthFang() {
        player.sendMessage("§6獣 §f肆ノ牙 切り細裂き §6(Fourth Fang: Slice 'n' Dice)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial sound
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.5f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger comboCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40; // 2 seconds duration
            private double currentAngle = 0;
            private boolean isRightSlash = true;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector direction = playerLoc.getDirection();

                // Perform slash every 3 ticks
                if (ticks % 3 == 0) {
                    // Alternate between right and left diagonal slashes
                    createDiagonalSlash(playerLoc, direction, isRightSlash);
                    isRightSlash = !isRightSlash;

                    // Movement boost during slash
                    Vector slashBoost = direction.multiply(0.5).setY(0.1);
                    player.setVelocity(slashBoost);

                    // Slash sound
                    world.playSound(playerLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f + (comboCount.get() * 0.1f));
                }

                // Create beast aura
                createBeastAura(playerLoc);

                ticks++;
            }

            private void createDiagonalSlash(Location center, Vector direction, boolean isRight) {
                double slashLength = 3.0;
                Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                // Calculate diagonal angle (45 degrees up or down)
                double baseAngle = isRight ? 45 : -45;
                currentAngle = Math.toRadians(baseAngle);

                // Create two parallel slashes (dual swords)
                for (int sword = 0; sword < 2; sword++) {
                    double swordOffset = (sword == 0) ? 0.5 : -0.5;
                    Location slashStart = center.clone().add(right.clone().multiply(swordOffset)).add(0, 1.5, 0);

                    // Create diagonal slash trail
                    for (double d = 0; d <= slashLength; d += 0.2) {
                        double x = d * Math.cos(currentAngle);
                        double y = d * Math.sin(currentAngle);

                        Location slashLoc = slashStart.clone()
                                .add(direction.clone().multiply(x))
                                .add(0, y, 0);

                        // Main slash particles
                        Particle.DustOptions slashColor = new Particle.DustOptions(
                                Color.fromRGB(255, 100 + comboCount.get() * 20, 0), 1.0f);
                        world.spawnParticle(Particle.DUST, slashLoc, 2, 0.1, 0.1, 0.1, 0, slashColor);

                        // Trail effect
                        world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);

                        // Energy particles
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.CRIT, slashLoc, 1, 0.1, 0.1, 0.1, 0.2);
                        }
                    }
                }

                // Check for hits
                List<Entity> nearbyEntities = player.getNearbyEntities(3, 3, 3);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate combo damage
                        double baseDamage = 6.0;
                        double comboMultiplier = 1.0 + (comboCount.get() * 0.2);
                        if (comboMultiplier > 2.5) comboMultiplier = 2.5;

                        // Apply damage
                        target.damage(baseDamage * comboMultiplier, player);
                        hitEntities.add(entity);
                        comboCount.incrementAndGet();

                        // Hit effects
                        createHitEffect(target.getLocation().add(0, 1, 0));

                        // Combo sound
                        world.playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.8f, 1.0f + (comboCount.get() * 0.1f));
                    }
                }
            }

            private void createBeastAura(Location location) {
                // Beast aura particles
                for (int i = 0; i < 3; i++) {
                    Vector offset = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).multiply(2);

                    Location auraLoc = location.clone().add(offset);

                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, auraLoc, 1, 0.1, 0.1, 0.1, 0, auraColor);
                }
            }

            private void createHitEffect(Location location) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, location, 3, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.2, 0.2, 0.2, 0);

                // Beast energy particles
                Particle.DustOptions hitColor = new Particle.DustOptions(
                        Color.fromRGB(255, 100, 0), 1.5f);
                for (int i = 0; i < 8; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 0.5);

                    Location hitLoc = location.clone().add(spread);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add speed effect during execution
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));

        // Add cooldown
       // addCooldown(player, "FourthFang", 12);
    }

    /**
     * Fifth Form: Crazy Cutting (伍ノ牙 狂い裂き)
     * Created: 2025-06-18 15:21:03
     * @author SkyForce-6
     *
     * A mid-air omnidirectional slashing technique that creates a sphere of destruction.
     */
    public void useFifthFang() {
        player.sendMessage("§6獣 §f伍ノ牙 狂い裂き §6(Fifth Fang: Crazy Cutting)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial leap sound and effect
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);

        // Initial leap
        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(1.2));

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger slashCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40;
            private double sphereRadius = 3.0;
            private double rotationHorizontal = 0;
            private double rotationVertical = 0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Mid-air stabilization
                if (ticks < 20) {
                    player.setVelocity(player.getVelocity().setY(Math.max(-0.1, player.getVelocity().getY())));
                }

                // Create omnidirectional slashes
                if (ticks % 2 == 0) {
                    createOmniSlash(playerLoc);
                }

                // Beast aura effect
                createBeastAura(playerLoc);

                // Spin effect
                rotationHorizontal += Math.PI / 8;
                rotationVertical += Math.PI / 12;

                ticks++;
            }

            private void createOmniSlash(Location center) {
                // Create multiple slash planes
                for (int plane = 0; plane < 3; plane++) {
                    double planeRotation = (Math.PI * 2 * plane) / 3;

                    // Create slashes in current plane
                    for (int slash = 0; slash < 4; slash++) {
                        double slashAngle = rotationHorizontal + (Math.PI / 2 * slash) + planeRotation;
                        createSlashPlane(center, slashAngle, rotationVertical);
                    }
                }

                // Hitbox detection
                for (Entity entity : player.getNearbyEntities(sphereRadius, sphereRadius, sphereRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate distance-based damage
                        double distance = target.getLocation().distance(center);
                        double damageMultiplier = 1.0 - (distance / sphereRadius);
                        double damage = 8.0 * Math.max(0.3, damageMultiplier);

                        // Apply damage and knockback
                        target.damage(damage, player);
                        Vector knockback = target.getLocation().subtract(center).toVector()
                                .normalize()
                                .multiply(0.8)
                                .setY(0.2);
                        target.setVelocity(knockback);

                        // Hit effects
                        createHitEffect(target.getLocation().add(0, 1, 0));
                        hitEntities.add(entity);
                        slashCount.incrementAndGet();
                    }
                }
            }

            private void createSlashPlane(Location center, double horizontalAngle, double verticalAngle) {
                // Calculate plane vectors
                Vector horizontal = new Vector(
                        Math.cos(horizontalAngle),
                        0,
                        Math.sin(horizontalAngle)
                );

                Vector vertical = new Vector(
                        Math.cos(verticalAngle) * Math.cos(horizontalAngle + Math.PI/2),
                        Math.sin(verticalAngle),
                        Math.cos(verticalAngle) * Math.sin(horizontalAngle + Math.PI/2)
                );

                // Create slash trail
                for (double d = 0; d <= sphereRadius; d += 0.2) {
                    Vector offset = horizontal.clone().multiply(d);

                    // Create dual sword effect
                    for (double h = -0.3; h <= 0.3; h += 0.6) {
                        Vector heightOffset = vertical.clone().multiply(h);
                        Location slashLoc = center.clone().add(offset).add(heightOffset);

                        // Main slash particles
                        Particle.DustOptions slashColor = new Particle.DustOptions(
                                Color.fromRGB(255, 100 + slashCount.get() * 10, 0), 1.2f);
                        world.spawnParticle(Particle.DUST, slashLoc, 1, 0.1, 0.1, 0.1, 0, slashColor);

                        // Sword trail effect
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                        }

                        // Energy particles
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.CRIT, slashLoc, 1, 0.1, 0.1, 0.1, 0.1);
                        }
                    }
                }
            }

            private void createBeastAura(Location location) {
                // Spiraling beast aura
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2 / 8) + rotationHorizontal;
                    double height = Math.sin(rotationVertical + i * Math.PI/4) * 2;

                    Vector offset = new Vector(
                            Math.cos(angle) * 2,
                            height,
                            Math.sin(angle) * 2
                    );

                    Location auraLoc = location.clone().add(offset);

                    // Beast aura particles
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 0), 1.5f);
                    world.spawnParticle(Particle.DUST, auraLoc, 1, 0.1, 0.1, 0.1, 0, auraColor);

                    // Energy wisps
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLAME, auraLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            private void createHitEffect(Location location) {
                // Explosive hit effect
                world.spawnParticle(Particle.EXPLOSION, location, 5, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 3, 0.2, 0.2, 0.2, 0);

                // Beast energy burst
                for (int i = 0; i < 12; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location burstLoc = location.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100, 0), 1.2f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(location, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.2f);
                world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add effects during execution
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));

        // Add cooldown
      //  addCooldown(player, "FifthFang", 15);
    }

    /**
     * Sixth Form: Palisade Bite (陸ノ牙 乱杭咬み)
     * Created: 2025-06-18 15:24:51
     * @author SkyForce-6
     *
     * A devastating technique using both swords in a saw-like motion,
     * creating a palisade of slashes from both sides.
     */
    public void useSixthFang() {
        player.sendMessage("§6獣 §f陸ノ牙 乱杭咬み §6(Sixth Fang: Palisade Bite)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 0.8f);
        world.playSound(startLoc, Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.5f);

        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger sawCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 30;
            private double sawAngle = 0;
            private boolean isForwardMotion = true;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector direction = playerLoc.getDirection();

                // Create saw motion every tick
                createSawSlash(playerLoc, direction);

                // Beast aura effect
                createBeastAura(playerLoc);

                // Movement during slash
                if (ticks % 5 == 0) {
                    Vector movement = direction.multiply(isForwardMotion ? 0.5 : -0.3);
                    player.setVelocity(movement);
                    isForwardMotion = !isForwardMotion;
                }

                sawAngle += Math.PI / 8;
                ticks++;
            }

            private void createSawSlash(Location center, Vector direction) {
                double slashLength = 3.0;
                Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                // Create two opposing saw patterns
                for (int side = 0; side < 2; side++) {
                    boolean isRightSide = side == 0;
                    double sideOffset = isRightSide ? 1.0 : -1.0;

                    // Create saw teeth pattern
                    for (int tooth = 0; tooth < 4; tooth++) {
                        double toothAngle = sawAngle + (Math.PI * tooth / 2);
                        double toothHeight = Math.sin(toothAngle) * 0.5;

                        // Create saw blade trail
                        for (double d = 0; d <= slashLength; d += 0.1) {
                            // Calculate saw tooth position
                            double waveHeight = Math.sin(d * 2 + sawAngle) * 0.3;

                            Location slashLoc = center.clone()
                                    .add(right.clone().multiply(sideOffset))
                                    .add(direction.clone().multiply(d))
                                    .add(0, waveHeight + toothHeight, 0);

                            // Main saw particles
                            Particle.DustOptions sawColor = new Particle.DustOptions(
                                    Color.fromRGB(255, 80 + sawCount.get() * 15, 0), 1.0f);
                            world.spawnParticle(Particle.DUST, slashLoc, 1, 0.05, 0.05, 0.05, 0, sawColor);

                            // Saw teeth effect
                            if (Math.abs(waveHeight) > 0.25) {
                                world.spawnParticle(Particle.SWEEP_ATTACK, slashLoc, 1, 0, 0, 0, 0);
                            }

                            // Energy particles
                            if (random.nextFloat() < 0.15) {
                                world.spawnParticle(Particle.CRIT, slashLoc, 1, 0.1, 0.1, 0.1, 0.1);
                            }
                        }
                    }
                }

                // Check for hits
                for (Entity entity : player.getNearbyEntities(3, 2, 3)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Multiple small hits to simulate saw effect
                        int hitCount = 3 + random.nextInt(3);
                        for (int i = 0; i < hitCount; i++) {
                            target.damage(3.0, player);
                            createSawHitEffect(target.getLocation().add(0, 1, 0));

                            // Small knockback on each hit
                            Vector knockback = target.getLocation().subtract(center).toVector()
                                    .normalize()
                                    .multiply(0.2)
                                    .setY(0.1);
                            target.setVelocity(knockback);
                        }

                        hitEntities.add(entity);
                        sawCount.incrementAndGet();

                        // Saw hit sound
                        world.playSound(target.getLocation(), Sound.BLOCK_CHAIN_BREAK, 0.8f, 1.2f);
                        world.playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.6f, 1.0f);
                    }
                }
            }

            private void createBeastAura(Location location) {
                // Create saw-like beast aura
                for (int i = 0; i < 6; i++) {
                    double angle = (i * Math.PI * 2 / 6) + sawAngle;
                    double height = Math.sin(sawAngle * 2 + i) * 1.5;

                    Vector offset = new Vector(
                            Math.cos(angle) * 2,
                            height,
                            Math.sin(angle) * 2
                    );

                    Location auraLoc = location.clone().add(offset);

                    // Beast aura particles
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(255, 40, 0), 1.3f);
                    world.spawnParticle(Particle.DUST, auraLoc, 1, 0.1, 0.1, 0.1, 0, auraColor);

                    // Additional saw-like effects
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.WAX_OFF, auraLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }
            }

            private void createSawHitEffect(Location location) {
                // Saw impact particles
                world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.1, 0.1, 0.1, 0);
                world.spawnParticle(Particle.BLOCK, location, 10, 0.2, 0.2, 0.2, 1, Material.REDSTONE_BLOCK.createBlockData());

                // Energy burst
                for (int i = 0; i < 6; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.3);

                    Location burstLoc = location.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 60, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add effects during execution
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 30, 0));

        // Add cooldown
      //  addCooldown(player, "SixthFang", 14);
    }

    public void useSeventhForm() {
        player.sendMessage("§6獣 §f漆ノ型 空間識覚 §6(Seventh Form: Spatial Awareness)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial sensory enhancement effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_AMBIENT, 0.5f, 0.7f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);

        // Hole den WeakPointListener
        BeastBreathingWeakPointListener weakPointListener = null;
        for (RegisteredListener registeredListener : HandlerList.getRegisteredListeners(plugin)) {
            if (registeredListener.getListener() instanceof BeastBreathingWeakPointListener) {
                weakPointListener = (BeastBreathingWeakPointListener) registeredListener.getListener();
                break;
            }
        }

        if (weakPointListener == null) {
            weakPointListener = new BeastBreathingWeakPointListener(plugin);
            plugin.getServer().getPluginManager().registerEvents(weakPointListener, plugin);
        }

        final BeastBreathingWeakPointListener listener = weakPointListener;

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200; // 10 seconds duration
            private double detectionRadius = 3.0;
            private double maxDetectionRadius = 50.0;
            private final Map<Entity, List<Location>> detectedWeakPoints = new HashMap<>();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    // Entferne alle Erkennungseffekte
                    detectedWeakPoints.keySet().forEach(entity -> {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).setGlowing(false);
                        }
                    });
                    listener.removeDetection(player.getUniqueId());
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Expanding detection radius
                if (detectionRadius < maxDetectionRadius) {
                    detectionRadius += 1.0;
                }

                // Create sensory pulse every 10 ticks
                if (ticks % 10 == 0) {
                    createSensoryPulse(playerLoc);
                }

                // Update detected entities
                updateDetectedEntities(playerLoc);

                // Visualize detected entities and their weakpoints
                visualizeDetections(playerLoc);

                ticks++;
            }

            private void createSensoryPulse(Location center) {
                double currentRadius = Math.min(detectionRadius, maxDetectionRadius);

                for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 16) {
                    double x = Math.cos(theta) * currentRadius;
                    double z = Math.sin(theta) * currentRadius;

                    Location pulseLoc = center.clone().add(x, 0, z);
                    pulseLoc.setY(getGroundLevel(pulseLoc));

                    // Pulse particles
                    Particle.DustOptions pulseColor = new Particle.DustOptions(
                            Color.fromRGB(100, 200, 255), 0.7f);
                    world.spawnParticle(Particle.DUST, pulseLoc, 1, 0, 0.5, 0, 0, pulseColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, pulseLoc, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }

                world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.3f, 1.5f);
            }

            private void updateDetectedEntities(Location center) {
                // Clear old detections periodically
                if (ticks % 20 == 0) {
                    detectedWeakPoints.keySet().forEach(entity -> {
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).setGlowing(false);
                        }
                    });
                    detectedWeakPoints.clear();
                }

                for (Entity entity : world.getNearbyEntities(center, detectionRadius, detectionRadius, detectionRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !detectedWeakPoints.containsKey(entity)) {
                        LivingEntity target = (LivingEntity) entity;
                        target.setGlowing(true);

                        // Generate weak points
                        List<Location> weakPoints = new ArrayList<>();
                        for (int i = 0; i < 3 + random.nextInt(2); i++) {
                            double angle = (Math.PI * 2 * i) / 3;
                            double height = 0.5 + random.nextDouble() * 1.5;

                            Location weakPoint = target.getLocation().clone().add(
                                    Math.cos(angle) * 0.5,
                                    height,
                                    Math.sin(angle) * 0.5
                            );
                            weakPoints.add(weakPoint);
                        }

                        detectedWeakPoints.put(entity, weakPoints);
                        listener.addDetection(target.getUniqueId(), target.getUniqueId(), weakPoints);
                        world.playSound(entity.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
                    }
                }
            }

            private void visualizeDetections(Location center) {
                for (Map.Entry<Entity, List<Location>> entry : detectedWeakPoints.entrySet()) {
                    Entity target = entry.getKey();
                    List<Location> weakPoints = entry.getValue();

                    // Draw connection line
                    drawDetectionLine(center, target.getLocation());

                    // Show weak points
                    for (Location weakPoint : weakPoints) {
                        Particle.DustOptions weakPointColor = new Particle.DustOptions(
                                Color.fromRGB(255, 50, 50), 0.8f);
                        world.spawnParticle(Particle.DUST, weakPoint, 2, 0.1, 0.1, 0.1, 0, weakPointColor);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, weakPoint, 1, 0, 0, 0, 0.02);
                        }
                    }
                }
            }

            private void drawDetectionLine(Location start, Location end) {
                Vector direction = end.clone().subtract(start).toVector().normalize();
                double distance = start.distance(end);

                for (double d = 0; d < distance; d += 0.5) {
                    Location lineLoc = start.clone().add(direction.clone().multiply(d));

                    Particle.DustOptions lineColor = new Particle.DustOptions(
                            Color.fromRGB(100, 200, 255), 0.5f);
                    world.spawnParticle(Particle.DUST, lineLoc, 1, 0, 0, 0, 0, lineColor);
                }
            }

            private double getGroundLevel(Location loc) {
                Location ground = loc.clone();
                while (ground.getBlock().getType() == Material.AIR && ground.getY() > 0) {
                    ground.subtract(0, 1, 0);
                }
                return ground.getY() + 1;
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));

        // Add cooldown
   //     addCooldown(player, "SeventhForm", 30);
    }

    /**
     * Eighth Form: Explosive Rush (捌ノ型 爆裂猛進)
     * Created: 2025-06-18 19:05:10
     * @author SkyForce-6
     *
     * A devastating charging technique that grants temporary invulnerability
     * and extreme speed to close in on opponents.
     */
    public void useEighthForm() {
        player.sendMessage("§6獣 §f捌ノ型 爆裂猛進 §6(Eighth Form: Explosive Rush)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();
        Vector direction = player.getLocation().getDirection();

        // Initial rush effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.ENTITY_WITHER_SHOOT, 0.5f, 2.0f);

        // Store hit entities to prevent multiple hits
        Set<Entity> hitEntities = new HashSet<>();
        AtomicInteger rushTicks = new AtomicInteger(0);

        // Make player temporarily invulnerable
        player.setInvulnerable(true);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40; // 2 seconds duration
            private final double RUSH_SPEED = 1.5;
            private final double DAMAGE = 12.0;
            private boolean isCharging = true;
            private Location lastLocation = player.getLocation();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    player.setInvulnerable(false);
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();
                Vector currentDirection = player.getLocation().getDirection();

                if (isCharging) {
                    // Apply rush momentum
                    Vector velocity = currentDirection.multiply(RUSH_SPEED);
                    player.setVelocity(velocity);

                    // Create rush effects
                    createRushEffect(currentLoc, lastLocation);

                    // Check for collisions
                    checkCollisions(currentLoc);
                }

                lastLocation = currentLoc;
                ticks++;
            }

            private void createRushEffect(Location current, Location last) {
                // Beast aura trail
                double distance = current.distance(last);
                Vector direction = current.toVector().subtract(last.toVector()).normalize();

                for (double d = 0; d < distance; d += 0.2) {
                    Location particleLoc = last.clone().add(direction.clone().multiply(d));

                    // Main rush particles
                    Particle.DustOptions rushColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50 + random.nextInt(100), 0), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 3, 0.2, 0.2, 0.2, 0, rushColor);

                    // Beast energy particles
                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }

                    // Air distortion
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                }

                // Sound effects
                if (ticks % 5 == 0) {
                    world.playSound(current, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 2.0f);
                    world.playSound(current, Sound.ENTITY_WOLF_AMBIENT, 0.4f, 1.5f);
                }
            }

            private void checkCollisions(Location current) {
                // Check for entity collisions
                for (Entity entity : current.getWorld().getNearbyEntities(current, 2, 2, 2)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate impact damage
                        double impactDamage = DAMAGE;
                        Vector knockback = player.getLocation().getDirection().multiply(2.0).setY(0.5);

                        // Apply damage and knockback
                        target.damage(impactDamage, player);
                        target.setVelocity(knockback);
                        hitEntities.add(target);

                        // Impact effects
                        createImpactEffect(target.getLocation());
                    }
                }

                // Check for block collisions
                Location ahead = current.clone().add(current.getDirection().multiply(1));
                if (!ahead.getBlock().isPassable()) {
                    isCharging = false;
                    player.setVelocity(new Vector(0, 0.2, 0));
                    createBlockImpactEffect(ahead);
                }
            }

            private void createImpactEffect(Location loc) {
                // Explosive impact particles
                world.spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);

                // Beast energy burst
                for (int i = 0; i < 20; i++) {
                    Vector direction = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location burstLoc = loc.clone().add(direction);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                world.playSound(loc, Sound.ENTITY_WOLF_GROWL, 1.0f, 0.5f);
            }

            private void createBlockImpactEffect(Location loc) {
                // Block collision particles
                world.spawnParticle(Particle.EXPLOSION, loc, 10, 0.5, 0.5, 0.5, 0.1);
                world.spawnParticle(Particle.BLOCK, loc, 40, 0.5, 0.5, 0.5, 0.1,
                        loc.getBlock().getBlockData());

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
                world.playSound(loc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Resistance effect during rush
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 4));

        // Add cooldown
        //addCooldown(player, "EighthForm", 20);
    }

    /**
     * Ninth Fang: Extending Bendy Slash (玖ノ牙 伸・うねり裂き)
     * Created: 2025-06-18 19:10:50
     * @author SkyForce-6
     *
     * A technique that utilizes extreme flexibility to extend attack range
     * through a rapid, snake-like forward strike.
     */
    public void useNinthFang() {
        player.sendMessage("§6獣 §f玖ノ牙 伸・うねり裂き §6(Ninth Fang: Extending Bendy Slash)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial stance effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.2f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 0.5f);

        // Store hit entities to prevent multiple hits
        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 20; // 1 second duration
            private final double MAX_RANGE = 8.0; // Extended attack range
            private double currentExtension = 0;
            private boolean isExtending = true;
            private final List<Location> slashPath = new ArrayList<>();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector direction = playerLoc.getDirection();

                // Calculate snake-like motion
                if (isExtending && currentExtension < MAX_RANGE) {
                    currentExtension += 0.8;
                    if (currentExtension >= MAX_RANGE) {
                        isExtending = false;
                    }
                } else if (!isExtending && currentExtension > 0) {
                    currentExtension -= 1.2;
                }

                // Create bendy slash effect
                createBendySlash(playerLoc, direction);

                // Check for hits
                checkHits();

                ticks++;
            }

            private void createBendySlash(Location start, Vector baseDirection) {
                slashPath.clear();
                double amplitude = 0.5; // Wave height
                double frequency = 2.0; // Wave frequency

                // Create snake-like motion path
                for (double d = 0; d < currentExtension; d += 0.2) {
                    double wave = Math.sin(d * frequency + ticks * 0.5) * amplitude;
                    Vector offset = baseDirection.clone().multiply(d);

                    // Calculate wave offset
                    Vector right = new Vector(-baseDirection.getZ(), 0, baseDirection.getX()).normalize();
                    Vector waveOffset = right.multiply(wave);

                    // Add vertical wave motion
                    double verticalWave = Math.sin(d * frequency * 0.5 + ticks * 0.3) * amplitude * 0.5;

                    Location slashLoc = start.clone()
                            .add(offset)
                            .add(waveOffset)
                            .add(0, 1.0 + verticalWave, 0);

                    slashPath.add(slashLoc);

                    // Create extending arm effect
                    createArmEffect(slashLoc, d / currentExtension);
                }
            }

            private void createArmEffect(Location loc, double progress) {
                // Main slash trail
                Particle.DustOptions slashColor = new Particle.DustOptions(
                        Color.fromRGB(
                                255,
                                (int)(100 + (100 * progress)),
                                0
                        ),
                        1.0f
                );
                world.spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0, slashColor);

                // Energy particles
                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.CRIT, loc, 1, 0.1, 0.1, 0.1, 0.1);
                }

                // Sword trail
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                }

                // Beast aura
                if (random.nextFloat() < 0.15) {
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 0), 0.8f);
                    world.spawnParticle(Particle.DUST, loc, 1, 0.2, 0.2, 0.2, 0, auraColor);
                }
            }

            private void checkHits() {
                // Only check hits during extension
                if (!isExtending) return;

                for (Location checkLoc : slashPath) {
                    for (Entity entity : checkLoc.getWorld().getNearbyEntities(checkLoc, 1.5, 1.5, 1.5)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Calculate damage based on distance
                            double distance = target.getLocation().distance(player.getLocation());
                            double damage = 10.0 * (1.0 - (distance / MAX_RANGE) * 0.5);

                            // Apply damage and effects
                            target.damage(damage, player);
                            hitEntities.add(target);

                            // Knockback based on slash direction
                            Vector knockback = checkLoc.toVector()
                                    .subtract(player.getLocation().toVector())
                                    .normalize()
                                    .multiply(1.0)
                                    .setY(0.2);
                            target.setVelocity(knockback);

                            // Hit effects
                            createHitEffect(target.getLocation());
                        }
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, loc, 3, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 2, 0.2, 0.2, 0.2, 0);

                // Beast energy burst
                for (int i = 0; i < 8; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 0.5);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100, 0), 1.2f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
                world.playSound(loc, Sound.ENTITY_WOLF_GROWL, 0.6f, 2.0f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add movement effects during slash
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 2));

        // Add cooldown
      // addCooldown(player, "NinthFang", 15);
    }

    /**
     * Tenth Fang: Whirling Fangs (拾ノ牙 円転旋牙)
     * Created: 2025-06-18 19:15:05
     * @author SkyForce-6
     *
     * A defensive technique that creates a whirlwind of sword strikes,
     * capable of deflecting incoming projectiles and attacks.
     */
    public void useTenthFang() {
        player.sendMessage("§6獣 §f拾ノ牙 円転旋牙 §6(Tenth Fang: Whirling Fangs)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial whirl effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.5f);
        world.playSound(startLoc, Sound.ITEM_SHIELD_BLOCK, 0.6f, 1.2f);

        // Store deflected projectiles
        Set<Entity> deflectedProjectiles = new HashSet<>();
        AtomicInteger deflectionCount = new AtomicInteger(0);

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 100; // 5 seconds duration
            private double rotationAngle = 0;
            private final double ROTATION_SPEED = Math.PI / 8; // Speed of rotation
            private final double WHIRL_RADIUS = 2.5; // Radius of defensive whirl

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Create whirling effect
                createWhirlEffect(playerLoc);

                // Check for and deflect projectiles
                checkProjectiles(playerLoc);

                // Update rotation
                rotationAngle += ROTATION_SPEED;
                if (rotationAngle >= Math.PI * 2) {
                    rotationAngle = 0;
                }

                ticks++;
            }

            private void createWhirlEffect(Location center) {
                // Create multiple spinning blade layers
                for (int layer = 0; layer < 3; layer++) {
                    double layerHeight = 0.8 + (layer * 0.8);
                    double layerRadius = WHIRL_RADIUS - (layer * 0.5);
                    double layerOffset = (layer * Math.PI / 3) + (ticks * 0.2);

                    // Create sword trails
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        double currentAngle = angle + rotationAngle + layerOffset;

                        // Calculate position
                        double x = Math.cos(currentAngle) * layerRadius;
                        double z = Math.sin(currentAngle) * layerRadius;

                        Location bladeLoc = center.clone().add(x, layerHeight, z);

                        // Create blade effects
                        createBladeEffect(bladeLoc, currentAngle);
                    }
                }

                // Ground effect
                if (ticks % 5 == 0) {
                    createGroundEffect(center);
                }
            }

            private void createBladeEffect(Location loc, double angle) {
                // Blade trail
                Particle.DustOptions bladeColor = new Particle.DustOptions(
                        Color.fromRGB(255, 150 + random.nextInt(100), 0), 1.0f);
                world.spawnParticle(Particle.DUST, loc, 2, 0.1, 0.1, 0.1, 0, bladeColor);

                // Sword swing effect
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                }

                // Energy particles
                if (random.nextFloat() < 0.15) {
                    world.spawnParticle(Particle.CRIT, loc, 1, 0.1, 0.1, 0.1, 0.1);
                }

                // Beast aura
                if (random.nextFloat() < 0.1) {
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 0), 0.8f);
                    world.spawnParticle(Particle.DUST, loc, 1, 0.2, 0.2, 0.2, 0, auraColor);
                }
            }

            private void createGroundEffect(Location center) {
                double groundRadius = WHIRL_RADIUS * 0.8;

                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * groundRadius;
                    double z = Math.sin(angle) * groundRadius;

                    Location groundLoc = center.clone().add(x, 0.1, z);

                    Particle.DustOptions groundColor = new Particle.DustOptions(
                            Color.fromRGB(200, 100, 0), 0.6f);
                    world.spawnParticle(Particle.DUST, groundLoc, 1, 0.1, 0, 0.1, 0, groundColor);
                }
            }

            private void checkProjectiles(Location center) {
                // Get nearby projectiles
                for (Entity entity : center.getWorld().getNearbyEntities(center, WHIRL_RADIUS, WHIRL_RADIUS, WHIRL_RADIUS)) {
                    if (isDeflectableProjectile(entity) && !deflectedProjectiles.contains(entity)) {
                        // Calculate deflection
                        Vector projectileVel = entity.getVelocity();
                        Location projectileLoc = entity.getLocation();

                        // Reflect velocity
                        Vector toCenter = center.toVector().subtract(projectileLoc.toVector()).normalize();
                        Vector reflection = projectileVel.multiply(-1.2); // Bounce back faster

                        // Apply reflection
                        entity.setVelocity(reflection);
                        deflectedProjectiles.add(entity);
                        deflectionCount.incrementAndGet();

                        // Deflection effects
                        createDeflectionEffect(projectileLoc);
                    }
                }
            }

            private boolean isDeflectableProjectile(Entity entity) {
                return entity instanceof Arrow ||
                        entity instanceof Snowball ||
                        entity instanceof ThrownPotion ||
                        entity instanceof Trident;
            }

            private void createDeflectionEffect(Location loc) {
                // Deflection particles
                world.spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.2, 0.2, 0.2, 0);

                // Energy burst
                for (int i = 0; i < 8; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Deflection sounds
                world.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.5f);
                world.playSound(loc, Sound.ENTITY_WOLF_GROWL, 0.4f, 2.0f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add defensive effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));

        // Add cooldown
      //  addCooldown(player, "TenthFang", 25);
    }

    /**
     * Eleventh Fang: Sudden Throwing Strike (思いつきの投げ裂き)
     * Created: 2025-06-18 19:18:50
     * @author SkyForce-6
     *
     * A technique where the user throws both swords in a deadly spinning motion,
     * creating a devastating projectile attack.
     */
    public void useEleventhFang() {
        player.sendMessage("§6獣 §f思いつきの投げ裂き §6(Eleventh Fang: Sudden Throwing Strike)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation().add(0, 1.5, 0);
        Vector direction = player.getLocation().getDirection();

        // Initial throw effects
        world.playSound(startLoc, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.8f);
        world.playSound(startLoc, Sound.ITEM_TRIDENT_THROW, 1.0f, 1.2f);

        // Store hit entities to prevent multiple hits
        Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40; // 2 seconds flight time
            private final double THROW_SPEED = 1.0;
            private final double DAMAGE = 15.0;
            private Location sword1Loc = startLoc.clone();
            private Location sword2Loc = startLoc.clone();
            private Vector sword1Dir = direction.clone();
            private Vector sword2Dir = direction.clone();
            private double rotationAngle = 0;
            private boolean returning = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                // Update sword positions
                updateSwordPositions();

                // Create sword effects
                createSwordEffects();

                // Check for hits
                checkHits();

                // Start return journey if halfway through
                if (ticks == MAX_TICKS / 2) {
                    returning = true;
                    // Calculate return vectors
                    Location playerNewLoc = player.getLocation().add(0, 1.5, 0);
                    sword1Dir = playerNewLoc.toVector().subtract(sword1Loc.toVector()).normalize();
                    sword2Dir = playerNewLoc.toVector().subtract(sword2Loc.toVector()).normalize();
                }

                ticks++;
            }

            private void updateSwordPositions() {
                // Update rotation angle
                rotationAngle += Math.PI / 8;

                // Calculate orbital offsets
                double orbitRadius = 0.8;
                Vector orbit1 = new Vector(
                        Math.cos(rotationAngle) * orbitRadius,
                        Math.sin(rotationAngle) * orbitRadius,
                        0
                );
                Vector orbit2 = new Vector(
                        Math.cos(rotationAngle + Math.PI) * orbitRadius,
                        Math.sin(rotationAngle + Math.PI) * orbitRadius,
                        0
                );

                // Rotate orbital vectors to match throw direction
                orbit1 = rotateVector(orbit1, direction);
                orbit2 = rotateVector(orbit2, direction);

                // Update sword locations
                Vector movement = returning ?
                        direction.clone().multiply(THROW_SPEED * 1.5) :
                        direction.clone().multiply(THROW_SPEED);

                sword1Loc.add(movement).add(orbit1);
                sword2Loc.add(movement).add(orbit2);
            }

            private void createSwordEffects() {
                // Create effects for both swords
                createSingleSwordEffect(sword1Loc);
                createSingleSwordEffect(sword2Loc);

                // Create connection trail
                Vector between = sword2Loc.toVector().subtract(sword1Loc.toVector());
                double distance = between.length();
                Vector step = between.normalize().multiply(0.2);

                for (double d = 0; d < distance; d += 0.2) {
                    Location trailLoc = sword1Loc.clone().add(step.clone().multiply(d));

                    // Energy trail
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100 + random.nextInt(100), 0), 0.8f);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0.1, 0.1, 0.1, 0, trailColor);
                }
            }

            private void createSingleSwordEffect(Location loc) {
                // Sword blade effect
                for (int i = 0; i < 3; i++) {
                    Location bladeLoc = loc.clone().add(
                            random.nextDouble() * 0.2 - 0.1,
                            random.nextDouble() * 0.2 - 0.1,
                            random.nextDouble() * 0.2 - 0.1
                    );

                    // Blade particles
                    Particle.DustOptions bladeColor = new Particle.DustOptions(
                            Color.fromRGB(255, 150 + random.nextInt(100), 0), 1.0f);
                    world.spawnParticle(Particle.DUST, bladeLoc, 1, 0, 0, 0, 0, bladeColor);
                }

                // Spinning effect
                if (random.nextFloat() < 0.3) {
                    world.spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                }

                // Beast energy
                if (random.nextFloat() < 0.2) {
                    world.spawnParticle(Particle.CRIT, loc, 2, 0.1, 0.1, 0.1, 0.1);
                }
            }

            private void checkHits() {
                checkSwordHits(sword1Loc);
                checkSwordHits(sword2Loc);
            }

            private void checkSwordHits(Location swordLoc) {
                for (Entity entity : swordLoc.getWorld().getNearbyEntities(swordLoc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply knockback
                        Vector knockback = swordLoc.getDirection().multiply(1.5).setY(0.2);
                        target.setVelocity(knockback);

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Impact particles
                world.spawnParticle(Particle.EXPLOSION, loc, 5, 0.2, 0.2, 0.2, 0.05);
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.2, 0.2, 0.2, 0);

                // Beast energy burst
                for (int i = 0; i < 12; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 0.8);

                    Location burstLoc = loc.clone().add(spread);

                    Particle.DustOptions burstColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 0), 1.0f);
                    world.spawnParticle(Particle.DUST, burstLoc, 1, 0, 0, 0, 0, burstColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.2f);
                world.playSound(loc, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.5f);
            }

            private Vector rotateVector(Vector vec, Vector axis) {
                // Rotate vector around axis
                double theta = Math.acos(axis.getY());
                double phi = Math.atan2(axis.getZ(), axis.getX());

                // Convert to spherical coordinates
                double x = vec.getX();
                double y = vec.getY() * Math.cos(theta) - vec.getZ() * Math.sin(theta);
                double z = vec.getY() * Math.sin(theta) + vec.getZ() * Math.cos(theta);

                // Rotate in XZ plane
                double xNew = x * Math.cos(phi) - z * Math.sin(phi);
                double zNew = x * Math.sin(phi) + z * Math.cos(phi);

                return new Vector(xNew, y, zNew);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add temporary effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));

        // Add cooldown
       // addCooldown(player, "EleventhFang", 20);
    }

}