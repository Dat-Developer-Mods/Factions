package com.demmodders.factions.util.structures;

import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportEvent {
    public TeleportEvent(EntityPlayerMP Player, Location Destination, int Delay){
        playerMP = Player;
        destination = Destination;
        delay = Delay;
        startTime = System.currentTimeMillis();
    }

    public EntityPlayerMP playerMP;
    public Location destination;
    public int delay;
    public long startTime;
}
