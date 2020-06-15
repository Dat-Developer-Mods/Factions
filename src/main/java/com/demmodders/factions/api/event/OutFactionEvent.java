package com.demmodders.factions.api.event;

import java.util.UUID;

public class OutFactionEvent extends FactionEvent{
    public OutFactionEvent(UUID Player) {
        super(Player);
    }

    /**
     * Fired when a faction is about to be created
     */
    public static class FactionCreateEvent extends OutFactionEvent {
        public String factionName;

        public FactionCreateEvent(UUID Player, String FactionName) {
            super(Player);
            factionName = FactionName;
        }
    }
}
