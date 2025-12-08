package com.nimonscooked.model.items;

// State saat bahan sudah matang sempurna (siap plating)
public class CookedState implements IngredientState {
    @Override
    public String getName() { return "COOKED"; }
}