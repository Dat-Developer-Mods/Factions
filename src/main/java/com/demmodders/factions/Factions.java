package com.demmodders.factions;

import com.demmodders.factions.commands.CommandRegister;
import com.demmodders.factions.faction.FactionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Factions.MODID, name = Factions.NAME, serverSideOnly = true, version = Factions.VERSION, acceptableRemoteVersions  = "*", dependencies = "required-after:datmoddingapi")
public class Factions {
    public static final String MODID = "demfactions";
    public static final String NAME = "Factions";
    public static final String VERSION = "0.0.1";
    public static final String MC_VERSION = "[1.12.2]";

    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);


    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load faction data
        FactionManager.getInstance();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info(Factions.NAME + " says hi!");
        // Register permissions
        CommandRegister.RegisterPermissionNodes();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent e){
        // register commands
        CommandRegister.RegisterCommands(e);
    }
}
