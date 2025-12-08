package com.nimonscooked.model.items;

public interface IngredientState {
    // Menghapus method 'process' generik, kita pindahkan logika transisi ke Ingredient
    // agar lebih fleksibel menangani berbagai tipe transisi (chop vs cook)
    String getName(); 
}