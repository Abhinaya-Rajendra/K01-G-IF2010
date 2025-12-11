package com.nimonscooked.view;

import javax.imageio.ImageIO;
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
        // 1. Environment
        load("wall", "resources/images/wall.png");
        load("floor", "resources/images/floor.png");
        
        // 2. Stations Umum
        load("station_cutting", "resources/images/station_cutting.png");
        load("station_stove", "resources/images/station_stove.png");
        load("station_serving", "resources/images/station_serving.png");
        load("station_assembly", "resources/images/station_assembly.png");
        load("station_plate", "resources/images/station_plate.png");
        load("station_wash", "resources/images/station_wash.png");
        load("station_trash", "resources/images/station_trash.png");

        // 3. Storage Spesifik (Pastikan nama file PNG sesuai dengan Enum IngredientType)
        load("storage_PASTA", "resources/images/storage_PASTA.png");
        load("storage_TOMATO", "resources/images/storage_TOMATO.png");
        load("storage_MEAT", "resources/images/storage_MEAT.png");
        load("storage_SHRIMP", "resources/images/storage_SHRIMP.png");
        load("storage_FISH", "resources/images/storage_FISH.png");
        
        // 4. Entities & Items
        load("chef", "resources/images/chef.png");
        load("pot", "resources/images/pot.png");
        load("pan", "resources/images/pan.png");
        load("plate", "resources/images/plate.png");
        
        // Item default (tomatcherry/bola merah)
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
}