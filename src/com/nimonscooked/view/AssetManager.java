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
        // Daftar tipe ingredient yang digunakan di game
        String[] ingredientTypes = {"pasta", "tomato", "meat", "shrimp", "fish"};
        
        // --- ASSETS SPRINT 1: CORE ITEMS & CHEF ACTIONS ---
        
        // 1. STATIONS (Keys dipertahankan, asumsi path sudah benar)
        load("wall", "resources/images/wall.png");
        load("floor", "resources/images/floor.png");
        load("station_cutting", "resources/images/station_cutting.png");
        load("station_stove", "resources/images/station_stove.png");
        load("station_serving", "resources/images/station_serving.png");
        load("station_assembly", "resources/images/station_assembly.png");
        load("station_plate", "resources/images/station_plate.png");
        load("station_wash", "resources/images/station_wash.png");
        load("station_trash", "resources/images/station_trash.png");

        // 2. INGREDIENT STORAGE (Menggunakan loop & lowercase key)
        for (String type : ingredientTypes) {
             // Menggunakan key lowercase: storage_pasta, storage_tomato, dll.
             load("storage_" + type, "resources/images/storage_" + type.toUpperCase() + ".png"); 
        }

        // 3. CHEF & UTENSILS (Standardisasi Key)
        load("chef_default", "resources/images/chef.png"); // Key default untuk Chef
        load("pot", "resources/images/pot.png");
        load("pan", "resources/images/pan.png");
        
        // Plate (Dipisahkan clean/dirty)
        load("plate_clean", "resources/images/plate_clean.png"); 
        load("plate_dirty", "resources/images/plate_dirty.png"); 
        load("plate", "resources/images/plate_clean.png"); // Fallback key lama
        
        // Item default
        load("item_default", "resources/images/item_default.png"); 


        // 4. INGREDIENT STATES (Keys: item_{tipe}_{status})
        String[] states = {"raw", "chopped", "cooked", "burned"};
        for (String type : ingredientTypes) {
             for (String state : states) {
                 // Asumsi nama file: resources/images/item_pasta_raw.png, item_pasta_chopped.png, dst.
                 load("item_" + type + "_" + state, "resources/images/item_" + type + "_" + state + ".png");
             }
        }
        
        // --- ASSETS SPRINT 3 (UI & DISHES) ---
        // Aset HUD
        load("ui_coin", "resources/images/ui_coin.png"); 
        load("ui_timer", "resources/images/ui_timer.png"); 

        // Aset Stage Select 
        load("ui_clothespin", "resources/images/ui_clothespin.png"); 
        load("ui_icon_o", "resources/images/ui_icon_o.png"); 
        load("ui_icon_x", "resources/images/ui_icon_x.png"); 
        
        // Aset Dish (Disesuaikan dengan format penamaan lowercase_underscore)
        load("pasta_marinara", "resources/images/dish_pasta_marinara.png"); 
        load("pasta_bolognese", "resources/images/dish_pasta_bolognese.png"); 
        load("pasta_frutti_di_mare", "resources/images/dish_pasta_frutti_di_mare.png"); 

        // Tambahkan di dalam private void loadAllImages()

        // --- SPRINT 2: CHEF MOVEMENT & ACTIONS ---
        load("chef_up", "resources/images/chef_up.png");
        load("chef_down", "resources/images/chef_down.png");
        load("chef_left", "resources/images/chef_left.png");
        load("chef_right", "resources/images/chef_right.png");

        // Animasi 2 Frame (Frame 0 & 1)
        load("chef_chopping_0", "resources/images/chef_chopping_1.png"); // Chef memotong Frame 1
        load("chef_chopping_1", "resources/images/chef_chopping_2.png"); // Chef memotong Frame 2
        load("chef_washing_0", "resources/images/chef_washing_1.png"); // Chef mencuci Frame 1
        load("chef_washing_1", "resources/images/chef_washing_2.png"); // Chef mencuci Frame 2

        // --- SPRINT 2: UTENSIL VISUAL ---
        // Utensil Cooking Visual (Satu aset untuk visual memasak)
        load("pot_cooking", "resources/images/pot_cooking.png"); 
        load("pan_cooking", "resources/images/pan_cooking.png"); 

        // --- SPRINT 2: PROJECTILE ---
        load("projectile_shadow", "resources/images/projectile_shadow.png");
    }

    private void load(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(name, img);
            } else {
                System.out.println("⚠️ Image not found: " + path + " (Key: " + name + "). Will use fallback color.");
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