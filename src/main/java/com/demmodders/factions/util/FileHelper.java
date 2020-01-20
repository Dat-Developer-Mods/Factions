package com.demmodders.factions.util;

import java.io.File;

public class FileHelper{
    public static File getbaseDir(){
        File dir =  new File("./Factions");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create faction Directory";
        }
        return dir;
    }

    public static File getFactionsDir(){
        File dir = new File(getbaseDir(), "Factions");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create factions Directory";
        }
        return dir;
    }

    public static File getClaimedDir(){
        File dir = new File(getbaseDir(), "Claimed");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create Claimed Directory";
        }
        return dir;
    }

    public static File getPlayerDir(){
        File dir = new File(getbaseDir(), "Players");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create Player Directory";
        }
        return dir;
    }
}