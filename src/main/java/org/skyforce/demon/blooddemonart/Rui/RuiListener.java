package org.skyforce.demon.blooddemonart.Rui;

import org.bukkit.Material;
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

public class RuiListener implements Listener {

    private final RuiAbility ruiAbility = new RuiAbility();
    private final PlayerDataManager playerDataManager;

    public RuiListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is a demon of type RUI
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.RUI)) return;

        Action action = event.getAction();

        // Right-click actions for different abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.STRING) {
                // Web Strings - Basic attack
                ruiAbility.activateWebStrings(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.COBWEB) {
                // Web Cage - Trap, deals damage if escaping
                ruiAbility.activateWebCage(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.ZOMBIE_HEAD) {
                // Summon: Father - Heavy slam damage
                ruiAbility.activateSummonFather(player, Main.getPlugin(Main.class));
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.SPIDER_EYE) {
                // Summon: Spiders - Multiple attackers, temporary
                ruiAbility.activateSummonSpiders(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.FISHING_ROD) {
                // Spider Web - Large area slash
                ruiAbility.activateSpiderWeb(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is a demon of type RUI
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (!profile.getDemonType().equals(DemonType.RUI)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.REDSTONE) {
            // Blood Webs - Stronger, red web variant
            ruiAbility.activateBloodWebs(player, Main.getPlugin(Main.class));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Check if player is a demon of type RUI
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (!profile.getDemonType().equals(DemonType.RUI)) return;

        // Apply additional web effects to normal attacks
        ruiAbility.applyWebEffectToAttack(player, event);
    }
}