package com.nimonscooked.controller;

import com.nimonscooked.core.Command;
import com.nimonscooked.model.entities.Chef;

public class ThrowCommand implements Command {
    private Chef chef;

    public ThrowCommand(Chef chef) {
        this.chef = chef;
    }

    @Override
    public void execute() {
        chef.throwItem();
    }
}