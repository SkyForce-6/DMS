package org.skyforce.demon;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.util.SunUtil;

public class DemonSunListener implements Listener {
    private final PlayerDataManager playerDataManager;

    public DemonSunListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.NORMAL) return;
        long newTime = event.getWorld().getTime() + event.getSkipAmount();
        
        if (event.getWorld().getTime() < 12000 && newTime >= 12000) {
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
                        if (profile.getPlayerClass() == PlayerClass.DEMON && player.getWorld().getEnvironment() == World.Environment.NORMAL && player.getWorld().getTime() < 12000) {
                            player.setFireTicks(200); 
                        }
                    }
                }
            }.runTaskLater(org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class), 20L); 
        }
    }

    public void startSunBurnTask() {
        Bukkit.getScheduler().runTaskTimer(org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
                if (profile.getPlayerClass() == PlayerClass.DEMON && SunUtil.isInSunlight(player)) {
                    player.setFireTicks(60); 
                }
            }
        }, 40L, 40L); 
    }
}
