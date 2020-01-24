package com.demmodders.factions.faction;

import com.demmodders.factions.Factions;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FileHelper;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.ChunkLocation;
import com.demmodders.factions.util.structures.Location;
import com.demmodders.factions.util.structures.Power;
import com.demmodders.factions.util.structures.Relationship;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FactionManager {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);
    public static final UUID WILDID = new UUID(0L, 0L);
    public static final UUID SAFEID = new UUID(0L, 1L);
    public static final UUID WARID = new UUID(0L, 2L);

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

        LOGGER.debug(Factions.MODID + " Loading Default faction data");
        loadDefaultFactions();


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

    @Nullable
    public static EntityPlayerMP getPlayerMPFromUUID(UUID PlayerID){
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(PlayerID);
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
        UUID oldFaction = PlayerMap.get(PlayerID).faction;
        if (oldFaction != null){
            FactionMap.get(oldFaction).removePlayer(PlayerID);
        }

        PlayerMap.get(PlayerID).faction = FactionID;
        PlayerMap.get(PlayerID).factionRank = FactionRank.GRUNT;
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
            saveFaction(FactionID);
            getPlayerMPFromUUID(PlayerID).sendMessage(new TextComponentString(TextFormatting.GOLD + FactionMap.get(FactionID).name + " Has invited you to join!, accept their invite with /faction join " + TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name));
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
        if ((FactionConfig.factionSubCat.maxMembers == 0 || faction.members.size() < FactionConfig.factionSubCat.maxMembers) && (faction.invites.contains(PlayerID) || faction.hasFlag("Open"))) return true;
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
            EntityPlayerMP player = getPlayerMPFromUUID(playerID);
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
        String chunkKey = makeChunkKey(ChunkX, ChunkZ);

        // Check it isn't owned, or owned by someone who can't afford it
        boolean owned = false;
        if (ClaimedLand.containsKey(Dim)) {
            if (ClaimedLand.get(Dim).containsKey(chunkKey)){
                owned = true;
                UUID owner = ClaimedLand.get(Dim).get(chunkKey);

                if (owner == FactionID) return 5;
                if (FactionMap.get(owner).hasFlag("StrongBorders") || FactionMap.get(owner).calculatePower() >= FactionMap.get(owner).checkCanAffordLand()) {
                    return 4;
                }
            }
        } else {
            // Create dimension
            ClaimedLand.put(Dim, new HashMap<>());
        }
        // Check they can afford it
        if (!(FactionMap.get(FactionID).checkCanAffordLand(1)) && !(FactionMap.get(FactionID).hasFlag("UnlimitedLand"))) return 2;

        // Check the land is connected
        if (FactionConfig.landSubCat.landRequireConnect && !(FactionConfig.landSubCat.landRequireConnectWhenStealing && owned) && !getFaction(FactionID).checkLandTouches(Dim, ChunkX, ChunkZ)) return 3;

        ClaimedLand.get(Dim).put(chunkKey, FactionID);
        saveClaimedChunks(Dim);
        FactionMap.get(FactionID).addLandToFaction(Dim, chunkKey);
        return (owned ? 1 : 0);
    }

    /**
     * Attempts to add the other faction as an ally to the given faction
     * @param FactionID The faction creating the alliance
     * @param OtherFaction the faction the alliance is with
     * @return The result of the alliance (0: Success them pending, 1: Success both allies, 2: Success but enemy, 3: Already allies, 4: That's you, 5: No)
     */
    public int addAlly(UUID FactionID, UUID OtherFaction){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.getOrDefault(OtherFaction, null);
        if (FactionID == OtherFaction) return 4;
        if (FactionMap.get(FactionID).hasFlag("Unrelateable")) return 5;
        if (currentRelation != null && currentRelation.relation == RelationState.ALLY) return 3;
        FactionMap.get(FactionID).relationships.put(OtherFaction, new Relationship(RelationState.ALLY));
        saveFaction(FactionID);
        if (!FactionMap.get(OtherFaction).relationships.containsKey(FactionID)) {
            FactionMap.get(OtherFaction).relationships.put(FactionID, new Relationship(RelationState.PENDINGALLY));
            saveFaction(OtherFaction);
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has made you their allies" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, but they can't build on yours" : "") + ", add them back with /faction ally " + FactionMap.get(FactionID).name));
            return 1;
        } else if (FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.ALLY){
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has added you as their allies as well" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land" : "")));
            return 0;
        } else {
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has added you as their allies," + TextFormatting.DARK_RED + " you still think they're enemies though"));
            return 2;
        }
    }

    /**
     * Attempts to add the other faction as an enemy to the given faction
     * @param FactionID The faction declaring the enemy
     * @param OtherFaction The faction becoming an enemy
     * @return The result of the declaration (0: Success, 1: Success enemies all round, 2: Success but you're an ally to them, 3: already enemies, 4: That's you, 5: No)
     */
    public int addEnemy(UUID FactionID, UUID OtherFaction){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.getOrDefault(OtherFaction, null);
        if (FactionID == OtherFaction) return 4;
        if (FactionMap.get(OtherFaction).hasFlag("Unrelateable")) return 5;
        if (currentRelation != null && currentRelation.relation == RelationState.ENEMY) return 3;
        FactionMap.get(FactionID).relationships.put(OtherFaction, new Relationship(RelationState.ENEMY));
        saveFaction(FactionID);
        if (!FactionMap.get(OtherFaction).relationships.containsKey(FactionID) || FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.PENDINGALLY) {
            FactionMap.get(OtherFaction).relationships.put(FactionID, new Relationship(RelationState.PENDINGENEMY));
            saveFaction(OtherFaction);
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_RED + FactionMap.get(FactionID).name + " has declared you an enemy" + (FactionConfig.factionSubCat.enemyBuild ? ", this means they can build on your land, and you can build on theirs" : "") + ", let them know you got the message with with /faction enemy " + FactionMap.get(FactionID).name));
            return 0;
        } else if (FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.ALLY){
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_RED + FactionMap.get(FactionID).name + " has declared you an enemy" + (FactionConfig.factionSubCat.enemyBuild ? ", this means they can build on your land, and you can build on theirs" : "") + ", but you still think they're an ally, let them know you got the message with /faction enemy " + FactionMap.get(FactionID).name));
            return 2;
        } else {
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_RED + FactionMap.get(FactionID).name + " has declared you an enemy as well, you are now at war"));
            return 1;
        }
    }

    /**
     * Adds the given the faction as a neutral faction
     * @param FactionID The faction becoming neutral
     * @param OtherFaction The faction to become neutral with
     * @return The result of the declaration (0: Success, 1: Removed enemy, 2: Removed ally, 3: cannot deny request, 4: no relation, 5: that's you
     */
    public int addNeutral(UUID FactionID, UUID OtherFaction){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.getOrDefault(OtherFaction, null);
        if (FactionID == OtherFaction) return 5;
        if (currentRelation == null) return 4;
        if (currentRelation.relation == RelationState.PENDINGALLY || currentRelation.relation == RelationState.PENDINGENEMY) return 3;
        FactionMap.get(FactionID).relationships.remove(OtherFaction);
        saveFaction(FactionID);
        if (FactionMap.get(OtherFaction).relationships.containsKey(FactionID)) {
            switch (FactionMap.get(OtherFaction).relationships.get(FactionID).relation){
                case ALLY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.GOLD + FactionMap.get(FactionID).name + " is no longer your Ally, however you still regard them as one" + (FactionConfig.factionSubCat.allyBuild ? ", this means they can build on your land, but you can't build on theirs" : "") + ", you can remove them as allies with /faction neutral " + FactionMap.get(FactionID).name));
                    break;
                case ENEMY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.GOLD + FactionMap.get(FactionID).name + " is no longer your enemy, however you still regard them as one" + (FactionConfig.factionSubCat.enemyBuild ? ", this means they you can still build on each other's land" : "") + ", you can remove them as an enemy with /faction neutral " + FactionMap.get(FactionID).name));
                    break;
                case PENDINGALLY:
                case PENDINGENEMY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.GOLD + FactionMap.get(FactionID).name + " No longer wants to be your " + (FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.PENDINGALLY ? "ally, " + (FactionConfig.factionSubCat.allyBuild ? "you can no longer build on their land" : "") : "enemy" + (FactionConfig.factionSubCat.enemyBuild ? "you can no longer build on each other's land" : ""))));
                    FactionMap.get(OtherFaction).relationships.remove(FactionID);
                    saveFaction(OtherFaction);
                    break;
            }
        }
        switch (currentRelation.relation){
            case ALLY:
                return 2;
            case ENEMY:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Attempts to set the faction home to the given position
     * @param FactionID The ID of the faction creating the home
     * @param position The position of the home
     * @return Whether the claim was a success
     */
    public boolean setFactionHome(UUID FactionID, Location position){
        ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(position.dim, position.x, position.z);
        String chunkKey = makeChunkKey(chunk.x, chunk.z);
        if (ClaimedLand.containsKey(position.dim) && ClaimedLand.get(position.dim).containsKey(chunkKey)){
            if (ClaimedLand.get(position.dim).get(chunkKey) == FactionID) {
                FactionMap.get(FactionID).homePos = position;
                return true;
            }
        }
        return false;
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
            File factionFile = FileHelper.openFile(new File(FileHelper.getFactionsDir(), FactionID.toString()  + ".json"));
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
            File playerFile = FileHelper.openFile(new File(FileHelper.getPlayerDir(), PlayerID.toString()  + ".json"));
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
            File dimFile = FileHelper.openFile(new File(FileHelper.getClaimedDir(), dim + ".json"));
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

    // Generate default factions
    public void generateWild(){
        ArrayList<String> flags = new ArrayList<>();
        flags.add("Default");
        flags.add("Permanent");
        flags.add("StrongBorders");
        flags.add("InfinitePower");
        flags.add("UnlimitedLand");
        flags.add("Unrelateable");
        Faction wild = new Faction("Wild", "Everywhere that isn't owned by a faction", flags);
        FactionMap.put(WILDID, wild);
        saveFaction(WILDID);
    }

    public void generateSafeZone(){
        ArrayList<String> flags = new ArrayList<>();
        flags.add("Default");
        flags.add("Permanent");
        flags.add("StrongBorders");
        flags.add("InfinitePower");
        flags.add("UnlimitedLand");
        flags.add("Unrelateable");
        flags.add("NoDamage");
        Faction wild = new Faction("Safe Zone", "You're safe here", flags);
        FactionMap.put(SAFEID, wild);
        saveFaction(SAFEID);
    }

    public void generateWarZone(){
        ArrayList<String> flags = new ArrayList<>();
        flags.add("Default");
        flags.add("Permanent");
        flags.add("StrongBorders");
        flags.add("InfinitePower");
        flags.add("UnlimitedLand");
        flags.add("Unrelateable");
        flags.add("BonusPower");
        Faction wild = new Faction("War Zone", "You're not safe here, you will lose more power when you die, but will gain more power when you kill", flags);
        FactionMap.put(WARID, wild);
        saveFaction(WARID);
    }

    // Load
    public void loadDefaultFactions(){
        File factionFile;
        // Wild
        factionFile = new File(FileHelper.getDefaultFactionDir(), "Wild.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, WILDID);
        } else {
            generateWild();
        }

        // SafeZone
        factionFile = new File(FileHelper.getDefaultFactionDir(), "SafeZone.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, SAFEID);
        } else {
            generateSafeZone();
        }

        // WarZone
        factionFile = new File(FileHelper.getDefaultFactionDir(), "WarZone.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, WARID);
        } else {
            generateWarZone();
        }
    }

    public void loadFactions(){
        File[] factions = FileHelper.getFactionsDir().listFiles();
        if (factions != null) {
            for (File faction : factions){
                loadFaction(faction);
            }
        }
    }

    public void loadFaction(File factionFile){
        loadFaction(factionFile, UUID.fromString(FileHelper.getBaseName(factionFile.getName())));
    }

    public void loadFaction(File factionFile, UUID ID){
        Gson gson = new Gson();
        try {
            Reader reader = new FileReader(factionFile);
            Faction factionObject = gson.fromJson(reader, Faction.class);
            if (factionObject != null){
                FactionMap.put(ID, factionObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFaction(UUID id){
        File theFile = new File(FileHelper.getFactionsDir(), id.toString() + ".json");
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
                PlayerMap.put(UUID.fromString(FileHelper.getBaseName(playerFile.getName())), playerObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayer(UUID id){
        File theFile = new File(FileHelper.getPlayerDir(), id.toString()  + ".json");
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
            ClaimedLand.put(Integer.parseInt(FileHelper.getBaseName(dimFile.getName())), dimChunks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadClaimedChunkDim(String dim){
        File theFile = new File(FileHelper.getClaimedDir(), dim  + ".json");
        if (theFile.exists()) loadClaimedChunkDim(theFile);
    }
}
