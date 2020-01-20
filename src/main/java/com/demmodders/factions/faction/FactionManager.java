package com.demmodders.factions.faction;

import com.demmodders.factions.Factions;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FileHelper;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class FactionManager {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    // Singleton
    private static FactionManager instance = null;
    public static FactionManager getInstance(){
        if (instance == null){
            instance = new FactionManager();
        }
        return instance;
    }

    FactionManager(){
        loadFactions();
        loadPlayers();
        loadClaimedChunks();
    }

    // Faction Objects
    private HashMap<UUID, Faction> FactionMap = new HashMap<>();
    private HashMap<UUID, Player> Players = new HashMap<>();
    private HashMap<String, HashMap<String, UUID>> ClaimedLand = new HashMap<>();

    // Utilities
    public UUID getFactionIDFromName(String Name){
        for(UUID key: FactionMap.keySet()){
            if(FactionMap.get(key).name.equals(Name)){
                return key;
            }
        }
        return null;
    }

    public boolean isPlayerRegistered(UUID PlayerID){
        return Players.containsKey(PlayerID);
    }

    // Faction Functions
    public int createFaction(String Name, UUID PlayerID) {
        if (Name.length() > FactionConfig.maxFactionNameLength) {
            return 1;
        } else if (Name.length() < 1){
            return 2;
        } else if(getFactionIDFromName(Name) != null){
            return 3;
        }
        UUID factionID = UUID.randomUUID();
        FactionMap.put(factionID, new Faction(Name));

        saveFaction(factionID);

        Players.get(PlayerID).faction = factionID;
        Players.get(PlayerID).factionRank = "Owner";
        savePlayer(PlayerID);

        return 0;
    }

    // Player Functions
    public void registerPlayer(UUID PlayerID){
        if(isPlayerRegistered(PlayerID)) return;
        Players.put(PlayerID, new Player(null, null, new Power(FactionConfig.playerStartingPower, FactionConfig.playerStartingMaxPower)));
        savePlayer(PlayerID);
    }

    // IO Functions
    // Save
    public void saveFaction(UUID FactionID){
        if (FactionMap.containsKey(FactionID)) {
            Gson gson = new Gson();
            File factionFile = FileHelper.openFile(new File(FileHelper.getFactionsDir(), FactionID.toString()));
            if (factionFile == null){
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(factionFile));
                String json = gson.toJson(FactionMap.get(FactionID));
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePlayer(UUID PlayerID){
        if (Players.containsKey(PlayerID)){
            Gson gson = new Gson();
            File playerFile = FileHelper.openFile(new File(FileHelper.getPlayerDir(), PlayerID.toString()));
            if (playerFile == null){
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(playerFile));
                String json = gson.toJson(Players.get(PlayerID));
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Load
    public void loadFactions(){
        File[] factions = FileHelper.getFactionsDir().listFiles();
        if (factions != null) {
            for (File faction : factions){
                loadFaction(faction);
            }
        }
    }

    public void loadFaction(File factionFile){
        Gson gson = new Gson();
        try {
            Reader reader = new FileReader(factionFile);
            Faction factionObject = gson.fromJson(reader, Faction.class);
            if (factionObject != null){
                FactionMap.put(UUID.fromString(factionFile.getName()), factionObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFaction(UUID id){
        File theFile = new File(FileHelper.getFactionsDir(), id.toString());
        if (theFile.exists()) loadFaction(theFile);
    }

    public void loadPlayers(){

    }

    public void loadplayer(UUID id){
        Gson gson = new Gson();

    }
    public void loadClaimedChunks(){

    }
}
