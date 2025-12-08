package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.KitchenUtensil;

public class TrashStation extends Station {

    public TrashStation(int x, int y) {
        super(x, y);
    }

    @Override
    public void interact(Chef chef) {
        // Trigger: Drop action
        Object heldItem = chef.getInventory().getItem();
        
        if (heldItem != null) {
            // Logika Spek [288]:
            // "Jika item adalah kitchen utensils, hanya isinya saja yang dihapus"
            
            if (heldItem instanceof KitchenUtensil) {
                KitchenUtensil utensil = (KitchenUtensil) heldItem;
                if (!utensil.isEmpty()) {
                    System.out.println("Emptying " + utensil.getName() + " into trash.");
                    utensil.clearContents();
                    
                    // Khusus Panci/Wajan: Matikan kompor/timer jika isinya dibuang
                    // Kita perlu casting ke BoilingPot/FryingPan untuk stop thread
                    if (utensil instanceof com.nimonscooked.model.items.BoilingPot) {
                        ((com.nimonscooked.model.items.BoilingPot) utensil).stopCooking();
                    } else if (utensil instanceof com.nimonscooked.model.items.FryingPan) {
                        ((com.nimonscooked.model.items.FryingPan) utensil).stopCooking();
                    }
                } else {
                    System.out.println("Utensil is already empty.");
                }
                // Wadah (Panci/Piring) TETAP di tangan chef
            } 
            else {
                // Jika item biasa (Ingredient/Dish tanpa piring), buang objeknya
                System.out.println("Trashing " + heldItem.toString());
                chef.getInventory().takeItem(); // Hapus dari tangan
            }
        }
    }
}