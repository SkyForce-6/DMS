package org.skyforce.demon.trainer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class RemoveTrainerCommand implements CommandExecutor {
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
        double radius = 5.0;
        Villager nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Villager villager) {
                if ((villager.getCustomName() != null) && villager.getCustomName().equals(ChatColor.AQUA + "Trainer")) {
                    double dist = player.getLocation().distanceSquared(villager.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = villager;
                    }
                }
            }
        }
        if (nearest != null) {
            nearest.remove();
            player.sendMessage(ChatColor.GREEN + "Trainer removed!");
        } else {
            player.sendMessage(ChatColor.RED + "No trainer found nearby!");
        }
        return true;
    }
}
