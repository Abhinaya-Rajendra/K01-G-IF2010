package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Item;
import javax.swing.Timer; // Import ini untuk timer

public class CuttingStation extends Station {
    private Item currentItem;
    private Timer chopTimer;
    private int chopProgress = 0;
    private final int CHOP_TIME = 1000; // Waktu memotong: 1 detik (1000ms)

    public CuttingStation(int x, int y) {
        super(x, y);
    }

    public Item getItem() { return currentItem; }

    @Override
    public void interact(Chef chef) {
        // KASUS 1: Chef taruh item, Station kosong
        if (!chef.getInventory().isEmpty() && currentItem == null) {
            currentItem = chef.getInventory().takeItem();
            System.out.println("Placed " + currentItem.getName() + " on Cutting Station.");
            // Hentikan timer lama jika ada (untuk jaga-jaga)
            if (chopTimer != null && chopTimer.isRunning()) chopTimer.stop(); 
        } 
        
        // KASUS 2: Chef kosong, Station ada item
        else if (chef.getInventory().isEmpty() && currentItem != null) {
            
            boolean canChop = currentItem instanceof Ingredient && ((Ingredient) currentItem).canBeChopped();

            // KASUS 2A: Mulai atau Lanjutkan Memotong
            if (canChop) {
                // Jika sedang ada proses (timer berjalan), tekan interaksi berarti mengambil item
                if (chopTimer != null && chopTimer.isRunning()) {
                    // Ambil paksa (Interrupting Chop)
                    forceStopChop(chef); 
                    // Lanjut ke pengambilan item setelah membatalkan chop
                } else {
                    // 1. Set Chef Busy (memblokir gerakan)
                    chef.setBusy(true); 

                    // 2. Start Timer
                    chopTimer = new Timer(50, e -> {
                        chopProgress += 50; // Tambah progress 50ms
                        
                        if (chopProgress >= CHOP_TIME) {
                            // Selesai Memotong
                            ((Ingredient) currentItem).chop();
                            System.out.println("Chop chop! Item processed.");
                            
                            // 3. Reset state & Lepaskan status busy chef
                            chopProgress = 0;
                            ((Timer)e.getSource()).stop();
                            chef.setBusy(false); 
                        }
                    });
                    chopTimer.start();
                    System.out.println("Started chopping...");
                    return; // Keluar setelah start chop
                }
            } 
            
            // KASUS 2B: Ambil Item (Jika sudah dipotong/tidak bisa dipotong/chop di-cancel)
            // (Hanya dijalankan jika tidak return di 2A, atau setelah interrupt)
            chef.setBusy(false); // Pastikan status busy dilepas
            chef.getInventory().setItem(currentItem);
            currentItem = null;
            System.out.println("Took item from Cutting Station.");
        }
    }
    
    // Dipanggil oleh Controller jika Chef bergerak menjauh (Cancel Action)
    public void forceStopChop(Chef chef) {
        if (chopTimer != null && chopTimer.isRunning()) {
            chopTimer.stop();
            chef.setBusy(false);
            // Progress TIDAK di-reset agar bisa dilanjutkan, tapi di Cutting biasanya langsung selesai.
            // Jika Anda ingin chopping di-reset jika di-cancel:
            chopProgress = 0; 
            System.out.println("Chopping interrupted and reset.");
        }
    }
}