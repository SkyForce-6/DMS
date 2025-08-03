package org.skyforce.demon.breathings.flamebreathing;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;

public class FlameBreathingListener implements Listener {
    
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    
    private final Map<Player, Long> activeUntil = new HashMap<>();
    
    private static final long COMBO_TIMEOUT = 2000;
    
    private static final long ACTIVE_DURATION = 10000;
    
    private static final long COOLDOWN = 15000;
    
    private final Map<Player, Long> lastComboTime = new HashMap<>();
    
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final FlameBreathingAbility flameBreathingAbility;
    private final FlameBreathingCombo flameBreathingCombo;

    public FlameBreathingListener() {
        this.playerDataManager = org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class).getPlayerDataManager();
        Plugin plugin = org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class);
        this.flameBreathingAbility = new FlameBreathingAbility(activeUntil, plugin);
        this.flameBreathingCombo = new FlameBreathingCombo(COMBO_TIMEOUT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.FLAME_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel nur mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 6;
            comboPhase.put(player, slot);
            String[] techniqueNames = {"Unknowing Fire", "Rising Scorching Sun", "Blazing Universe", "Blooming Flame Undulation", "Flame Tiger", "Rengoku"};
            player.sendMessage(techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen nur mit Rechtsklick
        if (!(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
        int slot = comboPhase.getOrDefault(player, 0);
        long now = System.currentTimeMillis();
        switch (slot) {
            case 0:
                // Unknowing Fire
                if (comboCooldownUntil.containsKey(player) && comboCooldownUntil.get(player) > now) {
                    long sekunden = (comboCooldownUntil.get(player) - now) / 1000 + 1;
                 //   player.sendMessage("§cUnknowing Fire ist noch " + sekunden + "s im Cooldown!");
                    return;
                }
                Entity target = null;
                if (player.getWorld() != null) {
                    var result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0, e -> e instanceof org.bukkit.entity.LivingEntity && e != player);
                    if (result != null) {
                        target = result.getHitEntity();
                    }
                }
                if (target == null) {
                    double minDist = 2.0;
                    for (Entity e : player.getNearbyEntities(2.0, 2.0, 2.0)) {
                        if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                            double angle = player.getLocation().getDirection().normalize().dot(e.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize());
                            double dist = e.getLocation().distance(player.getEyeLocation());
                            if (angle > 0.85 && dist < minDist) {
                                target = e;
                                minDist = dist;
                            }
                        }
                    }
                }
                if (target != null) {
                    flameBreathingAbility.unknowingFire(player, (org.bukkit.entity.LivingEntity) target, item);
                    player.sendMessage("§6First Form: Unknowing Fire triggert!");
                    comboCooldownUntil.put(player, now + 15000);
                } else {
                    player.sendMessage("§cNo target in line of sight and range!");
                }
                event.setCancelled(true);
                return;
            case 1:
                // Rising Scorching Sun
                Entity targetRising = null;
                if (player.getWorld() != null) {
                    var result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0, e -> e instanceof org.bukkit.entity.LivingEntity && e != player);
                    if (result != null) {
                        targetRising = result.getHitEntity();
                    }
                }
                if (targetRising == null) {
                    double minDist = 2.0;
                    for (Entity e : player.getNearbyEntities(2.0, 2.0, 2.0)) {
                        if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                            double angle = player.getLocation().getDirection().normalize().dot(e.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize());
                            double dist = e.getLocation().distance(player.getEyeLocation());
                            if (angle > 0.85 && dist < minDist) {
                                targetRising = e;
                                minDist = dist;
                            }
                        }
                    }
                }
                if (targetRising != null) {
                    flameBreathingAbility.risingScorchingSun(player, (org.bukkit.entity.LivingEntity) targetRising, item);
                    player.sendMessage("§6Second Form: Rising Scorching Sun triggered!");
                } else {
                    player.sendMessage("§cNo target in line of sight and range!");
                }
                event.setCancelled(true);
                return;
            case 2:
                // Blazing Universe
                Entity target2 = null;
                if (player.getWorld() != null) {
                    var result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), 5.0, e -> e instanceof org.bukkit.entity.LivingEntity && e != player);
                    if (result != null) {
                        target2 = result.getHitEntity();
                    }
                }
                if (target2 == null) {
                    double minDist = 2.0;
                    for (Entity e : player.getNearbyEntities(2.0, 2.0, 2.0)) {
                        if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                            double angle = player.getLocation().getDirection().normalize().dot(e.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize());
                            double dist = e.getLocation().distance(player.getEyeLocation());
                            if (angle > 0.85 && dist < minDist) {
                                target2 = e;
                                minDist = dist;
                            }
                        }
                    }
                }
                if (target2 != null) {
                    flameBreathingAbility.blazingUniverse(player, (org.bukkit.entity.LivingEntity) target2, item);
                    player.sendMessage("§6Third Form: Blazing Universe triggered!");
                } else {
                    player.sendMessage("§cNo target in line of sight and range!");
                }
                event.setCancelled(true);
                return;
            case 3:
                // Blooming Flame Undulation
                flameBreathingAbility.bloomingFlameUndulation(player);
                event.setCancelled(true);
                return;
            case 4:
                // Flame Tiger
                if (comboCooldownUntil.containsKey(player) && comboCooldownUntil.get(player) > now) {
                    long sekunden = (comboCooldownUntil.get(player) - now) / 1000 + 1;
                    player.sendMessage("§cFlame Tiger ist noch " + sekunden + "s im Cooldown!");
                    return;
                }
                flameBreathingAbility.flameTiger(player, null, item);
                comboCooldownUntil.put(player, now + 12); // 12s Cooldown
                break;
            case 5:
                // Rengoku
                if (comboCooldownUntil.containsKey(player) && comboCooldownUntil.get(player) > now) {
                    long sekunden = (comboCooldownUntil.get(player) - now) / 1000 + 1;
                    player.sendMessage("§cRengoku ist noch " + sekunden + "s im Cooldown!");
                    return;
                }
                flameBreathingAbility.useNinthForm(player);
                comboCooldownUntil.put(player, now + 15); // 15s Cooldown
                break;
            default:
                return;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.FLAME_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        Entity target = event.getEntity();
        flameBreathingAbility.applyFlameEffect(player, target, item);
    }
}
