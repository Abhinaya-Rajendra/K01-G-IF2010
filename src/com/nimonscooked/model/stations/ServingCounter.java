package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Dish;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.Plate;
import com.nimonscooked.model.logic.GameModel;

public class ServingCounter extends Station {

    public ServingCounter(int x, int y) {
        super(x, y);
    }

    @Override
    public void interact(Chef chef) {
        // Trigger: Pemain menekan Drop (Space/Interact biasanya untuk pick/put)
        // Di spek [286], Trigger Serving Action adalah "Drop" di Serving Counter.
        // Asumsi: Di game kita, menaruh item di stasiun menggunakan tombol Interact/Space juga.
        
        Object heldItem = chef.getInventory().getItem();
        
        // Input harus berupa Plate
        if (heldItem instanceof Plate) {
            Plate plate = (Plate) heldItem;
            
            // Input harus Dish lengkap (bukan piring kosong, bukan bahan mentah doang)
            // Di kode Plate kita, isi disimpan di 'contents'.
            // Dish dianggap valid jika ada isinya.
            if (!plate.isEmpty()) {
                
                // Ambil GameModel untuk akses OrderManager
                GameModel gm = GameModel.getInstance();
                
                // PROSES VALIDASI
                // validateService akan mengembalikan true jika cocok, false jika tidak.
                boolean isSuccess = gm.getOrderManager().validateService(plate);
                
                if (isSuccess) {
                    System.out.println("Serving Success! Score Added.");
                    // Skor ditambahkan di dalam OrderManager.validateService (atau bisa di sini)
                    // gm.addScore(reward); -> sudah dihandle OrderManager
                } else {
                    System.out.println("Serving Failed! Wrong Order. Kak Jendra eats it.");
                    // PENALTI
                    gm.addScore(-10); // Contoh nilai penalti
                }
                
                // OUTPUT & CLEANUP (Berlaku untuk Success maupun Fail)
                
                // 1. Hidangan Hilang (Tersaji atau Dimakan Kak Jendra)
                plate.clearContents(); 
                
                // 2. Piring hilang dari tangan Chef
                chef.getInventory().takeItem(); 
                
                // 3. Piring kembali ke Plate Storage dalam kondisi KOTOR setelah 10 detik
                gm.returnPlateLater(); 
                
            } else {
                System.out.println("Cannot serve empty plate!");
            }
        } else {
            System.out.println("Must serve using a Plate!");
        }
    }
}