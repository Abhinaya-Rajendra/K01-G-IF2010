package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.utils.IngredientType;

public class IngredientStorage extends Station {
    private IngredientType type;

    public IngredientStorage(int x, int y, IngredientType type) {
        super(x, y);
        this.type = type;
    }

    // --- TAMBAHKAN GETTER INI ---
    public IngredientType getType() {
        return type;
    }

    @Override
    public void interact(Chef chef) {
        if (chef.getInventory().isEmpty()) {
            chef.getInventory().setItem(new Ingredient(type));
            System.out.println("Chef took " + type);
        }
    }
}