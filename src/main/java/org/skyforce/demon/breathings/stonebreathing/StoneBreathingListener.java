package org.skyforce.demon.breathings.stonebreathing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.Main;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;

public class StoneBreathingListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final Map<Player, Integer> formSlot = new HashMap<>();
    private static final int MAX_FORMS = 6; // Passe an, falls mehr Formen vorhanden

    public StoneBreathingListener() {
        this.playerDataManager = Main.getPlugin(Main.class).getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.STONE_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel nur mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = formSlot.getOrDefault(player, 0);
            slot = (slot + 1) % MAX_FORMS;
            formSlot.put(player, slot);
            String[] formNames = {"Serpentinite Bipolar", "Second Form", "Third Form", "Fourth Form", "Fifth Form", "Sixth Form"};
            player.sendMessage("§bStone Breathing Technik gewechselt: §e" + formNames[slot]);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen nur mit Rechtsklick
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        int slot = formSlot.getOrDefault(player, 0);
        StoneBreathingAbility ability = new StoneBreathingAbility(Main.getPlugin(Main.class), player);
        switch (slot) {
            case 0:
                ability.useFirstForm();
                break;
             case 1: ability.useSecondForm(); break; // Weitere Formen ggf. ergänzen
             case 2: ability.useThirdForm(); break;
             case 3: ability.useFourthForm(); break;
             case 4: ability.useFifthForm(); break;
             case 5: ability.useSixthForm(); break; // Falls mehr als 5 Formen vorhanden sind
            default:
                ability.useFirstForm();
        }
        event.setCancelled(true);
    }
}
