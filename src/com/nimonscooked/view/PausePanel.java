package com.nimonscooked.view;

import com.nimonscooked.model.logic.GameModel;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Panel Overlay untuk Menu Pause.
 * Ditempatkan di atas GameDrawingPanel sebagai lapisan terpisah (POPUP_LAYER).
 */
public class PausePanel extends JPanel {
    
    // Callbacks yang dipanggil GameWindow saat tombol diklik
    private final Runnable onResume;
    private final Runnable onRestart;
    private final Runnable onMainMenu;

    private final GameModel model;
    private final AssetManager assets;

    // UI Styles
    private final Color OVERLAY_COLOR = new Color(0, 0, 0, 180); // Hitam semi-transparan
    private final Color BUTTON_BG = new Color(255, 165, 0); // Oranye
    private final Color TEXT_COLOR = Color.WHITE;
    private final Font FONT_TITLE = new Font("Comic Sans MS", Font.BOLD, 60);
    private final Font FONT_SCORE = new Font("Comic Sans MS", Font.PLAIN, 30);
    private final Font FONT_BUTTON = new Font("Comic Sans MS", Font.BOLD, 28);
    
    // Bounds Tombol (digunakan untuk deteksi klik)
    private Rectangle resumeBounds;
    private Rectangle restartBounds;
    private Rectangle menuBounds;

    public PausePanel(Runnable onResume, Runnable onRestart, Runnable onMainMenu) {
        this.onResume = onResume;
        this.onRestart = onRestart;
        this.onMainMenu = onMainMenu;
        this.model = GameModel.getInstance();
        this.assets = AssetManager.getInstance();

        setOpaque(false); // Penting: membuat background panel ini transparan
        setLayout(null);
        setupMouseListener();
        setVisible(false); // Awalnya tersembunyi
    }

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!model.isPaused()) return; // Jangan proses klik jika tidak dijeda
                
                if (resumeBounds != null && resumeBounds.contains(e.getPoint())) {
                    onResume.run();
                } else if (restartBounds != null && restartBounds.contains(e.getPoint())) {
                    onRestart.run();
                } else if (menuBounds != null && menuBounds.contains(e.getPoint())) {
                    onMainMenu.run();
                }
            }
        });
    }

    /**
     * Dipanggil oleh GamePanel (wrapper) saat GameModel berubah statusnya.
     */
    public void toggleVisibility() {
        if (isVisible() != model.isPaused()) {
            setVisible(model.isPaused());
            if (model.isPaused()) {
                repaint(); // Repaint untuk update skor/timer
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!model.isPaused()) return;
        
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. Overlay Semi-Transparan
        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0, 0, w, h);

        // 2. Judul Pause
        g2d.setFont(FONT_TITLE);
        g2d.setColor(TEXT_COLOR);
        // [ASSET PANDA: Image of game paused screen]
        drawOutlinedString(g2d, "GAME PAUSED", w / 4, h / 4, TEXT_COLOR, Color.RED.darker());

        // 3. Info Score & Timer (Data Terupdate)
        g2d.setFont(FONT_SCORE);
        g2d.setColor(TEXT_COLOR.brighter());
        
        String scoreStr = "Score: " + model.getScore();
        // CATATAN: GameDuration di GameModel sudah direvisi untuk memberikan waktu elapsed
        String timerStr = "Time Elapsed: " + formatTime(model.getGameDuration()); 
        
        drawCenteredString(g2d, scoreStr, w / 2, h / 4 + 70);
        drawCenteredString(g2d, timerStr, w / 2, h / 4 + 110);
        
        // 4. Tombol Aksi
        int btnWidth = 250;
        int btnHeight = 60;
        int startY = h / 2 + 50;
        int gap = 80;

        // Lanjutkan
        resumeBounds = drawButton(g2d, w / 2, startY, btnWidth, btnHeight, "LANJUTKAN", "ui_icon_resume"); // ASSET: ui_icon_resume
        
        // Restart
        restartBounds = drawButton(g2d, w / 2, startY + gap, btnWidth, btnHeight, "RESTART", "ui_icon_restart"); // ASSET: ui_icon_restart
        
        // Main Menu
        menuBounds = drawButton(g2d, w / 2, startY + gap * 2, btnWidth, btnHeight, "MAIN MENU", "ui_icon_menu"); // ASSET: ui_icon_menu
    }

    // Helper method untuk menggambar tombol
    private Rectangle drawButton(Graphics2D g2d, int centerX, int centerY, int width, int height, String text, String iconKey) {
        Rectangle bounds = new Rectangle(centerX - width / 2, centerY - height / 2, width, height);
        
        g2d.setColor(BUTTON_BG);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g2d.setStroke(new BasicStroke(1));
        
        g2d.setFont(FONT_BUTTON);
        drawCenteredString(g2d, text, centerX, centerY + g2d.getFontMetrics().getAscent() / 3);

        BufferedImage icon = assets.getImage(iconKey);
        int iconSize = 40;
        if (icon != null) {
            // Gambar icon di kiri tombol
            g2d.drawImage(icon, bounds.x + 15, centerY - iconSize/2, iconSize, iconSize, null);
        }

        return bounds;
    }

    // Helper method untuk menggambar teks bergaris tepi
    private void drawOutlinedString(Graphics2D g2d, String text, int x, int y, Color c, Color outline) {
        g2d.setColor(outline);
        g2d.drawString(text, x+3, y+3); 
        g2d.setColor(c);
        g2d.drawString(text, x, y);
    }
    
    // Helper method untuk menggambar teks di tengah X
    private void drawCenteredString(Graphics g, String text, int centerX, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = centerX - metrics.stringWidth(text) / 2;
        g.drawString(text, x, y);
    }
    
    // Helper method untuk memformat waktu
    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}