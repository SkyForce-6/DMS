//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.skyforce.demon.breathings.waterbreathing;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaterBreathingListener implements Listener {
    private final Map<Player, Integer> techniqueSlot = new HashMap<>();
    private final String[] techniqueNames = {
        "Water Surface Slash",
        "Water Wheel ",
        "Flowing Dance",
        "Striking Tide",
        "Blessed Rain After the Drought",
        "Whirlpool",
        "Drop Ripple Thrust",
        "Waterfall Basin",
        "Splashing Water Flow, Turbulent",
        "Constant Flux",
        "Eleventh Form"
    };
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_MS = 1200;
    private final PlayerDataManager playerDataManager;

    public WaterBreathingListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerAttack(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile == null || !profile.knowsTechnique(TechniqueType.WATER_BREATHING)) {
            return;
        }
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = techniqueSlot.getOrDefault(player, 0);
            slot = (slot + 1) % techniqueNames.length;
            techniqueSlot.put(player, slot);
            player.sendMessage("§bWater Breathing Technik gewechselt: §e" + techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }
        if (!event.getAction().toString().contains("RIGHT")) return;
        int slot = techniqueSlot.getOrDefault(player, 0);
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (!player.isSneaking()) {
            // Cooldown prüfen
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
                player.sendMessage("§7Water Breathing ist noch auf Cooldown!");
                event.setCancelled(true);
                return;
            }
            cooldowns.put(uuid, now);
        }
        if (!player.isSneaking()) {
            // Technik ausführen
            switch (slot) {
                case 0 -> {
                    WaterBreathingAbility.playWaterSurfaceSlash(player);
                    player.sendMessage("§eWater Breathing: Water Surface Slash!");
                }
                case 1 -> {
                    LivingEntity target = null;
                    double range = 5.0;
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            target = (LivingEntity) entity;
                            break;
                        }
                    }
                    if (target != null) {
                    WaterBreathingAbility.playWaterWheel(player, Main.getPlugin(Main.class), target);
                    player.sendMessage("§eWater Breathing: Water Wheel!");
                    } else {
                        player.sendMessage("§cKein Ziel in Reichweite für Water Wheel!");
                    }
                }
                case 2 -> {
                    LivingEntity target = null;
                    double range = 5.0;
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            target = (LivingEntity) entity;
                            break;
                        }
                    }
                    if (target != null) {
                        WaterBreathingAbility.playFlowingDance(player, target);
                        player.sendMessage("§eWater Breathing: Flowing Dance!");
                    } else {
                        player.sendMessage("§cKein Ziel in Reichweite für Flowing Dance!");
                    }
                }
                case 3 -> {
                    LivingEntity target = null;
                    double range = 5.0;
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            target = (LivingEntity) entity;
                            break;
                        }
                    }
                    if (target != null) {
                        WaterBreathingAbility.playStrikingTide(player, target);
                        player.sendMessage("§eWater Breathing: Striking Tide!");
                    }else {
                        player.sendMessage("§cKein Ziel in Reichweite für Striking Tide!");
                    }
                }
                case 4 -> {
                    LivingEntity target = null;
                    double range = 5.0;
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            target = (LivingEntity) entity;
                            break;
                        }
                    }
                    if (target != null) {
                        WaterBreathingAbility.playBlessedRainAfterDrought(player, target);
                        player.sendMessage("§eWater Breathing: Blessed Rain After the Drought!");
                    }else {
                        player.sendMessage("§cKein Ziel in Reichweite für Blessed Rain After the Drought!");
                    }
                }
                case 5 -> {
                    WaterBreathingAbility.playWhirlpool(player);
                    player.sendMessage("§eWater Breathing: Whirlpool!");
                }
                case 6 -> {
                    LivingEntity target = null;
                    double range = 10.0;
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            target = (LivingEntity) entity;
                            break;
                        }
                    }
                    if (target != null) {
                        WaterBreathingAbility.playDropRippleThrust(player, target);
                        player.sendMessage("§eWater Breathing: Drop Ripple Thrust!");
                    }else{
                            player.sendMessage("§cKein Ziel in Reichweite für Drop Ripple Thrust!");
                        }
                    }
                case 7 -> {
                    WaterBreathingAbility.playWaterfallBasin(player);
                    player.sendMessage("§eWater Breathing: Waterfall Basin!");
                }
                case 8 -> {
                    WaterBreathingAbility.useNinthForm(player);
                    player.sendMessage("§eWater Breathing: Splashing Water Flow, Turbulent!");
                }
                case 9 -> {
                    WaterBreathingAbility.useTenthForm(player);
                    player.sendMessage("§eWater Breathing: Constant Flux!");
                }
                case 10 -> {
                    WaterBreathingAbility.useEleventhForm(player);
                    player.sendMessage("§eWater Breathing: Eleventh Form!");
                }
            }
        }
        event.setCancelled(true);
    }
}
