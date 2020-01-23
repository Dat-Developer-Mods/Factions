package com.demmodders.factions.util.structures;

import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportEvent {
    public EntityPlayerMP playerMP;
    public Location destination;
    public int delay;
    public long startTime;
    public double startX,startY,startZ;

    public TeleportEvent(EntityPlayerMP Player, Location Destination, int Delay, double StartingX, double StartingY, double StartingZ){
        playerMP = Player;
        destination = Destination;
        delay = Delay;
        startTime = System.currentTimeMillis();

        startX = StartingX;
        startY = StartingY;
        startZ = StartingZ;
    }
}
