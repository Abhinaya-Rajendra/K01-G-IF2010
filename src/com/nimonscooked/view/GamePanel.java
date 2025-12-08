package com.nimonscooked.view;

import com.nimonscooked.core.CookingDevice; // Pastikan import ini ada
import com.nimonscooked.core.GameObserver;
import com.nimonscooked.core.Preparable;   // Import Preparable
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Order;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.model.items.KitchenUtensil;
import com.nimonscooked.model.items.Ingredient;
import com.nimonscooked.model.items.Plate;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements GameObserver {

    private GameModel model;
    private AssetManager assets;
    private final int TILE_SIZE = 50;
    private final int BLOCK_HEIGHT = 15; 

    public GamePanel() {
        this.model = GameModel.getInstance();
        this.model.addObserver(this);
        this.assets = AssetManager.getInstance(); 
        
        setPreferredSize(new Dimension(17 * TILE_SIZE, 10 * TILE_SIZE));
        setBackground(Color.LIGHT_GRAY);
        setFont(new Font("Arial", Font.BOLD, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFloor(g);

        if (model.getMap() != null) {
            for (int y = 0; y < model.getMap().getRows(); y++) {
                for (int x = 0; x < model.getMap().getCols(); x++) {
                    drawTileObject(g, x, y);
                }
                drawChefsAtRow(g, y);
                drawProjectilesAtRow(g, y);
            }
        }
        drawUI(g);
    }

    private void drawImageOrRect(Graphics g, String imageName, Color fallbackColor, int x, int y, int targetWidth, int targetHeight) {
        BufferedImage img = assets.getImage(imageName);
        
        if (img != null) {
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            double scale = (double) targetWidth / imgW;
            boolean isTileTexture = imageName.equals("floor") || imageName.equals("wall");
            
            if (isTileTexture) {
                g.drawImage(img, x, y, targetWidth, targetHeight, null);
            } else {
                int drawW = targetWidth; 
                int drawH = (int) (imgH * scale);
                int drawX = x + (targetWidth - drawW) / 2;
                int drawY = y + (targetHeight - drawH); 
                g.drawImage(img, drawX, drawY, drawW, drawH, null);
            }
        } else {
            g.setColor(fallbackColor);
            g.fillRect(x, y, targetWidth, targetHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, targetWidth, targetHeight);
        }
    }

    private void drawFloor(Graphics g) {
        Map map = model.getMap();
        if (map == null) return;
        for (int y = 0; y < map.getRows(); y++) {
            for (int x = 0; x < map.getCols(); x++) {
                drawImageOrRect(g, "floor", Color.WHITE, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawTileObject(Graphics g, int x, int y) {
        Tile tile = model.getMap().getTile(x, y);
        int px = x * TILE_SIZE;
        int py = y * TILE_SIZE;

        if (tile.isWall() || tile.getStation() != null) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(px, py + TILE_SIZE - BLOCK_HEIGHT, TILE_SIZE, BLOCK_HEIGHT);
            
            String imgKey = "wall"; 
            Color color = Color.DARK_GRAY;
            Station s = tile.getStation();
            
            if (s != null) {
                if (s instanceof IngredientStorage) {
                    imgKey = "storage_" + ((IngredientStorage) s).getType();
                    color = Color.ORANGE;
                } else if (s instanceof CuttingStation) {
                    imgKey = "station_cutting";
                    color = new Color(173, 216, 230);
                } else if (s instanceof CookingStation) {
                    imgKey = "station_stove";
                    color = new Color(255, 180, 180);
                } else if (s instanceof ServingCounter) {
                    imgKey = "station_serving";
                    color = new Color(144, 238, 144);
                } else if (s instanceof AssemblyStation) {
                    imgKey = "station_assembly";
                    color = new Color(200, 160, 255);
                } else if (s instanceof PlateStorage) {
                    imgKey = "station_plate";
                    color = new Color(255, 255, 224);
                } else if (s instanceof WashingStation) {
                    imgKey = "station_wash";
                    color = new Color(70, 130, 180);
                } else if (s instanceof TrashStation) {
                    imgKey = "station_trash";
                    color = new Color(101, 67, 33);
                }
            }

            int drawY = py - BLOCK_HEIGHT; 
            
            if (assets.getImage(imgKey) == null) {
                g.setColor(color.darker());
                g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            }
            
            drawImageOrRect(g, imgKey, color, px, drawY, TILE_SIZE, TILE_SIZE);
            drawStationDetails(g, tile, px, drawY);
        } 
        else if (tile.getGroundItem() != null) {
            drawImageOrRect(g, "item_default", Color.RED, px + 15, py + 15, 20, 20);
        }
    }

    private void drawStationDetails(Graphics g, Tile tile, int px, int py) {
        if (tile.getStation() == null) return;
        Station s = tile.getStation();

        // COOKING STATION
        if (s instanceof CookingStation) {
            CookingStation cs = (CookingStation) s;
            CookingDevice device = cs.getDevice(); // Ambil referensi Interface CookingDevice

            if (device != null) {
                String imgName = (device instanceof com.nimonscooked.model.items.BoilingPot) ? "pot" : "pan";
                drawImageOrRect(g, imgName, Color.GRAY, px + 10, py + 5, 30, 30); 

                // FIX 1: Gunakan referensi 'device' untuk cek isCooking()
                if (device.isCooking()) {
                    g.setColor(Color.BLACK); g.fillRect(px + 5, py - 10, 40, 6);
                    g.setColor(Color.GREEN);
                    // FIX 2: Gunakan getCookingProgress() (bukan getProgress)
                    int barWidth = (int) (40 * (device.getCookingProgress() / 100.0));
                    g.fillRect(px + 5, py - 10, barWidth, 6);
                }
                
                // Cek isi panci (perlu casting ke KitchenUtensil untuk akses list contents)
                if (device instanceof KitchenUtensil) {
                    KitchenUtensil utensil = (KitchenUtensil) device;
                    if (!utensil.isEmpty()) {
                        // FIX 3: Casting Preparable ke Ingredient untuk akses getState()
                        Preparable item = utensil.getContents().get(0);
                        if (item instanceof Ingredient) {
                            Ingredient ing = (Ingredient) item;
                            g.setColor(Color.BLUE);
                            g.drawString(ing.getState().getName().substring(0, 3), px + 10, py + 45);
                        }
                    }
                }
            }
        }
        
        // CUTTING & ASSEMBLY STATION
        else if (s instanceof CuttingStation || s instanceof AssemblyStation) {
            com.nimonscooked.model.items.Item itemOnTable = null;
            if (s instanceof CuttingStation) itemOnTable = ((CuttingStation)s).getItem();
            else itemOnTable = ((AssemblyStation)s).getStoredItem();

            if (itemOnTable != null) {
                if (itemOnTable instanceof Plate) {
                    drawImageOrRect(g, "plate", Color.WHITE, px + 5, py + 5, 40, 40);
                    if (!((Plate)itemOnTable).isEmpty()) {
                        g.setColor(Color.MAGENTA); g.fillOval(px + 15, py + 15, 20, 20);
                    }
                } else {
                    drawImageOrRect(g, "item_default", Color.RED, px + 15, py + 15, 20, 20);
                    if (itemOnTable instanceof Ingredient) {
                        g.setColor(Color.BLACK);
                        g.drawString(((Ingredient)itemOnTable).getState().getName().substring(0,3), px + 5, py + 45);
                    }
                }
            }
        }
        
        // WASHING STATION
        else if (s instanceof WashingStation) {
            WashingStation ws = (WashingStation) s;
            g.setColor(Color.WHITE);
            g.drawString("D:" + ws.getDirtyCount(), px + 5, py + 35);
            g.drawString("C:" + ws.getCleanCount(), px + 5, py + 45);
            if (ws.isWashing()) {
                g.setColor(Color.CYAN); g.fillRect(px + 5, py - 10, (int)(40 * ws.getProgress()/100.0), 6);
            }
        }
    }

    private void drawChefsAtRow(Graphics g, int rowY) {
        for (Chef chef : model.getChefs()) {
            // Gunakan Getter Position (karena protected/encapsulated)
            if (chef.getPosition().getY() == rowY) {
                int x = chef.getPosition().getX() * TILE_SIZE;
                int y = chef.getPosition().getY() * TILE_SIZE;
                int drawY = y - 15; 

                if (chef == model.getActiveChef()) {
                    g.setColor(new Color(255, 255, 0, 100)); 
                    g.fillOval(x, y, TILE_SIZE, TILE_SIZE/2);
                }

                drawImageOrRect(g, "chef", Color.GREEN, x + 5, drawY, TILE_SIZE - 10, TILE_SIZE - 10);

                if (!chef.getInventory().isEmpty()) {
                    int cx = x + TILE_SIZE/2;
                    int cy = drawY + TILE_SIZE/2;
                    Object item = chef.getInventory().getItem();

                    if (item instanceof Plate) {
                        Plate p = (Plate) item;
                        if (p.isClean()) drawImageOrRect(g, "plate", Color.WHITE, cx - 15, cy - 15, 30, 30);
                        else {
                            g.setColor(Color.DARK_GRAY); g.fillOval(cx - 15, cy - 15, 30, 30);
                        }
                        if (!p.isEmpty()) {
                            g.setColor(Color.MAGENTA); g.fillOval(cx - 5, cy - 5, 10, 10);
                        }
                    }
                    else if (item instanceof CookingDevice) { // Cek CookingDevice
                        String imgName = (item instanceof com.nimonscooked.model.items.BoilingPot) ? "pot" : "pan";
                        drawImageOrRect(g, imgName, Color.GRAY, cx - 12, cy - 12, 24, 24);
                        
                        if (item instanceof KitchenUtensil) {
                            KitchenUtensil u = (KitchenUtensil) item;
                            if(!u.isEmpty()) {
                                // FIX 4: Casting lagi di sini
                                Preparable content = u.getContents().get(0);
                                if (content instanceof Ingredient) {
                                    g.setColor(Color.WHITE);
                                    g.drawString(((Ingredient)content).getState().getName().substring(0,3), x+10, drawY+10);
                                }
                            }
                        }
                    }
                    else if (item instanceof Ingredient) {
                        drawImageOrRect(g, "item_default", Color.RED, cx - 10, cy - 10, 20, 20);
                        Ingredient ing = (Ingredient) item;
                        g.setColor(Color.BLACK);
                        g.drawString(ing.getState().getName().substring(0,3), x + 12, drawY + 15);
                    }
                }
            }
        }
    }
    
    private void drawProjectilesAtRow(Graphics g, int rowY) {
        if (model.getProjectiles() == null) return;
        for (Projectile p : model.getProjectiles()) {
            if (p.getPosition().getY() == rowY) {
                int x = p.getPosition().getX() * TILE_SIZE;
                int y = (p.getPosition().getY() * TILE_SIZE) - 20; 
                drawImageOrRect(g, "item_default", Color.YELLOW, x + 15, y + 15, 20, 20);
            }
        }
    }

    private void drawUI(Graphics g) {
    if (model.getOrderManager() == null) return;
    
    // PERBAIKAN: Pindahkan UI ke sisi kanan peta (Mulai dari tile 14)
    final int UI_START_X = 14 * TILE_SIZE + 10;
    
    // Background UI
    g.setColor(new Color(0, 0, 0, 180)); 
    // Lebar UI: 3 tiles (150px), Tinggi: 8 tiles (400px)
    g.fillRect(UI_START_X, 10, 3 * TILE_SIZE - 20, 8 * TILE_SIZE); 

    g.setColor(Color.YELLOW);
    g.setFont(new Font("Arial", Font.BOLD, 14));
    
    // 1. Draw Score
    g.drawString("SCORE: " + model.getScore(), UI_START_X + 10, 30);
    
    // 2. Draw Timer Durasi Game (Akan kita implementasikan di poin 2)
    long duration = model.getGameDuration(); // Asumsi method ini dibuat
    g.drawString("TIME: " + formatTime(duration), UI_START_X + 10, 50);

    // 3. Draw Active Orders
    g.setColor(Color.WHITE);
    g.drawString("ACTIVE ORDERS:", UI_START_X + 10, 80);
    g.setFont(new Font("Arial", Font.PLAIN, 12));
    
    int yPos = 100;
    for (Order order : model.getOrderManager().getActiveOrders()) {
        String text = "- " + order.getRecipe().getName() + " (" + order.getTimeLeft() + "s)";
        g.drawString(text, UI_START_X + 15, yPos);
        yPos += 20;
    }
}

// Method helper baru untuk format waktu (mm:ss)
private String formatTime(long totalSeconds) {
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
}

    @Override
    public void update(Object gameState) {
        repaint();
    }
}