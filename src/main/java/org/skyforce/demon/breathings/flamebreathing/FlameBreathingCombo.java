package org.skyforce.demon.breathings.flamebreathing;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FlameBreathingCombo {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> lastComboTime = new HashMap<>();
    private final long comboTimeout;

    public FlameBreathingCombo(long comboTimeout) {
        this.comboTimeout = comboTimeout;
    }

    public int getPhase(Player player) {
        return comboPhase.getOrDefault(player, 0);
    }

    public void reset(Player player) {
        comboPhase.put(player, 0);
    }

    public void updatePhase(Player player, int phase) {
        comboPhase.put(player, phase);
        lastComboTime.put(player, System.currentTimeMillis());
    }

    public boolean isTimedOut(Player player) {
        if (!lastComboTime.containsKey(player)) return false;
        return System.currentTimeMillis() - lastComboTime.get(player) > comboTimeout;
    }
}
