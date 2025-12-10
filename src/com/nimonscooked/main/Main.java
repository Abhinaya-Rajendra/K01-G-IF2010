package com.nimonscooked.main;

import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.view.AssetManager;
import com.nimonscooked.view.GameWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            System.out.println("Loading Assets...");
            AssetManager.getInstance();

            System.out.println("Initializing Game Model...");
            GameModel.getInstance(); 

            System.out.println("Starting UI...");
            GameWindow window = new GameWindow();
            window.setVisible(true);
            
            System.out.println("Nimonscooked is Running!");
        });
    }
}