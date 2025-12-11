package com.nimonscooked.view;

import com.nimonscooked.model.logic.GameModel;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends JPanel {

    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel targetLabel;
    private JLabel failedOrdersLabel;
    private JButton retryButton;
    private JButton menuButton;

    public ResultPanel(Runnable onRetry, Runnable onMenu) {
        setLayout(new GridBagLayout());
        setBackground(new Color(20, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. STATUS (PASSED / FAILED)
        statusLabel = new JLabel("STAGE CLEARED!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 50));
        add(statusLabel, gbc);

        // 2. DETAILS
        gbc.gridy++;
        scoreLabel = new JLabel("Your Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        scoreLabel.setForeground(Color.WHITE);
        add(scoreLabel, gbc);

        gbc.gridy++;
        targetLabel = new JLabel("Target Score: 0");
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        targetLabel.setForeground(Color.LIGHT_GRAY);
        add(targetLabel, gbc);

        gbc.gridy++;
        failedOrdersLabel = new JLabel("Failed Orders: 0");
        failedOrdersLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        failedOrdersLabel.setForeground(Color.RED);
        add(failedOrdersLabel, gbc);

        // 3. BUTTONS
        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        retryButton = new JButton("Retry Stage");
        retryButton.setFont(new Font("Arial", Font.BOLD, 18));
        retryButton.setPreferredSize(new Dimension(200, 50));
        retryButton.addActionListener(e -> onRetry.run());
        add(retryButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        menuButton = new JButton("Back to Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 18));
        menuButton.setPreferredSize(new Dimension(200, 50));
        menuButton.addActionListener(e -> onMenu.run());
        add(menuButton, gbc);
    }

    // Dipanggil saat CardLayout pindah ke halaman ini
    public void updateResult() {
        GameModel model = GameModel.getInstance();
        
        boolean passed = model.isStagePassed();
        int score = model.getScore();
        int target = model.getTargetScore();
        int failed = model.getFailedOrdersCount();

        if (passed) {
            statusLabel.setText("STAGE CLEARED!");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("STAGE FAILED");
            statusLabel.setForeground(Color.RED);
            
            // Info tambahan kenapa gagal
            if (failed >= model.getMaxFailedOrders()) {
                statusLabel.setText("TOO MANY FAILURES!");
            } else if (score < target) {
                statusLabel.setText("TIME'S UP!");
            }
        }

        scoreLabel.setText("Your Score: " + score);
        targetLabel.setText("Target Score: " + target);
        failedOrdersLabel.setText("Failed Orders: " + failed);
        
        repaint();
    }
}