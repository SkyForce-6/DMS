package org.skyforce.demon.blooddemonart.Gyutaro;

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

public class GyutaroListener implements Listener {

    private final GyutaroAbility gyutaroAbility = new GyutaroAbility();
    private final PlayerDataManager playerDataManager;

    public GyutaroListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is Gyutaro demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYUTARO)) return;

        Action action = event.getAction();

        // Right-click abilities
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.IRON_HOE) {
                // Blood Sickles (Basic Combo)
                gyutaroAbility.activateBloodSickles(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.DIAMOND_HOE) {
                // Flying Blood Sickles
                gyutaroAbility.activateFlyingBloodSickles(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.NETHERITE_HOE) {
                // Vengeful Slices
                gyutaroAbility.activateVengefulSlices(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.PINK_CONCRETE) {
                // Summon: Ume
                gyutaroAbility.activateSummonUme(player, Main.getPlugin(Main.class));
            }
        }
        // Left-click abilities
        else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.REDSTONE_TORCH) {
                // Blood Tornado
                gyutaroAbility.activateBloodTornado(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Check if player is Gyutaro demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYUTARO)) return;

        // All Gyutaro attacks inflict poison
        gyutaroAbility.handleGyutaroAttack(player, target, event);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Only activate when player starts sneaking
        if (!event.isSneaking()) return;

        // Check if player is Gyutaro demon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.GYUTARO)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // Sneak + Item combinations for quick access
        if (item.getType() == Material.IRON_HOE) {
            // Quick Blood Sickles combo
            gyutaroAbility.activateBloodSickles(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.REDSTONE_TORCH) {
            // Quick Blood Tornado
            gyutaroAbility.activateBloodTornado(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.PINK_CONCRETE) {
            // Quick Summon: Ume
            gyutaroAbility.activateSummonUme(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.NETHERITE_HOE) {
            // Quick Vengeful Slices
            gyutaroAbility.activateVengefulSlices(player, Main.getPlugin(Main.class));
        }
    }
}