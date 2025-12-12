package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.entities.Recipe;
import com.nimonscooked.model.logic.GameModel; // Diperlukan untuk akses OrderManager
import com.nimonscooked.model.items.IngredientState; // Diperlukan oleh Recipe.matchesPlate
import java.util.List;

public class Plate extends KitchenUtensil {
    
    private boolean isClean;
    private String finishedDishKey; 

    public Plate() {
        super("Plate"); 
        this.isClean = true; 
        this.finishedDishKey = null;
    }

    public boolean isClean() {
        return isClean;
    }

    public void setClean(boolean clean) {
        this.isClean = clean;
        if (clean) {
            this.clearContents();
            this.finishedDishKey = null; // Reset dish key saat dicuci
        }
    }

    @Override public int capacity() { 
        return 4; 
    }

    @Override
    public boolean canAccept(Preparable item) {
        if (!isClean || this.finishedDishKey != null) { 
            return false;
        }
        
        if (!(item instanceof Ingredient)) {
            return false; 
        }
        
        return contents.size() < capacity(); 
    }

    @Override
    public void addIngredient(Preparable item) {
        String itemName = (item instanceof Item) ? ((Item) item).getName() : "Unknown Item";
        
        if (canAccept(item)) {
            contents.add(item); 
            System.out.println("Added " + itemName + " to the plate. Current count: " + contents.size());
            
            checkAndSetCompletedDish(); // Panggil deteksi dish
            
        } else if (!isClean) {
            System.out.println("Cannot place food on a dirty plate!");
        } else if (this.finishedDishKey != null) {
            System.out.println("Dish is ready, cannot add more.");
        } else {
            System.out.println("Plate is full. Cannot add " + itemName);
        }
    }

    // --- GETTER/SETTER DISH KEY (Logika View) ---
    public void setFinishedDishKey(String dishKey) {
        this.finishedDishKey = dishKey;
    }

    public String getFinishedDishKey() {
        return finishedDishKey;
    }

    // --- LOGIKA UTAMA DETEKSI DISH (Diambil dari OrderManager) ---
    public void checkAndSetCompletedDish() {
        this.finishedDishKey = null; 
        
        if (this.contents.isEmpty()) return;

        // Ambil daftar resep dari OrderManager (melalui GameModel)
        try {
            List<Recipe> allRecipes = GameModel.getInstance().getOrderManager().getAllRecipes();
            
            for (Recipe recipe : allRecipes) {
                if (recipe.matchesPlate(this)) {
                    // Jika resep cocok, set key dish final (e.g., pasta_marinara)
                    String recipeName = recipe.getName();
                    String dishKey = recipeName.replace(" ", "_").toLowerCase();
                    this.finishedDishKey = dishKey;
                    System.out.println("Dish completed: " + dishKey);
                    return; 
                }
            }
        } catch (Exception e) {
            // Ini akan terjadi jika GameModel belum diinisialisasi
            System.err.println("Error accessing OrderManager/GameModel: " + e.getMessage());
        }
    }
}