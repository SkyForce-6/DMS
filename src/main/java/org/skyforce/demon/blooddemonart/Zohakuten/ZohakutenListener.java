package org.skyforce.demon.blooddemonart.Zohakuten;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class ZohakutenListener implements Listener {

    private final ZohakutenAbility zohakutenAbility = new ZohakutenAbility();
    private final PlayerDataManager playerDataManager;

    public ZohakutenListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is Zohakuten demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.HANTENGU)) return;

        Action action = event.getAction();

        // Right-click abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.OAK_LOG) {
                // Wood Dragons
                zohakutenAbility.activateWoodDragons(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.STICK) {
                // Wood Dragon Shockwave
                zohakutenAbility.activateWoodDragonShockwave(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.GLOWSTONE_DUST) {
                // Lightning Wave
                zohakutenAbility.activateLightningWave(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.REDSTONE_TORCH) {
                // Toggle Rage Mode
                zohakutenAbility.toggleRageMode(player, Main.getPlugin(Main.class));
            }
            // Rage Mode Emotions
            else if (zohakutenAbility.isInRageMode(player.getUniqueId())) {
                if (item.getType() == Material.FEATHER) {
                    // Fear
                    zohakutenAbility.activateFear(player, Main.getPlugin(Main.class));
                } else if (item.getType() == Material.FLINT_AND_STEEL) {
                    // Anger
                    zohakutenAbility.activateAnger(player, Main.getPlugin(Main.class));
                } else if (item.getType() == Material.PAPER) {
                    // Pleasure (Uchiwa fan)
                    zohakutenAbility.activatePleasure(player, Main.getPlugin(Main.class));
                } else if (item.getType() == Material.IRON_INGOT) {
                    // Sorrow
                    zohakutenAbility.activateSorrow(player, Main.getPlugin(Main.class));
                } else if (item.getType() == Material.GOLD_INGOT) {
                    // Joy
                    zohakutenAbility.activateJoy(player, Main.getPlugin(Main.class));
                } else if (item.getType() == Material.COAL) {
                    // Hatred
                    zohakutenAbility.activateHatred(player, Main.getPlugin(Main.class));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Check if player is Zohakuten demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.HANTENGU)) return;

        // Handle Anger emotion lightning attacks
        zohakutenAbility.handleAngerAttack(player, target, event);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is Zohakuten demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.HANTENGU)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // Sneak + Item combinations for quick access to emotions
        if (zohakutenAbility.isInRageMode(player.getUniqueId())) {
            if (item.getType() == Material.FEATHER) {
                zohakutenAbility.activateFear(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.FLINT_AND_STEEL) {
                zohakutenAbility.activateAnger(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.PAPER) {
                zohakutenAbility.activatePleasure(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.IRON_INGOT) {
                zohakutenAbility.activateSorrow(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.GOLD_INGOT) {
                zohakutenAbility.activateJoy(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.COAL) {
                zohakutenAbility.activateHatred(player, Main.getPlugin(Main.class));
            }
        } else {
            // Quick rage mode activation
            if (item.getType() == Material.REDSTONE_TORCH) {
                zohakutenAbility.toggleRageMode(player, Main.getPlugin(Main.class));
            }
        }
    }
}