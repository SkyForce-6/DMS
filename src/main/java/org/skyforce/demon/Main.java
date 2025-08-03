package org.skyforce.demon;

import org.bukkit.plugin.java.JavaPlugin;
import org.skyforce.demon.blooddemonart.Akaza.AkazaListener;
import org.skyforce.demon.blooddemonart.Daki.DakiListener;
import org.skyforce.demon.blooddemonart.Douma.DoumaListener;
import org.skyforce.demon.blooddemonart.Enmu.EnmuListener;
import org.skyforce.demon.blooddemonart.Gyokko.GyokkoListener;
import org.skyforce.demon.blooddemonart.Gyutaro.GyutaroListener;
import org.skyforce.demon.blooddemonart.Muzan.MuzanListener;
import org.skyforce.demon.blooddemonart.Nezuko.NezukoListener;
import org.skyforce.demon.blooddemonart.Rui.RuiListener;
import org.skyforce.demon.blooddemonart.Zohakuten.ZohakutenListener;
import org.skyforce.demon.breathings.beastbreathing.BeastBreathingListener;
import org.skyforce.demon.breathings.beastbreathing.BeastBreathingWeakPointListener;
import org.skyforce.demon.breathings.flowerbreathing.FlowerBreathingListener;
import org.skyforce.demon.breathings.galaxybreathing.GalaxyBreathingListener;
import org.skyforce.demon.breathings.insectbreathing.InsectBreathingListener;
import org.skyforce.demon.breathings.lovebreathing.LoveBreathingListener;
import org.skyforce.demon.breathings.moonbreathing.MoonBreathingListener;
import org.skyforce.demon.breathings.stonebreathing.StoneBreathingListener;
import org.skyforce.demon.breathings.sunbreathing.SunBreathingListener;
import org.skyforce.demon.breathings.thunderbreathing.ThunderBreathingAbility;
import org.skyforce.demon.breathings.thunderbreathing.ThunderBreathingListener;
import org.skyforce.demon.breathings.waterbreathing.WaterBreathingListener;
import org.skyforce.demon.breathings.windbreathing.WindBreathingAbility;
import org.skyforce.demon.breathings.windbreathing.WindBreathingListener;
import org.skyforce.demon.commands.BloodDemonArtCommand;
import org.skyforce.demon.commands.DMSCommand;
import org.skyforce.demon.commands.MeditateCommand;
import org.skyforce.demon.player.PlayerDataListener;
import org.skyforce.demon.player.PlayerDataManager;
import org.skyforce.demon.trainer.CreateTrainerCommand;
import org.skyforce.demon.trainer.RemoveTrainerCommand;
import org.skyforce.demon.trainer.TrainerListener;
import org.skyforce.demon.CooldownManager;

public final class Main extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        // DMS OME-Logo in der Konsole anzeigen (jetzt korrekt und bunt)
        String logo = "\n"
            + "§c██████╗ §c███╗   ███╗§c███████╗\n"
            + "§c██╔══██╗§c████╗ ████║§c██╔════╝\n"
            + "§c██║  ██║§c██╔████╔██║§c███████╗\n"
            + "§c██║  ██║§c██║╚██╔╝██║§c╚════██║\n"
            + "§c██████╔╝§c██║ ╚═╝ ██║§c███████║\n"
            + "§c╚═════╝ §c╚═╝     ╚═╝§c╚══════╝\n";
        getServer().getConsoleSender().sendMessage(logo);

        this.playerDataManager = new PlayerDataManager(this);
        CooldownManager.init(playerDataManager);
        this.eventManager = new EventManager();
        this.getCommand("startevent").setExecutor(new StartEventCommand(eventManager));
        this.getCommand("removetrainer").setExecutor(new RemoveTrainerCommand());
        this.getCommand("createtrainer").setExecutor(new CreateTrainerCommand(this));
        getCommand("dms").setExecutor(new DMSCommand(playerDataManager));
        getCommand("sk").setExecutor(new BloodDemonArtCommand(this));
        getCommand("meditate").setExecutor(new MeditateCommand(playerDataManager));

        getServer().getPluginManager().registerEvents(new EventWoolListener(eventManager), this);
        getServer().getPluginManager().registerEvents(new TrainerListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new DemonSunListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);
       // getServer().getPluginManager().registerEvents(new IntroListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new org.skyforce.demon.breathings.flamebreathing.FlameBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new WaterBreathingListener(playerDataManager), this);
        ThunderBreathingAbility thunderAbility = new ThunderBreathingAbility(this);
        getServer().getPluginManager().registerEvents(new ThunderBreathingListener(thunderAbility, playerDataManager), this);
        WindBreathingAbility windAbility = new WindBreathingAbility(this);
        getServer().getPluginManager().registerEvents(new WindBreathingListener(windAbility, playerDataManager), this);
        getServer().getPluginManager().registerEvents(new org.skyforce.demon.breathings.mistbreathing.MistBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new SunBreathingListener() , this);
        getServer().getPluginManager().registerEvents(new StoneBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new org.skyforce.demon.breathings.soundbreathing.SoundBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new LoveBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new org.skyforce.demon.breathings.serpentbreathing.SerpentBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new BeastBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new BeastBreathingWeakPointListener(this), this);
        getServer().getPluginManager().registerEvents(new MoonBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new GalaxyBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new InsectBreathingListener(), this);
        getServer().getPluginManager().registerEvents(new FlowerBreathingListener(), this);


        getServer().getPluginManager().registerEvents(new NezukoListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new DoumaListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new EnmuListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new RuiListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new MuzanListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new AkazaListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new ZohakutenListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new GyokkoListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new GyutaroListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new DakiListener(playerDataManager),this);

        getServer().getPluginManager().registerEvents(new ChatManager() , this);


    }

    @Override
    public void onDisable() {
        
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
