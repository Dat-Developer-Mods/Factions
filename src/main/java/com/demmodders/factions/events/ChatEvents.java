package com.demmodders.factions.events;

import com.demmodders.factions.Factions;
import com.demmodders.factions.util.enums.FactionChatMode;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.enums.RelationState;
import com.demmodders.factions.util.structures.Relationship;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.NORMAL;

@EventBusSubscriber(modid = Factions.MODID)
public class ChatEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent(priority = NORMAL)
    public static void checkFactionChat(ServerChatEvent e){
        UUID playerID = e.getPlayer().getUniqueID();
        if(FactionManager.getInstance().getPlayer(playerID).factionChat != FactionChatMode.NORMAL) {
            // Cancel event
            e.setCanceled(true);
            // Get faction details
            FactionManager fMan = FactionManager.getInstance();
            UUID factionID = fMan.getPlayersFactionID(playerID);

            // Make message
            ITextComponent message = (new TextComponentString(ChatFormatting.DARK_GREEN + "[Faction Chat]" + ChatFormatting.RESET + "[" + fMan.getFaction(factionID).name + ChatFormatting.RESET + "]")).appendSibling(e.getComponent());
            fMan.sendFactionwideMessage(factionID, message);

            // Send to allies if enabled
            if (fMan.getPlayer(playerID).factionChat == FactionChatMode.ALLY) {
                HashMap<UUID, Relationship> relationships = fMan.getFaction(factionID).relationships;
                for(UUID otherFaction : relationships.keySet()){
                    // Only send message to allies
                    if (relationships.get(otherFaction).relation == RelationState.ALLY) fMan.sendFactionwideMessage(otherFaction, message);
                }
            }
        }
    }
}
