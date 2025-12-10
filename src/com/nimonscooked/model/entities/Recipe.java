package com.nimonscooked.model.entities;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.utils.IngredientType;
import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String name;
    private List<IngredientType> requiredIngredients;

    public Recipe(String name) {
        this.name = name;
        this.requiredIngredients = new ArrayList<>();
    }

    public void addIngredient(IngredientType type) {
        requiredIngredients.add(type);
    }

    public String getName() {
        return name;
    }

    // --- TAMBAHAN BARU: Getter untuk UI ---
    public List<IngredientType> getRequiredIngredients() {
        return requiredIngredients;
    }
    // --------------------------------------

    public boolean matches(KitchenUtensil utensil) {
        if (utensil == null) return false;
        
        List<Preparable> contents = utensil.getContents();

        if (contents.size() != requiredIngredients.size()) return false;

        for (Preparable item : contents) {
            if (item instanceof Ingredient) {
                Ingredient ing = (Ingredient) item; 
                if (!ing.getState().getName().equals("COOKED")) {
                    return false; 
                }
            } else {
                return false; 
            }
        }

        List<IngredientType> checklist = new ArrayList<>(requiredIngredients);
        for (Preparable item : contents) {
            if (item instanceof Ingredient) {
                Ingredient ing = (Ingredient) item;
                if (checklist.contains(ing.getType())) {
                    checklist.remove(ing.getType()); 
                } else {
                    return false; 
                }
            }
        }
        return checklist.isEmpty();
    }
}