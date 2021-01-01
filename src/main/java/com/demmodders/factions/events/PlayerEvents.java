package com.demmodders.factions.events;

import com.demmodders.datmoddingapi.delayedexecution.DelayHandler;
import com.demmodders.datmoddingapi.structures.ChunkLocation;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.datmoddingapi.util.DemStringUtils;
import com.demmodders.factions.Factions;
import com.demmodders.factions.delayedevents.PowerIncrease;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.enums.ClaimType;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.ClaimResult;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Factions.MODID)
public class PlayerEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent
    public static void playerLogin(PlayerLoggedInEvent e){
        if (FactionManager.getInstance().isPlayerRegistered(e.player.getUniqueID())) {
            // record username for use when they're offline, for listing members
            FactionManager.getInstance().setPlayerLastKnownName(e.player.getUniqueID(), e.player.getName());
        } else {
            LOGGER.info(e.player.getName() + " is not registered with factions, amending");
            FactionManager.getInstance().registerPlayer(e.player);
        }

        // Add event for the player to
        DelayHandler.addEvent(new PowerIncrease(FactionConfig.powerSubCat.powerGainInterval, (EntityPlayerMP) e.player));
    }

    private static double rankModifier(FactionRank rank){
        if (rank != null) {
            switch (rank) {
                case LIEUTENANT:
                    return FactionConfig.powerSubCat.lieutenantMultiplier;
                case OFFICER:
                    return FactionConfig.powerSubCat.officerMultiplier;
                case OWNER:
                    return FactionConfig.powerSubCat.ownerMultiplier;
            }
        }
        return 1.D;
    }

    @SubscribeEvent
    public static void playerKilled(LivingDeathEvent e){
        // TODO: CHECK
        if (e.getEntity() instanceof EntityPlayer && e.getSource().getTrueSource() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            UUID killedFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
            UUID killerFaction = fMan.getPlayersFactionID(e.getSource().getTrueSource().getUniqueID());
            UUID chunkOwner = fMan.getChunkOwningFaction(e.getEntity().dimension, e.getEntity().chunkCoordX, e.getEntity().chunkCoordZ);
            RelationState relation = fMan.getFaction(killedFaction).getRelation(killerFaction);

            double relationModifier = 1.D;

            double landMultiplier = (fMan.getFaction(chunkOwner).hasFlag("bonuspower") ? FactionConfig.flagSubCat.bonusPowerMultiplier : 1.D);
            double rankMultiplier = rankModifier(fMan.getPlayer(e.getEntity().getUniqueID()).factionRank);

            // Power Gain
            // Only gain power if they're not wild
            if (!killedFaction.equals(FactionManager.WILDID)){
                if (relation != null) {
                    switch (relation) {
                        case ENEMY:
                            relationModifier = FactionConfig.powerSubCat.enemyMultiplier;
                            break;
                        case ALLY:
                            relationModifier = FactionConfig.powerSubCat.allyKillMultiplier;
                            break;
                    }
                }

                fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).addMaxPower((int) Math.ceil(FactionConfig.powerSubCat.killMaxPowerGain * landMultiplier * relationModifier * rankMultiplier));
                fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).addPower((int) Math.ceil(FactionConfig.powerSubCat.killPowerGain * landMultiplier * relationModifier * rankMultiplier));
                e.getSource().getTrueSource().sendMessage(new TextComponentString(TextFormatting.GREEN + "You've gained power, your power is now: " + fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).power.power + "/" + fMan.getPlayer(e.getSource().getTrueSource().getUniqueID()).power.maxPower));
            }

            // Power Loss
            if (relation != null) {
                switch (relation) {
                    case ENEMY:
                        relationModifier = FactionConfig.powerSubCat.enemyMultiplier;
                        break;
                    case ALLY:
                        relationModifier = FactionConfig.powerSubCat.killedByAllyMultiplier;
                        break;
                }
            }

            fMan.getPlayer(e.getEntity().getUniqueID()).addPower((int) Math.ceil(-1.D * FactionConfig.powerSubCat.deathPowerLoss * landMultiplier * relationModifier));
            e.getEntity().sendMessage(new TextComponentString(TextFormatting.RED + "You've lost power, your power is now: " + fMan.getPlayer(e.getEntity().getUniqueID()).power.power + "/" + fMan.getPlayer(e.getEntity().getUniqueID()).power.maxPower));
        }
    }

    @SubscribeEvent
    public static void enterChunk(EntityEvent.EnteringChunk e){
        // This fires really weirdly, sometimes 3 times giving: faction land, wild land, faction land, its really weird
        // Make sure its a player
        if (e.getEntity() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            UUID playerFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
            String message = "";

            // TODO: Test
            if (!playerFaction.equals(FactionManager.WILDID) && fMan.getPlayer(e.getEntity().getUniqueID()).autoClaim) {
                ClaimResult result = fMan.claimLand(playerFaction, e.getEntity().getUniqueID(), e.getEntity().dimension, e.getNewChunkX(), e.getNewChunkZ());
                // TODO: Finish
                switch (result.result) {

                }

                e.getEntity().sendMessage(new TextComponentString(message));
            }
            if (e.getOldChunkX() != e.getNewChunkX() || e.getOldChunkZ() != e.getNewChunkZ()) {
                UUID factionID = fMan.getChunkOwningFaction(e.getEntity().dimension, e.getNewChunkX(), e.getNewChunkZ());
                Faction theFaction = fMan.getFaction(factionID);

                if (fMan.isPlayerRegistered(e.getEntity().getUniqueID()) && (fMan.getPlayer(e.getEntity().getUniqueID()).lastFactionLand == null || !fMan.getPlayer(e.getEntity().getUniqueID()).lastFactionLand.equals(factionID))) {
                    message = "";
                    if (factionID.equals(FactionManager.WILDID)) {
                        message = FactionConfig.factionSubCat.wildLandTag;
                    } else if (factionID.equals(FactionManager.SAFEID)) {
                        message = FactionConfig.factionSubCat.safeLandTag;
                    } else if (factionID.equals(FactionManager.WARID)) {
                        message = FactionConstants.TextColour.ENEMY + FactionConfig.factionSubCat.warLandTag;
                    } else if (playerFaction.equals(factionID)) {
                        message = "Now entering " + FactionConstants.TextColour.OWN + "your land";
                    } else {
                        if (theFaction.desc.isEmpty()) message = FactionConfig.factionSubCat.factionLandTagNoDesc;
                        else message = FactionConfig.factionSubCat.factionLandTag;
                    }

                    e.getEntity().sendMessage(new TextComponentString(String.format(DemConstants.TextColour.INFO + message, fMan.getRelationColour(playerFaction, factionID) + theFaction.name + DemConstants.TextColour.INFO, fMan.getRelationColour(playerFaction, factionID) + DemStringUtils.makePossessive((theFaction.name)) + DemConstants.TextColour.INFO, theFaction.desc)));
                    fMan.getPlayer(e.getEntity().getUniqueID()).lastFactionLand = factionID;
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent e){
        FactionManager fMan = FactionManager.getInstance();
        ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(e.getPlayer().dimension, e.getPos().getX(), e.getPos().getZ());
        UUID chunkOwner = fMan.getChunkOwningFaction(chunk);
        if (!fMan.getPlayerCanBuild(chunkOwner, e.getPlayer().getUniqueID())) {
            e.getPlayer().sendMessage(new TextComponentString(DemConstants.TextColour.ERROR + "You're not allowed to build on " + DemStringUtils.makePossessive(fMan.getFaction(chunkOwner).name) + " Land"));
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent e){
        if (e.getEntity() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(e.getEntity().dimension, e.getPos().getX(), e.getPos().getZ());
            UUID chunkOwner = fMan.getChunkOwningFaction(chunk);

            if (!fMan.getPlayerCanBuild(chunkOwner, e.getEntity().getUniqueID())) {
                e.getEntity().sendMessage(new TextComponentString(DemConstants.TextColour.ERROR + "You're not allowed to build on " + DemStringUtils.makePossessive(fMan.getFaction(chunkOwner).name) + " Land"));
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void playerInteract(PlayerInteractEvent.RightClickBlock e) {
        if (FactionConfig.factionSubCat.inventoryBlockRestriction) {
            FactionManager fMan = FactionManager.getInstance();
            ChunkLocation chunk = ChunkLocation.coordsToChunkCoords(e.getEntity().dimension, e.getPos().getX(), e.getPos().getZ());
            UUID chunkOwner = fMan.getChunkOwningFaction(chunk);
            if (!fMan.getPlayerCanBuild(chunkOwner, e.getEntity().getUniqueID())) {
                e.getEntity().sendMessage(new TextComponentString(DemConstants.TextColour.ERROR + "You're not allowed to interact with blocks on " + DemStringUtils.makePossessive(fMan.getFaction(chunkOwner).name) + " Land"));
                e.setUseBlock(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void playerAttack(LivingAttackEvent e){
        if (e.getEntity() instanceof EntityPlayer && e.getSource().getImmediateSource() instanceof EntityPlayer) {
            FactionManager fMan = FactionManager.getInstance();
            UUID attackedPlayerFaction = fMan.getPlayersFactionID(e.getEntity().getUniqueID());
            UUID attackingPlayerFaction = fMan.getPlayersFactionID(e.getSource().getImmediateSource().getUniqueID());
            if (!attackedPlayerFaction.equals(FactionManager.WILDID) && !attackingPlayerFaction.equals(FactionManager.WILDID)){
                if (attackingPlayerFaction.equals(attackedPlayerFaction) && !fMan.getFaction(attackedPlayerFaction).hasFlag("friendlyfire")){
                    e.getSource().getImmediateSource().sendMessage(new TextComponentString(DemConstants.TextColour.ERROR + "You cannot damage other members of your faction"));
                    e.setCanceled(true);
                }
            }
        }
    }
}
