package com.demmodders.factions;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = Factions.MODID)
public class EventsChat{
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent
    public static void checkCrowding(EntityJoinWorldEvent e){
        LOGGER.info(Factions.NAME + " " + e.toString());
    }

    @SubscribeEvent
    public static void ServerChatEvent(ServerChatEvent e){
        LOGGER.info(Factions.NAME + " " + e.toString());
    }

    @SubscribeEvent
    public static void clientChatReceived(ServerChatEvent e) {
        if(!Minecraft.getMinecraft().world.isRemote) {
            LOGGER.info(Factions.NAME + " " + e.toString());
        }
    }
}
