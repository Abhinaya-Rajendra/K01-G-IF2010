package com.nimonscooked.model.logic;

import com.nimonscooked.model.items.BoilingPot;
import com.nimonscooked.model.items.FryingPan;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.utils.IngredientType;

import java.awt.Point;
import java.util.*;

public class Map {
    private Tile[][] grid;
    private int rows = 10;
    private int cols = 14;

    // Layout Type B (Static)
    private final String[] LAYOUT_TYPE_B = {
        "XXXXXXXXXXXXXX",
        "AARRAAXXXXXXXX", 
        "I....AXXX....W", 
        "I....AXXX....W", 
        "I.V..AXXX....A", 
        "A....XXXX....R", 
        "P....XXXC....R", 
        "S....XXXC..V.I", 
        "S....XXXA....I", 
        "A............T", 
        "XXXXXXXXXXXXXX"
    };

    public Map() {
        this(false);
    }
    
    public Map(boolean isRandom) {
        // Inisialisasi array grid
        grid = new Tile[rows][cols];
        
        if (isRandom) {
            generateValidRandomMap();
        } else {
            loadMapFromString(LAYOUT_TYPE_B);
        }
    }

    // --- ALGORITMA SMART RANDOM GENERATION ---
    private void generateValidRandomMap() {
        boolean isValid = false;
        int attempts = 0;

        // Coba generate sampai map valid (maksimal 1000 kali coba agar tidak infinite loop)
        while (!isValid && attempts < 1000) {
            // 1. Reset Grid Kosong
            initializeEmptyGrid();
            
            // 2. Buat Tembok Keliling
            createPerimeter();
            
            // 3. Taruh Obstacle Acak
            placeRandomObstacles();
            
            // 4. Pastikan Spawn Chef Kosong
            clearChefSpawns();
            
            // 5. Cek Koneksi (Apakah Chef bisa jalan ke semua lantai?)
            if (isMapConnected()) {
                // 6. Taruh Station Penting
                if (placeEssentialStations()) {
                    isValid = true;
                    System.out.println("Random Map Generated (Attempts: " + (attempts+1) + ")");
                }
            }
            attempts++;
        }
        
        // Jika gagal total (jarang terjadi), pakai map default
        if (!isValid) {
            System.out.println("Failed to generate random map. Loading default.");
            loadMapFromString(LAYOUT_TYPE_B);
        }
    }

    private void initializeEmptyGrid() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Tile(x, y);
            }
        }
    }

    private void createPerimeter() {
        for (int x = 0; x < cols; x++) {
            grid[0][x].setWall(true);
            grid[rows-1][x].setWall(true);
        }
        for (int y = 0; y < rows; y++) {
            grid[y][0].setWall(true);
            grid[y][cols-1].setWall(true);
        }
    }

    private void placeRandomObstacles() {
        Random rand = new Random();
        int obstacleCount = 10 + rand.nextInt(5); // 10-14 tembok tambahan
        
        for (int i = 0; i < obstacleCount; i++) {
            int rx = 1 + rand.nextInt(cols - 2);
            int ry = 1 + rand.nextInt(rows - 2);
            grid[ry][rx].setWall(true);
        }
    }

    private void clearChefSpawns() {
        // Spawn GameModel: (2,4) dan (11,4)
        // Kita kosongkan titik itu dan sekitarnya (cross pattern)
        int[] spawnsX = {2, 11};
        int spawnY = 4;

        for (int sx : spawnsX) {
            safeClear(sx, spawnY);
            safeClear(sx+1, spawnY);
            safeClear(sx-1, spawnY);
            safeClear(sx, spawnY+1);
            safeClear(sx, spawnY-1);
        }
    }

    private void safeClear(int x, int y) {
        if (x > 0 && x < cols-1 && y > 0 && y < rows-1) {
            grid[y][x].setWall(false);
            grid[y][x].setStation(null);
        }
    }

    // Algoritma Flood Fill (BFS) untuk cek jalan buntu
    private boolean isMapConnected() {
        boolean[][] visited = new boolean[rows][cols];
        Queue<Point> queue = new LinkedList<>();
        
        // Start dari Chef 1 Spawn (2,4)
        Point start = new Point(2, 4);
        if (grid[start.y][start.x].isWall()) return false;

        queue.add(start);
        visited[start.y][start.x] = true;
        
        int reachableCount = 0;

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            reachableCount++;

            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    if (!visited[ny][nx] && !grid[ny][nx].isWall()) {
                        visited[ny][nx] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }

        // Hitung total lantai sebenarnya
        int totalFloor = 0;
        for(int y=0; y<rows; y++) {
            for(int x=0; x<cols; x++) {
                if(!grid[y][x].isWall()) totalFloor++;
            }
        }

        return reachableCount == totalFloor;
    }

    private boolean placeEssentialStations() {
        // Cari tembok yang 'aktif' (sebelahnya ada lantai)
        List<Tile> wallCandidates = new ArrayList<>();
        
        for(int y=0; y<rows; y++) {
            for(int x=0; x<cols; x++) {
                if (grid[y][x].isWall()) {
                    // Cek 4 sisi, jika ada lantai, ini kandidat bagus
                    if (hasNeighborFloor(x, y)) {
                        wallCandidates.add(grid[y][x]);
                    }
                }
            }
        }
        
        Collections.shuffle(wallCandidates);

        // List Station Wajib
        Queue<Station> essentials = new LinkedList<>();
        essentials.add(new IngredientStorage(0,0, IngredientType.PASTA));
        essentials.add(new IngredientStorage(0,0, IngredientType.TOMATO));
        essentials.add(new IngredientStorage(0,0, IngredientType.MEAT));
        essentials.add(new PlateStorage(0,0));
        essentials.add(new ServingCounter(0,0));
        essentials.add(new WashingStation(0,0));
        essentials.add(new TrashStation(0,0));
        essentials.add(new CookingStation(0,0)); // Panci
        essentials.add(new CookingStation(0,0)); // Wajan
        essentials.add(new CuttingStation(0,0)); // Potong

        // Pasang Station
        while(!essentials.isEmpty()) {
            if (wallCandidates.isEmpty()) return false; // Gagal, tembok habis
            
            Tile t = wallCandidates.remove(0);
            Station s = essentials.poll();
            
            s.setPosition(t.getX(), t.getY());
            // s.setX(t.getX());
            // s.setY(t.getY());
            t.setStation(s); // Tile tetap isWall = true, tapi ada stationnya
        }
        
        // Isi sisa tembok dengan Meja (Assembly) atau Cutting tambahan (Random)
        for (Tile t : wallCandidates) {
            if (Math.random() > 0.6) {
                Station filler;
                if (Math.random() > 0.5) filler = new AssemblyStation(t.getX(), t.getY());
                else filler = new CuttingStation(t.getX(), t.getY());
                
                t.setStation(filler);
            }
        }
        
        return true;
    }
    
    private boolean hasNeighborFloor(int x, int y) {
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for(int i=0; i<4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx>=0 && nx<cols && ny>=0 && ny<rows) {
                if (!grid[ny][nx].isWall()) return true;
            }
        }
        return false;
    }

    // --- END SMART RANDOM LOGIC ---

    public void registerStations(GameModel model) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Station s = grid[y][x].getStation();
                if (s != null) {
                    if (s instanceof PlateStorage) {
                        model.registerPlateStorage((PlateStorage) s);
                    }
                    if (s instanceof CookingStation) {
                        CookingStation cs = (CookingStation) s;
                        // Randomize Pot vs Pan jika belum diset
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
                    case '.': case 'V': break;
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