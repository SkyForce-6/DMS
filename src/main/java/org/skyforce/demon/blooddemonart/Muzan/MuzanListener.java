package org.skyforce.demon.blooddemonart.Muzan;

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

public class MuzanListener implements Listener {

    private final MuzanAbility muzanAbility = new MuzanAbility();
    private final PlayerDataManager playerDataManager;

    public MuzanListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is a demon of type MUZAN
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.MUZAN)) return;

        Action action = event.getAction();

        // Right-click actions for different abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.NETHER_STAR) {
                // Demon King – Shockwave ability
                muzanAbility.activateDemonKing(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.END_PORTAL_FRAME) {
                // Infinity Castle – Opens portals
                muzanAbility.activateInfinityCastle(player, Main.getPlugin(Main.class));
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.NETHERITE_SWORD) {
                // Demon King's Wrath – AoE red tentacle strike
                muzanAbility.activateDemonKingsWrath(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is a demon of type MUZAN
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.MUZAN)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.WITHER_SKELETON_SKULL) {
            // Upper Moons – Summons all Upper Moons as mobs
            muzanAbility.activateUpperMoons(player, Main.getPlugin(Main.class));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Check if player is a demon of type MUZAN
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.MUZAN)) return;

        // Apply poison effect to all attacks and check for demon one-shot
        muzanAbility.applyMuzanAttackEffects(player, event);
    }
}