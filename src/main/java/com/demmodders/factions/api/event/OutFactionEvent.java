package com.demmodders.factions.api.event;

import java.util.UUID;

public class OutFactionEvent extends FactionEvent{
    /**
     * Fired when a faction is about to be created
     */
    public static class FactionCreateEvent extends InFactionEvent {
        private String factionName;

        public FactionCreateEvent(UUID Player, String FactionName) {
            playerID = Player;
            factionName = FactionName;
        }

        public String getFactionName() {
            return factionName;
        }
    }
}
