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

    @Override
    public boolean canAccept(Preparable item) {
        // Piring hanya menerima makanan jika BERSIH
        return isClean;
    }

    @Override
    public void addIngredient(Preparable item) {
        if (isClean) {
            super.addIngredient(item);
        } else {
            System.out.println("Cannot place food on a dirty plate!");
        }
    }
}