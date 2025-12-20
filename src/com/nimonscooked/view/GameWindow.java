package com.nimonscooked.view;

import com.nimonscooked.controller.InputHandler;
import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

import java.util.function.Consumer;

public class GameWindow extends JFrame implements GameObserver {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Panels
    private MainMenuPanel menuPanel;
    private StageSelectPanel stageSelectPanel;
    private ResultPanel resultPanel; 
    
    // Game Container
    private JPanel gameContainer;
    private GamePanel gamePanel; 
    
    private InputHandler inputHandler;

    public GameWindow() {
        setTitle("Nimonscooked - Tugas Besar OOP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true); // Pastikan ini true agar bisa di-maximize

        // 1. Observer Model
        GameModel.getInstance().addObserver(this);

        // 2. Setup CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 3. Init Panels
        initPanels();

        // 4. Add Panels to CardLayout
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(stageSelectPanel, "STAGE_SELECT");
        mainContainer.add(gameContainer, "GAME");
        mainContainer.add(resultPanel, "RESULT");

        add(mainContainer);
        
        // HANYA PANGGIL pack() SATU KALI DI SINI
        pack(); 
        setLocationRelativeTo(null); 
        
        // Show initial screen
        cardLayout.show(mainContainer, "MENU");

        // 5. Input Handler
        this.inputHandler = new InputHandler();
        this.addKeyListener(inputHandler);
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void initPanels() {
        // --- GAME CONTROLS (Callbacks Baru) ---
        
        Runnable goToMenu = () -> {
            if (GameModel.getInstance().isPaused()) {
                GameModel.getInstance().togglePause();
            }
            
            // REVISI: Matikan Game Loop UI saat kembali ke menu
            if (gamePanel != null) {
                gamePanel.stopGameLoop();
            }
            
            cardLayout.show(mainContainer, "MENU");
            
            // Pastikan fokus kembali ke window agar keyboard bisa dipakai di menu
            requestFocusInWindow();
        };
        
        Runnable onRestartGame = () -> {
            int currentStageId = GameModel.getInstance().getCurrentStageId();
            startGame(currentStageId);
        };
        
        Consumer<Integer> onStartGame = (stageId) -> {
            startGame(stageId);
        };
        
        // --- MENU ---
        menuPanel = new MainMenuPanel(
            () -> {
                cardLayout.show(mainContainer, "STAGE_SELECT");
            }
        );

        // --- STAGE SELECT ---
        stageSelectPanel = new StageSelectPanel(
            onStartGame, 
            goToMenu 
        );

        // --- GAME CONTAINER ---
        gameContainer = new JPanel(new BorderLayout());
        gamePanel = new GamePanel(onRestartGame, goToMenu); 
        gameContainer.add(gamePanel, BorderLayout.CENTER);

        // --- RESULT ---
        resultPanel = new ResultPanel(
            onRestartGame, 
            goToMenu 
        );
    }

    private void startGame(int stageId) {
        // 1. Reset Model Logic
        GameModel.getInstance().resetGame(stageId);
        
        // 2. REVISI: Mulai Game Loop UI (Repaint)
        if (gamePanel != null) {
            gamePanel.startGameLoop();
        }
        
        // 3. Ganti tampilan ke Game
        cardLayout.show(mainContainer, "GAME");
        
        // Sangat Penting: Fokuskan agar InputHandler bisa menangkap keyboard
        this.requestFocusInWindow(); 
    }

    // --- GAME OBSERVER IMPLEMENTATION ---
    @Override
    public void update(Object arg) {
        GameModel model = GameModel.getInstance();
        
        if (model.isGameOver()) {
            // Logic transisi ke Result Panel
            if (!resultPanel.isVisible()) { // Cek sederhananya begini
                
                // REVISI: Matikan Game Loop UI saat Game Over
                if (gamePanel != null) {
                    gamePanel.stopGameLoop();
                }

                resultPanel.updateResult(); 
                cardLayout.show(mainContainer, "RESULT");
                
                requestFocusInWindow();
            }
        }
    }
}