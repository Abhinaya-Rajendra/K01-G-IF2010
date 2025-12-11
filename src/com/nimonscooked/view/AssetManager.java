package com.nimonscooked.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetManager: Singleton untuk load semua gambar dari resources/images/
 * Coba load dari classpath dulu, jika gagal coba dari file system.
 */
public class AssetManager {
    private static AssetManager instance;
    private final Map<String, BufferedImage> images;

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
        System.out.println("🎮 AssetManager: Starting to load images...\n");

        // 1. Environment
        load("wall", "images/wall.png");
        load("floor", "images/floor.png");
        
        // 2. Stations Umum
        load("station_cutting", "images/station_cutting.png");
        load("station_stove", "images/station_stove.png");
        load("station_serving", "images/station_serving.png");
        load("station_assembly", "images/station_assembly.png");
        load("station_plate", "images/station_plate.png");
        load("station_wash", "images/station_wash.png");
        load("station_trash", "images/station_trash.png");

        // 3. Storage Spesifik
        load("storage_PASTA", "images/storage_PASTA.png");
        load("storage_TOMATO", "images/storage_TOMATO.png");
        load("storage_MEAT", "images/storage_MEAT.png");
        load("storage_SHRIMP", "images/storage_SHRIMP.png");
        load("storage_FISH", "images/storage_FISH.png");
        
        // 4. Entities & Items
        load("chef", "images/chef.png");
        load("pot", "images/pot.png");
        load("pan", "images/pan.png");
        load("plate", "images/plate.png");
        load("item_default", "images/item_tomato.png");
        
        // 5. MAIN MENU ASSETS
        System.out.println("\n📋 Loading main menu images...");
        load("main_menu", "images/main_menu.png");
        load("start_game(1)", "images/main_menu/start_game_button(1).png");
        load("start_game(2)", "images/main_menu/start_game_button(2).png");
        load("start_game(3)", "images/main_menu/start_game_button(3).png");
        load("start_random(1)", "images/main_menu/start_random_button(1).png");
        load("start_random(2)", "images/main_menu/start_random_button(2).png");
        load("start_random(3)", "images/main_menu/start_random_button(3).png");
        load("how_to_play(1)", "images/main_menu/how_to_play(1).png");
        load("how_to_play(2)", "images/main_menu/how_to_play(2).png");
        load("how_to_play(3)", "images/main_menu/how_to_play(3).png");
        load("exit(1)", "images/main_menu/exit_button(1).png");
        load("exit(2)", "images/main_menu/exit_button(2).png");
        load("exit(3)", "images/main_menu/exit_button(3).png");
        
        System.out.println("\n✅ AssetManager: Image loading complete!\n");
    }

    /**
     * Load image dari resources folder.
     * Coba multiple approaches:
     * 1. Classpath resource (saat dalam JAR atau IDE)
     * 2. File system dari folder "resources"
     */
    private void load(String key, String path) {
        BufferedImage img = null;
        
        // Approach 1: Try classpath resource dengan "/" prefix
        try (InputStream is = getClass().getResourceAsStream("/" + path)) {
            if (is != null) {
                img = ImageIO.read(is);
                System.out.println("✅ [Classpath] Loaded: " + path);
            }
        } catch (IOException e) {
            System.out.println("⚠️ [Classpath] Failed to read: " + path);
        }
        
        // Approach 2: Jika classpath gagal, coba dari file system
        if (img == null) {
            try {
                File file = new File("resources" + File.separator + path);
                if (file.exists()) {
                    img = ImageIO.read(file);
                    System.out.println("✅ [FileSystem] Loaded: " + file.getAbsolutePath());
                } else {
                    System.out.println("❌ File not found: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("❌ [FileSystem] Error reading: " + path);
                e.printStackTrace();
            }
        }
        
        // Store image (bisa null jika gagal, akan ditangani di getImage)
        if (img != null) {
            images.put(key, img);
        }
    }

    /**
     * Get image by key name
     * Return null jika tidak ditemukan (bukan throw exception)
     */
    public BufferedImage getImage(String name) {
        if (name == null) return null;
        
        BufferedImage img = images.get(name);
        
        if (img == null) {
            System.out.println("⚠️ Image not found in AssetManager: " + name);
        }
        
        return img;
    }

    /**
     * Debugging: Print semua image yang sudah di-load
     */
    public void printLoadedAssets() {
        System.out.println("\n📦 Loaded Assets:");
        images.forEach((key, value) -> {
            if (value != null) {
                System.out.println("  ✓ " + key + " (" + value.getWidth() + "x" + value.getHeight() + ")");
            }
        });
        System.out.println("Total: " + images.size() + " images\n");
    }
}