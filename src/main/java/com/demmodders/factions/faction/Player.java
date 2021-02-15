package com.demmodders.factions.faction;

import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.factions.util.DemUtils;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.enums.FactionChatMode;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.structures.Power;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.UUID;

public class Player {
    public UUID faction = null;
    public FactionRank factionRank = null;
    public Power power = null;
    public String lastKnownName = "";
    public long lastOnline = 0L;
    public transient UUID lastFactionLand  = null;
    public transient boolean autoClaim = false;
    public transient ArrayList<UUID> invites = new ArrayList<>();
    public transient FactionChatMode factionChat = FactionChatMode.NORMAL;

    public transient long lastTeleport = 0L;

    public Player(){
    }

    public Player(UUID Faction, FactionRank Rank, Power power, String name){
        this.faction = Faction;
        this.factionRank = Rank;
        this.power = power;
        this.lastKnownName = name;
    }

    public boolean hasInvite(UUID factionID){
        return invites.contains(factionID);
    }

    /**
     * Removes any faction stuff from the player
     */
    public void clearFaction(){
        this.faction = null;
        this.factionRank = null;
        factionChat = FactionChatMode.NORMAL;
    }

    /**
     * Adds the given amount of power to the player, clamped below their maximum power
     * @param Power The amount of power to add to the player
     */
    public void addPower(int Power){
        power.setPower(Power + power.power);
    }

    /**
     * Adds the given amount of power to the player's max power
     * @param MaxPower The amount of max power to add to the player
     */
    public void addMaxPower(int MaxPower){
        if (MaxPower + power.maxPower > FactionConfig.playerSubCat.playerMaxPowerCap) power.maxPower = FactionConfig.playerSubCat.playerMaxPowerCap;
        else power.maxPower = MaxPower + power.maxPower;
    }

    public String printPlayerInfo(UUID askingFaction){
        FactionManager fMan = FactionManager.getInstance();

        String relationColour = fMan.getRelationColour(askingFaction, faction);

        StringBuilder message = new StringBuilder();

        message.append(DemConstants.TextColour.INFO).append("======").append(FactionConstants.TextColour.OWN).append(lastKnownName).append(DemConstants.TextColour.INFO).append("======\n");
        message.append(DemConstants.TextColour.INFO).append("Faction: ").append(TextFormatting.RESET).append(relationColour).append(faction != FactionManager.WILDID ? fMan.getFaction(faction).name : "N/A").append("\n");
        message.append(DemConstants.TextColour.INFO).append("Rank: ").append(factionRank != null ? FactionRank.getFactionRankString(factionRank) : "N/A").append("\n");
        if (lastOnline == 0L) {
            message.append(DemConstants.TextColour.INFO).append("Last Online: ").append(DemUtils.displayAge(DemUtils.calculateAge(lastOnline) / 60000)).append(" ago").append("\n");
        }
        message.append(DemConstants.TextColour.INFO).append("Personal Power: ").append(power.power).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Personal Max Power: ").append(power.maxPower);

        return message.toString();
    }
}

