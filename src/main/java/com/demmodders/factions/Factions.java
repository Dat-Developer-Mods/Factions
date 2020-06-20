package com.demmodders.factions;

import com.demmodders.factions.commands.CommandRegister;
import com.demmodders.factions.commands.FactionCommandList;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FlagDescriptions;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mod(modid = Factions.MODID, name = Factions.NAME, serverSideOnly = true, version = Factions.VERSION, acceptableRemoteVersions  = "*", dependencies = "required-after:datmoddingapi@[1.1.1,)")
public class Factions {
    public static final String MODID = "demfactions";
    public static final String NAME = "Factions";
    public static final String VERSION = "1.0.2";
    public static final String MC_VERSION = "[1.12.2]";

    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);


    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load faction data
        FactionManager.getInstance();
        File translationsDir = new File(event.getModConfigurationDirectory(), "/Dat Factions/Translations");
        if (!translationsDir.exists() || !translationsDir.isDirectory()) {
            if (!translationsDir.mkdir()) {
                LOGGER.error("Failed to create translations directory, command description and flag translations won't load");
                return;
            }
        }

        File commandList = new File(translationsDir, "Commands.json");
        if (!commandList.exists()){
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("JSON/Commands.json"), commandList.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to create Commands translations file, command description and flag translations won't load");
                return;
            }
        }
        FactionCommandList.commandFile = commandList;

        File flagList = new File(translationsDir, "Flags.json");
        if (!flagList.exists()){
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("JSON/Flags.json"), flagList.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to create flags translations file, command description and flag translations won't load");
                return;
            }
        }
        FlagDescriptions.flagFile = flagList;
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
