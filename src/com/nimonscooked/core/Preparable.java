package com.nimonscooked.core;

public interface Preparable {
    // --- WAJIB SESUAI SPESIFIKASI  ---
    boolean canBeChopped();
    boolean canBeCooked();
    boolean canBePlacedOnPlate();
    
    void chop();
    void cook();
}