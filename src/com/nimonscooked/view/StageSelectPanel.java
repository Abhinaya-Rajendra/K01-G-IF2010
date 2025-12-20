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
    private Consumer<Integer> onStageSelected; 
    private Runnable onBack; 
    
    private GameModel model;
    private AssetManager assets;
    private int selectedStage = 0; 
    
    // --- KONSTANTA UI & WARNA ---
    private final Color HEADER_BG = new Color(74, 184, 189); 
    private final Color FOOTER_BG = new Color(85, 45, 140);  
    private final Color MAIN_BG = new Color(255, 245, 220);  
    private final Color CARD_BG = new Color(110, 240, 255);  
    private final Color HIGHLIGHT_COLOR = new Color(255, 200, 0); 
    
    // FONT
    private final Font FONT_HEADER = new Font("Comic Sans MS", Font.BOLD, 48); 
    private final Font FONT_STAGE_TITLE = new Font("Comic Sans MS", Font.BOLD, 30);
    private final Font FONT_STAGE_DETAIL = new Font("Comic Sans MS", Font.ITALIC, 20);
    private final Font FONT_TARGET_SCORE = new Font("Comic Sans MS", Font.BOLD, 22);
    private final Font FONT_BUTTON = new Font("Comic Sans MS", Font.BOLD, 22);
    
    // --- STAGE DATA (UPDATED) ---
    // Menambahkan parameter string terakhir sebagai Key untuk AssetManager
    private final StageInfo[] stages = {
        new StageInfo(1, "Stage 1: The Beginning", "normal map", 150, "preview_stage_1"),
        new StageInfo(2, "Stage 2: Chaos Kitchen", "random map", 200, "preview_stage_2") 
    };
    
    // Helper Class Updated
    private static class StageInfo {
        int id;
        String title;
        String mapType;
        int targetScore;
        String imageKey; // Field baru untuk kunci aset gambar

        public StageInfo(int id, String title, String mapType, int targetScore, String imageKey) {
            this.id = id;
            this.title = title;
            this.mapType = mapType;
            this.targetScore = targetScore;
            this.imageKey = imageKey;
        }
    }

    public StageSelectPanel(Consumer<Integer> onStageSelected, Runnable onBack) {
        this.onStageSelected = onStageSelected;
        this.onBack = onBack;
        this.model = GameModel.getInstance();
        this.assets = AssetManager.getInstance();
        
        setLayout(null); 
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
                
                // 1. Cek Klik Card Stage
                for (int i = 0; i < stages.length; i++) {
                    Rectangle cardBounds = getCardBounds(w, h, i);
                    if (cardBounds.contains(x, y)) {
                        selectedStage = stages[i].id;
                        repaint();
                        return;
                    }
                }
                
                // 2. Cek Klik Tombol Footer
                Rectangle btnBackBounds = getButtonBounds(w, h, "KEMBALI");
                Rectangle btnLanjutBounds = getButtonBounds(w, h, "LANJUTKAN");

                if (btnBackBounds.contains(x, y)) {
                    selectedStage = 0; 
                    onBack.run(); 
                } else if (btnLanjutBounds.contains(x, y)) {
                    if (selectedStage != 0) {
                        onStageSelected.accept(selectedStage); 
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

        drawBackgroundStripes(g2d, w, h);
        drawHeader(g2d, w);
        drawFooter(g2d, w, h);

        for (int i = 0; i < stages.length; i++) {
            drawStageCard(g2d, w, h, stages[i], i);
        }
    }

    private void drawBackgroundStripes(Graphics2D g2d, int w, int h) {
        g2d.setColor(HEADER_BG.darker()); 
        int stripeWidth = 20;
        int startY = 100 + 10; 
        
        g2d.setClip(0, startY, w, h - startY - 80); 
        
        for (int x = 0; x < w; x += stripeWidth * 2) {
            g2d.fillRect(x, 0, stripeWidth, h);
        }
        g2d.setClip(null); 
    }

    private void drawHeader(Graphics2D g2d, int w) {
        g2d.setColor(HEADER_BG);
        g2d.fillRect(0, 0, w, 100);
        
        g2d.setColor(HEADER_BG.darker());
        int zigZagH = 10;
        for (int x = 0; x < w; x += 20) {
            g2d.fillPolygon(new int[]{x, x + 10, x + 20}, new int[]{100, 100 + zigZagH, 100}, 3);
        }
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(FONT_HEADER);
        drawOutlinedString(g2d, "SELECT STAGE", 50, 70, Color.WHITE, Color.BLACK.darker());
    }

    private void drawFooter(Graphics2D g2d, int w, int h) {
        g2d.setColor(FOOTER_BG);
        g2d.fillRect(0, h - 80, w, 80);
        
        g2d.setColor(FOOTER_BG.darker());
        int zigZagH = 10;
        for (int x = 0; x < w; x += 20) {
            g2d.fillPolygon(new int[]{x, x + 10, x + 20}, new int[]{h - 80, h - 80 - zigZagH, h - 80}, 3);
        }
        
        drawButton(g2d, w, h, "KEMBALI", Color.WHITE, "ui_icon_o");
        Color lanjutColor = (selectedStage != 0) ? Color.WHITE : Color.LIGHT_GRAY;
        drawButton(g2d, w, h, "LANJUTKAN", lanjutColor, "ui_icon_x");
    }
    
    private void drawButton(Graphics2D g2d, int w, int h, String text, Color textColor, String iconKey) {
        Rectangle bounds = getButtonBounds(w, h, text);
        int iconSize = 40;
        int iconX = bounds.x - iconSize - 5; 
        int iconY = bounds.y - 15; 

        BufferedImage icon = assets.getImage(iconKey);
        
        if (icon != null) {
            g2d.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2d.setColor(textColor);
            if (iconKey.endsWith("_o")) g2d.drawOval(iconX, iconY, iconSize, iconSize);
            else {
                g2d.drawLine(iconX, iconY, iconX + iconSize, iconY + iconSize);
                g2d.drawLine(iconX + iconSize, iconY, iconX, iconY + iconSize);
            }
        }

        g2d.setColor(textColor);
        g2d.setFont(FONT_BUTTON);
        drawOutlinedString(g2d, text, bounds.x, bounds.y + bounds.height - 5, textColor, Color.BLACK.darker().darker());
    }
    
    private Rectangle getButtonBounds(int w, int h, String text) {
        FontMetrics fm = getFontMetrics(FONT_BUTTON);
        int textW = fm.stringWidth(text);
        int textH = fm.getHeight();
        
        int y = h - 50;
        int x;

        if (text.equals("KEMBALI")) {
            x = w / 2 - textW - 100; 
        } else { 
            x = w / 2 + 60; 
        }
        
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

    // --- BAGIAN UTAMA YANG DIUBAH UNTUK GAMBAR PREVIEW ---
    private void drawStageCard(Graphics2D g2d, int w, int h, StageInfo stage, int index) {
        Rectangle bounds = getCardBounds(w, h, index);
        
        // 1. ROTASI
        double rotation = (index == 0) ? -2 : 3;
        
        // Simpan transform lama sebelum rotasi untuk card ini
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotation), bounds.getCenterX(), bounds.getCenterY());
        
        // Highlight jika terpilih
        if (selectedStage == stage.id) {
            g2d.setColor(HIGHLIGHT_COLOR);
            g2d.fillRoundRect(bounds.x - 10, bounds.y - 10, bounds.width + 20, bounds.height + 20, 30, 30);
        }

        // 2. KARTU FISIK (DASAR)
        g2d.setColor(Color.WHITE.darker()); 
        g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        int padding = 5;
        g2d.setColor(CARD_BG);
        g2d.fill(new RoundRectangle2D.Double(
            bounds.x + padding, bounds.y + padding, 
            bounds.width - padding * 2, bounds.height - padding * 2, 
            10, 10
        ));
        
        // --- 3. GAMBAR PREVIEW MAP (DENGAN CLIPPING) ---
        // Tentukan area gambar (sama seperti kotak putih sebelumnya)
        int previewX = bounds.x + 20;
        int previewY = bounds.y + 100;
        int previewW = bounds.width - 40;
        int previewH = bounds.height - 120;
        
        // Buat bentuk Rounded Rectangle untuk area gambar
        RoundRectangle2D previewShape = new RoundRectangle2D.Double(previewX, previewY, previewW, previewH, 10, 10);
        
        // Gambar background putih dulu (jika gambar transparan/belum load)
        g2d.setColor(Color.WHITE);
        g2d.fill(previewShape);
        
        // Ambil Gambar dari AssetManager
        BufferedImage previewImg = assets.getImage(stage.imageKey);
        
        if (previewImg != null) {
            // Simpan Clip lama
            Shape oldClip = g2d.getClip();
            
            // Set Clip baru sesuai bentuk Rounded Rectangle
            g2d.setClip(previewShape);
            
            // Gambar image (di-stretch sesuai ukuran area)
            g2d.drawImage(previewImg, previewX, previewY, previewW, previewH, null);
            
            // Kembalikan Clip lama
            g2d.setClip(oldClip);
            
            // Tambahkan border tipis agar rapi
            g2d.setColor(Color.GRAY);
            g2d.draw(previewShape);
        } else {
            // Fallback Text jika gambar tidak ada
            g2d.setColor(Color.GRAY);
            drawCenteredString(g2d, "No Preview", (int)previewShape.getCenterX(), (int)previewShape.getCenterY());
        }

        // --- 4. DETAIL (TEKS & JEPITAN) ---
        
        // Kita perlu membatalkan rotasi SEMENTARA untuk teks agar tegak lurus (opsional, tapi di kode lama dilakukan)
        // Di kode lama: g2d.rotate(-rotation, ...). 
        // Tapi di sini lebih aman kita restore transform saja nanti di akhir method.
        // Untuk mengikuti style kode lama agar teks tidak miring berlebihan:
        g2d.rotate(Math.toRadians(-rotation), bounds.getCenterX(), bounds.getCenterY());

        // Clothespin
        BufferedImage pin = assets.getImage("ui_clothespin"); 
        int pinSize = 30;
        if (pin != null) {
            g2d.drawImage(pin, (int)bounds.getCenterX() - pinSize / 2, bounds.y - 20, pinSize, pinSize, null);
        } else {
             g2d.setColor(new Color(160, 82, 45)); 
             g2d.fillRect((int)bounds.getCenterX() - 10, bounds.y - 15, 20, 25);
        }
        
        // Texts
        g2d.setColor(FOOTER_BG.darker());
        g2d.setFont(FONT_STAGE_TITLE);
        drawCenteredString(g2d, stage.title, (int)bounds.getCenterX(), bounds.y + 35);
        
        g2d.setColor(Color.BLACK.darker());
        g2d.setFont(FONT_STAGE_DETAIL);
        drawCenteredString(g2d, "Map Type: " + stage.mapType, (int)bounds.getCenterX(), bounds.y + 60);
        
        g2d.setColor(Color.RED.darker());
        g2d.setFont(FONT_TARGET_SCORE);
        drawCenteredString(g2d, "Target: " + stage.targetScore + " pts", (int)bounds.getCenterX(), bounds.y + 90);
        
        // Kembalikan Transformasi awal (termasuk membatalkan rotasi)
        g2d.setTransform(oldTransform);
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