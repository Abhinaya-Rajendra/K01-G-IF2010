package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class BoilingPot extends KitchenUtensil {

    public BoilingPot() {
        super("Boiling Pot");
        // Konfigurasi Waktu Masak (Override nilai default KitchenUtensil)
        this.cookTimeMs = 8000;  // 8 Detik matang
        this.burnTimeMs = 16000; // 16 Detik gosong
    }

    @Override
    public boolean canAccept(Preparable item) {
        if (!contents.isEmpty()) return false;
        
        if (item instanceof Ingredient) {
            Ingredient ing = (Ingredient) item;
            IngredientType type = ing.getType();
            // Panci hanya menerima Pasta atau Beras
            return (type == IngredientType.PASTA || type == IngredientType.RICE);
        }
        return false;
    }
    
    // Tidak perlu @Override startCooking/stopCooking lagi!
    // Sudah ditangani oleh KitchenUtensil.
}