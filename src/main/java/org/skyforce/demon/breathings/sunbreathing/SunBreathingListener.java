package org.skyforce.demon.breathings.sunbreathing;

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

public class SunBreathingListener implements Listener {
    private final Map<Player, Integer> comboPhase = new HashMap<>();
    private final Map<Player, Long> comboCooldownUntil = new HashMap<>();
    private static final long COOLDOWN = 1;
    private final PlayerDataManager playerDataManager;

    public SunBreathingListener() {
        this.playerDataManager = org.skyforce.demon.Main.getPlugin(org.skyforce.demon.Main.class).getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseSunBreathing(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.SUN_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        // Technikwechsel mit Shift+Linksklick
        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = comboPhase.getOrDefault(player, 0);
            slot = (slot + 1) % 13;
            comboPhase.put(player, slot);
            String[] techniqueNames = {
                "First Form: Dance", "Second Form: Clear Blue Sky", "Third Form: Raging Sun", "Fourth Form: Burning Bones, Summer Sun",
                "Fifth Form: Setting Sun Transformation", "Sixth Form: Solar Heat Haze", "Seventh Form: Beneficent Radiance",
                "Eighth Form: Sunflower Thrust", "Ninth Form: Dragon Sun Halo Head Dance", "Tenth Form: Fire Wheel",
                "Eleventh Form: Fake Rainbow", "Twelfth Form: Flame Dance", "Thirteenth Form: Combination of all previous"
            };
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
            //player.sendMessage("§cDie Technik ist noch " + sekunden + "s im Cooldown!");
            return;
        }
        SunBreathingAbility ability = new SunBreathingAbility(player);
        switch (slot) {
            case 0: ability.useFirstForm(); break;
            case 1: ability.useSecondForm(); break;
            case 2: ability.useThirdForm(); break;
            case 3: ability.useFourthForm(); break;
            case 4: ability.useFifthForm(); break;
            case 5: ability.useSixthForm(); break;
            case 6: ability.useSeventhForm(); break;
            case 7: ability.useEighthForm(); break;
            case 8: ability.useNinthForm(); break;
            case 9: ability.useTenthForm(); break;
            case 10: ability.useEleventhForm(); break;
            case 11: ability.useTwelfthForm(); break;
            case 12: ability.useThirteenthForm(); break;
        }
        comboCooldownUntil.put(player, now + COOLDOWN);
    }
}
