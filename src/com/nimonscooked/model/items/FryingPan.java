package com.nimonscooked.model.items;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class FryingPan extends KitchenUtensil implements CookingDevice {
    private Thread cookingThread;
    private boolean isCooking = false;
    private int progress = 0;
    private long startTime;
    private long totalElapsedTime = 0; // TAMBAHKAN INI
    
    private final int TIME_TO_COOK = 12000; 
    private final int TIME_TO_BURN = 24000;

    public FryingPan() {
        super("Frying Pan");
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
            IngredientType t = ing.getType();
            
            // Frying Pan biasanya menolak PASTA/RICE
            if (t == IngredientType.PASTA || t == IngredientType.RICE) return false;

            // Frying Pan hanya menerima CHOPPED
            return (ing.getState() instanceof ChoppedState);
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
        // --- PERBAIKAN START TIME ---
        // Waktu mulai sekarang = Waktu saat ini - Waktu yang sudah terlewat sebelumnya
        startTime = System.currentTimeMillis() - totalElapsedTime;
        
        cookingThread = new Thread(() -> {
            try {
                System.out.println("Frying started...");
                while (isCooking) {
                    
                    // 🔥 FIX ERROR: CEK JIKA ISI SUDAH KOSONG 🔥
                    if (contents.isEmpty()) { 
                        stopCooking(); // Hentikan thread jika bahan sudah diambil/dituang
                        return; // Keluar dari loop thread
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    Ingredient ing = (Ingredient) contents.get(0);
                    
                    if (elapsed < TIME_TO_COOK) {
                        progress = (int) ((elapsed / (double) TIME_TO_COOK) * 100);
                        if (!(ing.getState() instanceof CookingState)) ing.cook();
                    } 
                    else if (elapsed >= TIME_TO_COOK && elapsed < TIME_TO_BURN) {
                        progress = 100;
                        if (!(ing.getState() instanceof CookedState)) ing.changeState(new CookedState());
                    } 
                    else if (elapsed >= TIME_TO_BURN) {
                        if (!(ing.getState() instanceof BurnedState)) ing.changeState(new BurnedState());
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