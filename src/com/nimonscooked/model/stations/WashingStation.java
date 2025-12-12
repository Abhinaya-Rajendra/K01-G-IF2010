package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Plate;
import com.nimonscooked.model.logic.GameModel;
import java.util.Stack;
import javax.swing.Timer;

public class WashingStation extends Station {
    
    private Stack<Plate> dirtyQueue;
    private Stack<Plate> cleanResult;
    
    private Chef activeChef = null; // Chef yang sedang mencuci
    private long washStartTime = 0;
    private int currentProgressMs = 0; // Progress dalam milidetik
    private final int WASH_TIME_MS = 3000; // 3 detik

    public WashingStation(int x, int y) {
        super(x, y);
        this.dirtyQueue = new Stack<>();
        this.cleanResult = new Stack<>();
    }

    public void addDirtyPlate() {
        Plate p = new Plate();
        p.setClean(false);
        dirtyQueue.push(p);
    }
    
    // GETTER BARU UNTUK GAMEDRAWINGPANEL
    public int getProgress() {
        if (currentProgressMs >= WASH_TIME_MS) return 100;
        return (int) (currentProgressMs / (double) WASH_TIME_MS * 100);
    }
    public boolean isWashing() { return activeChef != null; }

    @Override
    public void interact(Chef chef) {
        Object heldItem = chef.getInventory().getItem();
        
        // KASUS 1: DROP Piring Kotor
        if (heldItem instanceof Plate) {
            Plate p = (Plate) heldItem;
            if (!p.isClean() && p.isEmpty()) { // Hanya piring kotor kosong yang bisa dicuci
                dirtyQueue.push(p);
                chef.getInventory().takeItem(); 
                currentProgressMs = 0; // Reset progress jika proses idle/selesai
                System.out.println("Plate queued for washing.");
                return;
            }
        }
        
        // KASUS 2: PICKUP Bersih / ACTION Cuci
        if (heldItem == null) {
            // KASUS 2A: PICKUP Piring Bersih
            if (!cleanResult.isEmpty()) {
                chef.getInventory().setItem(cleanResult.pop());
                System.out.println("Took a clean plate.");
                return;
            }
            
            // KASUS 2B: ACTION Cuci
            if (!dirtyQueue.isEmpty()) {
                if (activeChef == null) {
                    startWashing(chef);
                } else if (activeChef == chef) {
                    stopWashing(chef); // Berhenti mencuci jika interaksi ganda
                }
            } else {
                System.out.println("Washing station empty.");
            }
        }
    }
    
    private void startWashing(Chef chef) {
        if (dirtyQueue.isEmpty()) return;
        
        activeChef = chef;
        washStartTime = System.currentTimeMillis();
        
        // Hanya set busy jika progress dimulai dari 0
        if (currentProgressMs == 0) {
            activeChef.setBusy(true); 
        }
        
        // Memulai loop update di GameModel
        GameModel.getInstance().getGameLoop().addActionListener(e -> updateWashing(chef));
        System.out.println("Started washing progress: " + currentProgressMs);
    }
    
    private void stopWashing(Chef chef) {
        if (activeChef == chef) {
            GameModel.getInstance().getGameLoop().removeActionListener(e -> updateWashing(chef));
            activeChef = null;
            chef.setBusy(false); 
            System.out.println("Washing interrupted, progress saved.");
        }
    }

    public void updateWashing(Chef chef) {
        if (activeChef != chef) return;

        // Periksa apakah chef sudah pindah (jarak > 1.5 tile)
        double dx = getGridX() - chef.getGridX();
        double dy = getGridY() - chef.getGridY();
        if (dx * dx + dy * dy > 1.5 * 1.5) {
            forceStopWashing(chef); 
            return;
        }

        // Hitung penambahan progres
        long elapsed = System.currentTimeMillis() - washStartTime;
        currentProgressMs += elapsed;
        washStartTime = System.currentTimeMillis();

        if (currentProgressMs >= WASH_TIME_MS) {
            finishOnePlate();
            currentProgressMs = 0; // Reset progress untuk piring berikutnya

            if (dirtyQueue.isEmpty()) {
                // Selesai mencuci semua piring
                GameModel.getInstance().getGameLoop().removeActionListener(e -> updateWashing(chef));
                activeChef = null;
                chef.setBusy(false); 
            } else {
                 // Langsung mulai piring berikutnya (reset timer)
                 washStartTime = System.currentTimeMillis();
            }
        }
    }
    
    public void forceStopWashing(Chef chef) {
        if (activeChef == chef) {
             GameModel.getInstance().getGameLoop().removeActionListener(e -> updateWashing(chef));
             activeChef = null;
             chef.setBusy(false);
             System.out.println("Washing forcibly stopped due to distance.");
        }
    }
    
    private void finishOnePlate() {
        if (!dirtyQueue.isEmpty()) {
            Plate p = dirtyQueue.pop();
            p.setClean(true); 
            cleanResult.push(p);
        }
    }
    
    // --- GETTERS LAMA ---
    // isWashing() digantikan oleh isWashing() yang baru
    // getProgress() digantikan oleh getProgress() yang baru
    public int getDirtyCount() { return dirtyQueue.size(); }
    public int getCleanCount() { return cleanResult.size(); }
}