package org.skyforce.demon.breathings.windbreathing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindBreathingListener implements Listener {
    private final WindBreathingAbility ability;
    private final PlayerDataManager playerDataManager;
    private final Map<Player, Integer> techniqueSlot = new HashMap<>();
    private final String[] techniqueNames = {"Dust Whirlwind Cutter", "Claws-Purifying Wind", "Clear Storm Wind Tree", "Rising Cyclone", "Cold Mountain Wind", "Black Wind Mountain Mist", "Gale Force Slash", "Primary Gale Slash", "Idaten Typhoon"};
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_MS = 1200;

    public WindBreathingListener(WindBreathingAbility ability, PlayerDataManager playerDataManager) {
        this.ability = ability;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile == null || !profile.knowsTechnique(TechniqueType.WIND_BREATHING)) {
            return;
        }
        // Technikwechsel
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = techniqueSlot.getOrDefault(player, 0);
            slot = (slot + 1) % 9;
            techniqueSlot.put(player, slot);
            player.sendMessage(techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }
        // Technik ausführen
        if (event.getAction().toString().contains("RIGHT")) {
            int slot = techniqueSlot.getOrDefault(player, 0);
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(player.getUniqueId()) && now - cooldowns.get(player.getUniqueId()) < COOLDOWN_MS) {
              //  player.sendMessage("§cDie Technik ist noch auf Abklingzeit!");
                return;
            }
            cooldowns.put(player.getUniqueId(), now);
            switch (slot) {
                case 0:
                    ability.useFirstForm(player);
                    break;
                case 1:
                    ability.useSecondForm(player);
                    break;
                case 2:
                    ability.useThirdForm(player);
                    break;
                case 3:
                    ability.useFourthForm(player);
                    break;
                case 4:
                    ability.useFifthForm(player);
                    break;
                case 5:
                    ability.useSixthForm(player);
                    break;
                case 6:
                    ability.useSeventhForm(player);
                    break;
                case 7:
                    ability.useEighthForm(player);
                    break;
                case 8:
                    ability.useNinthForm(player);
                    break;
            }
            event.setCancelled(true);
        }
    }
}
