package com.demmodders.factions.util.structures;

import com.demmodders.datmoddingapi.structures.ChunkLocation;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.UUID;

public class ChunkClaim {
    List<ChunkLocation> locations;
    UUID player;
    UUID faction;
}
