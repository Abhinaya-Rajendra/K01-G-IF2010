package com.nimonscooked.model.items;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;

// Pastikan semua Import ini ada agar tidak error "Symbol Not Found"
import java.util.ArrayList;
import java.util.List;

public abstract class KitchenUtensil extends Item implements CookingDevice {

    protected List<Preparable> contents;
    
    // Status Masak
    protected boolean isCooking = false;
    protected int cookingProgress = 0; 
    protected Thread cookingThread;
    
    // Konfigurasi Waktu (Default)
    protected int cookTimeMs = 5000;
    protected int burnTimeMs = 10000;

    public KitchenUtensil(String name) {
        super(name);
        this.contents = new ArrayList<>();
    }

    public List<Preparable> getContents() { return contents; }
    public boolean isEmpty() { return contents.isEmpty(); }
    
    public void clearContents() {
        contents.clear();
        this.cookingProgress = 0;
        this.isCooking = false;
    }
    
    @Override
    public void addIngredient(Preparable item) {
        if (canAccept(item)) {
            contents.add(item);
            this.cookingProgress = 0; 
            startCooking();
        }
    }
    
    @Override
    public abstract boolean canAccept(Preparable item);

    // --- IMPLEMENTASI COOKING DEVICE ---

    @Override public boolean isPortable() { return true; }
    @Override public int capacity() { return 1; }
    @Override public boolean isCooking() { return isCooking; }
    @Override public int getCookingProgress() { return cookingProgress; }

    @Override
    public void startCooking() {
        if (isEmpty()) return;
        if (!(contents.get(0) instanceof Ingredient)) return;
        if (this instanceof Plate) return; 
        if (isCooking) return;
        
        isCooking = true;
        
        cookingThread = new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis() - (long)((cookingProgress / 100.0) * cookTimeMs);
                
                while (isCooking) {
                    if (contents.isEmpty()) { stopCooking(); return; }

                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    cookingProgress = (int) ((elapsed / (double) cookTimeMs) * 100);
                    
                    Ingredient ing = (Ingredient) contents.get(0);
                    IngredientState currentState = ing.getState();
                    
                    // Update State Bahan
                    // Pastikan file BurnedState, CookedState, CookingState sudah dibuat!
                    if (cookingProgress >= 200) { 
                        if (!(currentState instanceof BurnedState)) ing.setState(new BurnedState());
                    } else if (cookingProgress >= 100) { 
                        if (!(currentState instanceof CookedState) && !(currentState instanceof BurnedState)) ing.setState(new CookedState());
                    } else if (cookingProgress > 0) {
                        if (!(currentState instanceof CookingState) && !(currentState instanceof CookedState)) ing.setState(new CookingState());
                    }
                    
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) { }
        });
        cookingThread.start();
    }

    @Override
    public void stopCooking() {
        isCooking = false;
    }

    public void moveContentsTo(Plate targetPlate) {
        if (targetPlate == null) return;
        for (Preparable item : new ArrayList<>(contents)) {
            targetPlate.addIngredient(item);
        }
        this.clearContents();
        this.stopCooking(); 
    }
}