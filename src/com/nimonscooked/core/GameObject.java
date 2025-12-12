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

    // FIX AKHIR: Mengambil nilai X dari objek 'position'
    public int getGridX() {
        return this.position.getX(); 
    }

    // FIX AKHIR: Mengambil nilai Y dari objek 'position'
    public int getGridY() {
        return this.position.getY(); 
    }
}