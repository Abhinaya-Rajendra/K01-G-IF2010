package com.nimonscooked.controller;

import com.nimonscooked.core.Command;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.logic.GameModel;

public class DashCommand implements Command {
    private Chef chef;

    public DashCommand(Chef chef) {
        this.chef = chef;
    }

    @Override
    public void execute() {
        // PERBAIKAN: Hapus parameter model.getMap()
        chef.dash(); 
        
        // Update view
        GameModel.getInstance().notifyObservers();
    }
}