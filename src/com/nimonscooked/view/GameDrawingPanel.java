package com.nimonscooked.view;

import com.nimonscooked.core.CookingDevice;
import com.nimonscooked.core.GameObserver; // Import ini tetap diperlukan untuk tipe data
import com.nimonscooked.model.logic.GameModel;
import com.nimonscooked.model.logic.Map;
import com.nimonscooked.model.logic.Tile;
import com.nimonscooked.model.entities.Chef;
import com.nimonscooked.model.entities.Order;
import com.nimonscooked.model.entities.Projectile;
import com.nimonscooked.model.stations.*;
import com.nimonscooked.model.items.*;
import com.nimonscooked.utils.IngredientType;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

// REVISI: GameDrawingPanel HANYA subklas JPanel (Tidak lagi implements GameObserver)
public class GameDrawingPanel extends JPanel { 

    private GameModel model;
    private AssetManager assets;

    // --- KONFIGURASI LAYOUT ---
    private final int TILE_SIZE = 50;
    private final int SIDEBAR_WIDTH = 220;
    
    // --- PALET WARNA ---
    private final Color SIDEBAR_BG = new Color(208, 232, 184); 
    private final Color CARD_BG_TOP = Color.WHITE;
    private final Color CARD_BG_BOTTOM = new Color(225, 225, 225); 
    private final Color PROGRESS_GREEN = new Color(130, 205, 70);
    private final Color PROGRESS_YELLOW = new Color(255, 200, 0);
    private final Color PROGRESS_RED = new Color(255, 60, 60);
    private final Color GAME_BG = Color.BLACK;

    // --- FONT ---
    private final Font FONT_HUD = new Font("Comic Sans MS", Font.BOLD, 32);
    private final Font FONT_ORDER_TITLE = new Font("Comic Sans MS", Font.BOLD, 12);
    private final Font FONT_MSG = new Font("Comic Sans MS", Font.BOLD, 50);

    // REVISI: Konstruktor diubah agar dipanggil oleh GamePanel (wrapper)
    public GameDrawingPanel(GameModel model) { 
        this.model = model;
        // HILANG: this.model.addObserver(this); -> Observer pindah ke GamePanel (wrapper)
        this.assets = AssetManager.getInstance();
        
        setPreferredSize(new Dimension(14 * TILE_SIZE + SIDEBAR_WIDTH, 10 * TILE_SIZE + 50));
        setBackground(GAME_BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // FIX: Jangan gambar apa-apa jika game dijeda (kecuali latar belakang hitam)
        if (model.isPaused()) {
            return; 
        }

        Graphics2D g2d = (Graphics2D) g;

        // --- AKTIVASI KUALITAS RENDERING TINGGI ---
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        // --- END REVISI ---

        int w = getWidth();
        int h = getHeight();
        int gameAreaWidth = w - SIDEBAR_WIDTH;

        // 1. Gambar Map (Kiri)
        drawGameWorld(g2d, gameAreaWidth, h);
        
        // 2. Gambar Sidebar (Kanan)
        drawSidebar(g2d, gameAreaWidth, 0, SIDEBAR_WIDTH, h);
        
        // 3. Gambar HUD (Koin & Timer)
        drawHUD(g2d, gameAreaWidth, h);

        // 4. Overlay Game Over
        if (model.isGameOver()) {
            drawGameOverScreen(g2d);
        }
    }

    // ==========================================
    // BAGIAN 1: MAP RENDER
    // ==========================================
    
    private void drawGameWorld(Graphics2D g2d, int viewWidth, int viewHeight) {
        AffineTransform oldTransform = g2d.getTransform();
        Shape originalClip = g2d.getClip();

        g2d.setClip(0, 0, viewWidth, viewHeight);

        if (model.getMap() != null) {
            Map map = model.getMap();
            int mapW = map.getCols() * TILE_SIZE;
            int mapH = map.getRows() * TILE_SIZE;

            double scaleX = (double) viewWidth / mapW;
            double scaleY = (double) viewHeight / mapH;
            double scale = Math.min(scaleX, scaleY) * 0.95; 

            int scaledW = (int) (mapW * scale);
            int scaledH = (int) (mapH * scale);
            
            int offsetX = (viewWidth - scaledW) / 2;
            int offsetY = (viewHeight - scaledH) / 2;

            g2d.translate(offsetX, offsetY);
            g2d.scale(scale, scale);

            drawFloor(g2d);
            for (int y = 0; y < map.getRows(); y++) {
                for (int x = 0; x < map.getCols(); x++) {
                    drawTileObject(g2d, x, y);
                }
                drawEntitiesAtRow(g2d, y);
            }
        }

        g2d.setTransform(oldTransform);
        g2d.setClip(originalClip);
    }

    // ==========================================
    // BAGIAN 2: SIDEBAR (ORDER LIST)
    // ==========================================

    private void drawSidebar(Graphics2D g, int x, int y, int w, int h) {
        // Background
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        
        // Shadow Line
        g.setColor(new Color(0,0,0,30));
        g.fillRect(x, 0, 4, h);

        // Header
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 22));
        g.drawString("ORDERS", x + 20, y + 35);

        List<Order> orders = model.getOrderManager().getActiveOrders();
        
        int cardWidth = w - 30; 
        int cardHeight = 100;
        int startX = x + 15;
        int startY = 50; 
        int gap = 15;

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            int cy = startY + (i * (cardHeight + gap));
            drawOrderCard(g, startX, cy, cardWidth, cardHeight, order);
        }
    }

    private void drawOrderCard(Graphics2D g, int x, int y, int w, int h, Order order) {
        RoundRectangle2D roundedShape = new RoundRectangle2D.Float(x, y, w, h, 20, 20);
        Shape oldClip = g.getClip();
        g.setClip(roundedShape); 

        // Background Card
        int topHeight = (int) (h * 0.6);
        g.setColor(CARD_BG_TOP);
        g.fillRect(x, y, w, topHeight);
        g.setColor(CARD_BG_BOTTOM);
        g.fillRect(x, y + topHeight, w, h - topHeight);

        // Nama Order
        g.setColor(Color.BLACK);
        g.setFont(FONT_ORDER_TITLE);
        FontMetrics fm = g.getFontMetrics();
        String title = order.getRecipe().getName(); 
        int titleW = fm.stringWidth(title);
        g.drawString(title, x + (w - titleW)/2, y + 15);

        // Gambar Dish (ASET)
        final int DISH_SIZE = 40; 
        int dishX = x + (w - DISH_SIZE) / 2;
        int dishY = y + 20;
        
        String dishNameKey = order.getRecipe().getName().replace(" ", "_").toLowerCase();
        BufferedImage dishImg = assets.getImage(dishNameKey);
        
        if (dishImg != null) {
            g.drawImage(dishImg, dishX, dishY, DISH_SIZE, DISH_SIZE, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillOval(dishX, dishY, DISH_SIZE, DISH_SIZE);
        }

        // Ingredients (ASET ICON)
        List<IngredientType> ingredients = order.getRecipe().getRequiredIngredients();
        int ingSize = 22;
        int ingGap = 8;
        int totalIngWidth = (ingredients.size() * ingSize) + ((ingredients.size() - 1) * ingGap);
        int startIngX = x + (w - totalIngWidth) / 2;
        int ingY = y + topHeight + 8; 

        for (int i = 0; i < ingredients.size(); i++) {
            IngredientType type = ingredients.get(i);
            String iconKey = "icon_" + type.toString(); 
            BufferedImage iconImg = assets.getImage(iconKey);

            int currentX = startIngX + (i * (ingSize + ingGap));

            if (iconImg != null) {
                g.drawImage(iconImg, currentX, ingY, ingSize, ingSize, null);
            } else {
                g.setColor(Color.WHITE);
                g.fillOval(currentX, ingY, ingSize, ingSize);
                g.setColor(getColorForIngredient(type));
                g.fillOval(currentX + 5, ingY + 5, ingSize - 10, ingSize - 10);
            }
        }

        // Progress Bar
        int barHeight = 8;
        int barY = y + h - barHeight;
        float progress = (float) order.getTimeLeft() / order.getMaxTime();
        int barWidth = (int) (w * progress);

        Color barColor = PROGRESS_GREEN;
        if (progress < 0.25f) barColor = PROGRESS_RED;
        else if (progress < 0.5f) barColor = PROGRESS_YELLOW;

        g.setColor(barColor);
        g.fillRect(x, barY, barWidth, barHeight);

        g.setClip(oldClip);
    }

    // ==========================================
    // BAGIAN 3: HUD (KOIN & TIMER)
    // ==========================================

    private void drawHUD(Graphics2D g, int gameAreaWidth, int fullHeight) {
        int iconSize = 75;
        g.setFont(FONT_HUD);
        FontMetrics fm = g.getFontMetrics();

        // --- 1. KOIN (Kiri) ---
        int coinX = 20;
        int coinY = fullHeight - 90;
        
        BufferedImage coinImg = assets.getImage("ui_coin");
        if (coinImg != null) {
            g.drawImage(coinImg, coinX, coinY, iconSize, iconSize, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillOval(coinX, coinY, iconSize, iconSize);
        }

        String scoreStr = String.valueOf(model.getScore());
        int textH = fm.getAscent();
        
        int scoreTextX = coinX + iconSize - 25; 
        int scoreTextY = coinY + (iconSize + textH) / 2 - 8;

        drawOutlinedText(g, scoreStr, scoreTextX, scoreTextY, Color.WHITE, new Color(200, 100, 0));

        // --- 2. TIMER (Kanan) ---
        int timerX = gameAreaWidth - 90; 
        int timerY = fullHeight - 90;

        BufferedImage timerImg = assets.getImage("ui_timer");
        if (timerImg != null) {
            g.drawImage(timerImg, timerX, timerY, iconSize, iconSize, null);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(timerX, timerY, iconSize, iconSize);
        }

        String timeStr = formatTime(model.getGameDuration());
        int timeW = fm.stringWidth(timeStr);
        
        int timerTextX = timerX - timeW + 25;
        int timerTextY = timerY + (iconSize + textH) / 2 - 8;
        
        Color timerColor = (model.getGameDuration() <= 15) ? Color.RED : Color.WHITE;
        drawOutlinedText(g, timeStr, timerTextX, timerTextY, timerColor, new Color(200, 100, 0));
    }

    private void drawOutlinedText(Graphics g, String text, int x, int y, Color c, Color outline) {
        g.setColor(outline);
        // Outline Tebal (9 titik)
        for(int i=-2; i<=2; i++) {
            for(int j=-2; j<=2; j++) {
                if(i!=0 || j!=0) g.drawString(text, x+i, y+j);
            }
        }
        g.setColor(c);
        g.drawString(text, x, y);
    }

    // ==========================================
    // BAGIAN 4: UTILITIES
    // ==========================================

    private void drawFloor(Graphics g) {
        Map map = model.getMap();
        for (int y = 0; y < map.getRows(); y++) {
            for (int x = 0; x < map.getCols(); x++) {
                drawImageOrRect(g, "floor", new Color(210, 180, 140), x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }
    
    private void drawTileObject(Graphics g, int x, int y) {
        Tile tile = model.getMap().getTile(x, y);
        int px = x * TILE_SIZE;
        int py = y * TILE_SIZE;
        int blockH = 15; // Efek 3D tembok

        if (tile.isWall() || tile.getStation() != null) {
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect(px, py + TILE_SIZE - blockH, TILE_SIZE, blockH);

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

            int drawY = py - blockH;
            if (assets.getImage(imgKey) == null) {
                g.setColor(color.darker());
                g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            }
            drawImageOrRect(g, imgKey, color, px, drawY, TILE_SIZE, TILE_SIZE);
            drawStationDetails(g, tile, px, drawY);
        } else if (tile.getGroundItem() != null) {
            drawItem(g, tile.getGroundItem(), px + 15, py + 15, 20);
        }
    }
    
    private void drawStationDetails(Graphics g, Tile tile, int px, int py) {
        if (tile.getStation() == null) return;
        Station s = tile.getStation();

        if (s instanceof IngredientStorage) {
            IngredientStorage is = (IngredientStorage) s;
            if (is.getItemOnTop() != null) drawItem(g, is.getItemOnTop(), px + 10, py + 5, 30);
        } else if (s instanceof CookingStation) {
            CookingStation cs = (CookingStation) s;
            if (cs.getDevice() != null && cs.getDevice() instanceof Item) {
                drawItem(g, (Item) cs.getDevice(), px + 10, py + 5, 30);
            }
        } else if (s instanceof CuttingStation) {
            if (((CuttingStation) s).getItem() != null) drawItem(g, ((CuttingStation) s).getItem(), px + 5, py + 5, 40);
        } else if (s instanceof AssemblyStation) {
            if (((AssemblyStation) s).getStoredItem() != null) drawItem(g, ((AssemblyStation) s).getStoredItem(), px + 5, py + 5, 40);
        } else if (s instanceof WashingStation) {
            WashingStation ws = (WashingStation) s;
            if (ws.getDirtyCount() > 0) {
                drawImageOrRect(g, "plate", Color.DARK_GRAY, px + 5, py + 20, 20, 20);
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString("" + ws.getDirtyCount(), px + 5, py + 20);
            }
            if (ws.getCleanCount() > 0) {
                drawImageOrRect(g, "plate", Color.WHITE, px + 25, py + 20, 20, 20);
            }
            if (ws.isWashing()) {
                g.setColor(Color.CYAN);
                g.fillRect(px + 5, py - 10, (int) (40 * ws.getProgress() / 100.0), 6);
            }
        }
    }

    private void drawEntitiesAtRow(Graphics2D g, int rowY) {
        for (Chef chef : model.getChefs()) {
            double cY = chef.getWorldY();
            if (cY >= rowY - 0.5 && cY < rowY + 0.5) {
                drawChefSmooth(g, chef);
            }
        }
        if (model.getProjectiles() != null) {
            for (Projectile p : model.getProjectiles()) {
                if (Math.round(p.getPosition().getY()) == rowY) {
                    int x = p.getPosition().getX() * TILE_SIZE;
                    int y = (p.getPosition().getY() * TILE_SIZE) - 20;
                    drawItem(g, p.getItem(), x + 15, y + 15, 20);
                }
            }
        }
    }

    private void drawChefSmooth(Graphics2D g, Chef chef) {
        int x = (int) (chef.getWorldX() * TILE_SIZE);
        int y = (int) (chef.getWorldY() * TILE_SIZE);
        int drawY = y - 15;
        final int HELD_ITEM_SIZE = 30; // Ukuran item yang dibawa Chef
        
        if (chef == model.getActiveChef()) {
            g.setColor(new Color(255, 255, 0, 100));
            g.fillOval(x, y + 10, TILE_SIZE, TILE_SIZE / 3);
        }
        drawImageOrRect(g, "chef", Color.GREEN, x + 5, drawY, TILE_SIZE - 10, TILE_SIZE - 10);
        
        if (!chef.getInventory().isEmpty()) {
            drawItem(g, chef.getInventory().getItem(), x + 10, drawY - 20, HELD_ITEM_SIZE);
        }
    }

    private void drawItem(Graphics g, Item item, int x, int y, int size) {
        if (item == null) return;
        if (item instanceof Plate) {
            Plate p = (Plate) item;
            drawImageOrRect(g, "plate", p.isClean() ? Color.WHITE : Color.DARK_GRAY, x, y, size, size);
            if (!p.isEmpty() && p.getContents().get(0) instanceof Ingredient) {
                Ingredient ing = (Ingredient) p.getContents().get(0);
                g.setColor(getColorForIngredient(ing.getType()));
                g.fillOval(x + size / 4, y + size / 4, size / 2, size / 2);
            }
        } else if (item instanceof KitchenUtensil) {
            String imgName = (item instanceof BoilingPot) ? "pot" : "pan";
            drawImageOrRect(g, imgName, Color.GRAY, x, y, size, size);
            KitchenUtensil u = (KitchenUtensil) item;
            if (!u.isEmpty() && u.getContents().get(0) instanceof Ingredient) {
                Ingredient ing = (Ingredient) u.getContents().get(0);
                if (u.isCooking()) {
                    g.setColor(u.getCookingProgress() > 100 ? Color.RED : Color.GREEN);
                    g.fillRect(x, y - 5, (int)(size * (Math.min(u.getCookingProgress(),200)/100.0)), 4);
                }
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString(ing.getState().getName().substring(0, 2), x + 5, y + 10);
            }
        } else if (item instanceof Ingredient) {
            String ingredientKey = "item_" + ((Ingredient) item).getType().toString();
            BufferedImage ingredientImg = assets.getImage(ingredientKey);
            
            if (ingredientImg != null) {
                drawImageOrRect(g, ingredientKey, Color.RED, x, y, size, size);
            } else {
                drawImageOrRect(g, "item_default", Color.RED, x, y, size, size);
            }

            Ingredient ing = (Ingredient) item;
            g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString(ing.getState().getName().substring(0, 3), x, y + size + 10);
        }
    }

    private Color getColorForIngredient(IngredientType type) {
        switch (type) {
            case TOMATO: return Color.RED;
            case MEAT: return new Color(139, 69, 19);
            case PASTA: return Color.YELLOW;
            case FISH: return Color.BLUE;
            case SHRIMP: return Color.PINK;
            default: return Color.GRAY;
        }
    }

    private void drawImageOrRect(Graphics g, String imageName, Color fallbackColor, int x, int y, int w, int h) {
        BufferedImage img = assets.getImage(imageName);
        if (img != null) {
            g.drawImage(img, x, y, w, h, null);
        } else {
            g.setColor(fallbackColor);
            g.fillRect(x, y, w, h);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, w, h);
        }
    }

    private void drawGameOverScreen(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, w, h);
        g.setFont(FONT_MSG);
        String msg = model.isStagePassed() ? "STAGE CLEARED!" : "GAME OVER";
        g.setColor(model.isStagePassed() ? Color.GREEN : Color.RED);
        drawCenteredString(g, msg, w, h / 2 - 20);
        g.setColor(Color.WHITE);
        g.setFont(FONT_HUD);
        drawCenteredString(g, "Score: " + model.getScore(), w, h / 2 + 40);
    }
    
    private void drawCenteredString(Graphics g, String text, int width, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
    
    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // HILANG: public void update(Object gameState) { repaint(); }
}