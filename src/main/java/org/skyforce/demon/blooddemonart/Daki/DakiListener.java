package org.skyforce.demon.blooddemonart.Daki;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class DakiListener implements Listener {

    private final DakiAbility dakiAbility = new DakiAbility();
    private final PlayerDataManager playerDataManager;

    public DakiListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is Daki demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DAKI)) return;

        Action action = event.getAction();

        // Right-click abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.MAGENTA_WOOL) {
                // Obi Cage
                dakiAbility.activateObiCage(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.MAGENTA_BANNER) {
                // Obi Attack
                dakiAbility.activateObiAttack(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.SHIELD) {
                // Obi Shield
                dakiAbility.activateObiShield(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.MAGENTA_CONCRETE) {
                // Obi Grid
                dakiAbility.activateObiGrid(player, Main.getPlugin(Main.class));
            }
        }
        // Left-click abilities
        else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.MAGENTA_TERRACOTTA) {
                // Obi Punch
                dakiAbility.activateObiPunch(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if player is Daki demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DAKI)) return;

        // Handle Obi Shield damage absorption
        dakiAbility.handleObiShieldDamage(player, event);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is Daki demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DAKI)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // Sneak + Item combinations for quick access
        if (item.getType() == Material.SHIELD) {
            // Quick Obi Shield
            dakiAbility.activateObiShield(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.MAGENTA_TERRACOTTA) {
            // Quick Obi Punch
            dakiAbility.activateObiPunch(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.MAGENTA_WOOL) {
            // Quick Obi Cage
            dakiAbility.activateObiCage(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.MAGENTA_CONCRETE) {
            // Quick Obi Grid
            dakiAbility.activateObiGrid(player, Main.getPlugin(Main.class));
        }
    }
}