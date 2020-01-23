package com.demmodders.factions.faction;

import com.demmodders.factions.Factions;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FileHelper;
import com.demmodders.factions.util.Utils;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.Power;
import com.demmodders.factions.util.structures.Relationship;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.management.relation.Relation;
import java.io.*;
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
        // Load faction details
        LOGGER.info(Factions.MODID + " Loading Factions");
        LOGGER.debug(Factions.MODID + " Loading Faction data");
        loadFactions();
        LOGGER.debug(Factions.MODID + " Loading Player data");
        loadPlayers();
        LOGGER.debug(Factions.MODID + " Loading Claimed Chunks data");
        loadClaimedChunks();

        // Calculate metadata that we haven't saved
        LOGGER.debug(Factions.MODID + " Adding players to factions");
        addPlayersToFactions();

        LOGGER.debug(Factions.MODID + " Adding chunks to factions");
        addLandToFactions();

        LOGGER.debug(Factions.MODID + " Adding invites to players");
        addInvitesToPlayers();
    }

    // Faction Objects
    private HashMap<UUID, Faction> FactionMap = new HashMap<>();
    private HashMap<UUID, Player> PlayerMap = new HashMap<>();
    private HashMap<Integer, HashMap<String, UUID>> ClaimedLand = new HashMap<>();

    // Getters
    /**
     * Gets the faction object that has the given ID
     * @param ID The ID of the faction
     * @return The faction object of the ID
     */
    public Faction getFaction(UUID ID){
        return FactionMap.getOrDefault(ID, null);
    }

    /**
     * Gets the ID of the faction that has the given name
     * @param Name the name of the faction
     * @return The UUID of the faction with the given name
     */
    @Nullable
    public UUID getFactionIDFromName(String Name){
        for (UUID factionID : FactionMap.keySet()){
            if (FactionMap.get(factionID).name.toLowerCase().equals(Name)){
                return factionID;
            }
        }
        return null;
    }

    /**
     * Gets the Player object that has the given ID
     * @param ID the ID of the player
     * @return the player object of the ID
     */
    public Player getPlayer(UUID ID){
        return PlayerMap.getOrDefault(ID, null);
    }

    /**
     * Gets the ID of the player object that has the given name
     * @param Name the name of the player
     * @return The player with the given name, null if no player of that name is known
     */
    @Nullable
    public UUID getPlayerIDFromName(String Name){
        for (UUID playerID : PlayerMap.keySet()){
            if (PlayerMap.get(playerID).lastKnownName.equals(Name)){
                return playerID;
            }
        }
        return null;
    }

    // Setters
    /**
     * Sets the last known name of the player
     * @param PlayerID the ID of the player being updated
     * @param Name the new name of the player
     */

    public void setPlayerLastKnownName(UUID PlayerID, String Name){
        PlayerMap.get(PlayerID).lastKnownName = Name;
        savePlayer(PlayerID);
    }

    /**
     * Sets the faction of the given player
     * @param PlayerID the player who's faction is being change
     * @param FactionID the faction the player is being added to
     */
    public void setPlayerFaction(UUID PlayerID, UUID FactionID){
        UUID faction = PlayerMap.get(PlayerID).faction;
        if (faction != null){
            FactionMap.get(faction).removePlayer(PlayerID);
        }

        PlayerMap.get(PlayerID).faction = FactionID;
        if (FactionID != null) FactionMap.get(FactionID).addPlayer(PlayerID);
        savePlayer(PlayerID);
    }

    public void setPlayerRank(UUID PlayerID, FactionRank Rank){
        PlayerMap.get(PlayerID).factionRank = Rank;
        savePlayer(PlayerID);
    }

    // Utilities
    // Factions
    /**
     * Attempts to invite the given player to the given faction
     * @param PlayerID The player being invited
     * @param FactionID The faction the player is invited to
     * @return Whether the player was successfully invited to the faction
     */
    public boolean invitePlayerToFaction(UUID PlayerID, UUID FactionID){
        if (!FactionMap.get(FactionID).invites.contains(PlayerID)) {
            FactionMap.get(FactionID).invites.add(PlayerID);
            PlayerMap.get(PlayerID).invites.add(FactionID);
            removePlayerInvite(PlayerID, FactionID);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove the any invites to the player if they have any
     * @param PlayerID The player who's getting the invite
     * @param FactionID The faction the player's invited to
     * @return Whether there were any invites to remove
     */
    public boolean removePlayerInvite(UUID PlayerID, UUID FactionID){
        boolean removed = false;
        if (FactionMap.get(FactionID).invites.contains(PlayerID)) {
            FactionMap.get(FactionID).invites.remove(PlayerID);
            removed = true;
        }
        if (PlayerMap.get(PlayerID).invites.contains(FactionID)) {
           PlayerMap.get(PlayerID).invites.remove(FactionID);
            removed = true;
        }
        return removed;
    }

    /**
     * Iterates through all the players that factions is aware of and gives a reference to them to their owning faction
     */
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

    /**
     * Iterates through all the claimed land and gives references of them to their owning factions
     */
    private void addLandToFactions(){
        boolean pruned;
        for (int dim : ClaimedLand.keySet()){
            pruned = false;
            for (String land : ClaimedLand.get(dim).keySet()){
                if (FactionMap.containsKey(ClaimedLand.get(dim).get(land))) {
                    FactionMap.get(ClaimedLand.get(dim).get(land)).addLandToFaction(dim, land);
                } else {
                    LOGGER.warn("Discovered land owned by a faction that doesn't exist, removing owner");
                    ClaimedLand.get(dim).remove(land);
                    pruned = true;
                }
            }
            if (pruned){
                saveClaimedChunks(dim);
            }
        }
    }

    /**
     * Iterates through all the factions that factions and add's their invites to the invited players
     */
    private void addInvitesToPlayers(){
        for (UUID factionID : FactionMap.keySet()){
            for (UUID playerID : FactionMap.get(factionID).invites){
                PlayerMap.get(playerID).invites.add(factionID);
            }
        }
    }

    /**
     * Gets a list of all the factions in the game
     * @return A list of all the factions
     */
    public List<Faction> getListOfFactions(){
        return new ArrayList<Faction>(FactionMap.values());
    }

    /**
     * Gets a list of all IDs of the factions in the game
     * @return A list of all the faction IDs
     */
    public List<UUID> getListOfFactionsUUIDs(){
        return new ArrayList<UUID>(FactionMap.keySet());
    }

    // Players
    /**
     * Checks if a player is registered to the factions system
     * @param PlayerID The ID of the player
     * @return True if the player is registered with factions
     */
    public boolean isPlayerRegistered(UUID PlayerID){
        return PlayerMap.containsKey(PlayerID);
    }

    /**
     * Checks whether the given player can join the given faction
     * @param PlayerID The player trying to join the faction
     * @param FactionID The faction the player is trying to join
     * @return Whether the player can join the given faction
     */
    public boolean canAddPlayerToFaction(UUID PlayerID, UUID FactionID){
        Faction faction = FactionMap.get(FactionID);
        if (faction.hasFlag("Open")) return true;
        else if (faction.invites.contains(PlayerID)) return true;
        else return false;
    }

    /**
     * Gets the faction that the given player belongs to
     * @param PlayerID The ID of the player
     * @return The faction object the player belongs to
     */
    @Nullable
    public Faction getPlayersFaction(UUID PlayerID){
        UUID factionID = getPlayersFactionID(PlayerID);
        if (factionID != null){
            return FactionMap.get(factionID);
        }
        return null;
    }

    /**
     * Gets the ID of the faction that owns the given player
     * @param playerID The ID of the player
     * @return The UUID of the faction that owns the player
     */
    public UUID getPlayersFactionID(UUID playerID){
        return PlayerMap.get(playerID).faction;
    }

    // Chunks
    /**
     * Generates the key for the chunk that is used to identify it in the factions system
     * @param ChunkX The X coordinate of the chunk
     * @param ChunkZ The Z coordinate of the chunk
     * @return the key used to identify the chunk to factions
     */
    public static String makeChunkKey(int ChunkX, int ChunkZ){
        return String.valueOf(ChunkX) + ", " + String.valueOf(ChunkZ);
    }


    /**
     * Gets the owner of the chunk at the given coordinates
     * @param Dim The dimension containing the chunk
     * @param ChunkX The X coordinate of the chunk
     * @param ChunkZ The Z coordinate of the chunk
     * @return The UUID of the faction that owns the chunk
     */
    @Nullable
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
    /**
     * Creates a faction
     * @param Name The name of the faction
     * @param PlayerID The ID of the player who's creating the faction
     * @return The result of creating the faction (0 for success, 1 for name too long, 2 for name too short, 3 for faction exists)
     */
    public int createFaction(String Name, UUID PlayerID) {
        if (Name.length() > FactionConfig.factionSubCat.maxFactionNameLength) {
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
        PlayerMap.get(PlayerID).factionRank = FactionRank.OWNER;
        savePlayer(PlayerID);

        return 0;
    }

    /**
     * Sends the given message to all the online members of the given faction
     * @param FactionID The ID of the faction
     * @param Message The message to send to all the users
     */
    public void sendFactionwideMessage(UUID FactionID, ITextComponent Message){
        for(UUID playerID : FactionMap.get(FactionID).members){
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerID);
            if (player != null){
                player.sendMessage(Message);
            }
        }
    }

    /**
     * Removes all land owned by a faction
     * @param FactionID The ID of the faction
     */
    public void releaseAllFactionLand(UUID FactionID){
        for (int dim : ClaimedLand.keySet()){
            for (String land : ClaimedLand.get(dim).keySet()){
                if (ClaimedLand.get(dim).get(land) == FactionID) {
                    ClaimedLand.get(dim).remove(land);
                }
            }
            saveClaimedChunks(dim);
        }
    }

    /**
     * Attempts to claim a chunk for the given faction
     * @param FactionID The faction claiming the chunk
     * @param Dim The dimention the chunk is in
     * @param ChunkX The X Coord of the chunk
     * @param ChunkZ The Z Coord of the chunk
     * @return The result of the claim (0: Success, 1:Success, but stolen, 2: Not enough power, 3: Must touch other land, 4: Faction owns it, 5: You own it)
     */
    public int claimLand(UUID FactionID, int Dim, int ChunkX, int ChunkZ){
        // Check they can afford it
        if (!FactionMap.get(FactionID).calculateLandCost(1)) return 2;

        String chunkKey = makeChunkKey(ChunkX, ChunkZ);

        // Check it isn't owned, or owned by someone who can't afford it
        boolean owned = false;
        if (ClaimedLand.containsKey(Dim)) {
            if (ClaimedLand.get(Dim).containsKey(chunkKey)){
                owned = true;
                UUID owner = ClaimedLand.get(Dim).get(chunkKey);

                if (owner == FactionID) return 5;
                if ((FactionMap.get(owner).calculatePower() >= FactionMap.get(owner).calculateLandCost())) {
                    return 4;
                }
            }
        } else {
            // Create dimension
            ClaimedLand.put(Dim, new HashMap<>());
        }

        // Check the land is connected
        if (FactionConfig.landSubCat.landRequireConnect && !(FactionConfig.landSubCat.landRequireConnectWhenStealing && owned) && !getFaction(FactionID).checkLandTouches(Dim, ChunkX, ChunkZ)) return 3;

        ClaimedLand.get(Dim).put(chunkKey, FactionID);
        saveClaimedChunks(Dim);
        FactionMap.get(FactionID).addLandToFaction(Dim, chunkKey);
        return (owned ? 1 : 0);
    }

    /**
     * Adds the other faction as an ally to the given faction
     * @param FactionID The faction creating the alliance
     * @param otherFaction the faction the alliance is with
     * @return the result of the alliance (0: Success them pending, 1: Success both allies, 2: Already allies
     */
    public int addAlly(UUID FactionID, UUID otherFaction){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.get(otherFaction);
        if (currentRelation.relation == RelationState.ALLY) return 2;
        FactionMap.get(FactionID).relationships.put(otherFaction, new Relationship(RelationState.ALLY));
        if (!FactionMap.get(otherFaction).relationships.containsKey(FactionID)) {
            sendFactionwideMessage(otherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " Has made you their allies" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, but they can't build on yours" : "") + ", add them back with /faction ally" + FactionMap.get(FactionID).name));
            return 1;
        } else {
            FactionMap.get(otherFaction).relationships.put(FactionID, new Relationship(RelationState.PENDINGALLY));
            sendFactionwideMessage(otherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " Have added you as their allies as well" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land" : "")));
            return 0;
        }
    }

    public int addEnemy(UUID FactionID, UUID otherFaction){

    }

    // Player Functions
    /**
     * Registers a player to the factions system
     * @param Player The player object to register
     */
    public void registerPlayer(EntityPlayer Player){
        if(isPlayerRegistered(Player.getUniqueID())) {
            return;
        }
        PlayerMap.put(Player.getUniqueID(), new Player(null, null, new Power(FactionConfig.playerSubCat.playerStartingPower, FactionConfig.playerSubCat.playerStartingMaxPower), Player.getName()));
        savePlayer(Player.getUniqueID());
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
