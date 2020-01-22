package com.demmodders.factions.commands;

import com.demmodders.factions.util.FactionTeleporter;
import com.demmodders.factions.util.Utils;
import com.demmodders.factions.util.structures.Location;
import com.demmodders.factions.util.structures.TeleportEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.Queue;

@Mod.EventBusSubscriber
public class TeleportHandler {
    // Singleton
    private static TeleportHandler instance;
    public static TeleportHandler getInstance(){
        if (instance == null){
            instance = new TeleportHandler();
        }
        return instance;
    }

    Queue<TeleportEvent> teleportQueue = new ArrayDeque<>();

    public void addTeleportEvent(EntityPlayerMP player, Location destination, int delay){
        teleportQueue.add(new TeleportEvent(player, destination, delay));
    }

    //TODO: Cancel teleport when player moves

    @SubscribeEvent
    public static void onTickEvent(TickEvent.WorldTickEvent event) {
        TeleportHandler handler = TeleportHandler.getInstance();
        if (!handler.teleportQueue.isEmpty()) {
            TeleportEvent tele = handler.teleportQueue.remove();
            if (tele != null && ((int) (Utils.calculateAge(tele.startTime) / 1000)) > tele.delay) {
                if (tele.playerMP.dimension != tele.destination.dim)
                    tele.playerMP.changeDimension(tele.destination.dim, new FactionTeleporter(tele.destination.x, tele.destination.y, tele.destination.z));
                else tele.playerMP.setPositionAndUpdate(tele.destination.x, tele.destination.y, tele.destination.z);
            } else {
                handler.teleportQueue.add(tele);
            }
        }
    }
}

