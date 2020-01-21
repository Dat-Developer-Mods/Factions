package com.demmodders.factions.commands;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class CommandRegister {
    public static void RegisterPermissionNodes(){
        // Default
        PermissionAPI.registerNode("demfactions.faction.default", DefaultPermissionLevel.ALL, "Enables a user to use basic faction features, like joining factions, viewing invites");
        PermissionAPI.registerNode("demfactions.faction.manage", DefaultPermissionLevel.ALL, "Enables a user to use faction management commands, like ally, invite, and claim");
        PermissionAPI.registerNode("demfactions.faction.create", DefaultPermissionLevel.ALL, "Enables a user to create a faction of their own");
        PermissionAPI.registerNode("demfactions.faction.info", DefaultPermissionLevel.ALL, "Enables a user to look up info about factions, like a factions info, or a list of all the factions");
        PermissionAPI.registerNode("demfactions.faction.claim", DefaultPermissionLevel.ALL, "Enables a user to claim land for their faction");

        // Admin
        PermissionAPI.registerNode("demfactions.admin", DefaultPermissionLevel.OP, "Enables a user to use /factionadmin to manage factions");
    }

    public static void RegisterCommands(FMLServerStartingEvent e){
        e.registerServerCommand(new FactionCommand());
        e.registerServerCommand(new FAdminCommand());
    }
}
