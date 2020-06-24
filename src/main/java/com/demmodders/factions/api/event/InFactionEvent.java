package com.demmodders.factions.api.event;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.enums.ClaimType;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.List;
import java.util.UUID;

public class InFactionEvent extends FactionEvent {
    public UUID factionID;

    public InFactionEvent(UUID Player, UUID FactionID) {
        super(Player);
        factionID = FactionID;
    }

    public Faction getFaction(){
        return FactionManager.getInstance().getFaction(factionID);
    }

    public static class ChunkEvent extends InFactionEvent {
        public List<ChunkLocation> positions;
        public final ClaimType type;

        public ChunkEvent(List<ChunkLocation> Positions, UUID Player, UUID FactionID, ClaimType Type){
            super(Player, FactionID);
            positions = Positions;
            type = Type;
        }

        /**
         * Fired when a chunk is about to be claimed
         */
        @Cancelable
        public static class FactionClaimEvent extends ChunkEvent {

            public FactionClaimEvent(List<ChunkLocation> Positions, UUID Player, UUID FactionID, ClaimType Type) {
                super(Positions, Player, FactionID, Type);
            }
        }

        @Cancelable
        public static class FactionUnClaimEvent extends ChunkEvent {
            public FactionUnClaimEvent(List<ChunkLocation> chunkLocations, UUID playerID, UUID factionID, ClaimType Type) {
                super(chunkLocations, playerID, factionID, Type);
            }
        }
    }

    /**
     * Fired when a faction is about to be disbanded
     */
    @Cancelable
    public static class FactionDisbandEvent extends InFactionEvent {
        public FactionDisbandEvent(UUID Player, UUID FactionID) {
            super(Player, FactionID);
        }
    }

    /**
     * Fired when a faction is about to change relation with another faction
     */
    @Cancelable
    public static class FactionRelationEvent extends InFactionEvent {
        public final RelationState newRelation;
        public final UUID otherFaction;

        public FactionRelationEvent(UUID Player, UUID FactionID, UUID OtherFaction, RelationState NewRelation) {
            super(Player,FactionID);
            newRelation = NewRelation;
            otherFaction = OtherFaction;
        }
    }
}
