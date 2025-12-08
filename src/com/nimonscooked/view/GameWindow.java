package com.nimonscooked.view;

import com.nimonscooked.controller.InputHandler;
import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private GamePanel gamePanel;
    private MainMenuPanel menuPanel;
    private InputHandler inputHandler;

    public GameWindow() {
        setTitle("Nimonscooked - Tugas Besar OOP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Setup CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Init Panels
        this.gamePanel = new GamePanel(); // Pastikan GamePanel sudah Anda buat sebelumnya
        this.menuPanel = new MainMenuPanel(
            () -> startGame(false), // Aksi tombol Normal
            () -> startGame(true)   // Aksi tombol Random
        );

        // Add Panels to CardLayout
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(gamePanel, "GAME");

        add(mainContainer);
        pack(); // Sesuaikan ukuran window dengan konten
        setLocationRelativeTo(null); // Tengah layar

        // Setup Input Handler (Keyboard)
        this.inputHandler = new InputHandler();
        this.addKeyListener(inputHandler);
        
        // PENTING: Fokus agar keyboard terbaca
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void startGame(boolean isRandom) {
        // Reset Model Game Logic
        GameModel.getInstance().resetGame(isRandom);
        
        // Pindah tampilan ke GamePanel
        cardLayout.show(mainContainer, "GAME");
        
        // Pastikan fokus kembali ke JFrame agar keyboard jalan
        this.requestFocusInWindow();
    }
}