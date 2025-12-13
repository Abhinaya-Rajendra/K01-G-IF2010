package com.nimonscooked.model.logic;

import com.nimonscooked.model.entities.Order;
import com.nimonscooked.model.entities.Recipe;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.utils.IngredientType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class OrderManager {
    private List<Order> activeOrders;
    private List<Recipe> availableRecipes;
    private Random random;
    
    // --- KONFIGURASI ORDER ---
    private final int MAX_ORDERS = 4; // Maksimal order yang tampil
    private final int ORDER_SPAWN_INTERVAL = 15; // Order baru datang setiap 15 detik
    private int spawnTimer = 0; // Timer internal untuk spawn order

    public OrderManager() {
        this.activeOrders = new LinkedList<>();
        this.availableRecipes = new ArrayList<>();
        this.random = new Random();
        
        initializeRecipes();
        
        // REVISI: Mulai hanya dengan SATU order
        generateNewOrder(); 
    }

    private void initializeRecipes() {
        // 1. Pasta Marinara
        Recipe r1 = new Recipe("Pasta Marinara");
        r1.addIngredient(IngredientType.PASTA);
        r1.addIngredient(IngredientType.TOMATO);
        availableRecipes.add(r1);

        // 2. Pasta Bolognese
        Recipe r2 = new Recipe("Pasta Bolognese");
        r2.addIngredient(IngredientType.PASTA);
        r2.addIngredient(IngredientType.MEAT);
        availableRecipes.add(r2);
        
        // 3. Pasta Frutti di Mare
        Recipe r3 = new Recipe("Pasta Frutti di Mare");
        r3.addIngredient(IngredientType.PASTA);
        r3.addIngredient(IngredientType.SHRIMP);
        r3.addIngredient(IngredientType.FISH);
        availableRecipes.add(r3);
    }

    private void generateNewOrder() {
        // Jangan generate jika resep kosong atau slot penuh
        if (availableRecipes.isEmpty() || activeOrders.size() >= MAX_ORDERS) return;
        
        Recipe randomRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        // Durasi tetap 100 detik (bisa dirandomize jika perlu)
        int duration = 100; 
        
        Order newOrder = new Order(randomRecipe, duration);
        activeOrders.add(newOrder);
        System.out.println("NEW ORDER SPAWNED: " + randomRecipe.getName());
    }

    public List<Order> getActiveOrders() {
        return activeOrders;
    }

    // Dipanggil setiap detik oleh GameModel
    public void tick() {
        // 1. Update Timer Order yang sedang aktif
        List<Order> expiredOrders = new ArrayList<>();
        for (Order o : activeOrders) {
            o.tick();
            if (o.isExpired()) {
                expiredOrders.add(o);
            }
        }

        // 2. Hapus Order Kadaluarsa
        for (Order expired : expiredOrders) {
            System.out.println("ORDER EXPIRED: " + expired.getRecipe().getName());
            activeOrders.remove(expired);
            GameModel.getInstance().addScore(-50); 
            GameModel.getInstance().addFailedOrder(); 
        }
        
        // 3. LOGIKA SPAWN BERDASARKAN WAKTU
        // Tambahkan timer spawn
        spawnTimer++;
        if (spawnTimer >= ORDER_SPAWN_INTERVAL) {
            // Coba spawn order baru setiap interval
            if (activeOrders.size() < MAX_ORDERS) {
                generateNewOrder();
                spawnTimer = 0; // Reset timer setelah sukses spawn
            }
            // Jika slot penuh, timer terus jalan atau bisa di-reset 
            // (di sini kita biarkan jalan terus, jadi begitu ada slot kosong nanti dia akan menunggu interval lagi
            // atau reset ke 0 agar intervalnya tetap teratur)
            else {
                 spawnTimer = 0; // Reset agar interval tetap konsisten
            }
        }
        
        // REVISI: HAPUS loop 'while (activeOrders.size() < MAX_ORDERS)' yang lama
        // agar order tidak langsung penuh seketika.
    }

    public boolean validateService(KitchenUtensil dish) {
        if (activeOrders.isEmpty()) return false;

        // FIFO Check: Iterasi dari index 0 (terlama) ke terakhir
        Iterator<Order> it = activeOrders.iterator();
        while (it.hasNext()) {
            Order order = it.next();
            
            // Cek kecocokan resep
            if (order.getRecipe().matches(dish)) {
                System.out.println("Order Completed: " + order.getRecipe().getName());
                
                GameModel.getInstance().addScore(100);

                // Hapus order ini (yang pertama ketemu / paling lama)
                it.remove(); 
                
                // REVISI: Spawn 1 order baru sebagai pengganti (Instant Spawn on Complete)
                generateNewOrder();
                
                return true; // Selesai, jangan cek order lain
            }
        }
        return false; 
    }

    public List<Recipe> getAllRecipes() {
        return availableRecipes;
    }
}