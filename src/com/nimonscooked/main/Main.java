package com.nimonscooked.main;

import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.view.AssetManager;
import com.nimonscooked.view.GameWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Jalankan GUI di Event Dispatch Thread (Standard Swing)
        SwingUtilities.invokeLater(() -> {
            
            // 1. Load Assets (Gambar)
            System.out.println("Loading Assets...");
            AssetManager.getInstance(); // Memicu loading gambar di constructor

            // 2. Init Game Model (Singleton)
            System.out.println("Initializing Game Model...");
            GameModel.getInstance(); 

            // 3. Show Window
            System.out.println("Starting UI...");
            GameWindow window = new GameWindow();
            window.setVisible(true);
            
            System.out.println("Nimonscooked is Running!");

        });
    }
}