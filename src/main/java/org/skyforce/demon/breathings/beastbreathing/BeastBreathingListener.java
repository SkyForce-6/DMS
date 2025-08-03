package org.skyforce.demon.breathings.beastbreathing;

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

public class BeastBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private final PlayerDataManager playerDataManager;
    private final Main plugin;

    public BeastBreathingListener() {
        this.plugin = Main.getPlugin(Main.class);
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.BEAST_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel nur mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 11;
            comboPhase.put(player, slot);
            String[] techniqueNames = {"First Fang: Pierce", "Second Fang: Slice", "Third Fang: Devour", "Fourth Fang: Slice 'n' Dice", "Fifth Fang: Crazy Cutting", "Sixth Fang: Palisade Bite", "Seventh Fang: Beast Roar", "Eighth Fang: Beast Charge", "Ninth Fang: Beast Rampage", "Tenth Fang: Beast Rampage", "Eleventh Fang: Beast Rampage"};
            player.sendMessage( techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }

        // Technik ausführen nur mit Rechtsklick
        if (!(event.getAction().toString().contains("RIGHT"))) return;
        int slot = comboPhase.getOrDefault(player, 0);
        long now = System.currentTimeMillis();
        if (comboCooldownUntil.containsKey(player) && comboCooldownUntil.get(player) > now) {
            long sekunden = (comboCooldownUntil.get(player) - now) / 1000 + 1;
          //  player.sendMessage("§cTechnik ist noch " + sekunden + "s im Cooldown!");
            return;
        }
        BeastBreathingAbility ability = new BeastBreathingAbility(plugin, player);
        switch (slot) {
            case 0:
                // First Fang: Pierce
                 ability.useFirstFang();
                player.sendMessage("§6First Fang: Pierce");

                break;
            case 1:
                // Second Fang: Slice
                 ability.useSecondFang();
                player.sendMessage("§6Second Fang: Slice");
               // comboCooldownUntil.put(player, now + 12000);
                break;
            case 2:
                // Third Fang: Devour
                 ability.useThirdFang();
                player.sendMessage("§6Third Fang: Devour");
           //comboCooldownUntil.put(player, now + 12000);
                break;
            case 3:
                 ability.useFourthFang();
                player.sendMessage("§6Fourth Fang: Slice 'n' Dice");
                //comboCooldownUntil.put(player, now + 15000);
                break;
            case 4:
                 ability.useFifthFang();
                player.sendMessage("§6Fifth Fang: Crazy Cutting");
               // comboCooldownUntil.put(player, now + 18000);
                break;
            case 5:
                 ability.useSixthFang();
                player.sendMessage("§6Sixth Fang: Palisade Bite");
                //comboCooldownUntil.put(player, now + 20000);
                break;
            case 6:
                ability.useSeventhForm();
                player.sendMessage("§6Seventh Fang: Beast Roar");
                break;
            case 7:
                ability.useEighthForm();
                player.sendMessage("§6Eighth Fang: Beast Charge ");
                break;
            case 8:
                ability.useNinthFang();
                player.sendMessage("§6Ninth Fang: Beast Rampage");
                break;
            case 9:
                ability.useTenthFang();
                player.sendMessage("§6Tenth Fang: Beast Rampage");
                break;
            case 10:
                ability.useEleventhFang();
                player.sendMessage("§6Eleventh Fang: Beast Rampage");
                break;

        }
        event.setCancelled(true);
    }

}
