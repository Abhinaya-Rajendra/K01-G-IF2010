package com.nimonscooked.model.logic;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.PlateStorage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.CopyOnWriteArrayList; 

// Interface untuk objek yang perlu di-update setiap tick game
public interface GameTickable {
    void updateByTick();
} 

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
    
    // Waktu Game 
    private long gameStartTime;
    private long timePaused = 0; 
    private long pauseStartTime = 0;
    
    // Flag Utama Kontrol Loop
    private boolean isGameActive = false; 
    
    // FIX: List untuk melacak stasiun/objek yang sedang memiliki progress aktif
    private List<GameTickable> activeStations = new CopyOnWriteArrayList<>();
    
    // --- PAUSE LOGIC ---
    private boolean isPaused = false;
    
    // --- GAME OVER & STAGE LOGIC ---
    private boolean isGameOver = false;
    private boolean isStagePassed = false;
    private int failedOrdersCount = 0;
    private final int MAX_FAILED_ORDERS = 5; 
    private final int GAME_DURATION_LIMIT = 100; 
    
    private int currentStageId = 1;
    private int targetScore = 0;

    private List<PendingPlate> pendingPlates;
    private List<PlateStorage> plateStorages; 

    private class PendingPlate {
        long returnTime;
        public PendingPlate(long returnTime) {
            this.returnTime = returnTime;
        }
    }

    private GameModel() {
        this.observers = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.pendingPlates = new ArrayList<>();
        this.plateStorages = new ArrayList<>();
        this.chefs = new ArrayList<>();
        
        // REVISI: Timer dibuat tapi TIDAK di-start di sini.
        // Timer akan di-start saat resetGame() -> startGameLogic() dipanggil.
        this.gameLoop = new Timer(20, e -> updateGame());
        
        System.out.println("GameModel Initialized (Idle State).");
    }

    public static GameModel getInstance() {
        if (instance == null) {
            instance = new GameModel();
        }
        return instance;
    }

    // --- KONTROL LOGIC LOOP ---
    
    public void startGameLogic() {
        // Hentikan dulu untuk memastikan clean state
        stopGameLogic();
        
        this.isGameActive = true;
        this.lastTickTime = System.currentTimeMillis();
        
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
        System.out.println("Logic Loop Started.");
    }

    public void stopGameLogic() {
        this.isGameActive = false;
        if (gameLoop.isRunning()) {
            gameLoop.stop();
        }
        System.out.println("Logic Loop Stopped.");
    }

    private void updateGame() {
        // REVISI: Cek apakah game aktif. Jika di menu, ini false.
        if (!isGameActive) return; 

        if (isGameOver || isPaused) return; 
        if (map == null) return;
        
        // Cek Time Limit di awal untuk prioritas. 
        checkTimeLimit(); 
        if (isGameOver) return; 

        // 1. UPDATE CHEF PHYSICS
        for (Chef c : chefs) {
            c.update(map); 
        }

        // 2. PROJECTILES 
        if (!projectiles.isEmpty()) {
            Iterator<Projectile> it = projectiles.iterator();
            while (it.hasNext()) {
                Projectile p = it.next();
                p.update(map, chefs);
                
                if (!p.isActive()) {
                    it.remove();
                }
            }
        }
        
        // 3. FIX: UPDATE ACTIVE STATION PROGRESS 
        for (GameTickable station : activeStations) {
            station.updateByTick();
        }
        
        // 4. GAME LOGIC (Order Tick)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTickTime >= 1000) {
            if (orderManager != null) orderManager.tick();
            lastTickTime = currentTime;
        }
        
        // 5. PENDING PLATES
        if (!pendingPlates.isEmpty()) {
            Iterator<PendingPlate> it = pendingPlates.iterator();
            while (it.hasNext()) {
                PendingPlate pp = it.next();
                if (currentTime >= pp.returnTime) {
                    if (!plateStorages.isEmpty()) plateStorages.get(0).addDirtyPlate();
                    it.remove();
                }
            }
        }
        
        notifyObservers();
    }
    
    // --- METODE KONTROL PROGRESS STASIUN ---
    public void startStationProgress(GameTickable station) {
        if (!activeStations.contains(station)) {
            activeStations.add(station);
        }
    }

    public void stopStationProgress(GameTickable station) {
        activeStations.remove(station);
    }
    
    // --- PAUSE METHODS ---
    public void togglePause() {
        this.isPaused = !this.isPaused;
        
        if (isPaused) {
            pauseStartTime = System.currentTimeMillis();
        } else {
            timePaused += System.currentTimeMillis() - pauseStartTime;
            pauseStartTime = 0; 
        }
        
        for (Chef c : chefs) c.stopMovement();
        
        notifyObservers();
    }
    
    public boolean isPaused() {
        return isPaused;
    }

    // --- GAME OVER LOGIC ---
    
    public void addFailedOrder() {
        if (isGameOver) return;
        this.failedOrdersCount++;
        
        if (failedOrdersCount >= MAX_FAILED_ORDERS) {
            // FIX: Panggil finishGame(false) secara eksplisit untuk Game Over karena kegagalan
            finishGame(false); 
        } else {
            notifyObservers(); 
        }
    }
    
    private void checkTimeLimit() {
        // REVISI: Cek apakah sisa waktu sudah habis (0 atau kurang)
        if (getGameDuration() <= 0) {
            
            // Jika waktu habis, status ditentukan oleh perbandingan skor
            boolean passed = (score >= targetScore);
            finishGame(passed);
        }
    }
    
    private void finishGame(boolean passed) {
        if (isGameOver) return; 
        isGameOver = true;
        
        // FIX: Ensure isStagePassed is set correctly based on the 'passed' boolean
        isStagePassed = passed; 
        
        notifyObservers(); 
    }

    // --- HELPER METHODS ---
    public void switchChef() {
        if (isPaused || isGameOver) return; 
        if (chefs.size() < 2) return;
        getActiveChef().stopMovement();
        activeChefIndex = (activeChefIndex + 1) % chefs.size();
        notifyObservers();
    }

    public void resetGame(int stageId) {
        // Stop logic lama sebelum reset
        stopGameLogic();

        this.isGameOver = false;
        this.isStagePassed = false;
        this.failedOrdersCount = 0;
        this.currentStageId = stageId;
        this.isPaused = false; 
        this.timePaused = 0; 
        this.pauseStartTime = 0;
        this.activeStations.clear();
        
        // REVISI: Init OrderManager SEBELUM Map, karena Map generator butuh resep
        this.orderManager = new OrderManager();
        
        boolean isRandomMap = (stageId == 2);
        this.targetScore = (stageId == 1) ? 150 : 200; 
        
        this.map = new Map(isRandomMap); // Map dibuat setelah OrderManager siap
        this.gameStartTime = System.currentTimeMillis();
        
        this.chefs.clear();
        Point spawn1 = map.getSpawnPoint(0);
        Point spawn2 = map.getSpawnPoint(1);
        
        // INSTANCE CHEF DENGAN PREFIX BERBEDA
        this.chefs.add(new Chef(spawn1.x, spawn1.y, "fox"));     // Chef 1
        this.chefs.add(new Chef(spawn2.x, spawn2.y, "raccoon")); // Chef 2
        
        this.activeChefIndex = 0;
        
        this.projectiles.clear();
        this.pendingPlates.clear(); 
        this.plateStorages.clear(); 
        this.map.registerStations(this); 
        
        this.score = 0;
        
        // REVISI: Baru nyalakan Timer Logic di sini!
        startGameLogic();
        
        notifyObservers();
    }
    
    public long getGameDuration() {
        // Jika game belum aktif, kembalikan durasi penuh (misal 100 detik)
        if (!isGameActive && !isGameOver) return GAME_DURATION_LIMIT;

        long elapsedMillis = System.currentTimeMillis() - gameStartTime;
        
        if (isPaused && pauseStartTime > 0) {
            elapsedMillis -= (System.currentTimeMillis() - pauseStartTime);
        }
        
        elapsedMillis -= timePaused;
        
        long elapsedSeconds = elapsedMillis / 1000;
        long remainingSeconds = GAME_DURATION_LIMIT - elapsedSeconds;
        
        // Pastikan tidak negatif (stop di 0)
        return Math.max(0, remainingSeconds); 
    }

    public Timer getGameLoop() { 
        return gameLoop; 
    }

    public void registerPlateStorage(PlateStorage ps) {
        this.plateStorages.add(ps);
        ps.initPlates(); 
    }
    
    public void returnPlateLater() {
        long returnTime = System.currentTimeMillis() + 10000;
        pendingPlates.add(new PendingPlate(returnTime));
    }

    // --- GETTERS ---
    public Chef getActiveChef() {
        if (chefs.isEmpty()) return null;
        return chefs.get(activeChefIndex);
    }
    
    public List<Chef> getChefs() { return chefs; }
    public Map getMap() { return map; }
    public void addObserver(GameObserver observer) { observers.add(observer); }
    public void notifyObservers() { for (GameObserver o : observers) o.update(null); }
    public OrderManager getOrderManager() { return orderManager; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; notifyObservers(); }
    public void addProjectile(Projectile p) { projectiles.add(p); }
    public List<Projectile> getProjectiles() { return projectiles; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isStagePassed() { return isStagePassed; }
    public int getTargetScore() { return targetScore; }
    public int getCurrentStageId() { return currentStageId; }
    public int getFailedOrdersCount() { return failedOrdersCount; }
    public int getMaxFailedOrders() { return MAX_FAILED_ORDERS; }
}