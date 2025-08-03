package org.skyforce.demon.breathings.moonbreathing;


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
 * Listener for Moon Breathing techniques
 * Created: 2025-06-18 19:23:15
 * @author SkyForce-6
 */
public class MoonBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final Main plugin;

    public MoonBreathingListener() {
        this.plugin = Main.getPlugin(Main.class);
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.MOON_BREATHING)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 16; // Moon Breathing hat 16 Formen
            comboPhase.put(player, slot);

            String[] techniqueNames = {
                    "Dark Moon - Evening Palace",
                    "Pearl Flower Moongazing",
                    "Loathsome Moon - Chains",
                    "Rising Moon - Evening Palace",
                    "Moonlit Dance - Crescent Moon",
                    "Full Moon - Lunar Eclipse",
                    "Moonlit Night - Silver Moon",
                    "Mirror of Misfortune - Moonlit",
                    "Catastrophe - Tenman Crescent Moon",
                    "Drilling Slashes, Moon Through Bamboo Leaves",
                    "Catastrophe, Tenman Crescent Moon ",
                    "Moonbow, Half Moon",
            };

            String techniqueName = (slot >= 0 && slot < techniqueNames.length) ? techniqueNames[slot] : "Unbekannte Technik";
            player.sendMessage(techniqueName);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen mit Rechtsklick
        if (!(event.getAction().toString().contains("RIGHT"))) return;

        MoonBreathingAbility ability = new MoonBreathingAbility(plugin, player);
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
                ability.useFourteenthForm();
                break;
            case 11:
                ability.useSixteenthForm();
                break;
        }

        event.setCancelled(true);
    }
}