package com.demmodders.factions.commands;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedHashMap;

public class FactionCommandList {
    private static LinkedHashMap<String, String> commands = null;
    private static LinkedHashMap<String, String> adminCommands = new LinkedHashMap<>();

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
        try {
            Gson gson = new Gson();
            File json = new File(FactionCommandList.class.getClassLoader().getResource("JSON/Commands.json").getFile());
            Reader reader = new FileReader(json);
            Commands commandsList = gson.fromJson(reader, Commands.class);
            commands = new LinkedHashMap<>(commandsList.commands);
            adminCommands = new LinkedHashMap<>(commandsList.adminCommands);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class Commands{
    public LinkedHashMap<String, String> commands;
    public LinkedHashMap<String, String> adminCommands;
    Commands(){

    }
}
