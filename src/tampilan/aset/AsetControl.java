package tampilan.aset;

import connection.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.border.EmptyBorder;
import tampilan.util.UIStyle;

public class AsetControl extends JPanel {

    private JTextField idField, namaField, kodeField, jumlahBarangField, hargaPerMenitField, hargaPerHariField;
    private JComboBox<String> kategoriCombo;
    private JTextArea deskripsiArea;
    private JCheckBox tersediaCheckbox, disewakanCheckbox;
    private JButton tambahButton;

    public AsetControl() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        UIStyle.RoundedPanel mainPanel = new UIStyle.RoundedPanel(20);
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(UIStyle.CARD_BG);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Tambah Aset Baru");
        title.setFont(UIStyle.fontBold(24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(UIStyle.PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Auto-generate ID aset saat form dibuat
        idField = new JTextField(generateIdAset());
        idField.setEditable(false);
        namaField = new JTextField();
        kodeField = new JTextField(generateKodeBarang("PC GAMING") + "000");
        kodeField.setEditable(false);
        kategoriCombo = new JComboBox<>(new String[]{
            "PC GAMING", "RACING SIMULATOR", "MOTION RACING SIMULATOR", "FLIGHT SIMULATOR",
            "VIP DUOS", "VIP SQUAD", "PS5 OPEN SPACE", "PLAYSTATION 5", "PSVR 2",
            "VR OPULUS META SQUES 2", "VR OPULUS META SQUES 3", "XBOX S SERIES", "NINTENDO SWITCH CADANGAN"
        });
        kategoriCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String kategori = kategoriCombo.getSelectedItem().toString();
                String prefix = generateKodeBarang(kategori);
                int nextNumber = 1;

                String query = "SELECT kode_barang FROM aset WHERE kode_barang LIKE ? ORDER BY kode_barang DESC LIMIT 1";

                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pst = conn.prepareStatement(query)) {

                    pst.setString(1, prefix + "%");
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        String lastKode = rs.getString("kode_barang");
                        if (lastKode != null && lastKode.startsWith(prefix)) {
                            String numberPart = lastKode.substring(prefix.length());
                            nextNumber = Integer.parseInt(numberPart) + 1;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    UIStyle.showSuccessMessage(AsetControl.this, "Gagal menghasilkan Kode Barang: " + ex.getMessage());
                }

                kodeField.setText(prefix + String.format("%03d", nextNumber));
            }
        });
        deskripsiArea = new JTextArea(3, 20);
        jumlahBarangField = new JTextField();
        hargaPerMenitField = new JTextField();
        hargaPerHariField = new JTextField();
        tersediaCheckbox = new JCheckBox("Tersedia");
        disewakanCheckbox = new JCheckBox("Disewakan");

        tambahButton = UIStyle.modernButton("Tambah Aset");
        tambahButton.setBackground(UIStyle.PRIMARY);

        // Apply styles to components
        UIStyle.styleTextField(idField);
        UIStyle.styleTextField(namaField);
        UIStyle.styleTextField(kodeField);
        UIStyle.styleComboBox(kategoriCombo);
        UIStyle.styleTextArea(deskripsiArea);
        UIStyle.styleTextField(jumlahBarangField);
        UIStyle.styleTextField(hargaPerMenitField);
        UIStyle.styleTextField(hargaPerHariField);
        UIStyle.styleCheckBox(tersediaCheckbox);
        UIStyle.styleCheckBox(disewakanCheckbox);

        // Terapkan validasi real-time
        addValidationListener(namaField);
        addValidationListener(jumlahBarangField);
        addValidationListener(hargaPerMenitField);
        addValidationListener(hargaPerHariField);

        // Add components to form with proper spacing
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(createLabel("ID Aset"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Nama Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(namaField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Kode Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(kodeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Kategori"), gbc);
        gbc.gridx = 1;
        formPanel.add(kategoriCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createLabel("Deskripsi"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(deskripsiArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Jumlah Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(jumlahBarangField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Harga per Menit"), gbc);
        gbc.gridx = 1;
        formPanel.add(hargaPerMenitField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(createLabel("Harga per Hari"), gbc);
        gbc.gridx = 1;
        formPanel.add(hargaPerHariField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(createLabel("Status"), gbc);
        gbc.gridx = 1;
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(tersediaCheckbox);
        statusPanel.add(disewakanCheckbox);
        formPanel.add(statusPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(tambahButton, gbc);
        
        // Add glue to push everything up and prevent empty space
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        gbc.weighty = 1.0; // This will take any extra vertical space
        formPanel.add(Box.createVerticalGlue(), gbc);

        // Create a scroll pane for the form panel
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIStyle.CARD_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Remove the default border from the scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        disewakanCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = disewakanCheckbox.isSelected();
                hargaPerHariField.setEnabled(selected);
                if (!selected) {
                    hargaPerHariField.setText(""); // Kosongkan inputan
                    // Kembalikan border ke normal jika tidak dipilih
                    UIStyle.styleTextField(hargaPerHariField);
                } else {
                    // Jika dipilih dan kosong, langsung tandai sebagai error
                    if (hargaPerHariField.getText().trim().isEmpty()) {
                        hargaPerHariField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
                    }
                }
            }
        });

        // Tambah highlight merah untuk field kosong dan validasi manual
        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Validasi input (sudah bagus, kita pertahankan)
                if (!validateInput()) {
                    UIStyle.showErrorMessage(AsetControl.this, "Mohon lengkapi semua kolom yang wajib diisi.");
                    return;
                }
        
                // Deklarasi dan parsing nilai input
                String namaBarang = namaField.getText();
                String kategori = kategoriCombo.getSelectedItem().toString();
                String deskripsi = deskripsiArea.getText();
                
                int jumlah;
                double hargaMenit;
                double hargaHari = 0.0;
        
                try {
                    jumlah = Integer.parseInt(jumlahBarangField.getText());
                    hargaMenit = Double.parseDouble(hargaPerMenitField.getText());
                    if (disewakanCheckbox.isSelected()) {
                        hargaHari = Double.parseDouble(hargaPerHariField.getText());
                    }
                } catch (NumberFormatException ex) {
                    UIStyle.showErrorMessage(AsetControl.this, "Input jumlah atau harga harus berupa angka yang valid.");
                    return;
                }
        
                // Ambil nilai dari checkbox
                boolean isTersedia = tersediaCheckbox.isSelected();
                boolean isDisewakan = disewakanCheckbox.isSelected();
        
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // PERBAIKAN: Sertakan status_tersedia dan status_disewakan dalam INSERT
                    String sql = "INSERT INTO aset (id_aset, nama_barang, kode_barang, kategori, deskripsi, harga_sewa_menit, harga_sewa_hari, status_tersedia, status_disewakan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    // Logika untuk mendapatkan nomor urut terakhir dari ID Aset dan Kode Barang
                    int lastIdNumber = getLastNumberFromDb(conn, "id_aset", "AST");
                    String kodePrefix = generateKodeBarang(kategori);
                    int lastKodeNumber = getLastNumberFromDb(conn, "kode_barang", kodePrefix);
        
                    for (int i = 1; i <= jumlah; i++) {
                        String finalId = "AST" + String.format("%03d", lastIdNumber + i);
                        String finalKode = kodePrefix + String.format("%03d", lastKodeNumber + i);
                        String finalNama = namaBarang;
                        // Jika jumlah lebih dari 1, tambahkan nomor unik pada nama
                        if (jumlah > 1) {
                            finalNama = namaBarang + " " + String.format("%02d", lastKodeNumber + i);
                        }
        
                        try (PreparedStatement pst = conn.prepareStatement(sql)) {
                            pst.setString(1, finalId);
                            pst.setString(2, finalNama);
                            pst.setString(3, finalKode);
                            pst.setString(4, kategori);
                            pst.setString(5, deskripsi);
                            pst.setDouble(6, hargaMenit);
                            pst.setDouble(7, hargaHari);
                            // PERBAIKAN: Set nilai status berdasarkan checkbox
                            pst.setInt(8, isTersedia ? 1 : 0);
                            pst.setInt(9, isDisewakan ? 1 : 0);
                            
                            pst.executeUpdate();
                        }
                    }
        
                    UIStyle.showSuccessMessage(AsetControl.this, jumlah + " aset berhasil ditambahkan!");
                    clearForm();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    UIStyle.showSuccessMessage(AsetControl.this, "Gagal menambahkan aset: " + ex.getMessage());
                }
            }
        });
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.fontMedium(14));
        label.setForeground(UIStyle.TEXT);
        return label;
    }

    private boolean validateInput() {
        boolean valid = true;
        if (namaField.getText().trim().isEmpty()) {
            namaField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(namaField);
        }
    
        if (jumlahBarangField.getText().trim().isEmpty()) {
            jumlahBarangField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(jumlahBarangField);
        }
        
        if (hargaPerMenitField.getText().trim().isEmpty()) {
            hargaPerMenitField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(hargaPerMenitField);
        }
    
        if (disewakanCheckbox.isSelected() && hargaPerHariField.getText().trim().isEmpty()) {
            hargaPerHariField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(hargaPerHariField);
        }
        
        return valid;
    }

    private int getLastNumberFromDb(Connection conn, String columnName, String prefix) throws SQLException {
        int lastNumber = 0;
        String query = "SELECT " + columnName + " FROM aset WHERE " + columnName + " LIKE ? ORDER BY " + columnName + " DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String lastValue = rs.getString(columnName);
                if (lastValue != null && lastValue.startsWith(prefix)) {
                    String numberPart = lastValue.substring(prefix.length());
                    if (numberPart.matches("\\d+")) { // Pastikan bagian angka valid
                        lastNumber = Integer.parseInt(numberPart);
                    }
                }
            }
        }
        return lastNumber;
    }

    private void clearForm() {
        idField.setText(generateIdAset());
        namaField.setText("");
        kodeField.setText("");
        kategoriCombo.setSelectedIndex(0);
        deskripsiArea.setText("");
        jumlahBarangField.setText("");
        hargaPerMenitField.setText("");
        hargaPerHariField.setText("");
        tersediaCheckbox.setSelected(false);
        disewakanCheckbox.setSelected(false);
        hargaPerHariField.setEnabled(false);
    }

    private String generateIdAset() {
        String prefix = "AST";
        int counter = 1;
        // Menggunakan try-with-resources untuk memastikan koneksi dan statement ditutup
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT id_aset FROM aset ORDER BY id_aset DESC LIMIT 1");
            ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id_aset");
                if (lastId != null && lastId.startsWith(prefix)) {
                    String numberPart = lastId.substring(prefix.length());
                    counter = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Tampilkan pesan error jika gagal generate ID
            UIStyle.showErrorMessage(this, "Gagal menghasilkan ID Aset baru: " + e.getMessage());
        }
        return prefix + String.format("%03d", counter);
    }

    private String generateKodeBarang(String kategori) {
        String prefix;
        switch (kategori.toUpperCase()) {
            case "PC GAMING": prefix = "PC"; break;
            case "MOTION RACING SIMULATOR": prefix = "MRS"; break;
            case "RACING SIMULATOR": prefix = "RS"; break;
            case "FLIGHT SIMULATOR": prefix = "FS"; break;
            case "VIP DUOS": prefix = "VD"; break;
            case "VIP SQUAD": prefix = "VS"; break;
            case "PS5 OPEN SPACE": prefix = "PO"; break;
            case "PLAYSTATION 5": prefix = "PS5"; break;
            case "PSVR 2": prefix = "PV2"; break;
            case "VR OPULUS META SQUES 2": prefix = "VR2"; break;
            case "VR OPULUS META SQUES 3": prefix = "VR3"; break;
            case "XBOX S SERIES": prefix = "XS"; break;
            case "NINTENDO SWITCH CADANGAN": prefix = "NS"; break;
            default: prefix = "KD"; break;
        }
        return prefix;
    }
    
    // Custom scroll bar UI for a modern look
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private static final int SCROLL_BAR_ALPHA_ROLLOVER = 100;
        private static final int SCROLL_BAR_ALPHA = 60;
        private static final int THUMB_SIZE = 8;
        private static final Color THUMB_COLOR = UIStyle.PRIMARY;

        public ModernScrollBarUI() {
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Paint no track
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
            int orientation = scrollbar.getOrientation();
            int x = thumbBounds.x;
            int y = thumbBounds.y;

            int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE : thumbBounds.width;
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ? thumbBounds.height : THUMB_SIZE;
            height = Math.max(height, THUMB_SIZE);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(THUMB_COLOR.getRed(), THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
            graphics2D.fillRoundRect(x, y, width, height, 10, 10);
            graphics2D.dispose();
        }

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            scrollbar.repaint();
        }
    }
    // Tambahkan metode baru ini di dalam kelas AsetControl
    private void addValidationListener(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
                } else {
                    // Kembalikan ke style normal jika sudah diisi
                    UIStyle.styleTextField(field);
                }
            }
        });
    }
}