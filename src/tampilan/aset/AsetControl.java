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

        UIStyle.RoundedPanel mainPanel = new UIStyle.RoundedPanel(25);
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Submit Aset");
        title.setFont(UIStyle.fontBold(24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(UIStyle.PRIMARY);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setOpaque(false);

        // Auto-generate ID aset saat form dibuat
        idField = new JTextField(generateIdAset());
        idField.setEditable(false);
        namaField = new JTextField();
        kodeField = new JTextField(generateKodeBarang("PC GAMING") + "000");
        kodeField.setEditable(false);
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

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT kode_barang FROM aset WHERE kode_barang LIKE '" + prefix + "%' ORDER BY kode_barang DESC LIMIT 1");
            if (rs.next()) {
                String lastKode = rs.getString("kode_barang");
                if (lastKode != null && lastKode.startsWith(prefix)) {
                    String numberPart = lastKode.substring(prefix.length());
                    nextNumber = Integer.parseInt(numberPart) + 1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

        tambahButton = new JButton("Tambah Aset");
        tambahButton.setBackground(UIStyle.PRIMARY);
        tambahButton.setForeground(UIStyle.PRIMARY);
        tambahButton.setFocusPainted(false);

        formPanel.add(new JLabel("ID Aset")); formPanel.add(idField);
        formPanel.add(new JLabel("Nama Barang")); formPanel.add(namaField);
        formPanel.add(new JLabel("Kode Barang")); formPanel.add(kodeField);
        formPanel.add(new JLabel("Kategori")); formPanel.add(kategoriCombo);
        formPanel.add(new JLabel("Deskripsi")); formPanel.add(new JScrollPane(deskripsiArea));
        formPanel.add(new JLabel("Jumlah Barang")); formPanel.add(jumlahBarangField);
        formPanel.add(new JLabel("Harga per Menit")); formPanel.add(hargaPerMenitField);
        formPanel.add(new JLabel("Harga per Hari")); formPanel.add(hargaPerHariField);
        formPanel.add(new JLabel("Status Tersedia")); formPanel.add(tersediaCheckbox);
        formPanel.add(new JLabel("Status Disewakan")); formPanel.add(disewakanCheckbox);
        formPanel.add(new JLabel("")); formPanel.add(tambahButton);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        disewakanCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = disewakanCheckbox.isSelected();
                hargaPerHariField.setEnabled(selected);
                if (!selected) {
                    hargaPerHariField.setText(""); // Kosongkan inputan jika checkbox tidak dipilih
                }
            }
        });

        hargaPerHariField.setEnabled(false);

        // Tambah highlight merah untuk field kosong dan validasi manual
        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Validasi input (sudah bagus, kita pertahankan)
                if (!validateInput()) {
                    JOptionPane.showMessageDialog(AsetControl.this, "Mohon lengkapi semua kolom yang wajib diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
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
                    JOptionPane.showMessageDialog(AsetControl.this, "Input jumlah atau harga harus berupa angka yang valid.", "Format Salah", JOptionPane.WARNING_MESSAGE);
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
        
                    JOptionPane.showMessageDialog(AsetControl.this, jumlah + " aset berhasil ditambahkan!");
                    clearForm();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AsetControl.this, "Gagal menambahkan aset: " + ex.getMessage());
                }
            }
        });
    }

    private boolean validateInput() {
        boolean valid = true;
        if (namaField.getText().trim().isEmpty()) {
            namaField.setBorder(BorderFactory.createLineBorder(Color.RED));
            valid = false;
        } else {
            namaField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        }
    
        if (jumlahBarangField.getText().trim().isEmpty()) {
            jumlahBarangField.setBorder(BorderFactory.createLineBorder(Color.RED));
            valid = false;
        } else {
            jumlahBarangField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        }
        
        if (hargaPerMenitField.getText().trim().isEmpty()) {
            hargaPerMenitField.setBorder(BorderFactory.createLineBorder(Color.RED));
            valid = false;
        } else {
            hargaPerMenitField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        }
    
        if (disewakanCheckbox.isSelected() && hargaPerHariField.getText().trim().isEmpty()) {
            hargaPerHariField.setBorder(BorderFactory.createLineBorder(Color.RED));
            valid = false;
        } else {
            hargaPerHariField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
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
    try (Connection conn = DatabaseConnection.getConnection()) {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id_aset FROM aset ORDER BY id_aset DESC LIMIT 1");
        if (rs.next()) {
            String lastId = rs.getString("id_aset");
            if (lastId != null && lastId.startsWith(prefix)) {
                String numberPart = lastId.substring(prefix.length());
                counter = Integer.parseInt(numberPart) + 1;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
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
}
