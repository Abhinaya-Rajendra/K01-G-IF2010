package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;

public class Plate extends KitchenUtensil {
    
    private boolean isClean;

    public Plate() {
        super("Plate"); 
        this.isClean = true; 
    }

    public boolean isClean() {
        return isClean;
    }

    public void setClean(boolean clean) {
        this.isClean = clean;
        if (clean) {
            this.clearContents();
        }
    }

    @Override public int capacity() { 
        return 4; // Kapasitas yang realistis (misalnya, 4)
    }

    @Override
    public boolean canAccept(Preparable item) {
        if (!isClean) {
            return false;
        }
        
        // Cek apakah item yang ditambahkan adalah Ingredient
        if (!(item instanceof Ingredient)) {
             return false; 
        }
        
        // Cek kapasitas
        return contents.size() < capacity(); 
    }

    @Override
    public void addIngredient(Preparable item) {
        // Mendapatkan nama item secara aman untuk logging/debugging
        String itemName = (item instanceof Item) ? ((Item) item).getName() : "Unknown Item";
        
        if (canAccept(item)) {
            // TIDAK MEMANGGIL super.addIngredient() karena logika super memblokir setelah item pertama!
            contents.add(item); 
            
            System.out.println("Added " + itemName + " to the plate. Current count: " + contents.size());
        } else if (!isClean) {
            System.out.println("Cannot place food on a dirty plate!");
        } else {
            // FIX: Menggunakan itemName
            System.out.println("Plate is full. Cannot add " + itemName);
        }
    }
}