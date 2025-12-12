package com.nimonscooked.view;

import com.nimonscooked.model.logic.GameModel;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class StageSelectPanel extends JPanel {
    
    // Callback ke GameWindow
    private Consumer<Integer> onStageSelected; // Untuk startGame(stageId)
    private Runnable onBack; // Untuk kembali ke MENU
    
    private GameModel model;
    private AssetManager assets;
    private int selectedStage = 0; // Stage ID yang sedang dipilih (0: tidak ada)
    
    // --- KONSTANTA UI & WARNA ---
    private final Color HEADER_BG = new Color(74, 184, 189); // Biru muda
    private final Color FOOTER_BG = new Color(85, 45, 140);  // Ungu
    private final Color MAIN_BG = new Color(255, 245, 220);  // Krem (Beige tipis)
    private final Color CARD_BG = new Color(110, 240, 255);  // Cyan kartu
    private final Color HIGHLIGHT_COLOR = new Color(255, 200, 0); // Kuning emas
    
    // FONT
    private final Font FONT_HEADER = new Font("Comic Sans MS", Font.BOLD, 48); 
    private final Font FONT_STAGE_TITLE = new Font("Comic Sans MS", Font.BOLD, 30);
    private final Font FONT_STAGE_DETAIL = new Font("Comic Sans MS", Font.ITALIC, 20);
    private final Font FONT_TARGET_SCORE = new Font("Comic Sans MS", Font.BOLD, 22);
    private final Font FONT_BUTTON = new Font("Comic Sans MS", Font.BOLD, 22);
    
    // --- STAGE DATA ---
    private final StageInfo[] stages = {
        new StageInfo(1, "Stage 1: The Beginning", "normal map", 150),
        new StageInfo(2, "Stage 2: Chaos Kitchen", "random map", 200) 
    };
    
    // Helper Class untuk data stage
    private static class StageInfo {
        int id;
        String title;
        String mapType;
        int targetScore;
        public StageInfo(int id, String title, String mapType, int targetScore) {
            this.id = id;
            this.title = title;
            this.mapType = mapType;
            this.targetScore = targetScore;
        }
    }

    public StageSelectPanel(Consumer<Integer> onStageSelected, Runnable onBack) {
        this.onStageSelected = onStageSelected;
        this.onBack = onBack;
        this.model = GameModel.getInstance();
        this.assets = AssetManager.getInstance();
        
        setLayout(null); // Gunakan null layout
        setBackground(MAIN_BG);
        setupMouseListener();
    }
    
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int w = getWidth();
                int h = getHeight();
                
                // 1. Cek Klik Card Stage (Seleksi)
                for (int i = 0; i < stages.length; i++) {
                    Rectangle cardBounds = getCardBounds(w, h, i);
                    if (cardBounds.contains(x, y)) {
                        selectedStage = stages[i].id;
                        repaint();
                        return;
                    }
                }
                
                // 2. Cek Klik Tombol Footer (Area klik tetap dihitung dari teks)
                Rectangle btnBackBounds = getButtonBounds(w, h, "KEMBALI");
                Rectangle btnLanjutBounds = getButtonBounds(w, h, "LANJUTKAN");

                if (btnBackBounds.contains(x, y)) {
                    selectedStage = 0; // Reset selection
                    onBack.run(); // Kembali ke Main Menu
                } else if (btnLanjutBounds.contains(x, y)) {
                    if (selectedStage != 0) {
                        onStageSelected.accept(selectedStage); // Start Game
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();

        // 1. Background Utama (Krem/Beige tipis) - Sudah diatur di constructor
        
        // 2. Background Garis Vertikal (Biru Muda Tua)
        drawBackgroundStripes(g2d, w, h);

        // 3. Header (Biru Muda)
        drawHeader(g2d, w);
        
        // 4. Footer (Ungu)
        drawFooter(g2d, w, h);

        // 5. Stage Cards
        for (int i = 0; i < stages.length; i++) {
            drawStageCard(g2d, w, h, stages[i], i);
        }
    }

    private void drawBackgroundStripes(Graphics2D g2d, int w, int h) {
        // Gambar garis vertikal biru muda tua di area bawah (sesuai desain)
        g2d.setColor(HEADER_BG.darker()); 
        int stripeWidth = 20;
        int startY = 100 + 10; // Mulai di bawah header + zig-zag
        
        // Gunakan clip untuk membatasi gambar hanya di area tengah
        g2d.setClip(0, startY, w, h - startY - 80); 
        
        for (int x = 0; x < w; x += stripeWidth * 2) {
            g2d.fillRect(x, 0, stripeWidth, h);
        }
        g2d.setClip(null); // Reset clip
    }

    private void drawHeader(Graphics2D g2d, int w) {
        g2d.setColor(HEADER_BG);
        g2d.fillRect(0, 0, w, 100);
        
        // Zig-zag border di bawah header
        g2d.setColor(HEADER_BG.darker());
        int zigZagH = 10;
        for (int x = 0; x < w; x += 20) {
            g2d.fillPolygon(new int[]{x, x + 10, x + 20}, new int[]{100, 100 + zigZagH, 100}, 3);
        }
        
        // Text "SELECT STAGE"
        g2d.setColor(Color.WHITE);
        g2d.setFont(FONT_HEADER);
        drawOutlinedString(g2d, "SELECT STAGE", 50, 70, Color.WHITE, Color.BLACK.darker());
    }

    private void drawFooter(Graphics2D g2d, int w, int h) {
        g2d.setColor(FOOTER_BG);
        g2d.fillRect(0, h - 80, w, 80);
        
        // Zig-zag border di atas footer
        g2d.setColor(FOOTER_BG.darker());
        int zigZagH = 10;
        for (int x = 0; x < w; x += 20) {
            g2d.fillPolygon(new int[]{x, x + 10, x + 20}, new int[]{h - 80, h - 80 - zigZagH, h - 80}, 3);
        }
        
        // Tombol Kembali
        drawButton(g2d, w, h, "KEMBALI", Color.WHITE, "ui_icon_o");
        
        // Tombol Lanjutkan (Berwarna jika stage sudah dipilih)
        Color lanjutColor = (selectedStage != 0) ? Color.WHITE : Color.LIGHT_GRAY;
        drawButton(g2d, w, h, "LANJUTKAN", lanjutColor, "ui_icon_x");
    }
    
    private void drawButton(Graphics2D g2d, int w, int h, String text, Color textColor, String iconKey) {
        // Ambil bounds (ini adalah bounds untuk teks saja, digunakan juga untuk klik)
        Rectangle bounds = getButtonBounds(w, h, text);
        
        int iconSize = 40;
        // Posisi Icon: 5px sebelum awal teks.
        int iconX = bounds.x - iconSize - 5; 
        // Posisi Y Icon: Disesuaikan agar center vertikal dengan teks
        int iconY = bounds.y - 15; 

        // --- Gambar Icon ---
        BufferedImage icon = assets.getImage(iconKey);
        
        if (icon != null) {
            g2d.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            // Fallback (jika aset gagal dimuat)
            g2d.setColor(textColor);
            if (iconKey.endsWith("_o")) g2d.drawOval(iconX, iconY, iconSize, iconSize);
            else {
                g2d.drawLine(iconX, iconY, iconX + iconSize, iconY + iconSize);
                g2d.drawLine(iconX + iconSize, iconY, iconX, iconY + iconSize);
            }
        }

        // --- Gambar Text ---
        g2d.setColor(textColor);
        g2d.setFont(FONT_BUTTON);
        // Gambar text di posisi yang telah ditentukan (bounds.x, bounds.y)
        drawOutlinedString(g2d, text, bounds.x, bounds.y + bounds.height - 5, textColor, Color.BLACK.darker().darker());
    }
    
    private Rectangle getButtonBounds(int w, int h, String text) {
        FontMetrics fm = getFontMetrics(FONT_BUTTON);
        int textW = fm.stringWidth(text);
        int textH = fm.getHeight();
        
        int y = h - 50;
        int x;

        if (text.equals("KEMBALI")) {
            x = w / 2 - textW - 100; // Posisi Teks KEMBALI
        } else { // LANJUTKAN
            x = w / 2 + 60; // Posisi Teks LANJUTKAN
        }
        
        // Catatan: Bounds ini hanya mencakup area teks (untuk klik), ikon tidak termasuk dalam bounds ini
        return new Rectangle(x, y, textW, textH); 
    }
    
    private Rectangle getCardBounds(int w, int h, int index) {
        int cardW = w / 3;
        int cardH = h / 2;
        int padding = 40;
        
        int x = (index == 0) ? padding : w - cardW - padding;
        int y = h / 2 - cardH / 2 + 20; 

        return new Rectangle(x, y, cardW, cardH);
    }

    private void drawStageCard(Graphics2D g2d, int w, int h, StageInfo stage, int index) {
        Rectangle bounds = getCardBounds(w, h, index);
        
        // --- 1. ROTASI & HIGHLIGHT ---
        double rotation = (index == 0) ? -2 : 3;
        
        g2d.rotate(Math.toRadians(rotation), bounds.getCenterX(), bounds.getCenterY());
        
        // Highlight jika terpilih
        if (selectedStage == stage.id) {
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.fillRoundRect(bounds.x - 10, bounds.y - 10, bounds.width + 20, bounds.height + 20, 30, 30);
        }

        // --- 2. KARTU FISIK (DASAR) ---
        // Shadow/Border Putih
        g2d.setColor(Color.WHITE.darker()); 
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Gambar Kartu Cyan
        int padding = 5;
        g2d.setColor(CARD_BG);
        g2d.fill(new RoundRectangle2D.Double(
            bounds.x + padding, bounds.y + padding, 
            bounds.width - padding * 2, bounds.height - padding * 2, 
            10, 10
        ));
        
        // Gambar Map Preview di dalam Kartu (Placeholder)
        g2d.setColor(Color.WHITE); // Area Preview Map
        g2d.fillRoundRect(bounds.x + 20, bounds.y + 100, bounds.width - 40, bounds.height - 120, 10, 10);
        // [TODO: Gambar preview map bisa ditambahkan di sini]

        // --- 3. DETAIL (TEKS & JEPITAN) ---
        
        // Clothespin (Penjepit) - Menggunakan ASET
        BufferedImage pin = assets.getImage("ui_clothespin"); 
        int pinSize = 30;
        if (pin != null) {
            g2d.drawImage(pin, (int)bounds.getCenterX() - pinSize / 2, bounds.y - 20, pinSize, pinSize, null);
        } else {
             g2d.setColor(new Color(160, 82, 45)); 
             g2d.fillRect((int)bounds.getCenterX() - 10, bounds.y - 15, 20, 25);
        }
        
        // Kembalikan rotasi agar teks digambar lurus di atas kartu
        g2d.rotate(Math.toRadians(-rotation), bounds.getCenterX(), bounds.getCenterY());
        
        // Text Stage Title
        g2d.setColor(FOOTER_BG.darker());
        g2d.setFont(FONT_STAGE_TITLE);
        drawCenteredString(g2d, stage.title, (int)bounds.getCenterX(), bounds.y + 35);
        
        // Text Detail (map type)
        g2d.setColor(Color.BLACK.darker());
        g2d.setFont(FONT_STAGE_DETAIL);
        drawCenteredString(g2d, "Map Type: " + stage.mapType, (int)bounds.getCenterX(), bounds.y + 60);
        
        // Text Target Score
        g2d.setColor(Color.RED.darker());
        g2d.setFont(FONT_TARGET_SCORE);
        drawCenteredString(g2d, "Target: " + stage.targetScore + " pts", (int)bounds.getCenterX(), bounds.y + 90);
        
        // Kembalikan rotasi global untuk kartu berikutnya
        g2d.rotate(Math.toRadians(rotation), bounds.getCenterX(), bounds.getCenterY());
    }

    private void drawOutlinedString(Graphics2D g2d, String text, int x, int y, Color c, Color outline) {
        g2d.setColor(outline);
        for(int i=-2; i<=2; i++) {
            for(int j=-2; j<=2; j++) {
                if(i!=0 || j!=0) g2d.drawString(text, x+i, y+j);
            }
        }
        g2d.setColor(c);
        g2d.drawString(text, x, y);
    }
    
    private void drawCenteredString(Graphics g, String text, int centerX, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = centerX - metrics.stringWidth(text) / 2;
        g.drawString(text, x, y);
    }
}