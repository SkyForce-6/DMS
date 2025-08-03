package org.skyforce.demon.commands;

import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.skyforce.demon.CustomSkeleton;
import org.skyforce.demon.Main;

public class BloodDemonArtCommand implements CommandExecutor {
    private final Main plugin;

    public BloodDemonArtCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl benutzen!");
            return true;
        }

        Location loc = player.getLocation();
        net.minecraft.world.level.World nmsWorld = ((org.bukkit.craftbukkit.v1_21_R4.CraftWorld) loc.getWorld()).getHandle();

        CustomSkeleton skeleton = new CustomSkeleton(nmsWorld);
        // Position setzen, kompatibel mit verschiedenen NMS-Versionen
        try {
            // Versuche setPos
            var method = skeleton.getClass().getMethod("setPos", double.class, double.class, double.class);
            method.invoke(skeleton, loc.getX(), loc.getY(), loc.getZ());
        } catch (NoSuchMethodException e1) {
            try {
                // Versuche setPosition
                var method = skeleton.getClass().getMethod("setPosition", double.class, double.class, double.class);
                method.invoke(skeleton, loc.getX(), loc.getY(), loc.getZ());
            } catch (Exception e2) {
                // Fallback: Felder direkt setzen
                try {
                    var fieldX = skeleton.getClass().getSuperclass().getSuperclass().getDeclaredField("x");
                    var fieldY = skeleton.getClass().getSuperclass().getSuperclass().getDeclaredField("y");
                    var fieldZ = skeleton.getClass().getSuperclass().getSuperclass().getDeclaredField("z");
                    fieldX.setAccessible(true);
                    fieldY.setAccessible(true);
                    fieldZ.setAccessible(true);
                    fieldX.set(skeleton, loc.getX());
                    fieldY.set(skeleton, loc.getY());
                    fieldZ.set(skeleton, loc.getZ());
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        nmsWorld.addFreshEntity(skeleton, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);

        player.sendMessage("§aCustom Skeleton wurde gespawnt!");
        return true;
    }

}