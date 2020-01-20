package com.demmodders.factions.events;

import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.NORMAL;

@EventBusSubscriber(modid = Factions.MODID)
public class ChatEvents {
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    @SubscribeEvent(priority = NORMAL)
    public static void ServerChatEvent(ServerChatEvent e){
        if(FactionManager.getInstance().getPlayer(e.getPlayer().getUniqueID()).useFactionChat){
            Faction faction = FactionManager.getInstance().getPlayersFaction(e.getPlayer().getUniqueID());
            if (faction != null) {
                e.setCanceled(true);
                ArrayList<UUID> members = faction.members;
                ITextComponent message = (new TextComponentString("&f[" + faction.name + "&f]")).appendSibling(e.getComponent());
                for (UUID playerid : members) {
                    EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerid);
                    if (player != null) {
                        player.sendMessage(message);
                    }
                }
            } else {
                LOGGER.warn(Factions.NAME + " Player " + e.getPlayer().getName() + " tried to talk in faction chat while not in a faction, this should not be possible");
            }
        }
    }
}
