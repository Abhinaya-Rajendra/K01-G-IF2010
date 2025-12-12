package com.nimonscooked.model.entities;

import com.nimonscooked.core.GameObject;
import com.nimonscooked.model.items.*;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.utils.Direction;

import java.util.List;

public class Chef extends GameObject {
    
    private Inventory<Item> inventory;
    
    // Physics
    private double worldX, worldY; 
    private final double MOVE_SPEED = 0.15; 
    private final double DASH_MULTIPLIER = 2.5;
    
    // Input
    private boolean inputUp, inputDown, inputLeft, inputRight;
    
    // Direction Vector
    private double lastDirX = 0;
    private double lastDirY = 1.0; 

    // Collision
    private final double COLLISION_SIZE = 0.6; 

    // Status
    private boolean isBusy = false;
    private boolean isDashing = false;
    private long lastDashTime = 0;
    private int dashDurationTicks = 0;
    private final long DASH_COOLDOWN = 1000; 

    public Chef(int startGridX, int startGridY) {
        super(startGridX, startGridY);
        this.worldX = startGridX;
        this.worldY = startGridY;
        this.inventory = new Inventory<>();
    }

    public void setMovementInput(boolean up, boolean down, boolean left, boolean right) {
        this.inputUp = up;
        this.inputDown = down;
        this.inputLeft = left;
        this.inputRight = right;
    }

    public void stopMovement() {
        inputUp = inputDown = inputLeft = inputRight = false;
    }

    public void update(Map map) {
        if (isBusy) return;

        double dx = 0;
        double dy = 0;

        if (inputUp) dy -= 1;
        if (inputDown) dy += 1;
        if (inputLeft) dx -= 1;
        if (inputRight) dx += 1;

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
            this.lastDirX = dx;
            this.lastDirY = dy;
        }

        double speed = MOVE_SPEED;
        if (dashDurationTicks > 0) {
            speed *= DASH_MULTIPLIER;
            dashDurationTicks--;
            isDashing = true;
        } else {
            isDashing = false;
        }

        dx *= speed;
        dy *= speed;

        // --- PHYSICAL MOVEMENT & COLLISION ---
        
        // Move X
        if (dx != 0) {
            double nextX = worldX + dx;
            if (!checkWallCollision(nextX, worldY, map) && !checkChefCollision(nextX, worldY)) {
                worldX = nextX;
            }
        }

        // Move Y
        if (dy != 0) {
            double nextY = worldY + dy;
            if (!checkWallCollision(worldX, nextY, map) && !checkChefCollision(worldX, nextY)) {
                worldY = nextY;
            }
        }

        this.setPosition((int) Math.round(worldX), (int) Math.round(worldY));
    }

    // Cek Tabrakan Tembok
    private boolean checkWallCollision(double targetX, double targetY, Map map) {
        double left = targetX + (1 - COLLISION_SIZE) / 2;
        double right = left + COLLISION_SIZE;
        double top = targetY + (1 - COLLISION_SIZE) / 2;
        double bottom = top + COLLISION_SIZE;

        return isWall(left, top, map) || isWall(right, top, map) || 
               isWall(left, bottom, map) || isWall(right, bottom, map);
    }
    
    // --- BARU: Cek Tabrakan Antar Chef ---
    private boolean checkChefCollision(double targetX, double targetY) {
        List<Chef> chefs = GameModel.getInstance().getChefs();
        for (Chef other : chefs) {
            if (other == this) continue; // Jangan cek diri sendiri
            
            // Jarak Euclidean antar pusat chef
            double dx = targetX - other.getWorldX();
            double dy = targetY - other.getWorldY();
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            // Jika jarak kurang dari collision size (0.6), anggap tabrakan (Body Block)
            if (dist < COLLISION_SIZE) {
                return true; 
            }
        }
        return false;
    }
    
    private boolean isWall(double x, double y, Map map) {
        int tileX = (int) x;
        int tileY = (int) y;
        if (tileX < 0 || tileX >= map.getCols() || tileY < 0 || tileY >= map.getRows()) return true;
        Tile t = map.getTile(tileX, tileY);
        return !t.isWalkable();
    }

    public void interact(Map map) {
        if (isBusy) { setBusy(false); return; }

        Tile facingTile = getFacingTile(map);
        if (facingTile == null) return;

        if (facingTile.getStation() != null) {
            facingTile.getStation().interact(this);
        } else {
            Item groundItem = facingTile.getGroundItem();
            Item heldItem = inventory.getItem();

            if (heldItem == null && groundItem != null) {
                inventory.setItem(groundItem);
                facingTile.setGroundItem(null);
            } else if (heldItem != null) {
                handleDropOrPlate(heldItem, groundItem, facingTile);
            }
        }
    }
    
    private void handleDropOrPlate(Item held, Item ground, Tile tile) {
        if (held instanceof Plate && ground instanceof Ingredient) {
            if (((Plate) held).isClean()) {
                ((Plate) held).addIngredient((Ingredient) ground);
                tile.setGroundItem(null);
            }
        } else if (held instanceof Ingredient && ground instanceof Plate) {
            if (((Plate) ground).isClean()) {
                ((Plate) ground).addIngredient((Ingredient) held);
                inventory.takeItem();
            }
        } else if (ground == null) {
            tile.setGroundItem(inventory.takeItem());
        }
    }

    public Tile getFacingTile(Map map) {
        double interactX = worldX + (lastDirX * 0.8) + 0.5; 
        double interactY = worldY + (lastDirY * 0.8) + 0.5;
        int tx = (int) interactX;
        int ty = (int) interactY;
        if (tx < 0 || tx >= map.getCols() || ty < 0 || ty >= map.getRows()) return null;
        return map.getTile(tx, ty);
    }

    public void dash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime >= DASH_COOLDOWN) {
            dashDurationTicks = 5; 
            lastDashTime = now;
        }
    }

    public void throwItem() {
        if (inventory.getItem() == null) return;
        // Hanya Ingredient yang bisa dilempar (Plate pecah)
        if (!(inventory.getItem() instanceof Ingredient)) return;
        
        Item itemThrown = inventory.takeItem();
        
        // Konversi Vector halus ke Direction terdekat (untuk kompatibilitas constructor)
        Direction dir = Direction.DOWN;
        if (Math.abs(lastDirX) > Math.abs(lastDirY)) {
            dir = (lastDirX > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            dir = (lastDirY > 0) ? Direction.DOWN : Direction.UP;
        }

        // Spawn projectile sedikit di depan agar tidak kena hitbox sendiri
        int spawnX = (int) Math.round(worldX); 
        int spawnY = (int) Math.round(worldY);

        // Kirim 'this' sebagai thrower
        Projectile proj = new Projectile(spawnX, spawnY, dir, itemThrown, this);
        GameModel.getInstance().addProjectile(proj);
    }
    
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public Inventory<Item> getInventory() { return inventory; }
    public void setBusy(boolean busy) { this.isBusy = busy; }
    public boolean isBusy() { return isBusy; }
}