package org.skyforce.demon;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.skyforce.demon.player.PlayerDataListener;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;

public class IntroListener implements Listener {
    private final PlayerDataManager playerDataManager;

    public IntroListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());

        if (profile != null && profile.getPlayerClass() == null) {
            player.sendMessage("intro");
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 10, false, false, false));
            player.sendTitle("§aHey", "§fYou finally woke up!", 10, 40, 10);
            player.playSound(player.getLocation(), Sound.UI_LOOM_TAKE_RESULT, 1.0f, 1.0f);
            player.sendMessage("§7You finally woke up! Choose your class...");

            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerDataListener.openChooseGuiStatic(player, playerDataManager);
                }
            }.runTaskLater(Main.getPlugin(Main.class), 40L);
        }
    }
}