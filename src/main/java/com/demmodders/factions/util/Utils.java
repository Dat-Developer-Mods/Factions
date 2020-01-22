package com.demmodders.factions.util;

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
