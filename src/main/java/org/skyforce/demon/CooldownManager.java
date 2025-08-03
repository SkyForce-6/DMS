package org.skyforce.demon;

import org.bukkit.entity.Player;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private static PlayerDataManager dataManager;
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static void init(PlayerDataManager manager) {
        dataManager = manager;
    }

    public static void addCooldown(Player player, String ability, int baseSeconds) {
        PlayerProfile profile = dataManager.loadProfile(player.getUniqueId());
        int level = profile.getMeditationLevel();
        double reduction = 1 - 0.05 * (level - 1);
        int effective = (int) Math.max(1, Math.round(baseSeconds * reduction));
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(ability.toLowerCase(), System.currentTimeMillis() + effective * 1000L);
    }

    public static boolean isOnCooldown(Player player, String ability) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return false;
        Long until = map.get(ability.toLowerCase());
        return until != null && until > System.currentTimeMillis();
    }

    public static long getRemaining(Player player, String ability) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long until = map.get(ability.toLowerCase());
        if (until == null) return 0;
        long diff = until - System.currentTimeMillis();
        return diff > 0 ? diff / 1000L : 0;
    }
}
