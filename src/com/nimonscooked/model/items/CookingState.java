package com.nimonscooked.model.items;

// State saat bahan sedang berada di dalam panci/wajan (proses timer berjalan)
public class CookingState implements IngredientState {
    @Override
    public String getName() { return "COOKING"; }
}