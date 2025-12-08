package com.nimonscooked.model.items;

import com.nimonscooked.core.Preparable;
import java.util.ArrayList;
import java.util.List;

public abstract class KitchenUtensil extends Item {
    // Sesuai Spec: List<Preparable> atau Set<Preparable>
    protected List<Preparable> contents; 
    
    public KitchenUtensil(String name) {
        this.name = name;
        this.contents = new ArrayList<>();
    }

    public List<Preparable> getContents() {
        return contents;
    }
    
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public void clearContents() {
        contents.clear();
    }
    
    // Abstract method agar subclass wajib implementasi logika validasi sendiri
    public abstract boolean canAccept(Preparable item);

    // --- PERBAIKAN 1: Tambahkan method addIngredient di sini ---
    // Agar @Override di Plate valid dan super.addIngredient() bisa dipanggil
    public void addIngredient(Preparable item) {
        if (canAccept(item)) {
            contents.add(item);
        }
    }

    // --- PERBAIKAN 2: Tambahkan method moveContentsTo ---
    // Agar AssemblyStation tidak error saat menuang isi panci ke piring
    public void moveContentsTo(Plate targetPlate) {
        if (targetPlate == null) return;
        
        // Pindahkan semua isi ke target (Plate)
        // Kita gunakan ArrayList baru untuk iterasi agar aman
        for (Preparable item : new ArrayList<>(contents)) {
            targetPlate.addIngredient(item);
        }
        
        // Kosongkan wadah asal (Panci/Wajan) setelah dituang
        this.clearContents();
        System.out.println(this.name + " contents moved to " + targetPlate.getName());
    }
}