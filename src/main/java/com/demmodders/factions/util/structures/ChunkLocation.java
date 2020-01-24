package com.demmodders.factions.util.structures;

public class ChunkLocation {
    public int dim, x,z;

    public ChunkLocation(int Dim, int X, int Z){
        dim = Dim;
        x = X;
        z = Z;
    }

    public static ChunkLocation coordsToChunkCoords(int Dim, double X,  double Z){
        return new ChunkLocation(Dim, ((int)X) >> 4, ((int)Z) >> 4);
    }
}
