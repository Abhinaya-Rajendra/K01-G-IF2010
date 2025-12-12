package com.nimonscooked.model.stations;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.Plate;

public class CookingStation extends Station {
    private CookingDevice device;

    public CookingStation(int x, int y) {
        super(x, y);
    }

    public CookingDevice getDevice() { return device; }
    public void setDevice(CookingDevice device) { this.device = device; }

    @Override
    public void interact(Chef chef) {
        Object heldItem = chef.getInventory().getItem();

        // KASUS A: Ada Alat Masak (Utensil) di Stove
        if (device != null && device instanceof KitchenUtensil) {
            KitchenUtensil utensil = (KitchenUtensil) device;

            // 1. PLATING (Piring Bersih -> Ambil Masakan)
            if (heldItem instanceof Plate) {
                Plate plate = (Plate) heldItem;
                
                // FIX: Panggil moveContentsTo Utensil. Utensil yang menentukan apakah bisa di-scoop.
                utensil.moveContentsTo(plate); 
                
            }
            
            // 2. COOKING (Bahan -> Masuk Panci)
            else if (heldItem instanceof Ingredient) {
                Ingredient ing = (Ingredient) heldItem;
                if (device.canAccept(ing)) {
                    device.addIngredient((Preparable) chef.getInventory().takeItem());
                    System.out.println("Added ingredient to pot.");
                } else {
                    System.out.println("Item rejected (Wrong type or Full).");
                }
            }
            
            // 3. PICKUP (Tangan Kosong -> Ambil Panci)
            else if (heldItem == null) {
                device.stopCooking(); 
                chef.getInventory().setItem(utensil); 
                this.device = null; 
                System.out.println("Took the cooking utensil.");
            }
        }
        
        // KASUS B: Station Kosong (Taruh Panci)
        else {
            if (heldItem instanceof CookingDevice) {
                this.device = (CookingDevice) chef.getInventory().takeItem();
                
                // Resume cooking jika ada isi
                if (this.device instanceof KitchenUtensil && !((KitchenUtensil)this.device).isEmpty()) {
                    this.device.startCooking();
                }
                System.out.println("Placed utensil on stove.");
            } 
            else if (heldItem instanceof Plate) {
                System.out.println("Cannot place plate directly on stove!");
            }
        }
    }
}