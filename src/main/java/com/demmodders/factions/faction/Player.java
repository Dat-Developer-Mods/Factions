package com.demmodders.factions.faction;

import com.demmodders.factions.util.enums.FactionChatMode;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.structures.Power;

import java.util.ArrayList;
import java.util.UUID;

public class Player {
    public UUID faction = null;
    public FactionRank factionRank = null;
    public Power power = null;
    public String lastKnownName = "";
    public transient UUID lastFactionLand  = null;
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

    /**
     * Removes any faction stuff from the player
     */
    public void clearFaction(){
        this.faction = null;
        this.factionRank = null;
        factionChat = FactionChatMode.NORMAL;
    }
}

