package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import java.util.ArrayList;

public class Plate extends KitchenUtensil {
    
    private boolean isClean;

    public Plate() {
        super("Plate"); 
        this.isClean = true; // Default bersih saat diambil
    }

    public boolean isClean() {
        return isClean;
    }

    public void setClean(boolean clean) {
        this.isClean = clean;
        if (clean) {
            this.clearContents();
        }
    }

    @Override
    public boolean canAccept(Preparable item) {
        // Piring hanya menerima makanan jika BERSIH
        return isClean;
    }

    @Override
    public void addIngredient(Preparable item) {
        if (isClean) {
            // Panggil method induk yang baru saja kita tambahkan
            super.addIngredient(item);
        } else {
            System.out.println("Cannot place food on a dirty plate!");
        }
    }

    // // --- FIX ERROR: IMPLEMENTASI METHOD YANG HILANG ---

    // // 1. Implementasi isPortable (Wajib return true untuk Piring)
    // @Override
    // public boolean isPortable() {
    //     return true; 
    // }

    // // 2. Implementasi capacity (Tentukan batas maksimal piring, misal 10)
    // @Override
    // public int capacity() {
    //     return 10; 
    // }
}