package com.nimonscooked.model.entities;

import com.nimonscooked.core.GameObject;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.items.*;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.utils.Direction;

import java.util.List;

public class Projectile extends GameObject {
    
    private Item item;
    private double worldX, worldY; 
    private double speedX, speedY; 
    
    private final double SPEED = 0.35; // Sedikit dipercepat agar lemparan terasa responsif
    private double distanceTraveled = 0;
    private final double MAX_DISTANCE = 5.0; 
    
    private boolean isActive = true;
    private Chef thrower;

    public Projectile(int startX, int startY, Direction dir, Item item, Chef thrower) {
        super(startX, startY);
        this.item = item;
        this.thrower = thrower;
        
        this.worldX = startX;
        this.worldY = startY;
        
        this.speedX = dir.getDeltaX() * SPEED;
        this.speedY = dir.getDeltaY() * SPEED;
    }

    public void update(Map map, List<Chef> chefs) {
        if (!isActive) return;

        // 1. Simpan posisi 'aman' sebelumnya (sebelum menabrak)
        double prevX = worldX;
        double prevY = worldY;

        // 2. Gerakkan Projectile
        worldX += speedX;
        worldY += speedY;
        distanceTraveled += SPEED;

        // Update Grid Position
        this.setPosition((int)Math.round(worldX), (int)Math.round(worldY));

        // 3. DETEKSI TABRAKAN (Wall / Station)
        if (checkObstacleCollision(worldX, worldY, map)) {
            
            // Ambil Tile yang ditabrak
            int hitX = (int) Math.round(worldX);
            int hitY = (int) Math.round(worldY);
            Tile hitTile = map.getTile(hitX, hitY);

            // LOGIKA BARU: Jika kena STATION, coba taruh di atasnya
            if (hitTile != null && hitTile.getStation() != null) {
                boolean success = tryPlaceOnStation(hitTile.getStation());
                
                if (success) {
                    System.out.println("Item landed ON STATION.");
                    isActive = false;
                    return; // Selesai, item sudah di station
                }
                // Jika gagal (Station penuh/salah tipe), lanjut ke logika "jatuh sebelumnya"
            }

            // Jika kena TEMBOK atau Station Penuh -> Jatuh di lantai sebelumnya (prevX, prevY)
            landOnGround(map, prevX, prevY);
            return;
        }

        // 4. DETEKSI TANGKAPAN CHEF (Catch)
        for (Chef c : chefs) {
            if (c == thrower && distanceTraveled < 1.0) continue; // Skip pelempar sendiri di awal

            double dx = c.getWorldX() - worldX;
            double dy = c.getWorldY() - worldY;
            double dist = Math.sqrt(dx*dx + dy*dy);

            if (dist < 0.6) {
                if (c.getInventory().getItem() == null) {
                    c.getInventory().setItem(item);
                    System.out.println("CATCH! Caught " + item.getName());
                    isActive = false;
                    return;
                }
            }
        }

        // 5. Jarak Maksimal (Jatuh di tempat)
        if (distanceTraveled >= MAX_DISTANCE) {
            landOnGround(map, worldX, worldY);
        }
    }

    // --- HELPER LOGIC: Menaruh Item di Station dari Jarak Jauh ---
    private boolean tryPlaceOnStation(Station s) {
        
        // A. COOKING STATION (Lempar bahan ke Panci/Wajan)
        if (s instanceof CookingStation) {
            CookingStation cs = (CookingStation) s;
            if (cs.getDevice() != null && item instanceof Preparable) {
                if (cs.getDevice().canAccept((Preparable) item)) {
                    cs.getDevice().addIngredient((Preparable) item);
                    return true;
                }
            }
        }
        
        // B. CUTTING STATION (Lempar bahan ke Meja Potong)
        else if (s instanceof CuttingStation) {
            CuttingStation cs = (CuttingStation) s;
            if (cs.getItem() == null) {
                cs.setItem(item);
                return true;
            }
        }
        
        // C. ASSEMBLY STATION (Lempar ke Meja Rakit)
        else if (s instanceof AssemblyStation) {
            AssemblyStation as = (AssemblyStation) s;
            if (as.getStoredItem() == null) {
                as.setStoredItem(item);
                return true;
            }
        }
        
        // D. INGREDIENT STORAGE (Lempar ke atas Box Bahan - Fitur Item On Top)
        else if (s instanceof IngredientStorage) {
            IngredientStorage is = (IngredientStorage) s;
            if (is.getItemOnTop() == null) {
                is.setItemOnTop(item);
                return true;
            }
        }
        
        // E. PLATE STORAGE (Lempar Piring Kotor ke tumpukan - Opsional)
        else if (s instanceof WashingStation) {
            WashingStation ws = (WashingStation) s;
            if (item instanceof Plate && !((Plate)item).isClean()) {
                ws.addDirtyPlate();
                return true;
            }
        }

        return false; // Station penuh atau tipe item salah
    }

    private boolean checkObstacleCollision(double x, double y, Map map) {
        int tx = (int) Math.round(x);
        int ty = (int) Math.round(y);
        
        if (tx < 0 || tx >= map.getCols() || ty < 0 || ty >= map.getRows()) return true;

        Tile t = map.getTile(tx, ty);
        // Collision jika Wall ATAU Station
        return !t.isWalkable() || t.getStation() != null;
    }

    private void landOnGround(Map map, double x, double y) {
        isActive = false;
        int tx = (int) Math.round(x);
        int ty = (int) Math.round(y);

        Tile tile = map.getTile(tx, ty);
        
        // Pastikan jatuh di tile yang valid (Lantai kosong)
        if (tile != null && tile.isWalkable() && tile.getStation() == null && tile.getGroundItem() == null) {
            tile.setGroundItem(item);
            System.out.println("Item landed on ground.");
        } else {
            // Jika jatuh di tempat yang tidak valid (misal di dalam tembok karena glitch), item hilang
            System.out.println("Item destroyed/lost.");
        }
    }

    public boolean isActive() { return isActive; }
    public Item getItem() { return item; }
}