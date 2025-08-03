package org.skyforce.demon.breathings.soundbreathing;

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

public class SoundBreathingListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final Map<Player, Integer> formSlot = new HashMap<>();
    private static final int MAX_FORMS = 3;

    public SoundBreathingListener() {
        this.playerDataManager = Main.getPlugin(Main.class).getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (!profile.knowsTechnique(TechniqueType.SOUND_BREATHING)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_SWORD")) return;

        if (player.isSneaking() && event.getAction().toString().contains("LEFT")) {
            int slot = formSlot.getOrDefault(player, 0);
            slot = (slot + 1) % MAX_FORMS;
            formSlot.put(player, slot);
            String[] formNames = {"First Form", "Fourth Form", "Fifth Form"};
            player.sendMessage(formNames[slot]);
            event.setCancelled(true);
            return;
        }

        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        int slot = formSlot.getOrDefault(player, 0);
        SoundBreathingAbility ability = new SoundBreathingAbility(Main.getPlugin(Main.class), player);
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
        }
        event.setCancelled(true);
    }
}
