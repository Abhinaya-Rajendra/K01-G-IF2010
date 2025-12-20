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
 private boolean isBusy = false; // Status untuk memblokir interaksi/aksi lain
 private boolean isDashing = false;
 private long lastDashTime = 0;
 private int dashDurationTicks = 0;
 private final long DASH_COOLDOWN = 1000; 
private String texturePrefix; // "fox" atau "raccoon"

    // Ubah Constructor untuk menerima tipe chef
    public Chef(int startGridX, int startGridY, String texturePrefix) {
        super(startGridX, startGridY);
        this.worldX = startGridX;
        this.worldY = startGridY;
        this.texturePrefix = texturePrefix; // Simpan identitas
        this.inventory = new Inventory<>();
    }
    
    // Getter
    public String getTexturePrefix() {
        return texturePrefix;
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

 private boolean checkWallCollision(double targetX, double targetY, Map map) {
  double left = targetX + (1 - COLLISION_SIZE) / 2;
  double right = left + COLLISION_SIZE;
  double top = targetY + 0.3 + (1 - COLLISION_SIZE) / 2;
  double bottom = top + COLLISION_SIZE;

  return isWall(left, top, map) || isWall(right, top, map) || 
   isWall(left, bottom, map) || isWall(right, bottom, map);
 }
 
 private boolean checkChefCollision(double targetX, double targetY) {
  List<Chef> chefs = GameModel.getInstance().getChefs();
  for (Chef other : chefs) {
   if (other == this) continue; 
   
   double dx = targetX - other.getWorldX();
   double dy = targetY - other.getWorldY();
   double dist = Math.sqrt(dx*dx + dy*dy);
   
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
  // Chef yang sedang busy hanya bisa interaksi yang terkait stasiun yang membuatnya busy
  if (isBusy) { 
   // Logika akan ditangani sepenuhnya oleh Stasiun jika chef berinteraksi dengan stasiun
  }

  Tile facingTile = getFacingTile(map);
  if (facingTile == null) return;

  Item groundItem = facingTile.getGroundItem();
  Item heldItem = inventory.getItem();

  // 1. INTERAKSI DENGAN STASIUN
  if (facingTile.getStation() != null) {
   // Serahkan kontrol ke stasiun.
   facingTile.getStation().interact(this);
  } 
  // 2. INTERAKSI TANPA STASIUN (Pick Up, Drop, Transfer)
  else if (!isBusy) { // Hanya proses Pick Up/Drop/Transfer jika TIDAK sibuk
        
        // FIX BARU: SCOOPING dari UTENSIL yang diletakkan di COUNTER/FLOOR
        if (heldItem instanceof Plate && groundItem instanceof KitchenUtensil) {
            KitchenUtensil utensil = (KitchenUtensil) groundItem;
            Plate plate = (Plate) heldItem;
            
            // Panggil moveContentsTo Utensil, yang sudah menangani logic scoop (burned/empty/etc)
            utensil.moveContentsTo(plate);
            return; // Interaksi scoop selesai
        } 

        // Existing Pick Up/Drop/Plate-Ingredient Transfer logic
    Item ground = facingTile.getGroundItem();

    if (heldItem == null && ground != null) {
      // Pick up
      inventory.setItem(ground);
      facingTile.setGroundItem(null);
    } else if (heldItem != null) {
      handleDropOrPlate(heldItem, ground, facingTile);
    }
  }
 }
 
 private void handleDropOrPlate(Item held, Item ground, Tile tile) {
    // Logika ini hanya menangani transfer antara Plate dan Ingredient (bukan Utensil)
    
    // KASUS 1: Held Plate, Ground Ingredient (Tambah ke Piring yang dipegang)
    if (held instanceof Plate && ground instanceof Ingredient) {
      // Tambahkan pengecekan canAccept (kapasitas)
      if (((Plate) held).isClean() && ((Plate) held).canAccept((Ingredient) ground)) { 
        ((Plate) held).addIngredient((Ingredient) ground);
        tile.setGroundItem(null);
      }
    } 
    // KASUS 2: Held Ingredient, Ground Plate (Tambah ke Piring yang di tanah)
    else if (held instanceof Ingredient && ground instanceof Plate) {
      // Tambahkan pengecekan canAccept (kapasitas)
      if (((Plate) ground).isClean() && ((Plate) ground).canAccept((Ingredient) held)) { 
        ((Plate) ground).addIngredient((Ingredient) held);
        inventory.takeItem();
      }
    } 
    // KASUS 3: Drop item
    else if (ground == null) {
      // --- FIX: CEK APAKAH TILE ADALAH WALKABLE (BUKAN TEMBOK) ---
      // Jika tile tidak bisa jalan (isWalkable == false), berarti itu tembok/obstacle
      // Maka jangan drop item di situ.
      if (tile.isWalkable()) { 
          tile.setGroundItem(inventory.takeItem());
      }
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
    if (isBusy) return; 
  if (inventory.getItem() == null) return;
  if (!(inventory.getItem() instanceof Ingredient)) return;
  
  Item itemThrown = inventory.takeItem();
  
  Direction dir = Direction.DOWN;
  if (Math.abs(lastDirX) > Math.abs(lastDirY)) {
   dir = (lastDirX > 0) ? Direction.RIGHT : Direction.LEFT;
  } else {
   dir = (lastDirY > 0) ? Direction.DOWN : Direction.UP;
  }

  int spawnX = (int) Math.round(worldX); 
  int spawnY = (int) Math.round(worldY);

  Projectile proj = new Projectile(spawnX, spawnY, dir, itemThrown, this);
  GameModel.getInstance().addProjectile(proj);
 }
  
 public double getWorldX() { return worldX; }
 public double getWorldY() { return worldY; }
 public Inventory<Item> getInventory() { return inventory; }
 public void setBusy(boolean busy) { this.isBusy = busy; }
 public boolean isBusy() { return isBusy; }

 public double getLastDirX() {
    return lastDirX;
}

public double getLastDirY() {
    return lastDirY;
}
}