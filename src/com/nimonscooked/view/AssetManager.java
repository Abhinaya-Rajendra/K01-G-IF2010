package com.nimonscooked.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        // ... dst
        
        // Ikon Dish Hasil Jadi (Untuk di kartu order)
        load("dish_Pasta Marinara", "resources/images/dish_marinara.png");
        // ... dst sesuaikan nama resep
    }

    private void load(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(name, img);
            } else {
                // Jangan error, cukup print info agar kita tahu gambar mana yang hilang
                System.out.println("⚠️ Image not found: " + path + " (Will use fallback color)");
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
        }
    }

    // --- INI METHOD YANG DICARI OLEH GAMEPANEL ---
    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}