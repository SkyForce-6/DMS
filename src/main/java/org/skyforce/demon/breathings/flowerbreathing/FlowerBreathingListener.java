package org.skyforce.demon.breathings.flowerbreathing;

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
 * Listener for Flower Breathing techniques
 * Created: 2025-06-19 09:41:23
 * @author SkyForce-6
 */
public class FlowerBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final Main plugin;

    public FlowerBreathingListener() {
        this.plugin = Main.getPlugin(Main.class);
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.FLOWER_BREATHING)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technique switch with Shift+Left click
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 5; // Flower Breathing has 5 forms
            comboPhase.put(player, slot);

            String[] techniqueNames = {
                    "Second Form: Honorable Shadow Plum",
                    "Fourth Form: Crimson Hanagoromo ",
                    "Fifth Form: Peonies of Futility",
                    "Sixth Form: Whirling Peach",
                    "Final Form: Equinoctial Vermilion Eye"
            };

            String techniqueName = (slot >= 0 && slot < techniqueNames.length) ? techniqueNames[slot] : "Unknown Technique";
            player.sendMessage("§dFlower Breathing technique changed: §e" + techniqueName);
            event.setCancelled(true);
            return;
        }

        // Execute technique with right click
        if (!(event.getAction().toString().contains("RIGHT"))) return;

        FlowerBreathingAbility ability = new FlowerBreathingAbility(plugin, player);
        int slot = comboPhase.getOrDefault(player, 0);

        switch (slot) {
            case 0:
                ability.useFirstForm();
                break;
            case 1:
                ability.useFourthForm();
                break;
            case 2:
                ability.useFifthForm();
                break;
            case 3:
                ability.useSixthForm();
                break;
            case 4:
                ability.useFinalForm();
                break;
        }

        event.setCancelled(true);
    }
}