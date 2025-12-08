package com.nimonscooked.core;

import com.nimonscooked.utils.Point;

public abstract class GameObject {
    protected Point position;

    public GameObject(int x, int y) {
        this.position = new Point(x, y);
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        this.position.setX(x);
        this.position.setY(y);
    }
}