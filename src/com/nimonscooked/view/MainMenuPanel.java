package com.nimonscooked.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel {

    private AssetManager assets;
    
    // Tombol-tombol
    private JButton startButton;
    private JButton helpButton;
    private JButton musicButton;
    private JButton exitButton;

    // State Musik Sederhana (Karena SoundManager belum ada)
    private boolean isMusicOn = true;

    public MainMenuPanel(Runnable goToStageSelect) {
        this.assets = AssetManager.getInstance();
        
        // Gunakan GridBagLayout untuk menaruh tombol di sisi Kiri (West)
        setLayout(new GridBagLayout());
        setOpaque(false); // Agar paintComponent bisa menggambar background

        initLayout(goToStageSelect);
    }

private void initLayout(Runnable goToStageSelect) {
        // --- 1. SETUP PANEL UTAMA (Wrapper) ---
        // Panel ini akan menampung Logo (Atas) dan Tombol (Bawah)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS)); 
        mainContent.setOpaque(false); // Transparan

        // // --- 2. SETUP LOGO (Agar ikut ke tengah) ---
        // // Kita masukkan logo sebagai komponen, bukan digambar manual di paintComponent
        // JLabel logoLabel = new JLabel();
        // BufferedImage logoImg = assets.getImage("menu_logo");
        // if (logoImg != null) {
        //     // Resize logo agar proporsional (Misal lebar 500px)
        //     int targetWidth = 500; 
        //     int targetHeight = (int) ((double) logoImg.getHeight() / logoImg.getWidth() * targetWidth);
        //     Image scaledLogo = logoImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        //     logoLabel.setIcon(new ImageIcon(scaledLogo));
        // } else {
        //     // Fallback jika gambar logo tidak ada
        //     logoLabel.setText("NIMONSCOOKED");
        //     logoLabel.setFont(new Font("Arial", Font.BOLD, 40));
        //     logoLabel.setForeground(Color.ORANGE);
        // }
        // logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Rata Tengah Horizontal
        
        // // Masukkan Logo ke panel utama
        // mainContent.add(logoLabel);
        // mainContent.add(Box.createRigidArea(new Dimension(0, 30))); // Jarak Logo ke Tombol

        // --- 3. SETUP TOMBOL ---
        startButton = createImageButton("btn_start", e -> goToStageSelect.run());
        helpButton = createImageButton("btn_help", e -> showHelpDialog());
        exitButton = createImageButton("btn_exit", e -> System.exit(0));

        // Masukkan tombol ke panel utama (langsung, tidak perlu panel buttonContainer terpisah lagi)
        mainContent.add(startButton);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15))); // Spasi
        mainContent.add(helpButton);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15))); // Spasi
        mainContent.add(exitButton);

        // --- 4. PENEMPATAN KE LAYAR (GridBagLayout) ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // KUNCI RESPONSIF: 
        // weightx/y = 1.0 artinya panel akan mengambil seluruh ruang kosong di sekitarnya
        // anchor = CENTER artinya konten diletakkan di tengah ruang tersebut
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER; 
        
        // Hapus Insets besar yang memaksa ke kiri/atas. Cukup 0 atau secukupnya.
        gbc.insets = new Insets(0, 0, 0, 0); 

        add(mainContent, gbc);
    }

    /**
     * Helper untuk membuat tombol berbasis gambar tanpa border standar Swing.
     */
    private JButton createImageButton(String assetKey, java.awt.event.ActionListener action) {
        JButton btn = new JButton();
        
        BufferedImage img = assets.getImage(assetKey);
        if (img != null) {
            // --- LOGIKA RESIZE (BARU) ---
            
            // 1. Tentukan Lebar yang diinginkan (Misal 200px atau 250px)
            int targetWidth = 300; 
            
            // 2. Hitung Tinggi secara proporsional (Aspect Ratio) agar gambar tidak gepeng
            int targetHeight = (int) ((double) img.getHeight() / img.getWidth() * targetWidth);

            // 3. Lakukan Scaling
            Image scaledImg = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            
            // 4. Set Icon menggunakan gambar yang sudah dikecilkan
            btn.setIcon(new ImageIcon(scaledImg));
            
            // 5. Set Ukuran Tombol sesuai hasil resize
            btn.setPreferredSize(new Dimension(targetWidth, targetHeight));
            
            // -----------------------------

            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
        } else {
            // Fallback
            btn.setText("MISSING: " + assetKey);
            btn.setBackground(Color.RED);
            btn.setPreferredSize(new Dimension(200, 60));
        }

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        // PENTING: Gunakan alignment CENTER agar rapi di tengah layout baru
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); 

        // Efek Hover Sederhana
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setLocation(btn.getX(), btn.getY() - 2); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setLocation(btn.getX(), btn.getY() + 2); }
        });

        return btn;
    }

    private void toggleMusic() {
        isMusicOn = !isMusicOn;
        
        // Ganti Icon Tombol
        String key = isMusicOn ? "btn_music_on" : "btn_music_off";
        BufferedImage img = assets.getImage(key);
        if (img != null) {
            musicButton.setIcon(new ImageIcon(img));
        }

        // TODO: Panggil SoundManager nanti di sini
        // SoundManager.getInstance().setMute(!isMusicOn);
        System.out.println("Music Toggled: " + (isMusicOn ? "ON" : "OFF"));
    }

    private void showHelpDialog() {
        // 1. Ambil gambar dari AssetManager
        BufferedImage helpImg = assets.getImage("menu_help");

        if (helpImg != null) {
            // 2. Opsional: Resize jika gambar terlalu besar untuk layar
            // Misal kita batasi tinggi maksimal 600px agar muat di laptop
            int maxHeight = 450;
            if (helpImg.getHeight() > maxHeight) {
                int newWidth = (int) ((double) helpImg.getWidth() / helpImg.getHeight() * maxHeight);
                Image scaled = helpImg.getScaledInstance(newWidth, maxHeight, Image.SCALE_SMOOTH);
                helpImg = new BufferedImage(newWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics g = helpImg.createGraphics();
                g.drawImage(scaled, 0, 0, null);
                g.dispose();
            }

            // 3. Tampilkan Gambar
            ImageIcon icon = new ImageIcon(helpImg);
            
            JOptionPane.showMessageDialog(this, 
                null,                  // Pesan teks dikosongkan
                "How to Play",         // Judul Dialog
                JOptionPane.PLAIN_MESSAGE, 
                icon);                 // Gambar dijadikan konten utama
        } else {
            // 4. Fallback: Jika gambar "menu_help" belum ada, tampilkan teks lama
            JOptionPane.showMessageDialog(this, 
                "<html><body style='width: 300px; color: black;'>" +
                "<h2>👨‍🍳 How to Play</h2>" +
                "<p>Serve orders before time runs out!</p><br>" +
                "<b>Controls:</b><br>" +
                "WASD: Move | SPACE: Chop/Cook/Wash | F: Throw | SHIFT: Dash" +
                "</body></html>", 
                "Chef's Guide", JOptionPane.PLAIN_MESSAGE);
        }
    }

@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Ambil ukuran layar saat ini (Dynamic saat di-resize)
        int w = getWidth();
        int h = getHeight();

        // 1. Gambar Background
        BufferedImage bg = assets.getImage("menu_bg");
        if (bg != null) {
            g2d.drawImage(bg, 0, 0, w, h, null);
        } else {
            g2d.setColor(new Color(50, 50, 80));
            g2d.fillRect(0, 0, w, h);
        }

        // 2. Gambar Logo
        BufferedImage logo = assets.getImage("menu_logo");
        if (logo != null) {
            // Tentukan ukuran logo
            int logoW = 600; 
            // Opsional: Jika layar kecil, kecilkan logo agar tidak kepotong
            if (w < 650) logoW = w - 50; 
            
            int logoH = (int) ((double) logo.getHeight() / logo.getWidth() * logoW);
            
            // --- RUMUS TENGAH (CENTERING) ---
            int x = (w - logoW) / 2;  // (Lebar Layar - Lebar Logo) bagi 2
            int y = 30;               // Jarak dari atas (Margin Top)
            
            // Gambar dengan koordinat x yang sudah dihitung
            g2d.drawImage(logo, x, y, logoW, logoH, null);
            
        } else {
            // Fallback Text Title (Juga harus ditengahkan)
            String title = "NIMONSCOOKED";
            Font font = new Font("Comic Sans MS", Font.BOLD, 60);
            g2d.setFont(font);
            g2d.setColor(Color.ORANGE);
            
            // Hitung lebar teks agar pas di tengah
            FontMetrics metrics = g2d.getFontMetrics(font);
            int textX = (w - metrics.stringWidth(title)) / 2;
            int textY = 100; // Jarak dari atas
            
            g2d.drawString(title, textX, textY);
        }
    }
}