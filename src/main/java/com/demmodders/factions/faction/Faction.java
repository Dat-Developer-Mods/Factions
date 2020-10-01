package com.demmodders.factions.faction;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.factions.Factions;
import com.demmodders.factions.util.DemUtils;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.Power;
import com.demmodders.factions.util.structures.Relationship;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Faction {
    public transient UUID ID;
    public String name = "";
    public String desc = "";
    public String motd = "";
    public Location homePos = null;
    public Long foundingTime = 0L;
    public Power power = null;
    public ArrayList<UUID> invites = new ArrayList<>();
    public ArrayList<String>flags = new ArrayList<>();
    public HashMap<UUID, Relationship> relationships = new HashMap<>();
    public transient ArrayList<UUID> members = new ArrayList<>();
    public transient HashMap<Integer, ArrayList<String>> land = new HashMap<>();

    Faction(){

    }

    Faction(UUID factionID, String Name, String Desc, ArrayList<String> Flags){
        ID = factionID;
        name = Name;
        desc = Desc;

        power = new Power(FactionConfig.factionSubCat.factionStartingPower, FactionConfig.factionSubCat.factionStartingMaxPower);
        flags = Flags;
    }

    public Faction(UUID factionID, String name, UUID playerID){
        this.ID = factionID;
        this.name = name;
        this.foundingTime = System.currentTimeMillis();

        power = new Power(FactionConfig.factionSubCat.factionStartingPower, FactionConfig.factionSubCat.factionStartingMaxPower);
        invites = new ArrayList<>();

        flags = new ArrayList<>();
        relationships = new HashMap<>();

        this.land = new HashMap<>();
        this.members.add(playerID);
    }

    /**
     * Adds the specified player to the faction
     * @param PlayerID the ID of the player to add
     */
    public void addPlayer(UUID PlayerID){
        if (!members.contains(PlayerID)) {
            members.add(PlayerID);
        } else {
            Factions.LOGGER.info("Tried to add player " + PlayerID + " to faction " + name + " when it already has that player, ignoring");
        }
    }

    /**
     * Removes the specified player to the faction
     * @param PlayerID the ID of the player to remove
     */
    public void removePlayer(UUID PlayerID){
        if (members.contains(PlayerID)) {
            members.remove(PlayerID);
        } else {
            Factions.LOGGER.info("Tried to remove player " + PlayerID + " from faction " + name + " when it didn't have that player, ignoring");
        }
    }

    /**
     * Adds chunk to the faction
     * @param Dimension The dimension of the chunk
     * @param Land The position of the chunk
     */
    public void addLandToFaction(int Dimension, String Land){
        if (!land.containsKey(Dimension)){
            land.put(Dimension, new ArrayList<>());
        }

        if (!land.get(Dimension).contains(Land)){
            land.get(Dimension).add(Land);
        } else {
            Factions.LOGGER.info("Tried to add claimed land " + Land + " in Dim " + Dimension + " to faction " + name + " when it already has that land, ignoring");
        }
    }

    /**
     * removes chunk from the faction
     * @param Location The Location of the chunk
     */
    public void removeLandFromFaction(ChunkLocation Location){
        String chunkKey = FactionManager.makeChunkKey(Location.x, Location.z);
        removeLandFromFaction(Location.dim, chunkKey);
    }

    /**
     * removes chunk from the faction
     * @param Dimension The dimension of the chunk
     * @param Land The position of the chunk
     */
    public void removeLandFromFaction(int Dimension, String Land){
        if (land.containsKey(Dimension) && land.get(Dimension).contains(Land)){
            land.get(Dimension).remove(Land);
        } else {
            Factions.LOGGER.info("Tried to remove claimed land " + Land + " in Dim " + Dimension + " from faction " + name + " when it didn't have that land, ignoring");
        }
    }

    /**
     * Get the relation with the given faction ID
     * @param FactionID The faction to check the relation with
     * @return The relation with the other faction, null if neutral
     */
    @Nullable
    public RelationState getRelation(UUID FactionID){
        Relationship relation = relationships.getOrDefault(FactionID, null);
        if (relation != null){
            return relation.relation;
        }
        return null;
    }

    /**
     * Gets the owner of the faction
     * @return The owner of the faction
     */
    public UUID getOwnerID(){
        for (UUID member : members){
            if (FactionManager.getInstance().getPlayer(member).factionRank == FactionRank.OWNER){
                return member;
            }
        }
        return null;
    }

    public List<String> getMemberNames(){
        List<String> memberList = new ArrayList<>();
        for (UUID member : members){
            memberList.add(FactionManager.getInstance().getPlayer(member).lastKnownName);
        }
        return memberList;
    }

    /**
     * Checks if the faction has the specified flag
     * @param Flag The flag to check for
     * @return Whether the faction has the flag
     */
    public boolean hasFlag(String Flag){
        return flags.contains(Flag);
    }

    /**
     * Adds the specified flag to the faction
     * @param Flag The flag to add
     */
    public void setFlag(String Flag){
        if (!hasFlag(Flag)){
            flags.add(Flag);
        }
    }

    /**
     * Removes the specified flag to the faction
     * @param Flag The flag to remove
     */
    public void removeFlag(String Flag){
        if (hasFlag(Flag)){
            flags.remove(Flag);
        }
    }

    /**
     * Calculates the amount of power this faction has
     * @return The amount of power the faction has
     */
    public int calculatePower(){
        if (hasFlag("infinitepower")) return Integer.MAX_VALUE;
        int factionPower = power.power;
        FactionManager fMan = FactionManager.getInstance();
        for (UUID memberID : members) {
            factionPower += fMan.getPlayer(memberID).power.power;
        }
        return factionPower;
    }

    /**
     *  Calculates the maximum amount of power this faction can have
     * @return the maximum amount of power the faction can have
     */
    public int calculateMaxPower(){
        if (hasFlag("infinitepower")) return Integer.MAX_VALUE;
        int factionMaxPower = power.maxPower;
        FactionManager fMan = FactionManager.getInstance();
        for (UUID memberID : members) {
            factionMaxPower += fMan.getPlayer(memberID).power.maxPower;
        }
        return factionMaxPower;
    }

    /**
     * Calculates the cost of all the land this faction owns
     * @return the cost of the land this faction owns
     */
    public int calculateLandValue(){
        int landCount = 0;
        for (int dim: land.keySet()) {
            landCount += land.get(dim).size();
        }
        return landCount * FactionConfig.landSubCat.landPowerCost;
    }

    /**
     * Calculates whether the faction can have the extra amount of land given
     * @param extraLand the amount more land to test
     * @return Whether the faction can claim the amount of land given
     */
    public boolean checkCanAffordLand(int extraLand){
        int landCount = extraLand;
        for (int dim: land.keySet()) {
            landCount += land.get(dim).size();
        }
        return landCount * FactionConfig.landSubCat.landPowerCost <= calculatePower();
    }


    /**
     * Checks to see if the given chunk is connected to any other chunk owned by the faction, also true if the player has no land in that dimension
     * @param Dim The dimension of the chunk to check
     * @param X The X Coord of the chunk
     * @param Z The Z Coord of the chunl
     * @return If the chuck is connected to another owned chunk, or the faction has no land
     */
    public boolean checkLandTouches(int Dim, int X, int Z){
        if (land.size() == 0){
            return true;
        }
        else if (land.get(Dim).size() == 0) return true;
        String[] coords;
        for (String key : land.get(Dim)){
            coords = key.split(", ");
            if (X == Integer.parseInt(coords[0]) && Z == (Integer.parseInt(coords[1]) + 1) ||
                X == Integer.parseInt(coords[0]) && Z == (Integer.parseInt(coords[1]) - 1) ||
                X == (Integer.parseInt(coords[0]) + 1) && Z == Integer.parseInt(coords[1]) ||
                X == (Integer.parseInt(coords[0]) - 1) && Z == Integer.parseInt(coords[1])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a message giving information about the faction
     * @return a long message detailing public information about the faction
     */
    public String printFactionInfo(UUID askingFaction){
        FactionManager fMan = FactionManager.getInstance();

        String relationColour = fMan.getRelationColour(askingFaction, ID);

        // Work out age to display
        StringBuilder message = new StringBuilder();
        String age;

        long minutes = DemUtils.calculateAge(foundingTime) / 60000;
        if (minutes < 60){
            age = minutes + " Minutes";
        } else if (minutes < 1440){
            age = (minutes / 60) + " Hours";
        } else {
            age = (minutes / 1440) + " Days";
        }

        // Work out invitation policy to display
        String invitePolicy;
        if (hasFlag("open")){
            invitePolicy = "Open";
        } else {
            invitePolicy = "Invite only";
        }

        // Format Members
        StringBuilder memberText = new StringBuilder();
        boolean first = true;
        for (UUID player : members){
            if(!first) {
                memberText.append(TextFormatting.RESET).append(", ");
            } else {
                first = false;
            }

            memberText.append(fMan.getPlayerStatusColour(player, false)).append(fMan.getPlayer(player).lastKnownName);
        }

        StringBuilder allies = new StringBuilder();
        StringBuilder enemies = new StringBuilder();
        first = true;
        boolean enemyFirst = true;

        for (UUID factionID : relationships.keySet()){
            if (relationships.get(factionID).relation == RelationState.ALLY){
                if (!first) allies.append(TextFormatting.RESET).append(", ");
                else first = false;
                allies.append(FactionConstants.TextColour.ALLY).append(fMan.getFaction(factionID).name);
            } else if (relationships.get(factionID).relation == RelationState.ENEMY){
                if (!enemyFirst) allies.append(TextFormatting.RESET).append(", ");
                else enemyFirst = false;
                enemies.append(FactionConstants.TextColour.ENEMY).append(fMan.getFaction(factionID).name);
            }
        }

        message.append(DemConstants.TextColour.INFO).append("======").append(TextFormatting.RESET).append(relationColour).append(name).append(DemConstants.TextColour.INFO).append("======\n");
        message.append(DemConstants.TextColour.INFO).append("Description: ").append(TextFormatting.RESET).append(desc).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Age: ").append(TextFormatting.RESET).append(age).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Invitation Policy: ").append(TextFormatting.RESET).append(invitePolicy).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Land worth: ").append(TextFormatting.RESET).append(calculateLandValue()).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Power: ").append(TextFormatting.RESET).append(calculatePower()).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Max Power: ").append(TextFormatting.RESET).append(calculateMaxPower()).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Members: ").append(memberText.toString()).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Allies: ").append(allies.toString()).append("\n");
        message.append(DemConstants.TextColour.INFO).append("Enemies: ").append(enemies.toString());

        return message.toString();
    }
}


