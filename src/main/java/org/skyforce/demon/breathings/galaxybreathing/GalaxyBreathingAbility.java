package org.skyforce.demon.breathings.galaxybreathing;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Main class for Galaxy Breathing abilities
 * Created: 2025-06-18 21:11:18
 * @author SkyForce-6
 */
public class GalaxyBreathingAbility {
    private final Plugin plugin;
    private final Player player;
    private final Random random;
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public GalaxyBreathingAbility(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.random = new Random();

    }

    protected void addCooldown(Player player, String ability, int seconds) {
        UUID playerId = player.getUniqueId();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + (seconds * 1000L));
    }

    protected boolean isOnCooldown(Player player, String ability) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        if (playerCooldowns == null || !playerCooldowns.containsKey(ability)) {
            return false;
        }

        return System.currentTimeMillis() < playerCooldowns.get(ability);
    }

    protected long getCooldownTime(Player player, String ability) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        if (playerCooldowns == null || !playerCooldowns.containsKey(ability)) {
            return 0;
        }

        long cooldownUntil = playerCooldowns.get(ability);
        return Math.max(0, (cooldownUntil - System.currentTimeMillis()) / 1000);
    }

    /**
     * First Form: Dancing Comet (壱いちノ型かた 新しん星せい光こう)
     * Created: 2025-06-18 21:13:16
     * @author SkyForce-6
     *
     * The most basic form of Galaxy Breathing.
     * Performs a graceful vertical slash that can be chained into multiple strikes
     * while incorporating twisting and flipping movements similar to Sun Breathing's waltz.
     */
    public void useFirstForm() {
        player.sendMessage("§9銀河 §f壱いちノ型かた 新しん星せい光こう §9(First Form: Dancing Comet)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial comet effects
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2.0f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> cometTrail = new ArrayList<>();
        final double[] currentRotation = {0.0};
        final int[] comboCount = {0};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double DAMAGE = 8.0; // Base damage
            private Vector lastDirection;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Vector direction = playerLoc.getDirection();

                // Update movement and rotation
                if (ticks % 5 == 0) { // Every 0.25 seconds
                    // Move player forward
                    Vector velocity = direction.multiply(0.8);
                    player.setVelocity(velocity);

                    // Update rotation for waltz movement
                    currentRotation[0] += Math.PI / 4;

                    // Add combo strike
                    if (comboCount[0] < 5 && random.nextFloat() < 0.4) {
                        comboCount[0]++;
                        createComboStrike(playerLoc);
                    }
                }

                // Create comet trail
                createCometEffects(playerLoc, direction);

                // Check for hits
                if (ticks % 2 == 0) {
                    checkHits(playerLoc);
                }

                lastDirection = direction;
                ticks++;
            }

            private void createCometEffects(Location loc, Vector dir) {
                // Create comet head
                for (int i = 0; i < 8; i++) {
                    Vector offset = dir.clone().multiply(random.nextDouble() * 2);
                    offset.add(new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).multiply(0.5));

                    Location particleLoc = loc.clone().add(offset);

                    // Comet particles
                    Particle.DustOptions cometColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, cometColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Add trail point
                cometTrail.add(loc.clone());
                if (cometTrail.size() > 20) {
                    cometTrail.remove(0);
                }

                // Create trail
                for (int i = 0; i < cometTrail.size(); i++) {
                    Location trailLoc = cometTrail.get(i);
                    float trailSize = 0.5f * (i / (float)cometTrail.size());

                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), trailSize);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);
                }
            }

            private void createComboStrike(Location loc) {
                // Combo strike effects
                double angle = currentRotation[0];
                Vector slashDir = new Vector(Math.cos(angle), 0, Math.sin(angle));

                for (double d = 0; d < 3; d += 0.2) {
                    Location slashLoc = loc.clone().add(slashDir.clone().multiply(d));

                    // Slash particles
                    Particle.DustOptions slashColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.FLASH, slashLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Combo sound
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.8f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4f, 1.5f);
            }

            private void checkHits(Location loc) {
                double hitRadius = 2.5;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate combo damage
                        double comboDamage = DAMAGE * (1 + (comboCount[0] * 0.2));

                        // Apply damage
                        target.damage(comboDamage, player);
                        hitEntities.add(target);

                        // Apply effects
                        if (lastDirection != null) {
                            target.setVelocity(lastDirection.clone().multiply(0.5));
                        }
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Hit flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Hit particles
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(180, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.4f, 1.2f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 1));

        // Add cooldown
        addCooldown(player, "FirstForm", 15);
    }

    /**
     * Second Form: Emitting Deity (弍にノ型かた 降こう下か彗ずい星せい)
     * Created: 2025-06-18 21:19:13
     * @author SkyForce-6
     *
     * A powerful horizontal slash that utilizes the user's full body momentum
     * to deliver a devastating decapitating strike.
     */
    public void useSecondForm() {
        player.sendMessage("§9銀河 §f弍にノ型かた 降こう下か彗ずい星せい §9(Second Form: Emitting Deity)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial deity manifestation effects
        world.playSound(startLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final double[] chargeProgress = {0.0}; // 0.0 to 1.0
        final boolean[] released = {false};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40; // 2 seconds duration
            private final double DAMAGE = 16.0; // High damage for decapitation
            private Vector slashDirection;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Charging phase (first 20 ticks)
                if (ticks < 20 && !released[0]) {
                    chargeProgress[0] = Math.min(1.0, ticks / 20.0);
                    createChargingEffect(playerLoc);

                    // Slow player during charge
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, 2));
                }
                // Release slash
                else if (!released[0]) {
                    released[0] = true;
                    slashDirection = playerLoc.getDirection();

                    // Add momentum to player
                    player.setVelocity(slashDirection.clone().multiply(1.5));

                    // Initial slash effect
                    createSlashRelease(playerLoc);
                }
                // After release effects
                else {
                    createAfterSlashEffects(playerLoc);

                    // Check for hits
                    if (ticks % 2 == 0) {
                        checkSlashHits(playerLoc);
                    }
                }

                ticks++;
            }

            private void createChargingEffect(Location loc) {
                // Deity aura particles
                for (int i = 0; i < 20; i++) {
                    double angle = (Math.PI * 2 * i) / 20;
                    double radius = 2.0 * chargeProgress[0];

                    Vector offset = new Vector(
                            Math.cos(angle) * radius,
                            0.1 * chargeProgress[0],
                            Math.sin(angle) * radius
                    );

                    Location particleLoc = loc.clone().add(offset);

                    // Aura particles
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    100 + (int)(155 * chargeProgress[0]),
                                    150 + (int)(105 * chargeProgress[0]),
                                    255
                            ),
                            1.0f
                    );
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, auraColor);

                    if (random.nextFloat() < 0.3 * chargeProgress[0]) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Charging sound
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.5f + (float)chargeProgress[0]);
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.0f + (float)chargeProgress[0]);
                }
            }

            private void createSlashRelease(Location loc) {
                // Release flash
                world.spawnParticle(Particle.FLASH, loc, 5, 0.5, 0.5, 0.5, 0);

                // Initial slash wave
                Vector right = new Vector(-slashDirection.getZ(), 0, slashDirection.getX()).normalize();

                for (double d = -3; d <= 3; d += 0.2) {
                    Location slashLoc = loc.clone().add(right.clone().multiply(d));

                    // Slash particles
                    Particle.DustOptions slashColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 1.5f);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLASH, slashLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Release sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.5f);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 2.0f);
                world.playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.5f);
            }

            private void createAfterSlashEffects(Location loc) {
                // Trail effects
                Vector right = new Vector(-slashDirection.getZ(), 0, slashDirection.getX()).normalize();
                double progress = (ticks - 20) / 20.0; // 0.0 to 1.0 after release

                for (double d = -3; d <= 3; d += 0.5) {
                    Location trailLoc = loc.clone().add(right.clone().multiply(d));

                    // Trail particles
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(180, 200, 255), 0.8f * (1.0f - (float)progress));
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);

                    if (random.nextFloat() < 0.2 * (1.0 - progress)) {
                        world.spawnParticle(Particle.END_ROD, trailLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void checkSlashHits(Location loc) {
                if (!released[0]) return;

                double slashWidth = 3.0;
                double slashHeight = 2.0;
                Vector right = new Vector(-slashDirection.getZ(), 0, slashDirection.getX()).normalize();

                for (Entity entity : world.getNearbyEntities(loc, slashWidth, slashHeight, slashWidth)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply effects
                        Vector knockback = slashDirection.clone().multiply(1.2);
                        knockback.setY(0.2); // Small upward component
                        target.setVelocity(knockback);

                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Hit flash
                world.spawnParticle(Particle.FLASH, loc, 3, 0.2, 0.2, 0.2, 0);

                // Hit particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 0.5f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1));

        // Add cooldown
        addCooldown(player, "SecondForm", 20);
    }

    /**
     * Third Form: Shining Blaze Course (参さんノ型かた 螺ら旋せん雲うん)
     * Created: 2025-06-18 21:22:07
     * @author SkyForce-6
     *
     * A spinning technique similar to Hinokami Kagura's Burning Bones Summer Sun,
     * where the user performs two consecutive rotating slashes infused with galactic energy.
     */
    public void useThirdForm() {
        player.sendMessage("§9銀河 §f参さんノ型かた 螺ら旋せん雲うん §9(Third Form: Shining Blaze Course)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial spin effects
        world.playSound(startLoc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final double[] spinAngle = {0.0};
        final int[] spinCount = {0};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 80; // 4 seconds duration
            private final double DAMAGE = 12.0; // Damage per spin hit
            private final double SPIN_RADIUS = 3.0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Determine spin phase
                if (ticks < 35) { // First spin
                    executeSpinPhase(playerLoc, 1);
                } else if (ticks == 35) { // Brief pause between spins
                    spinCount[0]++;
                    hitEntities.clear(); // Reset hit entities for second spin
                    createTransitionEffect(playerLoc);
                } else if (ticks < 70) { // Second spin
                    executeSpinPhase(playerLoc, 2);
                }

                ticks++;
            }

            private void executeSpinPhase(Location loc, int phase) {
                // Update spin angle
                spinAngle[0] += Math.PI / 8; // Rotation speed

                // Create spin effects
                createSpinningBlade(loc, phase);

                // Check for hits every other tick
                if (ticks % 2 == 0) {
                    checkSpinHits(loc, phase);
                }

                // Add momentum to player
                if (ticks % 5 == 0) {
                    Vector momentum = new Vector(
                            Math.cos(spinAngle[0]),
                            0,
                            Math.sin(spinAngle[0])
                    ).multiply(0.3);
                    player.setVelocity(momentum);
                }
            }

            private void createSpinningBlade(Location loc, int phase) {
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double offsetAngle = angle + spinAngle[0];
                    Vector offset = new Vector(
                            Math.cos(offsetAngle) * SPIN_RADIUS,
                            0,
                            Math.sin(offsetAngle) * SPIN_RADIUS
                    );

                    Location bladeLoc = loc.clone().add(offset);

                    // Blade particles
                    Particle.DustOptions bladeColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    100 + (phase * 50),
                                    150 + (phase * 30),
                                    255
                            ),
                            1.2f
                    );
                    world.spawnParticle(Particle.DUST, bladeLoc, 1, 0, 0, 0, 0, bladeColor);

                    // Additional effects based on phase
                    if (phase == 1) {
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.END_ROD, bladeLoc, 1, 0, 0, 0, 0.02);
                        }
                    } else {
                        if (random.nextFloat() < 0.4) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, bladeLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Spin sounds
                if (ticks % 4 == 0) {
                    world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.0f + (phase * 0.2f));
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.5f + (phase * 0.2f));
                }
            }

            private void createTransitionEffect(Location loc) {
                // Transition flash
                world.spawnParticle(Particle.FLASH, loc, 5, 0.5, 0.5, 0.5, 0);

                // Transition particles
                for (int i = 0; i < 50; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * SPIN_RADIUS);

                    Location particleLoc = loc.clone().add(spread);

                    Particle.DustOptions transitionColor = new Particle.DustOptions(
                            Color.fromRGB(180, 200, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, transitionColor);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.05);
                }

                // Transition sounds
                world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);
                world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
            }

            private void checkSpinHits(Location loc, int phase) {
                for (Entity entity : world.getNearbyEntities(loc, SPIN_RADIUS, 2.0, SPIN_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate phase damage
                        double phaseDamage = DAMAGE * (1 + (phase * 0.2));

                        // Apply damage
                        target.damage(phaseDamage, player);
                        hitEntities.add(target);

                        // Apply effects
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(0.8).setY(0.2);
                        target.setVelocity(knockback);

                        if (phase == 2) {
                            target.setFireTicks(60); // Set on fire in second phase
                        }

                        // Create hit effect
                        createHitEffect(target.getLocation(), phase);
                    }
                }
            }

            private void createHitEffect(Location loc, int phase) {
                // Hit flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Hit particles
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(200 + (phase * 20), 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);

                    if (phase == 2) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, hitLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 1.0f + (phase * 0.2f));
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.4f, 1.2f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));

        // Add cooldown
        addCooldown(player, "ThirdForm", 25);
    }

    /**
     * Fourth Form: Triple Galaxy (肆しノ型かた 夜よ明あけ日び)
     * Created: 2025-06-18 21:25:48
     * @author SkyForce-6
     *
     * A devastating combination of one vertical and two overlapping horizontal slashes,
     * creating a triple-layered galactic pattern that tears through space.
     */
    public void useFourthForm() {
        player.sendMessage("§9銀河 §f肆しノ型かた 夜よ明あけ日び §9(Fourth Form: Triple Galaxy)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial activation effects
        world.playSound(startLoc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 2.0f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final int[] slashPhase = {0}; // 0: vertical, 1: first horizontal, 2: second horizontal

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double DAMAGE = 14.0; // Base damage
            private Vector lastSlashDirection;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute slashes based on timing
                if (ticks == 0) { // Vertical slash
                    executeVerticalSlash(playerLoc);
                } else if (ticks == 20) { // First horizontal slash
                    slashPhase[0] = 1;
                    executeHorizontalSlash(playerLoc, 1);
                } else if (ticks == 40) { // Second horizontal slash
                    slashPhase[0] = 2;
                    executeHorizontalSlash(playerLoc, 2);
                }

                // Maintain slash effects
                if (ticks < 20) {
                    maintainVerticalEffects(playerLoc);
                } else if (ticks < 40) {
                    maintainHorizontalEffects(playerLoc, 1);
                } else {
                    maintainHorizontalEffects(playerLoc, 2);
                }

                // Check for hits
                if (ticks % 2 == 0) {
                    checkSlashHits(playerLoc);
                }

                ticks++;
            }

            private void executeVerticalSlash(Location loc) {
                lastSlashDirection = loc.getDirection();

                // Initial vertical slash effect
                for (double y = -2; y <= 2; y += 0.2) {
                    Location slashLoc = loc.clone().add(0, y, 0);

                    // Slash particles
                    Particle.DustOptions slashColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 1.5f);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, slashLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Vertical slash sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 2.0f);
            }

            private void executeHorizontalSlash(Location loc, int phase) {
                lastSlashDirection = loc.getDirection();
                Vector right = new Vector(-lastSlashDirection.getZ(), 0, lastSlashDirection.getX()).normalize();

                // Initial horizontal slash effect
                for (double d = -3; d <= 3; d += 0.2) {
                    Location slashLoc = loc.clone().add(right.clone().multiply(d));

                    // Slash particles with phase-specific colors
                    Particle.DustOptions slashColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    150 + (phase * 30),
                                    200 + (phase * 20),
                                    255
                            ),
                            1.5f
                    );
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);

                    if (random.nextFloat() < 0.4) {
                        world.spawnParticle(Particle.END_ROD, slashLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Horizontal slash sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.5f + (phase * 0.2f));
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.0f + (phase * 0.2f));
            }

            private void maintainVerticalEffects(Location loc) {
                double progress = ticks / 20.0;

                for (double y = -2; y <= 2; y += 0.4) {
                    Location effectLoc = loc.clone().add(
                            lastSlashDirection.getX() * progress * 2,
                            y,
                            lastSlashDirection.getZ() * progress * 2
                    );

                    // Trail particles
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.0f * (1.0f - (float)progress));
                    world.spawnParticle(Particle.DUST, effectLoc, 1, 0, 0, 0, 0, trailColor);
                }
            }

            private void maintainHorizontalEffects(Location loc, int phase) {
                double progress = (ticks - (20 * phase)) / 20.0;
                Vector right = new Vector(-lastSlashDirection.getZ(), 0, lastSlashDirection.getX()).normalize();

                for (double d = -3; d <= 3; d += 0.4) {
                    Location effectLoc = loc.clone().add(right.clone().multiply(d)).add(
                            lastSlashDirection.getX() * progress * 2,
                            0,
                            lastSlashDirection.getZ() * progress * 2
                    );

                    // Trail particles
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    100 + (phase * 30),
                                    150 + (phase * 20),
                                    255
                            ),
                            1.0f * (1.0f - (float)progress)
                    );
                    world.spawnParticle(Particle.DUST, effectLoc, 1, 0, 0, 0, 0, trailColor);
                }
            }

            private void checkSlashHits(Location loc) {
                double hitRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate phase damage
                        double phaseDamage = DAMAGE * (1 + (slashPhase[0] * 0.2));

                        // Apply damage
                        target.damage(phaseDamage, player);
                        hitEntities.add(target);

                        // Apply effects based on slash phase
                        switch (slashPhase[0]) {
                            case 0: // Vertical slash
                                target.setVelocity(new Vector(0, 0.5, 0));
                                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                                break;
                            case 1: // First horizontal
                                Vector horizontalKnockback = lastSlashDirection.clone().multiply(0.8);
                                target.setVelocity(horizontalKnockback);
                                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                                break;
                            case 2: // Second horizontal
                                Vector finalKnockback = lastSlashDirection.clone().multiply(1.2);
                                target.setVelocity(finalKnockback);
                                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                                break;
                        }

                        // Create hit effect
                        createHitEffect(target.getLocation());
                    }
                }
            }

            private void createHitEffect(Location loc) {
                // Hit flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Hit particles
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(180 + (slashPhase[0] * 20), 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.0f + (slashPhase[0] * 0.2f));
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.6f, 1.5f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));

        // Add cooldown
        addCooldown(player, "FourthForm", 25);
    }

    /**
     * Fifth Form: Cosmic Dragon (伍ごノ型かた 宇う宙ちゅう竜りゅう)
     * Created: 2025-06-18 21:30:09
     * @author SkyForce-6
     *
     * A transcendent state where the user enters a selfless, zen-like condition,
     * allowing them to deliver multiple precise strikes aimed at decapitation.
     * The form manifests as a cosmic dragon striking at multiple targets.
     */
    public void useFifthForm() {
        player.sendMessage("§9銀河 §f伍ごノ型かた 宇う宙ちゅう竜りゅう §9(Fifth Form: Cosmic Dragon)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial transcendence effects
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> dragonPath = new ArrayList<>();
        final double[] currentAngle = {0.0};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 100; // 5 seconds duration
            private final double DAMAGE = 16.0; // High damage for decapitation
            private Location lastTargetLoc = null;
            private Entity currentTarget = null;
            private int strikePhase = 0;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Enter zen state (first 20 ticks)
                if (ticks < 20) {
                    createZenEffect(playerLoc);
                }
                // Execute strikes (20-80 ticks)
                else if (ticks < 80) {
                    if (ticks % 15 == 0) { // New strike every 15 ticks
                        findAndStrikeTarget(playerLoc);
                    }
                    maintainDragonEffects(playerLoc);
                }
                // Final flourish (last 20 ticks)
                else {
                    createFinalEffect(playerLoc);
                }

                ticks++;
            }

            private void createZenEffect(Location loc) {
                double progress = ticks / 20.0;

                // Rising particles
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8 + currentAngle[0];
                    double radius = 2.0 * progress;

                    Vector offset = new Vector(
                            Math.cos(angle) * radius,
                            progress * 2,
                            Math.sin(angle) * radius
                    );

                    Location particleLoc = loc.clone().add(offset);

                    // Zen state particles
                    Particle.DustOptions zenColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, zenColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                currentAngle[0] += Math.PI / 16;

                // Zen state sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.5f + (float)progress);
                    world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.3f, 0.7f + (float)progress);
                }
            }

            private void findAndStrikeTarget(Location loc) {
                double searchRadius = 10.0;
                Entity newTarget = null;
                double closestDist = Double.MAX_VALUE;

                // Find nearest valid target
                for (Entity entity : world.getNearbyEntities(loc, searchRadius, searchRadius, searchRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        double dist = entity.getLocation().distance(loc);
                        if (dist < closestDist) {
                            closestDist = dist;
                            newTarget = entity;
                        }
                    }
                }

                if (newTarget != null) {
                    currentTarget = newTarget;
                    lastTargetLoc = newTarget.getLocation();
                    strikePhase++;
                    executeDragonStrike(loc, newTarget.getLocation());
                }
            }

            private void executeDragonStrike(Location startLoc, Location targetLoc) {
                Vector direction = targetLoc.subtract(startLoc).toVector().normalize();
                double distance = startLoc.distance(targetLoc);

                // Create dragon head at start
                createDragonHead(startLoc);

                // Create path points
                for (double d = 0; d <= distance; d += 0.5) {
                    // Add some waviness to the path
                    double wave = Math.sin(d * 0.5) * 0.3;
                    Vector offset = new Vector(-direction.getZ(), wave, direction.getX()).multiply(wave);

                    Location pathLoc = startLoc.clone().add(direction.clone().multiply(d)).add(offset);
                    dragonPath.add(pathLoc);
                }

                // Strike effects
                world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.5f);
                world.playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
            }

            private void createDragonHead(Location loc) {
                // Dragon head particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(0.5);

                    Location headLoc = loc.clone().add(spread);

                    Particle.DustOptions dragonColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.5f);
                    world.spawnParticle(Particle.DUST, headLoc, 1, 0, 0, 0, 0, dragonColor);
                    world.spawnParticle(Particle.DRAGON_BREATH, headLoc, 1, 0.1, 0.1, 0.1, 0.02);
                }
            }

            private void maintainDragonEffects(Location loc) {
                // Update dragon path
                Iterator<Location> it = dragonPath.iterator();
                while (it.hasNext()) {
                    Location pathLoc = it.next();

                    // Path particles
                    Particle.DustOptions pathColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, pathColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, pathLoc, 1, 0, 0, 0, 0.02);
                    }

                    // Remove old path points
                    if (random.nextFloat() < 0.1) {
                        it.remove();
                    }
                }

                // Check for hits if we have a current target
                if (currentTarget != null && !hitEntities.contains(currentTarget)) {
                    checkDragonHit((LivingEntity)currentTarget);
                }
            }

            private void checkDragonHit(LivingEntity target) {
                // Apply damage and effects
                target.damage(DAMAGE + (strikePhase * 2), player);
                hitEntities.add(target);

                // Special effects
                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2));

                // Create hit effect
                createHitEffect(target.getLocation());

                // Reset current target
                currentTarget = null;
            }

            private void createHitEffect(Location loc) {
                // Hit flash
                world.spawnParticle(Particle.FLASH, loc, 3, 0.2, 0.2, 0.2, 0);

                // Dragon bite particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                    world.spawnParticle(Particle.DRAGON_BREATH, hitLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Hit sounds
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, 0.8f, 1.5f);
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
            }

            private void createFinalEffect(Location loc) {
                // Final dragon dissipation
                for (int i = 0; i < 50; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 5);

                    Location finalLoc = loc.clone().add(spread);

                    Particle.DustOptions finalColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, finalLoc, 1, 0, 0, 0, 0, finalColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.DRAGON_BREATH, finalLoc, 1, 0, 0, 0, 0.05);
                    }
                }

                // Final sounds
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

        // Add cooldown
        addCooldown(player, "FifthForm", 35);
    }

    /**
     * Sixth Form: Azure Universe (陸ろくノ型かた 楕だ円えん状じょう)
     * Created: 2025-06-18 21:33:57
     * @author SkyForce-6
     *
     * A spiritual technique where the user enters a state of perfect tranquility,
     * flowing like a cosmic spirit while delivering devastating attacks.
     * The form manifests as an azure elliptical galaxy surrounding the user.
     */
    public void useSixthForm() {
        player.sendMessage("§9銀河 §f陸ろくノ型かた 楕だ円えん状じょう §9(Sixth Form: Azure Universe)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial spirit manifestation effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> spiritPath = new ArrayList<>();
        final double[] spiritAngle = {0.0};
        final Vector[] lastDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 120; // 6 seconds duration
            private final double DAMAGE = 15.0; // High continuous damage
            private final double SPIRIT_RADIUS = 4.0;
            private Location lastLoc = player.getLocation();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Initial spirit state (first 20 ticks)
                if (ticks < 20) {
                    enterSpiritState(playerLoc);
                }
                // Main spirit attacks (20-100 ticks)
                else if (ticks < 100) {
                    executeSpiritAttacks(playerLoc);
                }
                // Final dissipation (last 20 ticks)
                else {
                    createDissipationEffect(playerLoc);
                }

                lastLoc = playerLoc.clone();
                ticks++;
            }

            private void enterSpiritState(Location loc) {
                double progress = ticks / 20.0;

                // Rising azure particles
                for (int i = 0; i < 16; i++) {
                    double angle = (Math.PI * 2 * i) / 16 + spiritAngle[0];
                    double radius = SPIRIT_RADIUS * progress;

                    Vector offset = new Vector(
                            Math.cos(angle) * radius,
                            progress * 2,
                            Math.sin(angle) * radius
                    );

                    Location particleLoc = loc.clone().add(offset);

                    // Spirit manifestation particles
                    Particle.DustOptions spiritColor = new Particle.DustOptions(
                            Color.fromRGB(100, 180, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, spiritColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                spiritAngle[0] += Math.PI / 32;

                // Manifestation sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 0.5f + (float)progress);
                    world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.3f, 0.7f + (float)progress);
                }
            }

            private void executeSpiritAttacks(Location loc) {
                // Update spirit movement
                updateSpiritMovement(loc);

                // Create elliptical pattern
                createEllipticalPattern(loc);

                // Check for hits
                if (ticks % 2 == 0) {
                    checkSpiritHits(loc);
                }

                // Spirit movement sounds
                if (ticks % 10 == 0) {
                    world.playSound(loc, Sound.ENTITY_VEX_AMBIENT, 0.5f, 1.5f);
                }
            }

            private void updateSpiritMovement(Location loc) {
                // Smooth direction changes
                Vector newDir = loc.getDirection();
                Vector smoothDir = lastDirection[0].clone().add(newDir).normalize();
                lastDirection[0] = smoothDir;

                // Add location to spirit path
                spiritPath.add(loc.clone());
                if (spiritPath.size() > 20) {
                    spiritPath.remove(0);
                }

                // Create spirit trail
                for (Location pathLoc : spiritPath) {
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, trailColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, pathLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void createEllipticalPattern(Location loc) {
                spiritAngle[0] += Math.PI / 32;

                // Create elliptical galaxy pattern
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8 + spiritAngle[0];

                    // Elliptical transformation
                    double a = SPIRIT_RADIUS;
                    double b = SPIRIT_RADIUS * 0.6;
                    double x = Math.cos(angle) * a;
                    double z = Math.sin(angle) * b;

                    // Rotate ellipse based on movement
                    double rotatedX = x * Math.cos(spiritAngle[0]) - z * Math.sin(spiritAngle[0]);
                    double rotatedZ = x * Math.sin(spiritAngle[0]) + z * Math.cos(spiritAngle[0]);

                    Location ellipseLoc = loc.clone().add(rotatedX, 0, rotatedZ);

                    // Ellipse particles
                    Particle.DustOptions ellipseColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, ellipseLoc, 1, 0, 0, 0, 0, ellipseColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.WITCH, ellipseLoc, 1, 0, 0, 0, 0);
                    }
                }
            }

            private void checkSpiritHits(Location loc) {
                for (Entity entity : world.getNearbyEntities(loc, SPIRIT_RADIUS, SPIRIT_RADIUS, SPIRIT_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate spirit damage
                        double spiritDamage = DAMAGE * (1 + (spiritPath.size() * 0.05));

                        // Apply damage and effects
                        target.damage(spiritDamage, player);
                        hitEntities.add(target);

                        // Apply spirit effects
                        target.setVelocity(lastDirection[0].clone().multiply(0.5));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));

                        // Create spirit impact
                        createSpiritImpact(target.getLocation());
                    }
                }
            }

            private void createSpiritImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Spirit impact particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.WITCH, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_VEX_HURT, 0.8f, 1.5f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.6f, 1.2f);
            }

            private void createDissipationEffect(Location loc) {
                double progress = (ticks - 100) / 20.0;

                // Spirit dissipation particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(SPIRIT_RADIUS * (1 - progress));

                    Location dissipationLoc = loc.clone().add(spread);

                    Particle.DustOptions dissipationColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 0.8f * (1.0f - (float)progress));
                    world.spawnParticle(Particle.DUST, dissipationLoc, 1, 0, 0, 0, 0, dissipationColor);

                    if (random.nextFloat() < 0.3 * (1 - progress)) {
                        world.spawnParticle(Particle.WITCH, dissipationLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Dissipation sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f * (1.0f - (float)progress), 0.5f);
                    world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.3f * (1.0f - (float)progress), 0.7f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 120, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 120, 0));

        // Add cooldown
        addCooldown(player, "SixthForm", 40);
    }

    /**
     * Seventh Form: Enveloping Galaxy (漆しちノ型かた 渦うず巻まきの力ちから)
     * Created: 2025-06-19 07:44:59
     * @author SkyForce-6
     *
     * A defensive and offensive technique that creates a spiral galaxy formation around the user,
     * allowing them to both ward off incoming attacks while closing in on their target.
     */
    public void useSeventhForm() {
        player.sendMessage("§9銀河 §f漆しちノ型かた 渦うず巻まきの力ちから §9(Seventh Form: Enveloping Galaxy)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial galaxy formation effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> spiralPoints = new ArrayList<>();
        final double[] spiralAngle = {0.0};
        final Vector[] targetDirection = {null};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 140; // 7 seconds duration
            private final double DAMAGE = 12.0; // Balanced damage
            private final double SPIRAL_RADIUS = 5.0;
            private Entity currentTarget = null;
            private boolean targetLocked = false;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Find and lock onto target (first 20 ticks)
                if (!targetLocked && ticks < 20) {
                    findAndLockTarget(playerLoc);
                }
                // Execute spiral galaxy technique
                else if (ticks < 120) {
                    executeSpiral(playerLoc);
                }
                // Dissipate effects
                else {
                    createDissipation(playerLoc);
                }

                ticks++;
            }

            private void findAndLockTarget(Location loc) {
                if (currentTarget == null) {
                    double closest = Double.MAX_VALUE;

                    // Find nearest valid target
                    for (Entity entity : world.getNearbyEntities(loc, 15, 15, 15)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            double dist = entity.getLocation().distance(loc);
                            if (dist < closest) {
                                closest = dist;
                                currentTarget = entity;
                            }
                        }
                    }

                    if (currentTarget != null) {
                        targetDirection[0] = currentTarget.getLocation().subtract(loc).toVector().normalize();
                        targetLocked = true;
                    } else {
                        // No target found, use player's look direction
                        targetDirection[0] = player.getLocation().getDirection();
                        targetLocked = true;
                    }
                }
            }

            private void executeSpiral(Location loc) {
                updateSpiralFormation(loc);
                createSpiralEffects(loc);
                if (ticks % 2 == 0) {
                    checkSpiralCollisions(loc);
                }
                moveTowardsTarget(loc);
            }

            private void updateSpiralFormation(Location loc) {
                spiralAngle[0] += Math.PI / 16; // Rotation speed

                // Calculate spiral arms
                for (int arm = 0; arm < 2; arm++) {
                    double armOffset = (Math.PI * arm) / 2;
                    double r = 0;

                    for (double theta = 0; theta < Math.PI * 4; theta += Math.PI / 8) {
                        r = theta * 0.3; // Increasing radius for spiral effect

                        double x = r * Math.cos(theta + spiralAngle[0] + armOffset);
                        double z = r * Math.sin(theta + spiralAngle[0] + armOffset);

                        Vector spiralVector = new Vector(x, 0, z);
                        if (targetDirection[0] != null) {
                            // Align spiral with movement direction
                            double angle = Math.atan2(targetDirection[0].getZ(), targetDirection[0].getX());
                            double rotatedX = x * Math.cos(angle) - z * Math.sin(angle);
                            double rotatedZ = x * Math.sin(angle) + z * Math.cos(angle);
                            spiralVector = new Vector(rotatedX, 0, rotatedZ);
                        }

                        Location spiralLoc = loc.clone().add(spiralVector);
                        spiralPoints.add(spiralLoc);
                    }
                }

                // Trim old points
                while (spiralPoints.size() > 64) {
                    spiralPoints.remove(0);
                }
            }

            private void createSpiralEffects(Location loc) {
                // Create spiral galaxy particles
                for (Location point : spiralPoints) {
                    // Main spiral particles
                    Particle.DustOptions spiralColor = new Particle.DustOptions(
                            Color.fromRGB(100, 150, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, spiralColor);

                    // Additional effects
                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0.02);
                    }
                }

                // Defensive barrier particles
                if (ticks % 3 == 0) {
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle + spiralAngle[0]) * SPIRAL_RADIUS;
                        double z = Math.sin(angle + spiralAngle[0]) * SPIRAL_RADIUS;

                        Location barrierLoc = loc.clone().add(x, 1, z);

                        Particle.DustOptions barrierColor = new Particle.DustOptions(
                                Color.fromRGB(150, 200, 255), 1.0f);
                        world.spawnParticle(Particle.DUST, barrierLoc, 1, 0, 0, 0, 0, barrierColor);
                        world.spawnParticle(Particle.WITCH, barrierLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Sound effects
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.5f);
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 1.0f);
                }
            }

            private void checkSpiralCollisions(Location loc) {
                // Check for entities in spiral range
                for (Entity entity : world.getNearbyEntities(loc, SPIRAL_RADIUS, SPIRAL_RADIUS, SPIRAL_RADIUS)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on spiral intensity
                        double spiralDamage = DAMAGE * (1 + (spiralPoints.size() * 0.02));

                        // Apply damage and effects
                        target.damage(spiralDamage, player);
                        hitEntities.add(target);

                        // Apply spiral effects
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(0.5).setY(0.2);
                        target.setVelocity(knockback);

                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));

                        // Create impact effect
                        createSpiralImpact(target.getLocation());
                    }
                }
            }

            private void createSpiralImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Spiral impact particles
                for (int i = 0; i < 15; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(150, 200, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.WITCH, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.4f, 1.2f);
            }

            private void moveTowardsTarget(Location loc) {
                if (currentTarget != null && currentTarget.isValid()) {
                    // Update target direction
                    targetDirection[0] = currentTarget.getLocation().subtract(loc)
                            .toVector().normalize();

                    // Move player towards target while maintaining spiral
                    if (ticks % 5 == 0) {
                        Vector movement = targetDirection[0].clone().multiply(0.5);
                        player.setVelocity(movement);
                    }
                }
            }

            private void createDissipation(Location loc) {
                double progress = (ticks - 120) / 20.0;

                // Spiral dissipation
                for (Location point : spiralPoints) {
                    if (random.nextFloat() > progress) {
                        Particle.DustOptions dissipateColor = new Particle.DustOptions(
                                Color.fromRGB(150, 200, 255), 0.8f * (1.0f - (float)progress));
                        world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dissipateColor);
                    }
                }

                // Final particles
                if (ticks % 2 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                            0.5f * (1.0f - (float)progress), 1.0f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 140, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 140, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 140, 0));

        // Add cooldown
        addCooldown(player, "SeventhForm", 45);
    }

    /**
     * Eighth Form: Divine Chariot (捌はちノ型かた 無む限げん烏お空くう)
     * Created: 2025-06-19 07:49:23
     * @author SkyForce-6
     *
     * An aerial technique where the user performs a graceful backflip combined with
     * an upward sword slash aimed at decapitation, channeling the power of infinite space.
     */
    public void useEighthForm() {
        player.sendMessage("§9銀河 §f捌はちノ型かた 無む限げん烏お空くう §9(Eighth Form: Divine Chariot)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial leap effects
        world.playSound(startLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> slashPath = new ArrayList<>();
        final double[] rotationAngle = {0.0};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 40; // 2 seconds duration
            private final double DAMAGE = 20.0; // High damage for decisive strike
            private boolean hasStruck = false;
            private Location peakLocation = null;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute technique phases
                if (ticks < 10) { // Initial backflip
                    executeBackflip(playerLoc);
                } else if (ticks < 20) { // Peak and slash
                    executeSlash(playerLoc);
                } else { // Landing and aftermath
                    executeLanding(playerLoc);
                }

                ticks++;
            }

            private void executeBackflip(Location loc) {
                // Calculate backflip trajectory
                double progress = ticks / 10.0;
                Vector velocity = new Vector(
                        -loc.getDirection().getX() * 0.5,
                        1.2 - (progress * 0.3),
                        -loc.getDirection().getZ() * 0.5
                );

                player.setVelocity(velocity);

                // Backflip particles
                rotationAngle[0] += Math.PI / 4;
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8 + rotationAngle[0];
                    Vector offset = new Vector(
                            Math.cos(angle) * 0.8,
                            0,
                            Math.sin(angle) * 0.8
                    );

                    Location particleLoc = loc.clone().add(offset);

                    // Divine energy particles
                    Particle.DustOptions divineColor = new Particle.DustOptions(
                            Color.fromRGB(200, 220, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, divineColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Ascension sounds
                if (ticks % 2 == 0) {
                    world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 1.5f + (float)(progress * 0.5f));
                }
            }

            private void executeSlash(Location loc) {
                if (peakLocation == null) {
                    peakLocation = loc.clone();

                    // Peak reached effects
                    world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 2.0f);
                    world.spawnParticle(Particle.FLASH, loc, 5, 0.5, 0.5, 0.5, 0);
                }

                // Create divine slash
                double slashProgress = (ticks - 10) / 10.0;
                Vector slashDir = loc.getDirection();

                // Create vertical slash pattern
                for (double y = -2; y <= 2; y += 0.2) {
                    Vector offset = slashDir.clone().multiply(slashProgress * 2);
                    offset.setY(y);

                    Location slashLoc = peakLocation.clone().add(offset);
                    slashPath.add(slashLoc);

                    // Slash particles
                    Particle.DustOptions slashColor = new Particle.DustOptions(
                            Color.fromRGB(220, 235, 255), 1.5f);
                    world.spawnParticle(Particle.DUST, slashLoc, 1, 0, 0, 0, 0, slashColor);

                    if (random.nextFloat() < 0.4) {
                        world.spawnParticle(Particle.END_ROD, slashLoc, 1, 0, 0, 0, 0.02);
                        world.spawnParticle(Particle.FLASH, slashLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Check for hits during slash
                if (!hasStruck) {
                    checkSlashHits(loc);
                }
            }

            private void checkSlashHits(Location loc) {
                double hitRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply massive damage
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Apply divine effects
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(1.5).setY(0.8);
                        target.setVelocity(knockback);

                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));

                        // Create divine impact
                        createDivineImpact(target.getLocation());

                        hasStruck = true;
                    }
                }
            }

            private void createDivineImpact(Location loc) {
                // Divine explosion
                world.spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);

                // Divine impact particles
                for (int i = 0; i < 40; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2);

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 255), 1.2f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.END_ROD, impactLoc, 1, 0.1, 0.1, 0.1, 0.1);
                }

                // Divine impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                world.playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.8f, 2.0f);
            }

            private void executeLanding(Location loc) {
                double progress = (ticks - 20) / 20.0;

                // Maintain slash trail
                for (Location pathLoc : slashPath) {
                    if (random.nextFloat() > progress) {
                        Particle.DustOptions trailColor = new Particle.DustOptions(
                                Color.fromRGB(180, 200, 255), 0.8f * (1.0f - (float)progress));
                        world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, trailColor);
                    }
                }

                // Landing effects
                if (ticks == 20) {
                    world.playSound(loc, Sound.BLOCK_NETHERITE_BLOCK_FALL, 0.8f, 1.2f);
                    world.spawnParticle(Particle.CLOUD, loc, 20, 0.5, 0, 0.5, 0.1);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 2));

        // Add cooldown
        addCooldown(player, "EighthForm", 30);
    }

    /**
     * Ninth Form: Star Curse (至し高こう型がた)
     * Created: 2025-06-19 07:54:39
     * @author SkyForce-6
     *
     * A devastating two-part technique combining a vertical aerial slash
     * followed by a ground-based horizontal strike, resembling a falling star's curse.
     */
    public void useNinthForm() {
        player.sendMessage("§9銀河 §f至し高こう型がた §9(Ninth Form: Star Curse)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial activation effects
        world.playSound(startLoc, Sound.ENTITY_PHANTOM_DEATH, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 2.0f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> slashPath = new ArrayList<>();
        final boolean[] hasLanded = {false};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double VERTICAL_DAMAGE = 18.0;
            private final double HORIZONTAL_DAMAGE = 16.0;
            private Location jumpPeakLoc = null;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute technique phases
                if (ticks < 20) { // Vertical slash phase
                    executeVerticalSlash(playerLoc);
                } else if (ticks < 40) { // Landing and horizontal slash phase
                    executeHorizontalSlash(playerLoc);
                } else { // Aftermath effects
                    createAftermath(playerLoc);
                }

                ticks++;
            }

            private void executeVerticalSlash(Location loc) {
                // Initial jump for vertical slash
                if (ticks == 0) {
                    Vector jumpVelocity = new Vector(0, 1.2, 0);
                    player.setVelocity(jumpVelocity);
                }

                // Track jump peak
                if (ticks == 10) {
                    jumpPeakLoc = loc.clone();
                }

                // Create cursed star trail
                double progress = ticks / 20.0;
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8;
                    Vector offset = new Vector(
                            Math.cos(angle) * 0.5,
                            progress * 2,
                            Math.sin(angle) * 0.5
                    );

                    Location starLoc = loc.clone().add(offset);

                    // Star curse particles
                    Particle.DustOptions curseColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 50), 1.2f);
                    world.spawnParticle(Particle.DUST, starLoc, 1, 0, 0, 0, 0, curseColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, starLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Check for vertical slash hits
                if (ticks > 10 && jumpPeakLoc != null) {
                    checkVerticalHits(loc);
                }

                // Vertical slash sounds
                if (ticks % 4 == 0) {
                    world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 2.0f);
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.5f);
                }
            }

            private void checkVerticalHits(Location loc) {
                double hitRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply vertical slash damage
                        target.damage(VERTICAL_DAMAGE, player);
                        hitEntities.add(target);

                        // Apply curse effects
                        target.setVelocity(new Vector(0, 0.8, 0));
                        target.setFireTicks(100); // Cursed flames
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));

                        // Create curse impact
                        createCurseImpact(target.getLocation(), true);
                    }
                }
            }

            private void executeHorizontalSlash(Location loc) {
                if (!hasLanded[0] && player.isOnGround()) {
                    hasLanded[0] = true;

                    // Landing impact
                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                    world.spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);

                    // Prepare horizontal slash
                    Vector slashDirection = loc.getDirection();

                    // Create horizontal curse wave
                    for (double d = -3; d <= 3; d += 0.2) {
                        Vector perpendicular = new Vector(-slashDirection.getZ(), 0, slashDirection.getX());
                        Location slashLoc = loc.clone().add(perpendicular.multiply(d));
                        slashPath.add(slashLoc);
                    }
                }

                if (hasLanded[0]) {
                    // Maintain curse wave
                    for (Location pathLoc : slashPath) {
                        // Curse wave particles
                        Particle.DustOptions waveColor = new Particle.DustOptions(
                                Color.fromRGB(200, 50, 50), 1.5f);
                        world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, waveColor);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, pathLoc, 1, 0.1, 0.1, 0.1, 0);
                    }

                    // Check for horizontal slash hits
                    checkHorizontalHits(loc);
                }
            }

            private void checkHorizontalHits(Location loc) {
                double hitRadius = 4.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, 2.0, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply horizontal slash damage
                        target.damage(HORIZONTAL_DAMAGE, player);
                        hitEntities.add(target);

                        // Apply curse effects
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(1.2).setY(0.2);
                        target.setVelocity(knockback);

                        target.setFireTicks(80); // Cursed flames
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));

                        // Create curse impact
                        createCurseImpact(target.getLocation(), false);
                    }
                }
            }

            private void createCurseImpact(Location loc, boolean isVertical) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Curse impact particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 50, 50), 1.0f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                if (isVertical) {
                    world.playSound(loc, Sound.ENTITY_BLAZE_HURT, 0.8f, 0.5f);
                } else {
                    world.playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.8f, 0.5f);
                }
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 0.5f);
            }

            private void createAftermath(Location loc) {
                double progress = (ticks - 40) / 20.0;

                // Curse dissipation
                for (Location pathLoc : slashPath) {
                    if (random.nextFloat() > progress) {
                        Particle.DustOptions aftermathColor = new Particle.DustOptions(
                                Color.fromRGB(150, 50, 50), 0.8f * (1.0f - (float)progress));
                        world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, aftermathColor);

                        if (random.nextFloat() < 0.2 * (1.0 - progress)) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, pathLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Aftermath sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT,
                            0.4f * (1.0f - (float)progress), 0.5f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20, 2));

        // Add cooldown
        addCooldown(player, "NinthForm", 35);
    }

    /**
     * Tenth Form: Peaceful Lion (闇やみノ型かた 暗あん実じっ体たい)
     * Created: 2025-06-19 07:57:32
     * @author SkyForce-6
     *
     * A deceptive technique that creates an illusory strike, confusing the opponent
     * into believing they've evaded while actually taking unpredictable damage.
     * The attack manifests as a peaceful lion's subtle yet deadly strike.
     */
    public void useTenthForm() {
        player.sendMessage("§9銀河 §f闇やみノ型かた 暗あん実じっ体たい §9(Tenth Form: Peaceful Lion)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial illusion effects
        world.playSound(startLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f);
        world.playSound(startLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.8f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> illusionPath = new ArrayList<>();
        final Vector[] strikeDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double BASE_DAMAGE = 14.0;
            private boolean hasStruck = false;
            private Location illusionLoc = null;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute technique phases
                if (ticks < 20) { // Illusion preparation
                    createIllusion(playerLoc);
                } else if (ticks < 40) { // Deceptive strike
                    executeDeceptiveStrike(playerLoc);
                } else { // Aftermath
                    createAftermath(playerLoc);
                }

                ticks++;
            }

            private void createIllusion(Location loc) {
                if (illusionLoc == null) {
                    illusionLoc = loc.clone();
                }

                double progress = ticks / 20.0;

                // Create peaceful lion aura
                for (int i = 0; i < 12; i++) {
                    double angle = (Math.PI * 2 * i) / 12;
                    Vector offset = new Vector(
                            Math.cos(angle) * (2 - progress),
                            0.5 * progress,
                            Math.sin(angle) * (2 - progress)
                    );

                    Location auraLoc = loc.clone().add(offset);

                    // Peaceful aura particles
                    Particle.DustOptions auraColor = new Particle.DustOptions(
                            Color.fromRGB(
                                    200 + (int)(55 * Math.sin(progress * Math.PI)),
                                    200 + (int)(55 * Math.cos(progress * Math.PI)),
                                    255
                            ),
                            1.0f
                    );
                    world.spawnParticle(Particle.DUST, auraLoc, 1, 0, 0, 0, 0, auraColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.WITCH, auraLoc, 1, 0, 0, 0, 0);
                    }
                }

                // Illusion sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.3f, 0.5f + (float)progress);
                    world.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.2f, 0.7f + (float)progress);
                }
            }

            private void executeDeceptiveStrike(Location loc) {
                // Create illusory strikes
                double progress = (ticks - 20) / 20.0;

                // Generate multiple illusory paths
                for (int i = 0; i < 3; i++) {
                    Vector direction = strikeDirection[0].clone();
                    double angle = (i - 1) * Math.PI / 6; // Spread illusions

                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double x = direction.getX() * cos - direction.getZ() * sin;
                    double z = direction.getX() * sin + direction.getZ() * cos;
                    Vector illusoryDir = new Vector(x, 0, z).normalize();

                    // Create illusory strike path
                    for (double d = 0; d < 3; d += 0.2) {
                        Location strikeLoc = loc.clone().add(illusoryDir.clone().multiply(d));
                        illusionPath.add(strikeLoc);

                        // Illusory particles
                        Particle.DustOptions illusionColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.8f);
                        world.spawnParticle(Particle.DUST, strikeLoc, 1, 0, 0, 0, 0, illusionColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.WITCH, strikeLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Check for actual hits
                if (!hasStruck && ticks >= 30) {
                    checkDeceptiveHits(loc);
                }
            }

            private void checkDeceptiveHits(Location loc) {
                double hitRadius = 3.5;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate unpredictable damage
                        double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 80% to 120%
                        double deceptiveDamage = BASE_DAMAGE * randomFactor;

                        // Apply damage and effects
                        target.damage(deceptiveDamage, player);
                        hitEntities.add(target);

                        // Apply confusion effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));

                        // Create deceptive impact
                        createDeceptiveImpact(target.getLocation());

                        hasStruck = true;
                    }
                }
            }

            private void createDeceptiveImpact(Location loc) {
                // Impact illusion
                world.spawnParticle(Particle.FLASH, loc, 1, 0.2, 0.2, 0.2, 0);

                // Deceptive impact particles
                for (int i = 0; i < 25; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(180, 180, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.WITCH, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.5f);
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.6f, 0.8f);
            }

            private void createAftermath(Location loc) {
                double progress = (ticks - 40) / 20.0;

                // Fade illusion paths
                for (Location pathLoc : illusionPath) {
                    if (random.nextFloat() > progress) {
                        Particle.DustOptions fadeColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.6f * (1.0f - (float)progress));
                        world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, fadeColor);
                    }
                }

                // Aftermath sounds
                if (ticks % 5 == 0) {
                    world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                            0.3f * (1.0f - (float)progress), 1.0f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));

        // Add cooldown
        addCooldown(player, "TenthForm", 30);
    }

    /**
     * Eleventh Form: Scorching Parhelion (第じゅう一いち型かた 灼しゃく刻こく)
     * Created: 2025-06-19 08:00:50
     * @author SkyForce-6
     *
     * An evasive technique that utilizes rapid twists and turns to dodge attacks
     * while creating illusory sun-dog effects to disorient opponents.
     * The form manifests as multiple false suns surrounding the user.
     */
    public void useEleventhForm() {
        player.sendMessage("§9銀河 §f第じゅう一いち型かた 灼しゃく刻こく §9(Eleventh Form: Scorching Parhelion)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial parhelion effects
        world.playSound(startLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        world.playSound(startLoc, Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 1.2f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> parhelionPoints = new ArrayList<>();
        final double[] rotationAngle = {0.0};
        final Vector[] lastDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 80; // 4 seconds duration
            private final double DAMAGE = 10.0; // Moderate damage with multiple hits
            private final int PARHELION_COUNT = 3; // Number of false suns
            private final Map<Integer, Location> parhelionCenters = new HashMap<>();

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute technique phases
                if (ticks < 20) { // Initial dodge setup
                    initializeParhelions(playerLoc);
                } else if (ticks < 60) { // Main evasion phase
                    executeEvasiveManeuvers(playerLoc);
                } else { // Fade out
                    createFadeout(playerLoc);
                }

                ticks++;
            }

            private void initializeParhelions(Location loc) {
                double progress = ticks / 20.0;

                // Initialize parhelion centers
                for (int i = 0; i < PARHELION_COUNT; i++) {
                    double angle = (Math.PI * 2 * i) / PARHELION_COUNT;
                    Vector offset = new Vector(
                            Math.cos(angle) * 3,
                            1.5,
                            Math.sin(angle) * 3
                    );
                    parhelionCenters.put(i, loc.clone().add(offset));
                }

                // Initial formation particles
                for (Location center : parhelionCenters.values()) {
                    createParhelionEffect(center, progress);
                }

                // Grant dodge boost
                if (ticks == 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 1));
                }
            }

            private void createParhelionEffect(Location center, double intensity) {
                // Create false sun effect
                for (int i = 0; i < 16; i++) {
                    double angle = (Math.PI * 2 * i) / 16 + rotationAngle[0];
                    Vector offset = new Vector(
                            Math.cos(angle) * 0.8,
                            Math.sin(angle) * 0.8,
                            0
                    ).rotateAroundY(rotationAngle[0]);

                    Location sunLoc = center.clone().add(offset);

                    // Sun particles
                    Particle.DustOptions sunColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 50), 1.2f * (float)intensity);
                    world.spawnParticle(Particle.DUST, sunLoc, 1, 0, 0, 0, 0, sunColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLAME, sunLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void executeEvasiveManeuvers(Location loc) {
                rotationAngle[0] += Math.PI / 16;

                // Update parhelion positions
                updateParhelionPositions(loc);

                // Create dodge trails
                createDodgeTrails(loc);

                // Check for counter hits
                if (ticks % 5 == 0) {
                    checkCounterHits(loc);
                }

                // Movement assistance
                if (ticks % 10 == 0) {
                    assistDodgeMovement(loc);
                }
            }

            private void updateParhelionPositions(Location loc) {
                for (int i = 0; i < PARHELION_COUNT; i++) {
                    double baseAngle = (Math.PI * 2 * i) / PARHELION_COUNT + rotationAngle[0];
                    Vector offset = new Vector(
                            Math.cos(baseAngle) * 3,
                            1.5 + Math.sin(rotationAngle[0] * 2) * 0.5,
                            Math.sin(baseAngle) * 3
                    );
                    parhelionCenters.put(i, loc.clone().add(offset));
                }

                // Update effects
                for (Location center : parhelionCenters.values()) {
                    createParhelionEffect(center, 1.0);
                }
            }

            private void createDodgeTrails(Location loc) {
                // Calculate movement vector
                if (lastDirection[0] != null) {
                    Vector moveVec = loc.toVector().subtract(lastDirection[0]);

                    if (moveVec.lengthSquared() > 0.01) {
                        // Create dodge trail particles
                        for (int i = 0; i < 5; i++) {
                            Location trailLoc = loc.clone().add(
                                    random.nextDouble() - 0.5,
                                    random.nextDouble(),
                                    random.nextDouble() - 0.5
                            );

                            Particle.DustOptions trailColor = new Particle.DustOptions(
                                    Color.fromRGB(255, 220, 150), 0.8f);
                            world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);
                        }
                    }
                }

                lastDirection[0] = loc.toVector();
            }

            private void checkCounterHits(Location loc) {
                double hitRadius = 3.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        target.damage(DAMAGE, player);
                        hitEntities.add(target);

                        // Disorient target
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));

                        // Create counter hit effect
                        createCounterHitEffect(target.getLocation());
                    }
                }
            }

            private void createCounterHitEffect(Location loc) {
                // Counter flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Counter hit particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location hitLoc = loc.clone().add(spread);

                    Particle.DustOptions hitColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 50), 1.0f);
                    world.spawnParticle(Particle.DUST, hitLoc, 1, 0, 0, 0, 0, hitColor);
                    world.spawnParticle(Particle.FLAME, hitLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Counter hit sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
                world.playSound(loc, Sound.ENTITY_BLAZE_HURT, 0.4f, 1.2f);
            }

            private void assistDodgeMovement(Location loc) {
                // Add small random dodge movements
                Vector dodge = new Vector(
                        (random.nextDouble() - 0.5) * 0.3,
                        0.1,
                        (random.nextDouble() - 0.5) * 0.3
                );
                player.setVelocity(player.getVelocity().add(dodge));
            }

            private void createFadeout(Location loc) {
                double progress = (ticks - 60) / 20.0;

                // Fade parhelion effects
                for (Location center : parhelionCenters.values()) {
                    createParhelionEffect(center, 1.0 - progress);
                }

                // Final particles
                if (ticks % 2 == 0) {
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT,
                            0.4f * (1.0f - (float)progress), 1.0f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));

        // Add cooldown
        addCooldown(player, "EleventhForm", 25);
    }

    /**
     * Twelfth Form: Star Fragment Phoenix Flame (第一二型かた 星片不死鳥炎)
     * Created: 2025-06-19 08:06:11
     * @author SkyForce-6
     *
     * A dramatic technique combining a wide circular slash with a rising strike,
     * manifesting as a phoenix made of stellar flames and stardust.
     */
    public void useTwelfthForm() {
        player.sendMessage("§9銀河 §f第一二型かた 星片不死鳥炎 §9(Twelfth Form: Star Fragment Phoenix Flame)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial phoenix manifestation
        world.playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 2.0f);
        world.playSound(startLoc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> slashPath = new ArrayList<>();
        final double[] rotationAngle = {0.0};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 60; // 3 seconds duration
            private final double HORIZONTAL_DAMAGE = 16.0;
            private final double RISING_DAMAGE = 18.0;
            private boolean horizontalComplete = false;
            private Location jumpPeakLoc = null;

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Execute technique phases
                if (ticks < 30) { // Horizontal phoenix slash
                    executeHorizontalSlash(playerLoc);
                } else if (ticks < 50) { // Rising phoenix strike
                    executeRisingSlash(playerLoc);
                } else { // Phoenix dissipation
                    createPhoenixDissipation(playerLoc);
                }

                ticks++;
            }

            private void executeHorizontalSlash(Location loc) {
                rotationAngle[0] += Math.PI / 8; // Fast rotation

                // Create horizontal phoenix wing effect
                for (double radius = 0; radius <= 4; radius += 0.2) {
                    double angle = rotationAngle[0] + (Math.PI * radius / 8);
                    Vector offset = new Vector(
                            Math.cos(angle) * radius,
                            0.5,
                            Math.sin(angle) * radius
                    );

                    Location flameLoc = loc.clone().add(offset);
                    slashPath.add(flameLoc);

                    // Phoenix flame particles
                    Particle.DustOptions flameColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100 + (int)(100 * Math.sin(angle)), 50), 1.2f);
                    world.spawnParticle(Particle.DUST, flameLoc, 1, 0, 0, 0, 0, flameColor);

                    if (random.nextFloat() < 0.3) {
                        world.spawnParticle(Particle.FLAME, flameLoc, 1, 0, 0, 0, 0.05);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, flameLoc, 1, 0, 0, 0, 0.02);
                    }
                }

                // Check for horizontal hits
                if (!horizontalComplete) {
                    checkHorizontalHits(loc);
                }

                // Horizontal slash sounds
                if (ticks % 3 == 0) {
                    world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1.5f);
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.2f);
                }
            }

            private void checkHorizontalHits(Location loc) {
                double hitRadius = 4.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, 2.0, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply horizontal slash damage
                        target.damage(HORIZONTAL_DAMAGE, player);
                        hitEntities.add(target);

                        // Apply phoenix effects
                        target.setFireTicks(100);
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(1.2).setY(0.2);
                        target.setVelocity(knockback);

                        // Create impact effect
                        createPhoenixImpact(target.getLocation(), false);
                    }
                }
            }

            private void executeRisingSlash(Location loc) {
                if (!horizontalComplete) {
                    horizontalComplete = true;

                    // Initial jump
                    Vector jumpVelocity = new Vector(0, 1.2, 0);
                    player.setVelocity(jumpVelocity);
                }

                if (jumpPeakLoc == null && ticks >= 40) {
                    jumpPeakLoc = loc.clone();
                }

                // Create rising phoenix effect
                double progress = (ticks - 30) / 20.0;

                for (int i = 0; i < 2; i++) { // Two phoenix wings
                    double wingOffset = i * Math.PI;

                    for (double t = 0; t <= Math.PI; t += Math.PI / 16) {
                        double radius = 2 * Math.sin(t);
                        double height = t * 2;

                        double angle = rotationAngle[0] + wingOffset + t;
                        Vector offset = new Vector(
                                Math.cos(angle) * radius,
                                height,
                                Math.sin(angle) * radius
                        );

                        Location wingLoc = loc.clone().add(offset);

                        // Phoenix wing particles
                        Particle.DustOptions wingColor = new Particle.DustOptions(
                                Color.fromRGB(255, 150 + (int)(100 * Math.sin(t)), 50), 1.2f);
                        world.spawnParticle(Particle.DUST, wingLoc, 1, 0, 0, 0, 0, wingColor);

                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.FLAME, wingLoc, 1, 0, 0, 0, 0.05);
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, wingLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Check for rising hits
                if (jumpPeakLoc != null) {
                    checkRisingHits(loc);
                }

                // Rising sounds
                if (ticks % 2 == 0) {
                    world.playSound(loc, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.5f);
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.5f);
                }
            }

            private void checkRisingHits(Location loc) {
                double hitRadius = 3.0;
                double hitHeight = 4.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitHeight, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply rising slash damage
                        target.damage(RISING_DAMAGE, player);
                        hitEntities.add(target);

                        // Apply phoenix effects
                        target.setFireTicks(120);
                        Vector upward = new Vector(0, 1.0, 0);
                        target.setVelocity(upward);

                        // Create impact effect
                        createPhoenixImpact(target.getLocation(), true);
                    }
                }
            }

            private void createPhoenixImpact(Location loc, boolean isRising) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Phoenix impact particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 150, 50), 1.2f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.FLAME, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                if (isRising) {
                    world.playSound(loc, Sound.ENTITY_VEX_HURT, 0.8f, 1.5f);
                } else {
                    world.playSound(loc, Sound.ENTITY_BLAZE_HURT, 0.8f, 1.2f);
                }
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.2f);
            }

            private void createPhoenixDissipation(Location loc) {
                double progress = (ticks - 50) / 10.0;

                // Phoenix ash particles
                for (Location pathLoc : slashPath) {
                    if (random.nextFloat() > progress) {
                        Particle.DustOptions ashColor = new Particle.DustOptions(
                                Color.fromRGB(100, 100, 100), 0.8f * (1.0f - (float)progress));
                        world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, ashColor);

                        if (random.nextFloat() < 0.2 * (1.0 - progress)) {
                            world.spawnParticle(Particle.ASH, pathLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }

                // Dissipation sounds
                if (ticks % 3 == 0) {
                    world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT,
                            0.4f * (1.0f - (float)progress), 0.5f);
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 2));

        // Add cooldown
        addCooldown(player, "TwelfthForm", 40);
    }

    /**
     * Thirteenth Form: Universal Convergence (第十三型かた 宇宙収束)
     * Created: 2025-06-19 08:10:11
     * @author SkyForce-6
     *
     * The ultimate combination of previous forms in sequence:
     * 1. Galactic Cross - Initial cross slash
     * 2. Lunar Eclipse - Orbital dark strikes
     * 4. Gravity Spirit - Force manipulation
     * 11. Scorching Parhelion - Evasive maneuvers
     * 3. Solar Flare - Explosive burst
     * 10. Peaceful Lion - Deceptive strike
     * 8. Divine Chariot - Aerial decapitation
     * 12. Star Fragment Phoenix - Final phoenix combination
     */
    public void useThirteenthForm() {
        player.sendMessage("§9銀河 §f第十三型かた 宇宙収束 §9(Thirteenth Form: Universal Convergence)");

        World world = player.getLocation().getWorld();
        Location startLoc = player.getLocation();

        // Initial convergence effects
        world.playSound(startLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        world.playSound(startLoc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.5f);

        // Store technique data
        final Set<Entity> hitEntities = new HashSet<>();
        final List<Location> convergencePath = new ArrayList<>();
        final double[] rotationAngle = {0.0};
        final int[] currentPhase = {0}; // Track current form
        final Vector[] lastDirection = {player.getLocation().getDirection()};

        new BukkitRunnable() {
            private int ticks = 0;
            private final int MAX_TICKS = 200; // 10 seconds total duration
            private final double BASE_DAMAGE = 25.0; // High base damage
            private Location peakLocation = null;
            private boolean phaseComplete = false;

            // Phase durations (must sum to MAX_TICKS)
            private final int[] PHASE_DURATIONS = {20, 25, 25, 30, 25, 25, 25, 25};

            @Override
            public void run() {
                if (ticks >= MAX_TICKS) {
                    this.cancel();
                    return;
                }

                Location playerLoc = player.getLocation();

                // Update current phase
                updatePhase();

                // Execute current phase
                switch(currentPhase[0]) {
                    case 0: // Galactic Cross
                        executeGalacticCross(playerLoc);
                        break;
                    case 1: // Lunar Eclipse
                        executeLunarEclipse(playerLoc);
                        break;
                    case 2: // Gravity Spirit
                        executeGravitySpirit(playerLoc);
                        break;
                    case 3: // Scorching Parhelion
                        executeScorchingParhelion(playerLoc);
                        break;
                    case 4: // Solar Flare
                        executeSolarFlare(playerLoc);
                        break;
                    case 5: // Peaceful Lion
                        executePeacefulLion(playerLoc);
                        break;
                    case 6: // Divine Chariot
                        executeDivineChariot(playerLoc);
                        break;
                    case 7: // Star Fragment Phoenix
                        executeStarFragmentPhoenix(playerLoc);
                        break;
                }

                ticks++;
            }

            private void updatePhase() {
                int totalDuration = 0;
                for (int i = 0; i < PHASE_DURATIONS.length; i++) {
                    totalDuration += PHASE_DURATIONS[i];
                    if (ticks < totalDuration) {
                        if (currentPhase[0] != i) {
                            currentPhase[0] = i;
                            phaseComplete = false;
                            announcePhaseContinuation();
                        }
                        break;
                    }
                }
            }

            private void announcePhaseContinuation() {
                String[] phaseNames = {
                        "Galactic Cross",
                        "Lunar Eclipse",
                        "Gravity Spirit",
                        "Scorching Parhelion",
                        "Solar Flare",
                        "Peaceful Lion",
                        "Divine Chariot",
                        "Star Fragment Phoenix"
                };

                player.sendMessage("§9Continuing with: " + phaseNames[currentPhase[0]]);
            }

            // Implementation of each phase...
            // Note: Each phase would contain modified versions of their original form's code
            // adapted to work within the combined technique

            private void executeGalacticCross(Location loc) {
                double crossRadius = 4.0;


                // Für beide Arme des galaktischen Kreuzes
                for (int arm = 0; arm < 2; arm++) {
                    double baseAngle = arm * Math.PI / 2; // 0 und 90 Grad für die Kreuzarme

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 32) {
                        // Berechne Position für die Kreuzarme
                        for (double r = 0; r < crossRadius; r += 0.2) {
                            double x = Math.cos(angle + baseAngle) * r;
                            double y = Math.sin(angle + baseAngle) * r;

                            Location particleLoc = loc.clone().add(x, 0, y);

                            // Sichere Farbberechnung für die Galaxienpartikel
                            int redBase = 200;
                            int greenBase = 100;
                            int blueBase = 255;

                            // Farbmodulation basierend auf Position und Zeit
                            double colorMod = Math.sin(angle + rotationAngle[0]) * 0.5 + 0.5;

                            Particle.DustOptions galaxyColor = new Particle.DustOptions(
                                    Color.fromRGB(
                                            clampColor(redBase + (int)(55 * colorMod)),
                                            clampColor(greenBase + (int)(55 * colorMod)),
                                            clampColor(blueBase)
                                    ), 1.2f
                            );

                            // Spawn der Hauptpartikel
                            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, galaxyColor);

                            // Zusätzliche Effekte
                            if (random.nextFloat() < 0.2) {
                                world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0.02);
                                world.spawnParticle(Particle.WITCH, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                            }
                        }
                    }
                }

                // Rotationsupdate
                rotationAngle[0] += Math.PI / 32;
            }

            private void checkGalacticCrossHits(Location loc, double damage) {
                double hitRadius = 4.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, 2.0, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Apply effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(0.8).setY(0.2);
                        target.setVelocity(knockback);

                        // Create impact
                        createGalacticCrossImpact(target.getLocation());
                    }
                }
            }

            private void createGalacticCrossImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location impactLoc = loc.clone().add(spread);

                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(180, 180, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.END_ROD, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.6f, 1.5f);
                world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.4f, 2.0f);
            }

            private void executeLunarEclipse(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.7;
                double phaseProgress = ((ticks - PHASE_DURATIONS[0]) % PHASE_DURATIONS[1]) / (double)PHASE_DURATIONS[1];

                // Create orbital pattern
                for (int orbit = 0; orbit < 3; orbit++) {
                    double orbitRadius = 2 + orbit;
                    double orbitSpeed = 1.0 - (orbit * 0.2);

                    for (int point = 0; point < 8; point++) {
                        double angle = ((Math.PI * 2 * point) / 8) + (rotationAngle[0] * orbitSpeed);

                        // Calculate spiral movement
                        double heightOffset = Math.sin(phaseProgress * Math.PI * 2) * (1.5 - orbit * 0.3);
                        Vector direction = new Vector(
                                Math.cos(angle) * orbitRadius,
                                heightOffset,
                                Math.sin(angle) * orbitRadius
                        );

                        Location orbitLoc = loc.clone().add(direction);
                        convergencePath.add(orbitLoc);

                        // Lunar eclipse particles
                        Particle.DustOptions eclipseColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        20 + (int)(50 * phaseProgress),
                                        20 + (int)(50 * phaseProgress),
                                        40 + (int)(80 * phaseProgress)
                                ), 1.2f
                        );
                        world.spawnParticle(Particle.DUST, orbitLoc, 1, 0, 0, 0, 0, eclipseColor);

                        // Dark energy effects
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.DRAGON_BREATH, orbitLoc, 1, 0, 0, 0, 0.02);
                            world.spawnParticle(Particle.WITCH, orbitLoc, 1, 0, 0, 0, 0.03);
                        }

                        // Eclipse shadow trail
                        Location shadowLoc = orbitLoc.clone().add(0, -0.5, 0);
                        Particle.DustOptions shadowColor = new Particle.DustOptions(
                                Color.fromRGB(20, 20, 40), 0.8f
                        );
                        world.spawnParticle(Particle.DUST, shadowLoc, 1, 0.1, 0, 0.1, 0, shadowColor);
                    }
                }

                // Rotate eclipse pattern
                rotationAngle[0] += Math.PI / 12;

                // Check for hits
                if (!phaseComplete) {
                    checkLunarEclipseHits(loc, phaseDamage);
                }

                // Eclipse sounds
                if (ticks % 5 == 0) {
                    float pitch = 0.5f + (float)(phaseProgress * 0.5f);
                    world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, 0.4f, pitch);
                    world.playSound(loc, Sound.ENTITY_ENDERMAN_AMBIENT, 0.3f, pitch);
                }

                // Special eclipse pulse every second
                if (ticks % 20 == 0) {
                    createEclipsePulse(loc);
                }
            }

            private void checkLunarEclipseHits(Location loc, double damage) {
                double hitRadius = 5.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, 3.0, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage with dark energy effect
                        target.damage(damage, player);
                        hitEntities.add(target);

                        // Apply eclipse effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));

                        // Eclipse knockback (pulling effect)
                        Vector pull = loc.toVector().subtract(target.getLocation().toVector())
                                .normalize().multiply(0.5);
                        target.setVelocity(pull);

                        // Create dark impact
                        createLunarEclipseImpact(target.getLocation());
                    }
                }
            }

            private void createLunarEclipseImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0.2, 0.2, 0.2, 0);

                // Dark impact particles
                for (int i = 0; i < 25; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.2);

                    Location impactLoc = loc.clone().add(spread);

                    // Impact particles
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(40, 40, 80), 1.0f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.DRAGON_BREATH, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);

                    // Create light beams from impact location
                    if (i % 5 == 0) { // Create fewer beams for performance
                        createEclipseLightBeams(loc, impactLoc);
                    }
                }

                // Dark impact sounds
                world.playSound(loc, Sound.ENTITY_ENDERMAN_HURT, 0.6f, 0.5f);
                world.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.4f, 2.0f);
            }

            private void createEclipseLightBeams(Location center, Location target) {
                Vector direction = target.toVector().subtract(center.toVector());
                double distance = direction.length();
                direction.normalize();

                // Create beam particles
                for (double d = 0; d < distance; d += 0.2) {
                    Location beamLoc = center.clone().add(direction.multiply(d));

                    // Beam particles
                    Particle.DustOptions beamColor = new Particle.DustOptions(
                            Color.fromRGB(60, 60, 100), 0.8f * (float)(1.0 - d/distance));
                    world.spawnParticle(Particle.DUST, beamLoc, 1, 0, 0, 0, 0, beamColor);

                    // Additional effects
                    if (random.nextFloat() < 0.1) {
                        world.spawnParticle(Particle.DRAGON_BREATH, beamLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            private void createEclipsePulse(Location loc) {
                // Create expanding ring effect
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    Vector direction = new Vector(
                            Math.cos(angle) * 3,
                            0,
                            Math.sin(angle) * 3
                    );

                    Location pulseLoc = loc.clone().add(direction);

                    // Pulse particles
                    Particle.DustOptions pulseColor = new Particle.DustOptions(
                            Color.fromRGB(30, 30, 60), 1.5f);
                    world.spawnParticle(Particle.DUST, pulseLoc, 1, 0, 0, 0, 0, pulseColor);
                    world.spawnParticle(Particle.WITCH, pulseLoc, 1, 0, 0, 0, 0.02);
                }

                // Pulse sound
                world.playSound(loc, Sound.ENTITY_WITHER_HURT, 0.5f, 0.5f);
            }

            private void executeGravitySpirit(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.8;
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[2]) / (double)PHASE_DURATIONS[2];

                // Create gravitational field
                createGravitationalField(loc, phaseProgress);

                // Apply gravity effects
                if (!phaseComplete) {
                    applyGravityEffects(loc, phaseDamage, phaseProgress);
                }

                // Sound effects
                if (ticks % 4 == 0) {
                    float pitch = 0.5f + (float)(phaseProgress * 1.5f);
                    world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, pitch);
                    world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.3f, pitch * 2);
                }
            }

            private void createGravitationalField(Location loc, double progress) {
                // Create main gravity well
                double radius = 5.0;
                double height = 3.0;

                for (double y = 0; y <= height; y += 0.5) {
                    double circleRadius = radius * (1 - y/height);
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                        // Spiral effect
                        double spiralOffset = progress * Math.PI * 2;
                        double x = Math.cos(angle + spiralOffset) * circleRadius;
                        double z = Math.sin(angle + spiralOffset) * circleRadius;

                        Location particleLoc = loc.clone().add(x, y, z);

                        // Gravity well particles
                        Particle.DustOptions gravityColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        70 + (int)(100 * progress),
                                        20 + (int)(50 * progress),
                                        100 + (int)(155 * progress)
                                ), 1.5f
                        );
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, gravityColor);

                        // Energy streams
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0.05);
                            world.spawnParticle(Particle.WARPED_SPORE, particleLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Create floating debris effect
                for (int i = 0; i < 10; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double distance = random.nextDouble() * radius;
                    double heightVar = random.nextDouble() * height;

                    Vector offset = new Vector(
                            Math.cos(angle) * distance,
                            heightVar,
                            Math.sin(angle) * distance
                    );

                    Location debrisLoc = loc.clone().add(offset);

                    // Debris particles
                    world.spawnParticle(Particle.WITCH, debrisLoc, 1, 0.1, 0.1, 0.1, 0);
                    world.spawnParticle(Particle.ENCHANT, debrisLoc, 2, 0.1, 0.1, 0.1, 0.5);
                }
            }

            private void applyGravityEffects(Location loc, double damage, double progress) {
                double effectRadius = 6.0;

                for (Entity entity : world.getNearbyEntities(loc, effectRadius, effectRadius, effectRadius)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate distance for gravity effect
                        double distance = loc.distance(target.getLocation());
                        if (distance <= 0.1) distance = 0.1;

                        // Gravity pull effect
                        Vector pull = loc.toVector().subtract(target.getLocation().toVector())
                                .normalize().multiply(1.2 / distance);

                        // Add vertical oscillation
                        pull.setY(pull.getY() + Math.sin(progress * Math.PI * 2) * 0.3);

                        // Apply movement
                        target.setVelocity(pull);

                        // Check for damage
                        if (!hitEntities.contains(entity) && distance < 3.0) {
                            // Apply damage
                            target.damage(damage, player);
                            hitEntities.add(entity);

                            // Apply gravity effects
                            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));

                            // Create gravity impact
                            createGravityImpact(target.getLocation());
                        }
                    }
                }
            }

            private void createGravityImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0.2, 0.2, 0.2, 0);

                // Gravity distortion particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    // Impact particles
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(100, 50, 150), 1.2f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.REVERSE_PORTAL, impactLoc, 2, 0.1, 0.1, 0.1, 0.1);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_WARDEN_SONIC_CHARGE, 0.8f, 1.5f);
                world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.6f, 1.2f);
            }

            private void executeScorchingParhelion(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.6;
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1] + PHASE_DURATIONS[2];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[3]) / (double)PHASE_DURATIONS[3];

                // Create parhelion system
                createParhelionSystem(loc, phaseProgress);

                // Execute evasive movements
                executeEvasivePattern(loc, phaseProgress);

                // Check for hits while maintaining evasion
                if (!phaseComplete) {
                    checkParhelionHits(loc, phaseDamage, phaseProgress);
                }
            }

            private void createParhelionSystem(Location loc, double progress) {
                // Create multiple sun dogs (false suns)
                for (int sunDog = 0; sunDog < 4; sunDog++) {
                    double baseAngle = (Math.PI * 2 * sunDog / 4) + rotationAngle[0];
                    double radius = 3.0 + Math.sin(progress * Math.PI * 2) * 0.5;

                    // Calculate the main sun dog position once
                    Vector mainSunOffset = new Vector(
                            Math.cos(baseAngle) * radius,
                            1.5 + Math.cos(progress * Math.PI * 2) * 0.5,
                            Math.sin(baseAngle) * radius
                    );
                    Location mainSunDogLoc = loc.clone().add(mainSunOffset);

                    // Create connecting light beams first
                    createLightBeams(loc, mainSunDogLoc, progress);

                    // Create sun dog particles
                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        // Create sun dog core
                        double innerRadius = 0.5;
                        Vector innerOffset = new Vector(
                                Math.cos(angle) * innerRadius,
                                Math.sin(angle) * innerRadius,
                                0
                        ).rotateAroundAxis(mainSunOffset.clone().normalize(), progress * Math.PI * 2);

                        Location particleLoc = mainSunDogLoc.clone().add(innerOffset);

                        // Sun dog particles with safe color calculation
                        int redValue = 255;
                        int greenValue = clampColor(150 + (int)(105 * Math.sin(progress * Math.PI)));
                        int blueValue = 50;

                        Particle.DustOptions sunColor = new Particle.DustOptions(
                                Color.fromRGB(redValue, greenValue, blueValue),
                                1.2f
                        );
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, sunColor);

                        // Heat haze effect
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.WITCH, particleLoc, 1, 0, 0, 0, 0.02);
                            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                    }
                }

                rotationAngle[0] += Math.PI / 32;
            }

            private int clampColor(int value) {
                return Math.max(0, Math.min(255, value));
            }

            private void createLightBeams(Location center, Location sunDog, double progress) {
                Vector direction = sunDog.toVector().subtract(center.toVector()).normalize();
                double distance = center.distance(sunDog);

                for (double d = 0; d < distance; d += 0.5) {
                    Location beamLoc = center.clone().add(direction.clone().multiply(d));

                    // Beam particles
                    Particle.DustOptions beamColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 100), 0.8f * (float)(1.0 - d/distance));
                    world.spawnParticle(Particle.DUST, beamLoc, 1, 0.1, 0.1, 0.1, 0, beamColor);
                }
            }

            private void executeEvasivePattern(Location loc, double progress) {
                // Calculate evasive movement
                double angle = progress * Math.PI * 4;
                Vector evasion = new Vector(
                        Math.cos(angle) * 0.3,
                        Math.sin(angle * 2) * 0.2,
                        Math.sin(angle) * 0.3
                );

                // Apply evasive movement to player
                if (player.isOnGround()) {
                    player.setVelocity(player.getVelocity().add(evasion));
                }

                // Create evasion trail
                for (int i = 0; i < 3; i++) {
                    Location trailLoc = loc.clone().add(
                            random.nextDouble() - 0.5,
                            random.nextDouble(),
                            random.nextDouble() - 0.5
                    );

                    // Trail particles
                    Particle.DustOptions trailColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 100), 0.8f);
                    world.spawnParticle(Particle.DUST, trailLoc, 1, 0, 0, 0, 0, trailColor);
                }
            }

            private void checkParhelionHits(Location loc, double damage, double progress) {
                double hitRadius = 4.0;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate damage based on evasion effectiveness
                        double evasionMultiplier = 1.0 + progress * 0.5;
                        double finalDamage = damage * evasionMultiplier;

                        // Apply damage
                        target.damage(finalDamage, player);
                        hitEntities.add(target);

                        // Apply sun effects
                        target.setFireTicks(80);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));

                        // Create sun impact
                        createParhelionImpact(target.getLocation(), progress);
                    }
                }
            }

            private void createParhelionImpact(Location loc, double progress) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 25; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    // Layered impact particles
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 150 + (int)(105 * progress), 50), 1.2f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.FLAME, impactLoc, 2, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.LAVA, impactLoc, 1, 0.1, 0.1, 0.1, 0);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_BLAZE_HURT, 0.8f, 1.2f);
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.5f);
            }

            private void executeSolarFlare(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.9;
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1] +
                        PHASE_DURATIONS[2] + PHASE_DURATIONS[3];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[4]) / (double)PHASE_DURATIONS[4];

                // Create solar flare effect
                createSolarFlareEffect(loc, phaseProgress);

                // Handle explosion mechanics
                if (phaseProgress > 0.5 && !phaseComplete) {
                    executeSolarExplosion(loc, phaseDamage, phaseProgress);
                }

                // Sound effects
                playSolarFlareSounds(loc, phaseProgress);
            }

            private void createSolarFlareEffect(Location loc, double progress) {
                // Main flare core
                double coreSize = 2.0 + Math.sin(progress * Math.PI) * 3.0;

                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 16) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 16) {
                        double x = Math.sin(theta) * Math.cos(phi) * coreSize;
                        double y = Math.cos(theta) * coreSize;
                        double z = Math.sin(theta) * Math.sin(phi) * coreSize;

                        Location flareLoc = loc.clone().add(x, y, z);

                        // Core particles
                        Particle.DustOptions flareColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        255,
                                        50 + (int)(150 * Math.sin(progress * Math.PI)),
                                        0
                                ), 1.5f
                        );
                        world.spawnParticle(Particle.DUST, flareLoc, 1, 0, 0, 0, 0, flareColor);

                        // Plasma effects
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.FLAME, flareLoc, 1, 0.1, 0.1, 0.1, 0.05);
                            world.spawnParticle(Particle.LAVA, flareLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }

                // Solar prominence arcs
                createSolarProminences(loc, progress);
            }

            private void createSolarProminences(Location loc, double progress) {
                int prominenceCount = 4;
                for (int i = 0; i < prominenceCount; i++) {
                    double baseAngle = (Math.PI * 2 * i / prominenceCount) + rotationAngle[0];
                    double height = 3.0 + Math.sin(progress * Math.PI + i) * 2.0;

                    for (double t = 0; t < 1.0; t += 0.05) {
                        // Calculate prominence curve
                        double curveHeight = Math.sin(t * Math.PI) * height;
                        double radius = 2.0 + curveHeight * 0.5;

                        double x = Math.cos(baseAngle) * radius;
                        double y = curveHeight;
                        double z = Math.sin(baseAngle) * radius;

                        Location prominenceLoc = loc.clone().add(x, y, z);

                        // Prominence particles
                        Particle.DustOptions prominenceColor = new Particle.DustOptions(
                                Color.fromRGB(255, 100, 0), 1.2f);
                        world.spawnParticle(Particle.DUST, prominenceLoc, 1, 0.1, 0.1, 0.1, 0, prominenceColor);

                        // Additional effects
                        if (random.nextFloat() < 0.3) {
                            world.spawnParticle(Particle.FLAME, prominenceLoc, 2, 0.1, 0.1, 0.1, 0.02);
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, prominenceLoc, 1, 0.1, 0.1, 0.1, 0.01);
                        }
                    }
                }
            }

            private void executeSolarExplosion(Location loc, double damage, double progress) {
                double explosionRadius = 5.0;
                double expansionProgress = (progress - 0.5) * 2; // Normalize to 0-1 for explosion phase

                // Create explosion wave
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    for (double height = 0; height < Math.PI; height += Math.PI / 16) {
                        double radius = explosionRadius * expansionProgress;
                        double x = Math.sin(height) * Math.cos(angle) * radius;
                        double y = Math.cos(height) * radius;
                        double z = Math.sin(height) * Math.sin(angle) * radius;

                        Location explosionLoc = loc.clone().add(x, y, z);

                        // Explosion particles
                        Particle.DustOptions explosionColor = new Particle.DustOptions(
                                Color.fromRGB(255, 150, 0), 1.5f);
                        world.spawnParticle(Particle.DUST, explosionLoc, 1, 0, 0, 0, 0, explosionColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.EXPLOSION, explosionLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.LAVA, explosionLoc, 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }

                // Check for explosion hits
                for (Entity entity : world.getNearbyEntities(loc, explosionRadius, explosionRadius, explosionRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate distance-based damage
                        double distance = loc.distance(target.getLocation());
                        double distanceMultiplier = 1.0 - (distance / explosionRadius);
                        double finalDamage = damage * distanceMultiplier * 1.5; // 1.5x for explosion phase

                        // Apply damage and effects
                        target.damage(finalDamage, player);
                        hitEntities.add(target);

                        // Solar effects
                        target.setFireTicks(140);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));

                        // Explosion knockback
                        Vector knockback = target.getLocation().subtract(loc).toVector()
                                .normalize().multiply(2.0).setY(0.5);
                        target.setVelocity(knockback);

                        // Create impact
                        createSolarFlareImpact(target.getLocation(), progress);
                    }
                }
            }

            private void createSolarFlareImpact(Location loc, double progress) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 3, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2.0);

                    Location impactLoc = loc.clone().add(spread);

                    // Layered impact effects
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 100, 0), 1.4f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.FLAME, impactLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticle(Particle.LAVA, impactLoc, 1, 0.1, 0.1, 0.1, 0);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 0.5f);
            }

            private void playSolarFlareSounds(Location loc, double progress) {
                // Charging sounds
                if (progress < 0.5) {
                    if (ticks % 4 == 0) {
                        float pitch = 0.5f + (float)(progress * 1.5f);
                        world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.6f, pitch);
                        world.playSound(loc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.4f, pitch);
                    }
                }
                // Explosion sounds
                else {
                    if (ticks % 2 == 0) {
                        world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 0.5f);
                        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 2.0f);
                    }
                }
            }

            /**
             * Implementation of the sixth phase (Peaceful Lion - Tenth Form) for the 13th Form
             * Created: 2025-06-19 08:31:58
             * @author SkyForce-6
             */
            private void executePeacefulLion(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.7;
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1] +
                        PHASE_DURATIONS[2] + PHASE_DURATIONS[3] +
                        PHASE_DURATIONS[4];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[5]) / (double)PHASE_DURATIONS[5];

                // Create peaceful lion aura
                createPeacefulLionAura(loc, phaseProgress);

                // Execute deceptive strikes
                if (!phaseComplete) {
                    executeDeceptiveStrikes(loc, phaseDamage, phaseProgress);
                }
            }

            private void createPeacefulLionAura(Location loc, double progress) {
                // Main lion aura
                double auraRadius = 3.0 + Math.sin(progress * Math.PI * 2) * 0.5;

                // Create lion's mane effect
                for (double phi = 0; phi < Math.PI * 2; phi += Math.PI / 32) {
                    for (double theta = 0; theta < Math.PI; theta += Math.PI / 16) {
                        double radius = auraRadius * (1 + 0.3 * Math.sin(theta * 4 + progress * Math.PI * 2));

                        double x = Math.sin(theta) * Math.cos(phi) * radius;
                        double y = Math.cos(theta) * radius;
                        double z = Math.sin(theta) * Math.sin(phi) * radius;

                        Location maneLoc = loc.clone().add(x, y, z);

                        // Mane particles
                        Particle.DustOptions maneColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        200 + (int)(55 * Math.sin(progress * Math.PI)),
                                        200 + (int)(55 * Math.cos(progress * Math.PI)),
                                        255
                                ), 1.2f
                        );
                        world.spawnParticle(Particle.DUST, maneLoc, 1, 0, 0, 0, 0, maneColor);

                        // Peaceful energy particles
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.WITCH, maneLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.END_ROD, maneLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }

                // Create illusory duplicates
                createIllusoryDuplicates(loc, progress);
            }

            private void createIllusoryDuplicates(Location loc, double progress) {
                int duplicateCount = 3;

                for (int i = 0; i < duplicateCount; i++) {
                    double angle = (Math.PI * 2 * i / duplicateCount) + rotationAngle[0];
                    double offset = 2.0 + Math.sin(progress * Math.PI * 2 + i) * 0.5;

                    Vector direction = new Vector(
                            Math.cos(angle) * offset,
                            Math.sin(progress * Math.PI * 2) * 0.5,
                            Math.sin(angle) * offset
                    );

                    Location illusionLoc = loc.clone().add(direction);

                    // Create illusory lion silhouette
                    for (double t = 0; t < Math.PI * 2; t += Math.PI / 8) {
                        double illusionRadius = 1.0;
                        Vector silhouette = new Vector(
                                Math.cos(t) * illusionRadius,
                                Math.sin(t) * illusionRadius,
                                0
                        ).rotateAroundAxis(direction.clone().normalize(), progress * Math.PI * 2);

                        Location silhouetteLoc = illusionLoc.clone().add(silhouette);

                        // Illusion particles
                        Particle.DustOptions illusionColor = new Particle.DustOptions(
                                Color.fromRGB(180, 180, 255), 0.8f);
                        world.spawnParticle(Particle.DUST, silhouetteLoc, 1, 0, 0, 0, 0, illusionColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.WITCH, silhouetteLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }
            }

            private void executeDeceptiveStrikes(Location loc, double damage, double progress) {
                // Create deceptive strike paths
                List<Location> strikePaths = new ArrayList<>();

                int strikeCount = 4;
                for (int i = 0; i < strikeCount; i++) {
                    double angle = (Math.PI * 2 * i / strikeCount) + rotationAngle[0];
                    double strikeDistance = 4.0;

                    for (double d = 0; d < strikeDistance; d += 0.2) {
                        Vector direction = new Vector(
                                Math.cos(angle) * d,
                                Math.sin(progress * Math.PI * 2) * 0.5,
                                Math.sin(angle) * d
                        );

                        Location strikeLoc = loc.clone().add(direction);
                        strikePaths.add(strikeLoc);

                        // Strike path particles
                        if (random.nextFloat() < 0.3) {
                            Particle.DustOptions strikeColor = new Particle.DustOptions(
                                    Color.fromRGB(200, 200, 255), 0.6f);
                            world.spawnParticle(Particle.DUST, strikeLoc, 1, 0, 0, 0, 0, strikeColor);
                        }
                    }
                }

                // Check for deceptive hits
                checkDeceptiveHits(loc, damage, strikePaths);

                rotationAngle[0] += Math.PI / 16;
            }

            private void checkDeceptiveHits(Location loc, double damage, List<Location> strikePaths) {
                double hitRadius = 3.5;

                for (Entity entity : world.getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate unpredictable damage
                        double randomFactor = 0.8 + (random.nextDouble() * 0.4); // 80% to 120%
                        double finalDamage = damage * randomFactor;

                        // Apply damage and effects
                        target.damage(finalDamage, player);
                        hitEntities.add(target);

                        // Apply confusion effects
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));

                        // Create deceptive impact
                        createDeceptiveImpact(target.getLocation());

                        // Create connecting strikes to real hit location
                        createRevealingStrikes(loc, target.getLocation());
                    }
                }
            }

            private void createDeceptiveImpact(Location loc) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 1, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 20; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble());

                    Location impactLoc = loc.clone().add(spread);

                    // Impact effects
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(180, 180, 255), 1.0f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.WITCH, impactLoc, 1, 0.1, 0.1, 0.1, 0.05);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.8f, 1.2f);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.5f);
            }

            private void createRevealingStrikes(Location start, Location end) {
                Vector direction = end.toVector().subtract(start.toVector());
                double distance = direction.length();
                direction.normalize();

                for (double d = 0; d < distance; d += 0.2) {
                    Location strikeLoc = start.clone().add(direction.clone().multiply(d));

                    // Revealing strike particles
                    Particle.DustOptions strikeColor = new Particle.DustOptions(
                            Color.fromRGB(220, 220, 255), 0.8f);
                    world.spawnParticle(Particle.DUST, strikeLoc, 1, 0, 0, 0, 0, strikeColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.WITCH, strikeLoc, 1, 0, 0, 0, 0);
                    }
                }
            }

            /**
             * Implementation of the seventh phase (Divine Chariot - Eighth Form) for the 13th Form
             * Created: 2025-06-19 08:34:08
             * @author SkyForce-6
             */
            private void executeDivineChariot(Location loc) {
                double phaseDamage = BASE_DAMAGE * 0.8;
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1] +
                        PHASE_DURATIONS[2] + PHASE_DURATIONS[3] +
                        PHASE_DURATIONS[4] + PHASE_DURATIONS[5];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[6]) / (double)PHASE_DURATIONS[6];

                // Create divine chariot manifestation
                createDivineChariot(loc, phaseProgress);

                // Execute aerial attacks
                if (!phaseComplete) {
                    executeAerialStrikes(loc, phaseDamage, phaseProgress);
                }
            }

            private void createDivineChariot(Location loc, double progress) {
                // Create chariot base
                double baseRadius = 2 + Math.sin(progress * Math.PI * 2) * 0.5;
                createChariotBase(loc, baseRadius, progress);

                // Create energy wheels
                createEnergyWheels(loc, baseRadius, progress);

                // Create divine light streams
                createDivineLightStreams(loc, progress);
            }

            private void createChariotBase(Location loc, double radius, double progress) {
                // Create chariot platform
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    for (double r = 0; r < radius; r += 0.2) {
                        double x = Math.cos(angle) * r;
                        double y = Math.sin(progress * Math.PI * 2) * 0.3;
                        double z = Math.sin(angle) * r;

                        Location chariotLoc = loc.clone().add(x, y, z);

                        // Platform particles
                        Particle.DustOptions baseColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        220 + (int)(35 * Math.sin(progress * Math.PI)),
                                        220 + (int)(35 * Math.cos(progress * Math.PI)),
                                        255
                                ), 1.2f
                        );
                        world.spawnParticle(Particle.DUST, chariotLoc, 1, 0, 0, 0, 0, baseColor);

                        // Divine energy particles
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, chariotLoc, 1, 0, 0, 0, 0.02);
                            world.spawnParticle(Particle.WITCH, chariotLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }

            private void createEnergyWheels(Location loc, double radius, double progress) {
                // Create two energy wheels
                for (int wheel = 0; wheel < 2; wheel++) {
                    double wheelOffset = (wheel * 2 - 1) * radius * 0.8;

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 32) {
                        double wheelRadius = radius * 0.6;
                        double x = Math.cos(angle) * wheelRadius;
                        double y = Math.sin(angle) * wheelRadius;

                        Vector wheelVec = new Vector(x, y, 0)
                                .rotateAroundY(rotationAngle[0] * 2)
                                .rotateAroundAxis(new Vector(1, 0, 0), progress * Math.PI * 2);

                        Location wheelLoc = loc.clone().add(wheelVec).add(0, 0, wheelOffset);

                        // Wheel particles
                        Particle.DustOptions wheelColor = new Particle.DustOptions(
                                Color.fromRGB(255, 255, 200), 1.0f);
                        world.spawnParticle(Particle.DUST, wheelLoc, 1, 0, 0, 0, 0, wheelColor);

                        // Energy spokes
                        if (angle % (Math.PI / 4) < 0.1) {
                            createEnergySpoke(wheelLoc, angle, progress);
                        }
                    }
                }
            }

            private void createEnergySpoke(Location center, double angle, double progress) {
                double spokeLength = 0.8;
                Vector direction = new Vector(
                        Math.cos(angle) * spokeLength,
                        Math.sin(angle) * spokeLength,
                        0
                );

                for (double d = 0; d < spokeLength; d += 0.1) {
                    Location spokeLoc = center.clone().add(direction.clone().multiply(d / spokeLength));

                    // Spoke particles
                    Particle.DustOptions spokeColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 150), 0.8f);
                    world.spawnParticle(Particle.DUST, spokeLoc, 1, 0, 0, 0, 0, spokeColor);
                }
            }

            private void createDivineLightStreams(Location loc, double progress) {
                int streamCount = 6;
                double streamHeight = 4.0;

                for (int i = 0; i < streamCount; i++) {
                    double angle = (Math.PI * 2 * i / streamCount) + rotationAngle[0];

                    for (double h = 0; h < streamHeight; h += 0.2) {
                        double radius = 1.5 + Math.sin(h * Math.PI / streamHeight + progress * Math.PI * 2) * 0.5;
                        double x = Math.cos(angle) * radius;
                        double y = h;
                        double z = Math.sin(angle) * radius;

                        Location streamLoc = loc.clone().add(x, y, z);

                        // Light stream particles
                        Particle.DustOptions streamColor = new Particle.DustOptions(
                                Color.fromRGB(255, 255, 200), 1.0f);
                        world.spawnParticle(Particle.DUST, streamLoc, 1, 0, 0, 0, 0, streamColor);

                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.END_ROD, streamLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }
            }

            private void executeAerialStrikes(Location loc, double damage, double progress) {
                // Perform aerial attacks
                if (progress > 0.3 && !phaseComplete) {
                    // Launch player into air
                    if (!player.isGliding()) {
                        Vector launch = new Vector(0, 1.2, 0);
                        player.setVelocity(launch);
                    }

                    // Execute aerial strikes
                    double strikeRadius = 4.0;
                    double strikeHeight = 3.0;

                    for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeHeight, strikeRadius)) {
                        if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                            LivingEntity target = (LivingEntity) entity;

                            // Calculate height bonus damage
                            double heightDifference = player.getLocation().getY() - target.getLocation().getY();
                            double heightMultiplier = 1.0 + Math.min(heightDifference * 0.1, 0.5);
                            double finalDamage = damage * heightMultiplier;

                            // Apply damage and effects
                            target.damage(finalDamage, player);
                            hitEntities.add(target);

                            // Apply divine effects
                            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));

                            // Create divine impact
                            createDivineImpact(target.getLocation(), progress);
                        }
                    }
                }
            }

            private void createDivineImpact(Location loc, double progress) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 25; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 1.5);

                    Location impactLoc = loc.clone().add(spread);

                    // Divine impact particles
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 200), 1.2f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.END_ROD, impactLoc, 2, 0.1, 0.1, 0.1, 0.1);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 2.0f);
            }

            /**
             * Implementation of the eighth and final phase (Star Fragment Phoenix - Twelfth Form) for the 13th Form
             * Created: 2025-06-19 08:36:41
             * @author SkyForce-6
             */
            private void executeStarFragmentPhoenix(Location loc) {
                double phaseDamage = BASE_DAMAGE * 1.0; // Full damage for final phase
                int previousPhaseDuration = PHASE_DURATIONS[0] + PHASE_DURATIONS[1] +
                        PHASE_DURATIONS[2] + PHASE_DURATIONS[3] +
                        PHASE_DURATIONS[4] + PHASE_DURATIONS[5] +
                        PHASE_DURATIONS[6];
                double phaseProgress = ((ticks - previousPhaseDuration) % PHASE_DURATIONS[7]) / (double)PHASE_DURATIONS[7];

                // Create phoenix manifestation
                createPhoenixManifestation(loc, phaseProgress);

                // Execute final strikes
                if (!phaseComplete) {
                    executePhoenixStrikes(loc, phaseDamage, phaseProgress);
                }
            }

            private void createPhoenixManifestation(Location loc, double progress) {
                // Create phoenix body
                createPhoenixBody(loc, progress);

                // Create phoenix wings
                createPhoenixWings(loc, progress);

                // Create star fragments
                createStarFragments(loc, progress);

                // Update rotation
                rotationAngle[0] += Math.PI / 12;
            }

            private void createPhoenixBody(Location loc, double progress) {
                double bodyLength = 4.0;
                double bodyWidth = 1.5;

                // Create main body
                for (double t = 0; t < bodyLength; t += 0.2) {
                    double width = bodyWidth * Math.sin(t * Math.PI / bodyLength);

                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                        double x = Math.cos(angle) * width;
                        double y = t + Math.sin(progress * Math.PI * 2) * 0.5;
                        double z = Math.sin(angle) * width;

                        Vector bodyVec = new Vector(x, y, z)
                                .rotateAroundY(rotationAngle[0])
                                .rotateAroundAxis(new Vector(1, 0, 0), Math.PI / 4);

                        Location bodyLoc = loc.clone().add(bodyVec);

                        // Body particles
                        Particle.DustOptions bodyColor = new Particle.DustOptions(
                                Color.fromRGB(
                                        255,
                                        100 + (int)(100 * Math.sin(t + progress * Math.PI)),
                                        50
                                ), 1.2f
                        );
                        world.spawnParticle(Particle.DUST, bodyLoc, 1, 0, 0, 0, 0, bodyColor);

                        // Star energy particles
                        if (random.nextFloat() < 0.2) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, bodyLoc, 1, 0, 0, 0, 0.02);
                            world.spawnParticle(Particle.END_ROD, bodyLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }
            }

            private void createPhoenixWings(Location loc, double progress) {
                double wingSpan = 6.0;
                double wingDepth = 3.0;

                // Create both wings
                for (int wing = 0; wing < 2; wing++) {
                    double wingSign = wing * 2 - 1; // -1 or 1 for left/right wing

                    for (double s = 0; s < wingSpan; s += 0.2) {
                        double height = Math.sin(s * Math.PI / wingSpan) * wingDepth;
                        height *= (1 + Math.sin(progress * Math.PI * 2) * 0.2); // Wing flapping

                        for (double t = 0; t < height; t += 0.2) {
                            Vector wingVec = new Vector(
                                    s * wingSign,
                                    t + 2,
                                    Math.sin(s * Math.PI / wingSpan) * wingDepth * 0.3
                            ).rotateAroundY(rotationAngle[0]);

                            Location wingLoc = loc.clone().add(wingVec);

                            // Wing particles
                            Particle.DustOptions wingColor = new Particle.DustOptions(
                                    Color.fromRGB(
                                            255,
                                            150 + (int)(105 * Math.sin(s + progress * Math.PI)),
                                            50 + (int)(50 * Math.cos(t + progress * Math.PI))
                                    ), 1.2f
                            );
                            world.spawnParticle(Particle.DUST, wingLoc, 1, 0, 0, 0, 0, wingColor);

                            // Star flame particles
                            if (random.nextFloat() < 0.3) {
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, wingLoc, 1, 0, 0, 0, 0.05);
                                world.spawnParticle(Particle.ENCHANTED_HIT, wingLoc, 1, 0, 0, 0, 0.02);
                            }
                        }
                    }
                }
            }

            private void createStarFragments(Location loc, double progress) {
                int fragmentCount = 12;

                for (int i = 0; i < fragmentCount; i++) {
                    double angle = (Math.PI * 2 * i / fragmentCount) + rotationAngle[0];
                    double radius = 3.0 + Math.sin(progress * Math.PI * 2 + i) * 1.0;

                    for (double r = 0; r < 0.5; r += 0.1) {
                        Vector fragVec = new Vector(
                                Math.cos(angle) * (radius + r),
                                Math.sin(progress * Math.PI * 2) * 1.5,
                                Math.sin(angle) * (radius + r)
                        );

                        Location fragLoc = loc.clone().add(fragVec);

                        // Star fragment particles
                        Particle.DustOptions fragColor = new Particle.DustOptions(
                                Color.fromRGB(255, 255, 200), 1.0f);
                        world.spawnParticle(Particle.DUST, fragLoc, 1, 0, 0, 0, 0, fragColor);

                        // Stellar energy particles
                        if (random.nextFloat() < 0.4) {
                            world.spawnParticle(Particle.END_ROD, fragLoc, 1, 0, 0, 0, 0.02);
                            world.spawnParticle(Particle.ENCHANTED_HIT, fragLoc, 1, 0, 0, 0, 0.02);
                        }
                    }
                }
            }

            private void executePhoenixStrikes(Location loc, double damage, double progress) {
                double strikeRadius = 5.0;

                // Create strike paths
                List<Location> strikePaths = new ArrayList<>();
                int pathCount = 8;

                for (int i = 0; i < pathCount; i++) {
                    double angle = (Math.PI * 2 * i / pathCount) + rotationAngle[0];

                    for (double d = 0; d < strikeRadius; d += 0.2) {
                        Vector pathVec = new Vector(
                                Math.cos(angle) * d,
                                Math.sin(progress * Math.PI * 4) * 0.5,
                                Math.sin(angle) * d
                        );

                        Location pathLoc = loc.clone().add(pathVec);
                        strikePaths.add(pathLoc);

                        // Path particles
                        if (random.nextFloat() < 0.3) {
                            Particle.DustOptions pathColor = new Particle.DustOptions(
                                    Color.fromRGB(255, 200, 100), 0.8f);
                            world.spawnParticle(Particle.DUST, pathLoc, 1, 0, 0, 0, 0, pathColor);
                        }
                    }
                }

                // Check for hits
                for (Entity entity : world.getNearbyEntities(loc, strikeRadius, strikeRadius, strikeRadius)) {
                    if (entity instanceof LivingEntity && entity != player && !hitEntities.contains(entity)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Calculate phoenix damage
                        double progressMultiplier = 1.0 + progress * 0.5; // Up to 50% bonus at peak
                        double finalDamage = damage * progressMultiplier;

                        // Apply damage and effects
                        target.damage(finalDamage, player);
                        hitEntities.add(target);

                        // Apply phoenix effects
                        target.setFireTicks(160);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));

                        // Create stellar impact
                        createStellarImpact(target.getLocation(), progress);

                        // Connect with star fragments
                        connectStarFragments(loc, target.getLocation(), progress);
                    }
                }
            }

            private void createStellarImpact(Location loc, double progress) {
                // Impact flash
                world.spawnParticle(Particle.FLASH, loc, 3, 0.2, 0.2, 0.2, 0);

                // Impact particles
                for (int i = 0; i < 30; i++) {
                    Vector spread = new Vector(
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5,
                            random.nextDouble() - 0.5
                    ).normalize().multiply(random.nextDouble() * 2.0);

                    Location impactLoc = loc.clone().add(spread);

                    // Layered impact effects
                    Particle.DustOptions impactColor = new Particle.DustOptions(
                            Color.fromRGB(255, 200, 100), 1.4f);
                    world.spawnParticle(Particle.DUST, impactLoc, 1, 0, 0, 0, 0, impactColor);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, impactLoc, 2, 0.1, 0.1, 0.1, 0.1);
                    world.spawnParticle(Particle.END_ROD, impactLoc, 2, 0.1, 0.1, 0.1, 0.1);
                }

                // Impact sounds
                world.playSound(loc, Sound.ENTITY_VEX_HURT, 1.0f, 1.5f);
                world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 2.0f);
            }

            private void connectStarFragments(Location start, Location end, double progress) {
                Vector direction = end.toVector().subtract(start.toVector());
                double distance = direction.length();
                direction.normalize();

                for (double d = 0; d < distance; d += 0.2) {
                    Location connectorLoc = start.clone().add(direction.clone().multiply(d));

                    // Connector particles
                    Particle.DustOptions connectorColor = new Particle.DustOptions(
                            Color.fromRGB(255, 255, 200), 0.8f);
                    world.spawnParticle(Particle.DUST, connectorLoc, 1, 0, 0, 0, 0, connectorColor);

                    if (random.nextFloat() < 0.2) {
                        world.spawnParticle(Particle.END_ROD, connectorLoc, 1, 0, 0, 0, 0.02);
                    }
                }
            }

            // ... (rest of the implementations including hit detection,
            // particle effects, and sound effects for each phase)

        }.runTaskTimer(plugin, 0L, 1L);

        // Add user effects for the complete duration
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 1));

        // Add cooldown
        addCooldown(player, "ThirteenthForm", 180); // 3 minute cooldown
    }
}