package com.demmodders.factions.util;

import com.demmodders.factions.Factions;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Factions.MODID)
public class FactionConfig {
    public static Faction factionSubCat = new Faction();
    public static Player playerSubCat = new Player();
    public static Land landSubCat = new Land();

    public static class Faction {
        @Config.Name("Max Faction Name Length")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum length of characters in a faction's name")
        public int maxFactionNameLength = 20;

        @Config.Name("Max Faction Description Length")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum length of characters in a faction's description")
        public int maxFactionDescLength = 100;

        @Config.Name("Max Faction MOTD Length")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum length of characters in a faction's MOTD")
        public int maxFactionMOTDLength = 100;

        @Config.Name("MOTD Header")
        @Config.Comment("The text that appears at the top of every MOTD, use %s for the faction name")
        public String factionMOTDHeader = "_[%s MOTD]_";

        @Config.Name("Faction Starting Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power the faction starts with when it's created")
        public int factionStartingPower = 10;

        @Config.Name("Faction Starting Max Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can have when they it's created")
        public int factionStartingMaxPower = 10;


    }

    public static class Player {
        @Config.Name("Player Starting Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power the player starts with when they first join the server")
        public int playerStartingPower = 10;

        @Config.Name("Player Starting Max Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can have when they first join the server")
        public int playerStartingMaxPower = 10;

        @Config.Name("Player Starting Max Power cap")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can ever have")
        public int playerMaxPowerCap = 100;

        @Config.Name("Faction home Delay")
        @Config.RangeInt()
        @Config.Comment("The delay in seconds before a player teleports when using /faction home")
        public int teleportDelay = 5;
    }

    public static class Land {
        @Config.Name("Land Power worth")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power each chunk takes up when claimed")
        public int landPowerCost = 5;

        @Config.Name("Require land to connect")
        @Config.Comment("Require newly claimed land to be right next to previously claimed land")
        public boolean landRequireConnect = true;

        @Config.Name("Require land to connect when stealing")
        @Config.Comment("Require newly claimed land to be right next to previously claimed land when stealing the land of other factions")
        public boolean landRequireConnectWhenStealing = true;
    }

    @Mod.EventBusSubscriber(modid = Factions.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void configChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Factions.MODID)) {
                ConfigManager.sync(Factions.MODID, Config.Type.INSTANCE);
            }
        }

    }
}
