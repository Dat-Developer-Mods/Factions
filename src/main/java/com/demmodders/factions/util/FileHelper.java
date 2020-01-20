package com.demmodders.factions.util;

import com.demmodders.factions.Factions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileHelper{
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    public static File openFile(File theFile){
        try {
            boolean success = true;
            if (!theFile.getParentFile().exists()) success = theFile.getParentFile().mkdirs();
            if (!theFile.exists()) success = theFile.createNewFile();
            if (!success) throw new IOException();
            return theFile;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(com.demmodders.factions.Factions.MODID + " Unable to create file " + theFile.getPath() + "\n This faction data will not persist past a server restart");
        }
        return null;
    }

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