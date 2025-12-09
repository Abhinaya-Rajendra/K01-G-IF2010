package com.nimonscooked.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        // 3. Storage Spesifik (Pastikan nama file PNG sesuai dengan Enum IngredientType)
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
        
        // Item default (tomatcherry/bola merah)
        load("item_default", "images/item_tomato.png");
        
        // Item spesifik (Jika Anda punya gambarnya, aktifkan baris ini)
        // load("item_tomato", "images/item_tomato.png");
        // load("item_pasta", "images/item_pasta.png");
    }

    private void load(String name, String path) {
        // path seharusnya adalah "images/nama_file.png"
        InputStream is = null; 
        
        try {
            // 1. Dapatkan InputStream menggunakan Class Loader
            // Kami menggunakan "/" + path untuk mencari dari root classpath (folder resources kamu)
            is = getClass().getResourceAsStream("/" + path);
            
            if (is != null) {
                // 2. Baca gambar dari InputStream
                BufferedImage img = ImageIO.read(is);
                images.put(name, img);
                System.out.println("✅ Gambar berhasil dimuat: " + path);
            } else {
                // Pesan jika Class Loader tidak menemukan file
                System.out.println("⚠️ Image not found in classpath: " + path);
            }
        } catch (IOException e) {
            System.err.println("Error reading image data for: " + path);
            e.printStackTrace();
        } finally {
            // Selalu tutup InputStream untuk menghindari kebocoran sumber daya
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Abaikan error saat menutup stream
                }
            }
        }
    }

    // --- INI METHOD YANG DICARI OLEH GAMEPANEL ---
    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}