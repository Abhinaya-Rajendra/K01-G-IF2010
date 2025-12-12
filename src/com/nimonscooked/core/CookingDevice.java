package com.nimonscooked.core;

import com.nimonscooked.model.items.Ingredient;

public interface CookingDevice {
    // --- Method Wajib (Properties) ---
    boolean isPortable();
    int capacity();
    
    // --- Method Interaksi Bahan ---
    boolean canAccept(Preparable ingredient); 
    void addIngredient(Preparable ingredient);
    
    // --- Method Logika Memasak (Timer/Thread) ---
    void startCooking();
    void stopCooking(); 
    
    // --- Helper Status ---
    boolean isCooking();
    int getCookingProgress();
}