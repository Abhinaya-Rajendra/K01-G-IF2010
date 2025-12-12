package com.nimonscooked.view;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

/**
 * GamePanel BARU (Wrapper) - Bertindak sebagai JLayeredPane untuk menumpuk:
 * 1. GameDrawingPanel (Game World - Layer Bawah)
 * 2. PausePanel (Overlay Menu - Layer Atas)
 */
public class GamePanel extends JLayeredPane implements GameObserver { 

    private GameModel model;
    private GameDrawingPanel drawingPanel; // Panel yang berisi semua logika gambar lama
    private PausePanel pauseOverlay;       // Panel Overlay Menu

    private final int TILE_SIZE = 90;
    private final int SIDEBAR_WIDTH = 220;
    
    // Konstruktor menerima callbacks dari GameWindow
    public GamePanel(Runnable onRestart, Runnable onMainMenu) {
        this.model = GameModel.getInstance();
        // GamePanel (Wrapper) yang menjadi observer utama
        this.model.addObserver(this); 
        
        // 1. Inisialisasi Game Drawing Panel (Game World - Layer Bawah)
        // Ukuran panel dihitung dari GameDrawingPanel (berisi logika gambar lama)
        drawingPanel = new GameDrawingPanel(model); 
        Dimension preferredSize = new Dimension(14 * TILE_SIZE + SIDEBAR_WIDTH, 10 * TILE_SIZE + 50);
        drawingPanel.setPreferredSize(preferredSize);
        
        // Atur ukuran Layered Pane sama dengan drawing panel
        setPreferredSize(preferredSize);
        
        // 2. Inisialisasi Pause Overlay (Layer Atas)
        pauseOverlay = new PausePanel(() -> {
            // onResume: Panggil togglePause di model
            model.togglePause(); 
        }, onRestart, onMainMenu);

        // 3. Tambahkan komponen ke JLayeredPane
        // Layer Bawah: Game World
        drawingPanel.setBounds(0, 0, preferredSize.width, preferredSize.height);
        add(drawingPanel, JLayeredPane.DEFAULT_LAYER); 
        
        // Layer Atas: Pause Menu Overlay
        pauseOverlay.setBounds(0, 0, preferredSize.width, preferredSize.height);
        add(pauseOverlay, JLayeredPane.POPUP_LAYER); 
    }

    // --- GAME OBSERVER IMPLEMENTATION ---
    @Override
    public void update(Object gameState) {
        // 1. Update visibilitas Pause Menu berdasarkan status model
        pauseOverlay.toggleVisibility(); 
        
        // 2. Repaint game world
        // Drawing panel hanya akan repain jika game tidak dijeda/game over
        // Namun, jika tidak paused, updateGame() di GameModel akan terus berjalan.
        // Cukup repaint saja, logika if (model.isPaused) return; sudah ada di GameDrawingPanel
        drawingPanel.repaint(); 
    }
}