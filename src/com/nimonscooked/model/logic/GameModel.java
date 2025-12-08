package com.nimonscooked.model.logic;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.PlateStorage; // Import baru

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;

public class GameModel {
    private static GameModel instance;
    
    private Map map;
    private List<GameObserver> observers;
    
    private List<Chef> chefs;
    private int activeChefIndex = 0;
    
    private OrderManager orderManager;
    private List<Projectile> projectiles;
    private Timer gameLoop;
    private int score = 0;
    private long lastTickTime = 0;
    private long gameStartTime;

    private List<PendingPlate> pendingPlates;
    private List<PlateStorage> plateStorages; 

    private class PendingPlate {
        long returnTime;
        public PendingPlate(long returnTime) {
            this.returnTime = returnTime;
        }
    }

    private GameModel() {
        // PERBAIKAN: Initialize Map DULU
        this.map = new Map();
        this.gameStartTime = System.currentTimeMillis(); 
        
        this.observers = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.orderManager = new OrderManager();
        this.pendingPlates = new ArrayList<>();
        this.plateStorages = new ArrayList<>();
        
        // Spawn 2 Chef
        this.chefs = new ArrayList<>();
        chefs.add(new Chef(2, 4)); 
        chefs.add(new Chef(11, 4)); 
        
        // Game Loop (50ms)
        this.gameLoop = new Timer(50, e -> updateGame());
        this.gameLoop.start();
        
        // **********************************************
        // PENTING: PANGGIL INIT MAP dan DAFTARKAN STORAGE di sini, setelah model dibuat!
        // **********************************************
        this.map.registerStations(this); // Map akan mendaftarkan semua Station, termasuk PlateStorage
        
        System.out.println("GameModel Initialized.");
    }

    public static GameModel getInstance() {
        if (instance == null) {
            instance = new GameModel();
            // *PENTING*: JANGAN lakukan init di sini. Lakukan di constructor.
        }
        return instance;
    }

    public long getGameDuration() {
    long elapsedMillis = System.currentTimeMillis() - gameStartTime;
    return elapsedMillis / 1000; // Kembalikan dalam detik
    }

    // Dipanggil oleh Map saat inisialisasi untuk mendaftarkan PlateStorage
    public void registerPlateStorage(PlateStorage ps) {
        this.plateStorages.add(ps);
        ps.initPlates(); // Inisialisasi piring HANYA SETELAH didaftarkan
    }
    
    public void returnPlateLater() {
        long returnTime = System.currentTimeMillis() + 10000;
        pendingPlates.add(new PendingPlate(returnTime));
        System.out.println("Plate will return in 10 seconds...");
    }

    private void updateGame() {
        if (map == null) return;
        
        // ... (Logika updateGame tetap sama) ...
        
        if (!projectiles.isEmpty()) {
            Iterator<Projectile> it = projectiles.iterator();
            while (it.hasNext()) {
                Projectile p = it.next();
                p.updatePosition(map);
                if (!p.isActive()) {
                    it.remove();
                }
            }
            notifyObservers();
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTickTime >= 1000) {
            if (orderManager != null) {
                orderManager.tick();
            }
            lastTickTime = currentTime;
            notifyObservers();
        }
        
        if (!pendingPlates.isEmpty()) {
            Iterator<PendingPlate> it = pendingPlates.iterator();
            while (it.hasNext()) {
                PendingPlate pp = it.next();
                if (currentTime >= pp.returnTime) {
                    if (!plateStorages.isEmpty()) {
                        // Default: Masuk ke storage pertama
                        plateStorages.get(0).addDirtyPlate();
                        System.out.println("A dirty plate has returned to storage!");
                    }
                    it.remove();
                    notifyObservers();
                }
            }
        }
    }

    public void switchChef() {
        if (chefs.size() < 2) return;
        activeChefIndex = (activeChefIndex + 1) % chefs.size();
        notifyObservers();
    }

    public Chef getActiveChef() {
        if (chefs.isEmpty()) return null;
        return chefs.get(activeChefIndex);
    }
    
    // Getters & Setters
    public List<Chef> getChefs() { return chefs; }
    public Map getMap() { return map; }
    public void addObserver(GameObserver observer) { observers.add(observer); }
    public void notifyObservers() { for (GameObserver o : observers) o.update(null); }
    public OrderManager getOrderManager() { return orderManager; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; notifyObservers(); }
    
    public void resetGame(boolean randomMap) {
        // PERBAIKAN: Gunakan constructor Map(randomMap)
        this.map = new Map(randomMap);
        this.gameStartTime = System.currentTimeMillis();
        
        this.chefs.clear();
        this.chefs.add(new Chef(2, 4));
        this.chefs.add(new Chef(11, 4));
        this.activeChefIndex = 0;
        this.projectiles.clear();
        this.pendingPlates.clear(); 
        this.plateStorages.clear(); 
        
        // PERBAIKAN: Panggil registerStations di Map BARU
        this.map.registerStations(this); 
        
        this.score = 0;
        this.orderManager = new OrderManager();
        notifyObservers();
    }
    
    public void addProjectile(Projectile p) { projectiles.add(p); }
    public List<Projectile> getProjectiles() { return projectiles; }
}