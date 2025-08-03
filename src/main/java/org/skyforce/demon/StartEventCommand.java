package org.skyforce.demon;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartEventCommand implements CommandExecutor {
    private final EventManager eventManager;

    public StartEventCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be OP to use this command.");
            return true;
        }
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.LIME_WOOL) {
            player.sendMessage(ChatColor.RED + "You must look at a lime wool block to start the event!");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            if (eventManager.getParticipants().isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No players have registered for the event yet.");
            } else {
                player.sendMessage(ChatColor.AQUA + "Event participants:");
                for (Player p : eventManager.getParticipants()) {
                    player.sendMessage(ChatColor.GRAY + "- " + p.getName());
                }
            }
            return true;
        }
        Location woolLocation = targetBlock.getLocation();
        Location teleportLocation = player.getLocation();
        eventManager.startEvent(woolLocation, teleportLocation);
     //   player.sendMessage(ChatColor.GREEN + "DEBUG: Event started at lime wool block " + woolLocation + ", teleport location: " + teleportLocation);
        for (Player participant : eventManager.getParticipants()) {
            participant.teleport(teleportLocation);
            participant.sendMessage(ChatColor.GREEN + "You have been teleported to the event!");
        }
        player.sendMessage(ChatColor.GREEN + "Event started! All players who clicked the lime wool block have been teleported.");
        return true;
    }
}
