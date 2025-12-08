package com.nimonscooked.model.stations;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.KitchenUtensil;

public class CookingStation extends Station {
    // Station ini menyimpan alat masak (Panci/Wajan)
    private CookingDevice device;

    public CookingStation(int x, int y) {
        super(x, y);
    }

    public CookingDevice getDevice() {
        return device;
    }

    // Untuk inisialisasi awal map (menaruh panci di kompor)
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
                    // FIX 1: Lakukan Casting ke (Preparable) karena takeItem() mengembalikan Item
                    device.addIngredient((Preparable) chef.getInventory().takeItem());
                } else {
                    System.out.println("Cannot add ingredient (Full or Invalid)");
                }
            }
            // 1B. Chef tangan kosong
            else if (chef.getInventory().isEmpty()) {
                // Jika Alat Masak ada isinya -> NYALAKAN KOMPOR (Masak)
                if (!utensil.isEmpty()) {
                    // Cek apakah isinya sudah matang? (Cek isi pertama)
                    // FIX 2: Lakukan Casting ke (Ingredient) karena getContents() mengembalikan List<Preparable>
                    Ingredient firstIng = (Ingredient) utensil.getContents().get(0);
                    
                    // Logic sederhana: Kalau masih bisa dimasak (Raw/Chopped) -> Masak
                    if (firstIng.canBeCooked()) {
                        device.startCooking();
                    } 
                    // Kalau sudah matang (Cooked) -> Ambil Panci-nya
                    else {
                        chef.getInventory().setItem(utensil);
                        this.device = null; // Panci diambil chef
                        System.out.println("Took the cooking utensil.");
                    }
                } else {
                    // Panci kosong -> Ambil Panci-nya
                    chef.getInventory().setItem(utensil);
                    this.device = null;
                    System.out.println("Took empty utensil.");
                }
            }
        }
        // KASUS 2: Station Kosong (Tidak ada Panci)
        else {
            // Chef bawa Panci/Wajan -> Taruh di Kompor
            if (!chef.getInventory().isEmpty() && chef.getInventory().getItem() instanceof CookingDevice) {
                this.device = (CookingDevice) chef.getInventory().takeItem();
                // Kosongkan tangan chef setelah menaruh (takeItem sudah mengosongkan, tapi baris di bawah ini redundant jika takeItem sudah dipanggil di dalam cast)
                // chef.getInventory().takeItem();  <-- HAPUS baris ini karena takeItem() di atas sudah mengambil barangnya.
                
                System.out.println("Placed utensil on stove.");
            }
        }
    }
}