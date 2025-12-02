package tampilan.utama;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

import connection.DatabaseConnection;
import tampilan.util.UIStyle;
import tampilan.components.RoundedPanel;

public class TampilanHomeManual extends JPanel {

    private int totalAset = 0;
    private int bookingAktif = 0;
    private double pendapatanHariIni = 0;

    private JPanel content;
    private JPanel statsPanel;

    public TampilanHomeManual() {
        initComponents();
        loadStatisticsData();
    }

    private void loadStatisticsData() {
        // Tampilkan indikator loading
        setStatsLoading(true);

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Object[] stats = new Object[3];
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // 1. Total Aset
                    String sqlAset = "SELECT COUNT(*) FROM aset";
                    try (PreparedStatement pst = conn.prepareStatement(sqlAset);
                            ResultSet rs = pst.executeQuery()) {
                        if (rs.next())
                            stats[0] = rs.getInt(1);
                    }

                    // 2. Booking Aktif (Booking + PlayAtHome)
                    String sqlBooking = "SELECT " +
                            "(SELECT COUNT(*) FROM booking WHERE status IS NULL OR status NOT IN ('completed', 'cancelled')) + "
                            +
                            "(SELECT COUNT(*) FROM playathome WHERE status = 'aktif') as total_aktif";
                    try (PreparedStatement pst = conn.prepareStatement(sqlBooking);
                            ResultSet rs = pst.executeQuery()) {
                        if (rs.next())
                            stats[1] = rs.getInt("total_aktif");
                    }

                    // 3. Pendapatan Hari Ini
                    String sqlPendapatan = "SELECT COALESCE(SUM(total_harga), 0) as total FROM booking WHERE DATE(tanggal) = CURDATE() AND status != 'cancelled'";
                    try (PreparedStatement pst = conn.prepareStatement(sqlPendapatan);
                            ResultSet rs = pst.executeQuery()) {
                        if (rs.next())
                            stats[2] = rs.getDouble("total");
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
                    UIStyle.showErrorMessage(TampilanHomeManual.this, "Gagal memuat data statistik: " + e.getMessage());
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
        RoundedPanel headerPanel = new RoundedPanel(15);
        headerPanel.setLayout(new BorderLayout(5, 5));
        headerPanel.setBackground(UIStyle.CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel logo = new JLabel(resizeIcon("/img/iconloginn.png", 50, 50));
        JLabel title = new JLabel("CONSOLERENT INDONESIA");
        title.setFont(UIStyle.fontBold(24));
        title.setForeground(UIStyle.PRIMARY);

        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleContainer.setOpaque(false);
        titleContainer.add(logo);
        titleContainer.add(title);

        headerPanel.add(titleContainer, BorderLayout.WEST);

        // Konten Tengah - Menggunakan GridBagLayout untuk centering tanpa scroll
        content = new JPanel(new GridBagLayout());
        content.setBackground(UIStyle.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Image (Reduced size for better fit)
        JLabel image = new JLabel(resizeIcon("/img/icon.png", 250, 130));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(image, gbc);

        // Welcome Text
        gbc.gridy++;
        JLabel welcome = new JLabel("Welcome to Consolerent Indonesia Admin System");
        welcome.setFont(UIStyle.fontBold(24));
        welcome.setForeground(UIStyle.PRIMARY);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(welcome, gbc);

        // Info Panel
        gbc.gridy++;
        RoundedPanel infoPanel = new RoundedPanel(15);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBackground(UIStyle.CARD_BG);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Limit width of info panel
        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(infoPanel, BorderLayout.CENTER);

        // Use JLabel for better centering if text is short
        JLabel infoLabel = new JLabel("Have a great day managing your assets and bookings!", SwingConstants.CENTER);
        infoLabel.setFont(UIStyle.fontRegular(16));
        infoLabel.setForeground(UIStyle.TEXT);

        infoPanel.add(infoLabel, BorderLayout.CENTER);
        content.add(infoWrapper, gbc);

        // Stats Panel
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 20, 10); // More space for stats
        statsPanel = createStatsPanel();
        content.add(statsPanel, gbc);

        // Refresh Button
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        JButton refreshButton = UIStyle.modernButton("Refresh Data");
        refreshButton.setPreferredSize(new Dimension(180, 45));
        refreshButton.addActionListener(e -> refreshData());
        content.add(refreshButton, gbc);

        // Footer
        RoundedPanel footer = new RoundedPanel(0, false);
        footer.setBackground(UIStyle.CARD_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel footerText = new JLabel("© 2025 Consolerent Indonesia Admin System v1.0.0 | Internal Use Only",
                SwingConstants.CENTER);
        footerText.setFont(UIStyle.fontRegular(12));
        footerText.setForeground(UIStyle.TEXT_LIGHT);
        footer.add(footerText);

        // Layout utama
        add(headerPanel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER); // Add content directly, no ScrollPane
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshData() {
        // Cukup panggil loadStatisticsData yang sekarang sudah asinkron
        loadStatisticsData();
    }

    private void updateStatsUI() {
        // Hapus panel statistik lama dan buat yang baru dengan data terkini
        if (statsPanel != null) {
            content.remove(statsPanel);
        }
        statsPanel = createStatsPanel();

        // Re-add stats panel at the correct position (index 3 based on GridBagLayout
        // order)
        // Note: With GridBagLayout, we can't just add at index. We need to use
        // constraints.
        // So we remove everything and re-init or just update components inside
        // statsPanel.
        // Easier approach: Just rebuild the statsPanel content if possible, or re-add
        // with constraints.

        // Better approach for GridBagLayout: remove old, add new with same constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3; // Stats panel is at y=3
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        content.add(statsPanel, gbc);
        content.revalidate();
        content.repaint();
    }

    private void setStatsLoading(boolean isLoading) {
        if (isLoading && statsPanel != null) {
            // Tampilkan teks "Memuat..." pada setiap kartu statistik
            try {
                // Accessing components deeply nested is fragile, but kept for compatibility
                // with existing structure
                // Ideally, StatCard should be a custom component class with a setLoading method
                Component[] cards = statsPanel.getComponents();
                for (Component c : cards) {
                    if (c instanceof RoundedPanel) {
                        RoundedPanel card = (RoundedPanel) c;
                        JPanel contentPanel = (JPanel) card.getComponent(0);
                        JLabel valueLabel = (JLabel) contentPanel.getComponent(2); // Index 2 is valueLabel
                        valueLabel.setText("Memuat...");
                    }
                }
            } catch (Exception e) {
                // Tangani jika komponen belum sepenuhnya terinisialisasi
                // System.out.println("Gagal set teks loading: " + e.getMessage());
            }
        }
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(UIStyle.BACKGROUND);
        // statsPanel.setMaximumSize(new Dimension(800, 120)); // Removed max size
        // constraint

        // Format angka untuk pendapatan
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

        // Stat Card 1 - Total Aset
        RoundedPanel statCard1 = createStatCard(
                "Total Aset",
                String.valueOf(totalAset),
                UIStyle.PRIMARY,
                "💻");

        // Stat Card 2 - Booking Aktif
        RoundedPanel statCard2 = createStatCard(
                "Booking Aktif",
                String.valueOf(bookingAktif),
                UIStyle.SUCCESS_COLOR,
                "📅");

        // Stat Card 3 - Pendapatan Hari Ini
        RoundedPanel statCard3 = createStatCard(
                "Pendapatan Hari Ini",
                currencyFormat.format(pendapatanHariIni),
                UIStyle.SECONDARY_LIGHT,
                "💰");

        statsPanel.add(statCard1);
        statsPanel.add(statCard2);
        statsPanel.add(statCard3);

        return statsPanel;
    }

    private RoundedPanel createStatCard(String title, String value, Color color, String emoji) {
        RoundedPanel card = new RoundedPanel(15);
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
            fallbackLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, width / 2));
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