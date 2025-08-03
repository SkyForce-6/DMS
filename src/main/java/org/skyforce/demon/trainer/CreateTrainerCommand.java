package org.skyforce.demon.trainer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerProfile;

public class CreateTrainerCommand implements CommandExecutor {
    private final Main plugin;

    public CreateTrainerCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        PlayerProfile profile = null;
        if (plugin.getPlayerDataManager() != null) {
            profile = plugin.getPlayerDataManager().loadProfile(player.getUniqueId());
        }
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be OP to use this command.");
            return true;
        }
        Location loc = player.getLocation();
        org.bukkit.entity.Villager trainer = loc.getWorld().spawn(loc, org.bukkit.entity.Villager.class, villager -> {
            villager.setCustomName(ChatColor.AQUA + "Trainer");
            villager.setCustomNameVisible(true);
            villager.setInvulnerable(true);
            villager.setAI(false);
            villager.setCollidable(false);
            villager.setSilent(true);
            villager.setProfession(org.bukkit.entity.Villager.Profession.NONE);
            villager.setRemoveWhenFarAway(false);
            
            try {
                villager.setHealth(villager.getMaxHealth());
            } catch (NoSuchMethodError | NoSuchFieldError ignored) {
                
            }
        });
        
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
                if (event.getEntity().equals(trainer)) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
        player.sendMessage(ChatColor.GREEN + "Trainer (Villager) created at your location!");
        
        return true;
    }
}
