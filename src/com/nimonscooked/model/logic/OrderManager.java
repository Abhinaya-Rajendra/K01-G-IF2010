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
    
    private final int MAX_ORDERS = 4; // Maksimal 3 order aktif di sidebar

    public OrderManager() {
        this.activeOrders = new LinkedList<>();
        this.availableRecipes = new ArrayList<>();
        this.random = new Random();
        
        initializeRecipes();
        
        while (activeOrders.size() < MAX_ORDERS) {
            generateNewOrder();
        }
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
        
        // 3. Pasta Frutti di Mare (Baru)
        Recipe r3 = new Recipe("Pasta Frutti di Mare");
        r3.addIngredient(IngredientType.PASTA);
        r3.addIngredient(IngredientType.SHRIMP);
        r3.addIngredient(IngredientType.FISH);
        availableRecipes.add(r3);
    }

    private void generateNewOrder() {
        if (availableRecipes.isEmpty()) return;
        
        Recipe randomRecipe = availableRecipes.get(random.nextInt(availableRecipes.size()));
        // Durasi acak antara 40 - 60 detik
        int duration = 100 + random.nextInt(21); 
        
        Order newOrder = new Order(randomRecipe, duration);
        activeOrders.add(newOrder);
        System.out.println("NEW ORDER SPAWNED: " + randomRecipe.getName());
    }

    public List<Order> getActiveOrders() {
        return activeOrders;
    }

    public void tick() {
        List<Order> expiredOrders = new ArrayList<>();

        for (Order o : activeOrders) {
            o.tick();
            if (o.isExpired()) {
                expiredOrders.add(o);
            }
        }

        for (Order expired : expiredOrders) {
            System.out.println("ORDER EXPIRED: " + expired.getRecipe().getName());
            activeOrders.remove(expired);
            
            GameModel.getInstance().addScore(-50); 
            GameModel.getInstance().addFailedOrder(); 
        }
        
        while (activeOrders.size() < MAX_ORDERS) {
            generateNewOrder();
        }
    }

    public boolean validateService(KitchenUtensil dish) {
        if (activeOrders.isEmpty()) return false;

        Iterator<Order> it = activeOrders.iterator();
        while (it.hasNext()) {
            Order order = it.next();
            // Cek apakah isi piring sesuai resep order
            if (order.getRecipe().matches(dish)) {
                System.out.println("Order Completed: " + order.getRecipe().getName());
                
                GameModel.getInstance().addScore(100);

                it.remove(); 
                
                while (activeOrders.size() < MAX_ORDERS) {
                    generateNewOrder();
                }
                
                return true; 
            }
        }
        return false; 
    }
}