package org.skyforce.demon;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class EventManager {
    private boolean eventActive = false;
    private Location signLocation;
    private final Set<Player> participants = new HashSet<>();
    private Location teleportLocation;

    public void startEvent(Location signLocation, Location teleportLocation) {
        this.eventActive = true;
        this.signLocation = signLocation;
        this.teleportLocation = teleportLocation;
        participants.clear();
    }

    public void stopEvent() {
        this.eventActive = false;
        this.signLocation = null;
        this.teleportLocation = null;
        participants.clear();
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public boolean isSign(Block block) {
        if (signLocation == null) return false;
        return block.getLocation().equals(signLocation);
    }

    public void addParticipant(Player player) {
        participants.add(player);
    }

    public Set<Player> getParticipants() {
        return participants;
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }
}
