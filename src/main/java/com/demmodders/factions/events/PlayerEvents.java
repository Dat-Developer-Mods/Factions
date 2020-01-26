package com.demmodders.factions.events;

import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Factions.MODID)
public class PlayerEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent
    public static void playerLogin(PlayerLoggedInEvent e){
        if (FactionManager.getInstance().isPlayerRegistered(e.player.getUniqueID())) {
            // record username for use when they're offline
            FactionManager.getInstance().setPlayerLastKnownName(e.player.getUniqueID(), e.player.getName());
        } else {
            LOGGER.info(e.player.getName() + " is not registered, ammending");
            FactionManager.getInstance().registerPlayer(e.player);
        }
    }

    @SubscribeEvent
    public static void chunkTraversal(EntityEvent.EnteringChunk e){
        if(e.getEntity() instanceof EntityPlayer && (e.getOldChunkX() != e.getNewChunkX() || e.getOldChunkZ() != e.getNewChunkZ())) {
            FactionManager fMan = FactionManager.getInstance();
            UUID factionID = fMan.getChunkOwningFaction(e.getEntity().dimension, e.getNewChunkX(), e.getNewChunkZ());
            if (fMan.isPlayerRegistered(e.getEntity().getUniqueID()) && fMan.getPlayer(e.getEntity().getUniqueID()).lastFactionLand != factionID) {

                UUID playerFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
                String message = "";
                if (factionID == null) {
                    message = TextFormatting.GOLD + FactionManager.getInstance().getFaction(FactionManager.WILDID).getLandTag();
                } else if (playerFaction != null) {
                    if (playerFaction.equals(factionID)) {
                        message = TextFormatting.DARK_GREEN + "your land";
                    } else {
                        RelationState relation = fMan.getFaction(playerFaction).getRelation(factionID);
                        if (relation != null) {
                            if (relation == RelationState.ALLY) message += TextFormatting.GREEN;
                            else if (relation == RelationState.ENEMY) message += TextFormatting.RED;
                        } else {
                            message += TextFormatting.GOLD;
                        }
                        message += FactionManager.getInstance().getFaction(factionID).getLandTag();
                    }
                } else {
                    message += TextFormatting.GOLD + FactionManager.getInstance().getFaction(factionID).getLandTag();
                }
                e.getEntity().sendMessage(new TextComponentString("Now entering " + message));
                fMan.getPlayer(e.getEntity().getUniqueID()).lastFactionLand = factionID;
            }
        }
    }

    // TODO: Block Break
    // TODO: Block Place
    // TODO: Death
}
