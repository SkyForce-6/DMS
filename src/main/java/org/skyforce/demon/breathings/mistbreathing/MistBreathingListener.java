package org.skyforce.demon.breathings.mistbreathing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;

public class MistBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private static final long COOLDOWN = 1;
    private final PlayerDataManager playerDataManager;

    public MistBreathingListener() {
        this.playerDataManager = org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class).getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseMistBreathing(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.MIST_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;


        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 7;
            comboPhase.put(player, slot);
            String[] techniqueNames = {"First Form: Low Clouds, Distant Haze", "Second Form: Eight-Layered Mist", "Third Form: Scattering Mist", "Fourth Form: Shifting Clouds", "Fifth Form: Sea of Clouds and Haze", "Sixth Form: Lunar Dispersing Mist", "Seventh Form: Shifting Mist"};
            player.sendMessage(techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen mit Rechtsklick
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        int slot = comboPhase.getOrDefault(player, 0);
        long now = System.currentTimeMillis();
        if (comboCooldownUntil.containsKey(player) && comboCooldownUntil.get(player) > now) {
            long sekunden = (comboCooldownUntil.get(player) - now) / 1000 + 1;
       //     player.sendMessage("§cDie Technik ist noch " + sekunden + "s im Cooldown!");
            return;
        }
        switch (slot) {
            case 0:
                new MistBreathingAbility(player).useFirstForm();
                break;
            case 1:
                new MistBreathingAbility(player).useSecondForm();
                break;
            case 2:
                new MistBreathingAbility(player).useThirdForm();
                break;
            case 3:
                new MistBreathingAbility(player).useFourthForm();
                break;
            case 4:
                new MistBreathingAbility(player).useFifthForm();
                break;
            case 5:
                new MistBreathingAbility(player).useSixthForm();
                break;
            case 6:
                new MistBreathingAbility(player).useSeventhForm();
                break;
        }
        comboCooldownUntil.put(player, now + COOLDOWN);
    }
}
