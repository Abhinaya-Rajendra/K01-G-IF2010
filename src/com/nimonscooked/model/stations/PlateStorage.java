package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Plate;
import java.util.Stack;
// Import GameModel di sini tidak masalah, tapi di constructor kita hapus panggilannya
// import com.nimonscooked.model.logic.GameModel; 

public class PlateStorage extends Station {
    
    private Stack<Plate> plates;

    public PlateStorage(int x, int y) {
        super(x, y);
        this.plates = new Stack<>();
        
        // --- FIX ERROR: Hapus GameModel.getInstance() dari constructor! ---
        // plates.push(new Plate()); dsb akan dibuat di method initPlates().
    }
    
    // Method baru untuk inisialisasi piring (dipanggil setelah GameModel siap)
    public void initPlates() {
         // Isi awal: 4 Piring Bersih (sesuai spesifikasi)
        for (int i = 0; i < 4; i++) {
            plates.push(new Plate()); 
        }
        System.out.println("PlateStorage initialized with 4 clean plates.");
    }

    // Dipanggil oleh GameModel saat Timer 10 detik habis
    public void addDirtyPlate() {
        Plate dirtyPlate = new Plate();
        dirtyPlate.setClean(false); // Set Kotor
        plates.push(dirtyPlate);
    }
    
    public int getCount() {
        return plates.size();
    }
    
    public boolean hasDirtyPlateOnTop() {
        if (plates.isEmpty()) return false;
        return !plates.peek().isClean();
    }

    @Override
    public void interact(Chef chef) {
        if (plates.isEmpty()) return;
        
        if (chef.getInventory().getItem() == null) {
            Plate topPlate = plates.pop(); // Ambil dari atas
            chef.getInventory().setItem(topPlate);
            System.out.println("Chef took a plate. Clean: " + topPlate.isClean());
        }
    }
}