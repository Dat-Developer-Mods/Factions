package com.demmodders.factions.util;

import com.demmodders.factions.Factions;
import net.minecraftforge.common.config.Config;

@Config(modid = Factions.MODID)
public class FactionConfig {
    @Config.Name("Max Faction Name Length")
    @Config.RangeInt(min=1)
    @Config.Comment("The maximum length of characters in a faction's name")
    public static int maxFactionNameLength = 20;

    @Config.Name("Faction Starting Power")
    @Config.RangeInt(min=1)
    @Config.Comment("The amount of power the faction starts with when it's created")
    public static int factionStartingPower = 10;

    @Config.Name("Faction Starting Max Power")
    @Config.RangeInt(min=1)
    @Config.Comment("The maximum amount of power the player can have when they it's created")
    public static int factionStartingMaxPower = 10;

    // Player
    @Config.Name("Player Starting Power")
    @Config.RangeInt(min=1)
    @Config.Comment("The amount of power the player starts with when they first join the server")
    public static int playerStartingPower = 10;

    @Config.Name("Player Starting Max Power")
    @Config.RangeInt(min=1)
    @Config.Comment("The maximum amount of power the player can have when they first join the server")
    public static int playerStartingMaxPower = 10;
}
