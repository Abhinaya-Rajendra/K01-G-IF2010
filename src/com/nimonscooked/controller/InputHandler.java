package com.nimonscooked.controller;

import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.utils.Direction;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    
    @Override
    public void keyPressed(KeyEvent e) {
        GameModel model = GameModel.getInstance();
        int key = e.getKeyCode();

        // --- 1. HANDLING GAME OVER ---
        if (model.isGameOver()) {
            // Saat Game Over, tombol 'R' melakukan Restart pada Stage yang sama
            if (key == KeyEvent.VK_R) {
                System.out.println("Restarting Game...");
                // FIX: Gunakan getCurrentStageId(), bukan boolean false
                model.resetGame(model.getCurrentStageId()); 
            }
            return; // Blokir input lain
        }

        // --- 2. HANDLING GAMEPLAY NORMAL ---
        Chef activeChef = model.getActiveChef();
        if (activeChef == null) return;

        // Movement (WASD)
        if (key == KeyEvent.VK_W) {
            activeChef.move(Direction.UP, model.getMap());
        } else if (key == KeyEvent.VK_S) {
            activeChef.move(Direction.DOWN, model.getMap());
        } else if (key == KeyEvent.VK_A) {
            activeChef.move(Direction.LEFT, model.getMap());
        } else if (key == KeyEvent.VK_D) {
            activeChef.move(Direction.RIGHT, model.getMap());
        }
        
        // Interact / Pick / Drop (Space)
        else if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_V || key == KeyEvent.VK_C) {
            activeChef.interact(model.getMap());
        }
        
        // Switch Chef (Tab / B)
        else if (key == KeyEvent.VK_B || key == KeyEvent.VK_TAB) {
            model.switchChef();
        }
        
        // Dash (Shift)
        else if (key == KeyEvent.VK_SHIFT) {
            activeChef.dash();
        }
        
        // Throw (F)
        else if (key == KeyEvent.VK_F) {
            activeChef.throwItem();
        }
        
        // Debug Restart (R) saat main biasa
        else if (key == KeyEvent.VK_R) {
             // FIX: Gunakan getCurrentStageId() juga di sini
            model.resetGame(model.getCurrentStageId());
        }
        
        // Update View
        model.notifyObservers();
    }
}