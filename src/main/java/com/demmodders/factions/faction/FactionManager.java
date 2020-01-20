package com.demmodders.factions.faction;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.UUID;

public class FactionManager {
    // Singleton
    private static FactionManager instance = null;
    public static FactionManager getInstance(){
        if (instance == null){
            instance = new FactionManager();
        }
        return instance;
    }

    // Faction Objects
    private HashMap<UUID, Faction> Factions = new HashMap<>();
    private HashMap<UUID, Player> Players = new HashMap<>();
    private HashMap<String, HashMap<String, UUID>> ClaimedLand = new HashMap<>();

    public UUID getFactionIDFromName{
        
    }

    public int createFaction(String Name){

    }

    public void loadFaction(UUID id){
        Gson gson = new Gson();

    }
}
