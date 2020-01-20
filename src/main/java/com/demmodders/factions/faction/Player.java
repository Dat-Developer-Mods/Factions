package com.demmodders.factions.faction;

import java.util.UUID;

public class Player {
    public UUID faction;
    public String factionRank;
    public Power power;
    public transient UUID lastFactionLand;

    public Player(){
        faction = null;
        factionRank = "";
        power = new Power();
    }
    public Player(UUID Faction, String FactionRank, Power power){
        this.faction = Faction;
        this.factionRank = FactionRank;
        this.power = power;
    }
}
