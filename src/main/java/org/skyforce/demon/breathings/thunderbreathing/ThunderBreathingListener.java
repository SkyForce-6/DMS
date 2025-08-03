package org.skyforce.demon.breathings.thunderbreathing;

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

public class ThunderBreathingListener implements Listener {
    private final ThunderBreathingAbility ability;
    private final Map<Player, Integer> techniqueSlot = new HashMap<>();
    private final String[] techniqueNames = {"Thunderclap and Flash", "Sixfold", "Eightfold", "Godspeed", "Rice Spirit", "Thunder Swarm", "Distant Thunder", "Heat Lightning", "Rumble and Flash", "Honoikaz"};
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_MS = 1200; // 1,2 Sekunden
    private final PlayerDataManager playerDataManager;

    public ThunderBreathingListener(ThunderBreathingAbility ability, PlayerDataManager playerDataManager) {
        this.ability = ability;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;
        // Breathing-Prüfung
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile == null || !profile.knowsTechnique(TechniqueType.THUNDER_BREATHING)) {
          //  player.sendMessage("§cDu hast Thunder Breathing nicht gelernt!");
            return;
        }
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            // Technik wechseln nur bei Shift + Linksklick
            int slot = techniqueSlot.getOrDefault(player, 0);
            slot = (slot + 1) % 10;
            techniqueSlot.put(player, slot);
            player.sendMessage(techniqueNames[slot]);
            event.setCancelled(true);
            return;
        }
        if (!event.getAction().toString().contains("RIGHT")) return;

        int slot = techniqueSlot.getOrDefault(player, 0);
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (!player.isSneaking()) {
            // Cooldown prüfen
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) {
              //  player.sendMessage("§7Thunder Breathing ist noch auf Cooldown!");
                event.setCancelled(true);
                return;
            }
            cooldowns.put(uuid, now);
        }
        if (!player.isSneaking()) {
            // Technik ausführen
            switch (slot) {
                case 0 -> {
                    ability.thunderclapAndFlash(player);
                    player.sendMessage("§eThunder Breathing: Thunderclap and Flash!");
                }
                case 1 -> {
                    ability.sixfold(player);
                    player.sendMessage("§eThunder Breathing: Sixfold!");
                }
                case 2 -> {
                    ability.eightfold(player);
                    player.sendMessage("§eThunder Breathing: Eightfold!");
                }
                case 3 -> {
                    ability.godspeed(player);
                    player.sendMessage("§eThunder Breathing: Godspeed!");
                }
                case 4 -> {
                    ability.useSecondForm(player);
                    player.sendMessage("§eThunder Breathing: Rice Spirit!");
                }
                case 5 -> {
                    ability.useThirdForm(player);
                    player.sendMessage("§eThunder Breathing: Thunder Swarm!");
                }
                case 6 -> {
                    ability.useFourthForm(player);
                    player.sendMessage("§eThunder Breathing: Distant Thunder!");
                }
                case 7 -> {
                    ability.useFifthForm(player);
                    player.sendMessage("§eThunder Breathing: Fifth Form!");
                }
                case 8 -> {
                    ability.useSixthForm(player);
                    player.sendMessage("§eThunder Breathing: Rumble and Flash!");
                }
                case 9 -> {
                    ability.useSeventhForm(player);
                    player.sendMessage("§eThunder Breathing: Honoikazuchi!");
                }
            }
        }
        event.setCancelled(true);
    }
}
