package org.skyforce.demon.breathings.serpentbreathing;

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

public class SerpentBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> activeUntil = new HashMap<>();
    private static final long COMBO_TIMEOUT = 2000;
    private static final long ACTIVE_DURATION = 10000;
    private static final long COOLDOWN = 15000;
    private final Map<Player, Long> lastComboTime = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    // private final SerpentBreathingAbility serpentBreathingAbility;
    // private final SerpentBreathingCombo serpentBreathingCombo;

    public SerpentBreathingListener() {
        this.playerDataManager = org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class).getPlayerDataManager();
        // this.serpentBreathingAbility = new SerpentBreathingAbility(activeUntil);
        // this.serpentBreathingCombo = new SerpentBreathingCombo(COMBO_TIMEOUT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.SERPENT_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel nur mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 6; // Anzahl der Techniken ggf. anpassen
            comboPhase.put(player, slot);
            String[] techniqueNames = {"Winding Serpent Slash", "Venom Fangs", "Slithering Snake", "Coil Bind", "Twisting Strike", "Serpent's Dance"};
            player.sendMessage("§bSerpent Breathing Technik gewechselt: §e" + techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen nur mit Rechtsklick
        if (!(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
        int slot = comboPhase.getOrDefault(player, 0);
        long now = System.currentTimeMillis();
        SerpentBreathingAbility ability = new SerpentBreathingAbility(Main.getPlugin(Main.class), player);
        switch (slot) {
            case 0:
                // TODO: Winding Serpent Slash ausführen
                player.sendMessage("§aWinding Serpent Slash ausgeführt!");
                ability.useFirstForm(player);
                break;
            case 1:
                // TODO: Venom Fangs ausführen
                player.sendMessage("§aVenom Fangs ausgeführt!");
                ability.useSecondForm();
                break;
            case 2:
                // TODO: Slithering Snake ausführen
                player.sendMessage("§aSlithering Snake ausgeführt!");
                ability.useThirdForm();
                break;
            case 3:
                // TODO: Coil Bind ausführen
                player.sendMessage("§aCoil Bind ausgeführt!");
                ability.useFourthForm();
                break;
            case 4:
                // TODO: Twisting Strike ausführen
                player.sendMessage("§aTwisting Strike ausgeführt!");
                ability.useFifthForm();
                break;
            case 5:
                // TODO: Serpent's Dance ausführen
                player.sendMessage("§aSerpent's Dance ausgeführt!");
                break;
        }
    }
}
