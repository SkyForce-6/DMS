package org.skyforce.demon.breathings.insectbreathing;

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
 * Listener for Insect Breathing techniques
 * Created: 2025-06-19 09:06:37
 * @author SkyForce-6
 */
public class InsectBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final Main plugin;

    public InsectBreathingListener() {
        this.plugin = Main.getPlugin(Main.class);
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.INSECT_BREATHING)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technique switch with Shift+Left click
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 4; // Insect Breathing has 6 forms
            comboPhase.put(player, slot);

            String[] techniqueNames = {
                    "Dance of the Butterfly: Capric",
                    "Dance of the Bee Sting: True Flutter",
                    "Dance of the Dragonfly: Compound Eye Hexagon",
                    "Dance of the Centipede: Hundred-Legged Zigzag"
            };

            String techniqueName = (slot >= 0 && slot < techniqueNames.length) ? techniqueNames[slot] : "Unknown Technique";
            player.sendMessage("§bInsect Breathing technique changed: §e" + techniqueName);
            event.setCancelled(true);
            return;
        }

        // Execute technique with right click
        if (!(event.getAction().toString().contains("RIGHT"))) return;

        InsectBreathingAbility ability = new InsectBreathingAbility(plugin, player);
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
        }

        event.setCancelled(true);
    }
}