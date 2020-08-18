package com.demmodders.factions.faction;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.datmoddingapi.util.FileHelper;
import com.demmodders.factions.Factions;
import com.demmodders.factions.api.event.InFactionEvent;
import com.demmodders.factions.api.event.OutFactionEvent;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.FactionFileHelper;
import com.demmodders.factions.util.enums.ClaimType;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.ClaimResult;
import com.demmodders.factions.util.structures.Power;
import com.demmodders.factions.util.structures.Relationship;
import com.demmodders.factions.util.structures.UnClaimResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

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
            instance.claimThread.start(instance);
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
    private Map<UUID, Faction> FactionMap = new HashMap<>();
    private Map<UUID, Player> PlayerMap = new HashMap<>();
    private final Map<Integer, HashMap<String, UUID>> ClaimedLand = Collections.synchronizedMap(new HashMap<>());

    // Claim Thread
    ClaimThread claimThread = new ClaimThread();

    // Getters
    /**
     * Gets the faction object that has the given ID
     * @param ID The ID of the faction
     * @return The faction object of the ID
     */
    public Faction getFaction(UUID ID){
        return FactionMap.get(ID);
    }

    /**
     * Gets the ID of the faction that has the given name
     * @param Name the name of the faction
     * @return The UUID of the faction with the given name
     */
    @Nullable
    public UUID getFactionIDFromName(String Name){
        for (UUID factionID : FactionMap.keySet()){
            if (FactionMap.get(factionID).name.toLowerCase().equals(Name.toLowerCase())){
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
        return PlayerMap.get(ID);
    }

    /**
     * Gets the ID of the player object that has the given name
     * @param Name the name of the player
     * @return The player with the given name, null if no player of that name is known
     */
    @Nullable
    public UUID getPlayerIDFromName(String Name){
        for (UUID playerID : PlayerMap.keySet()){
            if (PlayerMap.get(playerID).lastKnownName.equalsIgnoreCase(Name)){
                return playerID;
            }
        }
        return null;
    }

    /**
     * Gets the player object from the player's uuid
     * @param PlayerID The ID of the player
     * @return The player object of the player
     */
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
     * @param removeFromFaction Whether to remove the player info from the faction, useful to disable when iterating through and removing the members
     */
    public void setPlayerFaction(UUID PlayerID, UUID FactionID, boolean removeFromFaction){
        Player player = PlayerMap.get(PlayerID);

        if (removeFromFaction){
            FactionMap.get(player.faction).removePlayer(PlayerID);
        }

        player.faction = FactionID;

        FactionMap.get(FactionID).addPlayer(PlayerID);
        player.factionRank = FactionRank.GRUNT;
        if (player.invites.contains(FactionID)){
            player.invites.remove(FactionID);
            FactionMap.get(FactionID).invites.remove(PlayerID);
        }

        savePlayer(PlayerID);
    }

    /**
     * Sets the given player's rank
     * @param PlayerID The player who's rank is being changed
     * @param Rank The new rank
     */
    public void setPlayerRank(UUID PlayerID, FactionRank Rank){
        PlayerMap.get(PlayerID).factionRank = Rank;
        savePlayer(PlayerID);
        String rank = "";
        switch (Rank){
            case GRUNT:
                rank = "a Grunt";
                break;
            case LIEUTENANT:
                rank = "a Lieutenant";
                break;
            case OFFICER:
                rank = "an Officer";
                break;
            case OWNER:
                rank = "";
                break;
        }
        if (!rank.isEmpty()) {
            sendMessageToPlayer(PlayerID, DemConstants.TextColour.INFO + "You are now " + rank);
        }
    }

    // Utilities
    // Factions
    /**
     * Attempts to invite the given player to the given faction
     * @param PlayerID The player being invited
     * @param FactionID The faction the player is invited to
     * @return The result of the invite (0: Success, 1: Already invited, 2: already a member)
     */
    public int invitePlayerToFaction(UUID PlayerID, UUID FactionID){
        if (FactionMap.get(FactionID).invites.contains(PlayerID)) return 1;

        if (PlayerMap.get(PlayerID).faction.equals(FactionID)) return 2;

        FactionMap.get(FactionID).invites.add(PlayerID);
        PlayerMap.get(PlayerID).invites.add(FactionID);
        saveFaction(FactionID);
        sendMessageToPlayer(PlayerID, DemConstants.TextColour.INFO + FactionMap.get(FactionID).name + " Has invited you to join, accept their invite with " + DemConstants.TextColour.COMMAND + "/faction join " + FactionConstants.TextColour.OWN + FactionMap.get(FactionID).name);
        return 0;
    }

    /**
     * Remove any invites to the player if they have any
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
        synchronized (ClaimedLand) {
            for (int dim : ClaimedLand.keySet()) {
                pruned = false;
                synchronized (ClaimedLand.get(dim)) {
                    for (String land : ClaimedLand.get(dim).keySet()) {
                        if (FactionMap.containsKey(ClaimedLand.get(dim).get(land))) {
                            FactionMap.get(ClaimedLand.get(dim).get(land)).addLandToFaction(dim, land);
                        } else {
                            LOGGER.warn("Discovered land owned by a faction that doesn't exist, removing owner");
                            ClaimedLand.get(dim).remove(land);
                            pruned = true;
                        }
                    }
                }
                if (pruned) {
                    saveClaimedChunks(dim);
                }
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
        return new ArrayList<>(FactionMap.values());
    }

    /**
     * Gets a list of all IDs of the factions in the game
     * @return A list of all the faction IDs
     */
    public List<UUID> getListOfFactionsUUIDs(){
        return new ArrayList<>(FactionMap.keySet());
    }

    /**
     * Gets a list of the names of all the factions in the game
     * @return A list of all the faction names
     */
    public List<String> getListOfFactionsNames(){
        ArrayList<String> names = new ArrayList<>();
        for (UUID faction : FactionMap.keySet()){
            names.add(FactionMap.get(faction).name);
        }
        return names;
    }

    /**
     * Gets a list of the names of all the factions passed to the function by their UUIDs
     * @param IDs a list of the IDs
     * @return A list of the faction names
     */
    public List<String> getListOfFactionsNamesFromFactionList(ArrayList<UUID> IDs){
        ArrayList<String> names = new ArrayList<>();
        for (UUID faction : IDs){
            names.add(FactionMap.get(faction).name);
        }
        return names;
    }

    /**
     * Gets the colour code for chat that should be shown based off the faction
     * @param OriginalFaction The faction that has the relation
     * @param OtherFaction The faction the Original faction has a relation with
     * @return The colour code for the relation
     */
    public String getRelationColour(UUID OriginalFaction, UUID OtherFaction){
        if (OriginalFaction != WILDID && OtherFaction != WILDID) {
            RelationState relationship = FactionMap.get(OriginalFaction).getRelation(OtherFaction);
            if (OriginalFaction.equals(OtherFaction)) return FactionConstants.TextColour.OWN.toString();
            else if (relationship == RelationState.ALLY) return FactionConstants.TextColour.ALLY.toString();
            else if (relationship == RelationState.ENEMY) return FactionConstants.TextColour.ENEMY.toString();
        }

        return "";
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
     * Registers a player to the factions system
     * @param Player The player object to register
     */
    public void registerPlayer(EntityPlayer Player){
        if(isPlayerRegistered(Player.getUniqueID())) {
            return;
        }
        PlayerMap.put(Player.getUniqueID(), new Player(WILDID, null, new Power(FactionConfig.playerSubCat.playerStartingPower, FactionConfig.playerSubCat.playerStartingMaxPower), Player.getName()));
        savePlayer(Player.getUniqueID());
    }

    /**
     * Checks whether the given player can join the given faction
     * @param PlayerID The player trying to join the faction
     * @param FactionID The faction the player is trying to join
     * @return Whether the player can join the given faction
     */
    public boolean canAddPlayerToFaction(UUID PlayerID, UUID FactionID){
        Faction faction = FactionMap.get(FactionID);
        if ((FactionConfig.factionSubCat.maxMembers == 0 || faction.members.size() < FactionConfig.factionSubCat.maxMembers) && (faction.invites.contains(PlayerID) || faction.hasFlag("open"))) return true;
        else return false;
    }

    /**
     * Gets the faction that the given player belongs to
     * @param PlayerID The ID of the player
     * @return The faction object the player belongs to
     */
    public Faction getPlayersFaction(UUID PlayerID){
        UUID factionID = getPlayersFactionID(PlayerID);
        return FactionMap.get(factionID);
    }

    /**
     * Gets the ID of the faction that owns the given player
     * @param playerID The ID of the player
     * @return The UUID of the faction that owns the player
     */
    public UUID getPlayersFactionID(UUID playerID){
        return PlayerMap.get(playerID).faction;
    }

    /**
     * Sends the given message to the given player
     * @param Player The ID of the player to send the message to
     * @param Message The Message to send to the player
     */
    public void sendMessageToPlayer(UUID Player, String Message){
        sendMessageToPlayer(Player, new TextComponentString(Message));
    }

    /**
     * Sends the given message to the given player
     * @param Player The ID of the player to send the message to
     * @param Message The Message to send to the player
     */
    public void sendMessageToPlayer(UUID Player, ITextComponent Message){
        EntityPlayerMP playerMP = getPlayerMPFromUUID(Player);
        if (playerMP != null){
            playerMP.sendMessage(Message);
        }
    }



    public String getPlayerStatusColour(UUID PlayerID, boolean ShowAway){
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        if(playerList.getPlayerByUUID(PlayerID) != null) {
            // TODO: AFK
            return DemConstants.playerColour.ONLINE.toString();
        }
        else return DemConstants.playerColour.OFFLINE.toString();
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
    public UUID getChunkOwningFaction(int Dim, int ChunkX, int ChunkZ){
        if (ClaimedLand.containsKey(Dim)){
            String chunkKey = makeChunkKey(ChunkX, ChunkZ);
            if (ClaimedLand.get(Dim).containsKey(chunkKey)){
                return ClaimedLand.get(Dim).get(chunkKey);
            }
        }
        return WILDID;
    }

    /**
     * Gets the owner of the chunk at the given ChunkLocation
     * @param Chunk the ChunkLocation
     * @return The UUID of the faction that owns the chunk
     */
    public UUID getChunkOwningFaction(ChunkLocation Chunk) {
        return getChunkOwningFaction(Chunk.dim, Chunk.x, Chunk.z);
    }

    /**
     * Checks the given player can build on the given chunk
     * @param Dim The dimension the chunk is in
     * @param ChunkX The Chunk's X coord
     * @param ChunkZ The Chunk's Z coord
     * @param PlayerID The ID of the player trying to build
     * @return Whether the player can build in the given chunk
     */
    public boolean getPlayerCanBuild(int Dim, int ChunkX, int ChunkZ, UUID PlayerID){
        return getPlayerCanBuild(getChunkOwningFaction(Dim, ChunkX, ChunkZ), PlayerID);
    }

    /**
     * Checks the given player can build on the given faction land
     * @param OwningFaction The faction that owns the land
     * @param PlayerID The player trying to build
     * @return Whether the player can build on the faction's land
     */
    public boolean getPlayerCanBuild(UUID OwningFaction, UUID PlayerID){
        if (OwningFaction.equals(WILDID)) return true;

        UUID playerFaction = getPlayersFactionID(PlayerID);

        if (OwningFaction.equals(playerFaction)) return true;

        RelationState relation = FactionMap.get(OwningFaction).getRelation(playerFaction);
        return (relation == RelationState.ALLY && FactionConfig.factionSubCat.allyBuild) || ((relation == RelationState.ENEMY || relation == RelationState.PENDINGENEMY) && FactionConfig.factionSubCat.enemyBuild);
    }

    // Faction Functions
    /**
     * Creates a faction
     * @param Name The name of the faction
     * @param PlayerID The ID of the player who's creating the faction
     * @return The result of creating the faction (0 for success, 1 for name too long, 2 for name too short, 3 for faction exists, 4 cancelled)
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
        OutFactionEvent.FactionCreateEvent event = new OutFactionEvent.FactionCreateEvent(PlayerID, Name);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 4;

        UUID factionID = UUID.randomUUID();
        FactionMap.put(factionID, new Faction(factionID, event.factionName, PlayerID));

        saveFaction(factionID);

        PlayerMap.get(PlayerID).faction = factionID;
        PlayerMap.get(PlayerID).factionRank = FactionRank.OWNER;
        savePlayer(PlayerID);

        return 0;
    }

    /**
     * Safely disbands the given faction
     * @param FactionID The faction being disbanded
     * @param PlayerID The player disbanding the faction
     * @return Whether the faction was successfully disbanded
     */
    public boolean disbandFaction(UUID FactionID, @Nullable UUID PlayerID){
        Faction faction = FactionMap.get(FactionID);

        // Ensure the faction can be disbanded
        if (faction.hasFlag("permanent")) return false;

        // Post event, fail if something cancelled it
        InFactionEvent.FactionDisbandEvent event = new InFactionEvent.FactionDisbandEvent(PlayerID, FactionID);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return false;

        releaseAllFactionLand(FactionID);

        // Remove members
        for (UUID playerID : faction.members) {
            setPlayerFaction(playerID, WILDID, false);
        }

        // Remove invites
        for (UUID invitedPlayerID : faction.invites){
            PlayerMap.get(invitedPlayerID).invites.remove(FactionID);
        }

        // Release relations
        for (UUID otherFactionID : faction.relationships.keySet()){
            FactionMap.get(otherFactionID).relationships.remove(FactionID);
        }

        FactionMap.remove(FactionID);
        deleteFactionFile(FactionID);
        return true;
    }

    /**
     * Sends the given message to all the online members of the given faction
     * @param FactionID The ID of the faction
     * @param Message The message to send to all the users
     */
    public void sendFactionwideMessage(UUID FactionID, ITextComponent Message){
        for(UUID playerID : FactionMap.get(FactionID).members){
            sendMessageToPlayer(playerID, Message);
        }
    }

    /**
     * Removes all land owned by a faction
     * @param FactionID The ID of the faction
     */
    public void releaseAllFactionLand(UUID FactionID){
        synchronized (ClaimedLand) {
            for (int dim : ClaimedLand.keySet()) {
                synchronized (ClaimedLand.get(dim)){
                    ClaimedLand.get(dim).values().removeIf(value -> value.equals(FactionID));
                }
                saveClaimedChunks(dim);
            }
        }
        if(FactionMap.containsKey(FactionID)) FactionMap.get(FactionID).land.clear();
    }

    /**
     * Attempts to claim some chunks for the given faction
     * @param FactionID The faction claiming the chunk
     * @param PlayerID The player claiming the chunk
     * @param Dim The dimention the chunk is in
     * @param ChunkX The X Coord of the chunk
     * @param ChunkZ The Z Coord of the chunk
     * @param Type The type of claim (One, Square, auto)
     * @param Radius The radius of the claim (only applies to square)
     * @return The result of the claim (0: Success, 1:Success, but stolen, 2: Not enough power, 3: Must touch other land, 4: Faction owns it, 5: You own it, 6: Nope)
     */
    public ClaimResult claimLand(UUID FactionID, @Nullable UUID PlayerID, int Dim, int ChunkX, int ChunkZ, ClaimType Type, int Radius){
        ClaimResult result = new ClaimResult();
        List<ChunkLocation> chunks = new ArrayList<>();
        Faction faction = FactionMap.get(FactionID);

        //TODO: Finish

        // Ensure dimension is in the system
        if (!ClaimedLand.containsKey(Dim)) {
            // Create dimension entry
            ClaimedLand.put(Dim, new HashMap<>());
        }

        // Get all chunks and check they connect
        boolean connected = false;

        // Work out which land we're claiming
        switch (Type) {
            case AUTO:
            case ONE:
                result.attemptedClaimedLandCount = 1;
                chunks.add(new ChunkLocation(Dim, ChunkX, ChunkZ));
                connected = faction.checkLandTouches(Dim, ChunkX, ChunkZ) || !getChunkOwningFaction(Dim, ChunkX, ChunkZ).equals(WILDID);
                break;
            case SQUARE:
                result.attemptedClaimedLandCount = (Radius * 2) + 1;
                for (int i = ChunkX - Radius; i <= ChunkX + Radius; i++) {
                    for (int j = ChunkZ - Radius; j <= ChunkZ + Radius; j++) {
                        UUID owner = getChunkOwningFaction(Dim, i, j);
                        if (!owner.equals(WILDID)) {
                            result.owners.add(owner);
                            continue;
                        };
                        chunks.add(new ChunkLocation(Dim, i, j));
                        connected = connected || faction.checkLandTouches(Dim, i, j);
                    }
                }
                break;
            default:
                result.result = 5;
                return result;
        }
        result.claimedLandCount = chunks.size();
        if (chunks.size() == 0) {
            result.result = 3;
            return result;
        } else if (!faction.checkCanAffordLand(chunks.size())) {
            result.result = 1;
            return result;
        } else if (!connected && FactionConfig.landSubCat.landRequireConnect) {
            result.result = 2;
            return result;
        }

        result.owners.clear();

        InFactionEvent.ChunkEvent.FactionClaimEvent event = new InFactionEvent.ChunkEvent.FactionClaimEvent(chunks, PlayerID, FactionID, Type);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            result.result = 5;
            return result;
        }

        // Check auto
        if (Type == ClaimType.AUTO) {
            if (PlayerID == null) {
                result.result = 5;
                return result;
            }
            Player thePlayer = PlayerMap.get(PlayerID);
            if (thePlayer == null) {
                result.result = 5;
                return result;
            }

            if (thePlayer.autoClaim) {
                thePlayer.autoClaim = false;
                sendMessageToPlayer(PlayerID, DemConstants.TextColour.INFO + "Disabled autoclaim");
            } else {
                thePlayer.autoClaim = true;
                sendMessageToPlayer(PlayerID, DemConstants.TextColour.INFO + "Enabled autoclaim");
            }
            chunks.add(new ChunkLocation(Dim, ChunkX, ChunkZ));
            result.attemptedClaimedLandCount = 1;
        }

        Set<Integer> dims = new HashSet<>();

        for (int i = 0; i < chunks.size(); i++) {
            ChunkLocation chunk = chunks.get(i);
            String chunkKey = makeChunkKey(chunk.x, chunk.z);
            UUID owner = getChunkOwningFaction(chunk.dim, chunk.x, chunk.z);
            if (!owner.equals(WILDID)) {
                if (owner.equals(FactionID) || FactionMap.get(owner).hasFlag("strongborders") || FactionMap.get(owner).calculatePower() >= FactionMap.get(owner).calculateLandValue() || FactionMap.get(owner).calculatePower() >= FactionMap.get(FactionID).calculatePower()) {
                    result.result = 3;
                    chunks.remove(i);
                    i--;
                    continue;
                    // TODO: Fix, will break when badly ordered
                } else if (!FactionConfig.landSubCat.landRequireConnectWhenStealing || faction.checkLandTouches(chunk.dim, chunk.x, chunk.z)) {
                    result.result = 2;
                    chunks.remove(i);
                    i--;
                    continue;
                } else {
                    result.owners.add(owner);
                    FactionMap.get(owner).removeLandFromFaction(chunk);
                }
            }

            ClaimedLand.get(chunk.dim).put(chunkKey, FactionID);
            dims.add(chunk.dim);
            FactionMap.get(FactionID).addLandToFaction(chunk.dim, chunkKey);
        }
        for (int dim : dims) saveClaimedChunks(dim);
        return result;
    }

    private void addLandToTheFaction(UUID FactionID, int Dim, int ChunkX, int ChunkZ){
        String chunkKey = makeChunkKey(ChunkX, ChunkZ);

        if (ClaimedLand.containsKey(Dim)) {
            if (ClaimedLand.get(Dim).containsKey(chunkKey)) {
                FactionMap.get(ClaimedLand.get(Dim).get(chunkKey)).removeLandFromFaction(Dim, chunkKey);
            }
        } else {
            // Create dimension entry
            ClaimedLand.put(Dim, new HashMap<>());
        }
        ClaimedLand.get(Dim).put(chunkKey, FactionID);
        FactionMap.get(FactionID).addLandToFaction(Dim, chunkKey);


    }

    /**
     * Add a chunk to a faction
     * @param FactionID The faction claiming the chunk
     * @param chunk The chunk location being claimed
     */
    public void addLandToFaction(UUID FactionID, ChunkLocation chunk) {
        addLandToTheFaction(FactionID, chunk.dim, chunk.x, chunk.z);
        saveClaimedChunks(chunk.dim);
    }

    /**
     * Add a chunk to a faction
     * @param FactionID The faction claiming the chunk
     * @param Dim The dimention the chunk is in
     * @param ChunkX The X Coord of the chunk
     * @param ChunkZ The Z Coord of the chunk
     */
    public void addLandToFaction(UUID FactionID, int Dim, int ChunkX, int ChunkZ) {
        addLandToTheFaction(FactionID, Dim, ChunkX, ChunkZ);
        saveClaimedChunks(Dim);
    }

    /**
     * Add all the given chunks to the given faction
     * @param FactionID The faction claiming the chunks
     * @param chunks The chunks to add to the faction
     */
    public void addLandToFaction(UUID FactionID, List<ChunkLocation> chunks) {
        Set<Integer> dims = new HashSet<>();

        for (ChunkLocation chunk : chunks) {
            dims.add(chunk.dim);
            addLandToFaction(FactionID, chunk);
        }

        // Save all the chunks
        for (int dim : dims) {
            saveClaimedChunks(dim);
        }
    }

    /**
     * Attempts to remove a factions claim on a chunk
     * @param FactionID The faction unclaiming the chunk
     * @param PlayerID The player unclaiming the chunk
     * @param Dim The dimention the chunk is in
     * @param ChunkX The X Coord of the chunk
     * @param ChunkZ The Z Coord of the chunk
     * @return The result of the claim (0: Success, 1: Faction doesn't own that chunk, 2: Cancelled)
     */
    public UnClaimResult unClaimLand(UUID FactionID, @Nullable UUID PlayerID, int Dim, int ChunkX, int ChunkZ, ClaimType Type, int Radius){
        UnClaimResult result = new UnClaimResult();
        List<ChunkLocation> chunks = new ArrayList<>();
        Faction faction = FactionMap.get(FactionID);
        // Work out which land we're claiming
        switch (Type) {
            case ALL:
                break;
            case ONE:
                if (getChunkOwningFaction(Dim, ChunkX, ChunkZ).equals(FactionID))
                    chunks.add(new ChunkLocation(Dim, ChunkX, ChunkZ));
                break;
            case SQUARE:
                for (int i = ChunkX - Radius; i <= ChunkX + Radius; i++) {
                    for (int j = ChunkZ - Radius; j <= ChunkZ + Radius; j++) {
                        if (getChunkOwningFaction(Dim, i, j).equals(FactionID))
                            chunks.add(new ChunkLocation(Dim, i, j));
                    }
                }
                break;
            default:
                result.result = 2;
                return result;
        }


        if (chunks.isEmpty() && Type != ClaimType.ALL) {
            result.result = 1;
            return result;
        }

        InFactionEvent.ChunkEvent.FactionUnClaimEvent event = new InFactionEvent.ChunkEvent.FactionUnClaimEvent(chunks, PlayerID, FactionID, Type);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            result.result = 2;
            return result;
        }

        if (Type == ClaimType.ALL){
            result.count = countFactionLand(FactionID);
            releaseAllFactionLand(FactionID);
            result.result = 0;
        } else {
            Set<Integer> dims = new HashSet<>();
            for (ChunkLocation chunk : chunks) {
                String chunkKey = makeChunkKey(chunk.x, ChunkZ);
                ClaimedLand.get(chunk.dim).remove(chunkKey);
                dims.add(chunk.dim);
                FactionMap.get(FactionID).removeLandFromFaction(chunk.dim, chunkKey);
            }
            for (int dim : dims) saveClaimedChunks(dim);
        }

        return result;
    }

    /**
     * Counts all the land a faction owns
     * @param FactionID The ID of the faction whose land you're counting
     * @return The amount of land the faction owns
     */
    public int countFactionLand(UUID FactionID){
        int landCount = 0;
        Faction faction = FactionMap.get(FactionID);
        for (ArrayList<String> landList : faction.land.values()) {
            landCount += landList.size();
        }
        return landCount;
    }

    /**
     * Counts all the land a faction owns in a dimension
     * @param FactionID The ID of the faction whose land you're counting
     * @param Dim The dimension that countains the land you want to count
     * @return The amount of land the given faction owns in the given dimension
     */
    public int countFactionLand(UUID FactionID, int Dim) {
        if (FactionMap.get(FactionID).land.get(Dim) != null)
            return FactionMap.get(FactionID).land.get(Dim).size();
        return 0;
    }

    /**
     * Attempts to add the other faction as an ally to the given faction
     * @param FactionID The faction creating the alliance
     * @param OtherFaction the faction the alliance is with
     * @param PlayerID The ID of the player creating the alliance
     * @return The result of the alliance (0: Success them pending, 1: Success both allies, 2: Success but enemy, 3: Already allies, 4: That's you, 5: No)
     */
    public int addAlly(UUID FactionID, UUID OtherFaction, @Nullable UUID PlayerID){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.get(OtherFaction);
        if (FactionID.equals(OtherFaction)) return 4;
        if (FactionMap.get(OtherFaction).hasFlag("unrelateable")) return 5;
        if (currentRelation != null && currentRelation.relation == RelationState.ALLY) return 3;

        InFactionEvent.FactionRelationEvent event = new InFactionEvent.FactionRelationEvent(PlayerID, FactionID, OtherFaction, RelationState.ALLY);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 6;

        setFactionRelation(FactionID, OtherFaction, RelationState.ALLY);
        saveFaction(FactionID);

        if (!FactionMap.get(OtherFaction).relationships.containsKey(FactionID)) {
            setFactionRelation(OtherFaction, FactionID, RelationState.PENDINGALLY);
            saveFaction(OtherFaction);
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has made you their allies" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, but they can't build on yours" : "") + ", add them back with /faction ally " + FactionMap.get(FactionID).name));
            return 0;
        } else if (FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.ALLY){
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has added you backc  as an ally, " + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land" : "")));
            return 1;
        } else {
            sendFactionwideMessage(OtherFaction, new TextComponentString(TextFormatting.DARK_GREEN + FactionMap.get(FactionID).name + " has added you as their allies," + TextFormatting.DARK_RED + " you still think they're enemies though"));
            return 2;
        }
    }

    /**
     * Attempts to add the other faction as an enemy to the given faction
     * @param FactionID The faction declaring the enemy
     * @param OtherFaction The faction becoming an enemy
     * @param PlayerID The ID of the player declaring the enemy
     * @return The result of the declaration (0: Success, 1: Success enemies all round, 2: Success but you're an ally to them, 3: already enemies, 4: That's you, 5: No)
     */
    public int addEnemy(UUID FactionID, UUID OtherFaction, @Nullable UUID PlayerID){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.get(OtherFaction);
        if (FactionID.equals(OtherFaction)) return 4;
        if (FactionMap.get(OtherFaction).hasFlag("unrelateable")) return 5;
        if (currentRelation != null && currentRelation.relation == RelationState.ENEMY) return 3;

        InFactionEvent.FactionRelationEvent event = new InFactionEvent.FactionRelationEvent(PlayerID, FactionID, OtherFaction, RelationState.ENEMY);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 6;

        setFactionRelation(FactionID, OtherFaction, RelationState.ENEMY);
        saveFaction(FactionID);

        if (!FactionMap.get(OtherFaction).relationships.containsKey(FactionID) || FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.PENDINGALLY) {
            setFactionRelation(OtherFaction, FactionID, RelationState.PENDINGENEMY);
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
     * @param PlayerID The ID of the player creating the alliance
     * @return The result of the declaration (0: Success, 1: Removed enemy, 2: Removed ally, 3: cannot deny request, 4: no relation, 5: that's you
     */
    public int addNeutral(UUID FactionID, UUID OtherFaction, UUID PlayerID){
        Relationship currentRelation = FactionMap.get(FactionID).relationships.get(OtherFaction);
        if (FactionID.equals(OtherFaction)) return 5;
        if (currentRelation == null) return 4;
        if (currentRelation.relation == RelationState.PENDINGALLY || currentRelation.relation == RelationState.PENDINGENEMY) return 3;

        InFactionEvent.FactionRelationEvent event = new InFactionEvent.FactionRelationEvent(PlayerID, FactionID, OtherFaction, RelationState.ALLY);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 6;

        setFactionRelation(FactionID, OtherFaction, null);
        saveFaction(FactionID);
        if (FactionMap.get(OtherFaction).relationships.containsKey(FactionID)) {
            switch (FactionMap.get(OtherFaction).relationships.get(FactionID).relation){
                case ALLY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(DemConstants.TextColour.INFO + FactionMap.get(FactionID).name + " is no longer your Ally, however you still regard them as one" + (FactionConfig.factionSubCat.allyBuild ? ", this means they can build on your land, but you can't build on theirs" : "") + ", you can remove them as allies with /faction neutral " + FactionMap.get(FactionID).name));
                    break;
                case ENEMY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(DemConstants.TextColour.INFO + FactionMap.get(FactionID).name + " is no longer your enemy, however you still regard them as one" + (FactionConfig.factionSubCat.enemyBuild ? ", this means they you can still build on each other's land" : "") + ", you can remove them as an enemy with /faction neutral " + FactionMap.get(FactionID).name));
                    break;
                case PENDINGALLY:
                case PENDINGENEMY:
                    sendFactionwideMessage(OtherFaction, new TextComponentString(DemConstants.TextColour.INFO + FactionMap.get(FactionID).name + " No longer wants to be your " + (FactionMap.get(OtherFaction).relationships.get(FactionID).relation == RelationState.PENDINGALLY ? "ally, " + (FactionConfig.factionSubCat.allyBuild ? "you can no longer build on their land" : "") : "enemy" + (FactionConfig.factionSubCat.enemyBuild ? "you can no longer build on each other's land" : ""))));
                    setFactionRelation(OtherFaction, FactionID, null);
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
     * Sets the given faction's relation with the other given faction to the given relation
     * @param FactionID The faction the relation is being set for
     * @param OtherFactionID The faction the relation is being set about
     * @param NewRelation The new relation to set the faction to, if null then removes the relationship
     */
    public void setFactionRelation(UUID FactionID, UUID OtherFactionID, @Nullable RelationState NewRelation){
        if (NewRelation != null) FactionMap.get(FactionID).relationships.put(OtherFactionID, new Relationship(NewRelation));
        else if (FactionMap.get(FactionID).relationships.containsKey(OtherFactionID)) FactionMap.get(FactionID).relationships.remove(OtherFactionID);
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
            if (ClaimedLand.get(position.dim).get(chunkKey).equals(FactionID)) {
                FactionMap.get(FactionID).homePos = position;
                return true;
            }
        }
        return false;
    }

    // IO Functions
    // Save
    public void saveFaction(UUID FactionID){
        if (FactionMap.containsKey(FactionID)) {
            Gson gson = new Gson();
            File factionFile;
            if (FactionID.equals(WILDID)) factionFile = FileHelper.openFile(new File(FactionFileHelper.getDefaultFactionDir(), "Wild.json"));
            else if (FactionID.equals(SAFEID)) factionFile = FileHelper.openFile(new File(FactionFileHelper.getDefaultFactionDir(), "SafeZone.json"));
            else if (FactionID.equals(WARID)) factionFile = FileHelper.openFile(new File(FactionFileHelper.getDefaultFactionDir(), "WarZone.json"));
            else factionFile = FileHelper.openFile(new File(FactionFileHelper.getFactionsDir(), FactionID.toString()  + ".json"));
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
            File playerFile = FileHelper.openFile(new File(FactionFileHelper.getPlayerDir(), PlayerID.toString() + ".json"));
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
            File dimFile = FileHelper.openFile(new File(FactionFileHelper.getClaimedDir(), dim + ".json"));
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
        flags.add("default");
        flags.add("permanent");
        flags.add("strongborders");
        flags.add("infinitepower");
        flags.add("unlimitedland");
        flags.add("unrelateable");
        Faction wild = new Faction(WILDID, "The Wild", "Everywhere that isn't owned by a faction", flags);
        FactionMap.put(WILDID, wild);
        saveFaction(WILDID);
    }

    public void generateSafeZone(){
        ArrayList<String> flags = new ArrayList<>();
        flags.add("default");
        flags.add("permanent");
        flags.add("strongborders");
        flags.add("infinitepower");
        flags.add("unlimitedland");
        flags.add("unrelateable");
        flags.add("nodamage");
        flags.add("nobuild");
        Faction wild = new Faction(SAFEID, "The SafeZone", "You're pretty safe here", flags);
        FactionMap.put(SAFEID, wild);
        saveFaction(SAFEID);
    }

    public void generateWarZone(){
        ArrayList<String> flags = new ArrayList<>();
        flags.add("default");
        flags.add("permanent");
        flags.add("strongborders");
        flags.add("infinitepower");
        flags.add("unlimitedland");
        flags.add("unrelateable");
        flags.add("bonuspower");
        Faction wild = new Faction(WARID, "The WarZone", "You're not safe here, you will lose more power when you die, but will gain more power when you kill", flags);
        FactionMap.put(WARID, wild);
        saveFaction(WARID);
    }

    // Load
    public void loadDefaultFactions(){
        File factionFile;
        // Wild
        factionFile = new File(FactionFileHelper.getDefaultFactionDir(), "Wild.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, WILDID);
        } else {
            generateWild();
        }

        // SafeZone
        factionFile = new File(FactionFileHelper.getDefaultFactionDir(), "SafeZone.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, SAFEID);
        } else {
            generateSafeZone();
        }

        // WarZone
        factionFile = new File(FactionFileHelper.getDefaultFactionDir(), "WarZone.json");
        if (factionFile.exists()) {
            loadFaction(factionFile, WARID);
        } else {
            generateWarZone();
        }
    }

    public void loadFactions(){
        File[] factions = FactionFileHelper.getFactionsDir().listFiles();
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
                factionObject.ID = ID;
                FactionMap.put(ID, factionObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteFactionFile(UUID FactionID){
        File factionFile = new File(FactionFileHelper.getFactionsDir(), FactionID + ".json");
        if (factionFile.exists()){
            factionFile.delete();
        }
    }

    public void loadFaction(UUID id){
        File theFile = new File(FactionFileHelper.getFactionsDir(), id.toString() + ".json");
        if (theFile.exists()) loadFaction(theFile);
    }

    public void loadPlayers(){
        File[] players = FactionFileHelper.getPlayerDir().listFiles();
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
        File theFile = new File(FactionFileHelper.getPlayerDir(), id.toString()  + ".json");
        if (theFile.exists()) loadFaction(theFile);
    }

    public void loadClaimedChunks(){
        File[] dims = FactionFileHelper.getClaimedDir().listFiles();
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
        File theFile = new File(FactionFileHelper.getClaimedDir(), dim  + ".json");
        if (theFile.exists()) loadClaimedChunkDim(theFile);
    }
}
