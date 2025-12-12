package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class MainMenuPanel extends JPanel {

    private JButton startButton;
    private JButton exitButton;
    private JButton helpButton;
    
    // Variabel untuk menyimpan gambar latar belakang
    private BufferedImage backgroundImage; 
    private BufferedImage bg;

    
    // Path gambar (Asumsi: resources/images/background.png)
    // private static final String BACKGROUND_IMAGE_PATH = "images/menu_bg.png";

    public MainMenuPanel(Runnable goToStageSelect) {
        backgroundImage = AssetManager.getInstance().getImage("main_menu");
        bg = AssetManager.getInstance().getImage("bg");
        



        setLayout(new GridBagLayout());
        setBackground(new Color(40, 40, 40)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        // BUTTONS
        gbc.gridy++; // Pindah ke Baris berikutnya (Baris 1)
        // Buat komponen kosong (spacer) dengan tinggi 50 piksel
        JLabel spacer = new JLabel();
        // Anda bisa menyesuaikan tinggi (misalnya 50, 80, 100)
        spacer.setPreferredSize(new Dimension(1, 250)); 
        add(spacer, gbc);
        gbc.gridy++;
        // Gantilah ini: startButton = createButton("Start Game"); 
        startButton = AssetManager.getInstance().createButton("start","startHover", "startPressed", () -> goToStageSelect.run(), 380, 100); 
        add(startButton, gbc);

        gbc.gridy++;
        // Gantilah ini: helpButton = createButton("How to Play");
        helpButton = AssetManager.getInstance().createButton("help", "helpHover", "helpPressed",() -> showHelpDialog(), 380, 100); 
        add(helpButton, gbc);

        gbc.gridy++;
        // Gantilah ini: exitButton = createButton("Exit");
        exitButton = AssetManager.getInstance().createButton("exit", "exitHover", "exitPressed",() -> System.exit(0), 380, 100);
        add(exitButton, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gambar latar belakang
        if(bg != null){
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = 900;
            int panelHeight = 900;
            int x = (getWidth() - panelWidth) / 2;
            int y = (getHeight() - panelHeight) / 2;
            // Skala gambar agar sesuai dengan ukuran panel
            g.drawImage(backgroundImage, x, y+25, panelWidth, panelHeight, this);
            
        }
    }
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(this, 
            "<html><body style='width: 300px;'>" +
            "<h2>How to Play</h2>" +
            "<p><b>Goal:</b> Prepare, Cook, and Serve orders to satisfy the customers!</p>" +
            "<br>" +
            "<p><b>Controls:</b></p>" +
            "<ul>" +
            "<li><b>WASD</b>: Move Chef</li>" +
            "<li><b>SPACE</b>: Pick Up / Drop / Interact (Chop/Wash)</li>" +
            "<li><b>TAB</b>: Switch Chef</li>" +
            "<li><b>SHIFT</b>: Dash</li>" +
            "<li><b>F</b>: Throw Item</li>" +
            "</ul>" +
            "<br>" +
            "<p><b>Rules:</b></p>" +
            "<ul>" +
            "<li>Serve orders before they expire.</li>" +
            "<li><b>Game Over</b> if Time runs out OR 5 Failed Orders.</li>" +
            "<li>Reach the <b>Target Score</b> to pass the stage!</li>" +
            "</ul>" +
            "</body></html>", 
            "Game Guide", JOptionPane.INFORMATION_MESSAGE);
    }

    // Di dalam class MainMenuPanel


}