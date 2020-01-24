package com.demmodders.factions.commands;

import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionTeleporter;
import com.demmodders.factions.util.Utils;
import com.demmodders.factions.util.structures.Location;
import com.demmodders.factions.util.structures.TeleportEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

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

    Queue<UUID> teleportQueue = new ArrayDeque<>();
    HashMap<UUID, TeleportEvent> teleportItems = new HashMap<>();

    /**
     * Adds a teleport event to the queue to be executed after a set delay
     * @param player The player to teleport
     * @param destination The location for the player to teleport to
     * @param delay The delay before the player should teleport
     */
    public boolean addTeleportEvent(EntityPlayerMP player, Location destination, int delay, double StartX, double StartY, double StartZ){
        UUID id = player.getUniqueID();
        if (teleportItems.containsKey(id)){
            return false;
        }
        teleportItems.put(id, new TeleportEvent(player, destination, delay, StartX, StartY, StartZ));
        teleportQueue.add(id);
        return true;
    }

    // TODO: Cancel teleport when player moves
    // FIXME: Cancel when logout

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        TeleportHandler handler = TeleportHandler.getInstance();
        if (handler.teleportItems.containsKey(event.player.getUniqueID())){
            TeleportHandler.getInstance().teleportItems.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onTickEvent(TickEvent.WorldTickEvent event) {
        TeleportHandler handler = TeleportHandler.getInstance();
        if (!handler.teleportQueue.isEmpty()) {
            UUID teleportID = handler.teleportQueue.remove();
                if (handler.teleportItems.containsKey(teleportID)) {
                TeleportEvent tele = handler.teleportItems.getOrDefault(teleportID, null);
                // check the delay has passed
                if (tele != null) {
                    double dx = tele.playerMP.posX - tele.startX;
                    double dy = tele.playerMP.posY - tele.startY;
                    double dz = tele.playerMP.posZ - tele.startZ;

                    double distance = Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2);

                    if ((int) (Utils.calculateAge(tele.startTime) / 1000) > tele.delay){
                        // Make sure we only teleport them cross dimensionally if we need to
                        if (tele.playerMP.dimension != tele.destination.dim)
                            tele.playerMP.changeDimension(tele.destination.dim, new FactionTeleporter(tele.destination));
                        else
                            tele.playerMP.connection.setPlayerLocation(tele.destination.x, tele.destination.y, tele.destination.z, tele.destination.yaw, tele.destination.pitch);
                        FactionManager.getInstance().getPlayer(tele.playerMP.getUniqueID()).lastTeleport = System.currentTimeMillis();
                        handler.teleportItems.remove(teleportID);
                    } else if (distance > 1){
                        // Remove from the queue if they've moved further than a block away
                        handler.teleportItems.remove(teleportID);
                        tele.playerMP.sendMessage(new TextComponentString(TextFormatting.RED + "Teleport cancelled"));
                    } else {
                        // Requeue the item at the back of the queue since we didn't use it
                        handler.teleportQueue.add(teleportID);
                    }
                }
            }
        }
    }
}

