package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel {

    private ImageButton startNormalButton;
    private ImageButton startRandomButton;
    private ImageButton helpButton;
    private ImageButton exitButton;

    private BufferedImage backgroundImage;
    private final AssetManager assets;

    private static final Dimension BUTTON_SIZE = new Dimension(300, 64);

    public MainMenuPanel(Runnable startNormalAction, Runnable startRandomAction) {
        this.assets = AssetManager.getInstance();

        setLayout(new GridBagLayout());
        setOpaque(false); // custom paint background

        // use AssetManager images (keys defined in AssetManager.loadAllImages)
        backgroundImage = assets.getImage("main_menu");

        // create buttons from AssetManager keys
        startNormalButton = createImageButton(
                assets.getImage("start_game(1)"),
                assets.getImage("start_game(2)"),
                assets.getImage("start_game(3)"),
                "Start Game"
        );

        startRandomButton = createImageButton(
                assets.getImage("start_random(1)"),
                assets.getImage("start_random(2)"),
                assets.getImage("start_random(3)"),
                "Start Random"
        );

        helpButton = createImageButton(
                assets.getImage("how_to_play(1)"),
                assets.getImage("how_to_play(2)"),
                assets.getImage("how_to_play(3)"),
                "How to Play"
        );

        exitButton = createImageButton(
                assets.getImage("exit(1)"),
                assets.getImage("exit(2)"),
                assets.getImage("exit(3)"),
                "Exit"
        );

        startNormalButton.addActionListener(e -> startNormalAction.run());
        startRandomButton.addActionListener(e -> startRandomAction.run());
        helpButton.addActionListener(e -> showHelpDialog());
        exitButton.addActionListener(e -> System.exit(0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("NIMONSCOOKED");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.ORANGE);
        add(titleLabel, gbc);

        gbc.gridy++;
        add(startNormalButton, gbc);
        gbc.gridy++;
        add(startRandomButton, gbc);
        gbc.gridy++;
        add(helpButton, gbc);
        gbc.gridy++;
        add(exitButton, gbc);
    }

    private ImageButton createImageButton(BufferedImage normal, BufferedImage hover, BufferedImage pressed, String alt) {
        ImageButton b = new ImageButton(normal, hover, pressed, alt);
        b.setPreferredSize(BUTTON_SIZE);
        return b;
    }

    private void showHelpDialog() {
        String help = "Controls:\n" +
                "WASD / Arrow : Move\n" +
                "SPACE : Interact\n" +
                "TAB / B : Switch Chef\n" +
                "SHIFT : Dash\n" +
                "F : Throw\n\nGoal: Cook and serve orders.";
        JOptionPane.showMessageDialog(this, help, "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // draw background image scaled; fallback to gradient
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, 0, getHeight(), Color.decode("#120843"));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
    }

    // --- ImageButton inner class with 3-state images ---
    private static class ImageButton extends JButton {
        private final BufferedImage normal;
        private final BufferedImage hover;
        private final BufferedImage pressedImg;
        private final String alt;
        private boolean hoverState = false;
        private static final int HOVER_SCALE = 110;

        ImageButton(BufferedImage normal, BufferedImage hover, BufferedImage pressedImg, String alt) {
            this.normal = normal;
            this.hover = hover != null ? hover : normal;
            this.pressedImg = pressedImg != null ? pressedImg : this.hover;
            this.alt = alt;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hoverState = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e)  { hoverState = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            ButtonModel m = getModel();

            BufferedImage imgToDraw;
            if (m.isPressed()) imgToDraw = pressedImg;
            else if (hoverState) imgToDraw = hover;
            else imgToDraw = normal;

            if (imgToDraw != null) {
                if (hoverState && !m.isPressed()) {
                    int sw = w * HOVER_SCALE / 100;
                    int sh = h * HOVER_SCALE / 100;
                    int sx = (w - sw) / 2;
                    int sy = (h - sh) / 2;
                    g2.drawImage(imgToDraw, sx, sy, sw, sh, this);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.drawRect(sx - 2, sy - 2, sw + 4, sh + 4);
                } else {
                    g2.drawImage(imgToDraw, 0, 0, w, h, this);
                }
            } else {
                g2.setColor(new Color(100, 100, 150));
                g2.fillRect(0, 0, w, h);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(alt)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(alt, tx, ty);
            }
            g2.dispose();
        }
    }
}