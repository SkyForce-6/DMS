package org.skyforce.demon.blooddemonart.Nezuko;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class NezukoListener implements Listener {

    private final NezukoAbility nezukoAbility = new NezukoAbility();
    private final PlayerDataManager playerDataManager;

    public NezukoListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }


    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity) entity : null;

        // Überprüfe, ob der Spieler ein Dämon ist und Nezuko heißt
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (!profile.isNezuko()) return;
        assert livingEntity != null;
        nezukoAbility.activateHealingFlames(player, livingEntity, Main.getPlugin(Main.class));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Überprüfe, ob der Spieler ein Dämon ist und Nezuko heißt
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() != PlayerClass.DEMON) return;
        if (!profile.isNezuko()) return;
        nezukoAbility.activateExplodingBlood(player, target, event);
    }
}