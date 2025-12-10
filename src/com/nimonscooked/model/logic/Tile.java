package com.nimonscooked.model.logic;

import com.nimonscooked.model.items.Item;
import com.nimonscooked.model.stations.Station;

public class Tile {
    private int x, y;
    private boolean isWall;
    private Station station;
    private Item groundItem; // Item yang dilempar/ditaruh di lantai

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        this.isWall = false;
    }

    public void setWall(boolean isWall) { this.isWall = isWall; }
    public boolean isWall() { return isWall; }

    public void setStation(Station station) {
        this.station = station;
        // Station dianggap sebagai 'obstacle' juga (tidak bisa diinjak)
    }
    public Station getStation() { return station; }

    public void setX(int x){ this.x = x; }
    public void setY(int y){ this.y = y; }

    public int getX(){ return x; }
    public int getY(){ return y; }

    public void setGroundItem(Item item) { this.groundItem = item; }
    public Item getGroundItem() { return groundItem; }

    // Helper untuk cek collision gerakan
    public boolean isWalkable() {
        // Tidak bisa jalan jika Tembok ATAU ada Station
        // (Kecuali station tertentu walkable? Spek bilang station tidak bisa diinjak [110])
        return !isWall && station == null;
    }
}