package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;
import connection.DatabaseConnection;
import tampilan.util.UIStyle;

public class TampilanHomeManual extends JPanel {

    private int totalAset = 0;
    private int bookingAktif = 0;
    private double pendapatanHariIni = 0;
    private JPanel statsPanel;
    private JPanel content; // Make content a field for easy access

    public TampilanHomeManual() {
        // Ambil data statistik dari database
        loadStatisticsData();
        initComponents();
    }

    private void loadStatisticsData() {
    // Beri umpan balik saat data dimuat di konstruktor
        setStatsLoading(true);

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Object[] stats = new Object[3];
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Hitung total aset
                    String sqlTotalAset = "SELECT COUNT(*) as total FROM aset";
                    try (PreparedStatement pst = conn.prepareStatement(sqlTotalAset); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) stats[0] = rs.getInt("total");
                    }

                    // Hitung booking aktif
                    String sqlBookingAktif = "SELECT COUNT(*) as total FROM booking WHERE status = 'confirmed' AND DATE(created_at) = CURDATE()";
                    try (PreparedStatement pst = conn.prepareStatement(sqlBookingAktif); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) stats[1] = rs.getInt("total");
                    }

                    // Hitung pendapatan hari ini
                    String sqlPendapatan = "SELECT COALESCE(SUM(total_harga), 0) as total FROM booking WHERE status = 'completed' AND DATE(created_at) = CURDATE()";
                    try (PreparedStatement pst = conn.prepareStatement(sqlPendapatan); ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) stats[2] = rs.getDouble("total");
                    }
                }
                return stats;
            }

            @Override
            protected void done() {
                try {
                    Object[] stats = get();
                    totalAset = (int) stats[0];
                    bookingAktif = (int) stats[1];
                    pendapatanHariIni = (double) stats[2];

                    // Panggil refresh untuk memperbarui UI setelah data didapat
                    updateStatsUI();

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TampilanHomeManual.this, "Gagal memuat data statistik: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setStatsLoading(false);
                }
            }
        }.execute();
    }

    private void initComponents() {
        setBackground(UIStyle.BACKGROUND);
        setLayout(new BorderLayout(0, 0));

        // Header dengan card style modern
        UIStyle.RoundedPanel headerPanel = new UIStyle.RoundedPanel(15);
        headerPanel.setLayout(new BorderLayout(10, 10));
        headerPanel.setBackground(UIStyle.CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel logo = new JLabel(resizeIcon("/img/icon.png", 60, 60));
        JLabel title = new JLabel("BYEBELI EXPERIENCE");
        title.setFont(UIStyle.fontBold(26));
        title.setForeground(UIStyle.PRIMARY);
        
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        titleContainer.setBackground(UIStyle.CARD_BG);
        titleContainer.add(logo);
        titleContainer.add(title);
        
        headerPanel.add(titleContainer, BorderLayout.WEST);

        // Konten Tengah - menggunakan JScrollPane untuk memastikan semua konten terlihat
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIStyle.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel image = new JLabel(resizeIcon("/img/png1.png", 350, 180));
        image.setAlignmentX(Component.CENTER_ALIGNMENT);
        image.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel welcome = new JLabel("Welcome to Byebeli Experience Admin System");
        welcome.setFont(UIStyle.fontBold(22));
        welcome.setForeground(UIStyle.PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Panel untuk info area dengan style card
        UIStyle.RoundedPanel infoPanel = new UIStyle.RoundedPanel(15);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBackground(UIStyle.CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        infoPanel.setMaximumSize(new Dimension(800, 80));
        
        JTextArea infoArea = new JTextArea(
            "Have a great day managing your assets and bookings!"
        );
        infoArea.setWrapStyleWord(true);
        infoArea.setLineWrap(true);
        infoArea.setEditable(false);
        infoArea.setFont(UIStyle.fontRegular(14));
        infoArea.setForeground(UIStyle.TEXT);
        infoArea.setBackground(UIStyle.CARD_BG);
        infoArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        infoPanel.add(infoArea, BorderLayout.CENTER);
        
        // Stats cards panel
        statsPanel = createStatsPanel();
        
        content.add(image);
        content.add(welcome);
        content.add(infoPanel);
        content.add(Box.createRigidArea(new Dimension(0, 30)));
        content.add(statsPanel);

        // Refresh button
        JButton refreshButton = UIStyle.modernButton("Refresh Data");
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshButton.setMaximumSize(new Dimension(200, 40));
        refreshButton.addActionListener(e -> refreshData());
        
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(refreshButton);
        
        // Add some flexible space at the bottom to ensure the button is visible
        content.add(Box.createVerticalGlue());

        // Create a scroll pane to ensure all content is accessible
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Footer
        UIStyle.RoundedPanel footer = new UIStyle.RoundedPanel(0, false);
        footer.setBackground(UIStyle.CARD_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel footerText = new JLabel("© 2025 Byebeli Experience Admin System v1.0.0 | Internal Use Only");
        footerText.setFont(UIStyle.fontRegular(12));
        footerText.setForeground(UIStyle.TEXT_LIGHT);
        footer.add(footerText);

        // Layout utama
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
    
    private void refreshData() {
        loadStatisticsData();
}

    private void updateStatsUI() {
        // Hapus panel statistik lama dan buat yang baru dengan data terkini
        if (statsPanel != null) {
            content.remove(statsPanel);
        }
        statsPanel = createStatsPanel();
        content.add(statsPanel, 4); // Index 4 adalah posisi stats panel
        content.revalidate();
        content.repaint();
    }

// Tambahkan metode baru untuk menampilkan status loading
    private void setStatsLoading(boolean isLoading) {
        if (isLoading && statsPanel != null) {
            // Tampilkan teks "Memuat..." pada setiap kartu statistik
            ((JLabel) ((JPanel) ((UIStyle.RoundedPanel) statsPanel.getComponent(0)).getComponent(0)).getComponent(2)).setText("Memuat...");
            ((JLabel) ((JPanel) ((UIStyle.RoundedPanel) statsPanel.getComponent(1)).getComponent(0)).getComponent(2)).setText("Memuat...");
            ((JLabel) ((JPanel) ((UIStyle.RoundedPanel) statsPanel.getComponent(2)).getComponent(0)).getComponent(2)).setText("Memuat...");
        }
        // Nonaktifkan tombol refresh saat sedang memuat
        // (Asumsikan Anda memiliki referensi ke tombol refresh)
        // refreshButton.setEnabled(!isLoading); 
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(UIStyle.BACKGROUND);
        statsPanel.setMaximumSize(new Dimension(800, 120));
        
        // Format angka untuk pendapatan
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        
        // Stat Card 1 - Total Aset
        UIStyle.RoundedPanel statCard1 = createStatCard(
            "Total Aset", 
            String.valueOf(totalAset), 
            UIStyle.PRIMARY, 
            "💻"
        );
        
        // Stat Card 2 - Booking Aktif
        UIStyle.RoundedPanel statCard2 = createStatCard(
            "Booking Aktif", 
            String.valueOf(bookingAktif), 
            UIStyle.SUCCESS_COLOR, 
            "📅"
        );
        
        // Stat Card 3 - Pendapatan Hari Ini
        UIStyle.RoundedPanel statCard3 = createStatCard(
            "Pendapatan Hari Ini", 
            currencyFormat.format(pendapatanHariIni), 
            UIStyle.ACCENT, 
            "💰"
        );
        
        statsPanel.add(statCard1);
        statsPanel.add(statCard2);
        statsPanel.add(statCard3);
        
        return statsPanel;
    }
    
    private UIStyle.RoundedPanel createStatCard(String title, String value, Color color, String emoji) {
        UIStyle.RoundedPanel card = new UIStyle.RoundedPanel(15);
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        emojiLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.fontRegular(12));
        titleLabel.setForeground(UIStyle.TEXT_LIGHT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIStyle.fontBold(20));
        valueLabel.setForeground(color);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(5, 5));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(emojiLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(valueLabel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }

    private Icon resizeIcon(String path, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(getClass().getResource(path));
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException | IllegalArgumentException e) {
            // Fallback menggunakan emoji jika gambar tidak ditemukan
            JLabel fallbackLabel = new JLabel("🎮");
            fallbackLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, width/2));
            return new ImageIcon(createImageFromComponent(fallbackLabel, width, height));
        }
    }
    
    private Image createImageFromComponent(JComponent component, int width, int height) {
        component.setSize(width, height);
        component.setPreferredSize(new Dimension(width, height));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        component.printAll(g2);
        g2.dispose();
        return image;
    }
}