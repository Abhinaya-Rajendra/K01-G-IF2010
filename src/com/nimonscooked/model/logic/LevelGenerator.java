package com.nimonscooked.model.logic;

import com.nimonscooked.model.entities.Recipe;
import com.nimonscooked.model.items.BoilingPot;
import com.nimonscooked.model.items.FryingPan;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.utils.IngredientType;

import java.awt.Point;
import java.util.*;

/**
 * Kelas khusus untuk menghasilkan level prosedural yang valid dan solvable.
 * Menggunakan pendekatan Room-Connector untuk menjamin konektivitas area.
 */
public class LevelGenerator {
    private int rows;
    private int cols;
    private Tile[][] grid;
    private Random random;

    // Daftar kebutuhan level berdasarkan resep
    private Set<IngredientType> requiredIngredients;
    private boolean needsPot = false;
    private boolean needsPan = false;
    private boolean needsCutting = false;

    public LevelGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.random = new Random();
        this.requiredIngredients = new HashSet<>();
    }

    public Tile[][] generateLevel(OrderManager orderManager) {
        // 1. Analisis Kebutuhan dari Recipe Manager
        analyzeRequirements(orderManager);

        boolean success = false;
        int attempts = 0;

        // Coba generate sampai valid (biasanya berhasil di percobaan ke-1 atau ke-2)
        while (!success && attempts < 100) {
            // Reset grid penuh tembok
            initializeGridWithWalls();

            // 2. Generate Layout Lantai (Room & Corridors Algorithm)
            generateFloorLayout();

            // 3. Identifikasi Posisi Stasiun Valid (Tembok yang menghadap lantai)
            List<Point> validStationSpots = getValidStationCoordinates();

            // 4. Tempatkan Stasiun Wajib (Agar level bisa diselesaikan)
            if (placeMandatoryStations(validStationSpots)) {
                // 5. Validasi Akhir (Flood Fill dari titik spawn)
                if (validateConnectivity()) {
                    success = true;
                }
            }
            attempts++;
        }

        if (!success) {
            System.err.println("Gagal generate level valid setelah 100 percobaan. Fallback ke layout statis.");
            // Logic fallback bisa ditangani di Map.java
            return null;
        }

        return grid;
    }

    private void initializeGridWithWalls() {
        grid = new Tile[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Tile(x, y);
                grid[y][x].setWall(true); // Default semua tembok
            }
        }
    }

    private void analyzeRequirements(OrderManager orderManager) {
        requiredIngredients.clear();
        needsPot = false;
        needsPan = false;
        needsCutting = false; // Default false, cek resep

        List<Recipe> recipes = orderManager.getAllRecipes();
        for (Recipe r : recipes) {
            // Catat semua ingredient yang dibutuhkan
            requiredIngredients.addAll(r.getRequiredIngredients());
            
            // Logika sederhana: di game ini biasanya resep butuh potong/masak
            // Kita asumsikan semua level butuh minimal 1 cutting board & 1 kompor untuk aman
            needsCutting = true; 
            needsPot = true;
            needsPan = true;
        }
    }

    /**
     * Algoritma "Drunkard's Walk" atau Room Carving yang dimodifikasi.
     * Kita mulai dari tengah dan menggali ruangan-ruangan.
     */
    private void generateFloorLayout() {
        int centerX = cols / 2;
        int centerY = rows / 2;

        // Buat Ruang Utama di tengah (agar spawn point aman)
        carveRoom(centerX - 2, centerY - 2, 5, 5);

        // Buat beberapa walker untuk menggali koridor/ruangan lain
        int walkers = 4;
        for (int i = 0; i < walkers; i++) {
            int currentX = centerX;
            int currentY = centerY;
            int lifeTime = 20 + random.nextInt(15); // Panjang langkah

            for (int step = 0; step < lifeTime; step++) {
                // Gerak acak
                int dir = random.nextInt(4);
                if (dir == 0) currentX++;
                else if (dir == 1) currentX--;
                else if (dir == 2) currentY++;
                else if (dir == 3) currentY--;

                // Clamp agar tidak keluar batas (sisakan border 1 tile untuk tembok luar)
                currentX = Math.max(1, Math.min(cols - 2, currentX));
                currentY = Math.max(1, Math.min(rows - 2, currentY));

                // Gali lantai (lebar 1 atau 2 tile agar tidak sempit)
                grid[currentY][currentX].setWall(false);
                
                // Kadang-kadang buat ruangan kecil di ujung jalan
                if (random.nextDouble() < 0.1) {
                    carveRoom(currentX - 1, currentY - 1, 3, 3);
                }
            }
        }
    }

    private void carveRoom(int x, int y, int w, int h) {
        for (int iy = y; iy < y + h; iy++) {
            for (int ix = x; ix < x + w; ix++) {
                if (ix > 0 && ix < cols - 1 && iy > 0 && iy < rows - 1) {
                    grid[iy][ix].setWall(false);
                }
            }
        }
    }

    /**
     * Mencari semua koordinat tembok yang bersebelahan dengan setidaknya satu lantai.
     * Ini adalah kandidat tempat meletakkan stasiun.
     */
    private List<Point> getValidStationCoordinates() {
        List<Point> spots = new ArrayList<>();
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        for (int y = 1; y < rows - 1; y++) {
            for (int x = 1; x < cols - 1; x++) {
                if (grid[y][x].isWall()) {
                    boolean hasFloorNeighbor = false;
                    for (int i = 0; i < 4; i++) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        if (!grid[ny][nx].isWall() && grid[ny][nx].getStation() == null) {
                            hasFloorNeighbor = true;
                            break;
                        }
                    }
                    if (hasFloorNeighbor) {
                        spots.add(new Point(x, y));
                    }
                }
            }
        }
        Collections.shuffle(spots, random); // Acak urutan
        return spots;
    }

    private boolean placeMandatoryStations(List<Point> spots) {
        Queue<Station> essentialQueue = new LinkedList<>();

        // 1. Storage Bahan (CRITICAL)
        for (IngredientType type : requiredIngredients) {
            essentialQueue.add(new IngredientStorage(0, 0, type));
        }

        // 2. Utilitas Dasar
        essentialQueue.add(new PlateStorage(0, 0));
        essentialQueue.add(new ServingCounter(0, 0));
        essentialQueue.add(new WashingStation(0, 0));
        essentialQueue.add(new TrashStation(0, 0));

        // 3. Alat Masak
        if (needsPot) {
            CookingStation cs = new CookingStation(0, 0);
            cs.setDevice(new BoilingPot());
            essentialQueue.add(cs);
        }
        if (needsPan) {
            CookingStation cs = new CookingStation(0, 0);
            cs.setDevice(new FryingPan());
            essentialQueue.add(cs);
        }
        if (needsCutting) {
            essentialQueue.add(new CuttingStation(0, 0));
            // Tambahkan ekstra cutting board karena sering dipakai
            essentialQueue.add(new CuttingStation(0, 0)); 
        }

        // --- Proses Penempatan ---
        while (!essentialQueue.isEmpty()) {
            if (spots.isEmpty()) return false; // Tidak cukup tempat!

            Point p = spots.remove(0);
            Station station = essentialQueue.poll();
            
            station.setPosition(p.x, p.y);
            grid[p.y][p.x].setStation(station);
        }

        // --- Isi sisa spot dengan Assembly / Cutting (Filler) ---
        for (Point p : spots) {
            // Jangan isi semua tembok, biarkan ada tembok kosong untuk variasi
            if (random.nextDouble() > 0.3) { 
                Station filler;
                if (random.nextDouble() > 0.6) filler = new AssemblyStation(p.x, p.y);
                else filler = new CuttingStation(p.x, p.y);
                
                grid[p.y][p.x].setStation(filler);
            }
        }

        return true;
    }

    private boolean validateConnectivity() {
        // Cari satu titik lantai untuk start BFS
        Point start = null;
        int totalFloor = 0;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (!grid[y][x].isWall()) {
                    if (start == null) start = new Point(x, y);
                    totalFloor++;
                }
            }
        }

        if (start == null) return false;

        // BFS Flood Fill
        boolean[][] visited = new boolean[rows][cols];
        Queue<Point> q = new LinkedList<>();
        q.add(start);
        visited[start.y][start.x] = true;
        int count = 0;

        while (!q.isEmpty()) {
            Point p = q.poll();
            count++;

            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    // Jalan jika lantai, atau jika stasiun (untuk memastikan stasiun terjangkau)
                    // Tapi flood fill biasanya hanya di lantai. 
                    // Kita asumsikan valid jika semua lantai terhubung satu sama lain.
                    if (!visited[ny][nx] && !grid[ny][nx].isWall()) {
                        visited[ny][nx] = true;
                        q.add(new Point(nx, ny));
                    }
                }
            }
        }

        // Jika jumlah lantai yang bisa dijangkau == total lantai, berarti terhubung
        return count == totalFloor;
    }
}