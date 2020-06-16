package com.demmodders.factions.util;

import com.demmodders.factions.commands.FactionCommandList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashMap;

public class FlagDescriptions {
    private static HashMap<String, String> playerFlags = null;
    private static HashMap<String, String> adminFlags = null;

    public FlagDescriptions(){}

    /**
     * Loads the flags from the flags file
     */
    private static void loadFlags(){
        Gson gson = new Gson();
        InputStream json = FactionCommandList.class.getClassLoader().getResourceAsStream("JSON/Flags.json");
        InputStreamReader reader = new InputStreamReader(json);
        Flags items = gson.fromJson(reader, Flags.class);
        playerFlags = new HashMap<>(items.playerFlags);
        adminFlags = new HashMap<>(items.adminFlags);
    }

    /**
     * Returns the flags available to players
     * @return A map of flags to their descriptions
     */
    public static HashMap<String, String> getPlayerFlags(){
        if (playerFlags == null){
            loadFlags();
        }
        return playerFlags;
    }

    /**
     * Returns the flags available to admins
     * @return A map of flags to their descriptions
     */
    public static HashMap<String, String> getAdminFlags(){
        if (adminFlags == null){
            loadFlags();
        }
        return adminFlags;
    }
}

class Flags{
    public HashMap<String, String> playerFlags;
    public HashMap<String, String> adminFlags;

    public Flags(){

    }
}