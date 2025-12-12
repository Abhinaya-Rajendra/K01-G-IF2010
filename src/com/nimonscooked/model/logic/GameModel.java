package com.nimonscooked.model.logic;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.PlateStorage;

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
    private long pauseStartTime;
    
    // --- GAME OVER & STAGE LOGIC ---
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isStagePassed = false;
    private int failedOrdersCount = 0;
    private final int MAX_FAILED_ORDERS = 5; 
    private final int GAME_DURATION_LIMIT = 180; // 3 Menit
    
    // Stage Settings
    private int currentStageId = 1;
    private int targetScore = 0;
    // -------------------------------

    private List<PendingPlate> pendingPlates;
    private List<PlateStorage> plateStorages; 

    private class PendingPlate {
        long returnTime;
        public PendingPlate(long returnTime) {
            this.returnTime = returnTime;
        }
    }

    private GameModel() {
        // Init awal (Default Stage 1)
        this.gameStartTime = System.currentTimeMillis(); 
        this.pauseStartTime = 0;
        this.observers = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.orderManager = new OrderManager();
        this.pendingPlates = new ArrayList<>();
        this.plateStorages = new ArrayList<>();
        this.chefs = new ArrayList<>();
        
        // Setup Map Default
        resetGame(1);
        
        this.gameLoop = new Timer(50, e -> updateGame());
        this.gameLoop.start();
        
        System.out.println("GameModel Initialized.");
    }

    public static GameModel getInstance() {
        if (instance == null) {
            instance = new GameModel();
        }
        return instance;
    }

    public long getGameDuration() {
        if (isPaused) { 
            long elapsed = pauseStartTime - gameStartTime;
            return elapsed / 1000;
        }
        if (isGameOver) {
            long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000;
            return Math.min(elapsed, GAME_DURATION_LIMIT); 
        }
        long elapsedMillis = System.currentTimeMillis() - gameStartTime;
        return elapsedMillis / 1000; 
    }

    public void registerPlateStorage(PlateStorage ps) {
        this.plateStorages.add(ps);
        ps.initPlates(); 
    }
    
    public void returnPlateLater() {
        long returnTime = System.currentTimeMillis() + 10000;
        pendingPlates.add(new PendingPlate(returnTime));
    }

    // --- LOGIC GAME OVER / STAGE ---
    public void addFailedOrder() {
        if (isGameOver) return;
        
        this.failedOrdersCount++;
        notifyObservers(); 
        
        if (failedOrdersCount >= MAX_FAILED_ORDERS) {
            finishGame(false); 
        }
    }
    
    private void checkTimeLimit() {
        if (getGameDuration() >= GAME_DURATION_LIMIT) {
            boolean passed = (score >= targetScore);
            finishGame(passed);
        }
    }
    
    private void finishGame(boolean passed) {
        isGameOver = true;
        isStagePassed = passed;
        System.out.println("GAME OVER. Status: " + (passed ? "PASSED" : "FAILED"));
        notifyObservers(); 
    }
    
    public void resetGame(int stageId) {
        this.isGameOver = false;
        this.isStagePassed = false;
        this.failedOrdersCount = 0;
        this.currentStageId = stageId;
        
        // --- STAGE CONFIGURATION ---
        boolean isRandomMap = false;
        if (stageId == 1) {
            this.targetScore = 150; 
            isRandomMap = false;
        } else if (stageId == 2) {
            this.targetScore = 300; 
            isRandomMap = true;
        }
        
        // Buat Map Baru
        this.map = new Map(isRandomMap);
        this.gameStartTime = System.currentTimeMillis();
        
        this.chefs.clear();
        this.chefs.add(new Chef(2, 4));
        this.chefs.add(new Chef(11, 4));
        this.activeChefIndex = 0;
        
        this.projectiles.clear();
        this.pendingPlates.clear(); 
        this.plateStorages.clear(); 
        
        this.map.registerStations(this); 
        
        this.score = 0;
        this.orderManager = new OrderManager();
        notifyObservers();
    }
    
    public boolean isGameOver() { return isGameOver; }
    public boolean isStagePassed() { return isStagePassed; }
    public int getTargetScore() { return targetScore; }
    public int getCurrentStageId() { return currentStageId; }
    public int getFailedOrdersCount() { return failedOrdersCount; }
    public int getMaxFailedOrders() { return MAX_FAILED_ORDERS; }

    private void updateGame() {
        if (isGameOver||isPaused) return; 
        if (map == null) return;
        
        checkTimeLimit(); 
        
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
                        plateStorages.get(0).addDirtyPlate();
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

    public void togglePause() {
        if(isGameOver) return;
        this.isPaused = !this.isPaused;
        if (this.isPaused) {
            gameLoop.stop(); 
            this.pauseStartTime = System.currentTimeMillis();
            
            System.out.println("Game Paused.");
        } else {
            long pausedDuration = System.currentTimeMillis() - this.pauseStartTime;
            this.gameStartTime += pausedDuration;
            this.pauseStartTime = 0;
            gameLoop.start();
            System.out.println("Game Resumed.");
        }
        notifyObservers();
        
    }
    
    public boolean isPaused() { return isPaused; }
    public List<Chef> getChefs() { return chefs; }
    public Map getMap() { return map; }
    public void addObserver(GameObserver observer) { observers.add(observer); }
    public void notifyObservers() { for (GameObserver o : observers) o.update(null); }
    public OrderManager getOrderManager() { return orderManager; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; notifyObservers(); }
    public void addProjectile(Projectile p) { projectiles.add(p); }
    public List<Projectile> getProjectiles() { return projectiles; }
}