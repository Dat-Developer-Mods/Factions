package com.demmodders.factions.util.structures;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Relationships {
    UUID Faction;
    RelationState relation;
    long timeOfHappening;
}

enum RelationState{
    @SerializedName("0")
    ENEMY,
    @SerializedName("1")
    WAR,
    @SerializedName("2")
    ALLY,
}