package com.demmodders.factions.faction;

import com.demmodders.factions.util.structures.ChunkClaim;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ClaimThread extends Thread {
    ConcurrentLinkedQueue<ChunkClaim> claims = new ConcurrentLinkedQueue<>();

    FactionManager fMan;

    public synchronized void start(FactionManager FMan) {
        fMan = FMan;
        super.start();
    }

    @Override
    public void run() {
        while(!isInterrupted()){
            ChunkClaim nextClaim = claims.poll();
            if (nextClaim != null){
                // TODO: Do something
            }
        }
        super.run();
    }
}
