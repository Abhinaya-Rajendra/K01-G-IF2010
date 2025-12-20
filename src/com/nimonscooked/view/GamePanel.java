package com.nimonscooked.view;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

/**
 * GamePanel (Wrapper) - Bertindak sebagai JLayeredPane untuk menumpuk:
 * 1. GameDrawingPanel (Game World - Layer Bawah)
 * 2. PausePanel (Overlay Menu - Layer Atas)
 * * REVISI: Menambahkan kontrol Game Loop (Thread) manual.
 */
public class GamePanel extends JLayeredPane implements GameObserver, Runnable { 

    private GameModel model;
    private GameDrawingPanel drawingPanel; 
    private PausePanel pauseOverlay; 
    
    // Thread Kontrol
    private Thread gameThread;
    private boolean isRunning = false;
    private final int FPS = 60;
    
    // Nilai-nilai ini dipertahankan sebagai referensi ukuran default/awal
    private final int TILE_SIZE = 50;
    private final int SIDEBAR_WIDTH = 220;
    
    // Konstruktor menerima callbacks dari GameWindow
    public GamePanel(Runnable onRestart, Runnable onMainMenu) {
        this.model = GameModel.getInstance();
        this.model.addObserver(this); 
        
        // Ukuran default
        Dimension preferredSize = new Dimension(14 * TILE_SIZE + SIDEBAR_WIDTH, 10 * TILE_SIZE + 50);
        
        // 1. Inisialisasi Game Drawing Panel
        drawingPanel = new GameDrawingPanel(model); 
        drawingPanel.setPreferredSize(preferredSize);
        
        // 2. Inisialisasi Pause Overlay
        pauseOverlay = new PausePanel(() -> {
            model.togglePause(); 
        }, onRestart, onMainMenu); 

        // 3. Tambahkan komponen ke JLayeredPane (tanpa setBounds fixed)
        add(drawingPanel, JLayeredPane.DEFAULT_LAYER); 
        add(pauseOverlay, JLayeredPane.POPUP_LAYER); 
        
        // Set ukuran awal
        setPreferredSize(preferredSize);
        
        // PENTING: JANGAN jalankan thread di sini (Constructor)
    }

    // --- GAME LOOP CONTROL (BARU) ---
    public void startGameLoop() {
        if (gameThread != null && gameThread.isAlive()) {
            return; // Cegah double start
        }
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
        System.out.println("Game Loop Started!");
    }

    public void stopGameLoop() {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join(100); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }
        System.out.println("Game Loop Stopped!");
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (isRunning) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                // Update Logic (Jika ada di View, biasanya input/animasi)
                // Tapi logika utama game biasanya di Model (Thread terpisah atau Timer)
                // Di sini kita fokus repaint UI agar mulus
                repaint();
                delta--;
            }
        }
    }

    // --- OVERRIDE doLayout() untuk Responsiveness ---
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();

        // GameDrawingPanel harus selalu menutupi seluruh GamePanel
        if (drawingPanel != null) {
            drawingPanel.setBounds(0, 0, w, h);
        }
        // PauseOverlay juga harus menutupi seluruh GamePanel
        if (pauseOverlay != null) {
            pauseOverlay.setBounds(0, 0, w, h);
        }
        super.doLayout();
    }

    // --- GAME OBSERVER IMPLEMENTATION ---
    @Override
    public void update(Object gameState) {
        // 1. Update visibilitas Pause Menu berdasarkan status model
        if (pauseOverlay != null) {
             pauseOverlay.toggleVisibility(); 
        }
        
        // 2. Repaint game world.
        if (drawingPanel != null) {
            drawingPanel.repaint(); 
        }
    }
}