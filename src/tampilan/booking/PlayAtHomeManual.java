package tampilan.booking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import connection.DatabaseConnection;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import tampilan.util.UIStyle;
import java.util.List;
import java.util.ArrayList;

public class PlayAtHomeManual extends JPanel {
    private JScrollPane itemScroll;
    private DefaultTableModel itemModel;
    private JPanel formWrapper;
    private JComboBox<String> itemCombo;
    private JLabel countLabel;
    private JComboBox<String> kategoriCombo;
    private JTextField hargaField, ongkirField, totalField;
    private JDateChooser dariDate, sampaiDate;

    public PlayAtHomeManual() {
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(600, 500));
        JPanel contentPanel = new JPanel(new BorderLayout());
        scrollPane.setViewportView(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
        initComponents();
    }

    private void initComponents() {
        this.formWrapper = new UIStyle.RoundedPanel(20);
        formWrapper.setLayout(new GridBagLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBorder(new EmptyBorder(30, 40, 30, 40));
    
        UIStyle.RoundedPanel headerPanel = new UIStyle.RoundedPanel(20);
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setLayout(new BorderLayout());
    
        JPanel contentPanel = (JPanel) ((JScrollPane) getComponent(0)).getViewport().getView();
        contentPanel.setLayout(new BorderLayout());
        
        JLabel headerLabel = new JLabel("PLAY AT HOME BYEBELI", SwingConstants.CENTER);
        headerLabel.setFont(UIStyle.fontBold(28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
    
        JTextField lokasiField = new JTextField();
        JTextField namaField = new JTextField();
        JTextField instagramField = new JTextField();
    
        this.itemModel = new DefaultTableModel(new String[]{"Barang", "Jumlah", "Harga Per Hari", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Integer.class : String.class;
            }
        };
        JTable itemTable = new JTable(this.itemModel);
    
        this.itemScroll = new JScrollPane(itemTable);
        itemScroll.setPreferredSize(new Dimension(400, 150));
    
        this.kategoriCombo = new JComboBox<>();
        this.itemCombo = new JComboBox<>();
        
        this.countLabel = new JLabel();
        JTextField jumlahField = new JTextField(5);
        JButton tambahItemButton = new JButton("Tambah Barang");
        this.dariDate = new JDateChooser();
        this.sampaiDate = new JDateChooser();

        JComboBox<String> metodeCombo = new JComboBox<>(new String[]{"Pick Up", "Delivery"});
        JTextField alamatAntarField = new JTextField();
        JTextField alamatKembaliField = new JTextField();
        JTextField keperluanField = new JTextField();

        this.hargaField = new JTextField();
        this.ongkirField = new JTextField();
        this.totalField = new JTextField();
        
        // Set date change listeners
        dariDate.addPropertyChangeListener("date", e -> calculateTotalPrice());
        sampaiDate.addPropertyChangeListener("date", e -> calculateTotalPrice());
        ongkirField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotalPrice(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotalPrice(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotalPrice(); }
        });
    
        int row = 0;
        addField(formWrapper, gbc, row++, "Lokasi:", lokasiField);
        addField(formWrapper, gbc, row++, "Nama Penyewa:", namaField);
        addField(formWrapper, gbc, row++, "Akun Instagram:", instagramField);
        addField(formWrapper, gbc, row++, "Pilih Kategori:", kategoriCombo);
        JPanel itemPanel = new JPanel(new BorderLayout(5, 0));
        itemPanel.add(itemCombo, BorderLayout.CENTER);
        itemPanel.add(countLabel, BorderLayout.EAST);
        addField(formWrapper, gbc, row++, "Pilih Barang:", itemPanel);
    
        addField(formWrapper, gbc, row++, "Jumlah:", jumlahField);
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(tambahItemButton, gbc);

        JButton hapusItemButton = new JButton("Hapus Barang");
        hapusItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = itemTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Silakan pilih barang yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String barangDihapus = itemModel.getValueAt(selectedRow, 0).toString();
                itemModel.removeRow(selectedRow);
                
                // Add item back to combo box if not already present
                boolean itemExists = false;
                for (int i = 0; i < itemCombo.getItemCount(); i++) {
                    if (itemCombo.getItemAt(i).equals(barangDihapus)) {
                        itemExists = true;
                        break;
                    }
                }
                if (!itemExists) {
                    itemCombo.addItem(barangDihapus);
                }
                
                updateAvailableCount(kategoriCombo.getSelectedItem().toString());
                calculateTotalPrice();
                updateTableVisibility();
            }
        });

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(hapusItemButton, gbc);

        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = row++;
        formWrapper.add(this.itemScroll, gbc);
        addField(formWrapper, gbc, row++, "Tanggal Mulai:", dariDate);
        addField(formWrapper, gbc, row++, "Tanggal Selesai:", sampaiDate);
        addField(formWrapper, gbc, row++, "Metode Pengambilan:", metodeCombo);
        addField(formWrapper, gbc, row++, "Alamat Antar:", alamatAntarField);
        addField(formWrapper, gbc, row++, "Alamat Kembali:", alamatKembaliField);
        addField(formWrapper, gbc, row++, "Keperluan:", keperluanField);
        addField(formWrapper, gbc, row++, "Harga Sewa:", hargaField);
        addField(formWrapper, gbc, row++, "Ongkir:", ongkirField);
        addField(formWrapper, gbc, row++, "Total:", totalField);
    
        JButton submitButton = new JButton("Simpan");
        submitButton.setBackground(UIStyle.PRIMARY);
        submitButton.setForeground(Color.BLACK);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        formWrapper.add(submitButton, gbc);

        JButton viewRentalsButton = new JButton("Lihat Penyewaan Aktif");
        viewRentalsButton.setBackground(UIStyle.PRIMARY);
        viewRentalsButton.setForeground(Color.BLACK);
        viewRentalsButton.addActionListener(e -> {
        javaapplication1.MainFrame.setPage(
            new tampilan.utama.SidebarShell(new PlayAtHomeRentalPanel()), 
            "playathome-rentals"
        );
    });
    
    gbc.gridx = 0;
    gbc.gridy = row++;
    formWrapper.add(viewRentalsButton, gbc);

    contentPanel.add(headerPanel, BorderLayout.NORTH);
    contentPanel.add(formWrapper, BorderLayout.CENTER);

    
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(formWrapper, BorderLayout.CENTER);
    
        loadCategories();
        
        kategoriCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedKategori = (kategoriCombo.getSelectedItem() != null) ? kategoriCombo.getSelectedItem().toString() : "";
                itemCombo.removeAllItems();
                countLabel.setText("");
            
                if (selectedKategori.isEmpty()) return;
                loadAvailableItems(selectedKategori);
            }
        });
        
        if (kategoriCombo.getItemCount() > 0) {
            kategoriCombo.setSelectedIndex(0);
        }
        updateTableVisibility();

        tambahItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (itemCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(null, "Silakan pilih barang terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
        
                String selectedItemName = itemCombo.getSelectedItem().toString();
                int jumlahDiminta;
                try {
                    jumlahDiminta = Integer.parseInt(jumlahField.getText());
                    if (jumlahDiminta <= 0) {
                        JOptionPane.showMessageDialog(null, "Jumlah harus lebih dari 0.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Jumlah harus berupa angka.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
        
                double hargaPerHari = 0.0;
                int jumlahTersediaDiDb = 0;
        
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String queryStok = "SELECT COUNT(*) as total, MIN(harga_sewa_hari) as harga FROM aset WHERE nama_barang = ? AND status_tersedia = 1 AND status_disewakan = 1";
                    PreparedStatement pstStok = conn.prepareStatement(queryStok);
                    pstStok.setString(1, selectedItemName);
                    ResultSet rs = pstStok.executeQuery();
        
                    if (rs.next()) {
                        jumlahTersediaDiDb = rs.getInt("total");
                        hargaPerHari = rs.getDouble("harga");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Gagal mengambil data stok dari database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                // Check stock already in table
                int jumlahSudahDitabel = 0;
                for (int i = 0; i < itemModel.getRowCount(); i++) {
                    if (itemModel.getValueAt(i, 0).toString().equals(selectedItemName)) {
                        jumlahSudahDitabel += Integer.parseInt(itemModel.getValueAt(i, 1).toString());
                    }
                }
        
                if ((jumlahSudahDitabel + jumlahDiminta) > jumlahTersediaDiDb) {
                    JOptionPane.showMessageDialog(null, "Jumlah barang yang diminta (" + (jumlahSudahDitabel + jumlahDiminta) + ") melebihi stok yang tersedia (" + jumlahTersediaDiDb + ").", "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Add item to table
                double subtotal = jumlahDiminta * hargaPerHari;
                itemModel.addRow(new Object[]{selectedItemName, jumlahDiminta, hargaPerHari, subtotal});
                
                // Remove item from combo box if all available items are selected
                if ((jumlahSudahDitabel + jumlahDiminta) == jumlahTersediaDiDb) {
                    itemCombo.removeItem(selectedItemName);
                }
                
                updateAvailableCount(kategoriCombo.getSelectedItem().toString());
                calculateTotalPrice();
                jumlahField.setText("");
                updateTableVisibility();
            }
        });


        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Validate required fields
                if (namaField.getText().trim().isEmpty() || 
                    lokasiField.getText().trim().isEmpty() ||
                    dariDate.getDate() == null ||
                    sampaiDate.getDate() == null ||
                    itemModel.getRowCount() == 0) {
                    
                    JOptionPane.showMessageDialog(null, "Harap lengkapi semua data yang diperlukan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Insert playathome record
                    String insertPlayHome = "INSERT INTO playathome (nama, lokasi, instagram, tgl_mulai, tgl_selesai, metode_pengambilan, alamat_antar, alamat_kembali, keperluan, ongkir, total_harga, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'aktif')";
                    PreparedStatement pst = conn.prepareStatement(insertPlayHome, Statement.RETURN_GENERATED_KEYS);

                    pst.setString(1, namaField.getText());
                    pst.setString(2, lokasiField.getText());
                    pst.setString(3, instagramField.getText());
                    pst.setDate(4, new java.sql.Date(dariDate.getDate().getTime()));
                    pst.setDate(5, new java.sql.Date(sampaiDate.getDate().getTime()));
                    pst.setString(6, metodeCombo.getSelectedItem().toString());
                    pst.setString(7, alamatAntarField.getText());
                    pst.setString(8, alamatKembaliField.getText());
                    pst.setString(9, keperluanField.getText());
                    pst.setDouble(10, ongkirField.getText().isEmpty() ? 0 : Double.parseDouble(ongkirField.getText()));
                    pst.setDouble(11, Double.parseDouble(totalField.getText()));
                    pst.executeUpdate();

                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    int idPlayhome = 0;
                    if (generatedKeys.next()) {
                        idPlayhome = generatedKeys.getInt(1);
                    }

                    // Insert playathome_detail and update asset availability
                    for (int i = 0; i < itemModel.getRowCount(); i++) {
                        String barang = itemModel.getValueAt(i, 0).toString();
                        int jumlah = Integer.parseInt(itemModel.getValueAt(i, 1).toString());
                        double hargaPerHari = Double.parseDouble(itemModel.getValueAt(i, 2).toString());

                        // Get available assets
                        String idQuery = "SELECT id_aset FROM aset WHERE nama_barang = ? AND status_tersedia = 1 AND status_disewakan = 1 LIMIT ?";
                        PreparedStatement getId = conn.prepareStatement(idQuery);
                        getId.setString(1, barang);
                        getId.setInt(2, jumlah);
                        ResultSet idRs = getId.executeQuery();

                        List<String> assetIds = new ArrayList<>();
                        while (idRs.next()) {
                            assetIds.add(idRs.getString("id_aset"));
                        }

                        if (assetIds.size() < jumlah) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(null, "Stok barang " + barang + " tidak mencukupi.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Insert detail records and update asset status
                        for (String idAset : assetIds) {
                            // Insert detail
                            PreparedStatement detailStmt = conn.prepareStatement("INSERT INTO playathome_detail (id_playhome, id_aset, jumlah, subtotal) VALUES (?, ?, ?, ?)");
                            detailStmt.setInt(1, idPlayhome);
                            detailStmt.setString(2, idAset);
                            detailStmt.setInt(3, 1); // Each record is for 1 asset
                            detailStmt.setDouble(4, hargaPerHari);
                            detailStmt.executeUpdate();

                            // Update asset status
                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE aset SET status_tersedia = 0 WHERE id_aset = ?");
                            updateStmt.setString(1, idAset);
                            updateStmt.executeUpdate();
                        }
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(null, "Penyimpanan berhasil.");
                    resetForm();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Gagal menyimpan data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT kategori FROM aset")) {
            while (rs.next()) {
                kategoriCombo.addItem(rs.getString("kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableItems(String kategori) {
        String queryBarang = "SELECT nama_barang FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1";
        String queryCount = "SELECT COUNT(id_aset) as total FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1";
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstBarang = conn.prepareStatement(queryBarang);
            pstBarang.setString(1, kategori);
            ResultSet rsBarang = pstBarang.executeQuery();
            while (rsBarang.next()) {
                itemCombo.addItem(rsBarang.getString("nama_barang"));
            }
    
            PreparedStatement pstCount = conn.prepareStatement(queryCount);
            pstCount.setString(1, kategori);
            ResultSet rsCount = pstCount.executeQuery();
            if (rsCount.next()) {
                int total = rsCount.getInt("total");
                countLabel.setText(" (" + total + " tersedia)");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateAvailableCount(String kategori) {
        if (kategori == null || kategori.isEmpty()) return;
        
        String queryCount = "SELECT nama_barang, COUNT(*) as total FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1 GROUP BY nama_barang";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstCount = conn.prepareStatement(queryCount);
            pstCount.setString(1, kategori);
            ResultSet rsCount = pstCount.executeQuery();
            
            int totalAvailable = 0;
            while (rsCount.next()) {
                String namaBarang = rsCount.getString("nama_barang");
                int countInDb = rsCount.getInt("total");
                
                // Subtract items already in the table
                for (int i = 0; i < itemModel.getRowCount(); i++) {
                    if (itemModel.getValueAt(i, 0).toString().equals(namaBarang)) {
                        countInDb -= Integer.parseInt(itemModel.getValueAt(i, 1).toString());
                    }
                }
                
                totalAvailable += countInDb;
            }
            
            countLabel.setText(" (" + totalAvailable + " tersedia)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void calculateTotalPrice() {
        if (dariDate.getDate() == null || sampaiDate.getDate() == null) {
            return;
        }

        long diff = sampaiDate.getDate().getTime() - dariDate.getDate().getTime();
        int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1; // Add 1 to include both start and end days

        if (days <= 0) {
            JOptionPane.showMessageDialog(null, "Tanggal selesai harus setelah tanggal mulai.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalHargaBarang = 0.0;
        for (int i = 0; i < itemModel.getRowCount(); i++) {
            int jumlah = Integer.parseInt(itemModel.getValueAt(i, 1).toString());
            double hargaPerHari = Double.parseDouble(itemModel.getValueAt(i, 2).toString());
            double subtotal = jumlah * hargaPerHari * days;
            itemModel.setValueAt(subtotal, i, 3); // Update subtotal column
            totalHargaBarang += subtotal;
        }
        hargaField.setText(String.valueOf(totalHargaBarang));
        
        double ongkir = ongkirField.getText().isEmpty() ? 0 : Double.parseDouble(ongkirField.getText());
        totalField.setText(String.valueOf(totalHargaBarang + ongkir));
    }

    private void resetForm() {
        itemModel.setRowCount(0);
        kategoriCombo.setSelectedIndex(0);
        countLabel.setText("");
        hargaField.setText("");
        ongkirField.setText("");
        totalField.setText("");
        updateTableVisibility();
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void updateTableVisibility() {
        itemScroll.setVisible(itemModel.getRowCount() > 0);
        formWrapper.revalidate();
        formWrapper.repaint();
    }
}