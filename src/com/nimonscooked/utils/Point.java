package com.nimonscooked.utils;

public class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters & Setters
    public int getX() { return x; }
    public int getY() { return y; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    // Helper: Mengubah posisi (untuk gerakan)
    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    // Helper: Cek kesamaan posisi
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }
}