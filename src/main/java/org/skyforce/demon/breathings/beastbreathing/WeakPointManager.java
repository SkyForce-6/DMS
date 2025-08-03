package org.skyforce.demon.breathings.beastbreathing;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WeakPointManager {
    private final Map<UUID, Map<UUID, List<Location>>> playerWeakPoints = new HashMap<>();

    public Map<UUID, List<Location>> getPlayerWeakPoints(UUID playerId) {
        return playerWeakPoints.getOrDefault(playerId, new HashMap<>());
    }

    public void setPlayerWeakPoints(UUID playerId, Map<UUID, List<Location>> weakPoints) {
        playerWeakPoints.put(playerId, weakPoints);
    }

    public void removePlayerWeakPoints(UUID playerId) {
        playerWeakPoints.remove(playerId);
    }
}
