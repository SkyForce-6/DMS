package org.skyforce.demon.blooddemonart.Rui;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RuiAbility {

    private final Map<UUID, Long> webStringsCooldown = new HashMap<>();
    private final Map<UUID, Long> webCageCooldown = new HashMap<>();
    private final Map<UUID, Long> summonFatherCooldown = new HashMap<>();
    private final Map<UUID, Long> summonSpidersCooldown = new HashMap<>();
    private final Map<UUID, Long> spiderWebCooldown = new HashMap<>();
    private final Map<UUID, Long> bloodWebsCooldown = new HashMap<>();

    // Track players in web cages
    private final Map<UUID, BukkitRunnable> trappedPlayers = new HashMap<>();

    // Track summoned entities
    private final List<Entity> summonedEntities = new ArrayList<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final List<Block> temporaryWebBlocks = new ArrayList<>();

    private final Random random = new Random();

    // Cooldown times in seconds
    private static final long WEB_STRINGS_COOLDOWN = 5;
    private static final long WEB_CAGE_COOLDOWN = 25;
    private static final long SUMMON_FATHER_COOLDOWN = 40;
    private static final long SUMMON_SPIDERS_COOLDOWN = 30;
    private static final long SPIDER_WEB_COOLDOWN = 15;
    private static final long BLOOD_WEBS_COOLDOWN = 60;

    /**
     * Web Strings - Basic attack
     * Shoots web strings in the direction the player is looking
     */
    public void activateWebStrings(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (webStringsCooldown.containsKey(playerId)) {
            long timeLeft = (webStringsCooldown.get(playerId) + WEB_STRINGS_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§fYou must wait " + timeLeft + " seconds before using Web Strings again!");
                return;
            }
        }

        webStringsCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§f§lWeb Strings§f launched!");
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.2F);

        // Launch multiple web strings
        for (int i = 0; i < 3; i++) {
            // Slight variation in direction
            Vector direction = player.getLocation().getDirection().normalize();
            direction.add(new Vector(
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1
            )).normalize();

            // Delay each string slightly
            final int delay = i * 3;
            final Vector finalDirection = direction;

            new BukkitRunnable() {
                @Override
                public void run() {
                    shootWebString(player.getEyeLocation(), finalDirection, player, plugin, false);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    /**
     * Helper method to shoot a web string
     */
    private void shootWebString(Location startLoc, Vector direction, Player owner, Main plugin, boolean isBloodWeb) {
        World world = startLoc.getWorld();

        // Create web string projectile
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 40; // 2 seconds max travel time
            private Location currentLoc = startLoc.clone();
            private boolean hasHit = false;
            private final List<Location> stringPath = new ArrayList<>();

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks || hasHit) {
                    // Fade effect for the string
                    new BukkitRunnable() {
                        private int fadeTicks = 0;
                        @Override
                        public void run() {
                            fadeTicks++;
                            if (fadeTicks > 20) {
                                this.cancel();
                                return;
                            }

                            // Fade out particle effect
                            for (int i = 0; i < stringPath.size(); i += 2) {
                                Location loc = stringPath.get(i);
                                if (isBloodWeb) {
                                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0,
                                            new Particle.DustOptions(Color.RED, 1.0F - (fadeTicks / 20.0F)));
                                } else {
                                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0,
                                            new Particle.DustOptions(Color.WHITE, 1.0F - (fadeTicks / 20.0F)));
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1);

                    this.cancel();
                    return;
                }

                // Move the web string
                currentLoc.add(direction.multiply(1));
                stringPath.add(currentLoc.clone());

                // Draw the web string
                if (isBloodWeb) {
                    world.spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.RED, 1.0F));
                } else {
                    world.spawnParticle(Particle.DUST, currentLoc, 1, 0, 0, 0,
                            new Particle.DustOptions(Color.WHITE, 1.0F));
                }

                // Check for collision with blocks
                if (currentLoc.getBlock().getType().isSolid()) {
                    hasHit = true;
                    return;
                }

                // Check for collision with entities
                for (Entity entity : world.getNearbyEntities(currentLoc, 1, 1, 1)) {
                    if (entity instanceof LivingEntity && entity != owner && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        double damage = isBloodWeb ? 6.0 : 4.0;
                        target.damage(damage, owner);

                        // Apply slow and weakness
                        int slowDuration = isBloodWeb ? 100 : 60; // 5 or 3 seconds
                        int slowAmplifier = isBloodWeb ? 2 : 1;   // Level 3 or 2

                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration, slowAmplifier));

                        if (isBloodWeb) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1)); // 4 seconds
                            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0)); // 3 seconds
                        }

                        // Visual effect
                        if (isBloodWeb) {
                            world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                                    20, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.RED, 1.0F));
                        } else {
                            world.spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                    20, 0.5, 0.5, 0.5, 0, Material.COBWEB.createBlockData());
                        }

                        world.playSound(target.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.5F);

                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Web Cage - Trap, deals damage if escaping
     */
    public void activateWebCage(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (webCageCooldown.containsKey(playerId)) {
            long timeLeft = (webCageCooldown.get(playerId) + WEB_CAGE_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§fYou must wait " + timeLeft + " seconds before using Web Cage again!");
                return;
            }
        }

        webCageCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§f§lWeb Cage§f deployed!");
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 0.8F);

        // Get target location (block player is looking at)
        Block targetBlock = player.getTargetBlock(null, 20);
        Location cageLocation = targetBlock.getLocation().add(0, 1, 0);

        // Create the web cage
        createWebCage(cageLocation, player, plugin, false);
    }

    /**
     * Helper method to create a web cage
     */
    private void createWebCage(Location center, Player owner, Main plugin, boolean isBloodWeb) {
        World world = center.getWorld();

        // Initial cage formation effect
        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 30;
            double radius = 3;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = center.clone().add(x, 0, z);

            if (isBloodWeb) {
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.RED, 1.0F));
            } else {
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                        new Particle.DustOptions(Color.WHITE, 1.0F));
            }
        }

        world.playSound(center, Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 0.5F);

        // Create web blocks in a cage pattern
        List<Block> webBlocks = new ArrayList<>();
        int radius = 2;
        int height = 3;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < height; y++) {
                    // Only create blocks on the perimeter
                    if (Math.abs(x) == radius || Math.abs(z) == radius || y == 0 || y == height - 1) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.COBWEB);
                            webBlocks.add(block);
                            temporaryWebBlocks.add(block);
                        }
                    }
                }
            }
        }

        // Find entities trapped in the cage
        for (Entity entity : world.getNearbyEntities(center, radius, height, radius)) {
            if (entity instanceof LivingEntity && entity != owner && !(entity instanceof ArmorStand)) {
                LivingEntity target = (LivingEntity) entity;

                // Initial damage and effects
                if (isBloodWeb) {
                    target.damage(3.0, owner);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1)); // 5 seconds
                }

                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    targetPlayer.sendMessage(isBloodWeb ?
                            "§c§lYou are trapped in Rui's Blood Web Cage!" :
                            "§f§lYou are trapped in Rui's Web Cage!");

                    // Track the trapped player
                    UUID targetId = targetPlayer.getUniqueId();

                    // If there's already a runnable for this player, cancel it
                    if (trappedPlayers.containsKey(targetId)) {
                        trappedPlayers.get(targetId).cancel();
                    }

                    // Create a new runnable to monitor player's position
                    Location initialPos = targetPlayer.getLocation().clone();
                    BukkitRunnable monitorTask = new BukkitRunnable() {
                        private int ticks = 0;
                        private final int duration = 200; // 10 seconds

                        @Override
                        public void run() {
                            ticks++;

                            if (ticks > duration || !targetPlayer.isOnline()) {
                                trappedPlayers.remove(targetId);
                                this.cancel();
                                return;
                            }

                            // Apply slowing effect continuously
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2)); // 2 seconds

                            // Check if player tries to move too far from initial position
                            double distance = targetPlayer.getLocation().distance(initialPos);
                            if (distance > 2.5) {
                                // Damage for trying to escape
                                double escapeDamage = isBloodWeb ? 4.0 : 2.0;
                                targetPlayer.damage(escapeDamage, owner);

                                targetPlayer.sendMessage(isBloodWeb ?
                                        "§c§lThe blood webs constrict as you try to escape!" :
                                        "§f§lThe webs tighten as you try to escape!");

                                // Visual effect
                                if (isBloodWeb) {
                                    world.spawnParticle(Particle.DUST, targetPlayer.getLocation().add(0, 1, 0),
                                            30, 0.5, 1, 0.5, new Particle.DustOptions(Color.RED, 1.0F));
                                } else {
                                    world.spawnParticle(Particle.BLOCK, targetPlayer.getLocation().add(0, 1, 0),
                                            30, 0.5, 1, 0.5, 0, Material.COBWEB.createBlockData());
                                }

                                // Push player back to center
                                Vector pushBack = initialPos.toVector().subtract(targetPlayer.getLocation().toVector()).normalize().multiply(1.0);
                                targetPlayer.setVelocity(pushBack);
                            }

                            // Particle effects to show the cage boundary
                            if (ticks % 10 == 0) {
                                for (int i = 0; i < 10; i++) {
                                    double angle = 2 * Math.PI * random.nextDouble();
                                    double r = radius;
                                    double x = Math.cos(angle) * r;
                                    double z = Math.sin(angle) * r;
                                    double y = random.nextDouble() * height;

                                    Location particleLoc = initialPos.clone().add(x, y, z);

                                    if (isBloodWeb) {
                                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                                new Particle.DustOptions(Color.RED, 1.0F));
                                    } else {
                                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                                new Particle.DustOptions(Color.WHITE, 1.0F));
                                    }
                                }
                            }
                        }
                    };

                    monitorTask.runTaskTimer(plugin, 0, 1);
                    trappedPlayers.put(targetId, monitorTask);
                }
            }
        }

        // Remove web blocks after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : webBlocks) {
                    if (block.getType() == Material.COBWEB) {
                        block.setType(Material.AIR);
                    }
                }
                temporaryWebBlocks.removeAll(webBlocks);
            }
        }.runTaskLater(plugin, 200); // 10 seconds
    }

    /**
     * Summon: Father - Heavy slam damage
     */
    public void activateSummonFather(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (summonFatherCooldown.containsKey(playerId)) {
            long timeLeft = (summonFatherCooldown.get(playerId) + SUMMON_FATHER_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§fYou must wait " + timeLeft + " seconds before using Summon: Father again!");
                return;
            }
        }

        summonFatherCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§f§lSummon: Father§f called!");
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0F, 0.5F);

        // Get summon location in front of player
        Location summonLoc = player.getLocation().add(player.getLocation().getDirection().multiply(3));

        // Create web portal effect
        createWebPortal(summonLoc, player, plugin, false);

        // Summon the Father
        new BukkitRunnable() {
            @Override
            public void run() {
                Zombie father = (Zombie) summonLoc.getWorld().spawnEntity(summonLoc, EntityType.ZOMBIE);

                // Customize the Father
                father.setCustomName("§f§lRui's Father");
                father.setCustomNameVisible(true);
                father.setRemoveWhenFarAway(false);
                father.getEquipment().setHelmet(new ItemStack(Material.ZOMBIE_HEAD));
                father.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                father.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                father.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

                // Apply effects
                father.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 600, 2)); // 30 seconds, Level 3
                father.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 1)); // 30 seconds, Level 2
                father.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // 30 seconds, Level 2

                summonedEntities.add(father);

                // Summon effect
                summonLoc.getWorld().spawnParticle(Particle.EXPLOSION, summonLoc, 1, 0, 0, 0, 0);
                summonLoc.getWorld().playSound(summonLoc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0F, 0.5F);

                // Make the Father target nearby entities
                for (Entity entity : summonLoc.getWorld().getNearbyEntities(summonLoc, 15, 15, 15)) {
                    if (entity instanceof LivingEntity && entity != player && entity != father && !(entity instanceof ArmorStand)) {
                        father.setTarget((LivingEntity) entity);
                        break;
                    }
                }

                // Remove the Father after 30 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (father.isValid()) {
                            // Disappearance effect
                            father.getWorld().spawnParticle(Particle.BLOCK, father.getLocation().add(0, 1, 0),
                                    30, 0.5, 1, 0.5, 0, Material.COBWEB.createBlockData());
                            father.getWorld().playSound(father.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1.0F, 0.5F);

                            father.remove();
                            summonedEntities.remove(father);
                        }
                    }
                }.runTaskLater(plugin, 600); // 30 seconds
            }
        }.runTaskLater(plugin, 20); // 1 second delay for portal effect
    }

    /**
     * Helper method to create a web portal effect
     */
    private void createWebPortal(Location location, Player owner, Main plugin, boolean isBloodWeb) {
        World world = location.getWorld();

        new BukkitRunnable() {
            private int ticks = 0;
            private final int formationTime = 20; // 1 second to form
            private double size = 0.5;
            private final double maxSize = 2.0;

            @Override
            public void run() {
                ticks++;

                if (ticks > formationTime) {
                    this.cancel();
                    return;
                }

                // Increase portal size
                size = 0.5 + (maxSize - 0.5) * (ticks / (double) formationTime);

                // Create portal particles
                for (int i = 0; i < 15; i++) {
                    double angle = 2 * Math.PI * i / 15;
                    double x = Math.cos(angle) * size;
                    double y = Math.sin(angle) * size;

                    Location particleLoc = location.clone().add(x, y, 0);

                    if (isBloodWeb) {
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.RED, 1.0F));
                    } else {
                        world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0,
                                new Particle.DustOptions(Color.WHITE, 1.0F));
                    }
                }

                // Web strands inside the portal
                for (int i = 0; i < 5; i++) {
                    double x1 = (random.nextDouble() * 2 - 1) * size;
                    double y1 = (random.nextDouble() * 2 - 1) * size;
                    double x2 = (random.nextDouble() * 2 - 1) * size;
                    double y2 = (random.nextDouble() * 2 - 1) * size;

                    Location start = location.clone().add(x1, y1, 0);
                    Location end = location.clone().add(x2, y2, 0);

                    // Draw a line between points
                    Vector direction = end.toVector().subtract(start.toVector());
                    double length = direction.length();
                    direction.normalize();

                    for (double d = 0; d < length; d += 0.2) {
                        Location point = start.clone().add(direction.clone().multiply(d));

                        if (isBloodWeb) {
                            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.RED, 0.7F));
                        } else {
                            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0,
                                    new Particle.DustOptions(Color.WHITE, 0.7F));
                        }
                    }
                }

                // Sound effect
                if (ticks % 5 == 0) {
                    world.playSound(location, Sound.ENTITY_SPIDER_AMBIENT, 0.5F, 1.0F);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Summon: Spiders - Multiple attackers, temporary
     */
    public void activateSummonSpiders(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (summonSpidersCooldown.containsKey(playerId)) {
            long timeLeft = (summonSpidersCooldown.get(playerId) + SUMMON_SPIDERS_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§fYou must wait " + timeLeft + " seconds before using Summon: Spiders again!");
                return;
            }
        }

        summonSpidersCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§f§lSummon: Spiders§f unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.0F);

        // Summon multiple spiders around the player
        int spiderCount = 5;
        List<Location> summonLocations = new ArrayList<>();

        // Calculate spawn positions in a circle around player
        for (int i = 0; i < spiderCount; i++) {
            double angle = 2 * Math.PI * i / spiderCount;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;

            Location summonLoc = player.getLocation().add(x, 0, z);
            summonLocations.add(summonLoc);

            // Create web portal effect
            createWebPortal(summonLoc, player, plugin, false);
        }

        // Summon spiders after portal effect
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location summonLoc : summonLocations) {
                    // 50% chance for regular spider, 50% chance for cave spider
                    EntityType spiderType = random.nextBoolean() ? EntityType.SPIDER : EntityType.CAVE_SPIDER;
                    LivingEntity spider = (LivingEntity) summonLoc.getWorld().spawnEntity(summonLoc, spiderType);

                    // Customize the spider
                    String spiderName = spiderType == EntityType.SPIDER ? "§f§lRui's Spider" : "§f§lRui's Venomous Spider";
                    spider.setCustomName(spiderName);
                    spider.setCustomNameVisible(true);
                    spider.setRemoveWhenFarAway(false);

                    // Apply effects
                    spider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1)); // 20 seconds, Level 2
                    if (spiderType == EntityType.CAVE_SPIDER) {
                        spider.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 400, 1)); // 20 seconds, Level 2
                    }

                    summonedEntities.add(spider);

                    // Summon effect
                    summonLoc.getWorld().spawnParticle(Particle.BLOCK, summonLoc,
                            20, 0.3, 0.3, 0.3, 0, Material.COBWEB.createBlockData());
                    summonLoc.getWorld().playSound(summonLoc, Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.2F);

                    // Make spiders target nearby entities
                    findTarget(spider, player, 15);
                }

                // Remove spiders after 20 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Entity entity : new ArrayList<>(summonedEntities)) {
                            if (entity.isValid() && (entity instanceof Spider || entity instanceof CaveSpider)) {
                                // Disappearance effect
                                entity.getWorld().spawnParticle(Particle.BLOCK, entity.getLocation(),
                                        15, 0.3, 0.3, 0.3, 0, Material.COBWEB.createBlockData());

                                entity.remove();
                                summonedEntities.remove(entity);
                            }
                        }
                    }
                }.runTaskLater(plugin, 400); // 20 seconds
            }
        }.runTaskLater(plugin, 20); // 1 second delay for portal effect
    }

    /**
     * Helper method to find a target for summoned entities
     */
    private void findTarget(LivingEntity summonedEntity, Player owner, int radius) {
        for (Entity entity : summonedEntity.getWorld().getNearbyEntities(summonedEntity.getLocation(), radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != owner && entity != summonedEntity &&
                    !(entity instanceof ArmorStand) && !summonedEntities.contains(entity)) {

                if (summonedEntity instanceof Spider) {
                    ((Spider) summonedEntity).setTarget((LivingEntity) entity);
                } else if (summonedEntity instanceof CaveSpider) {
                    ((CaveSpider) summonedEntity).setTarget((LivingEntity) entity);
                } else if (summonedEntity instanceof Zombie) {
                    ((Zombie) summonedEntity).setTarget((LivingEntity) entity);
                }

                return;
            }
        }
    }

    /**
     * Spider Web - Large area slash
     */
    public void activateSpiderWeb(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (spiderWebCooldown.containsKey(playerId)) {
            long timeLeft = (spiderWebCooldown.get(playerId) + SPIDER_WEB_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§fYou must wait " + timeLeft + " seconds before using Spider Web again!");
                return;
            }
        }

        spiderWebCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§f§lSpider Web§f slash!");
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.0F);

        // Get direction and create the slash effect
        Vector direction = player.getLocation().getDirection().normalize();
        createWebSlash(player.getEyeLocation(), direction, player, plugin, false);
    }

    /**
     * Helper method to create a web slash
     */
    private void createWebSlash(Location start, Vector direction, Player owner, Main plugin, boolean isBloodWeb) {
        World world = start.getWorld();

        // Adjust direction to be more horizontal
        direction.setY(direction.getY() * 0.5);
        direction.normalize();

        // Create perpendicular vector for width
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // Slash effect
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 20; // 1 second
            private double distance = 0;
            private final double maxDistance = 15;
            private final double slashWidth = 5;

            @Override
            public void run() {
                ticks++;

                if (ticks > maxTicks || distance >= maxDistance) {
                    this.cancel();
                    return;
                }

                // Advance the slash
                distance += maxDistance / maxTicks;
                Location currentCenter = start.clone().add(direction.clone().multiply(distance));

                // Create the slash visualization
                for (double w = -slashWidth/2; w <= slashWidth/2; w += 0.5) {
                    Location point = currentCenter.clone().add(perpendicular.clone().multiply(w));

                    if (isBloodWeb) {
                        world.spawnParticle(Particle.DUST, point, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.RED, 1.0F));
                    } else {
                        world.spawnParticle(Particle.DUST, point, 1, 0.1, 0.1, 0.1,
                                new Particle.DustOptions(Color.WHITE, 1.0F));
                    }

                    // Add some web strands for effect
                    if (random.nextDouble() < 0.2) {
                        double strandLength = random.nextDouble() * 1.5;
                        Vector strandDir = new Vector(
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1,
                                random.nextDouble() * 2 - 1
                        ).normalize();

                        for (double s = 0; s < strandLength; s += 0.2) {
                            Location strandPoint = point.clone().add(strandDir.clone().multiply(s));

                            if (isBloodWeb) {
                                world.spawnParticle(Particle.DUST, strandPoint, 1, 0, 0, 0,
                                        new Particle.DustOptions(Color.RED, 0.7F));
                            } else {
                                world.spawnParticle(Particle.DUST, strandPoint, 1, 0, 0, 0,
                                        new Particle.DustOptions(Color.WHITE, 0.7F));
                            }
                        }
                    }

                    // Randomly place cobwebs
                    if (random.nextDouble() < 0.02 && point.getBlock().getType() == Material.AIR) {
                        point.getBlock().setType(Material.COBWEB);
                        temporaryWebBlocks.add(point.getBlock());

                        // Remove the cobweb after some time
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (point.getBlock().getType() == Material.COBWEB) {
                                    point.getBlock().setType(Material.AIR);
                                    temporaryWebBlocks.remove(point.getBlock());
                                }
                            }
                        }.runTaskLater(plugin, 100 + random.nextInt(100)); // 5-10 seconds
                    }
                }

                // Sound effect
                if (ticks % 4 == 0) {
                    world.playSound(currentCenter, Sound.ENTITY_SPIDER_AMBIENT, 0.5F, 1.5F);
                }

                // Check for entities hit by the slash
                for (Entity entity : world.getNearbyEntities(currentCenter, slashWidth/2, 2, slashWidth/2)) {
                    if (entity instanceof LivingEntity && entity != owner && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply damage and effects
                        double damage = isBloodWeb ? 7.0 : 5.0;
                        target.damage(damage, owner);

                        // Apply slow and weakness
                        int slowDuration = isBloodWeb ? 100 : 60; // 5 or 3 seconds
                        int slowAmplifier = isBloodWeb ? 2 : 1;   // Level 3 or 2

                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration, slowAmplifier));

                        if (isBloodWeb) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1)); // 4 seconds
                            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0)); // 3 seconds
                        }

                        // Visual effect
                        if (isBloodWeb) {
                            world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0),
                                    30, 0.5, 1, 0.5, new Particle.DustOptions(Color.RED, 1.0F));
                        } else {
                            world.spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                                    30, 0.5, 1, 0.5, 0, Material.COBWEB.createBlockData());
                        }

                        world.playSound(target.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.0F);

                        // Push the target back
                        Vector knockback = direction.clone().multiply(1.5);
                        target.setVelocity(target.getVelocity().add(knockback));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Blood Webs - Stronger, red web variant (more damage)
     */
    public void activateBloodWebs(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (bloodWebsCooldown.containsKey(playerId)) {
            long timeLeft = (bloodWebsCooldown.get(playerId) + BLOOD_WEBS_COOLDOWN * 1000 - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cYou must wait " + timeLeft + " seconds before using Blood Webs again!");
                return;
            }
        }

        bloodWebsCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§c§lBlood Webs§c unleashed!");
        player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 0.5F);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8F, 0.8F);

        // Create blood web effect around player
        Location playerLoc = player.getLocation();

        // Initial blood effect
        player.getWorld().spawnParticle(Particle.DUST, playerLoc.add(0, 1, 0),
                50, 1, 1, 1, new Particle.DustOptions(Color.RED, 1.5F));

        // Create blood web tendrils in all directions
        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8;
            Vector direction = new Vector(Math.cos(angle), 0, Math.sin(angle));

            // Launch blood web strings
            new BukkitRunnable() {
                @Override
                public void run() {
                    shootWebString(player.getEyeLocation(), direction, player, plugin, true);
                }
            }.runTaskLater(plugin, i * 3);
        }

        // Create a blood web cage at target location
        Block targetBlock = player.getTargetBlock(null, 15);
        Location cageLocation = targetBlock.getLocation().add(0, 1, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                createWebCage(cageLocation, player, plugin, true);
            }
        }.runTaskLater(plugin, 10);

        // Create a blood web slash
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector slashDirection = player.getLocation().getDirection().normalize();
                createWebSlash(player.getEyeLocation(), slashDirection, player, plugin, true);
            }
        }.runTaskLater(plugin, 20);
    }

    /**
     * Apply web effect to normal attacks
     */
    public void applyWebEffectToAttack(Player player, EntityDamageByEntityEvent event) {
        // 25% chance to apply web effect on normal attacks
        if (random.nextDouble() < 0.25) {
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                // Apply slow effect
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0)); // 2 seconds, Level 1

                // Visual effect
                target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0),
                        15, 0.3, 0.5, 0.3, 0, Material.COBWEB.createBlockData());

                // Increase damage slightly
                event.setDamage(event.getDamage() * 1.2);

                player.sendMessage("§fYour attack ensnares " + target.getName() + " with webs!");
            }
        }
    }

    /**
     * Cleanup method to remove all temporary entities and blocks when plugin is reloaded
     */
    public void cleanup() {
        // Remove temporary armor stands
        for (ArmorStand entity : temporaryEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        temporaryEntities.clear();

        // Remove summoned entities
        for (Entity entity : summonedEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        summonedEntities.clear();

        // Remove temporary web blocks
        for (Block block : temporaryWebBlocks) {
            if (block != null && block.getType() == Material.COBWEB) {
                block.setType(Material.AIR);
            }
        }
        temporaryWebBlocks.clear();

        // Cancel all trapped player tasks
        for (BukkitRunnable task : trappedPlayers.values()) {
            task.cancel();
        }
        trappedPlayers.clear();
    }
}