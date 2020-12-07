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
    public static Flags flagSubCat = new Flags();
    public static PowerConfig powerSubCat = new PowerConfig();

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

        @Config.Name("Faction Land Tag")
        @Config.Comment("The text that is displayed when entering a faction's land, use %1$s for the faction name, %2$s for the faction's name as a possessive (with a 's or ' at the end), and %3$s for the faction description")
        public String factionLandTag = "Now entering %2$s land - %3$s";

        @Config.Name("Faction Land Tag no description")
        @Config.Comment("The text that is displayed when entering a faction's land, use %1$s for the faction name, %2$s for the faction's name as a possessive (with a 's or ' at the end)")
        public String factionLandTagNoDesc = "Now entering %2$s land";

        @Config.Name("Wild Land Tag")
        @Config.Comment("The text that is displayed when entering the wild, use %1$s for the wild's name, %2$s for the wild's name as a possessive (with a 's or ' at the end), and %3$s for the Wild's description")
        public String wildLandTag = "Now entering %1$s - %3$s";

        @Config.Name("Save Zone Land Tag")
        @Config.Comment("The text that is displayed when entering the SafeZone, use %1$s for the SafeZone's name, %2$s for the SafeZone's name as a possessive (with a 's or ' at the end), and %3$s for the SafeZone's description")
        public String safeLandTag = "Now entering %1$s - %3$s";

        @Config.Name("War Zone Land Tag")
        @Config.Comment("The text that is displayed when entering the WarZone, use %1$s for the WarZone's name, %2$s for the WarZone's name as a possessive (with a 's or ' at the end), and %3$s for the WarZone's description")
        public String warLandTag = "Now entering %1$s - %3$s";

        @Config.Name("Faction Starting Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power the faction starts with when it's created")
        public int factionStartingPower = 100;

        @Config.Name("Faction Starting Max Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can have when they it's created")
        public int factionStartingMaxPower = 100;

        @Config.Name("Allow ally build")
        @Config.Comment("Permit allies to build on each other's land")
        public boolean allyBuild = true;

        @Config.Name("Allow enemy build")
        @Config.Comment("Permit enemies to build on each other's land")
        public boolean enemyBuild = false;

        @Config.Name("Max Faction Members")
        @Config.RangeInt(min = 0)
        @Config.Comment("The maximum amount of members each faction is allowed (0 for infinite)")
        public int maxMembers = 0;
    }

    public static class Player {
        @Config.Name("Player Starting Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power the player starts with when they first join the server")
        public int playerStartingPower = 100;

        @Config.Name("Player Starting Max Power")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can have when they first join the server")
        public int playerStartingMaxPower = 100;

        @Config.Name("Player Max Power cap")
        @Config.RangeInt(min = 1)
        @Config.Comment("The maximum amount of power the player can ever have")
        public int playerMaxPowerCap = 1000;

        @Config.Name("Faction home teleport delay")
        @Config.Comment("The delay in seconds before a player teleports when using /faction home")
        public int teleportDelay = 3;

        @Config.Name("Faction home cooldown")
        @Config.Comment("The delay in seconds before a player can teleport when using /faction home another time")
        public int reTeleportDelay = 100;

        @Config.Name("Faction map width")
        @Config.RangeInt(min = 0)
        @Config.Comment("How many chunks in the x direction to display to the player with /faction map (must be odd, else will be +1)")
        public int mapWidth = 41;

        @Config.Name("Faction map height")
        @Config.RangeInt(min = 0)
        @Config.Comment("How many chunks in the y direction to display to the player with /faction map (must be odd, else will be +1)")
        public int mapHeight = 11;
    }

    public static class PowerConfig {
        @Config.Name("Kill power gain")
        @Config.Comment("The amount a players power recharges by when they kill")
        @Config.RangeInt(min=0)
        public int killPowerGain = 40;

        @Config.Name("Kill max power gain")
        @Config.Comment("The amount a player's maximum power increases by when killing someone")
        @Config.RangeInt(min=0)
        public int killMaxPowerGain = 30;

        @Config.Name("Death power Loss")
        @Config.Comment("The amount of power lost for dying")
        @Config.RangeInt(min=0)
        public int deathPowerLoss = 10;

        @Config.Name("Enemy Power multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much more power lost/gained for dying at the hands of/killing an enemy")
        public double enemyMultiplier = 2f;

        @Config.Name("Ally kill multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much more power gained for killing an ally")
        public double allyKillMultiplier = -.5f;

        @Config.Name("Killed by ally multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much more power lost for being killed by an ally")
        public double killedByAllyMultiplier = .5f;

        @Config.Name("Lieutenant kill/killed Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained/lost for killing/dying as a lieutenant")
        public double lieutenantMultiplier = 1.5f;

        @Config.Name("Officer kill/killed Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained/lost for killing/dying as a officer")
        public double officerMultiplier = 2f;

        @Config.Name("Owner kill/killed Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained/lost for killing/dying as the owner of a faction")
        public double ownerMultiplier = 3f;

        @Config.Name("Power gain rate")
        @Config.RangeInt(min=0)
        @Config.Comment("How many seconds in the interval between the player earning power for being on the server")
        public int powerGainInterval = 1800;

        @Config.Name("Power gain amount")
        @Config.RangeInt(min=0)
        @Config.Comment("How much power the player gains after each Power Gain Rate interval")
        public int powerGainAmount = 10;

        @Config.Name("Max Power gain amount")
        @Config.RangeInt(min=0)
        @Config.Comment("How much max power the player gains after each Power Gain Rate interval")
        public int maxPowerGainAmount = 5;

        @Config.Name("Grunt Power Gain Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained overtime for faction grunts")
        public double powerGainGruntMultiplier = 0.D;

        @Config.Name("Lieutenant Power Gain Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained overtime for faction lieutenants")
        public double powerGainLieutenantMultiplier = 1.5D;

        @Config.Name("Sergeant Power Gain Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained overtime for faction sergeants")
        public double powerGainSergeantMultiplier = 1.75D;

        @Config.Name("Owner Power Gain Multiplier")
        @Config.RangeDouble(min=0f)
        @Config.Comment("How much extra power is gained overtime for faction owners")
        public double powerGainOwnerMultiplier = 2.25D;
    }

    public static class Land {
        @Config.Name("Land Power worth")
        @Config.RangeInt(min = 1)
        @Config.Comment("The amount of power each chunk takes up when claimed")
        public int landPowerCost = 20;

        @Config.Name("Require land to connect")
        @Config.Comment("Require newly claimed land to be right next to previously claimed land")
        public boolean landRequireConnect = true;

        @Config.Name("Require land to connect when stealing")
        @Config.Comment("Require newly claimed land to be right next to previously claimed land when stealing the land of other factions")
        public boolean landRequireConnectWhenStealing = false;
    }

    public static class Flags {
        @Config.Name("Bonus Power Multiplier")
        @Config.Comment("The multiplier for the amount of power you lose/gain in factions with the BonusPower tag (Such as the War Zone)")
        public float bonusPowerMultiplier = 1.5f;
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
