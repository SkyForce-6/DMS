package org.skyforce.demon.breathings.galaxybreathing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for Galaxy Breathing techniques
 * Created: 2025-06-18 21:11:18
 * @author SkyForce-6
 */
public class GalaxyBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final Main plugin;

    public GalaxyBreathingListener() {
        this.plugin = Main.getPlugin(Main.class);
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.GALAXY_BREATHING)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technique switch with Shift+Left click
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 13; // Galaxy Breathing has 8 forms
            comboPhase.put(player, slot);

            String[] techniqueNames = {
                    "Starfall Cascade",
                    "Nebula Storm",
                    "Cosmic Void Strike",
                    "Celestial Dragon's Path",
                    "Supernova Explosion",
                    "Black Hole Absorption",
                    "Milky Way Revolution",
                    "Universal Extinction",
                    "Star Curse",
                    "Peaceful Lion",
                    "Scorching Parhelion",
                    "Star Fragment Phoenix Flame",
                    "Universal Convergence"
            };

            String techniqueName = (slot >= 0 && slot < techniqueNames.length) ? techniqueNames[slot] : "Unknown Technique";
            player.sendMessage("§bGalaxy Breathing technique changed: §e" + techniqueName);
            event.setCancelled(true);
            return;
        }

        // Execute technique with right click
        if (!(event.getAction().toString().contains("RIGHT"))) return;

        GalaxyBreathingAbility ability = new GalaxyBreathingAbility(plugin, player);
        int slot = comboPhase.getOrDefault(player, 0);

        switch (slot) {
            case 0:
                ability.useFirstForm();
                break;
            case 1:
                ability.useSecondForm();
                break;
            case 2:
                ability.useThirdForm();
                break;
            case 3:
                ability.useFourthForm();
                break;
            case 4:
                ability.useFifthForm();
                break;
            case 5:
                ability.useSixthForm();
                break;
            case 6:
                ability.useSeventhForm();
                break;
            case 7:
                ability.useEighthForm();
                break;
            case 8:
                ability.useNinthForm();
                break;
            case 9:
                ability.useTenthForm();
                break;
            case 10:
                ability.useEleventhForm();
                break;
            case 11:
                ability.useTwelfthForm();
                break;
            case 12:
                ability.useThirteenthForm();
                break;

        }

        event.setCancelled(true);
    }
}