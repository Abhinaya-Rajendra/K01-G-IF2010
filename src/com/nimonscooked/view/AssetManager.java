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
        
        // load("icon_PASTA", "resources/images/icon_pasta.png"); // Non-aktifkan jika belum ada aset
        // load("icon_TOMATO", "resources/images/icon_tomato.png");
        // load("icon_MEAT", "resources/images/icon_meat.png");
        // load("dish_Pasta Marinara", "resources/images/dish_marinara.png"); 
        
        // --- TAMBAHAN ASET UI BARU ---
        // Aset HUD (Koin & Jam)
        load("ui_coin", "resources/images/ui_coin.png");      // [ASSET: ui_coin.png]
        load("ui_timer", "resources/images/ui_timer.png");    // [ASSET: ui_timer.png]

        // Aset Stage Select (Penjepit, Tombol O & X)
        load("ui_clothespin", "resources/images/ui_clothespin.png"); // [ASSET: ui_clothespin.png]
        load("ui_icon_o", "resources/images/ui_icon_o.png");          // [ASSET: ui_icon_o.png]
        load("ui_icon_x", "resources/images/ui_icon_x.png");          // [ASSET: ui_icon_x.png]
        
        // Aset Dish (Pastikan nama resep sudah diubah menjadi underscore dan lowercase di GamePanel)
        load("pasta_marinara", "resources/images/dish_marinara.png"); 
        load("pasta_bolognese", "resources/images/dish_bolognese.png"); 
        load("pasta_frutti_di_mare", "resources/images/dish_frutti_di_mare.png"); 
    }

    private void load(String name, String path) {
        try {
            // MEMPERTAHANKAN METODE new File() UNTUK STABILITAS LOKAL
            File file = new File(path);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(name, img);
            } else {
                // Memberi tahu jika file tidak ditemukan
                System.out.println("⚠️ Image not found (File System): " + path + " (Will use fallback color)");
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + path);
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}