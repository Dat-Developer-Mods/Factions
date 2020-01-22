package com.demmodders.factions.util;

import com.demmodders.factions.util.enums.FactionChatMode;

import java.util.ArrayList;

public class Utils {
    /**
     * Calulate the age based of a starting time and the given time
     * @param startingDate starting Time in millis
     * @return the age in millis
     */
    public static long calculateAge(Long startingDate){
        long time = System.currentTimeMillis();
        return time - startingDate;
    }
}
