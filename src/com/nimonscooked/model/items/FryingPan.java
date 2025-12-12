package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class FryingPan extends KitchenUtensil {

    public FryingPan() {
        super("Frying Pan");
        this.cookTimeMs = 10000; // 10 Detik matang
        this.burnTimeMs = 20000; // 20 Detik gosong
    }

    @Override
    public boolean canAccept(Preparable item) {
        if (!contents.isEmpty()) return false;
        
        if (item instanceof Ingredient) {
            Ingredient ing = (Ingredient) item;
            // Wajan tidak menerima Pasta/Beras
            if (ing.getType() == IngredientType.PASTA || ing.getType() == IngredientType.RICE) return false;
            
            // Wajan hanya menerima bahan yang sudah dipotong (Chopped)
            return (ing.getState() instanceof ChoppedState);
        }
        return false;
    }
}