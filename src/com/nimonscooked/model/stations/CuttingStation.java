package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import javax.swing.Timer;

public class CuttingStation extends Station {
    private Item currentItem;
    private Timer chopTimer;
    private int chopProgress = 0;
    private final int CHOP_TIME = 1000; 

    public CuttingStation(int x, int y) {
        super(x, y);
    }

    // --- METHOD BARU UNTUK PROJECTILE ---
    public Item getItem() { return currentItem; }
    public void setItem(Item item) { this.currentItem = item; }

    @Override
    public void interact(Chef chef) {
        // KASUS 1: Chef taruh item
        if (!chef.getInventory().isEmpty() && currentItem == null) {
            currentItem = chef.getInventory().takeItem();
            System.out.println("Placed " + currentItem.getName() + " on Cutting Station.");
            if (chopTimer != null && chopTimer.isRunning()) chopTimer.stop(); 
        } 
        
        // KASUS 2: Chef ambil item / Potong
        else if (chef.getInventory().isEmpty() && currentItem != null) {
            
            boolean canChop = currentItem instanceof Ingredient && ((Ingredient) currentItem).canBeChopped();

            // KASUS 2A: Mulai Memotong
            if (canChop) {
                if (chopTimer != null && chopTimer.isRunning()) {
                    forceStopChop(chef); 
                } else {
                    chef.setBusy(true); 

                    chopTimer = new Timer(50, e -> {
                        chopProgress += 50; 
                        
                        if (chopProgress >= CHOP_TIME) {
                            ((Ingredient) currentItem).chop();
                            System.out.println("Chop chop! Item processed.");
                            
                            chopProgress = 0;
                            ((Timer)e.getSource()).stop();
                            chef.setBusy(false); 
                        }
                    });
                    chopTimer.start();
                    System.out.println("Started chopping...");
                    return; 
                }
            } 
            
            // KASUS 2B: Ambil Item
            chef.setBusy(false);
            chef.getInventory().setItem(currentItem);
            currentItem = null;
            System.out.println("Took item from Cutting Station.");
        }
    }
    
    public void forceStopChop(Chef chef) {
        if (chopTimer != null && chopTimer.isRunning()) {
            chopTimer.stop();
            chef.setBusy(false);
            chopProgress = 0; 
            System.out.println("Chopping interrupted.");
        }
    }
}