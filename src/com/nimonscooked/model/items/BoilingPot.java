package com.nimonscooked.model.items;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class BoilingPot extends KitchenUtensil implements CookingDevice {
    private Thread cookingThread;
    private boolean isCooking = false;
    private int progress = 0; 
    private long startTime;
    private long totalElapsedTime = 0; // Menyimpan waktu yang sudah berjalan sebelum dipause
    
    private final int TIME_TO_COOK = 12000; 
    private final int TIME_TO_BURN = 24000; 

    public BoilingPot() {
        super("Boiling Pot");
    }

    @Override
    public boolean isPortable() { return true; }

    @Override
    public int capacity() { return 1; }

    @Override
    public boolean canAccept(Preparable item) {
        if (!contents.isEmpty()) return false;
        
        if (item instanceof Ingredient) {
            Ingredient ing = (Ingredient) item;
            IngredientType type = ing.getType();
            // Hanya terima Pasta / Rice
            return (type == IngredientType.PASTA || type == IngredientType.RICE);
        }
        return false;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            this.totalElapsedTime = 0; // Reset timer untuk bahan baru
            startCooking(); 
        }
    }

    @Override
    public void startCooking() {
        if (isCooking || contents.isEmpty()) return;
        
        isCooking = true;
        // Logic Resume: Waktu mulai mundur ke belakang sebanyak durasi yang sudah lewat
        startTime = System.currentTimeMillis() - totalElapsedTime;
        
        cookingThread = new Thread(() -> {
            try {
                System.out.println("Boiling started...");
                while (isCooking) {
                    // Safety check: jika isi kosong tiba-tiba
                    if (contents.isEmpty()) { 
                        stopCooking(); 
                        return; 
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    Ingredient ing = (Ingredient) contents.get(0);
                    
                    // FASE 1: COOKING
                    if (elapsed < TIME_TO_COOK) {
                        progress = (int) ((elapsed / (double) TIME_TO_COOK) * 100);
                        if (!(ing.getState() instanceof CookingState)) ing.cook();
                    } 
                    // FASE 2: COOKED (Bar Penuh, tapi timer jalan terus menuju Burn)
                    else if (elapsed >= TIME_TO_COOK && elapsed < TIME_TO_BURN) {
                        progress = 100; 
                        if (!(ing.getState() instanceof CookedState)) {
                            ing.changeState(new CookedState());
                            System.out.println("Item Cooked!");
                        }
                    } 
                    // FASE 3: BURNED
                    else if (elapsed >= TIME_TO_BURN) {
                        if (!(ing.getState() instanceof BurnedState)) {
                            ing.changeState(new BurnedState());
                            System.out.println("Item Burned!");
                        }
                    }

                    Thread.sleep(100); 
                }
            } catch (InterruptedException e) {
                System.out.println("Cooking Interrupted.");
            }
        });
        cookingThread.start();
    }
    
    @Override
    public void stopCooking() {
        if (!isCooking) return;
        
        // Logic Pause: Simpan durasi yang sudah berjalan
        totalElapsedTime = System.currentTimeMillis() - startTime;
        
        isCooking = false;
        progress = 0; // Visual bar direset (akan hilang saat di tangan chef)
    }

    @Override
    public boolean isCooking() { return isCooking; }

    @Override
    public int getCookingProgress() { return progress; }
}