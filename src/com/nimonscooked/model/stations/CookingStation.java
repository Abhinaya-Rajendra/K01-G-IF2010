package com.nimonscooked.model.stations;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.KitchenUtensil;

public class CookingStation extends Station {
    private CookingDevice device;

    public CookingStation(int x, int y) {
        super(x, y);
    }

    public CookingDevice getDevice() {
        return device;
    }

    public void setDevice(CookingDevice device) {
        this.device = device;
    }

    @Override
    public void interact(Chef chef) {
        // KASUS 1: Di Station ada Alat Masak
        if (device != null) {
            KitchenUtensil utensil = (KitchenUtensil) device;

            // 1A. Chef bawa Ingredient -> Masukkan ke Alat Masak
            if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof Ingredient) {
                Ingredient ing = (Ingredient) chef.getInventory().getItem();
                
                if (device.canAccept(ing)) {
                    device.addIngredient((Preparable) chef.getInventory().takeItem());
                    System.out.println("Added ingredient to pot/pan.");
                } else {
                    System.out.println("Cannot add ingredient (Full or Invalid)");
                }
            }
            // 1B. Chef tangan kosong -> AMBIL ALAT MASAK (PICK UP)
            else if (chef.getInventory().isEmpty()) {
                
                // --- PERBAIKAN PENTING: STOP COOKING SAAT DIAMBIL ---
                device.stopCooking(); 
                // ----------------------------------------------------

                chef.getInventory().setItem(utensil);
                this.device = null; 
                System.out.println("Took the cooking utensil (Cooking Paused).");
            }
        }
        // KASUS 2: Station Kosong (Menaruh Panci)
        else {
            if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof CookingDevice) {
                this.device = (CookingDevice) chef.getInventory().takeItem();
                
                // --- PERBAIKAN PENTING: AUTO RESUME SAAT DITARUH ---
                // Jika panci ada isinya, lanjutkan memasak otomatis
                if (this.device instanceof KitchenUtensil && !((KitchenUtensil)this.device).isEmpty()) {
                    this.device.startCooking();
                    System.out.println("Placed utensil on stove (Cooking Resumed).");
                } else {
                    System.out.println("Placed empty utensil on stove.");
                }
            }
        }
    }
}