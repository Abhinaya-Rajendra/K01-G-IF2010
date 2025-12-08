package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    private JButton startNormalButton;
    private JButton startRandomButton;
    private JButton exitButton;

    public MainMenuPanel(Runnable startNormalAction, Runnable startRandomAction) {
        setLayout(new GridBagLayout());
        setBackground(new Color(60, 60, 60)); // Latar belakang gelap

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // --- TITLE ---
        JLabel titleLabel = new JLabel("NIMONSCOOKED");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.ORANGE);
        add(titleLabel, gbc);

        // --- BUTTONS ---
        gbc.gridy++;
        startNormalButton = createButton("Start Game (Type B)");
        startNormalButton.addActionListener(e -> startNormalAction.run());
        add(startNormalButton, gbc);

        gbc.gridy++;
        startRandomButton = createButton("Start Random Map (Bonus)");
        startRandomButton.addActionListener(e -> startRandomAction.run());
        add(startRandomButton, gbc);

        gbc.gridy++;
        JButton helpButton = createButton("How to Play");
        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Controls:\n" +
            "WASD : Move Active Chef\n" +
            "SPACE : Interact / Pick Up / Put Down\n" +
            "TAB / B : Switch Chef\n" +
            "SHIFT : Dash\n" +
            "F : Throw Item\n\n" +
            "Goal: Cook orders and serve them before time runs out!", 
            "How to Play", JOptionPane.INFORMATION_MESSAGE));
        add(helpButton, gbc);

        gbc.gridy++;
        exitButton = createButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        return btn;
    }
}