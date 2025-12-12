package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.Plate;

public class AssemblyStation extends Station {
    private Item storedItem;

    public AssemblyStation(int x, int y) {
        super(x, y);
    }

    public Item getStoredItem() { return storedItem; }
    public void setStoredItem(Item item) { this.storedItem = item; }

    @Override
    public void interact(Chef chef) {
        Item heldItem = chef.getInventory().getItem();

        // KASUS 1: MEJA KOSONG -> TARUH
        if (storedItem == null) {
            if (!chef.getInventory().isEmpty()) {
                storedItem = chef.getInventory().takeItem();
                System.out.println("Placed " + storedItem.getName() + " on Assembly.");
            }
        } 
        // KASUS 2: MEJA ADA BARANG
        else {
            
            // FIX BARU: 2A. Di meja ada UTENSIL, Chef bawa PIRING -> SCOOP
            if (storedItem instanceof KitchenUtensil && heldItem instanceof Plate) {
                KitchenUtensil utensil = (KitchenUtensil) storedItem;
                Plate plate = (Plate) heldItem;
                
                // Delegasikan ke Utensil (KitchenUtensil.moveContentsTo yang sudah direvisi)
                utensil.moveContentsTo(plate);
                return; // Interaksi selesai
            }
            
            // 2B. Di meja ada PIRING
            if (storedItem instanceof Plate) {
                Plate plate = (Plate) storedItem;
                
                // Chef bawa Panci/Wajan -> TUANG ISI (SCOOP)
                if (heldItem instanceof KitchenUtensil) {
                    KitchenUtensil pot = (KitchenUtensil) heldItem;
                    pot.moveContentsTo(plate); 
                }
                // Chef bawa Bahan -> TARUH/PASANG BAHAN
                else if (heldItem instanceof Ingredient) {
                    Ingredient ing = (Ingredient) chef.getInventory().takeItem();
                    if (plate.canAccept(ing)) {
                         plate.addIngredient(ing);
                    } else {
                         // Kembalikan item jika tidak bisa diterima
                         chef.getInventory().setItem(ing); 
                         System.out.println("Plate cannot accept item (Dirty or Full).");
                    }
                }
                
                // Chef tangan kosong -> AMBIL PIRING
                else if (heldItem == null) {
                    chef.getInventory().setItem(plate);
                    storedItem = null;
                }
            }
            // 2C. Di meja bukan piring/Utensil (kasus lain) -> AMBIL
            else {
                if (heldItem == null) {
                    chef.getInventory().setItem(storedItem);
                    storedItem = null;
                }
            }
        }
    }
}