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

    // --- METHOD BARU UNTUK PROJECTILE ---
    public Item getStoredItem() { return storedItem; }
    public void setStoredItem(Item item) { this.storedItem = item; }

    @Override
    public void interact(Chef chef) {
        // KASUS 1: MEJA KOSONG -> TARUH
        if (storedItem == null) {
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
                
                // Chef bawa Panci/Wajan -> TUANG
                if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof KitchenUtensil) {
                    KitchenUtensil pot = (KitchenUtensil) chef.getInventory().getItem();
                    if (!(pot instanceof Plate)) {
                        pot.moveContentsTo(plate); 
                    }
                }
                // Chef bawa Bahan -> TARUH
                else if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof Ingredient) {
                    plate.addIngredient((Ingredient) chef.getInventory().takeItem());
                }
                // Chef tangan kosong -> AMBIL PIRING
                else if (chef.getInventory().isEmpty()) {
                    chef.getInventory().setItem(plate);
                    storedItem = null;
                }
            }
            // 2B. Di meja bukan piring -> AMBIL
            else {
                if (chef.getInventory().isEmpty()) {
                    chef.getInventory().setItem(storedItem);
                    storedItem = null;
                }
            }
        }
    }
}