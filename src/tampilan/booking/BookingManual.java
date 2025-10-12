package tampilan.booking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import connection.DatabaseConnection;
import tampilan.util.UIStyle;

public class BookingManual extends JPanel {

    private JPanel headerPanel, formPanel;
    private JLabel titleLabel;
    private JTextField deviceField;
    private JPanel deviceFieldPanel;
    private JComboBox<String> experienceDropdown;
    private JTextField jumlahField;
    private JComboBox<String> jamDropdown;
    private JDateChooser tanggalField;
    private JComboBox<String> satuanDurasi;
    private JTextField durasiField;
    private JTextField namaField, instagramField, noHpField, emailField;
    private JLabel totalHargaLabel;

    public BookingManual() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        initHeader();
        initForm();

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        loadAvailableExperiences();
    }

    private void initHeader() {
        headerPanel = new UIStyle.RoundedPanel(20, false);
        headerPanel.setLayout(new GridBagLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(1024, 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        titleLabel = new JLabel("FORM BOOKING EXPERIENCE");
        titleLabel.setFont(UIStyle.fontBold(28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel);
    }

    private void initForm() {
        formPanel = new UIStyle.RoundedPanel(25);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(UIStyle.CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Title for form section
        JLabel formTitle = new JLabel("Data Pemesan");
        formTitle.setFont(UIStyle.fontBold(20));
        formTitle.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(formTitle, gbc);
        gbc.gridwidth = 1;
        
        namaField = new JTextField(20);
        UIStyle.styleTextField(namaField);
        addFormRow("Nama Lengkap:", namaField, gbc, row++);
        
        instagramField = new JTextField(20);
        UIStyle.styleTextField(instagramField);
        addFormRow("Akun Instagram:", instagramField, gbc, row++);
        
        noHpField = new JTextField(20);
        UIStyle.styleTextField(noHpField);
        addFormRow("Nomor HP:", noHpField, gbc, row++);
        
        emailField = new JTextField(20);
        UIStyle.styleTextField(emailField);
        addFormRow("Email:", emailField, gbc, row++);
        
        // Booking details title
        JLabel bookingTitle = new JLabel("Detail Booking");
        bookingTitle.setFont(UIStyle.fontBold(20));
        bookingTitle.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(bookingTitle, gbc);
        gbc.gridwidth = 1;
        
        tanggalField = new JDateChooser();
        tanggalField.setFont(UIStyle.fontRegular(14));
        tanggalField.setPreferredSize(new Dimension(200, 40));
        tanggalField.setBackground(Color.WHITE);
        tanggalField.setForeground(UIStyle.TEXT);
        addFormRow("Tanggal Booking:", tanggalField, gbc, row++);
        
        jamDropdown = new JComboBox<>(new String[] {
            "10:00", "11:00", "12:00", "13:00", "14:00",
            "15:00", "16:00", "17:00", "18:00", "19:00"
        });
        UIStyle.styleComboBox(jamDropdown);
        addFormRow("Jam Booking:", jamDropdown, gbc, row++);

        experienceDropdown = new JComboBox<>();
        experienceDropdown.addItem("Pilih Experience");
        UIStyle.styleComboBox(experienceDropdown);
        addFormRow("Pesan Experience:", experienceDropdown, gbc, row++);

        jumlahField = new JTextField(5);
        UIStyle.styleTextField(jumlahField);
        addFormRow("Jumlah Item:", jumlahField, gbc, row++);

        JPanel durasiPanel = new JPanel(new BorderLayout(10, 0));
        durasiPanel.setBackground(UIStyle.CARD_BG);
        durasiField = new JTextField();
        UIStyle.styleTextField(durasiField);
        satuanDurasi = new JComboBox<>(new String[] {"Menit", "Jam"});
        UIStyle.styleComboBox(satuanDurasi);
        durasiPanel.add(durasiField, BorderLayout.CENTER);
        durasiPanel.add(satuanDurasi, BorderLayout.EAST);
        addFormRow("Durasi Penggunaan:", durasiPanel, gbc, row++);

        JCheckBox addOnCheck = new JCheckBox("Include Device Tambahan");
        UIStyle.styleCheckBox(addOnCheck);
        addOnCheck.setBackground(UIStyle.CARD_BG);
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Add-On Device:"), gbc);
        gbc.gridx = 1;
        formPanel.add(addOnCheck, gbc);
        row++;

        deviceFieldPanel = new JPanel(new BorderLayout());
        deviceFieldPanel.setBackground(UIStyle.CARD_BG);
        deviceField = new JTextField(20);
        UIStyle.styleTextField(deviceField);
        deviceFieldPanel.add(deviceField, BorderLayout.CENTER);
        deviceFieldPanel.setVisible(false);

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Jenis Device Tambahan:"), gbc);
        gbc.gridx = 1;
        formPanel.add(deviceFieldPanel, gbc);
        row++;

        // Total Harga Label
        totalHargaLabel = new JLabel("Total Harga: Rp0");
        totalHargaLabel.setFont(UIStyle.fontBold(18));
        totalHargaLabel.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(totalHargaLabel, gbc);
        row++;

        JButton kirimButton = new UIStyle.RoundedButton("Kirim Booking", 10);
        kirimButton.setBackground(UIStyle.PRIMARY);
        kirimButton.setForeground(Color.WHITE);
        kirimButton.setFont(UIStyle.fontBold(16));
        kirimButton.setPreferredSize(new Dimension(200, 50));
        kirimButton.addActionListener(e -> submitBooking());
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(kirimButton, gbc);

        addOnCheck.addItemListener(e -> {
            deviceFieldPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
            formPanel.revalidate();
            formPanel.repaint();
        });

        // Add listeners for price calculation
        experienceDropdown.addActionListener(e -> calculatePrice());
        jumlahField.getDocument().addDocumentListener(new DocumentChangeListener());
        durasiField.getDocument().addDocumentListener(new DocumentChangeListener());
        satuanDurasi.addActionListener(e -> calculatePrice());
    }

    private class DocumentChangeListener implements javax.swing.event.DocumentListener {
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { calculatePrice(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { calculatePrice(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { calculatePrice(); }
    }

    private void calculatePrice() {
        if (experienceDropdown.getSelectedIndex() == 0 || 
            jumlahField.getText().isEmpty() || 
            durasiField.getText().isEmpty()) {
            totalHargaLabel.setText("Total Harga: Rp0");
            return;
        }

        try {
            String selectedExperience = (String) experienceDropdown.getSelectedItem();
            int jumlah = Integer.parseInt(jumlahField.getText());
            int durasi = Integer.parseInt(durasiField.getText());
            
            if (jumlah <= 0 || durasi <= 0) {
                totalHargaLabel.setText("Total Harga: Rp0");
                return;
            }

            // Convert duration to minutes
            int durasiMenit = satuanDurasi.getSelectedItem().equals("Jam") ? durasi * 60 : durasi;
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String getPrice = "SELECT MIN(harga_sewa_menit) as harga FROM aset WHERE kategori = ?";
                PreparedStatement pst = conn.prepareStatement(getPrice);
                pst.setString(1, selectedExperience);
                ResultSet rs = pst.executeQuery();
                
                if (rs.next()) {
                    double hargaPerMenit = rs.getDouble("harga");
                    double totalHarga = hargaPerMenit * durasiMenit * jumlah;
                    totalHargaLabel.setText(String.format("Total Harga: Rp%,.0f", totalHarga));
                }
            }
        } catch (NumberFormatException | SQLException e) {
            totalHargaLabel.setText("Total Harga: Rp0");
        }
    }

    private void addFormRow(String labelText, JComponent field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.fontMedium(16));
        label.setForeground(UIStyle.TEXT);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(label, gbc);
        gbc.gridx = 1;
        formPanel.add(field, gbc);
    }

    private void loadAvailableExperiences() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT DISTINCT kategori FROM aset WHERE status_tersedia = 1")) {
            
            experienceDropdown.removeAllItems();
            experienceDropdown.addItem("Pilih Experience");
            
            while (rs.next()) {
                experienceDropdown.addItem(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data experience: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitBooking() {
        // Validate all fields
        if (namaField.getText().trim().isEmpty() ||
            instagramField.getText().trim().isEmpty() ||
            noHpField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            tanggalField.getDate() == null ||
            experienceDropdown.getSelectedIndex() == 0 ||
            jumlahField.getText().trim().isEmpty() ||
            durasiField.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data yang diperlukan", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedExperience = (String) experienceDropdown.getSelectedItem();
        int jumlah;
        int durasi;
        
        try {
            jumlah = Integer.parseInt(jumlahField.getText());
            durasi = Integer.parseInt(durasiField.getText());
            
            if (jumlah <= 0 || durasi <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah dan durasi harus berupa angka positif", 
                "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert duration to minutes
        int durasiMenit = satuanDurasi.getSelectedItem().equals("Jam") ? durasi * 60 : durasi;
        String jamBooking = jamDropdown.getSelectedItem() + ":00";
        Date tanggalBooking = tanggalField.getDate();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Check time-based availability
            String checkAvailability = "SELECT COUNT(*) as available FROM aset a " +
                                     "WHERE a.kategori = ? AND a.status_tersedia = 1 " +
                                     "AND NOT EXISTS (" +
                                     "  SELECT 1 FROM booking b " +
                                     "  JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                                     "  WHERE bd.id_aset = a.id_aset " +
                                     "  AND b.tanggal = ? " +
                                     "  AND ( " +
                                     "    (TIME(?) BETWEEN b.jam AND ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60))) " +
                                     "    OR (ADDTIME(TIME(?), SEC_TO_TIME(? * 60)) BETWEEN b.jam AND ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60))) " +
                                     "  )" +
                                     ")";
            
            PreparedStatement pstCheck = conn.prepareStatement(checkAvailability);
            pstCheck.setString(1, selectedExperience);
            pstCheck.setDate(2, new java.sql.Date(tanggalBooking.getTime()));
            pstCheck.setString(3, jamBooking);
            pstCheck.setString(4, jamBooking);
            pstCheck.setInt(5, durasiMenit);
            
            ResultSet rs = pstCheck.executeQuery();
            int availableCount = 0;
            if (rs.next()) {
                availableCount = rs.getInt("available");
            }
            
            if (availableCount < jumlah) {
                JOptionPane.showMessageDialog(this, 
                    "Tidak cukup unit yang tersedia pada waktu tersebut. Tersedia: " + availableCount, 
                    "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get price
            String getPrice = "SELECT MIN(harga_sewa_menit) as harga FROM aset WHERE kategori = ?";
            PreparedStatement pstPrice = conn.prepareStatement(getPrice);
            pstPrice.setString(1, selectedExperience);
            ResultSet rsPrice = pstPrice.executeQuery();
            
            double hargaPerMenit = 0;
            if (rsPrice.next()) {
                hargaPerMenit = rsPrice.getDouble("harga");
            }
            
            double totalHarga = hargaPerMenit * durasiMenit * jumlah;
            
            // Insert booking
            String insertBooking = "INSERT INTO booking (nama, instagram, no_hp, email, tanggal, jam, durasi_menit, total_harga) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstBooking = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS);
            
            pstBooking.setString(1, namaField.getText());
            pstBooking.setString(2, instagramField.getText());
            pstBooking.setString(3, noHpField.getText());
            pstBooking.setString(4, emailField.getText());
            pstBooking.setDate(5, new java.sql.Date(tanggalBooking.getTime()));
            pstBooking.setString(6, jamBooking);
            pstBooking.setInt(7, durasiMenit);
            pstBooking.setDouble(8, totalHarga);
            pstBooking.executeUpdate();
            
            // Get generated booking ID
            int bookingId = 0;
            ResultSet generatedKeys = pstBooking.getGeneratedKeys();
            if (generatedKeys.next()) {
                bookingId = generatedKeys.getInt(1);
            }
            
            // Get available assets for this time slot
            String getAssets = checkAvailability.replace("COUNT(*) as available", "a.id_aset") + " LIMIT ?";
            PreparedStatement pstAssets = conn.prepareStatement(getAssets);
            pstAssets.setString(1, selectedExperience);
            pstAssets.setDate(2, new java.sql.Date(tanggalBooking.getTime()));
            pstAssets.setString(3, jamBooking);
            pstAssets.setString(4, jamBooking);
            pstAssets.setInt(5, durasiMenit);
            pstAssets.setInt(6, jumlah);
            
            ResultSet rsAssets = pstAssets.executeQuery();
            
            // Insert booking details
            String insertDetail = "INSERT INTO booking_detail (id_booking, id_aset, jumlah, add_on, subtotal) " +
                                "VALUES (?, ?, 1, 0, ?)";
            PreparedStatement pstDetail = conn.prepareStatement(insertDetail);
            
            while (rsAssets.next()) {
                String assetId = rsAssets.getString("id_aset");
                pstDetail.setInt(1, bookingId);
                pstDetail.setString(2, assetId);
                pstDetail.setDouble(3, hargaPerMenit * durasiMenit);
                pstDetail.addBatch();
            }
            
            pstDetail.executeBatch();
            
            // Handle add-on if selected
            if (deviceFieldPanel.isVisible() && !deviceField.getText().trim().isEmpty()) {
                String insertAddOn = "INSERT INTO booking_detail (id_booking, id_aset, jumlah, add_on, subtotal) " +
                                    "VALUES (?, NULL, 1, 1, 0)";
                PreparedStatement pstAddOn = conn.prepareStatement(insertAddOn);
                pstAddOn.setInt(1, bookingId);
                pstAddOn.executeUpdate();
            }
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, 
                "Booking berhasil dibuat!", 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            resetForm();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Gagal menyimpan booking: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        namaField.setText("");
        instagramField.setText("");
        noHpField.setText("");
        emailField.setText("");
        tanggalField.setDate(null);
        jamDropdown.setSelectedIndex(0);
        experienceDropdown.setSelectedIndex(0);
        jumlahField.setText("");
        durasiField.setText("");
        satuanDurasi.setSelectedIndex(0);
        deviceField.setText("");
        deviceFieldPanel.setVisible(false);
        totalHargaLabel.setText("Total Harga: Rp0");
    }
}