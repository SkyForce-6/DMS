package org.skyforce.demon.blooddemonart.Douma;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

public class DoumaAbility {

    private final Map<UUID, Long> frozenLotusCooldown = new HashMap<>();
    private final Map<UUID, Long> frozenMistCooldown = new HashMap<>();
    private final Map<UUID, Long> barrenHangingGardenCooldown = new HashMap<>();
    private final Map<UUID, Long> freezingCloudsCooldown = new HashMap<>();
    private final Map<UUID, Long> coldWhitePrincessesCooldown = new HashMap<>();
    private final Map<UUID, Long> wintryIciclesCooldown = new HashMap<>();
    private final Map<UUID, Long> lotusVinesCooldown = new HashMap<>();
    private final Map<UUID, Long> crystallineDivineChildCooldown = new HashMap<>();
    private final Map<UUID, Long> rimeWaterLilyBodhisattvaCooldown = new HashMap<>();
    private final Map<UUID, Long> iceCloneBreathAttackCooldown = new HashMap<>();

    private final List<Block> temporaryIceBlocks = new ArrayList<>();
    private final List<ArmorStand> temporaryEntities = new ArrayList<>();
    private final Random random = new Random();

    // Cooldown-Zeiten in Millisekunden
    private static final long FROZEN_LOTUS_COOLDOWN = 2; // 20 Sekunden
    private static final long FROZEN_MIST_COOLDOWN = 3; // 30 Sekunden
    private static final long BARREN_HANGING_GARDEN_COOLDOWN = 4; // 45 Sekunden
    private static final long FREEZING_CLOUDS_COOLDOWN = 1; // 15 Sekunden
    private static final long COLD_WHITE_PRINCESSES_COOLDOWN = 2; // 25 Sekunden
    private static final long WINTRY_ICICLES_COOLDOWN = 2; // 20 Sekunden
    private static final long LOTUS_VINES_COOLDOWN = 3; // 35 Sekunden
    private static final long CRYSTALLINE_DIVINE_CHILD_COOLDOWN = 6; // 60 Sekunden
    private static final long RIME_WATER_LILY_BODHISATTVA_COOLDOWN = 1; // 120 Sekunden (2 Minuten)
    private static final long ICE_CLONE_BREATH_ATTACK_COOLDOWN = 4; // 40 Sekunden

    /**
     * Frozen Lotus - Erzeugt eine Lotusblume aus Eis, die Gegner in der Nähe einfriert
     */
    public void activateFrozenLotus(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (frozenLotusCooldown.containsKey(playerId)) {
            long timeLeft = (frozenLotusCooldown.get(playerId) + FROZEN_LOTUS_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Frozen Lotus wieder einsetzen kannst!");
                return;
            }
        }

        frozenLotusCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lFrozen Lotus§b aktiviert!");
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.2F);

        Location targetLoc = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);

        // Erstelle eine Eis-Lotusblume (visueller Effekt mit Eisblöcken)
        createIceLotus(targetLoc, plugin);

        // Finde Gegner in der Nähe und wende Effekte an
        for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, 5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;

                // Verlangsame und füge Schaden zu
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2)); // 5 Sekunden, Level 3
                target.damage(6.0, player);

                // Eispartikel-Effekt
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                        30, 0.5, 1, 0.5, 0.1);
            }
        }
    }

    /**
     * Hilfsmethode zum Erstellen einer visuellen Lotusblume aus Eis
     */
    private void createIceLotus(Location center, Main plugin) {
        // Erstelle den Stiel
        for (int y = 0; y < 3; y++) {
            Block block = center.clone().add(0, y, 0).getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.PACKED_ICE);
                temporaryIceBlocks.add(block);
            }
        }

        // Erstelle die Blütenblätter (ein einfaches Muster)
        int[][] petalPattern = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {1, 1}, {-1, 1}, {1, -1}
        };

        for (int[] offset : petalPattern) {
            Block block = center.clone().add(offset[0], 3, offset[1]).getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(Material.BLUE_ICE);
                temporaryIceBlocks.add(block);
            }
        }

        // Eispartikel-Effekt
        center.getWorld().spawnParticle(Particle.SNOWFLAKE, center.clone().add(0, 4, 0),
                50, 2, 2, 2, 0.05);

        // Entferne die Eisblöcke nach 10 Sekunden
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : temporaryIceBlocks) {
                    if (block.getType() == Material.PACKED_ICE || block.getType() == Material.BLUE_ICE) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5),
                                10, 0.3, 0.3, 0.3, 0.05);
                    }
                }
                temporaryIceBlocks.clear();
            }
        }.runTaskLater(plugin, 200); // 200 Ticks = 10 Sekunden
    }

    /**
     * Frozen Mist - Erzeugt einen Nebel aus Eis, der Gegner verlangsamt und ihnen schadet
     */
    public void activateFrozenMist(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (frozenMistCooldown.containsKey(playerId)) {
            long timeLeft = (frozenMistCooldown.get(playerId) + FROZEN_MIST_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Frozen Mist wieder einsetzen kannst!");
                return;
            }
        }

        frozenMistCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lFrozen Mist§b hüllt die Umgebung ein!");
        player.playSound(player.getLocation(), Sound.BLOCK_SNOW_FALL, 1.0F, 0.5F);

        // Erzeuge einen Nebel aus Eispartikeln um den Spieler herum
        Location playerLoc = player.getLocation();

        // Erstelle den Nebeleffekt und führe Schadenseffekte über Zeit aus
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 100; // 5 Sekunden (100 Ticks)

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    this.cancel();
                    return;
                }

                // Eispartikel-Effekt in einem größeren Bereich
                for (int i = 0; i < 5; i++) {
                    double x = random.nextDouble() * 10 - 5;
                    double y = random.nextDouble() * 2;
                    double z = random.nextDouble() * 10 - 5;

                    Location particleLoc = playerLoc.clone().add(x, y, z);
                    playerLoc.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 1, 0.2, 0.2, 0.2, 0.01);
                    playerLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                }

                // Alle 20 Ticks (1 Sekunde) prüfe Gegner in der Nähe
                if (ticks % 20 == 0) {
                    for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 8, 3, 8)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Verlangsame und füge Schaden zu
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1)); // 2 Sekunden, Level 2
                            target.damage(2.0, player);

                            // Eispartikel-Effekt direkt am Ziel
                            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                                    10, 0.5, 1, 0.5, 0.05);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Barren Hanging Garden - Erzeugt ein "Garten" aus hängenden Eiszapfen, die auf Gegner fallen
     */
    public void activateBarrenHangingGarden(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (barrenHangingGardenCooldown.containsKey(playerId)) {
            long timeLeft = (barrenHangingGardenCooldown.get(playerId) + BARREN_HANGING_GARDEN_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Barren Hanging Garden wieder einsetzen kannst!");
                return;
            }
        }

        barrenHangingGardenCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lBarren Hanging Garden§b wird beschworen!");
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 0.8F);

        Location centerLoc = player.getLocation();

        // Erstelle mehrere fallende Eiszapfen über einen Zeitraum
        new BukkitRunnable() {
            private int ticks = 0;
            private final int duration = 100; // 5 Sekunden (100 Ticks)

            @Override
            public void run() {
                ticks++;

                if (ticks > duration) {
                    this.cancel();
                    return;
                }

                // Alle 10 Ticks (0.5 Sekunden) erstelle neue Eiszapfen
                if (ticks % 10 == 0) {
                    // Erstelle 3 Eiszapfen an zufälligen Positionen
                    for (int i = 0; i < 3; i++) {
                        double x = random.nextDouble() * 16 - 8;
                        double z = random.nextDouble() * 16 - 8;

                        Location icicleStartLoc = centerLoc.clone().add(x, 10, z);
                        createFallingIcicle(icicleStartLoc, player, plugin);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Hilfsmethode zum Erstellen eines fallenden Eiszapfens
     */
    private void createFallingIcicle(Location startLoc, Player owner, Main plugin) {
        // Erstelle einen visuellen ArmorStand mit Eisblock als Kopf
        ArmorStand icicle = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        icicle.setVisible(false);
        icicle.setGravity(false);
        icicle.setInvulnerable(true);
        icicle.getEquipment().setHelmet(new ItemStack(Material.ICE));

        temporaryEntities.add(icicle);

        // Eispartikel-Effekt
        startLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, startLoc, 15, 0.2, 0.2, 0.2, 0.05);

        // Animiere den fallenden Eiszapfen
        new BukkitRunnable() {
            private int ticks = 0;
            private final Location targetLoc = startLoc.clone().subtract(0, 10, 0);
            private final Vector direction = new Vector(0, -0.5, 0);
            private boolean hasHit = false;

            @Override
            public void run() {
                ticks++;

                if (ticks > 40 || hasHit || !icicle.isValid()) {
                    icicle.remove();
                    this.cancel();
                    return;
                }

                // Bewege den Eiszapfen nach unten
                icicle.teleport(icicle.getLocation().add(direction));

                // Eispartikel-Effekt während der Bewegung
                icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, icicle.getLocation(), 3, 0.1, 0.1, 0.1, 0.01);

                // Prüfe auf Kollision mit dem Boden oder Entitäten
                Block block = icicle.getLocation().getBlock();
                if (block.getType().isSolid()) {
                    hasHit = true;

                    // Eisexplosions-Effekt
                    icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, icicle.getLocation(), 30, 1, 0.2, 1, 0.1);
                    icicle.getWorld().playSound(icicle.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.2F);

                    return;
                }

                // Prüfe auf Kollision mit Entitäten
                for (Entity entity : icicle.getLocation().getWorld().getNearbyEntities(icicle.getLocation(), 1, 2, 1)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != icicle) {
                        LivingEntity target = (LivingEntity) entity;

                        // Füge Schaden zu und verlangsame
                        target.damage(8.0, owner);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 3 Sekunden, Level 2

                        // Eisexplosions-Effekt
                        icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                                30, 0.5, 1, 0.5, 0.1);
                        icicle.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.2F);

                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Freezing Clouds - Passiver Effekt beim Angreifen, der Gegner einfriert
     */
    public void activateFreezingClouds(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (freezingCloudsCooldown.containsKey(playerId)) {
            long timeLeft = (freezingCloudsCooldown.get(playerId) + FREEZING_CLOUDS_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                return; // Stiller Cooldown für passiven Effekt
            }
        }

        // 35% Chance, den Frost-Effekt zu aktivieren
        if (Math.random() < 0.35) {
            freezingCloudsCooldown.put(playerId, System.currentTimeMillis());

            // Verstärke Schaden um 30%
            double damage = event.getDamage() * 1.3;
            event.setDamage(damage);

            // Füge Verlangsamung und Mining Fatigue Effekte hinzu
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2)); // 4 Sekunden, Level 3
            target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1)); // 3 Sekunden, Level 2

            // Eispartikel-Effekt
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                    30, 0.5, 1, 0.5, 0.1);
            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, 1, 0),
                    15, 0.3, 0.5, 0.3, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0F, 1.2F);

            player.sendMessage("§b§lFreezing Clouds§b umhüllen " + target.getName() + "!");
        }
    }

    /**
     * Cold White Princesses - Beschwört eisige "Prinzessinnen", die Gegner angreifen
     */
    public void activateColdWhitePrincesses(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (coldWhitePrincessesCooldown.containsKey(playerId)) {
            long timeLeft = (coldWhitePrincessesCooldown.get(playerId) + COLD_WHITE_PRINCESSES_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Cold White Princesses wieder einsetzen kannst!");
                return;
            }
        }

        coldWhitePrincessesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lCold White Princesses§b erscheinen!");
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.2F);

        Location playerLoc = player.getLocation();

        // Finde bis zu 3 Gegner in der Nähe
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 20, 10, 20)) {
            if (entity instanceof LivingEntity && entity != player &&
                    !(entity instanceof Player && ((Player) entity).getGameMode().toString().contains("CREATIVE"))) {
                targets.add((LivingEntity) entity);
                if (targets.size() >= 3) break;
            }
        }

        if (targets.isEmpty()) {
            player.sendMessage("§cKeine Ziele für Cold White Princesses gefunden!");
            return;
        }

        // Erstelle "Prinzessinnen" für jeden Gegner
        for (LivingEntity target : targets) {
            createIcePrincess(player.getLocation(), target, player, plugin);
        }
    }

    /**
     * Hilfsmethode zum Erstellen einer "Eisprinzessin"
     */
    private void createIcePrincess(Location startLoc, LivingEntity target, Player owner, Main plugin) {
        // Erstelle einen ArmorStand als visuelle Darstellung
        ArmorStand princess = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        princess.setVisible(false);
        princess.setGravity(false);
        princess.setInvulnerable(true);
        princess.getEquipment().setHelmet(new ItemStack(Material.SNOW_BLOCK));

        temporaryEntities.add(princess);

        owner.sendMessage("§bEisprinzessin greift " + target.getName() + " an!");

        // Erstelle Eisprinzessin-Animation und Angriff
        new BukkitRunnable() {
            private int ticks = 0;
            private final Location targetLoc = target.getLocation();
            private final Vector direction = targetLoc.toVector().subtract(startLoc.toVector()).normalize().multiply(0.4);

            @Override
            public void run() {
                ticks++;

                if (ticks > 60 || !target.isValid() || !princess.isValid()) {
                    princess.remove();
                    this.cancel();
                    return;
                }

                // Berechne aktuelle Position
                princess.teleport(princess.getLocation().add(direction));

                // Eispartikel-Effekt
                princess.getWorld().spawnParticle(Particle.SNOWFLAKE, princess.getLocation(), 5, 0.2, 0.5, 0.2, 0.02);
                princess.getWorld().spawnParticle(Particle.CLOUD, princess.getLocation(), 2, 0.1, 0.3, 0.1, 0.01);

                // Wenn die Prinzessin das Ziel erreicht hat oder sehr nahe ist
                if (princess.getLocation().distance(target.getLocation()) < 1.5) {
                    // Schaden zufügen
                    target.damage(7.0, owner);

                    // Verlangsamungs- und Schwächeeffekt
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // 5 Sekunden, Level 2
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0)); // 4 Sekunden, Level 1

                    // Eisexplosions-Effekt
                    princess.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                            40, 0.5, 1, 0.5, 0.2);
                    princess.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, 1, 0),
                            20, 0.3, 0.8, 0.3, 0.1);
                    princess.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);

                    princess.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Wintry Icicles - Schießt Eiszapfen in Blickrichtung
     */
    public void activateWintryIcicles(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (wintryIciclesCooldown.containsKey(playerId)) {
            long timeLeft = (wintryIciclesCooldown.get(playerId) + WINTRY_ICICLES_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Wintry Icicles wieder einsetzen kannst!");
                return;
            }
        }

        wintryIciclesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lWintry Icicles§b werden geschleudert!");
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 0.8F);

        // Schieße mehrere Eiszapfen
        for (int i = 0; i < 5; i++) {
            // Leichte Streuung für jeden Eiszapfen
            Vector direction = player.getLocation().getDirection().normalize();
            direction.add(new Vector(random.nextDouble() * 0.2 - 0.1, random.nextDouble() * 0.1, random.nextDouble() * 0.2 - 0.1));

            // Verzögere jeden Eiszapfen leicht
            final int delay = i * 3;
            new BukkitRunnable() {
                @Override
                public void run() {
                    shootIcicle(player.getEyeLocation(), direction, player, plugin);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    /**
     * Hilfsmethode zum Schießen eines Eiszapfens
     */
    private void shootIcicle(Location startLoc, Vector direction, Player owner, Main plugin) {
        // Erstelle einen ArmorStand als visuelles Projektil
        ArmorStand icicle = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        icicle.setVisible(false);
        icicle.setGravity(false);
        icicle.setInvulnerable(true);
        icicle.setSmall(true);
        icicle.getEquipment().setHelmet(new ItemStack(Material.ICE));

        temporaryEntities.add(icicle);

        // Eispartikel-Effekt beim Start
        startLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, startLoc, 5, 0.1, 0.1, 0.1, 0.02);

        // Animiere den fliegenden Eiszapfen
        new BukkitRunnable() {
            private int ticks = 0;
            private boolean hasHit = false;

            @Override
            public void run() {
                ticks++;

                if (ticks > 40 || hasHit || !icicle.isValid()) {
                    icicle.remove();
                    this.cancel();
                    return;
                }

                // Bewege den Eiszapfen
                icicle.teleport(icicle.getLocation().add(direction));

                // Eispartikel-Effekt während der Bewegung
                icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, icicle.getLocation(), 1, 0.05, 0.05, 0.05, 0.01);

                // Prüfe auf Kollision mit dem Block
                Block block = icicle.getLocation().getBlock();
                if (block.getType().isSolid()) {
                    hasHit = true;

                    // Eisexplosions-Effekt
                    icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, icicle.getLocation(), 15, 0.3, 0.3, 0.3, 0.05);
                    icicle.getWorld().playSound(icicle.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8F, 1.2F);

                    return;
                }

                // Prüfe auf Kollision mit Entitäten
                for (Entity entity : icicle.getLocation().getWorld().getNearbyEntities(icicle.getLocation(), 0.8, 0.8, 0.8)) {
                    if (entity instanceof LivingEntity && entity != owner && entity != icicle) {
                        LivingEntity target = (LivingEntity) entity;

                        // Füge Schaden zu
                        target.damage(5.0, owner);

                        // Verlangsamungseffekt
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1)); // 2 Sekunden, Level 2

                        // Eisexplosions-Effekt
                        icicle.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                                20, 0.3, 0.8, 0.3, 0.1);
                        icicle.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.2F);

                        hasHit = true;
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Lotus Vines - Erschafft Eisranken, die Gegner festhalten und schädigen
     */
    public void activateLotusVines(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (lotusVinesCooldown.containsKey(playerId)) {
            long timeLeft = (lotusVinesCooldown.get(playerId) + LOTUS_VINES_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Lotus Vines wieder einsetzen kannst!");
                return;
            }
        }

        lotusVinesCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lLotus Vines§b breiten sich aus!");
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 0.6F);

        Location targetLoc = player.getTargetBlock(null, 15).getLocation();

        // Erstelle Eisranken (visuell mit Eisblöcken und Partikeln)
        createVines(targetLoc, player, plugin);
    }

    /**
     * Hilfsmethode zum Erstellen von Eisranken
     */
    private void createVines(Location center, Player owner, Main plugin) {
        // Bereich, in dem die Ranken erscheinen
        int radius = 4;
        int vineHeight = 2;

        // Speichere betroffene Gegner
        List<LivingEntity> affectedEntities = new ArrayList<>();

        // Finde Gegner im Bereich
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != owner) {
                affectedEntities.add((LivingEntity) entity);
            }
        }

        // Erstelle Ranken (zufällige Muster von Eisblöcken)
        List<Block> vineBlocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius && random.nextDouble() < 0.3) {
                    for (int y = 0; y < vineHeight; y++) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.BLUE_ICE);
                            vineBlocks.add(block);
                            temporaryIceBlocks.add(block);
                        }
                    }
                }
            }
        }

        // Eispartikel-Effekt
        for (Block block : vineBlocks) {
            block.getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5),
                    5, 0.3, 0.3, 0.3, 0.02);
        }

        // Wende Effekte auf Gegner an
        for (LivingEntity entity : affectedEntities) {
            // Starke Verlangsamung (fast Bewegungsunfähigkeit)
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4)); // 5 Sekunden, Level 5
            entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 128)); // Verhindert Springen

            // Schaden über Zeit
            new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    ticks++;

                    if (ticks > 5 || !entity.isValid()) {
                        this.cancel();
                        return;
                    }

                    // Füge alle Sekunde Schaden zu
                    entity.damage(2.0, owner);

                    // Eispartikel-Effekt
                    entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entity.getLocation().add(0, 1, 0),
                            10, 0.3, 0.8, 0.3, 0.05);
                }
            }.runTaskTimer(plugin, 20, 20); // Alle 20 Ticks (1 Sekunde)
        }

        // Entferne die Eisblöcke nach 5 Sekunden
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : vineBlocks) {
                    if (block.getType() == Material.BLUE_ICE) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5),
                                5, 0.2, 0.2, 0.2, 0.03);
                    }
                }
                vineBlocks.clear();
                temporaryIceBlocks.removeAll(vineBlocks);
            }
        }.runTaskLater(plugin, 100); // 100 Ticks = 5 Sekunden
    }

    /**
     * Crystalline Divine Child - Beschwört ein mächtiges Eiskind, das starken Schaden verursacht
     */
    public void activateCrystallineDivineChild(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (crystallineDivineChildCooldown.containsKey(playerId)) {
            long timeLeft = (crystallineDivineChildCooldown.get(playerId) + CRYSTALLINE_DIVINE_CHILD_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Crystalline Divine Child wieder einsetzen kannst!");
                return;
            }
        }

        crystallineDivineChildCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lCrystalline Divine Child§b wird beschworen!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5F, 1.5F);

        Location summonLoc = player.getLocation().add(0, 0.5, 0);

        // Erstelle das "Göttliche Kind" (visuell mit ArmorStand)
        ArmorStand divineChild = (ArmorStand) summonLoc.getWorld().spawnEntity(summonLoc, EntityType.ARMOR_STAND);
        divineChild.setVisible(false);
        divineChild.setGravity(false);
        divineChild.setInvulnerable(true);
        divineChild.setSmall(true); // Kleiner ArmorStand für "Kind"-Effekt
        divineChild.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_BLOCK));

        temporaryEntities.add(divineChild);

        // Erstelle einen beeindruckenden visuellen Effekt
        summonLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, summonLoc, 100, 1, 2, 1, 0.05);
        summonLoc.getWorld().spawnParticle(Particle.CLOUD, summonLoc, 50, 0.8, 1.5, 0.8, 0.02);
        summonLoc.getWorld().playSound(summonLoc, Sound.BLOCK_GLASS_BREAK, 1.0F, 0.5F);

        // Finde das nächste Ziel im Umkreis von 20 Blöcken
        LivingEntity target = null;
        double minDistance = Double.MAX_VALUE;

        for (Entity entity : summonLoc.getWorld().getNearbyEntities(summonLoc, 20, 20, 20)) {
            if (entity instanceof LivingEntity && entity != player &&
                    !(entity instanceof Player && ((Player) entity).getGameMode().toString().contains("CREATIVE"))) {

                double distance = entity.getLocation().distance(summonLoc);
                if (distance < minDistance) {
                    minDistance = distance;
                    target = (LivingEntity) entity;
                }
            }
        }

        if (target == null) {
            player.sendMessage("§cKein Ziel für Crystalline Divine Child gefunden!");
            divineChild.remove();
            return;
        }

        final LivingEntity finalTarget = target;
        player.sendMessage("§bCrystalline Divine Child greift " + finalTarget.getName() + " an!");

        // Animiere das "Göttliche Kind" und führe den Angriff aus
        new BukkitRunnable() {
            private int ticks = 0;
            private int phase = 0; // 0: Aufsteigen, 1: Zielen, 2: Angreifen
            private Location currentLoc = divineChild.getLocation().clone();
            private Vector attackDirection;

            @Override
            public void run() {
                ticks++;

                if (!divineChild.isValid() || !finalTarget.isValid()) {
                    divineChild.remove();
                    this.cancel();
                    return;
                }

                // Phase 0: Aufsteigen
                if (phase == 0) {
                    // Bewege nach oben
                    currentLoc.add(0, 0.1, 0);
                    ensureFiniteLocation(currentLoc);
                    divineChild.teleport(currentLoc);

                    // Effektpartikel
                    divineChild.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 5, 0.2, 0.2, 0.2, 0.02);

                    // Nach 20 Ticks (1 Sekunde) wechsle zur Zielphase
                    if (ticks >= 20) {
                        phase = 1;
                        ticks = 0;
                    }
                }
                // Phase 1: Zielen
                else if (phase == 1) {
                    // Drehe in Richtung des Ziels
                    Vector direction = finalTarget.getLocation().toVector().subtract(currentLoc.toVector()).normalize();
                    Location lookLoc = currentLoc.clone();
                    lookLoc.setDirection(direction);
                    ensureFiniteLocation(lookLoc);
                    divineChild.teleport(lookLoc);

                    // Intensivere Effektpartikel während des Zielens
                    divineChild.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 10, 0.3, 0.3, 0.3, 0.03);
                    divineChild.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 5, 0.2, 0.2, 0.2, 0.01);

                    // Nach 20 Ticks (1 Sekunde) wechsle zur Angriffsphase
                    if (ticks >= 20) {
                        phase = 2;
                        ticks = 0;
                        attackDirection = finalTarget.getLocation().toVector().subtract(currentLoc.toVector()).normalize();

                        // Angriffsgeräusch
                        divineChild.getWorld().playSound(divineChild.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 0.5F);
                    }
                }
                // Phase 2: Angreifen
                else if (phase == 2) {
                    // Bewege schnell zum Ziel
                    currentLoc.add(attackDirection.clone().multiply(0.8));
                    ensureFiniteLocation(currentLoc);
                    divineChild.teleport(currentLoc);

                    // Intensive Eispartikel während des Angriffs
                    divineChild.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 15, 0.2, 0.2, 0.2, 0.05);

                    // Prüfe auf Kollision mit dem Ziel
                    if (currentLoc.distance(finalTarget.getLocation()) < 1.5) {
                        // Starker Schaden
                        finalTarget.damage(15.0, player);

                        // Starke Effekte
                        finalTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3)); // 5 Sekunden, Level 4
                        finalTarget.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 2)); // 4 Sekunden, Level 3

                        // Spektakulärer Eisexplosions-Effekt
                        finalTarget.getWorld().spawnParticle(Particle.SNOWFLAKE, finalTarget.getLocation().add(0, 1, 0),
                                100, 1, 2, 1, 0.1);
                        finalTarget.getWorld().spawnParticle(Particle.EXPLOSION, finalTarget.getLocation().add(0, 1, 0),
                                1, 0, 0, 0, 0);
                        finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 0.5F);
                        finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 1.5F);

                        divineChild.remove();
                        this.cancel();
                    }

                    // Wenn nach 40 Ticks (2 Sekunden) kein Treffer erfolgt ist, beende
                    if (ticks >= 40) {
                        divineChild.remove();
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Rime – Water Lily Bodhisattva - Ultimative Fähigkeit, die das gesamte Gebiet mit Eis überzieht
     */
    public void activateRimeWaterLilyBodhisattva(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (rimeWaterLilyBodhisattvaCooldown.containsKey(playerId)) {
            long timeLeft = (rimeWaterLilyBodhisattvaCooldown.get(playerId) + RIME_WATER_LILY_BODHISATTVA_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Rime – Water Lily Bodhisattva wieder einsetzen kannst!");
                return;
            }
        }

        rimeWaterLilyBodhisattvaCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§l⚠ Rime – Water Lily Bodhisattva ⚠§b wird beschworen!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.5F);

        Location centerLoc = player.getLocation();

        // Großer Eispartikel-Effekt beim Start
        centerLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, centerLoc.clone().add(0, 1, 0),
                200, 3, 2, 3, 0.05);
        centerLoc.getWorld().spawnParticle(Particle.EXPLOSION, centerLoc, 3, 1, 0, 1, 0);

        // Frostkreis-Effekt, der sich ausbreitet
        new BukkitRunnable() {
            private int ticks = 0;
            private int radius = 1;
            private final int maxRadius = 15;

            @Override
            public void run() {
                ticks++;

                if (radius > maxRadius) {
                    this.cancel();
                    return;
                }

                // Alle 5 Ticks (0.25 Sekunden) erweitere den Radius
                if (ticks % 5 == 0) {
                    createFrostRing(centerLoc, radius, player, plugin);
                    radius++;
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Hilfsmethode zum Erstellen eines Frostrings für die ultimative Fähigkeit
     */
    private void createFrostRing(Location center, int radius, Player owner, Main plugin) {
        // Erstelle einen Ring aus Eisblöcken
        List<Block> ringBlocks = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Nur Blöcke am Rand des Kreises
                if (Math.abs(x*x + z*z - radius*radius) < radius) {
                    Block block = center.getWorld().getHighestBlockAt(center.getBlockX() + x, center.getBlockZ() + z).getLocation().add(0, 1, 0).getBlock();

                    if (block.getType() == Material.AIR || block.getType() == Material.SNOW ||
                            block.getType() == Material.GRASS_BLOCK || block.getType() == Material.TALL_GRASS) {

                        block.setType(Material.BLUE_ICE);
                        ringBlocks.add(block);
                        temporaryIceBlocks.add(block);

                        // Eispartikel-Effekt
                        block.getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5),
                                5, 0.3, 0.3, 0.3, 0.02);
                    }
                }
            }
        }

        // Finde Gegner im Ring und wende Effekte an
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, 5, radius)) {
            double distance = entity.getLocation().distance(center);

            // Nur Entitäten im Bereich des aktuellen Rings (mit einer kleinen Toleranz)
            if (Math.abs(distance - radius) <= 2 && entity instanceof LivingEntity && entity != owner) {
                LivingEntity target = (LivingEntity) entity;

                // Starker Schaden
                target.damage(7.0, owner);

                // Starke Verlangsamung und Schwäche
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 3)); // 8 Sekunden, Level 4
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 2)); // 6 Sekunden, Level 3

                // Eispartikel-Effekt
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                        30, 0.5, 1, 0.5, 0.1);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 0.8F);
            }
        }

        // Entferne die Eisblöcke nach 15 Sekunden
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : ringBlocks) {
                    if (block.getType() == Material.BLUE_ICE) {
                        block.setType(Material.AIR);
                        block.getWorld().spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5),
                                3, 0.2, 0.2, 0.2, 0.02);
                    }
                }
                ringBlocks.clear();
                temporaryIceBlocks.removeAll(ringBlocks);
            }
        }.runTaskLater(plugin, 300); // 300 Ticks = 15 Sekunden
    }

    /**
     * Ice Clone Breath Attack - Erzeugt einen kalten Hauch, der Gegner schwächt
     */
    public void activateIceCloneBreathAttack(Player player, Main plugin) {
        UUID playerId = player.getUniqueId();

        // Überprüfe Cooldown
        if (iceCloneBreathAttackCooldown.containsKey(playerId)) {
            long timeLeft = (iceCloneBreathAttackCooldown.get(playerId) + ICE_CLONE_BREATH_ATTACK_COOLDOWN - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cDu musst noch " + timeLeft + " Sekunden warten, bis du Ice Clone Breath Attack wieder einsetzen kannst!");
                return;
            }
        }

        iceCloneBreathAttackCooldown.put(playerId, System.currentTimeMillis());

        player.sendMessage("§b§lIce Clone Breath Attack§b wird ausgeführt!");
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.8F, 0.5F);

        // Richtung des Spielers
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        // Erstelle den Atemeffekt
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxDistance = 15; // Maximale Reichweite in Blöcken
            private double currentDistance = 0;

            @Override
            public void run() {
                ticks++;

                if (ticks > 100 || currentDistance >= maxDistance) { // 5 Sekunden Maximalzeit
                    this.cancel();
                    return;
                }

                // Bewege den Effekt vorwärts
                currentDistance += 0.5;
                Location currentLoc = startLoc.clone().add(direction.clone().multiply(currentDistance));

                // Eispartikel-Effekt
                currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 15, 0.5, 0.5, 0.5, 0.02);
                currentLoc.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 8, 0.4, 0.4, 0.4, 0.01);

                // Alle 5 Ticks (0.25 Sekunden) prüfe auf Gegner im Effektbereich
                if (ticks % 5 == 0) {
                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 2, 2, 2)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Füge leichten Schaden zu
                            target.damage(3.0, player);

                            // Verlangsamung und Schwäche
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 3 Sekunden, Level 2
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0)); // 2 Sekunden, Level 1

                            // Eispartikel-Effekt direkt am Ziel
                            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0),
                                    20, 0.3, 0.8, 0.3, 0.05);
                        }
                    }
                }

                // Prüfe auf Kollision mit Block
                Block block = currentLoc.getBlock();
                if (block.getType().isSolid()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Führe jeden Tick aus
    }

    /**
     * Entfernt alle temporären Entitäten und Blöcke, wenn das Plugin neugeladen wird
     */
    public void cleanup() {
        // Entferne temporäre ArmorStands und andere Entitäten
        for (ArmorStand entity : temporaryEntities) {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        temporaryEntities.clear();

        // Entferne temporäre Eisblöcke
        for (Block block : temporaryIceBlocks) {
            if (block != null && (block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE ||
                    block.getType() == Material.BLUE_ICE)) {
                block.setType(Material.AIR);
            }
        }
        temporaryIceBlocks.clear();
    }

    /**
     * Stellt sicher, dass die gegebene Location gültige (nicht unendliche) Werte für Welt, X, Y, Z, Pitch und Yaw hat.
     */
    private void ensureFiniteLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        // Welt muss gleich bleiben
        String worldName = location.getWorld().getName();

        // X, Y, Z müssen endlich sein
        double x = Double.isFinite(location.getX()) ? location.getX() : 0;
        double y = Double.isFinite(location.getY()) ? location.getY() : 0;
        double z = Double.isFinite(location.getZ()) ? location.getZ() : 0;

        // Pitch und Yaw müssen endlich sein
        float pitch = Float.isFinite(location.getPitch()) ? location.getPitch() : 0;
        float yaw = Float.isFinite(location.getYaw()) ? location.getYaw() : 0;

        location.setWorld(location.getWorld());
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        location.setPitch(pitch);
        location.setYaw(yaw);
    }
}