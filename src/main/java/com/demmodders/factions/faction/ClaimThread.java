package com.demmodders.factions.faction;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.structures.ChunkClaim;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;

import javax.swing.text.html.parser.Entity;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClaimThread extends Thread {
    ConcurrentLinkedQueue<ChunkClaim> claims = new ConcurrentLinkedQueue<>();

    FactionManager fMan;

    public synchronized void start(FactionManager FMan) {
        fMan = FMan;
        super.start();
    }

    @Override
    public void run() {
        super.run();
        while(!isInterrupted()){
            ChunkClaim nextClaim = claims.poll();
            if (nextClaim != null) {
                // Filter unconnect chunks
                List<ChunkGroup> chunkGroups = new ArrayList<>();
                for (ChunkLocation claim : nextClaim.chunks) {
                    if (chunkGroups.isEmpty()) {
                        ChunkGroup group = new ChunkGroup(claim, nextClaim.faction);
                        chunkGroups.add(group);
                    } else {
                        ChunkGroup firstHit = null;
                        for (int i = 0; i < chunkGroups.size(); i++) {
                            ChunkGroup group = chunkGroups.get(i);
                            if (group.canAdd(claim, nextClaim.faction)) {
                                if (firstHit == null) {
                                    group.add(claim);
                                    firstHit = group;
                                } else {
                                    firstHit.merge(group);
                                    chunkGroups.remove(i);
                                    i--;
                                }
                            }
                        }
                        if (firstHit == null) {
                            chunkGroups.add(new ChunkGroup(claim, nextClaim.faction));
                        }
                    }
                }
                // Make Claims
                int claimCount = 0;
                Set<UUID> claimedFrom = new HashSet<>();
                for (ChunkGroup group : chunkGroups) {
                    if (group.connected) {
                        fMan.addLandToFaction(nextClaim.faction, group.chunks);
                        claimCount += group.chunks.size();
                        claimedFrom.addAll(group.owners);
                    }
                }

                EntityPlayerMP player = FactionManager.getPlayerMPFromUUID(nextClaim.player);

                // Tell involved parties
                if (claimCount > 0) {

                    // tell factions that have had their land stolen
                    for (UUID faction : claimedFrom) {
                        fMan.sendFactionwideMessage(faction, new TextComponentString(fMan.getRelationColour(faction, nextClaim.faction) + fMan.getFaction(nextClaim.faction).name + DemConstants.TextColour.ERROR + " claimed some of you land"));
                    }

                    // Tell player about the claims
                    StringBuilder message = new StringBuilder();
                    message.append(DemConstants.TextColour.INFO).append("Successfully claimed ").append(claimCount).append(" chunks");

                    // Mention factions that they stole land from
                    if (!claimedFrom.isEmpty()) {
                        message.append(", including chunks from ");
                        boolean first = true;
                        for (UUID faction : claimedFrom) {
                            if (!first) {
                                message.append(", ");
                            } else {
                                first = false;
                            }
                            message.append(fMan.getRelationColour(nextClaim.faction, faction)).append(fMan.getFaction(faction).name);
                        }
                    }
                    if (player != null) player.sendMessage(new TextComponentString(message.toString()));
                } else {
                    if (player != null) player.sendMessage(new TextComponentString(DemConstants.TextColour.ERROR + "Failed to claim any land"));
                }
            }
        }
    }
}

class ChunkGroup {
    public List<ChunkLocation> chunks = new ArrayList<>();
    public List<UUID> owners = new ArrayList<>();
    public boolean connected = false;

    public ChunkGroup(ChunkLocation chunk, UUID FactionID) {
        chunks.add(chunk);
        checkFactionConnected(chunk, FactionID);
    }

    private void checkFactionConnected(ChunkLocation chunk, UUID FactionID) {
        if (FactionConfig.landSubCat.landRequireConnect || FactionManager.getInstance().getFaction(FactionID).checkLandTouches(chunk.dim, chunk.x, chunk.z)) {
            connected = true;
        }
        UUID owner = FactionManager.getInstance().getChunkOwningFaction(chunk);
        if (owner != FactionID && FactionConfig.landSubCat.landRequireConnectWhenStealing) {

            connected = true;
        }
    }

    public static boolean checkConnected(ChunkLocation location1, ChunkLocation location2) {
        return (location1.dim == location2.dim) && (Math.abs(location1.x - location2.x) == 1 || Math.abs(location1.z - location2.z) == 1);
    }

    public boolean canAdd(ChunkLocation chunk, UUID FactionID){
        for (ChunkLocation testChunk : chunks) {
            if (checkConnected(chunk, testChunk)) {
                return true;
            }
        }
        return false;
    }

    public void add(ChunkLocation chunk) {
        chunks.add(chunk);
    }

    public void merge(ChunkGroup otherGroup) {
        chunks.addAll(otherGroup.chunks);
    }
}