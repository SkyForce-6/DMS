package org.skyforce.demon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MeditateCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Long> meditating = new HashMap<>();

    public MeditateCommand(PlayerDataManager manager) {
        this.playerDataManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can meditate.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (meditating.containsKey(uuid)) {
            long start = meditating.remove(uuid);
            long seconds = (System.currentTimeMillis() - start) / 1000L;
            PlayerProfile profile = playerDataManager.loadProfile(uuid);
            profile.addMeditationTime(seconds);
            playerDataManager.saveProfile(profile);
            player.sendMessage("§aMeditation beendet. Level: " + profile.getMeditationLevel());
        } else {
            meditating.put(uuid, System.currentTimeMillis());
            player.sendMessage("§aMeditation gestartet. Nutze den Befehl erneut, um sie zu beenden.");
        }
        return true;
    }
}
