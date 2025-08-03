package org.skyforce.demon.blooddemonart.Douma;

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

public class DoumaListener implements Listener {

    private final DoumaAbility doumaAbility = new DoumaAbility();
    private final PlayerDataManager playerDataManager;

    public DoumaListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Überprüfe, ob der Spieler ein Dämon ist und Douma heißt
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DOMA)) return;

        Action action = event.getAction();

        // Rechtsklick mit verschiedenen Items für verschiedene Fähigkeiten
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.ICE) {
                // Frozen Lotus
                doumaAbility.activateFrozenLotus(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.SNOWBALL) {
                // Frozen Mist
                doumaAbility.activateFrozenMist(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.DIAMOND) {
                // Cold White Princesses
                doumaAbility.activateColdWhitePrincesses(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.STICK) {
                // Wintry Icicles
                doumaAbility.activateWintryIcicles(player, Main.getPlugin(Main.class));
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (item.getType() == Material.DIAMOND_SWORD) {
                // Barren Hanging Garden
                doumaAbility.activateBarrenHangingGarden(player, Main.getPlugin(Main.class));
            } else if (item.getType() == Material.BLAZE_ROD) {
                // Lotus Vines
                doumaAbility.activateLotusVines(player, Main.getPlugin(Main.class));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Überprüfe, ob der Spieler ein Dämon ist und Douma heißt
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DOMA)) return;

        // Freezing Clouds - passiver Effekt beim Angreifen
        doumaAbility.activateFreezingClouds(player, target, event);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Nur aktivieren, wenn der Spieler anfängt zu sneaken
        if (!event.isSneaking()) return;

        // Überprüfe, ob der Spieler ein Dämon ist und Douma heißt
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (profile.getDemonType() == null || !profile.getDemonType().equals(DemonType.DOMA)) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.GHAST_TEAR) {
            // Rime – Water Lily Bodhisattva (Ultimate)
            doumaAbility.activateRimeWaterLilyBodhisattva(player, Main.getPlugin(Main.class));
        } else if (item.getType() == Material.ENDER_PEARL) {
            // Ice Clone Breath Attack
            doumaAbility.activateIceCloneBreathAttack(player, Main.getPlugin(Main.class));
        } else {
            // Crystalline Divine Child
            doumaAbility.activateCrystallineDivineChild(player, Main.getPlugin(Main.class));
        }
    }
}