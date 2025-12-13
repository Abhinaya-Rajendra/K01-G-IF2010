package com.nimonscooked.model.logic;

import com.nimonscooked.model.items.BoilingPot;
import com.nimonscooked.model.items.FryingPan;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.utils.IngredientType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map {
    private Tile[][] grid;
    private int rows = 10;
    private int cols = 16;
    
    // Menyimpan titik spawn yang aman (calculated dynamically)
    private List<Point> dynamicSpawnPoints = new ArrayList<>();

    // Layout Type B (Static Fallback)
    private final String[] LAYOUT_TYPE_B = {
        "XXXXXXXXXXXXXXXX",
        "XAARRAAXXXXXXXXX", 
        "XI....AXXX....WX", 
        "XI....AXXX....WX", 
        "XI.V..AXXX....AX", 
        "XA....XXXX....RX", 
        "XP....XXXC....RX", 
        "XS....XXXC..V.IX", 
        "XS....XXXA....IX", 
        "XA............TX", 
        "XXXXXXXXXXXXXXXX"
    };

    public Map() {
        this(false);
    }
    
    public Map(boolean isRandom) {
        // Inisialisasi awal
        grid = new Tile[rows][cols];
        
        if (isRandom) {
            generateMapUsingAlgorithm();
            calculateDynamicSpawns();
        } else {
            loadMapFromString(LAYOUT_TYPE_B);
            dynamicSpawnPoints.add(new Point(3, 4));
            dynamicSpawnPoints.add(new Point(12, 7));
        }
        
        // REVISI: Hitung spawn point yang aman SETELAH map selesai dibuat/diload
    }

    private void generateMapUsingAlgorithm() {
        // Menggunakan LevelGenerator yang baru
        LevelGenerator generator = new LevelGenerator(rows, cols);
        
        // Kita butuh akses ke OrderManager untuk tahu requirements
        OrderManager om = GameModel.getInstance().getOrderManager();
        
        Tile[][] generatedGrid = generator.generateLevel(om);
        
        if (generatedGrid != null) {
            this.grid = generatedGrid;
        } else {
            // Fallback jika generator gagal total
            loadMapFromString(LAYOUT_TYPE_B);
        }
    }
    
    // REVISI: Metode Baru untuk Menghitung Titik Spawn Dinamis
    // Mencari 2 titik lantai kosong yang berjauhan agar chef tidak terjebak
    private void calculateDynamicSpawns() {
        dynamicSpawnPoints.clear();
        List<Point> floorTiles = new ArrayList<>();
        
        // 1. Kumpulkan semua koordinat lantai yang benar-benar kosong
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // Pastikan grid tidak null (safety check)
                if (grid[y][x] != null) {
                    Tile t = grid[y][x];
                    if (!t.isWall() && t.getStation() == null) {
                        floorTiles.add(new Point(x, y));
                    }
                }
            }
        }

        // Jika lantai terlalu sedikit (fallback ke hardcoded)
        if (floorTiles.size() < 2) {
            dynamicSpawnPoints.add(new Point(2, 4));
            dynamicSpawnPoints.add(new Point(11, 4));
            return;
        }

        // 2. Pilih titik pertama secara acak
        Random rand = new Random();
        Point p1 = floorTiles.get(rand.nextInt(floorTiles.size()));
        dynamicSpawnPoints.add(p1);

        // 3. Pilih titik kedua yang jaraknya paling jauh dari p1
        Point p2 = p1;
        double maxDist = -1;

        for (Point p : floorTiles) {
            double dist = p.distance(p1);
            if (dist > maxDist) {
                maxDist = dist;
                p2 = p;
            }
        }
        dynamicSpawnPoints.add(p2);
        
        System.out.println("Dynamic Spawns Calculated: " + p1 + " & " + p2);
    }
    
    // REVISI: Getter untuk titik spawn
    public Point getSpawnPoint(int chefIndex) {
        if (dynamicSpawnPoints.isEmpty()) {
            // Fallback layout B statis
            if (chefIndex == 0) return new Point(2, 4);
            return new Point(11, 4);
        }
        // Pastikan index aman dengan modulo
        return dynamicSpawnPoints.get(chefIndex % dynamicSpawnPoints.size());
    }

    public void registerStations(GameModel model) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Station s = grid[y][x].getStation();
                if (s != null) {
                    if (s instanceof PlateStorage) {
                        model.registerPlateStorage((PlateStorage) s);
                    }
                    // Cooking station yang digenerate oleh LevelGenerator sudah punya device
                    // Tapi untuk layout static, kita perlu cek
                    if (s instanceof CookingStation) {
                        CookingStation cs = (CookingStation) s;
                        if (cs.getDevice() == null) {
                            if (Math.random() > 0.5) cs.setDevice(new BoilingPot());
                            else cs.setDevice(new FryingPan());
                        }
                    }
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
            IngredientType.PASTA, IngredientType.TOMATO, IngredientType.MEAT, 
            IngredientType.SHRIMP, IngredientType.FISH
        };
        int stoveCount = 0;

        for (int y = 0; y < rows; y++) {
            String line = layout[y];
            for (int x = 0; x < cols; x++) {
                char c = line.charAt(x);
                Tile tile = new Tile(x, y);

                switch (c) {
                    case 'X': tile.setWall(true); break;
                    case '.': break;
                    case 'A': tile.setStation(new AssemblyStation(x, y)); break;
                    case 'C': tile.setStation(new CuttingStation(x, y)); break;
                    case 'S': tile.setStation(new ServingCounter(x, y)); break;
                    case 'W': tile.setStation(new WashingStation(x, y)); break;
                    case 'P': tile.setStation(new PlateStorage(x, y)); break;
                    case 'T': tile.setStation(new TrashStation(x, y)); break;
                    case 'I':
                        IngredientType type = IngredientType.PASTA; 
                        if (storageCount < storageTypes.length) type = storageTypes[storageCount++];
                        tile.setStation(new IngredientStorage(x, y, type));
                        break;
                    case 'R':
                        CookingStation cs = new CookingStation(x, y);
                        if (stoveCount < 2) cs.setDevice(new BoilingPot());
                        else cs.setDevice(new FryingPan());
                        stoveCount++;
                        tile.setStation(cs);
                        break;
                }
                if (tile.getStation() != null) tile.setWall(true);
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