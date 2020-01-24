package com.demmodders.factions.util.structures;

public class Location {
    public int dim;
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    public Location(){

    }
    public Location(int Dim, double x, double y, double z, float pitch, float yaw){
        this.dim = Dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}

