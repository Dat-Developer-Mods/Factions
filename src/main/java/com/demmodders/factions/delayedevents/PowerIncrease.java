package com.demmodders.factions.delayedevents;

import com.demmodders.datmoddingapi.delayedexecution.delayedevents.BaseDelayedEvent;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import net.minecraft.entity.player.EntityPlayerMP;

public class PowerIncrease extends BaseDelayedEvent {
    public boolean cancelled = false;
    public EntityPlayerMP player;

    public PowerIncrease(int Delay, EntityPlayerMP Player) {
        super(Delay);
        player = Player;
    }

    @Override
    public void execute() {
        FactionManager.getInstance().getPlayer(player.getUniqueID()).addMaxPower(FactionConfig.powerSubCat.maxPowerGainAmount);
        FactionManager.getInstance().getPlayer(player.getUniqueID()).addPower(FactionConfig.powerSubCat.powerGainAmount);
    }

    @Override
    public boolean canExecute() {
        if (player.hasDisconnected()){
            cancelled = true;
        }
        return super.canExecute() && !cancelled;
    }

    @Override
    public boolean shouldRequeue(boolean hasFinished) {
        return !cancelled;
    }
}
