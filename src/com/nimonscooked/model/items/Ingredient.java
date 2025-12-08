package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class Ingredient extends Item implements Preparable {
    private IngredientType type;
    private IngredientState state;

    public Ingredient(IngredientType type) {
        this.type = type;
        this.name = type.toString();
        this.state = new RawState(); // Default RAW [cite: 143]
    }

    public IngredientState getState() { return state; }
    public IngredientType getType() { return type; }

    public void changeState(IngredientState newState) {
        this.state = newState;
        // Opsional: Print debug info
        // System.out.println(this.name + " changed state to " + newState.getName());
    }

    // --- IMPLEMENTASI PREPARABLE  ---

    @Override
    public void chop() {
        if (canBeChopped()) {
            changeState(new ChoppedState());
        }
    }

    @Override
    public void cook() {
        if (canBeCooked()) {
            // Saat mulai dimasak, status berubah jadi COOKING [cite: 143]
            // Nanti alat masak (BoilingPot/FryingPan) yang akan mengubahnya 
            // menjadi COOKED atau BURNED berdasarkan Timer.
            changeState(new CookingState());
        }
    }

    @Override
    public boolean canBeChopped() {
        // Pasta/Roti biasanya tidak dipotong (tergantung resep), tapi mayoritas bahan bisa dipotong saat RAW
        if (type == IngredientType.PASTA || type == IngredientType.BREAD) return false;
        return state instanceof RawState;
    }

    @Override
    public boolean canBeCooked() {
        // [Aturan Map B & D]: Pasta & Adonan Pizza bisa dimasak langsung dari RAW
        if ((type == IngredientType.PASTA || type == IngredientType.DOUGH) && state instanceof RawState) {
            return true;
        }
        // Bahan lain (Daging, Ikan, dll) harus CHOPPED dulu baru bisa dimasak
        return state instanceof ChoppedState;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        // Bahan BURNED tidak boleh ditaruh di piring (harus dibuang)
        if (state instanceof BurnedState) return false;
        
        // Biasanya bahan RAW juga tidak bisa di-plating (kecuali Sushi Map/Nori)
        // Tapi untuk aman, kita izinkan true dulu, validasi resep nanti di ServingCounter
        return true; 
    }
}