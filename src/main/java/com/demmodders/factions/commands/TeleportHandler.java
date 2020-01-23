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

    /**
     * Adds a teleport event to the queue to be executed after a set delay
     * @param player The player to teleport
     * @param destination The location for the player to teleport to
     * @param delay The delay before the player should teleport
     */
    public void addTeleportEvent(EntityPlayerMP player, Location destination, int delay){
        teleportQueue.add(new TeleportEvent(player, destination, delay));
    }

    //TODO: Cancel teleport when player moves

    @SubscribeEvent
    public static void onTickEvent(TickEvent.WorldTickEvent event) {
        TeleportHandler handler = TeleportHandler.getInstance();
        if (!handler.teleportQueue.isEmpty()) {
            TeleportEvent tele = handler.teleportQueue.remove();
            // check the delay has passed
            if (tele != null && ((int) (Utils.calculateAge(tele.startTime) / 1000)) > tele.delay)
            {
                // Make sure we only teleport them cross dimensionally if we need to
                if (tele.playerMP.dimension != tele.destination.dim) tele.playerMP.changeDimension(tele.destination.dim, new FactionTeleporter(tele.destination.x, tele.destination.y, tele.destination.z));
                else tele.playerMP.setPositionAndUpdate(tele.destination.x, tele.destination.y, tele.destination.z);
            } else {
                // Requeue the item at the back of the queue since we didn't use it
                handler.teleportQueue.add(tele);
            }
        }
    }
}

