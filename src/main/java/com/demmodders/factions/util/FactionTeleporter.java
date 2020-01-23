package com.demmodders.factions.util;

import com.demmodders.factions.util.structures.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class FactionTeleporter implements ITeleporter {
    double x, y, z;

    public FactionTeleporter(double X, double Y, double Z){
        x = X;
        y = Y;
        z = Z;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        if (entity instanceof EntityPlayerMP){
            // We only need to update their position
            ((EntityPlayerMP)entity).setPositionAndUpdate(x, y, z);
        }
    }
}
