package org.skyforce.demon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class ChatManager implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String prefix;

        PlayerProfile profile = Main.getPlugin(Main.class).getPlayerDataManager().loadProfile(player.getUniqueId());
        PlayerClass playerClass = profile.getPlayerClass();

        if (playerClass == PlayerClass.HUMAN) {
            prefix = ChatColor.GREEN + "⚔ Human " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
        } else if (playerClass == PlayerClass.DEMON) {
            DemonType demonType = profile.getDemonType();
            if (demonType == DemonType.KOKUSHIBO) {
                prefix = ChatColor.DARK_RED + "🌙 Upper Rank 1 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.DOMA) {
                prefix = ChatColor.DARK_PURPLE + "❄️ Upper Rank 2 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.AKAZA) {
                prefix = ChatColor.RED + "🔥 Upper Rank 3 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.NAKIME) {
                prefix = ChatColor.DARK_BLUE + "🎻 Upper Rank 4 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.HANTENGU) {
                prefix = ChatColor.DARK_GREEN + "💢 Former Upper Rank 4 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.GYOKKO) {
                prefix = ChatColor.BLUE + "🌊 Upper Rank 5 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.GYUTARO || demonType == DemonType.DAKI) {
                prefix = ChatColor.LIGHT_PURPLE + "💉 Upper Rank 6 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.ENMU) {
                prefix = ChatColor.DARK_AQUA + "🌑 Lower Rank 1 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.ROKURO) {
                prefix = ChatColor.DARK_AQUA + "💀 Lower Rank 2 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.WAKURABA) {
                prefix = ChatColor.DARK_AQUA + "🦊 Lower Rank 3 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.MUKAGO) {
                prefix = ChatColor.DARK_AQUA + "🐍 Lower Rank 4 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.RUI) {
                prefix = ChatColor.DARK_AQUA + "🕸 Lower Rank 5 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.KAMANUE) {
                prefix = ChatColor.DARK_AQUA + "🐀 Lower Rank 6 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else if (demonType == DemonType.KYOGAI) {
                prefix = ChatColor.GRAY + "🥁 Former Lower Rank 6 " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            } else {
                prefix = ChatColor.DARK_RED + "😈 Demon " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
            }
        } else {
            prefix = ChatColor.GRAY + "❓ Unknown " + ChatColor.DARK_GRAY + "»" + ChatColor.RESET;
        }

        event.setFormat(prefix + " " + ChatColor.WHITE + "%1$s: %2$s");
    }
}
