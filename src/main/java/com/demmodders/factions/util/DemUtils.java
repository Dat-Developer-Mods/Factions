package com.demmodders.factions.util;

import com.demmodders.factions.util.enums.FactionChatMode;

import java.util.ArrayList;

public class DemUtils {
    /**
     * Calculate the age based of a starting time and the given time
     *
     * @param startingDate starting Time in millis
     * @return the age in millis
     */
    public static long calculateAge(Long startingDate) {
        long time = System.currentTimeMillis();
        return time - startingDate;
    }

    public static int clamp(int Value, int Min, int Max) {
        if (Value < Min) return Min;
        else return Math.min(Value, Max);
    }

    public static float clamp(float Value, float Min, float Max) {
        if (Value < Min) return Min;
        else return Math.min(Value, Max);
    }

    public static double clamp(double Value, double Min, double Max) {
        if (Value < Min) return Min;
        else return Math.min(Value, Max);
    }
}
