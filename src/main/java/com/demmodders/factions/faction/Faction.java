package com.demmodders.factions.faction;

import com.demmodders.factions.util.Location;

public class Faction {
    public String name;
    public String desc;
    public Location homePos;
    public Power power;

    Faction(){

    }

    public Faction(String name){
        this.name = name;
    }
}


