package com.nimonscooked.model.entities;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Plate; // Import Plate
import com.nimonscooked.model.items.RawState;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.IngredientState; // Import IngredientState
import com.nimonscooked.utils.IngredientType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<IngredientType> getRequiredIngredients() {
        return requiredIngredients;
    }
    
    // Metode lama matches(KitchenUtensil) - Diasumsikan ini digunakan untuk validasi SERVICE

    // --- METODE BARU: MATCHES PLATE (Digunakan oleh Plate.checkAndSetCompletedDish) ---
    public boolean matchesPlate(Plate plate) {
        List<Preparable> plateContents = plate.getContents();

        // 1. Cek Jumlah Bahan
        if (plateContents.size() != this.requiredIngredients.size()) {
            return false;
        }

        // 2. Cek Kondisi dan Tipe Bahan
        List<IngredientType> requiredTypes = new ArrayList<>(this.requiredIngredients);
        List<IngredientType> plateTypes = new ArrayList<>();

        for (Preparable item : plateContents) {
            if (!(item instanceof Ingredient)) {
                return false; 
            }
            
            Ingredient ing = (Ingredient) item;
            
            // Aturan Umum: Jika sudah diletakkan di piring, tidak boleh RAW (mentah)
            if (ing.getState() instanceof RawState) {
                return false; 
            }
            
            plateTypes.add(ing.getType());
        }

        // 3. Cek Kecocokan Tipe Bahan (mengabaikan urutan)
        Collections.sort(requiredTypes);
        Collections.sort(plateTypes);

        return requiredTypes.equals(plateTypes);
    }
    
    public boolean matches(KitchenUtensil utensil) {
        // Logika validasi service lama Anda, kita ganti agar konsisten dengan matchesPlate
        if (!(utensil instanceof Plate)) return false;
        
        Plate plate = (Plate) utensil;
        
        // Dish hanya valid jika sudah disetel FinishedDishKey (sudah diverifikasi oleh Plate)
        String finishedKey = plate.getFinishedDishKey();
        if (finishedKey == null || finishedKey.isEmpty()) {
            return false;
        }
        
        // Bandingkan apakah key dish yang sudah jadi di piring cocok dengan nama resep ini
        String recipeKey = this.getName().replace(" ", "_").toLowerCase();
        
        return recipeKey.equals(finishedKey);
    }
}