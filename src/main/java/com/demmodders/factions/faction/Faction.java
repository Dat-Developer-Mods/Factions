package com.demmodders.factions.faction;

import com.demmodders.factions.util.structures.Location;
import com.demmodders.factions.util.structures.Relationship;

import java.util.ArrayList;
import java.util.UUID;

public class Faction {
    public String name;
    public String desc;
    public String motd;
    public Location homePos;
    public Power power;
    public ArrayList<Relationship> relationships = new ArrayList<>();
    public transient ArrayList<UUID> members;

    Faction(){

    }

    public Faction(String name, UUID playerID){
        this.name = name;
        this.members = new ArrayList<>();
        this.members.add(playerID);
    }
}


