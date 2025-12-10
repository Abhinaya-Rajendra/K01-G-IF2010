package com.nimonscooked.view;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.GameObserver;
import com.nimonscooked.core.Preparable;
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.model.items.*;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
        
        setPreferredSize(new Dimension(14 * TILE_SIZE, 10 * TILE_SIZE));
        setBackground(new Color(30, 30, 30)); 
        setFont(new Font("Arial", Font.BOLD, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        // --- 1. LOGIKA SCALING & CENTERING MAP ---
        if (model.getMap() != null) {
            int mapTilesX = model.getMap().getCols();
            int mapTilesY = model.getMap().getRows();
            
            int mapWidth = mapTilesX * TILE_SIZE;
            int mapHeight = mapTilesY * TILE_SIZE;
            
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            
            double scaleX = (double) panelWidth / mapWidth;
            double scaleY = (double) panelHeight / mapHeight;
            double scale = Math.min(scaleX, scaleY);
            scale *= 0.95; 

            int scaledMapW = (int) (mapWidth * scale);
            int scaledMapH = (int) (mapHeight * scale);
            int offsetX = (panelWidth - scaledMapW) / 2;
            int offsetY = (panelHeight - scaledMapH) / 2;

            g2d.translate(offsetX, offsetY);
            g2d.scale(scale, scale);
            
            // --- DRAW GAME WORLD ---
            drawFloor(g2d);
            
            for (int y = 0; y < mapTilesY; y++) {
                for (int x = 0; x < mapTilesX; x++) {
                    drawTileObject(g2d, x, y);
                }
                drawChefsAtRow(g2d, y);
                drawProjectilesAtRow(g2d, y);
            }
        }

        // --- 2. LOGIKA HUD (Tanpa Scaling) ---
        g2d.setTransform(oldTransform);
        drawHUD(g2d);

        // --- 3. LAYAR GAME OVER (Overlay) ---
        if (model.isGameOver()) {
            drawGameOverScreen(g2d);
        }
    }

    // --- GAME OVER SCREEN ---
    private void drawGameOverScreen(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();

        // 1. Overlay Hitam Transparan
        g.setColor(new Color(0, 0, 0, 200)); 
        g.fillRect(0, 0, w, h);

        // 2. Teks "GAME OVER"
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        drawCenteredString(g, "GAME OVER", w, h / 2 - 50);

        // 3. Teks Skor Akhir
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        drawCenteredString(g, "Final Score: " + model.getScore(), w, h / 2 + 10);

        // 4. Instruksi Restart
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Animasi kedip sederhana
        if (System.currentTimeMillis() / 500 % 2 == 0) {
            drawCenteredString(g, "Press 'R' to Restart", w, h / 2 + 60);
        }
    }

    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    // --- HUD ---
    private void drawHUD(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        // COIN (Skor)
        BufferedImage coinImg = assets.getImage("ui_coin");
        if (coinImg == null) coinImg = assets.getImage("item_default");
        if (coinImg != null) g.drawImage(coinImg, 20, h - 70, 50, 50, null);
        
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.BLACK); g.drawString(String.valueOf(model.getScore()), 82, h - 35);
        g.setColor(Color.ORANGE); g.drawString(String.valueOf(model.getScore()), 80, h - 37);

        // TIMER
        BufferedImage timerImg = assets.getImage("ui_timer");
        if (timerImg == null) timerImg = assets.getImage("item_default");
        String timeStr = formatTime(model.getGameDuration());
        int timerX = w - 160;
        if (timerImg != null) g.drawImage(timerImg, timerX, h - 70, 50, 50, null);

        // Ubah warna timer jadi merah jika waktu habis
        if (model.getGameDuration() > 150) g.setColor(Color.RED); // > 2.5 menit
        else g.setColor(Color.WHITE);
        
        g.drawString(timeStr, timerX + 55, h - 37); 

        // FAILED ORDERS INDICATOR
        int failed = model.getFailedOrdersCount();
        int maxFailed = model.getMaxFailedOrders();
        
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String failText = "Failed: " + failed + "/" + maxFailed;
        
        int textW = g.getFontMetrics().stringWidth(failText);
        int textX = (w - textW) / 2;
        
        g.setColor(new Color(50, 0, 0, 180));
        g.fillRect(textX - 10, 10, textW + 20, 30);
        
        if (failed >= maxFailed - 1) g.setColor(Color.RED);
        else g.setColor(Color.WHITE);
        
        g.drawString(failText, textX, 32);
    }

    // --- DRAWING HELPERS ---

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
            // Shadow
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

        // COOKING STATION VISUAL LOGIC
        if (s instanceof CookingStation) {
            CookingStation cs = (CookingStation) s;
            CookingDevice device = cs.getDevice(); 

            if (device != null) {
                String imgName = (device instanceof BoilingPot) ? "pot" : "pan";
                drawImageOrRect(g, imgName, Color.GRAY, px + 10, py + 5, 30, 30); 

                if (device instanceof KitchenUtensil) {
                    KitchenUtensil utensil = (KitchenUtensil) device;
                    if (!utensil.isEmpty()) {
                        Preparable item = utensil.getContents().get(0);
                        if (item instanceof Ingredient) {
                            Ingredient ing = (Ingredient) item;
                            String stateName = ing.getState().getName();

                            if (stateName.equals("COOKING")) {
                                // Green Bar
                                g.setColor(Color.BLACK); g.fillRect(px + 5, py - 10, 40, 6);
                                g.setColor(Color.GREEN);
                                int barWidth = (int) (40 * (device.getCookingProgress() / 100.0));
                                g.fillRect(px + 5, py - 10, barWidth, 6);
                            } 
                            else if (stateName.equals("COOKED")) {
                                g.setColor(Color.GREEN);
                                g.setFont(new Font("Arial", Font.BOLD, 10));
                                g.drawString("READY", px + 8, py - 5);
                            }
                            else if (stateName.equals("BURNED")) {
                                g.setColor(Color.RED);
                                g.setFont(new Font("Arial", Font.BOLD, 10));
                                g.drawString("BURNED!", px + 2, py - 5);
                            }
                            else {
                                // Raw / Chopped
                                g.setColor(Color.BLUE);
                                g.drawString(stateName.substring(0, 3), px + 10, py + 45);
                            }
                        }
                    }
                }
            }
        }
        // CUTTING & ASSEMBLY
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
        // WASHING
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
                    else if (item instanceof CookingDevice) {
                         String imgName = (item instanceof BoilingPot) ? "pot" : "pan";
                         drawImageOrRect(g, imgName, Color.GRAY, cx - 12, cy - 12, 24, 24);
                         
                         if (item instanceof KitchenUtensil) {
                             KitchenUtensil u = (KitchenUtensil) item;
                             if (!u.isEmpty() && u.getContents().get(0) instanceof Ingredient) {
                                 Ingredient ing = (Ingredient) u.getContents().get(0);
                                 g.setColor(Color.WHITE);
                                 g.setFont(new Font("Arial", Font.BOLD, 10));
                                 g.drawString(ing.getState().getName().substring(0,3), x+10, drawY+10);
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

    private void drawImageOrRect(Graphics g, String imageName, Color fallbackColor, int x, int y, int targetWidth, int targetHeight) {
        BufferedImage img = assets.getImage(imageName);
        if (img != null) {
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            boolean isTileTexture = imageName.equals("floor") || imageName.equals("wall");
            
            if (isTileTexture) {
                g.drawImage(img, x, y, targetWidth, targetHeight, null);
            } else {
                double scale = (double) targetWidth / imgW;
                int drawH = (int) (imgH * scale);
                int drawX = x + (targetWidth - targetWidth) / 2; 
                int drawY = y + (targetHeight - drawH); 
                g.drawImage(img, drawX, drawY, targetWidth, drawH, null);
            }
        } else {
            g.setColor(fallbackColor);
            g.fillRect(x, y, targetWidth, targetHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, targetWidth, targetHeight);
        }
    }

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