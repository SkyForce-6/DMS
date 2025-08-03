package org.skyforce.demon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventWoolListener implements Listener {
    private final EventManager eventManager;

    public EventWoolListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
          //  player.sendMessage(ChatColor.YELLOW + "DEBUG: No block clicked!");
            return;
        }
        if (block.getType() != Material.LIME_WOOL) {
            
            return;
        }
        event.setCancelled(true); 
        if (eventManager.getParticipants().contains(player)) {
            player.sendMessage(ChatColor.YELLOW + "You are already registered for the event. Please wait until it starts!");
            return;
        }
        eventManager.addParticipant(player);
        player.sendMessage(ChatColor.GREEN + "You are now participating in the event! Please wait until it starts.");
    }
}
