package org.skyforce.demon.player;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerDataListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final Set<UUID> awaitingClassChoice = new HashSet<>();
    private static final String CHOOSE_GUI_TITLE = "§8Choose your Origin";

    public PlayerDataListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() == null) {
            //Bukkit.getScheduler().runTaskLater(playerDataManager.getPlugin(), () -> openChooseGui(player), 20L);
           // awaitingClassChoice.add(player.getUniqueId());
            //set player human by default
            profile.setPlayerClass(PlayerClass.HUMAN);
            playerDataManager.saveProfile(profile);
        }
    }

    private void openChooseGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, CHOOSE_GUI_TITLE);
        ItemStack human = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta humanMeta = human.getItemMeta();
        humanMeta.setDisplayName("§eHuman");
        human.setItemMeta(humanMeta);
        gui.setItem(3, human);

        ItemStack demon = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta demonMeta = demon.getItemMeta();
        demonMeta.setDisplayName("§cDemon");
        demon.setItemMeta(demonMeta);
        gui.setItem(5, demon);

        player.openInventory(gui);
    }

    // Static helper method to open the class GUI from anywhere
    public static void openChooseGuiStatic(Player player, PlayerDataManager playerDataManager) {
        // Calls the instance method by creating a temporary listener object
        new PlayerDataListener(playerDataManager).openChooseGui(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!awaitingClassChoice.contains(player.getUniqueId())) return;
        // Titelvergleich geändert: enthält statt equals, um MC 1.20.5+ zu unterstützen
        if (event.getView().getTitle().contains("Choose your Origin")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();
            PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
            if (name.equals("§eHuman")) {
                profile.setPlayerClass(PlayerClass.HUMAN);
                playerDataManager.saveProfile(profile);
                awaitingClassChoice.remove(player.getUniqueId());
                giveHumanStarterKit(player);
                player.sendMessage("§aYou are now a Human! Good luck.");
                player.closeInventory();
            } else if (name.equals("§cDemon")) {
                profile.setPlayerClass(PlayerClass.DEMON);
                playerDataManager.saveProfile(profile);
                awaitingClassChoice.remove(player.getUniqueId());
                player.sendMessage("§cYou are now a Demon! Good luck.");
                player.closeInventory();
            }
        }
    }

    private void giveHumanStarterKit(Player player) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 8));
        // You can add more starter items here
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Profile is automatically saved when class is chosen
        awaitingClassChoice.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        // Titelvergleich geändert: enthält statt equals, um MC 1.20.5+ zu unterstützen
        if (awaitingClassChoice.contains(player.getUniqueId()) && event.getView().getTitle().contains("Choose your Origin")) {
            Bukkit.getScheduler().runTaskLater(playerDataManager.getPlugin(), () -> {
                PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
                if (profile.getPlayerClass() == null) {
                    openChooseGui(player);
                    player.sendMessage("§cYou must choose a class!");
                }
            }, 2L);
        }
    }
}