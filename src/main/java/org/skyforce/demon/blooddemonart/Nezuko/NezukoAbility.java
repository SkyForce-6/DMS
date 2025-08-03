package org.skyforce.demon.blooddemonart.Nezuko;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


public class NezukoAbility {

    /**
     * Activates the Exploding Blood ability which deals fire damage to already injured enemies
     * @param player The player using the ability
     * @param target The target entity that was hit
     * @param event The damage event
     */
    public void activateExplodingBlood(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        // Configuration values - you can move these to your config file
        double requiredHealthPercentage = 0.8; // Target must be below 80% health
        double explosionDamage = 4.0; // Extra damage on explosion
        int fireTicks = 60; // Fire duration (3 seconds)
        boolean enableExplosionEffect = true; // Visual explosion
        float explosionPower = 0.0F; // No block damage

        // Check if target is already injured (below requiredHealthPercentage)
        if (target.getHealth() >= target.getMaxHealth() * requiredHealthPercentage) {
            return; // Target not injured enough, don't activate ability
        }

        // Apply exploding blood effect
        Location location = target.getLocation();
        World world = location.getWorld();

        // Visual and sound effects
        // Blood particles (red dust)
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255,105,180), 1.0F);
        for (int i = 0; i < 30; i++) {
            world.spawnParticle(Particle.DUST, location.clone().add(
                            Math.random() * 2 - 1,
                            Math.random() * 2,
                            Math.random() * 2 - 1),
                    1, dustOptions);
        }

        // Fire particles
        world.spawnParticle(Particle.FLAME, location, 20, 0.5, 0.5, 0.5, 0.05);
        world.spawnParticle(Particle.LAVA, location, 10, 0.5, 0.5, 0.5, 0.1);

        // Explosion effect (visual only, no block damage)
        if (enableExplosionEffect) {
            world.createExplosion(location, explosionPower, false, false, player);
        }

        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
        world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
        double originalDamage = event.getDamage();
        event.setDamage(originalDamage + explosionDamage);
        target.setFireTicks(fireTicks);

    }

    public void activateHealingFlames(Player player, LivingEntity target, JavaPlugin plugin) {
        // Configuration values - you can move these to your config file
        double healthThreshold = 0.2; // Target must be below 20% health
        double healAmount = 8.0; // Amount to heal
        int regenerationDuration = 100; // 5 seconds (20 ticks * 5)
        int regenerationAmplifier = 2; // Regeneration III

        // Check if target is below health threshold
        if (target.getHealth() > target.getMaxHealth() * healthThreshold) {
            player.sendMessage(ChatColor.RED + "Target is not injured enough for Healing Flames!");
            return;
        }

        // Apply healing flames effect
        Location location = target.getLocation();
        World world = location.getWorld();

        // Visual and sound effects
        // Flame particles
        for (int i = 0; i < 5; i++) {
            // Create a delayed particle effect for nicer visuals
            final int iteration = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Spiral flame particles
                    for (int j = 0; j < 10; j++) {
                        double angle = j * Math.PI * 2 / 10;
                        double radius = 0.5 + (iteration * 0.2);
                        Location particleLoc = location.clone().add(
                                Math.cos(angle) * radius,
                                0.2 * iteration,
                                Math.sin(angle) * radius);

                        world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.05, 0.05, 0.05, 0.01);

                        // Pink healing particles (Nezuko's flames are pink/pinkish-red)
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 120, 220), 1.0F);
                        world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
                    }
                }
            }.runTaskLater(plugin, i * 4L);
        }

        // Sound effects
        world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.5F);
        world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 1.2F);

        // Apply healing - safely to avoid exceeding max health
        double newHealth = Math.min(target.getMaxHealth(), target.getHealth() + healAmount);
        target.setHealth(newHealth);

        // Apply regeneration effect
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                regenerationDuration,
                regenerationAmplifier,
                false, // ambient particles
                true,  // show particles
                true   // show icon
        ));

        // Notify both player and target
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Healing Flames " +
                ChatColor.GOLD + "activated on " + target.getName() + "!");

        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetPlayer.sendMessage(ChatColor.LIGHT_PURPLE + player.getName() +
                    ChatColor.GOLD + " healed you with Healing Flames!");
        }
    }
}