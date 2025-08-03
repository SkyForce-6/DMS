package org.skyforce.demon.trainer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;
import org.skyforce.demon.player.TechniqueType;

public class TrainerListener implements Listener {
    private static final String TRAINER_NAME = ChatColor.AQUA + "Trainer";
    private static final String TRAINER_MENU_TITLE = ChatColor.DARK_AQUA + "Breathing Styles";

    private final PlayerDataManager playerDataManager;

    public TrainerListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        if (profile.getPlayerClass() == PlayerClass.DEMON) {
            return;
        }
       // player.sendMessage(ChatColor.YELLOW + "DEBUG: Interacted with entity: " + entity.getType());
        if (entity instanceof org.bukkit.entity.Villager villager) {
        //    player.sendMessage(ChatColor.YELLOW + "DEBUG: Villager name: " + villager.getCustomName());
            if (TRAINER_NAME.equals(villager.getCustomName())) {
                event.setCancelled(true);
          //      player.sendMessage(ChatColor.GREEN + "DEBUG: Opening trainer menu!");
                openTrainerMenu(player);
            }
        }
    }

    private void openTrainerMenu(Player player) {
        PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
        TechniqueType selected = null;
        for (TechniqueType t : TechniqueType.values()) {
            if (profile.knowsTechnique(t)) {
                selected = t;
                break;
            }
        }
        int size = Math.max(9, ((TechniqueType.values().length + 8) / 9) * 9); // immer Vielfaches von 9
        Inventory inv = Bukkit.createInventory(null, size, TRAINER_MENU_TITLE);

        int slot = 0;
        for (TechniqueType t : TechniqueType.values()) {
            ItemStack item;
            ItemMeta meta;
            switch (t) {
                case FLAME_BREATHING -> {
                    item = new ItemStack(Material.BLAZE_POWDER);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Flame Breathing");
                }
                case WATER_BREATHING -> {
                    item = new ItemStack(Material.WATER_BUCKET);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + "Water Breathing");
                }
                case THUNDER_BREATHING -> {
                    item = new ItemStack(Material.YELLOW_DYE);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "Thunder Breathing");
                }
                case WIND_BREATHING -> {
                    item = new ItemStack(Material.FEATHER);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + "Wind Breathing");
                }
                case MIST_BREATHING -> {
                    item = new ItemStack(Material.GRAY_DYE);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GRAY + "Mist Breathing");
                }
                case STONE_BREATHING -> {
                    item = new ItemStack(Material.COBBLESTONE);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_GRAY + "Stone Breathing");
                }
                case SOUND_BREATHING -> {
                    item = new ItemStack(Material.NOTE_BLOCK);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Sound Breathing");
                }
                case LOVE_BREATHING -> {
                    item = new ItemStack(Material.PINK_DYE);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Love Breathing");
                }
                case SERPENT_BREATHING -> {
                    item = new ItemStack(Material.VINE);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_GREEN + "Serpent Breathing");
                }
                case SUN_BREATHING -> {
                    item = new ItemStack(Material.SUNFLOWER);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.GOLD + "Sun Breathing");
                }
                case BEAST_BREATHING -> {
                    item = new ItemStack(Material.RABBIT_FOOT);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_AQUA + "Beast Breathing");
                }
                case MOON_BREATHING -> {
                    item = new ItemStack(Material.ENDER_PEARL);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_PURPLE + "Moon Breathing");
                }
                case GALAXY_BREATHING -> {
                    item = new ItemStack(Material.AMETHYST_SHARD);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.AQUA + "Galaxy Breathing");
                }
                case INSECT_BREATHING -> {
                    item = new ItemStack(Material.HONEYCOMB);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "Insect Breathing");
                }
                case FLOWER_BREATHING -> {
                    item = new ItemStack(Material.POPPY);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + "Flower Breathing");
                }
                default -> {
                    item = new ItemStack(Material.BARRIER);
                    meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + "Unknown Technique");
                }
            }
            if (selected == t) {
                meta.setLore(java.util.Collections.singletonList(ChatColor.GRAY + "Already selected!"));
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                try {
                    item.addUnsafeEnchantment(Enchantment.LURE, 1);
                } catch (IllegalArgumentException ignored) {
                    // If LUCK is not allowed, skip glow
                }
            }
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onTrainerMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(TRAINER_MENU_TITLE)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            PlayerProfile profile = playerDataManager.loadProfile(player.getUniqueId());
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            // Block if already selected
            if (clicked.getItemMeta().hasLore() && clicked.getItemMeta().getLore().contains(ChatColor.GRAY + "Already selected!")) {
                player.sendMessage(ChatColor.GRAY + "You have already selected this breathing style!");
                return;
            }
            // Technik anhand des Namens finden
            TechniqueType chosen = null;
            for (TechniqueType t : TechniqueType.values()) {
                String displayName;
                switch (t) {
                    case FLAME_BREATHING -> displayName = "Flame Breathing";
                    case WATER_BREATHING -> displayName = "Water Breathing";
                    case THUNDER_BREATHING -> displayName = "Thunder Breathing";
                    case WIND_BREATHING -> displayName = "Wind Breathing";
                    case MIST_BREATHING -> displayName = "Mist Breathing";
                    case STONE_BREATHING -> displayName = "Stone Breathing";
                    case SOUND_BREATHING -> displayName = "Sound Breathing";
                    case LOVE_BREATHING -> displayName = "Love Breathing";
                    case SERPENT_BREATHING -> displayName = "Serpent Breathing";
                    case SUN_BREATHING -> displayName = "Sun Breathing";
                    case BEAST_BREATHING -> displayName = "Beast Breathing";
                    case MOON_BREATHING -> displayName = "Moon Breathing";
                    case GALAXY_BREATHING -> displayName = "Galaxy Breathing";
                    case INSECT_BREATHING -> displayName = "Insect Breathing";
                    case FLOWER_BREATHING -> displayName = "Flower Breathing";
                    default -> displayName = null;
                }
                if (name.equals(displayName)) {
                    chosen = t;
                    break;
                }
            }
            if (chosen != null) {
                profile.getLearnedTechniques().clear();
                profile.learnTechnique(chosen);
                playerDataManager.saveProfile(profile);
                player.sendMessage(ChatColor.GREEN + "You have selected " + name + "!");
            }
            player.closeInventory();
        }
    }
}
