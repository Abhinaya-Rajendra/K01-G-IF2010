package com.nimonscooked.model.entities;

import com.nimonscooked.core.Preparable; // Jangan lupa import ini
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

    // Cek apakah isi panci/piring sesuai dengan resep ini
    public boolean matches(KitchenUtensil utensil) {
        if (utensil == null) return false;
        
        // PERBAIKAN 1: Ubah tipe data variabel penampung jadi List<Preparable>
        // Sesuai dengan return type dari utensil.getContents()
        List<Preparable> contents = utensil.getContents();

        // 1. Cek Jumlah Bahan
        if (contents.size() != requiredIngredients.size()) return false;

        // 2. Cek Apakah Semua Bahan MATANG (COOKED)?
        // PERBAIKAN 2: Loop menggunakan Preparable, lalu di-cast ke Ingredient
        for (Preparable item : contents) {
            // Pastikan item adalah Ingredient sebelum dicek
            if (item instanceof Ingredient) {
                Ingredient ing = (Ingredient) item; // Casting
                
                if (!ing.getState().getName().equals("COOKED")) {
                    return false; // Ada yang belum matang
                }
            } else {
                return false; // Item bukan bahan makanan (misal batu/sampah)
            }
        }

        // 3. Cek Kecocokan Jenis Bahan
        List<IngredientType> checklist = new ArrayList<>(requiredIngredients);
        
        for (Preparable item : contents) {
            if (item instanceof Ingredient) {
                Ingredient ing = (Ingredient) item; // Casting lagi
                
                if (checklist.contains(ing.getType())) {
                    checklist.remove(ing.getType()); // Centang bahan ini
                } else {
                    return false; // Bahan salah (tidak ada di resep)
                }
            }
        }

        // Jika checklist kosong, berarti semua bahan cocok
        return checklist.isEmpty();
    }
}