package com.nimonscooked.model.items;

public abstract class Item {
    protected String name;

    // --- FIX ERROR 1: Constructor ini WAJIB ADA ---
    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}