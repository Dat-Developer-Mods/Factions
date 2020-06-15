package com.demmodders.factions.api.event;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

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
        public ChunkLocation position;

        public ChunkEvent(ChunkLocation Position, UUID Player, UUID FactionID){
            super(Player, FactionID);
            position = Position;
        }

        /**
         * Fired when a chunk is about to be claimed
         */
        @Cancelable
        public static class FactionClaimEvent extends ChunkEvent {

            public final UUID currentOwner;

            public FactionClaimEvent(ChunkLocation Position, UUID Player, UUID FactionID, UUID CurrentOwner) {
                super(Position, Player, FactionID);
                currentOwner = CurrentOwner;
            }
        }

        @Cancelable
        public static class FactionUnClaimEvent extends ChunkEvent {
            public FactionUnClaimEvent(ChunkLocation chunkLocation, UUID playerID, UUID factionID) {
                super(chunkLocation, playerID, factionID);
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
