package com.demmodders.factions.faction;

import com.demmodders.factions.util.structures.Location;

import java.util.ArrayList;
import java.util.UUID;

public class Faction {
    public String name;
    public String desc;
    public String motd;
    public Location homePos;
    public Power power;
    public transient ArrayList<UUID> members;

    Faction(){

    }

    public Faction(String name){
        this.name = name;
    }
}


