package com.demmodders.factions.events;

import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.FactionManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Factions.MODID)
public class PlayerEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent e){
        UUID playerID = e.player.getUniqueID();
        if(!FactionManager.getInstance().isPlayerRegistered(playerID)){
            FactionManager.getInstance().registerPlayer(playerID);
        }
    }

    @SubscribeEvent
    public static void chunkTraversal(EntityEvent.EnteringChunk e){
        if(e.getEntity() instanceof EntityPlayer) {
            FactionManager.getInstance().getChunkOwningFaction(e.getEntity().dimension, e.getNewChunkX(), e.getNewChunkZ());
            LOGGER.info("Dimension ID = " + e.getEntity().dimension);
        }
    }

    // TODO: long last seen when player leaves
}
