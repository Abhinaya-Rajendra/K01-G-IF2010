package com.nimonscooked.model.items;

// State saat bahan gosong karena kelamaan dimasak (harus dibuang)
public class BurnedState implements IngredientState {
    @Override
    public String getName() { return "BURNED"; }
}