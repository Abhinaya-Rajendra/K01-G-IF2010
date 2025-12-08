package com.nimonscooked.model.entities;

public class Order {
    private Recipe recipe;
    private int timeLeft; // Timer mundur (detik)
    private int maxTime;

    public Order(Recipe recipe, int duration) {
        this.recipe = recipe;
        this.maxTime = duration;
        this.timeLeft = duration;
    }

    public Recipe getRecipe() { return recipe; }
    
    public int getTimeLeft() { return timeLeft; }
    
    // Method untuk mengurangi waktu (dipanggil setiap detik nanti)
    public void tick() {
        if (timeLeft > 0) timeLeft--;
    }
    
    public boolean isExpired() {
        return timeLeft <= 0;
    }
    
    public int getReward() {
        return 100; // Skor standar
    }
}