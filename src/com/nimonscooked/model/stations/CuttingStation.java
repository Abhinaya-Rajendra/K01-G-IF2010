package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import com.nimonscooked.model.items.KitchenUtensil; // Tambah import
import com.nimonscooked.model.items.Plate; // Tambah import
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.GameTickable; 

public class CuttingStation extends Station implements GameTickable { 
    private Item currentItem;
    
    private long lastUpdateTime = 0; 
    private int chopProgress = 0;   
    private final int CHOP_TIME = 3000; 
    private Chef activeChef = null;

    public CuttingStation(int x, int y) {
        super(x, y);
    }

    public Item getItem() { return currentItem; }
    public void setItem(Item item) { this.currentItem = item; }
    
    public int getProgress() {
        if (chopProgress >= CHOP_TIME) return 100;
        return (int) (chopProgress / (double) CHOP_TIME * 100);
    }
    public boolean isChopping() {
        return activeChef != null; 
    }

    @Override
    public void interact(Chef chef) {
        Item heldItem = chef.getInventory().getItem();

        // FIX BARU: SCOOPING (Utensil di stasiun, Plate di tangan)
        if (currentItem instanceof KitchenUtensil && heldItem instanceof Plate) {
            ((KitchenUtensil) currentItem).moveContentsTo((Plate) heldItem);
            return; // Interaksi scoop selesai
        } 
        
        // 1. KASUS 1: Chef taruh item (Hanya jika stasiun kosong)
        if (heldItem != null && currentItem == null) {
            currentItem = chef.getInventory().takeItem();
            this.chopProgress = 0; 
            if (isChopping()) forceStopChopping(activeChef); 
            System.out.println("Placed " + ((Item)currentItem).getName() + " on Cutting Station.");
            return;
        } 
        
        // 2. KASUS 2: Chef ambil item / Mulai/Lanjutkan/Stop Potong (Hanya jika tangan kosong)
        if (heldItem == null && currentItem != null) {
            
            boolean canChop = currentItem instanceof Ingredient && ((Ingredient) currentItem).canBeChopped();

            if (!canChop) { 
                // KASUS 2A: Ambil item yang tidak dapat diproses (Plate, Utensil, dll.)
                chef.getInventory().setItem(currentItem);
                currentItem = null;
                chopProgress = 0;
                forceStopChopping(chef); 
                System.out.println("Took item (not ingredient) from Cutting Station.");
                return;
            }
            
            // KASUS 2B: Item adalah Ingredient yang BISA dipotong
            if (chopProgress < CHOP_TIME) {
                if (!isChopping()) {
                    startChopping(chef);
                } else if (activeChef == chef) {
                    stopChopping(chef);
                }
                return;
            }
            
            // KASUS 2C: Ambil Item (Jika sudah selesai dipotong)
            if (chopProgress >= CHOP_TIME) {
                Ingredient ing = (Ingredient) currentItem;
                chef.getInventory().setItem(ing);
                currentItem = null;
                chopProgress = 0;
                forceStopChopping(chef);
                System.out.println("Took chopped item from Cutting Station.");
            }
        }
    }
    
    private void startChopping(Chef chef) {
        activeChef = chef;
        activeChef.setBusy(true); 
        lastUpdateTime = System.currentTimeMillis();
        GameModel.getInstance().startStationProgress(this);
        System.out.println("Started chopping progress: " + chopProgress);
    }

    private void stopChopping(Chef chef) {
        if (activeChef == chef) {
            GameModel.getInstance().stopStationProgress(this);
            activeChef = null;
            chef.setBusy(false); 
            System.out.println("Chopping manually paused, progress saved.");
        }
    }

    public void forceStopChopping(Chef chef) {
        if (activeChef == chef) {
             GameModel.getInstance().stopStationProgress(this);
             activeChef = null;
             chef.setBusy(false);
             System.out.println("Chopping forcibly stopped due to distance.");
        }
    }

    // --- IMPLEMENTASI GAMETICKABLE ---
    @Override
    public void updateByTick() {
        if (activeChef == null || GameModel.getInstance().isPaused() || !(currentItem instanceof Ingredient)) return;
        
        if (isChefTooFar(activeChef)) { 
            forceStopChopping(activeChef); 
            return;
        }

        long now = System.currentTimeMillis();
        chopProgress += (now - lastUpdateTime);
        lastUpdateTime = now; 

        if (chopProgress >= CHOP_TIME) {
            Ingredient ing = (Ingredient) currentItem;
            ing.chop(); 
            
            GameModel.getInstance().stopStationProgress(this);
            activeChef.setBusy(false); 
            activeChef = null;
            System.out.println("Chop chop! Item processed.");
        }
    }
    
    private boolean isChefTooFar(Chef chef) {
        double dx = getGridX() - chef.getWorldX();
        double dy = getGridY() - chef.getWorldY();
        return dx * dx + dy * dy > 1.5 * 1.5; 
    }
}