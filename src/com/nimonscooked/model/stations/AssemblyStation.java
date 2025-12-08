package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.Plate;

public class AssemblyStation extends Station {
    // Barang yang ditaruh di meja (bisa Piring, Bahan, atau Panci)
    private Item storedItem;

    public AssemblyStation(int x, int y) {
        super(x, y);
    }

    public Item getStoredItem() { return storedItem; }

    @Override
    public void interact(Chef chef) {
        // KASUS 1: MEJA KOSONG
        if (storedItem == null) {
            // Chef taruh barang apa saja (Piring/Bahan)
            if (!chef.getInventory().isEmpty()) {
                storedItem = chef.getInventory().takeItem();
                System.out.println("Placed " + storedItem.getName() + " on Assembly.");
            }
        } 
        // KASUS 2: MEJA ADA BARANG
        else {
            // 2A. Di meja ada PIRING
            if (storedItem instanceof Plate) {
                Plate plate = (Plate) storedItem;
                
                // Chef bawa Panci/Wajan -> TUANG KE PIRING
                if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof KitchenUtensil) {
                    KitchenUtensil pot = (KitchenUtensil) chef.getInventory().getItem();
                    // Jangan tuang piring ke piring
                    if (!(pot instanceof Plate)) {
                        pot.moveContentsTo(plate); // ACTION: PLATING!
                    }
                }
                // Chef bawa Bahan -> TARUH KE PIRING
                else if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof Ingredient) {
                    plate.addIngredient((Ingredient) chef.getInventory().takeItem());
                }
                // Chef tangan kosong -> AMBIL PIRING
                else if (chef.getInventory().isEmpty()) {
                    chef.getInventory().setItem(plate);
                    storedItem = null;
                }
            }
            // 2B. Di meja bukan piring (misal Bahan nganggur)
            else {
                // Ambil saja
                if (chef.getInventory().isEmpty()) {
                    chef.getInventory().setItem(storedItem);
                    storedItem = null;
                }
            }
        }
    }
}