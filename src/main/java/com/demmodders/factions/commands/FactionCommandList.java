package com.demmodders.factions.commands;

import com.demmodders.factions.Factions;
import com.google.gson.Gson;

import java.io.*;
import java.util.LinkedHashMap;

public class FactionCommandList {
    private static LinkedHashMap<String, String> commands = null;
    private static LinkedHashMap<String, String> adminCommands = null;

    public static File commandFile = null;

    FactionCommandList(){}

    public static LinkedHashMap<String, String> getCommands(){
        if (commands == null){
            loadCommands();
        }
        return commands;
    }

    public static LinkedHashMap<String, String> getAdminCommands(){
        if (adminCommands == null){
            loadCommands();
        }
        return adminCommands;
    }

    private static void loadCommands(){
        Gson gson = new Gson();
        InputStream json;

        if (commandFile != null) {
            try {
                json = new FileInputStream(commandFile);
            } catch (FileNotFoundException e) {
                json = FactionCommandList.class.getClassLoader().getResourceAsStream("JSON/Commands.json");
                Factions.LOGGER.warn("Failed to find flag file, defaulting to default translation");
            }
        } else {
            json = FactionCommandList.class.getClassLoader().getResourceAsStream("JSON/Commands.json");
        }

        InputStreamReader reader = new InputStreamReader(json);
        Commands commandsList = gson.fromJson(reader, Commands.class);
        commands = new LinkedHashMap<>(commandsList.commands);
        adminCommands = new LinkedHashMap<>(commandsList.adminCommands);
    }
}

class Commands{
    public LinkedHashMap<String, String> commands;
    public LinkedHashMap<String, String> adminCommands;
    Commands(){

    }
}
