package com.demmodders.factions.util.structures;

import com.demmodders.factions.util.enums.EUnclaimResult;

public class UnclaimResult {
    public EUnclaimResult result;
    public int count;

    public UnclaimResult(EUnclaimResult result, int count) {
        this.result = result;
        this.count = count;
    }
}
