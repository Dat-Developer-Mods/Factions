package com.demmodders.factions.api.event;

import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.faction.Player;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public class FactionEvent extends Event {
    protected UUID playerID;

    public UUID getPlayerID() {
        return playerID;
    }

    public EntityPlayerMP getPlayerMP(){
        return (EntityPlayerMP) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(playerID);
    }

    public Player getFactionPlayer(){
        return FactionManager.getInstance().getPlayer(playerID);
    }
}
