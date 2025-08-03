package org.skyforce.demon.player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private PlayerClass playerClass;
    private DemonType demonType;
    private boolean nezuko;
    private Set<TechniqueType> learnedTechniques;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.playerClass = PlayerClass.HUMAN; // Default to HUMAN
        this.demonType = null;
        this.nezuko = false;
        this.learnedTechniques = new HashSet<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;

        // Clear techniques if changing to demon
        if (playerClass == PlayerClass.DEMON) {
            learnedTechniques.clear();
        }
    }

    public DemonType getDemonType() {
        return demonType;
    }

    public void setDemonType(DemonType demonType) {
        this.demonType = demonType;
    }

    public boolean isNezuko() {
        return nezuko;
    }

    public void setNezuko(boolean nezuko) {
        this.nezuko = nezuko;
    }

    public Set<TechniqueType> getLearnedTechniques() {
        return learnedTechniques;
    }

    public void learnTechnique(TechniqueType technique) {
        learnedTechniques.add(technique);
    }

    public boolean knowsTechnique(TechniqueType technique) {
        return learnedTechniques.contains(technique);
    }

    public boolean hasFlameBreathing() {
        return knowsTechnique(TechniqueType.FLAME_BREATHING);
    }

    public boolean isDemon() {
        return playerClass == PlayerClass.DEMON;
    }

    public boolean isHuman() {
        return playerClass == PlayerClass.HUMAN;
    }
}