package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Plate;
import java.util.Stack;

public class WashingStation extends Station {
    
    private Stack<Plate> dirtyQueue;
    private Stack<Plate> cleanResult;
    
    private Thread washingThread;
    private boolean isWashing = false;
    private int currentProgress = 0; 
    private final int WASH_TIME = 3000; 

    public WashingStation(int x, int y) {
        super(x, y);
        this.dirtyQueue = new Stack<>();
        this.cleanResult = new Stack<>();
    }

    // --- METHOD BARU UNTUK PROJECTILE ---
    public void addDirtyPlate() {
        Plate p = new Plate();
        p.setClean(false);
        dirtyQueue.push(p);
        System.out.println("Dirty plate thrown into sink!");
    }

    @Override
    public void interact(Chef chef) {
        Object heldItem = chef.getInventory().getItem();
        
        // KASUS 1: DROP Piring Kotor
        if (heldItem instanceof Plate) {
            Plate p = (Plate) heldItem;
            if (p.isClean()) {
                System.out.println("Cannot wash a clean plate!");
                return;
            }
            dirtyQueue.push(p);
            chef.getInventory().takeItem(); 
            System.out.println("Plate queued for washing.");
        }
        
        // KASUS 2: PICKUP Bersih / ACTION Cuci
        else if (heldItem == null) {
            if (!cleanResult.isEmpty()) {
                chef.getInventory().setItem(cleanResult.pop());
                System.out.println("Took a clean plate.");
                return;
            }
            
            if (!dirtyQueue.isEmpty()) {
                toggleWashing(chef);
            } else {
                System.out.println("Washing station empty.");
            }
        }
    }
    
    private void toggleWashing(Chef chef) {
        if (isWashing) {
            pauseWashing();
            chef.setBusy(false); 
        } else {
            startWashing(chef);
        }
    }

    private void startWashing(Chef chef) {
        if (dirtyQueue.isEmpty() || isWashing) return;
        
        isWashing = true;
        chef.setBusy(true); 
        
        washingThread = new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis() - (long)((currentProgress / 100.0) * WASH_TIME); 
                
                while (isWashing && currentProgress < 100) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    currentProgress = (int) ((elapsed / (double) WASH_TIME) * 100);
                    
                    if (currentProgress >= 100) {
                        finishOnePlate();
                        isWashing = false; 
                        currentProgress = 0;
                        chef.setBusy(false); 
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                System.out.println("Washing interrupted");
            }
        });
        washingThread.start();
    }
    
    private void pauseWashing() {
        isWashing = false;
    }
    
    private void finishOnePlate() {
        if (!dirtyQueue.isEmpty()) {
            Plate p = dirtyQueue.pop();
            p.setClean(true); 
            cleanResult.push(p);
        }
    }

    public void forceStopWashing(Chef chef) {
        if (isWashing) {
            pauseWashing();
            chef.setBusy(false);
        }
    }

    public boolean isWashing() { return isWashing; }
    public int getProgress() { return currentProgress; }
    public int getDirtyCount() { return dirtyQueue.size(); }
    public int getCleanCount() { return cleanResult.size(); }
}