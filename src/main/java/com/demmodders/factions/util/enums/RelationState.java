package com.demmodders.factions.util.enums;

import com.google.gson.annotations.SerializedName;

public enum RelationState{
    @SerializedName("0")
    ENEMY,
    @SerializedName("2")
    ALLY,
    @SerializedName("3")
    PENDINGALLY,
    @SerializedName("4")
    PENDINGENEMY
}
