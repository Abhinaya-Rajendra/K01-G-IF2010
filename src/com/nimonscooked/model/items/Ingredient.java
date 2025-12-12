package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.utils.IngredientType;

public class Ingredient extends Item implements Preparable {
    private IngredientType type;
    private IngredientState state;

    public Ingredient(IngredientType type) {
        super(type.toString()); // Ini memanggil constructor Item(String)
        this.type = type;
        this.state = new RawState(); // Default
    }

    public IngredientState getState() { return state; }
    public IngredientType getType() { return type; }

    // --- FIX ERROR 2: Ganti nama 'changeState' jadi 'setState' ---
    public void setState(IngredientState newState) {
        this.state = newState;
        // System.out.println(this.name + " state changed to " + newState.getName());
    }

    // --- IMPLEMENTASI PREPARABLE ---

    @Override
    public void chop() {
        if (canBeChopped()) {
            setState(new ChoppedState());
        }
    }

    @Override
    public void cook() {
        if (canBeCooked()) {
            // KitchenUtensil yang akan mengubah statenya via Thread
            setState(new CookingState());
        }
    }

    @Override
    public boolean canBeChopped() {
        if (type == IngredientType.PASTA || type == IngredientType.BREAD) return false;
        return state instanceof RawState;
    }

    @Override
    public boolean canBeCooked() {
        // Pasta & Dough bisa dimasak dari RAW
        if ((type == IngredientType.PASTA || type == IngredientType.DOUGH) && state instanceof RawState) {
            return true;
        }
        // Bahan lain harus CHOPPED
        // Izinkan juga jika sedang COOKING (untuk resume masak)
        return (state instanceof ChoppedState) || (state instanceof CookingState);
    }

    @Override
    public boolean canBePlacedOnPlate() {
        if (state instanceof BurnedState) return false;
        return true; 
    }
}