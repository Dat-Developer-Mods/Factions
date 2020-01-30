package com.demmodders.factions.api.event;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.UUID;

public class InFactionEvent extends FactionEvent {
    protected UUID factionID;

    public UUID getFactionID() {
        return factionID;
    }

    public Faction getFaction(){
        return FactionManager.getInstance().getFaction(factionID);
    }

    /**
     * Fired when a faction is about to be claimed
     */
    @Cancelable
    public static class FactionClaimEvent extends InFactionEvent {
        private ChunkLocation position;

        public FactionClaimEvent(ChunkLocation Position, UUID Player, UUID FactionID) {
            position = Position;
            playerID = Player;
            factionID = FactionID;
        }

        public ChunkLocation getPosition() {
            return position;
        }
    }

    /**
     * Fired when a faction is about to be disbanded
     */
    @Cancelable
    public static class FactionDisbandEvent extends InFactionEvent {

        public FactionDisbandEvent(UUID Player, UUID FactionID) {
            playerID = Player;
            factionID = FactionID;
        }
    }

    /**
     * Fired when a faction is about to change relation with another faction
     */
    @Cancelable
    public static class FactionRelationEvent extends InFactionEvent {
        private RelationState newRelation;
        private UUID otherFaction;

        public FactionRelationEvent(UUID Player, UUID FactionID, UUID OtherFaction, RelationState NewRelation) {
            playerID = Player;
            factionID = FactionID;
            newRelation = NewRelation;
            otherFaction = OtherFaction;
        }

        public RelationState getNewRelation() {
            return newRelation;
        }

        public UUID getOtherFaction() {
            return otherFaction;
        }
    }
}
