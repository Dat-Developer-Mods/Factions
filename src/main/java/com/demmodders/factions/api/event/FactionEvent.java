package com.demmodders.factions.api.event;

import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.faction.Player;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public class FactionEvent extends Event {
    public final UUID causingPlayerID;

    public FactionEvent(UUID Player){
        causingPlayerID = Player;
    }

    public EntityPlayerMP getPlayerMP(){
        return FactionManager.getPlayerMPFromUUID(causingPlayerID);
    }

    public Player getFactionPlayer(){
        return FactionManager.getInstance().getPlayer(causingPlayerID);
    }
}
