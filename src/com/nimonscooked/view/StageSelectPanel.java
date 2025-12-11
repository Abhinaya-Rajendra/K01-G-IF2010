package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class StageSelectPanel extends JPanel {

    private Consumer<Integer> onStageSelected; // Callback yang menerima Stage ID
    private Runnable onBack;

    public StageSelectPanel(Consumer<Integer> onStageSelected, Runnable onBack) {
        this.onStageSelected = onStageSelected;
        this.onBack = onBack;
        
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 60));

        // Header
        JLabel header = new JLabel("SELECT STAGE", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 40));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        // Stage Container
        JPanel stagesContainer = new JPanel(new GridBagLayout());
        stagesContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // --- STAGE 1 ---
        JPanel stage1 = createStageCard("Stage 1: The Beginning", "Normal Map", 150, 1);
        stagesContainer.add(stage1, gbc);

        // --- STAGE 2 ---
        JPanel stage2 = createStageCard("Stage 2: Chaos Kitchen", "Randomized Map", 300, 2);
        stagesContainer.add(stage2, gbc);

        add(stagesContainer, BorderLayout.CENTER);

        // Back Button
        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        backBtn.addActionListener(e -> onBack.run());
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backBtn);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createStageCard(String title, String desc, int target, int stageId) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(250, 300));
        card.setBackground(new Color(220, 220, 220));
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Arial", Font.ITALIC, 14));
        descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel targetLbl = new JLabel("Target: " + target + " pts");
        targetLbl.setFont(new Font("Arial", Font.BOLD, 16));
        targetLbl.setForeground(new Color(0, 100, 0));
        targetLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playBtn = new JButton("PLAY");
        playBtn.setFont(new Font("Arial", Font.BOLD, 20));
        playBtn.setBackground(new Color(0, 150, 0));
        playBtn.setForeground(Color.WHITE);
        playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playBtn.addActionListener(e -> onStageSelected.accept(stageId));

        card.add(Box.createVerticalStrut(20));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(20));
        card.add(targetLbl);
        card.add(Box.createVerticalGlue()); // Push button to bottom
        card.add(playBtn);
        card.add(Box.createVerticalStrut(20));

        return card;
    }
}