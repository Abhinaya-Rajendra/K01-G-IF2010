package com.nimonscooked.model.items;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class BoilingPot extends KitchenUtensil implements CookingDevice {
    private Thread cookingThread;
    private boolean isCooking = false;
    private int progress = 0; 
    private long startTime;
    private long totalElapsedTime = 0; // TAMBAHKAN INI
    
    private final int TIME_TO_COOK = 12000; 
    private final int TIME_TO_BURN = 24000; 

    public BoilingPot() {
        super("Boiling Pot");
    }

    // --- IMPLEMENTASI COOKING DEVICE ---

    @Override
    public boolean isPortable() {
        return true; 
    }

    @Override
    public int capacity() {
        return 1; 
    }

    @Override
    public boolean canAccept(Preparable item) {
        if (!contents.isEmpty()) return false;
        
        if (item instanceof Ingredient) {
            Ingredient ing = (Ingredient) item;
            IngredientType type = ing.getType();
            
            return (type == IngredientType.PASTA || type == IngredientType.RICE);
        }
        return false;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            this.totalElapsedTime = 0; // WAJIB RESET TIMER LAMA
            startCooking(); 
    }
}
    @Override
    public void startCooking() {
        if (isCooking || contents.isEmpty()) return;
        
        isCooking = true;
        startTime = System.currentTimeMillis() - totalElapsedTime;
        
        cookingThread = new Thread(() -> {
            try {
                System.out.println("Boiling started...");
                while (isCooking) {
                    
                    // 🔥 FIX ERROR: CEK JIKA ISI SUDAH KOSONG 🔥
                    if (contents.isEmpty()) { 
                        stopCooking(); // Hentikan thread jika bahan sudah diambil/dituang
                        return; // Keluar dari loop thread
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    // Baris 71 yang error ada di sini:
                    Ingredient ing = (Ingredient) contents.get(0);
                    
                    // --- FASE 1: COOKING (0 - 12 detik) ---
                    if (elapsed < TIME_TO_COOK) {
                        progress = (int) ((elapsed / (double) TIME_TO_COOK) * 100);
                        if (!(ing.getState() instanceof CookingState)) {
                             ing.cook(); 
                        }
                    } 
                    // --- FASE 2: COOKED (12 - 24 detik) ---
                    else if (elapsed >= TIME_TO_COOK && elapsed < TIME_TO_BURN) {
                        progress = 100; 
                        if (!(ing.getState() instanceof CookedState)) {
                            System.out.println("Boiling Done! Item is COOKED.");
                            ing.changeState(new CookedState());
                        }
                    } 
                    // --- FASE 3: BURNED (> 24 detik) ---
                    else if (elapsed >= TIME_TO_BURN) {
                        if (!(ing.getState() instanceof BurnedState)) {
                            System.out.println("ALARM! Item is BURNED!");
                            ing.changeState(new BurnedState());
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
        if (!isCooking) return; // Penting: Hanya lakukan jika sedang memasak
        
        // --- PERBAIKAN STOP TIME ---
        // Simpan total waktu yang sudah terlewat
        totalElapsedTime = System.currentTimeMillis() - startTime; 
        
        isCooking = false;
        progress = 0;
    }

    @Override
    public boolean isCooking() { return isCooking; }

    @Override
    public int getCookingProgress() { return progress; }
}