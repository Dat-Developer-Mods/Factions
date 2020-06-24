package com.demmodders.factions.util.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimResult {
    public int result = 0;
    public int claimedLandCount = 0;
    public int attemptedClaimedLandCount = 0;
    public List<UUID> owners = new ArrayList<>();

    public ClaimResult(){}

    ClaimResult(int Result, int ClaimedLand, int AttemptedClaimedLand) {
        result = Result;
        claimedLandCount = ClaimedLand;
        attemptedClaimedLandCount = AttemptedClaimedLand;
    }
}
