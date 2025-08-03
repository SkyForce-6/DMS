package org.skyforce.demon.blooddemonart.Enmu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class EnmuListener implements Listener {

    private final EnmuAbility enmuAbility = new EnmuAbility();
    private final PlayerDataManager playerDataManager;

    public EnmuListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is a demon of type ENMU
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.ENMU)) return;

        Action action = event.getAction();

        // Right-click actions for different abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.PURPLE_DYE) {
                // Sleep - Powder AoE
                enmuAbility.activateSleepPowder(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.MAGENTA_DYE) {
                // Sleep: Core - Triggered version with extra stun
                enmuAbility.activateSleepCore(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.LEAD) {
                // Tentacles - 2 tentacles, 4 hits
                enmuAbility.activateTentacles(player, Main.getPlugin(Main.class));
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.TRIPWIRE_HOOK) {
                // Tentacles: Series - 4 tentacles, 8 hits
                enmuAbility.activateTentaclesSeries(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.MINECART) {
                // Train - Portal spawns train attack
                enmuAbility.activateTrain(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is a demon of type ENMU
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (!profile.getDemonType().equals(DemonType.ENMU)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.ENDER_EYE) {
            // Sleep Dimension - Ultimate ability
            enmuAbility.activateSleepDimension(player, Main.getPlugin(Main.class));
        }
    }
}