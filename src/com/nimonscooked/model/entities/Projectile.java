package com.nimonscooked.model.entities;

import com.nimonscooked.core.GameObject;
import com.nimonscooked.model.items.Item;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.utils.Direction;

public class Projectile extends GameObject {
    private Direction direction;
    private Item item;
    private int distanceTraveled = 0;
    private final int MAX_DISTANCE = 4;
    private boolean isActive = true;

    public Projectile(int startX, int startY, Direction dir, Item item) {
        super(startX, startY);
        this.direction = dir;
        this.item = item;
    }

    public void updatePosition(Map map) {
        if (!isActive) return;

        // PERBAIKAN: Gunakan position.getX()/getY()
        int currentX = position.getX();
        int currentY = position.getY();

        int nextX = currentX + direction.getDeltaX();
        int nextY = currentY + direction.getDeltaY();
        
        // 1. Cek Validasi Map Boundary
        if (nextX < 0 || nextX >= map.getCols() || nextY < 0 || nextY >= map.getRows()) {
            landOnGround(map.getTile(currentX, currentY));
            return;
        }

        Tile nextTile = map.getTile(nextX, nextY);

        // 2. Cek Collision dengan Tembok/Station
        if (nextTile.isWall() || nextTile.getStation() != null) {
            landOnGround(map.getTile(currentX, currentY));
            return;
        }
        
        // 3. Cek Collision dengan Chef Lain (CATCH)
        for (Chef c : GameModel.getInstance().getChefs()) {
            // Gunakan c.getPosition().getX() karena Chef pakai GameObject yang sama
            if (c.getPosition().getX() == nextX && c.getPosition().getY() == nextY) {
                if (c.getInventory().getItem() == null) {
                    c.getInventory().setItem(item);
                    System.out.println("CATCH! Chef caught " + item.getName());
                    isActive = false;
                    return;
                }
            }
        }

        // Maju langkah (Gunakan setter)
        this.setPosition(nextX, nextY);
        distanceTraveled++;

        // 4. Cek Jarak Maksimal
        if (distanceTraveled >= MAX_DISTANCE) {
            landOnGround(nextTile);
        }
    }

    private void landOnGround(Tile tile) {
        isActive = false;
        if (tile != null && tile.getGroundItem() == null && tile.getStation() == null && !tile.isWall()) {
            tile.setGroundItem(item);
            System.out.println("Item landed on ground.");
        } else {
            System.out.println("Item lost (nowhere to land).");
        }
    }

    public boolean isActive() { return isActive; }
    public Item getItem() { return item; }
}