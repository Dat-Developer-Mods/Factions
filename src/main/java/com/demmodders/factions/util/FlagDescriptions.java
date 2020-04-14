package com.demmodders.factions.util;

import com.demmodders.factions.commands.FactionCommandList;
import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;

public class FlagDescriptions {
    public HashMap<String, String> playerFlags = new HashMap<>();
    public HashMap<String, String> adminFlags = new HashMap<>();

    public FlagDescriptions(){}

    public static FlagDescriptions getFlagDescriptions(){
        try {
            Gson gson = new Gson();
            File json = new File(FactionCommandList.class.getClassLoader().getResource("JSON/Flags.json").getFile());
            Reader reader = new FileReader(json);
            return gson.fromJson(reader, FlagDescriptions.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
