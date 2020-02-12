package com.demmodders.factions.events;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
            LOGGER.info(e.player.getName() + " is not registered with factions, amending");
            FactionManager.getInstance().registerPlayer(e.player);
        }
    }

    @SubscribeEvent
    public static void playerKilled(LivingDeathEvent e){
        // TODO: CHECK
        // TODO: Change based on land
        if (e.getEntity() instanceof EntityPlayer && e.getSource().getTrueSource() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            UUID killedFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
            UUID killerFaction = fMan.getPlayersFactionID(e.getSource().getTrueSource().getUniqueID());
            double powerLossMultiplier = -1;
            double powerGainMultiplier = 0;
            double powerMaxGainMultiplier = 0;

            if (killedFaction != null) {
                powerGainMultiplier = 1;
                powerMaxGainMultiplier = 1;
                if (killerFaction != null && fMan.getFaction(killedFaction).relationships.containsKey(killerFaction)) {
                    if (fMan.getFaction(killedFaction).relationships.get(killerFaction).relation == RelationState.ENEMY || fMan.getFaction(killerFaction).relationships.get(killedFaction).relation == RelationState.ENEMY) {
                        powerGainMultiplier *= FactionConfig.powerSubCat.enemyKillMultiplier;
                        powerMaxGainMultiplier *= FactionConfig.powerSubCat.enemyKillMultiplier;
                        powerLossMultiplier *= FactionConfig.powerSubCat.deathByEnemyMultiplier;
                    }
                }

                switch(fMan.getPlayer(e.getEntity().getUniqueID()).factionRank){
                    case LIEUTENANT:
                        powerGainMultiplier *= FactionConfig.powerSubCat.lieutenantMultiplier;
                        powerLossMultiplier *= FactionConfig.powerSubCat.lieutenantMultiplier;
                        powerMaxGainMultiplier *= FactionConfig.powerSubCat.lieutenantMultiplier;
                        break;
                    case OFFICER:
                        powerGainMultiplier *= FactionConfig.powerSubCat.officerMultiplier;
                        powerLossMultiplier *= FactionConfig.powerSubCat.officerMultiplier;
                        powerMaxGainMultiplier *= FactionConfig.powerSubCat.officerMultiplier;
                        break;
                    case OWNER:
                        powerGainMultiplier *= FactionConfig.powerSubCat.ownerMultiplier;
                        powerLossMultiplier *= FactionConfig.powerSubCat.ownerMultiplier;
                        powerMaxGainMultiplier *= FactionConfig.powerSubCat.ownerMultiplier;
                        break;
                }
            }



            fMan.getPlayer(e.getEntity().getUniqueID()).addPower((int) Math.ceil(FactionConfig.powerSubCat.deathPowerLoss * powerLossMultiplier));
            e.getEntity().sendMessage(new TextComponentString(TextFormatting.RED + "You've lost power, your power is now: " + fMan.getPlayer(e.getEntity().getUniqueID()).power.power + "/" + fMan.getPlayer(e.getEntity().getUniqueID()).power.maxPower));
            if (powerGainMultiplier != 0){
                fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).addMaxPower((int) Math.ceil(FactionConfig.powerSubCat.killMaxPowerGain * powerMaxGainMultiplier));
                fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).addPower((int) Math.ceil(FactionConfig.powerSubCat.killPowerGain * powerGainMultiplier));
                e.getSource().getTrueSource().sendMessage(new TextComponentString(TextFormatting.GREEN + "You've gained power, your power is now: " + fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).power.power + "/" + fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).power.maxPower));
            }

        }
    }

    @SubscribeEvent
    public static void enterChunk(EntityEvent.EnteringChunk e){
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

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent e){
        FactionManager fMan = FactionManager.getInstance();
        ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(e.getPlayer().dimension, e.getPos().getX(), e.getPos().getZ());
        UUID chunkOwner = fMan.getChunkOwningFaction(chunk);
        // Check the chunk is owned
        if (chunkOwner != null) {
            UUID playerFaction = fMan.getPlayersFactionID(e.getPlayer().getUniqueID());
            // Check the chunk isn't owned by the player breaking
            if (!chunkOwner.equals(playerFaction)) {

                RelationState relation = fMan.getFaction(chunkOwner).getRelation(playerFaction);
                // Check the relations
                if (relation == null || (!(FactionConfig.factionSubCat.allyBuild && relation == RelationState.ALLY) && !(FactionConfig.factionSubCat.enemyBuild && (relation == RelationState.ENEMY && fMan.getFaction(playerFaction).getRelation(chunkOwner) == RelationState.ENEMY)))) {
                    e.getPlayer().sendMessage(new TextComponentString(TextFormatting.RED + "You're not allowed to build on " + fMan.getFaction(chunkOwner).name + "'s Land"));
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent e){
        if (e.getEntity() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(e.getEntity().dimension, e.getPos().getX(), e.getPos().getZ());
            UUID chunkOwner = fMan.getChunkOwningFaction(chunk);
            // Check the chunk is owned
            if (chunkOwner != null) {
                UUID playerFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
                // Check the chunk isn't owned by the player placing
                if (!chunkOwner.equals(playerFaction)) {
                    RelationState relation = fMan.getFaction(chunkOwner).getRelation(playerFaction);
                    // Check the relations
                    if (relation == null || (!(FactionConfig.factionSubCat.allyBuild && relation == RelationState.ALLY) && !(FactionConfig.factionSubCat.enemyBuild && (relation == RelationState.ENEMY && fMan.getFaction(playerFaction).getRelation(chunkOwner) == RelationState.ENEMY)))) {
                        e.getEntity().sendMessage(new TextComponentString(TextFormatting.RED + "You're not allowed to build on " + fMan.getFaction(chunkOwner).name + "'s Land"));
                        e.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerAttack(LivingAttackEvent e){
        if (e.getEntity() instanceof EntityPlayer && e.getSource().getTrueSource() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            UUID attackedPlayerFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
            UUID attackingPlayerFaction = fMan.getPlayersFactionID(e.getSource().getTrueSource().getUniqueID());
            if (attackedPlayerFaction != null && attackingPlayerFaction != null){
                if (attackingPlayerFaction.equals(attackedPlayerFaction) && !fMan.getFaction(attackedPlayerFaction).hasFlag("FriendlyFire")){
                    e.getSource().getTrueSource().sendMessage(new TextComponentString(TextFormatting.RED + "You cannot damage other members of your faction"));
                    e.setCanceled(true);
                }
            }
        }
    }
}
