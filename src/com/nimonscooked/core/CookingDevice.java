package com.nimonscooked.core;

public interface CookingDevice {
    // --- Method Wajib ---
    boolean isPortable();
    int capacity();
    boolean canAccept(Preparable ingredient); 
    void addIngredient(Preparable ingredient);
    
    void startCooking();
    void stopCooking(); // <--- BARIS INI DITAMBAHKAN
    
    // --- Helper Tambahan ---
    boolean isCooking();
    int getCookingProgress();
}