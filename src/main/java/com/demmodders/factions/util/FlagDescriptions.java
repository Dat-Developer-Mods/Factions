package com.demmodders.factions.util;

import com.google.gson.Gson;

import java.io.InputStream;
import java.util.HashMap;

public class FlagDescriptions {
    private HashMap<String, String> playerFlags = new HashMap<>();
    private HashMap<String, String> adminFlags = new HashMap<>();

    public FlagDescriptions(){}

    public static FlagDescriptions loadFlagDescriptions(){
        Gson gson = new Gson();
        InputStream json = FlagDescriptions.class.getClassLoader().getResourceAsStream("JSON/Flags.json");
        if (json != null) {
            return gson.fromJson(json.toString(), FlagDescriptions.class);
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
