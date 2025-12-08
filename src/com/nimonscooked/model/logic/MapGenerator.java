package com.nimonscooked.model.logic;

import com.nimonscooked.model.stations.*;
import com.nimonscooked.model.items.BoilingPot;
import com.nimonscooked.model.items.FryingPan;
import com.nimonscooked.utils.IngredientType;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class MapGenerator {
    
    private static final int ROWS = 10;
    private static final int COLS = 14;
    private Random rand = new Random();

    public Tile[][] generateRandomMap() {
        Tile[][] grid = new Tile[ROWS][COLS];
        
        // 1. Inisialisasi Lantai Kosong & Tembok Pinggir
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                // PERBAIKAN DI SINI:
                // Panggil constructor Tile(x, y) sesuai definisi di Tile.java
                Tile tile = new Tile(x, y);
                
                // Cek apakah ini pinggiran map
                boolean isBorder = (y == 0 || y == ROWS-1 || x == 0 || x == COLS-1);
                
                // Jika pinggiran, set sebagai tembok
                if (isBorder) {
                    tile.setWall(true);
                }
                
                grid[y][x] = tile;
            }
        }

        // 2. Tambahkan Tembok Acak di Tengah (Obstacles)
        int obstacleCount = 5 + rand.nextInt(4);
        for (int i = 0; i < obstacleCount; i++) {
            int rx = 2 + rand.nextInt(COLS - 4);
            int ry = 2 + rand.nextInt(ROWS - 4);
            grid[ry][rx].setWall(true);
        }

        // 3. DAFTAR STASIUN WAJIB
        List<Runnable> mandatoryPlacements = new ArrayList<>();
        
        mandatoryPlacements.add(() -> placeStation(grid, new IngredientStorage(0,0, IngredientType.PASTA)));
        mandatoryPlacements.add(() -> placeStation(grid, new IngredientStorage(0,0, IngredientType.TOMATO)));
        mandatoryPlacements.add(() -> placeStation(grid, new IngredientStorage(0,0, IngredientType.MEAT)));
        mandatoryPlacements.add(() -> placeStation(grid, new PlateStorage(0,0)));
        mandatoryPlacements.add(() -> placeStation(grid, new CuttingStation(0,0)));
        mandatoryPlacements.add(() -> placeStation(grid, new CuttingStation(0,0))); 
        
        mandatoryPlacements.add(() -> {
            CookingStation cs = new CookingStation(0,0);
            cs.setDevice(new BoilingPot());
            placeStation(grid, cs);
        });
        
        mandatoryPlacements.add(() -> {
            CookingStation cs = new CookingStation(0,0);
            cs.setDevice(new FryingPan());
            placeStation(grid, cs);
        });

        mandatoryPlacements.add(() -> placeStation(grid, new WashingStation(0,0)));
        mandatoryPlacements.add(() -> placeStation(grid, new ServingCounter(0,0)));
        mandatoryPlacements.add(() -> placeStation(grid, new TrashStation(0,0)));
        
        for(int k=0; k<3; k++) mandatoryPlacements.add(() -> placeStation(grid, new AssemblyStation(0,0)));

        // 4. Eksekusi Penempatan
        for (Runnable task : mandatoryPlacements) {
            task.run();
        }

        return grid;
    }

    private void placeStation(Tile[][] grid, Station s) {
        int attempts = 0;
        while (attempts < 100) {
            int rx = 1 + rand.nextInt(COLS - 2);
            int ry = 1 + rand.nextInt(ROWS - 2);
            
            Tile t = grid[ry][rx];
            
            boolean isSpawnPos = (rx == 2 && ry == 4) || (rx == 11 && ry == 4);
            
            if (!t.isWall() && t.getStation() == null && !isSpawnPos) {
                s.setPosition(rx, ry);
                t.setStation(s);
                t.setWall(false); 
                return; 
            }
            attempts++;
        }
        System.out.println("Failed to place station randomly.");
    }
}