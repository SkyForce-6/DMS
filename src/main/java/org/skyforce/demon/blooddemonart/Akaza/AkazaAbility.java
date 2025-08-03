package org.skyforce.demon.blooddemonart.Akaza;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skyforce.demon.Main;

import java.util.*;

public class AkazaAbility {
    private final Map<UUID, Long> compassNeedleCooldown = new HashMap<>();
    private final Map<UUID, Long> airTypeCooldown = new HashMap<>();
    private final Map<UUID, Long> disorderCooldown = new HashMap<>();
    private final Map<UUID, Long> annihilationTypeCooldown = new HashMap<>();
    private final Map<UUID, Long> crushingTypeCooldown = new HashMap<>();
    private final Map<UUID, Long> legTypeCooldown = new HashMap<>();
    private final Map<UUID, Long> shatterCooldown = new HashMap<>();
    private final Map<UUID, Long> punishCooldown = new HashMap<>();
    private final Map<UUID, Long> cloneCooldown = new HashMap<>();

    private final Random random = new Random();

    // Setze sinnvolle Cooldownzeiten in ms!
    private static final long COMPASS_NEEDLE_COOLDOWN = 15_000;
    private static final long AIR_TYPE_COOLDOWN = 10_000;
    private static final long DISORDER_COOLDOWN = 12_000;
    private static final long ANNIHILATION_TYPE_COOLDOWN = 30_000;
    private static final long CRUSHING_TYPE_COOLDOWN = 20_000;
    private static final long LEG_TYPE_COOLDOWN = 18_000;
    private static final long SHATTER_COOLDOWN = 25_000;
    private static final long PUNISH_COOLDOWN = 20_000;
    private static final long CLONE_COOLDOWN = 40_000;

    // 1. Compass Needle (Stance Buff)
    public void activateCompassNeedle(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(compassNeedleCooldown, id, COMPASS_NEEDLE_COOLDOWN, player, "Compass Needle")) return;
        compassNeedleCooldown.put(id, System.currentTimeMillis());
        player.sendMessage("§d§lCompass Needle aktiviert! §7Deine Sinne sind geschärft!");
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1.2f);
    }

    // 2. Air Type – Long-range shockwave
    public void activateAirType(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(airTypeCooldown, id, AIR_TYPE_COOLDOWN, player, "Air Type")) return;
        airTypeCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§b§lAir Type! §7Du feuerst eine Schockwelle ab!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1.5f);

        Vector dir = player.getLocation().getDirection().normalize();
        Location loc = player.getEyeLocation();

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 10) { cancel(); return; }
                loc.add(dir);
                loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 2, 0.3, 0.1, 0.3, 0.05);
                for (Entity e : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                    if (e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(6, player);
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
                        cancel();
                    }
                }
                if (loc.getBlock().getType().isSolid()) cancel();
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    // 3. Disorder – Multi-shockwave punches
    public void activateDisorder(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(disorderCooldown, id, DISORDER_COOLDOWN, player, "Disorder")) return;
        disorderCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§e§lDisorder! §7Schnelle Schlagserie!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.4f);

        new BukkitRunnable() {
            int hits = 0;
            @Override
            public void run() {
                if (hits++ >= 5) { cancel(); return; }
                for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 3, 2, 3)) {
                    if (e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(4, player);
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
                    }
                }
                player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 8, 1, 0.2, 1, 0.1);
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    // 4. Annihilation Type – Strong punch
    public void activateAnnihilationType(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(annihilationTypeCooldown, id, ANNIHILATION_TYPE_COOLDOWN, player, "Annihilation Type")) return;
        annihilationTypeCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§c§lAnnihilation Type! §7Vernichtender Schlag!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 0.7f);

        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 4, 2, 4)) {
            if (e instanceof LivingEntity && e != player) {
                ((LivingEntity) e).damage(12, player);
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                e.getWorld().spawnParticle(Particle.EXPLOSION, e.getLocation(), 1);
            }
        }
    }

    // 5. Crushing Type – Ten-Thousand Leaves
    public void activateCrushingType(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(crushingTypeCooldown, id, CRUSHING_TYPE_COOLDOWN, player, "Crushing Type")) return;
        crushingTypeCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§a§lCrushing Type! §7Tausend Schläge!");
        for (int i = 0; i < 3; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 3, 2, 3)) {
                        if (e instanceof LivingEntity && e != player) {
                            ((LivingEntity) e).damage(5, player);
                            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 1));
                        }
                    }
                }
            }.runTaskLater(plugin, i * 15);
        }
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 16, 1, 0.5, 1, 0.2);
    }

    // 6. Leg Type – Flying Planet Thousand Wheels (Sprung-Angriff)
    public void activateLegType(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(legTypeCooldown, id, LEG_TYPE_COOLDOWN, player, "Leg Type")) return;
        legTypeCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§6§lLeg Type! §7Du springst voran und trittst alles weg!");
        Vector jump = player.getLocation().getDirection().normalize().multiply(2).setY(1);
        player.setVelocity(jump);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1, 1.1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2)) {
                    if (e instanceof LivingEntity && e != player) {
                        ((LivingEntity) e).damage(7, player);
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                    }
                }
            }
        }.runTaskLater(plugin, 15);
    }

    // 7. Shatter – Defense-breaker
    public void activateShatter(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(shatterCooldown, id, SHATTER_COOLDOWN, player, "Shatter")) return;
        shatterCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§b§lShatter! §7Zerstört die Verteidigung deines Gegners!");
        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 3, 2, 3)) {
            if (e instanceof LivingEntity && e != player) {
                ((LivingEntity) e).damage(6, player);
                ((LivingEntity) e).removePotionEffect(PotionEffectType.RESISTANCE);
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 2));
            }
        }
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 20, 1, 0.5, 1, Material.REDSTONE_BLOCK.createBlockData());
    }

    // 8. Punish – Against weak opponents
    public void activatePunish(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(punishCooldown, id, PUNISH_COOLDOWN, player, "Punish")) return;
        punishCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§c§lPunish! §7Bestrafe Schwache!");
        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 3, 2, 3)) {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity le = (LivingEntity) e;
                if (le.getHealth() <= le.getMaxHealth() / 3) {
                    le.damage(10, player);
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                }
            }
        }
    }

    // 9. Clone with shockwave fists
    public void activateClone(Player player, Main plugin) {
        UUID id = player.getUniqueId();
        if (onCooldown(cloneCooldown, id, CLONE_COOLDOWN, player, "Shockwave Clone")) return;
        cloneCooldown.put(id, System.currentTimeMillis());

        player.sendMessage("§5§lShockwave Clone! §7Ein Klon schlägt mit Schockwellen um sich!");

        Location loc = player.getLocation().add(1, 0, 1);
        ArmorStand clone = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        clone.setVisible(false);
        clone.setGravity(false);
        clone.setInvulnerable(true);
        clone.getEquipment().setHelmet(new ItemStack(Material.PINK_WOOL));

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!clone.isValid() || ticks++ > 40) {
                    clone.remove();
                    cancel();
                }
                clone.getWorld().spawnParticle(Particle.SWEEP_ATTACK, clone.getLocation(), 5, 0.3, 0.2, 0.3, 0.05);
                for (Entity e : clone.getWorld().getNearbyEntities(clone.getLocation(), 2, 1, 2)) {
                    if (e instanceof LivingEntity && e != player && e != clone) {
                        ((LivingEntity) e).damage(5, player);
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    // Hilfsmethode Cooldown
    private boolean onCooldown(Map<UUID, Long> map, UUID id, long cooldown, Player player, String name) {
        if (map.containsKey(id)) {
            long timeLeft = (map.get(id) + cooldown - System.currentTimeMillis());
            if (timeLeft > 0) {
                player.sendMessage("§c" + name + " ist noch " + (timeLeft / 1000) + " Sekunden auf Cooldown.");
                return true;
            }
        }
        return false;
    }
}