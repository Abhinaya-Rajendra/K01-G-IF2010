package com.nimonscooked.model.stations;

import com.nimonscooked.core.GameObject;
import com.nimonscooked.model.entities.Chef;

public abstract class Station extends GameObject {

    public Station(int x, int y) {
        super(x, y);
    }

    // Method interaksi wajib (Abstract)
    public abstract void interact(Chef chef);
}