package com.demmodders.factions.delayedevents;

import com.demmodders.datmoddingapi.delayedexecution.delayedevents.BaseDelayedEvent;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.DemUtils;
import com.demmodders.factions.util.FactionConfig;

public class FactionCleanout extends BaseDelayedEvent {
    boolean cancelled = false;
    FactionManager fMan;

    public FactionCleanout() {
        super(FactionConfig.factionSubCat.clearFactionPeriod * 60);
        fMan = FactionManager.getInstance();
    }

    @Override
    public void execute() {
        for (Faction faction : fMan.getListOfFactions()) {
            if (DemUtils.calculateAge(faction.getLastOnline()) > (long) FactionConfig.factionSubCat.maxFactionOffline * 60000) {
                fMan.disbandFaction(faction.ID, null);
            }
        }
        exeTime = System.currentTimeMillis() + FactionConfig.factionSubCat.clearFactionPeriod * 60000L;
    }

    @Override
    public boolean shouldRequeue(boolean hasFinished) {
        return !cancelled;
    }
}
