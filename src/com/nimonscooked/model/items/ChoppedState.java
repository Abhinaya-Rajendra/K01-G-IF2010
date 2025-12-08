package com.nimonscooked.model.items;

// State saat bahan sudah matang sempurna (siap plating)
public class ChoppedState implements IngredientState {
    @Override
    public String getName() { return "CHOPPED"; }
}