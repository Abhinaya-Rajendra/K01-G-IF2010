package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import com.nimonscooked.utils.IngredientType;

public class IngredientStorage extends Station {
    private IngredientType type;
    
    // Item yang ditaruh di ATAS storage
    private Item itemOnTop; 

    public IngredientStorage(int x, int y, IngredientType type) {
        super(x, y);
        this.type = type;
        this.itemOnTop = null;
    }

    public IngredientType getType() { return type; }
    
    // --- METHOD BARU UNTUK PROJECTILE ---
    public Item getItemOnTop() { return itemOnTop; }
    public void setItemOnTop(Item item) { this.itemOnTop = item; }

    @Override
    public void interact(Chef chef) {
        Item heldItem = chef.getInventory().getItem();

        // KASUS 1: Ada barang di atas Storage
        if (itemOnTop != null) {
            // Jika tangan kosong -> Ambil barang di atasnya
            if (heldItem == null) {
                chef.getInventory().setItem(itemOnTop);
                itemOnTop = null;
                System.out.println("Took item from top of storage.");
            }
            else {
                System.out.println("Storage blocked by item on top.");
            }
        }
        
        // KASUS 2: Atas Storage Kosong
        else {
            // Jika tangan kosong -> Ambil Ingredient Baru
            if (heldItem == null) {
                chef.getInventory().setItem(new Ingredient(type));
                System.out.println("Took fresh " + type);
            }
            // Jika tangan ada barang -> Taruh barang di atas Storage
            else {
                itemOnTop = chef.getInventory().takeItem();
                System.out.println("Placed item on top of storage.");
            }
        }
    }
}