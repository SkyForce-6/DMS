package org.skyforce.demon.util;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class SunUtil {

    public static boolean isInSunlight(Player player) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return false;
        long time = world.getTime();
        boolean isDay = time < 12000;
        int playerY = player.getLocation().getBlockY();
        int highestY = world.getHighestBlockYAt(player.getLocation());
        return isDay && playerY >= highestY - 1;
    }
}

