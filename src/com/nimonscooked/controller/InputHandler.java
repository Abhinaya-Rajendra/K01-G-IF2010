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
        Chef activeChef = model.getActiveChef();
        
        if (activeChef == null) return;

        int key = e.getKeyCode();

        // --- MOVEMENT (WASD) ---
        if (key == KeyEvent.VK_W) {
            activeChef.move(Direction.UP, model.getMap());
        } else if (key == KeyEvent.VK_S) {
            activeChef.move(Direction.DOWN, model.getMap());
        } else if (key == KeyEvent.VK_A) {
            activeChef.move(Direction.LEFT, model.getMap());
        } else if (key == KeyEvent.VK_D) {
            activeChef.move(Direction.RIGHT, model.getMap());
        }
        
        // --- INTERACT / PICK UP / DROP (Space / V) ---
        // Spek: Interact (V), Pick/Drop (C). Kita gabung jadi Spasi atau pisah.
        // Mari kita ikuti default spek tapi mapping ke tombol yang nyaman.
        // Space = Interact/Action (Potong/Cuci) & Pick/Drop (Standard Gaming)
        else if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_V || key == KeyEvent.VK_C) {
            activeChef.interact(model.getMap());
        }
        
        // --- SWITCH CHEF (B / Tab) ---
        else if (key == KeyEvent.VK_B || key == KeyEvent.VK_TAB) {
            model.switchChef();
        }
        
        // --- DASH (Shift) ---
        else if (key == KeyEvent.VK_SHIFT) {
            activeChef.dash();
        }
        
        // --- THROW (F) ---
        else if (key == KeyEvent.VK_F) {
            activeChef.throwItem();
        }
        
        // --- DEBUG: RESET (R) ---
        else if (key == KeyEvent.VK_R) {
            model.resetGame(false);
        }
        
        // Update View setelah input
        model.notifyObservers();
    }
}