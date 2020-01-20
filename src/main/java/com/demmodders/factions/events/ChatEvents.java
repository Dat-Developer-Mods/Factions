package com.demmodders.factions.events;

import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.FactionManager;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = Factions.MODID)
public class ChatEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent
    public static void ServerChatEvent(ServerChatEvent e){
        if(FactionManager.getInstance().getPlayer(e.getPlayer().getUniqueID()).useFactionChat){
            e.setCanceled(true);
        }
        LOGGER.info(Factions.NAME + " " + e.toString());
    }
}
