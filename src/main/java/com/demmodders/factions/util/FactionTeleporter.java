package com.demmodders.factions.util;

import com.demmodders.factions.util.structures.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class FactionTeleporter implements ITeleporter {
    Location location;

    public FactionTeleporter(Location Destination){
        location = Destination;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        if (entity instanceof EntityPlayerMP){
            // We only need to update their position
            ((EntityPlayerMP)entity).connection.setPlayerLocation(location.x, location.y, location.z, location.yaw, location.pitch);
        }
    }
}
