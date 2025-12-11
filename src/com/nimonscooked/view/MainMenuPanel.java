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

    
    // Path gambar (Asumsi: resources/images/background.png)
    // private static final String BACKGROUND_IMAGE_PATH = "images/menu_bg.png";

    public MainMenuPanel(Runnable goToStageSelect) {
        backgroundImage = AssetManager.getInstance().getImage("main_menu");
        



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
        spacer.setPreferredSize(new Dimension(1, 120)); 
        add(spacer, gbc);
        gbc.gridy++;
        // Gantilah ini: startButton = createButton("Start Game"); 
        startButton = createButton("start", () -> goToStageSelect.run(), 380, 100); 
        add(startButton, gbc);

        gbc.gridy++;
        // Gantilah ini: helpButton = createButton("How to Play");
        helpButton = createButton("help", () -> showHelpDialog(), 380, 100); 
        add(helpButton, gbc);

        gbc.gridy++;
        // Gantilah ini: exitButton = createButton("Exit");
        exitButton = createButton("exit", () -> System.exit(0), 380, 100);
        add(exitButton, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gambar latar belakang
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = 900;
            int panelHeight = 1000;
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

    private JButton createButton(String assetName, Runnable action, int targetWidth, int targetHeight) {
        // 1. Dapatkan Gambar dari AssetManager
    BufferedImage iconImg = AssetManager.getInstance().getImage(assetName);
        // 2. Buat Ikon dari Gambar
    Image scaledImage = iconImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    ImageIcon icon = new ImageIcon(scaledImage);

    // 3. Setup Tombol
    JButton btn = new JButton(icon); 
    
    // ** PENTING UNTUK TOMBOL GAMBAR **
    btn.setBorderPainted(false); // Hapus bingkai di sekitar tombol
    btn.setContentAreaFilled(false); // Hapus area latar belakang tombol
    btn.setFocusPainted(false); // Hapus fokus (garis kotak saat diklik)
    
    // Opsional: atur ukuran tombol berdasarkan ukuran gambar
    btn.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.addActionListener(e -> action.run());
    
    return btn;
    }
}