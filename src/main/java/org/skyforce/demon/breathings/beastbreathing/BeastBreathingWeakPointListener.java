package org.skyforce.demon.breathings.beastbreathing;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skyforce.demon.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeastBreathingWeakPointListener implements Listener {
    private final Main plugin;
    private final Map<UUID, Map<UUID, List<Location>>> activeDetections = new HashMap<>();

    public BeastBreathingWeakPointListener(Main plugin) {
        this.plugin = plugin;
    }

    public void addDetection(UUID playerId, UUID entityId, List<Location> weakPoints) {
        activeDetections.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(entityId, weakPoints);
    }

    public void removeDetection(UUID playerId) {
        activeDetections.remove(playerId);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        Entity target = event.getEntity();

        Map<UUID, List<Location>> playerDetections = activeDetections.get(player.getUniqueId());
        if (playerDetections == null) return;

        List<Location> weakPoints = playerDetections.get(target.getUniqueId());
        if (weakPoints == null) return;

        // Überprüfe, ob der Treffer nahe eines Schwachpunkts ist
        Location hitLoc = target.getLocation();
        for (Location weakPoint : weakPoints) {
            if (weakPoint.distance(hitLoc) <= 1.0) {
                // Doppelter Schaden bei Schwachpunkt-Treffer
                event.setDamage(event.getDamage() * 2.0);
                createWeakPointHitEffect(hitLoc);
                break;
            }
        }
    }

    private void createWeakPointHitEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.EXPLOSION, location, 5, 0.2, 0.2, 0.2, 0.05);
        world.spawnParticle(Particle.CRIT, location, 15, 0.3, 0.3, 0.3, 0.2);

        world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
        world.playSound(location, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.5f);
    }
}