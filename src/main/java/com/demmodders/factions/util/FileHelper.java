package com.demmodders.factions.util;

import com.demmodders.factions.Factions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FileHelper{
    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);

    /**
     * Safely opens the given file, creating the required directories and file if needed
     * @param theFile The file to open
     * @return The opened file
     */
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

    /**
     * Gets the base directory the faction data is stored in
     * @return A File object at the base directory
     */
    public static File getBaseDir(){
        File dir =  new File("./Factions");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create faction Directory";
        }
        return dir;
    }

    /**
     * Gets the directory the faction data is stored in
     * @return A file object at the directory the faction data is stored in
     */
    public static File getFactionsDir(){
        File dir = new File(getBaseDir(), "Factions");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create factions Directory";
        }
        return dir;
    }

    /**
     * Gets the directory the claimed chunks data is stored in
     * @return A file object at the directory the claimed chunk data is stored in
     */
    public static File getClaimedDir(){
        File dir = new File(getBaseDir(), "Claimed");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create Claimed Directory";
        }
        return dir;
    }

    /**
     * Gets the directory the player data is stored in
     * @return A file object at the directory the player data is stored in
     */
    public static File getPlayerDir(){
        File dir = new File(getBaseDir(), "Players");
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            assert success : "Unable to create Player Directory";
        }
        return dir;
    }
}