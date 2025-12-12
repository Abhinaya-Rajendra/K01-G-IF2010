package com.nimonscooked.model.items;

import java.util.ArrayList;
import java.util.List;

public class Dish extends Item {
    private List<Ingredient> components;
    public Dish(String name) {
        super(name);
        this.components = new ArrayList<>();
    }

    public void addComponent(Ingredient ingredient) {
        components.add(ingredient);
    }

    public List<Ingredient> getComponents() {
        return components;
    }
    
    @Override
    public String getName() {
        return super.getName();
    }
}