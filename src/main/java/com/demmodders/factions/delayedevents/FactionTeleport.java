package com.demmodders.factions.delayedevents;

import com.demmodders.datmoddingapi.delayedexecution.delayedevents.DelayedTeleportEvent;
import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.factions.faction.FactionManager;
import net.minecraft.entity.player.EntityPlayerMP;

public class FactionTeleport extends DelayedTeleportEvent {
    public FactionTeleport(Location Destination, EntityPlayerMP Player, int Delay) {
        super(Destination, Player, Delay);
    }

    @Override
    public void execute() {
        super.execute();
        FactionManager.getInstance().getPlayer(player.getUniqueID()).lastTeleport = System.currentTimeMillis();
    }
}
