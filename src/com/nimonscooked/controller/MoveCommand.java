package com.nimonscooked.controller;

import com.nimonscooked.core.Command;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.utils.Direction;

public class MoveCommand implements Command {
    private Chef chef;
    private Direction direction;

    public MoveCommand(Chef chef, Direction direction) {
        this.chef = chef;
        this.direction = direction;
    }

    @Override
    public void execute() {
        if (chef == null) return;
        
        // Ambil map dari singleton GameModel
        GameModel model = GameModel.getInstance();
        
        // Perintahkan chef bergerak
        // chef.move(direction, model.getMap());
        
        // Beritahu GUI untuk update layar (Repaint)
        model.notifyObservers();
    }
}