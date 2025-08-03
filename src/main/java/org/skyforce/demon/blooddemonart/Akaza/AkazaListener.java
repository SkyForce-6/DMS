package org.skyforce.demon.blooddemonart.Akaza;

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
import org.skyforce.demon.player.*;

public class AkazaListener implements Listener {
    private final AkazaAbility akazaAbility = new AkazaAbility();
    private final PlayerDataManager playerDataManager;

    public AkazaListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Nur Akaza-Dämon
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.AKAZA)) return;

        Action action = event.getAction();

        // Rechtsklick: Verschiedene Attacken auf Items
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.COMPASS) {
                akazaAbility.activateCompassNeedle(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.FEATHER) {
                akazaAbility.activateAirType(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.BLAZE_POWDER) {
                akazaAbility.activateDisorder(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.NETHERITE_SCRAP) {
                akazaAbility.activateAnnihilationType(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.BIG_DRIPLEAF) {
                akazaAbility.activateCrushingType(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.RABBIT_FOOT) {
                akazaAbility.activateLegType(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.SHIELD) {
                akazaAbility.activateShatter(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.BONE) {
                akazaAbility.activatePunish(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.ARMOR_STAND) {
                akazaAbility.activateClone(player, Main.getPlugin(Main.class));
            }
        }
    }

    // Optional: Passiv-Effekt beim Angriff (z.B. Chance auf extra Damage/Knockback)
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Hier könnte man Effekte wie Crit-Chance, Knockback etc. für Akaza ergänzen
    }

    // Sneaken für Spezialmanöver
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Nur beim Starten des Sneakens
        if (!event.isSneaking()) return;

        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.AKAZA)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.ARMOR_STAND) {
            akazaAbility.activateClone(player, Main.getPlugin(Main.class));
        }
    }
}