package com.nimonscooked.model.stations;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.items.Plate;
import java.util.Stack;

public class WashingStation extends Station {
    
    private Stack<Plate> dirtyQueue;
    private Stack<Plate> cleanResult;
    
    private Thread washingThread;
    private boolean isWashing = false;
    private int currentProgress = 0; // 0 - 100% per piring
    
    private final int WASH_TIME = 3000; // 3 Detik per piring

    public WashingStation(int x, int y) {
        super(x, y);
        this.dirtyQueue = new Stack<>();
        this.cleanResult = new Stack<>();
    }

    @Override
    public void interact(Chef chef) {
        Object heldItem = chef.getInventory().getItem();
        
        // KASUS 1: Chef bawa Piring Kotor -> Taruh di antrian cuci
        if (heldItem instanceof Plate) {
            Plate p = (Plate) heldItem;
            if (!p.isClean()) {
                dirtyQueue.push(p);
                chef.getInventory().takeItem(); 
                System.out.println("Plate placed in washing queue.");
                return;
            }
        }
        
        // KASUS 2: Chef tangan kosong
        if (heldItem == null) {
            // Prioritas A: Ambil hasil bersih
            if (!cleanResult.isEmpty()) {
                chef.getInventory().setItem(cleanResult.pop());
                System.out.println("Took a clean plate.");
                return;
            }
            
            // Prioritas B: Mulai/Lanjutkan Cuci
            if (!dirtyQueue.isEmpty()) {
                toggleWashing(chef);
            }
        }
    }
    
    // Method untuk memulai/melanjutkan proses cuci
    private void toggleWashing(Chef chef) {
        if (isWashing) {
            // Jika sedang mencuci dan di-interact lagi -> Pause
            pauseWashing();
            chef.setBusy(false); 
        } else {
            // Start / Resume
            startWashing(chef);
        }
    }

    private void startWashing(Chef chef) {
        if (dirtyQueue.isEmpty() || isWashing) return;
        
        isWashing = true;
        chef.setBusy(true); // 🔥 SET CHEF BUSY 🔥
        
        washingThread = new Thread(() -> {
            try {
                System.out.println("Washing started...");
                // Sesuaikan start time jika proses di-pause sebelumnya
                long startTime = System.currentTimeMillis() - (long)((currentProgress / 100.0) * WASH_TIME); 
                
                while (isWashing && currentProgress < 100) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    currentProgress = (int) ((elapsed / (double) WASH_TIME) * 100);
                    
                    if (currentProgress >= 100) {
                        // Selesai 1 piring
                        finishOnePlate();
                        
                        // Reset state untuk piring berikutnya
                        isWashing = false; 
                        currentProgress = 0;
                        
                        // LEPASKAN BUSY setelah selesai 1 piring
                        chef.setBusy(false); 
                        
                        // Jika masih ada antrian, bisa otomatis start lagi di sini
                        // atau biarkan player interact lagi. Kita biarkan player interact lagi.
                    }
                    
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                System.out.println("Washing interrupted");
            }
        });
        washingThread.start();
    }
    
    private void pauseWashing() {
        isWashing = false;
        // currentProgress tetap tersimpan untuk resume
        System.out.println("Washing Paused at " + currentProgress + "%");
    }
    
    private void finishOnePlate() {
        if (!dirtyQueue.isEmpty()) {
            Plate p = dirtyQueue.pop();
            p.setClean(true); 
            cleanResult.push(p);
            System.out.println("Plate Cleaned!");
        }
    }

    // Dipanggil oleh Controller jika Chef bergerak menjauh
    public void forceStopWashing(Chef chef) {
        if (isWashing) {
            pauseWashing();
            chef.setBusy(false); // 🔥 LEPASKAN BUSY SAAT DICANCEL 🔥
            System.out.println("Washing canceled by movement.");
        }
    }

    public boolean isWashing() { return isWashing; }
    public int getProgress() { return currentProgress; }
    public int getDirtyCount() { return dirtyQueue.size(); }
    public int getCleanCount() { return cleanResult.size(); }
}