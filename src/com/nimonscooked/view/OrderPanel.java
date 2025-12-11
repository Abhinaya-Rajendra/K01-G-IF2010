package com.nimonscooked.view;

import com.nimonscooked.core.GameObserver;
import com.nimonscooked.model.entities.Order;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.utils.IngredientType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class OrderPanel extends JPanel implements GameObserver {

    private GameModel model;
    private AssetManager assets;

    public OrderPanel() {
        this.model = GameModel.getInstance();
        this.model.addObserver(this); // Subscribe agar repaint saat ada update
        this.assets = AssetManager.getInstance();

        // Lebar panel kanan (misal 200px)
        setPreferredSize(new Dimension(200, 600)); 
        setBackground(new Color(60, 63, 65)); // Warna latar panel (Dark Gray)
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Judul Panel
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("ORDERS", 10, 25);

        if (model.getOrderManager() == null) return;

        int yPos = 40;
        int cardHeight = 80;
        int gap = 10;

        // Loop semua order aktif
        for (Order order : model.getOrderManager().getActiveOrders()) {
            drawOrderCard(g, order, 10, yPos, 180, cardHeight);
            yPos += cardHeight + gap;
        }
    }

    private void drawOrderCard(Graphics g, Order order, int x, int y, int w, int h) {
        // 1. Background Kartu (Putih/Terang)
        g.setColor(new Color(240, 240, 240));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setColor(Color.GRAY);
        g.drawRoundRect(x, y, w, h, 15, 15);

        // 2. Gambar Hasil Masakan (Kiri)
        String dishName = order.getRecipe().getName();
        BufferedImage dishImg = assets.getImage("dish_" + dishName);
        if (dishImg == null) dishImg = assets.getImage("plate"); // Fallback
        
        if (dishImg != null) {
            g.drawImage(dishImg, x + 10, y + 10, 40, 40, null);
        }

        // 3. Nama Masakan (Atas)
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(dishName, x + 60, y + 20);

        // 4. Bahan-bahan yang diperlukan (Tengah - Icon Kecil)
        int iconX = x + 60;
        int iconY = y + 25;
        List<IngredientType> ingredients = order.getRecipe().getRequiredIngredients();
        
        for (IngredientType type : ingredients) {
            // Coba ambil icon kecil, kalau ga ada pakai storage image
            BufferedImage ingImg = assets.getImage("icon_" + type.name());
            if (ingImg == null) ingImg = assets.getImage("storage_" + type.name());
            
            if (ingImg != null) {
                g.drawImage(ingImg, iconX, iconY, 20, 20, null);
                iconX += 22; // Geser ke kanan
            }
        }

        // 5. Progress Bar Waktu (Bawah)
        int barHeight = 8;
        int barY = y + h - 15;
        int barWidth = w - 20;
        
        // Hitung persentase waktu
        // Asumsi Time Limit order adalah 60 detik (sesuai spec)
        // Jika Order punya method getDuration(), pakai itu. Jika tidak, hardcode 60.
        int maxTime = 60; 
        int timeLeft = order.getTimeLeft();
        double progress = (double) timeLeft / maxTime;

        // Background Bar (Abu)
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x + 10, barY, barWidth, barHeight);

        // Foreground Bar (Hijau -> Merah)
        if (progress > 0.5) g.setColor(Color.GREEN);
        else if (progress > 0.2) g.setColor(Color.ORANGE);
        else g.setColor(Color.RED);

        int fillWidth = (int) (barWidth * progress);
        g.fillRect(x + 10, barY, fillWidth, barHeight);
    }

    @Override
    public void update(Object gameState) {
        repaint(); // Repaint saat model berubah
    }
}