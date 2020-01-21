package com.demmodders.factions.faction;

import com.demmodders.factions.Factions;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FileHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        LOGGER.info(Factions.MODID + " Loading Factions");
        LOGGER.debug(Factions.MODID + " Loading Faction data");
        loadFactions();
        LOGGER.debug(Factions.MODID + " Loading Player data");
        loadPlayers();
        LOGGER.debug(Factions.MODID + " Loading Claimed Chunks data");
        loadClaimedChunks();

        LOGGER.debug(Factions.MODID + " Adding players to factions");
        addPlayersToFactions();
    }

    // Faction Objects
    private HashMap<UUID, Faction> FactionMap = new HashMap<>();
    private HashMap<UUID, Player> PlayerMap = new HashMap<>();
    private HashMap<Integer, HashMap<String, UUID>> ClaimedLand = new HashMap<>();

    // Getters
    public Faction getFaction(UUID ID){
        return FactionMap.getOrDefault(ID, null);
    }

    public UUID getFactionIDFromName(String Name){
        for (UUID factionID : FactionMap.keySet()){
            if (FactionMap.get(factionID).name.equals(Name)){
                return factionID;
            }
        }
        return null;
    }

    public Player getPlayer(UUID ID){
        return PlayerMap.getOrDefault(ID, null);
    }

    public UUID getPlayerIDFromName(String Name){
        for (UUID playerID : PlayerMap.keySet()){
            if (FactionMap.get(playerID).name.equals(Name)){
                return playerID;
            }
        }
        return null;
    }

    // Utilities
    // Factions
    private void addPlayersToFactions(){
        for (UUID playerID : PlayerMap.keySet()){
            UUID factionID = PlayerMap.get(playerID).faction;
            if(FactionMap.containsKey(factionID)){
                FactionMap.get(PlayerMap.get(playerID).faction).members.add(playerID);
            } else {
                LOGGER.warn(Factions.MODID + " Player references faction that doesn't exist, removing reference");
                PlayerMap.get(playerID).clearFaction();
                savePlayer(playerID);
            }
        }
    }

    public List<Faction> getListOfFactions(){
        return new ArrayList<Faction>(FactionMap.values());
    }

    public List<UUID> getListOfFactionsUUIDs(){
        return new ArrayList<UUID>(FactionMap.keySet());
    }

    // Players
    public boolean isPlayerRegistered(UUID PlayerID){
        return PlayerMap.containsKey(PlayerID);
    }

    public Faction getPlayersFaction(UUID playerID){
        UUID factionID = PlayerMap.get(playerID).faction;
        if (factionID != null){
            return FactionMap.get(factionID);
        }
        return null;
    }

    public UUID getPlayersFactionID(UUID playerID){
        return PlayerMap.get(playerID).faction;
    }

    // Chunks
    public static String makeChunkKey(int ChunkX, int ChunkZ){
        return String.valueOf(ChunkX) + ", " + String.valueOf(ChunkZ);
    }

    public UUID getChunkOwningFaction(int Dim, int ChunkX, int ChunkZ){
        if (ClaimedLand.containsKey(Dim)){
            String chunkKey = makeChunkKey(ChunkX, ChunkZ);
            if (ClaimedLand.get(Dim).containsKey(chunkKey)){
                return ClaimedLand.get(Dim).get(chunkKey);
            }
        }
        return null;
    }

    // Faction Functions
    public int createFaction(String Name, UUID PlayerID) {
        if (Name.length() > FactionConfig.maxFactionNameLength) {
            LOGGER.warn(Factions.MODID + " Failed to create faction, name too long");
            return 1;
        } else if (Name.length() < 1){
            LOGGER.warn(Factions.MODID + " Failed to create faction, name too short");
            return 2;
        } else if(getFactionIDFromName(Name) != null){
            LOGGER.warn(Factions.MODID + " Failed to create faction, Faction already exists");
            return 3;
        }
        UUID factionID = UUID.randomUUID();
        FactionMap.put(factionID, new Faction(Name, PlayerID));

        saveFaction(factionID);

        PlayerMap.get(PlayerID).faction = factionID;
        PlayerMap.get(PlayerID).factionRank = "Owner";
        savePlayer(PlayerID);

        return 0;
    }

    // Player Functions
    public void registerPlayer(UUID PlayerID){
        if(isPlayerRegistered(PlayerID)) {
            return;
        }
        PlayerMap.put(PlayerID, new Player(null, null, new Power(FactionConfig.playerStartingPower, FactionConfig.playerStartingMaxPower)));
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
        if (PlayerMap.containsKey(PlayerID)){
            Gson gson = new Gson();
            File playerFile = FileHelper.openFile(new File(FileHelper.getPlayerDir(), PlayerID.toString()));
            if (playerFile == null){
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(playerFile));
                String json = gson.toJson(PlayerMap.get(PlayerID));
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveClaimedChunks(int dim){
        if (ClaimedLand.containsKey(dim)){
            Gson gson = new Gson();
            File dimFile = FileHelper.openFile(new File(FileHelper.getClaimedDir(), String.valueOf(dim)));
            if (dimFile == null){
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(dimFile));
                String json = gson.toJson(ClaimedLand.get(dim));
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
        File[] players = FileHelper.getFactionsDir().listFiles();
        if (players != null) {
            for (File player : players){
                loadPlayer(player);
            }
        }
    }

    public void loadPlayer(File playerFile){
        Gson gson = new Gson();
        try {
            Reader reader = new FileReader(playerFile);
            Player playerObject = gson.fromJson(reader, Player.class);
            if (playerObject != null){
                PlayerMap.put(UUID.fromString(playerFile.getName()), playerObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayer(UUID id){
        File theFile = new File(FileHelper.getPlayerDir(), id.toString());
        if (theFile.exists()) loadFaction(theFile);
    }

    public void loadClaimedChunks(){
        File[] dims = FileHelper.getClaimedDir().listFiles();
        if (dims != null) {
            for (File dim : dims){
                loadClaimedChunkDim(dim);
            }
        }
    }

    public void loadClaimedChunkDim(File dimFile){
        Gson gson = new Gson();
        try {
            Reader reader = new FileReader(dimFile);
            Type typeOfHashMap = new TypeToken<HashMap<String, UUID>>(){}.getType();
            HashMap<String, UUID> dimChunks = gson.fromJson(reader, typeOfHashMap);
            ClaimedLand.put(Integer.parseInt(dimFile.getName()), dimChunks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadClaimedChunkDim(String dim){
        File theFile = new File(FileHelper.getClaimedDir(), dim);
        if (theFile.exists()) loadClaimedChunkDim(theFile);
    }


}
