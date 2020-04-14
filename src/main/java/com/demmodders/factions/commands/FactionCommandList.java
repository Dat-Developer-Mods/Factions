package com.demmodders.factions.commands;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedHashMap;

public class FactionCommandList {
    public LinkedHashMap<String, String> commands = new LinkedHashMap<>();
    public LinkedHashMap<String, String> adminCommands = new LinkedHashMap<>();

    FactionCommandList(){}

    @Nullable
    public static FactionCommandList getCommands(){
        try {
            Gson gson = new Gson();
            File json = new File(FactionCommandList.class.getClassLoader().getResource("JSON/Commands.json").getFile());
            Reader reader = new FileReader(json);
            return gson.fromJson(reader, FactionCommandList.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
