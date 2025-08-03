package org.skyforce.demon.player;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.skyforce.demon.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {
    private final Main plugin;
    private final File dataFolder;

    public PlayerDataManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public FileConfiguration getPlayerConfig(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void savePlayerConfig(UUID uuid, FileConfiguration config) {
        try {
            config.save(getPlayerFile(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Main getPlugin() {
        return plugin;
    }

    public boolean hasFlameBreathing(UUID uuid) {
        PlayerProfile profile = loadProfile(uuid);
        return profile != null && profile.hasFlameBreathing();
    }

    public void setFlameBreathing(UUID uuid, boolean learned) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("flame_breathing", learned);
        savePlayerConfig(uuid, config);
    }

    public PlayerProfile loadProfile(UUID uuid) {
        FileConfiguration config = getPlayerConfig(uuid);
        PlayerProfile profile = new PlayerProfile(uuid);

        // Fix: Kein Default mehr, sondern nur setzen wenn vorhanden!
        String clazz = config.getString("class", null);
        if (clazz != null) {
            try {
                profile.setPlayerClass(PlayerClass.valueOf(clazz.toUpperCase()));
            } catch (IllegalArgumentException e) {
                profile.setPlayerClass(null);
            }
        } else {
            profile.setPlayerClass(null);
        }

        // Load demon type if player is a demon
        if (profile.getPlayerClass() == PlayerClass.DEMON) {
            String demonTypeStr = config.getString("demon_type");
            if (demonTypeStr != null) {
                DemonType demonType = DemonType.fromString(demonTypeStr);
                profile.setDemonType(demonType);
            }
        }

        profile.setNezuko(config.getBoolean("nezuko", false));

        // Load techniques
        Set<String> techniques = new HashSet<>(config.getStringList("techniques"));
        for (String t : techniques) {
            try {
                profile.learnTechnique(TechniqueType.valueOf(t));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return profile;
    }

    public void saveProfile(PlayerProfile profile) {
        FileConfiguration config = getPlayerConfig(profile.getUuid());

        // Save class
        config.set("class", profile.getPlayerClass() != null ? profile.getPlayerClass().name() : null);

        // Save demon type if player is a demon
        if (profile.getPlayerClass() == PlayerClass.DEMON) {
            config.set("demon_type", profile.getDemonType() != null ? profile.getDemonType().name() : null);
        } else {
            config.set("demon_type", null); // Clear demon type if player is not a demon
        }

        config.set("nezuko", profile.isNezuko());

        // Save techniques
        Set<String> techs = new HashSet<>();
        for (TechniqueType t : profile.getLearnedTechniques()) {
            techs.add(t.name());
        }
        config.set("techniques", new java.util.ArrayList<>(techs));

        savePlayerConfig(profile.getUuid(), config);
    }

    /**
     * Set a player's class to demon or human
     */
    public void setPlayerClass(UUID uuid, PlayerClass playerClass) {
        PlayerProfile profile = loadProfile(uuid);
        profile.setPlayerClass(playerClass);
        saveProfile(profile);
    }

    /**
     * Set a player's demon type
     */
    public void setDemonType(UUID uuid, DemonType demonType) {
        PlayerProfile profile = loadProfile(uuid);
        if (profile.getPlayerClass() == PlayerClass.DEMON) {
            profile.setDemonType(demonType);
            saveProfile(profile);
        }
    }
}