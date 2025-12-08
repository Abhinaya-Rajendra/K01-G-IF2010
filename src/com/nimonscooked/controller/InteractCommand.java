package com.nimonscooked.controller;

import com.nimonscooked.core.Command;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.logic.GameModel;

public class InteractCommand implements Command {
    private Chef chef;

    public InteractCommand(Chef chef) {
        this.chef = chef;
    }

    @Override
    public void execute() {
        GameModel model = GameModel.getInstance();
        // Panggil interact dengan map
        chef.interact(model.getMap());
        model.notifyObservers(); // Update GUI
    }
}