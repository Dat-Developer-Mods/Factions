package com.demmodders.factions.util;

import com.demmodders.factions.commands.FactionCommandList;
import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;

public class FlagDescriptions {
    private HashMap<String, String> playerFlags = new HashMap<>();
    private HashMap<String, String> adminFlags = new HashMap<>();

    public FlagDescriptions(){}

    public static FlagDescriptions loadFlagDescriptions(){
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

    private static FlagDescriptions instance = null;

    public static FlagDescriptions getInstance(){
        if (instance == null){
            instance = loadFlagDescriptions();
        }
        return instance;
    }
}
