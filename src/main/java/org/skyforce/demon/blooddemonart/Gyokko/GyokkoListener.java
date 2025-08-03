package org.skyforce.demon.blooddemonart.Gyokko;

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

public class GyokkoListener implements Listener {

    private final GyokkoAbility gyokkoAbility = new GyokkoAbility();
    private final PlayerDataManager playerDataManager;

    public GyokkoListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is Gyokko demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYOKKO)) return;

        Action action = event.getAction();

        // Right-click abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.COD) {
                // Fish Summon
                gyokkoAbility.activateFishSummon(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.WATER_BUCKET) {
                // Water Cage
                gyokkoAbility.activateWaterCage(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.CACTUS) {
                // Poison Needles
                gyokkoAbility.activatePoisonNeedles(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.DECORATED_POT) {
                // Vase Transportation
                gyokkoAbility.activateVaseTransportation(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.TROPICAL_FISH) {
                // Fish Swarm
                gyokkoAbility.activateFishSwarm(player, Main.getPlugin(Main.class));
            }
        }
        // Left-click abilities
        else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.NETHER_STAR) {
                // Molting Transformation
                gyokkoAbility.activateMoltingTransformation(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Check if player is Gyokko demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYOKKO)) return;

        // Handle molting transformation enhanced attacks
        gyokkoAbility.handleMoltingAttack(player, target, event);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is Gyokko demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYOKKO)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // Sneak + Item combinations for quick access
        if (item.getType() == Material.NETHER_STAR) {
            // Quick Molting Transformation
            gyokkoAbility.activateMoltingTransformation(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.DECORATED_POT) {
            // Quick Vase Transportation
            gyokkoAbility.activateVaseTransportation(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.COD) {
            // Quick Fish Summon
            gyokkoAbility.activateFishSummon(player, Main.getPlugin(Main.class));
        }
    }
}