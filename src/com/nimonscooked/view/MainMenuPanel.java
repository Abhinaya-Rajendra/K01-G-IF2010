package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    private JButton startButton;
    private JButton exitButton;
    private JButton helpButton;

    public MainMenuPanel(Runnable goToStageSelect) {
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 40, 40)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // TITLE
        JLabel titleLabel = new JLabel("NIMONSCOOKED");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(Color.ORANGE);
        add(titleLabel, gbc);

        // BUTTONS
        gbc.gridy++;
        startButton = createButton("Start Game");
        startButton.addActionListener(e -> goToStageSelect.run());
        add(startButton, gbc);

        gbc.gridy++;
        helpButton = createButton("How to Play");
        helpButton.addActionListener(e -> showHelpDialog());
        add(helpButton, gbc);

        gbc.gridy++;
        exitButton = createButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton, gbc);
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

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}