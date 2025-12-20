package com.nimonscooked.model.items;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.items.Ingredient; 
import com.nimonscooked.model.items.IngredientState;
import com.nimonscooked.model.items.BurnedState; 
import com.nimonscooked.model.items.CookedState; 
import com.nimonscooked.model.items.CookingState;
import java.util.ArrayList;
import java.util.List;

public abstract class KitchenUtensil extends Item implements CookingDevice {

    protected List<Preparable> contents;
    
    protected boolean isCooking = false;
    protected int cookingProgress = 0; 
    protected Thread cookingThread;
    
    protected int cookTimeMs = 12000; 
    protected int burnTimeMs = 24000; 

    public KitchenUtensil(String name) {
        super(name);
        this.contents = new ArrayList<>();
    }

    public List<Preparable> getContents() { return contents; }
    public boolean isEmpty() { return contents.isEmpty(); }
    
    public void clearContents() {
        if (cookingThread != null && cookingThread.isAlive()) {
             cookingThread.interrupt();
        }
        contents.clear();
        this.cookingProgress = 0;
        this.isCooking = false;
    }
    
    @Override
    public void addIngredient(Preparable item) {
        if (!isEmpty()) {
            String itemName = (item instanceof Item) ? ((Item) item).getName() : "item";
            System.out.println("Utensil is already full. Cannot add " + itemName);
            return;
        }

        if (canAccept(item)) {
            contents.add(item);
            this.cookingProgress = 0; // Reset hanya ketika item baru ditambahkan
            startCooking();
        }
    }
    
    @Override
    public abstract boolean canAccept(Preparable item);

    @Override public boolean isPortable() { return true; }
    // Asumsi kapasitas Pot/Pan adalah 1, Plate adalah 4
    @Override public int capacity() { return 1; } 
    @Override public boolean isCooking() { return isCooking; }
    
    @Override 
    public int getCookingProgress() { 
        return Math.min(cookingProgress, 200); 
    }

    @Override
    public void startCooking() {
        if (isEmpty()) return;
        if (!(contents.get(0) instanceof Ingredient)) return;
        if (this instanceof Plate) return; 
        
        // Jika dipanggil saat sudah berjalan, abaikan (thread lama masih hidup)
        if (isCooking && cookingThread != null && cookingThread.isAlive()) {
             return; 
        }

        Ingredient ing = (Ingredient) contents.get(0); 
        
        if (ing.getState() instanceof BurnedState) { 
            System.out.println("Cannot cook burned ingredient. Clear Utensil first.");
            return;
        }
        
        isCooking = true;
        
        // Hentikan thread lama jika ada, untuk restart dengan offset waktu yang benar
        if (cookingThread != null && cookingThread.isAlive()) {
             cookingThread.interrupt();
        }

        cookingThread = new Thread(() -> {
            try {
                // FIX PROGRESS RESET: Hitung offset waktu yang sudah berlalu
                // Jika cookingProgress = 50, maka offset harus 50% dari cookTimeMs.
                long elapsedOffsetMs = (long) (this.cookingProgress * cookTimeMs / 100.0);
                
                // Set startTime seolah-olah proses sudah berjalan selama elapsedOffsetMs
                long startTime = System.currentTimeMillis() - elapsedOffsetMs; 
                
                while (isCooking) {
                    if (contents.isEmpty()) { 
                        stopCooking(); 
                        return; 
                    }
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    
                    // Hitung progress baru. Jika dimulai dari 50%, elapsed awal sudah 6000ms.
                    cookingProgress = (int) ((elapsed / (double) cookTimeMs) * 100);
                    
                    Ingredient currentIng = (Ingredient) contents.get(0); 
                    IngredientState currentState = currentIng.getState();
                    
                    if (cookingProgress >= 200) { 
                        if (!(currentState instanceof BurnedState)) currentIng.setState(new BurnedState());
                        stopCooking(); 
                        return; 
                    } else if (cookingProgress >= 100) { 
                        if (!(currentState instanceof CookedState) && !(currentState instanceof BurnedState)) currentIng.setState(new CookedState());
                    } else if (cookingProgress > 0) {
                        if (!(currentState instanceof CookingState) && !(currentState instanceof CookedState)) currentIng.setState(new CookingState());
                    }
                    
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) { 
                // Thread dihentikan (misalnya saat clearContents atau Utensil dipindahkan)
                Thread.currentThread().interrupt();
            }
        });
        cookingThread.start();
    }

    @Override
    public void stopCooking() {
        isCooking = false;
    }

    public void moveContentsTo(Plate targetPlate) {
        if (targetPlate == null) return;
        
        // Cek apakah panci/wajan kosong
        if (contents.isEmpty()) return;
            
        Preparable item = contents.get(0);
        
        if (item instanceof Ingredient) {
            Ingredient ing = (Ingredient) item;
            
            // 1. VALIDASI COOKING PROCESS
            // Jika masih dimasak (belum 100%), jangan boleh diambil
            if (isCooking && cookingProgress < 100) {
                System.out.println("Cannot scoop: Food is still cooking.");
                return; 
            }
            
            // 2. VALIDASI STATE (RAW)
            // Jika masih mentah (belum COOKED dan belum BURNED), jangan boleh diambil
            if (!(ing.getState() instanceof CookedState) && !(ing.getState() instanceof BurnedState)) {
                    System.out.println("Cannot scoop: Food is raw.");
                    return;
            }
            
            // 3. VALIDASI BURNED (Gosong)
            // Makanan gosong biasanya tidak bisa ditaruh di piring (harus dibuang ke tong sampah)
            if (ing.getState() instanceof BurnedState) {
                System.out.println("Cannot move burned ingredient to plate. Throw it in trash!");
                return; // Jangan clearContents(), biarkan pemain membuangnya manual (huruf F)
            }

            // 4. VALIDASI PIRING (INI BAGIAN TERPENTING!)
            // Cek dulu: Apakah piring BERSIH? DAN Apakah piring MUAT?
            if (targetPlate.isClean() && targetPlate.canAccept(ing)) {
                
                // A. Pindahkan ke Piring
                targetPlate.addIngredient(ing);
                
                // B. Hapus dari Panci (Hanya jika langkah A sukses)
                this.clearContents(); 
                
                // C. Reset status masak panci
                this.stopCooking(); 
                
                System.out.println("Success: Moved food to plate.");
                
            } else {
                // JIKA PIRING KOTOR / PENUH
                System.out.println("Failed: Plate is dirty or full. Keeping food in pan.");
                // KARENA KITA TIDAK MEMANGGIL clearContents(), MAKANAN AMAN DI PANCI
            }
        }
    }
}