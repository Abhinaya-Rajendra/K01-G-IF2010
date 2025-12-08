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
    
    // Konstanta jumlah maksimal order
    private final int MAX_ORDERS = 3; 

    public OrderManager() {
        this.activeOrders = new LinkedList<>();
        this.availableRecipes = new ArrayList<>();
        this.random = new Random();
        
        initializeRecipes();
        
        // ISI PENUH ANTRIAN DI AWAL (3 ORDER)
        while (activeOrders.size() < MAX_ORDERS) {
            generateNewOrder();
        }
    }

    // Definisi Resep Map Type B (Pasta)
    private void initializeRecipes() {
        // Resep 1: Pasta Marinara (Pasta + Tomato)
        Recipe r1 = new Recipe("Pasta Marinara");
        r1.addIngredient(IngredientType.PASTA);
        r1.addIngredient(IngredientType.TOMATO);
        availableRecipes.add(r1);

        // Resep 2: Pasta Bolognese (Pasta + Meat)
        Recipe r2 = new Recipe("Pasta Bolognese");
        r2.addIngredient(IngredientType.PASTA);
        r2.addIngredient(IngredientType.MEAT);
        availableRecipes.add(r2);
    }

    // Method untuk membuat 1 order baru
    private void generateNewOrder() {
        if (availableRecipes.isEmpty()) return;
        
        Recipe randomRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        
        // Durasi order acak antara 40 - 60 detik
        int duration = 40 + random.nextInt(21); 
        
        Order newOrder = new Order(randomRecipe, duration);
        activeOrders.add(newOrder);
        System.out.println("NEW ORDER SPAWNED: " + randomRecipe.getName());
    }

    public List<Order> getActiveOrders() {
        return activeOrders;
    }

    // Update Timer (Dipanggil dari GameModel setiap detik)
    public void tick() {
        List<Order> expiredOrders = new ArrayList<>();

        // 1. Kurangi waktu semua order
        for (Order o : activeOrders) {
            o.tick();
            if (o.isExpired()) {
                expiredOrders.add(o);
            }
        }

        // 2. Hapus yang expired & Beri Penalti
        for (Order expired : expiredOrders) {
            System.out.println("ORDER EXPIRED: " + expired.getRecipe().getName());
            activeOrders.remove(expired);
            // Singleton GameModel dipanggil di sini untuk update skor
            GameModel.getInstance().addScore(-50); 
        }
        
        // 3. REFILL: Jika order kurang dari 3, tambah baru!
        while (activeOrders.size() < MAX_ORDERS) {
            generateNewOrder();
        }
    }

    // Validasi saat menyajikan makanan
    public boolean validateService(KitchenUtensil dish) {
        if (activeOrders.isEmpty()) return false;

        // Cek apakah dish cocok dengan SALAH SATU order yang ada
        // Kita pakai Iterator agar aman menghapus saat looping (kalau perlu)
        Iterator<Order> it = activeOrders.iterator();
        while (it.hasNext()) {
            Order order = it.next();
            if (order.getRecipe().matches(dish)) {
                System.out.println("Order Completed: " + order.getRecipe().getName());
                
                // Hapus order yang spesifik ini dari list
                it.remove(); 
                
                // Langsung refill agar tetap 3
                while (activeOrders.size() < MAX_ORDERS) {
                    generateNewOrder();
                }
                
                return true; // Sukses
            }
        }
        return false; // Gagal
    }
}