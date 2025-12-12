package com.nimonscooked.controller;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.logic.GameModel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    
    // Status Tombol (Apakah sedang ditekan?)
    private boolean up, down, left, right;

    @Override
    public void keyPressed(KeyEvent e) {
        GameModel model = GameModel.getInstance();
        int key = e.getKeyCode();

        // 0. PAUSE TOGGLE (Selalu aktif kecuali Game Over)
        if (key == KeyEvent.VK_ESCAPE && !model.isGameOver()) {
            // Kita tidak perlu resetInputFlags di sini karena togglePause() sudah memanggil activeChef.stopMovement()
            model.togglePause();
            return; // Penting: Jangan proses input lain saat jeda
        }
        
        // 1. GAME OVER (Restart)
        if (model.isGameOver()) {
            if (key == KeyEvent.VK_R) {
                // Biarkan GameWindow/ResultPanel yang menangani restart melalui callback
            }
            return;
        }
        
        // 2. JIKA GAME DIJEDA, JANGAN PROSES INPUT LAINNYA
        if (model.isPaused()) return;


        // 3. GAMEPLAY
        Chef activeChef = model.getActiveChef();
        if (activeChef == null) return;

        // --- MOVEMENT (Set Flag True) ---
        if (key == KeyEvent.VK_W) up = true;
        else if (key == KeyEvent.VK_S) down = true;
        else if (key == KeyEvent.VK_A) left = true;
        else if (key == KeyEvent.VK_D) right = true;

        // --- ACTIONS (Trigger Sekali) ---
        
        // Interact (Space / V / C)
        else if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_V || key == KeyEvent.VK_C) {
            activeChef.interact(model.getMap());
        }
        
        // Throw Item (F)
        else if (key == KeyEvent.VK_F) {
            activeChef.throwItem();
        }
        
        // Dash (Shift) - Trigger sprint
        else if (key == KeyEvent.VK_SHIFT) {
            activeChef.dash();
        }
        
        // Switch Chef (Tab / B)
        else if (key == KeyEvent.VK_TAB || key == KeyEvent.VK_B) {
            resetInputFlags();
            activeChef.stopMovement();
            model.switchChef();
        }
        
        // Restart Manual (R)
        else if (key == KeyEvent.VK_R) {
            model.resetGame(model.getCurrentStageId());
        }

        // Update Input ke Chef
        if (model.getActiveChef() != null) {
            model.getActiveChef().setMovementInput(up, down, left, right);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        GameModel model = GameModel.getInstance();
        // FIX: Hanya proses keyReleased jika game tidak dijeda atau game over
        if (model.isGameOver() || model.isPaused()) return; 
        
        Chef activeChef = model.getActiveChef();
        if (activeChef == null) return;

        int key = e.getKeyCode();

        // --- MOVEMENT (Set Flag False) ---
        if (key == KeyEvent.VK_W) up = false;
        else if (key == KeyEvent.VK_S) down = false;
        else if (key == KeyEvent.VK_A) left = false;
        else if (key == KeyEvent.VK_D) right = false;

        // Update Input ke Chef
        activeChef.setMovementInput(up, down, left, right);
    }
    
    // Helper untuk mereset tombol saat ganti chef/restart
    private void resetInputFlags() {
        up = false;
        down = false;
        left = false;
        right = false;
    }
}