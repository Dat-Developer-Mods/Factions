package com.demmodders.factions.delayedevents;

import com.demmodders.datmoddingapi.delayedexecution.delayedevents.BaseDelayedEvent;

public class PowerIncrease extends BaseDelayedEvent {

    public PowerIncrease(int Delay) {
        super(Delay);
    }

    @Override
    public boolean shouldRequeue(boolean hasFinished) {
        if (hasFinished){

        }
        return true;
    }
}
