package com.nimonscooked.model.entities;

import com.nimonscooked.core.GameObject;
import com.nimonscooked.model.items.*;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.utils.Direction;

public class Chef extends GameObject {
    private String name;
    private Direction direction;
    private Inventory<Item> inventory;
    
    // Status
    private boolean isBusy = false;
    
    // Bonus Dash
    private boolean isDashing = false;
    private long lastDashTime = 0;
    private final long DASH_COOLDOWN = 2000; 
    
    public Chef(int x, int y) {
        super(x, y);
        this.direction = Direction.DOWN; 
        this.inventory = new Inventory<>();
    }

    // --- MOVEMENT ---
    public void move(Direction dir, Map map) {
        if (isBusy) return;

        this.direction = dir;

        // PERBAIKAN: Gunakan position.getX() / getY()
        int currentX = position.getX();
        int currentY = position.getY();

        int nextX = currentX + dir.getDeltaX();
        int nextY = currentY + dir.getDeltaY();
        
        if (isDashing) {
            nextX += dir.getDeltaX();
            nextY += dir.getDeltaY();
            isDashing = false; 
        }

        if (isValidMove(nextX, nextY, map)) {
            // PERBAIKAN: Gunakan setPosition / position.setX
            this.setPosition(nextX, nextY);
            
            // Cek Auto-Plating (pakai koordinat baru)
            checkAutoPlating(map.getTile(nextX, nextY));
        } else {
            // Fallback dash
            int fallbackX = currentX + dir.getDeltaX();
            int fallbackY = currentY + dir.getDeltaY();
            if (isValidMove(fallbackX, fallbackY, map)) {
                this.setPosition(fallbackX, fallbackY);
                checkAutoPlating(map.getTile(fallbackX, fallbackY));
            }
        }
    }
    
    private boolean isValidMove(int targetX, int targetY, Map map) {
        if (targetX < 0 || targetX >= map.getCols() || targetY < 0 || targetY >= map.getRows()) {
            return false;
        }
        
        Tile tile = map.getTile(targetX, targetY);
        if (!tile.isWalkable()) return false;
        
        for (Chef other : GameModel.getInstance().getChefs()) {
            // PERBAIKAN: Akses posisi chef lain juga lewat getter
            if (other != this && other.getPosition().getX() == targetX && other.getPosition().getY() == targetY) {
                return false;
            }
        }
        
        return true;
    }

    // --- INTERACTION ---
    public void interact(Map map) {
        if (isBusy) {
            setBusy(false);
            return;
        }

        Tile facingTile = getFacingTile(map);
        if (facingTile == null) return;

        if (facingTile.getStation() != null) {
            facingTile.getStation().interact(this);
        } else if (facingTile.getGroundItem() != null) {
            pickUpItem(facingTile);
        } else {
            placeItemOnGround(facingTile);
        }
    }

    private void pickUpItem(Tile tile) {
        Item groundItem = tile.getGroundItem();
        Item heldItem = inventory.getItem();
        
        if (heldItem == null) {
            inventory.setItem(groundItem);
            tile.setGroundItem(null);
        } else if (heldItem instanceof Plate && groundItem instanceof Ingredient) {
            Plate p = (Plate) heldItem;
            if (p.isClean()) {
                p.addIngredient((Ingredient) groundItem);
                tile.setGroundItem(null);
                System.out.println("Auto-Plated from Ground!");
            }
        } else if (heldItem instanceof Ingredient && groundItem instanceof Plate) {
            Plate p = (Plate) groundItem;
            if (p.isClean()) {
                p.addIngredient((Ingredient) heldItem);
                inventory.takeItem();
                System.out.println("Auto-Plated to Ground Plate!");
            }
        }
    }

    private void placeItemOnGround(Tile tile) {
        if (inventory.getItem() != null) {
            tile.setGroundItem(inventory.takeItem());
        }
    }
    
    private void checkAutoPlating(Tile currentTile) {
        // Kosong (Optional logic)
    }

    public void dash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime >= DASH_COOLDOWN) {
            isDashing = true;
            lastDashTime = now;
            System.out.println("DASH!");
        }
    }

    public void throwItem() {
        if (inventory.getItem() == null) return;
        
        if (!(inventory.getItem() instanceof Ingredient)) {
            System.out.println("Cannot throw this item!");
            return;
        }

        Item itemThrown = inventory.takeItem();
        
        // PERBAIKAN: Ambil posisi dari getter
        Projectile proj = new Projectile(position.getX(), position.getY(), this.direction, itemThrown);
        GameModel.getInstance().addProjectile(proj);
        System.out.println("Threw " + itemThrown.getName());
    }

    public Tile getFacingTile(Map map) {
        // PERBAIKAN: Akses posisi via getter
        int tx = position.getX() + direction.getDeltaX();
        int ty = position.getY() + direction.getDeltaY();
        
        // Validasi agar tidak crash array index out of bounds
        if (tx < 0 || tx >= map.getCols() || ty < 0 || ty >= map.getRows()) return null;
        
        return map.getTile(tx, ty);
    }
    
    // Helper pendek untuk koordinat (opsional, biar tidak panjang ngetik position.getX())
    public int getX() { return position.getX(); }
    public int getY() { return position.getY(); }

    public Inventory<Item> getInventory() { return inventory; }
    public Direction getDirection() { return direction; }
    public void setBusy(boolean busy) { this.isBusy = busy; }
    public boolean isBusy() { return isBusy; }
}