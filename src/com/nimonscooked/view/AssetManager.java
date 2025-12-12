package com.nimonscooked.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// import java.io.File; // <-- HAPUS INI
import java.io.IOException;
import java.io.InputStream; // <-- TAMBAHKAN INI
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static AssetManager instance;
    private Map<String, BufferedImage> images;

    private AssetManager() {
        images = new HashMap<>();
        loadAllImages();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    private void loadAllImages() {
        // --- MEMUAT ASET LAMA ---
        load("wall", "resources/images/wall.png");
        load("floor", "resources/images/floor.png");
        load("station_cutting", "resources/images/station_cutting.png");
        load("station_stove", "resources/images/station_stove.png");
        load("station_serving", "resources/images/station_serving.png");
        load("station_assembly", "resources/images/station_assembly.png");
        load("station_plate", "resources/images/station_plate.png");
        load("station_wash", "resources/images/station_wash.png");
        load("station_trash", "resources/images/station_trash.png");

        load("storage_PASTA", "resources/images/storage_PASTA.png");
        load("storage_TOMATO", "resources/images/storage_TOMATO.png");
        load("storage_MEAT", "resources/images/storage_MEAT.png");
        load("storage_SHRIMP", "resources/images/storage_SHRIMP.png");
        load("storage_FISH", "resources/images/storage_FISH.png");
        
        load("chef", "resources/images/chef.png");
        load("pot", "resources/images/pot.png");
        load("pan", "resources/images/pan.png");
        load("plate", "resources/images/plate.png");
        load("item_default", "resources/images/item_tomato.png");
        
        // Item spesifik (Jika Anda punya gambarnya, aktifkan baris ini)
        // load("item_tomato", "resources/images/item_tomato.png");
        // load("item_pasta", "resources/images/item_pasta.png");
        // --- TAMBAHAN BARU UNTUK UI ---
        load("ui_coin", "resources/images/ui_coin.png");   // Icon Koin
        load("ui_timer", "resources/images/ui_timer.png"); // Icon Jam
        load("ui_card", "resources/images/ui_card.png");   // Background kartu order (opsional)

        // Ikon Bahan Kecil (Untuk di kartu order)
        load("icon_PASTA", "resources/images/icon_pasta.png");
        load("icon_TOMATO", "resources/images/icon_tomato.png");
        load("icon_MEAT", "resources/images/icon_meat.png");

        load("main_menu", "images/main_menu/bgMenu.png");
        load("bg", "images/main_menu/bg.png");
        // ... dst
        
        // Ikon Dish Hasil Jadi (Untuk di kartu order)
        load("dish_Pasta Marinara", "resources/images/dish_marinara.png");
        // ... dst sesuaikan nama resep

        load("start", "images/main_menu/start_game(1).png");
        load("help", "images/main_menu/how_to_play(1).png");
        load("exit", "images/main_menu/exit(1).png");

        load("startHover", "images/main_menu/start_game(2).png");
        load("helpHover", "images/main_menu/how_to_play(2).png");
        load("exitHover", "images/main_menu/exit(2).png");

        load("startPressed", "images/main_menu/start_game(3).png");
        load("helpPressed", "images/main_menu/how_to_play(3).png");
        load("exitPressed", "images/main_menu/exit(3).png");
        load("ui_coin", "images/ui_coin.png");      // [ASSET: ui_coin.png]
        load("ui_timer", "images/ui_timer.png");
        load("ui_clothespin", "images/ui_clothespin.png"); // [ASSET: ui_clothespin.png]
        load("ui_icon_o", "images/ui_icon_o.png");          // [ASSET: ui_icon_o.png]
        load("ui_icon_x", "images/ui_icon_x.png");
        load("pasta_marinara", "images/dish_marinara.png"); 
        load("pasta_bolognese", "images/dish_bolognese.png"); 
        load("pasta_frutti_di_mare", "images/dish_frutti_di_mare.png"); 

    }

    private void load(String name, String path) {
        InputStream is = null;
        try {
            // Menggunakan Class Loader untuk memuat sumber daya dari classpath
            // path sekarang adalah "images/..."
            is = getClass().getResourceAsStream("/" + path);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                images.put(name, img);
                System.out.println("✅ Loaded: " + path);
            } else {
                // Beri tahu jika Class Loader tidak menemukan
                System.out.println("⚠️ Image not found in classpath: " + path);
            }
        } catch (IOException e) {
            System.err.println("Error reading image data for: " + path);
            e.printStackTrace();
        } finally {
            // Selalu tutup InputStream
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }


    // --- INI METHOD YANG DICARI OLEH GAMEPANEL ---
    public BufferedImage getImage(String name) {
        return images.get(name);
    }

    public JButton createButton(String defaultAsset, 
                                 String hoverAsset, 
                                 String pressedAsset, 
                                 Runnable action,
                                 int targetWidth, 
                                 int targetHeight) {
    
    BufferedImage defaultImg = AssetManager.getInstance().getImage(defaultAsset);
    BufferedImage hoverImg = AssetManager.getInstance().getImage(hoverAsset);
    BufferedImage pressedImg = AssetManager.getInstance().getImage(pressedAsset);
    
    if (defaultImg == null) {
        // ... (Kode fallback tetap ada)
        return new JButton(defaultAsset); 
    }
    
    // --- LANGKAH PENTING: SKALA SEMUA GAMBAR SECARA TERPISAH ---

    // 1. Skala Gambar Default dan buat Icon
    Image scaledDefault = defaultImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    ImageIcon defaultIcon = new ImageIcon(scaledDefault);
    
    JButton btn = new JButton(defaultIcon); 
    
    // 2. Skala Gambar Hover dan atur RolloverIcon
    if (hoverImg != null) {
        Image scaledHover = hoverImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        ImageIcon hoverIcon = new ImageIcon(scaledHover);
        btn.setRolloverIcon(hoverIcon); 
    }
    
    // 3. Skala Gambar Pressed dan atur PressedIcon
    if (pressedImg != null) {
        Image scaledPressed = pressedImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        ImageIcon pressedIcon = new ImageIcon(scaledPressed);
        btn.setPressedIcon(pressedIcon); 
    }
    
    // --- PENGATURAN UKURAN KETAT ---
    
    btn.setBorderPainted(false); 
    btn.setContentAreaFilled(false); 
    btn.setFocusPainted(false); 
    
    
    Dimension fixedSize = new Dimension(targetWidth, targetHeight);
    btn.setPreferredSize(fixedSize); 
    btn.setMinimumSize(fixedSize); 
    btn.setMaximumSize(fixedSize); 

    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.addActionListener(e -> action.run());
    
    return btn;
}
}