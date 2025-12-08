package com.nimonscooked.model.logic;

import com.nimonscooked.model.items.BoilingPot;
import com.nimonscooked.model.items.FryingPan;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.utils.IngredientType;

public class Map {
    private Tile[][] grid;
    private int rows;
    private int cols;

    // Layout Type B (Pasta Map) sesuai Spesifikasi [228]
    private final String[] LAYOUT_TYPE_B = {
        "AARRAAXXXXXXXX", 
        "I....AXXX....W", 
        "I....AXXX....W", 
        "I.V..AXXX....A", 
        "A....XXXX....R", 
        "P....XXXC....R", 
        "S....XXXC..V.I", 
        "S....XXXA....I", 
        "A...........T.", 
        "XXXXXXXXXXXXXX"
    };

    public Map() {
        this(false);
    }
    
    public Map(boolean isRandom) {
        if (isRandom) {
            // ... (Random map logic) ...
        } else {
            loadMapFromString(LAYOUT_TYPE_B);
        }
    }
    
    // Method baru: Station dibuat di loadMapFromString, tapi didaftarkan di sini
    public void registerStations(GameModel model) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Station s = grid[y][x].getStation();
                if (s instanceof PlateStorage) {
                    // Panggil register PlateStorage dari GameModel
                    model.registerPlateStorage((PlateStorage) s);
                }
            }
        }
    }

    private void loadMapFromString(String[] layout) {
        this.rows = layout.length;
        this.cols = layout[0].length();
        this.grid = new Tile[rows][cols];

        int storageCount = 0;
        IngredientType[] storageTypes = {
            IngredientType.PASTA, 
            IngredientType.TOMATO, 
            IngredientType.MEAT, 
            IngredientType.SHRIMP, 
            IngredientType.FISH
        };

        int stoveCount = 0;

        for (int y = 0; y < rows; y++) {
            String line = layout[y];
            for (int x = 0; x < cols; x++) {
                char c = line.charAt(x);
                Tile tile = new Tile(x, y);

                switch (c) {
                    case 'X': 
                        tile.setWall(true);
                        break;
                    case '.':
                    case 'V': 
                        break;
                        
                    case 'A':
                        tile.setStation(new AssemblyStation(x, y));
                        break;
                    case 'C':
                        tile.setStation(new CuttingStation(x, y));
                        break;
                    case 'S':
                        tile.setStation(new ServingCounter(x, y));
                        break;
                    case 'W':
                        tile.setStation(new WashingStation(x, y));
                        break;
                    case 'P':
                        // FIX: Buat PlateStorage TANPA inisialisasi piring
                        tile.setStation(new PlateStorage(x, y));
                        break;
                    case 'T':
                        tile.setStation(new TrashStation(x, y));
                        break;
                        
                    case 'I':
                        IngredientType type = IngredientType.PASTA; 
                        if (storageCount < storageTypes.length) {
                            type = storageTypes[storageCount];
                            storageCount++;
                        }
                        tile.setStation(new IngredientStorage(x, y, type));
                        break;
                        
                    case 'R':
                        CookingStation cs = new CookingStation(x, y);
                        if (stoveCount < 2) {
                            cs.setDevice(new BoilingPot());
                        } else {
                            cs.setDevice(new FryingPan());
                        }
                        stoveCount++;
                        tile.setStation(cs);
                        break;
                }
                grid[y][x] = tile;
            }
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return null;
        return grid[y][x];
    }
}