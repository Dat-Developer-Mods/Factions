package com.demmodders.factions.util.structures;

import com.demmodders.factions.util.enums.EClaimResult;

import java.util.UUID;

public class ClaimResult {
    public EClaimResult result;
    public int count;
    public UUID owner;

    public ClaimResult(EClaimResult result, int count, UUID owner) {
        this.result = result;
        this.count = count;
        this.owner = owner;
    }
}
