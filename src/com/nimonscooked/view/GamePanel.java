package com.nimonscooked.view;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.logic.GameModel;
import java.awt.*;
import javax.swing.*;

/**
 * GamePanel (Wrapper) - Bertindak sebagai JLayeredPane untuk menumpuk:
 * 1. GameDrawingPanel (Game World - Layer Bawah)
 * 2. PausePanel (Overlay Menu - Layer Atas)
 */
public class GamePanel extends JLayeredPane implements GameObserver { 

    private GameModel model;
    private GameDrawingPanel drawingPanel; 
    private PausePanel pauseOverlay; 
    
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
    }

    // --- FIX: OVERRIDE doLayout() untuk Responsiveness ---
    // Ketika JLayeredPane diubah ukurannya, doLayout akan dipanggil,
    // dan kita set bounds komponen internal agar mengisi seluruh ruang yang tersedia (getWidth/getHeight).
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
    // --- END FIX ---


    // --- GAME OBSERVER IMPLEMENTATION ---
    @Override
    public void update(Object gameState) {
        if (pauseOverlay.isVisible() != model.isPaused()) {
            pauseOverlay.toggleVisibility();
        }
    }
}