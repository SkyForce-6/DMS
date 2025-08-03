package org.skyforce.demon.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.player.DemonType;
import org.skyforce.demon.player.PlayerClass;
import org.skyforce.demon.player.PlayerProfile;

public class DMSCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;

    public DMSCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "setclass":
                if (args.length < 3) {
                    sender.sendMessage("§cVerwendung: /dms setclass <spieler> <demon|human>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cSpieler nicht gefunden!");
                    return true;
                }
                String classType = args[2].toLowerCase();
                PlayerProfile profile = playerDataManager.loadProfile(target.getUniqueId());
                if (classType.equals("demon")) {
                    profile.setPlayerClass(PlayerClass.DEMON);
                    playerDataManager.saveProfile(profile);
                    sender.sendMessage("§a" + target.getName() + " ist jetzt ein Dämon!");
                    sender.sendMessage("§aVerwende §6/dms setdemontype " + target.getName() + " <typ>§a, um den Dämonentyp festzulegen.");
                } else if (classType.equals("human")) {
                    profile.setPlayerClass(PlayerClass.HUMAN);
                    playerDataManager.saveProfile(profile);
                    sender.sendMessage("§a" + target.getName() + " ist jetzt ein Mensch!");
                } else {
                    sender.sendMessage("§cUnbekannte Klasse: " + classType);
                }
                break;

            case "setdemontype":
                if (args.length < 3) {
                    sender.sendMessage("§cVerwendung: /dms setdemontype <spieler> <dämonentyp>");
                    sender.sendMessage("§cBeispiel: /dms setdemontype SkyForce-6 NEZUKO");
                    listDemonTypes(sender);
                    return true;
                }
                Player demonTarget = Bukkit.getPlayer(args[1]);
                if (demonTarget == null) {
                    sender.sendMessage("§cSpieler nicht gefunden!");
                    return true;
                }

                PlayerProfile demonProfile = playerDataManager.loadProfile(demonTarget.getUniqueId());
                if (demonProfile.getPlayerClass() != PlayerClass.DEMON) {
                    sender.sendMessage("§c" + demonTarget.getName() + " ist kein Dämon! Setze zuerst die Klasse auf Dämon mit §6/dms setclass " + demonTarget.getName() + " demon");
                    return true;
                }

                String demonTypeStr = args[2].toUpperCase();
                try {
                    DemonType demonType = DemonType.valueOf(demonTypeStr);
                    demonProfile.setDemonType(demonType);
                    playerDataManager.saveProfile(demonProfile);
                    sender.sendMessage("§a" + demonTarget.getName() + " ist jetzt der Dämon: §5" + demonType.getDisplayName());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cUnbekannter Dämonentyp: " + demonTypeStr);
                    listDemonTypes(sender);
                }
                break;

            case "listdemontypes":
                listDemonTypes(sender);
                break;

            case "help":
                sendHelpMessage(sender);
                break;

            default:
                sender.sendMessage("§cUnbekannte Aktion: " + action);
                sendHelpMessage(sender);
                break;
        }
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6=== DMS Befehle ===");
        sender.sendMessage("§6/dms setclass <spieler> <demon|human> §f- Setze die Klasse eines Spielers");
        sender.sendMessage("§6/dms setdemontype <spieler> <dämonentyp> §f- Setze den Dämonentyp eines Spielers");
        sender.sendMessage("§6/dms listdemontypes §f- Zeige alle verfügbaren Dämonentypen an");
        sender.sendMessage("§6/dms help §f- Zeige diese Hilfe");
    }

    private void listDemonTypes(CommandSender sender) {
        sender.sendMessage("§6=== Verfügbare Dämonentypen ===");

        // Regular Demons
        sender.sendMessage("§e§lRegulare Dämonen:");
        StringBuilder regularDemons = new StringBuilder("§f");
        for (DemonType type : DemonType.values()) {
            if (!type.name().contains("RANK")) {
                regularDemons.append(type.name()).append("§8, §f");
            }
        }
        sender.sendMessage(regularDemons.substring(0, regularDemons.length() - 6));

        // Upper Ranks
        sender.sendMessage("§e§lObere Ränge (Upper Ranks):");
        StringBuilder upperRanks = new StringBuilder("§f");
        for (DemonType type : DemonType.values()) {
            if (type.name().contains("KOKUSHIBO") || type.name().contains("DOMA") ||
                    type.name().contains("AKAZA") || type.name().contains("NAKIME") ||
                    type.name().contains("HANTENGU") || type.name().contains("GYOKKO") ||
                    type.name().contains("GYUTARO") || type.name().contains("DAKI")) {
                upperRanks.append(type.name()).append("§8, §f");
            }
        }
        sender.sendMessage(upperRanks.substring(0, upperRanks.length() - 6));

        // Lower Ranks
        sender.sendMessage("§e§lUntere Ränge (Lower Ranks):");
        StringBuilder lowerRanks = new StringBuilder("§f");
        for (DemonType type : DemonType.values()) {
            if (type.name().contains("ENMU") || type.name().contains("ROKURO") ||
                    type.name().contains("WAKURABA") || type.name().contains("MUKAGO") ||
                    type.name().contains("RUI") || type.name().contains("KAMANUE") ||
                    type.name().contains("KYOGAI")) {
                lowerRanks.append(type.name()).append("§8, §f");
            }
        }
        sender.sendMessage(lowerRanks.substring(0, lowerRanks.length() - 6));
    }
}