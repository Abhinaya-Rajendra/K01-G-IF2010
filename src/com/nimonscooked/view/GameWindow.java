package com.nimonscooked.view;

import com.nimonscooked.controller.InputHandler;
import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame implements GameObserver {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Panels
    private MainMenuPanel menuPanel;
    private StageSelectPanel stageSelectPanel;
    private ResultPanel resultPanel;
    
    // Game Container (Game + Order UI)
    private JPanel gameContainer;
    private GamePanel gamePanel;
    private OrderPanel orderPanel;
    
    private InputHandler inputHandler;

    public GameWindow() {
        setTitle("Nimonscooked - Tugas Besar OOP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // 1. Observer Model (Untuk deteksi Game Over otomatis)
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
        pack();
        setLocationRelativeTo(null); 

        // 5. Input Handler
        this.inputHandler = new InputHandler();
        this.addKeyListener(inputHandler);
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void initPanels() {
        // --- MENU ---
        menuPanel = new MainMenuPanel(
            () -> cardLayout.show(mainContainer, "STAGE_SELECT") // Go to Stage Select
        );

        // --- STAGE SELECT ---
        stageSelectPanel = new StageSelectPanel(
            (stageId) -> startGame(stageId), // On Stage Selected
            () -> cardLayout.show(mainContainer, "MENU") // On Back
        );

        // --- GAME CONTAINER ---
        gameContainer = new JPanel(new BorderLayout());
        gamePanel = new GamePanel();
        orderPanel = new OrderPanel();
        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.add(orderPanel, BorderLayout.EAST);

        // --- RESULT ---
        resultPanel = new ResultPanel(
            () -> startGame(GameModel.getInstance().getCurrentStageId()), // Retry
            () -> cardLayout.show(mainContainer, "MENU") // Back to Menu
        );
    }

    private void startGame(int stageId) {
        GameModel.getInstance().resetGame(stageId);
        cardLayout.show(mainContainer, "GAME");
        this.requestFocusInWindow();
    }

    // --- GAME OBSERVER IMPLEMENTATION ---
    @Override
    public void update(Object arg) {
        // Cek jika Game Model memberi sinyal Game Over
        GameModel model = GameModel.getInstance();
        
        // Kita cek apakah panel yang sedang aktif adalah GAME
        // (Agar tidak switch berulang-ulang)
        // Namun, karena model hanya set isGameOver sekali, kita bisa cek flagnya.
        
        if (model.isGameOver()) {
            // Kita beri sedikit delay atau langsung pindah ke result screen
            // Tapi karena update dipanggil berkali-kali, kita harus pastikan
            // kita belum di result screen.
            
            // Cara aman: ResultPanel update data, lalu switch.
            // Cek apakah ResultPanel sudah tampil? Tidak mudah di CardLayout.
            // Jadi kita trigger showResult jika belum.
            
            if (!resultPanel.isShowing()) {
                resultPanel.updateResult();
                cardLayout.show(mainContainer, "RESULT");
            }
        }
    }
}